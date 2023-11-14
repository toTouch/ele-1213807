package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.InvitationActivity;
import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.InvitationActivityUserMapper;
import com.xiliulou.electricity.query.InvitationActivityUserAddQuery;
import com.xiliulou.electricity.query.InvitationActivityUserQuery;
import com.xiliulou.electricity.query.InvitationActivityUserSaveQuery;
import com.xiliulou.electricity.service.InvitationActivityMemberCardService;
import com.xiliulou.electricity.service.InvitationActivityService;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InvitationActivityMemberCardVO;
import com.xiliulou.electricity.vo.InvitationActivityUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<InvitationActivityUser> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityUserMapper.queryAllByLimit(offset, limit);
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
    public List<InvitationActivityUserVO> selectByPage(InvitationActivityUserQuery query) {
        List<InvitationActivityUserVO> list = invitationActivityUserMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.parallelStream().peek(item -> {
            User user = userService.queryByUidFromCache(item.getOperator());
            item.setOperatorName(Objects.nonNull(user) ? user.getName() : "");
            
        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectByPageCount(InvitationActivityUserQuery query) {
        return invitationActivityUserMapper.selectByPageCount(query);
    }

    @Override
    public Triple<Boolean, String, Object> save(InvitationActivityUserSaveQuery query) {
    
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
    
        // 活动及对应的套餐id
        List<InvitationActivityUserAddQuery> invitationActivityUserAddQueries = query.getInvitationActivityUserAddQueries();
    
        // 获取该邀请人已绑定的活动
        List<InvitationActivityUser> invitationActivityUserList = this.selectByUid(query.getUid());
    
        // 根据已绑定的活动获取其对应的套餐id
        List<Long> memberCardIdsByActivity = null;
        if (CollectionUtils.isNotEmpty(invitationActivityUserList)) {
            List<Long> boundActivityIds = invitationActivityUserList.stream().map(InvitationActivityUser::getActivityId).collect(Collectors.toList());
            memberCardIdsByActivity = invitationActivityMemberCardService.selectMemberCardIdsByActivityIds(boundActivityIds);
        }
        
        if (CollectionUtils.isEmpty(invitationActivityUserAddQueries)) {
            return Triple.of(false, "ELECTRICITY.0069", "未找到活动");
        }
    
        // 判断所选活动的套餐是否包含已绑定的活动的套餐
        if (CollectionUtils.isNotEmpty(memberCardIdsByActivity)) {
        
            for (InvitationActivityUserAddQuery activityUserAddQuery : invitationActivityUserAddQueries) {
                // 每个活动对应的套餐id
                List<Long> memberCardIdsEveryActivity = activityUserAddQuery.getMemberCardIds();
            
                // 判断 每个活动对应的套餐id是否含有该邀请用户已绑定的套餐id，如果包含，移除该活动
                if (CollectionUtils.isNotEmpty(memberCardIdsEveryActivity)) {
                    for (Long cardId : memberCardIdsByActivity) {
                        if (memberCardIdsEveryActivity.contains(cardId)) {
                            return Triple.of(false, "ELECTRICITY.0069", "所选的活动包含其已绑定的活动套餐");
                        }
                    }
                }
            }
        }
    
        invitationActivityUserAddQueries.stream().peek(item -> {
            InvitationActivityUser invitationActivityUser1 = InvitationActivityUser.builder().activityId(item.getId()).uid(query.getUid()).operator(SecurityUtils.getUid())
                    .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
            this.invitationActivityUserMapper.insertOne(invitationActivityUser1);
        
        }).collect(Collectors.toList());
    
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
    public List<InvitationActivityUser> selectByUid(Long uid) {
        return this.invitationActivityUserMapper.selectByUid(uid);
    }
    
}
