package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.InvitationActivityUserMapper;
import com.xiliulou.electricity.query.InvitationActivityUserQuery;
import com.xiliulou.electricity.query.InvitationActivityUserSaveQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InvitationActivityMemberCardService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InvitationActivityUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * (InvitationActivityUser)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-05 16:11:08
 */
@Service("invitationActivityUserService")
@Slf4j
public class InvitationActivityUserServiceImpl implements InvitationActivityUserService {
    
    @Resource
    private InvitationActivityUserMapper invitationActivityUserMapper;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private InvitationActivityService invitationActivityService;
    
    @Autowired
    private InvitationActivityMemberCardService invitationActivityMemberCardService;
    
    @Autowired
    private RedisService redisService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityUser queryByIdFromDB(Long id) {
        return this.invitationActivityUserMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityUser queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param invitationActivityUser 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivityUser insert(InvitationActivityUser invitationActivityUser) {
        this.invitationActivityUserMapper.insertOne(invitationActivityUser);
        return invitationActivityUser;
    }
    
    /**
     * 修改数据
     *
     * @param invitationActivityUser 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivityUser invitationActivityUser) {
        return this.invitationActivityUserMapper.update(invitationActivityUser);
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.invitationActivityUserMapper.deleteById(id) > 0;
    }
    
    @Override
    @Slave
    public List<InvitationActivityUserVO> selectByPage(InvitationActivityUserQuery query) {
        List<InvitationActivityUserVO> list = invitationActivityUserMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list.parallelStream().peek(item -> {
            User user = userService.queryByUidFromCache(item.getOperator());
            item.setOperatorName(Objects.nonNull(user) ? user.getName() : "");
            
            Long franchiseeId = item.getFranchiseeId();
            if (Objects.nonNull(franchiseeId)) {
                item.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(franchiseeId)).map(Franchisee::getName).orElse(StringUtils.EMPTY));
            }
            
        }).collect(Collectors.toList());
    }
    
    @Override
    @Slave
    public Integer selectByPageCount(InvitationActivityUserQuery query) {
        return invitationActivityUserMapper.selectByPageCount(query);
    }
    
    @Override
    public Triple<Boolean, String, Object> save(InvitationActivityUserSaveQuery query) {
        
        if (!redisService.setNx(CacheConstant.CACHE_INVITATION_ACTIVITY_USER_SAVE_LOCK + query.getUid(), NumberConstant.ONE.toString(), TimeConstant.THREE_SECOND_MILLISECOND,
                false)) {
            return Triple.of(false, "100002", "操作频繁");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        // 所选活动id
        List<Long> activityIds = query.getActivityIds();
        if (CollectionUtils.isEmpty(activityIds)) {
            return Triple.of(false, "100396", "请选择活动名称");
        }
        
        // 获取所选活动对应的套餐id
        List<Long> memberCardIdsByActivityIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityIds(activityIds);
        if (CollectionUtils.isEmpty(memberCardIdsByActivityIds)) {
            return Triple.of(false, "100393", "所选活动未绑定套餐");
        }
        
        // 判断所选活动是否包含相同的套餐
        if (memberCardIdsByActivityIds.stream().distinct().count() < memberCardIdsByActivityIds.size()) {
            return Triple.of(false, "100395", "邀请人参加的活动中不允许包含相同的套餐，请修改后提交");
        }
        
        // 获取该邀请人已绑定的活动
        List<InvitationActivityUser> invitationActivityUserList = this.selectByUid(query.getUid());
        
        if (CollectionUtils.isNotEmpty(invitationActivityUserList)) {
            // 获取已绑定的活动对应的套餐id
            List<Long> boundActivityIds = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toList());
            List<Long> boundMemberCardIds = invitationActivityMemberCardService.selectMemberCardIdsByActivityIds(boundActivityIds);
            
            // 所选活动的套餐不能包含已绑定的活动的套餐
            if (CollectionUtils.isNotEmpty(boundMemberCardIds)) {
                if (memberCardIdsByActivityIds.stream().anyMatch(boundMemberCardIds::contains)) {
                    return Triple.of(false, "100394", "该活动配置的套餐，邀请人已参加，请重新选择");
                }
            }
        }
        
        List<InvitationActivityUser> invitationActivityUsers = activityIds.stream()
                .map(activityId -> InvitationActivityUser.builder().activityId(activityId).uid(query.getUid()).operator(SecurityUtils.getUid())
                        .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build())
                .collect(Collectors.toList());
        
        invitationActivityUserMapper.batchInsert(invitationActivityUsers);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        InvitationActivityUser invitationActivityUser = this.queryByIdFromDB(id);
        if (Objects.isNull(invitationActivityUser) || !Objects.equals(invitationActivityUser.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        invitationActivityUserMapper.deleteById(id);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Slave
    public List<InvitationActivityUser> selectByUid(Long uid) {
        return this.invitationActivityUserMapper.selectByUid(uid);
    }
    
}
