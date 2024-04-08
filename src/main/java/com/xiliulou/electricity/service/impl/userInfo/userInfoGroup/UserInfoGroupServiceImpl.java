package com.xiliulou.electricity.service.impl.userInfo.userInfoGroup;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.mapper.userInfo.userInfoGroup.UserInfoGroupMapper;
import com.xiliulou.electricity.request.user.UserGroupSaveRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 20:05:19
 */
@Slf4j
@Service
public class UserInfoGroupServiceImpl implements UserInfoGroupService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private UserInfoGroupMapper userInfoGroupMapper;
    
    @Override
    public R save(UserGroupSaveRequest userGroupSaveRequest, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_SAVE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        Long franchiseId = userGroupSaveRequest.getFranchiseId();
        String userGroupName = userGroupSaveRequest.getName();
        
        if (Objects.isNull(franchiseeService.queryByIdFromCache(franchiseId))) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        UserInfoGroup userInfoGroup = userInfoGroupMapper.queryByName(userGroupName, tenantId);
        if (Objects.nonNull(userInfoGroup)) {
            // TODO 提示语
            return R.fail("", "");
        }
        
        //TODO
        String groupNo = "";
    
        long nowTime = System.currentTimeMillis();
    
        userInfoGroup = UserInfoGroup.builder()
                .groupNo(groupNo)
                .name(userGroupName)
                .operator(uid)
                .franchiseeId(franchiseId)
                .tenantId(tenantId)
                .createTime(nowTime)
                .updateTime(nowTime)
                .build();
        
        
        return R.ok(userInfoGroupMapper.insertOne(userInfoGroup));
    }
}
