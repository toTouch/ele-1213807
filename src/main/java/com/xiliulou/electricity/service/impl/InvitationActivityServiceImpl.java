package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.InvitationActivityMapper;
import com.xiliulou.electricity.query.InvitationActivityQuery;
import com.xiliulou.electricity.query.InvitationActivityStatusQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InvitationActivityVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private InvitationActivityJoinHistoryService invitationActivityJoinHistoryService;

    @Autowired
    private ElectricityMemberCardService memberCardService;

    @Autowired
    private InvitationActivityUserService invitationActivityUserService;

    @Override
    public List<InvitationActivity> selectBySearch(InvitationActivityQuery query) {
        return this.invitationActivityMapper.selectBySearch(query);
    }

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
//        Integer usableActivityCount = invitationActivityMapper.checkUsableActivity(TenantContextHolder.getTenantId());
//        if (Objects.equals(query.getStatus(), InvitationActivity.STATUS_UP) && Objects.nonNull(usableActivityCount)) {
//            return Triple.of(false, "", "已存在上架的活动");
//        }

        InvitationActivity invitationActivity = new InvitationActivity();
        BeanUtils.copyProperties(query, invitationActivity);
        invitationActivity.setDiscountType(InvitationActivity.DISCOUNT_TYPE_FIXED_AMOUNT);
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
        if (Objects.isNull(invitationActivity) || !Objects.equals(invitationActivity.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100390", "活动不存在");
        }

        InvitationActivity invitationActivityUpdate = new InvitationActivity();
        invitationActivityUpdate.setId(query.getId());
        invitationActivityUpdate.setName(query.getName());
        invitationActivityUpdate.setHours(query.getHours());
        invitationActivityUpdate.setDescription(query.getDescription());
        invitationActivityUpdate.setFirstReward(query.getFirstReward());
        invitationActivityUpdate.setOtherReward(query.getOtherReward());
        invitationActivityUpdate.setUpdateTime(System.currentTimeMillis());
        Integer update = this.update(invitationActivityUpdate);

        if (update > 0) {
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
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateStatus(InvitationActivityStatusQuery query) {
        InvitationActivity invitationActivity = this.queryByIdFromCache(query.getId());
        if (Objects.isNull(invitationActivity) || !Objects.equals(invitationActivity.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "100390", "活动不存在");
        }

//        Integer usableActivityCount = invitationActivityMapper.checkUsableActivity(TenantContextHolder.getTenantId());
//        if (Objects.equals(query.getStatus(), InvitationActivity.STATUS_UP) && Objects.nonNull(usableActivityCount)) {
//            return Triple.of(false, "", "已存在上架的活动");
//        }

        InvitationActivity invitationActivityUpdate = new InvitationActivity();

        invitationActivityUpdate.setId(query.getId());
        invitationActivityUpdate.setStatus(query.getStatus());
        invitationActivityUpdate.setUpdateTime(System.currentTimeMillis());
        Integer update = this.update(invitationActivityUpdate);

        if (Objects.equals(query.getStatus(), InvitationActivity.STATUS_DOWN) && update > 0) {
            invitationActivityJoinHistoryService.updateStatusByActivityId(query.getId(), InvitationActivityJoinHistory.STATUS_OFF);
        }

        return Triple.of(true, null, null);
    }

    @Override
    public Integer checkUsableActivity(Integer tenantId) {
        return invitationActivityMapper.checkUsableActivity(tenantId);
    }

    @Override
    public List<InvitationActivityVO> selectByPage(InvitationActivityQuery query) {

        List<InvitationActivity> invitationActivities = invitationActivityMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(invitationActivities)) {
            return Collections.emptyList();
        }

        return invitationActivities.parallelStream().map(item -> {
            InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
            BeanUtils.copyProperties(item, invitationActivityVO);

            List<Long> membercardIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(item.getId());
            if (!CollectionUtils.isEmpty(membercardIds)) {
                List<ElectricityMemberCard> memberCardList = Lists.newArrayList();
                for (Long membercardId : membercardIds) {
                    ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(membercardId.intValue());
                    if (Objects.nonNull(electricityMemberCard)) {
                        memberCardList.add(electricityMemberCard);
                    }
                }

                invitationActivityVO.setMemberCardList(memberCardList);
            }
            return invitationActivityVO;
        }).collect(Collectors.toList());

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

    @Override
    public List<InvitationActivity> selectUsableActivity(Integer tenantId) {
        return invitationActivityMapper.selectUsableActivity(tenantId);
    }

    @Override
    public Triple<Boolean, String, Object> activityInfo() {

        InvitationActivityUser invitationActivityUser = invitationActivityUserService.selectByUid(SecurityUtils.getUid());
        if(Objects.isNull(invitationActivityUser)){
            return Triple.of(true, null, null);
        }

        InvitationActivity invitationActivity = this.queryByIdFromCache(invitationActivityUser.getActivityId());
        if(Objects.isNull(invitationActivity)){
            return Triple.of(true, null, null);
        }

        InvitationActivityVO invitationActivityVO = new InvitationActivityVO();
        BeanUtils.copyProperties(invitationActivity, invitationActivityVO);

        List<Long> membercardIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityId(invitationActivity.getId());
        if (!CollectionUtils.isEmpty(membercardIds)) {
            List<ElectricityMemberCard> memberCardList = Lists.newArrayList();
            for (Long membercardId : membercardIds) {
                ElectricityMemberCard electricityMemberCard = memberCardService.queryByCache(membercardId.intValue());
                if (Objects.nonNull(electricityMemberCard)) {
                    memberCardList.add(electricityMemberCard);
                }
            }

            invitationActivityVO.setMemberCardList(memberCardList);
        }

        return Triple.of(true, null, invitationActivityVO);
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
