package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.entity.InvitationActivityMemberCard;
import com.xiliulou.electricity.mapper.InvitationActivityMapper;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.service.InvitationActivityMemberCardService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (InvitationActivity)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-01 15:55:48
 */
@Service("invitationActivityService")
@Slf4j
public class InvitationActivityServiceImpl implements InvitationActivityService {
    @Resource
    private InvitationActivityMapper invitationActivityMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private InvitationActivityMemberCardService invitationActivityMemberCardService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivity queryByIdFromDB(Long id) {
        return this.invitationActivityMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivity queryByIdFromCache(Long id) {

        InvitationActivity cacheInvitationActivity = redisService.getWithHash(CacheConstant.CACHE_INVITATION_ACTIVITY + id, InvitationActivity.class);
        if (Objects.nonNull(cacheInvitationActivity)) {
            return cacheInvitationActivity;
        }

        InvitationActivity invitationActivity = this.queryByIdFromDB(id);
        if (Objects.isNull(invitationActivity)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_INVITATION_ACTIVITY + id, invitationActivity);

        return invitationActivity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(InvitationActivityQuery query) {
        InvitationActivity invitationActivity = new InvitationActivity();
        BeanUtils.copyProperties(query, invitationActivity);
        invitationActivity.setDelFlag(InvitationActivity.DEL_NORMAL);
        invitationActivity.setOperateUid(SecurityUtils.getUid());
        invitationActivity.setType(InvitationActivity.TYPE_DEFAULT);
        invitationActivity.setTenantId(TenantContextHolder.getTenantId());
        invitationActivity.setCreateTime(System.currentTimeMillis());
        invitationActivity.setUpdateTime(System.currentTimeMillis());
        Integer insert = this.insert(invitationActivity);

        if (insert > 0) {
            List<InvitationActivityMemberCard> shareActivityMemberCards = buildShareActivityMemberCard(invitationActivity.getId(), query.getMembercardIds());
            if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
                invitationActivityMemberCardService.batchInsert(shareActivityMemberCards);
            }
        }

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modify(InvitationActivityQuery query) {
        InvitationActivity invitationActivity = this.queryByIdFromCache(query.getId());
        if(Objects.isNull(invitationActivity) || !Objects.equals( invitationActivity.getTenantId(),TenantContextHolder.getTenantId() )){
            return Triple.of(false,"100390","活动不存在");
        }

        InvitationActivity invitationActivityUpdate = new InvitationActivity();
        invitationActivityUpdate.setId(query.getId());
        invitationActivityUpdate.setName(query.getName());
        invitationActivityUpdate.setHours(query.getHours());
        invitationActivityUpdate.setFirstReward(query.getFirstReward());
        invitationActivityUpdate.setOtherReward(query.getOtherReward());
        invitationActivityUpdate.setUpdateTime(System.currentTimeMillis());
        Integer update = this.update(invitationActivityUpdate);

        if(update>0){
            //删除绑定的套餐
            invitationActivityMemberCardService.deleteByActivityId(query.getId());

            List<InvitationActivityMemberCard> shareActivityMemberCards = buildShareActivityMemberCard(query.getId(), query.getMembercardIds());
            if (CollectionUtils.isNotEmpty(shareActivityMemberCards)) {
                invitationActivityMemberCardService.batchInsert(shareActivityMemberCards);
            }
        }

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> updateStatus(InvitationActivityStatusQuery query) {
        InvitationActivity invitationActivity = this.queryByIdFromCache(query.getId());
        if(Objects.isNull(invitationActivity) || !Objects.equals( invitationActivity.getTenantId(),TenantContextHolder.getTenantId() )){
            return Triple.of(false,"100390","活动不存在");
        }

        InvitationActivity invitationActivityUpdate = new InvitationActivity();

        invitationActivityUpdate.setId(query.getId());
        invitationActivityUpdate.setStatus(query.getStatus());
        invitationActivityUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(invitationActivityUpdate);

        return Triple.of(true, null, null);
    }

    @Override
    public List<InvitationActivity> selectByPage(InvitationActivityQuery query) {
        return invitationActivityMapper.selectByPage(query);
    }

    @Override
    public Integer selectByPageCount(InvitationActivityQuery query) {
        return invitationActivityMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(InvitationActivity invitationActivity) {
        return this.invitationActivityMapper.insertOne(invitationActivity);
    }

    /**
     * 修改数据
     *
     * @param invitationActivity 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivity invitationActivity) {
        int update = this.invitationActivityMapper.update(invitationActivity);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_INVITATION_ACTIVITY + invitationActivity.getId());
        });

        return update;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteById(Long id) {
        int delete = this.invitationActivityMapper.deleteById(id);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_INVITATION_ACTIVITY + id);
        });
        return delete;
    }

    private List<InvitationActivityMemberCard> buildShareActivityMemberCard(Long id, List<Long> membercardIds) {
        List<InvitationActivityMemberCard> list = Lists.newArrayList();

        for (Long membercardId : membercardIds) {
            InvitationActivityMemberCard invitationActivityMemberCard = new InvitationActivityMemberCard();
            invitationActivityMemberCard.setActivityId(id);
            invitationActivityMemberCard.setMid(membercardId);
            invitationActivityMemberCard.setTenantId(TenantContextHolder.getTenantId());
            invitationActivityMemberCard.setCreateTime(System.currentTimeMillis());
            invitationActivityMemberCard.setUpdateTime(System.currentTimeMillis());
            list.add(invitationActivityMemberCard);
        }

        return list;
    }
}
