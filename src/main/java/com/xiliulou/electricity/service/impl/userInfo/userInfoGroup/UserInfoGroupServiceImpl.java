package com.xiliulou.electricity.service.impl.userInfo.userInfoGroup;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.mapper.userInfo.userInfoGroup.UserInfoGroupMapper;
import com.xiliulou.electricity.query.UserInfoGroupQuery;
import com.xiliulou.electricity.request.user.UserInfoGroupBatchImportRequest;
import com.xiliulou.electricity.request.user.UserInfoGroupSaveRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.userinfo.BatchImportUserInfoVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 20:05:19
 */
@Slf4j
@Service
public class UserInfoGroupServiceImpl implements UserInfoGroupService {
    
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("BATCH_IMPORT_USER_INFO_THREAD_POOL", 7, "batch_import_user_info_thread");
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private UserInfoGroupMapper userInfoGroupMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Override
    public R save(UserInfoGroupSaveRequest userInfoGroupSaveRequest, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_SAVE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Long franchiseeId = userInfoGroupSaveRequest.getFranchiseId();
            String userGroupName = userInfoGroupSaveRequest.getName();
            
            if (Objects.isNull(franchiseeService.queryByIdFromCache(franchiseeId))) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            Integer tenantId = TenantContextHolder.getTenantId();
            
            UserInfoGroup userInfoGroup = userInfoGroupMapper.queryByName(userGroupName, tenantId);
            if (Objects.nonNull(userInfoGroup)) {
                return R.fail("120110", "分组名称已存在");
            }
            
            long nowTime = System.currentTimeMillis();
            
            userInfoGroup = UserInfoGroup.builder().groupNo(IdUtil.simpleUUID()).name(userGroupName).operator(uid).franchiseeId(franchiseeId).tenantId(tenantId).createTime(nowTime)
                    .updateTime(nowTime).build();
            
            return R.ok(userInfoGroupMapper.insertOne(userInfoGroup));
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_SAVE_LOCK + uid);
        }
    }
    
    @Slave
    @Override
    public List<UserInfoGroupVO> listByPage(UserInfoGroupQuery query) {
        List<UserInfoGroupVO> pageList = userInfoGroupMapper.selectPage(query);
        
        if (CollectionUtils.isEmpty(pageList)) {
            return Collections.emptyList();
        }
        
        return pageList.stream().peek(userInfoGroupVO -> {
            userInfoGroupVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(userInfoGroupVO.getFranchiseeId())).orElse(new Franchisee()).getName());
            userInfoGroupVO.setOperatorName(Optional.ofNullable(userService.queryByUidFromCache(userInfoGroupVO.getFranchiseeId())).orElse(new User()).getName());
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(UserInfoGroupQuery query) {
        return userInfoGroupMapper.selectCount(query);
    }
    
    @Override
    public R batchImport(UserInfoGroupBatchImportRequest request, Long uid) {
        Long franchiseeId = request.getFranchiseeId();
        List<Long> groupIds = request.getGroupIds();
        Set<String> phones = new HashSet<>(JsonUtil.fromJsonArray(request.getJsonPhones(), String.class));
        
        if (CollectionUtils.isEmpty(phones)) {
            return R.fail("120111", "手机号不可以为空");
        }
        
        if (Objects.isNull(franchiseeService.queryByIdFromCache(franchiseeId))) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        ConcurrentHashSet<Long> notExistsUserGroup = new ConcurrentHashSet<>();
        ConcurrentHashSet<UserInfoGroup> existsUserGroup = new ConcurrentHashSet<>();
        ConcurrentHashSet<String> notExistsPhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<String> notBoundFranchiseePhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<String> notSameFranchiseePhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<UserInfo> existsPhone = new ConcurrentHashSet<>();
        
        for (String e : phones) {
            UserInfo userInfo = userInfoService.queryUserInfoByPhone(e, tenantId);
            if (Objects.isNull(userInfo)) {
                notExistsPhone.add(e);
            } else {
                Long bindFranchiseeId = userInfo.getFranchiseeId();
                
                if (Objects.isNull(bindFranchiseeId) || Objects.equals(bindFranchiseeId, NumberConstant.ZERO_L)) {
                    notBoundFranchiseePhone.add(e);
                } else {
                    if (Objects.equals(bindFranchiseeId, franchiseeId)) {
                        existsPhone.add(userInfo);
                    } else {
                        notSameFranchiseePhone.add(e);
                    }
                }
            }
        }
        
        groupIds.parallelStream().forEach(e -> {
            UserInfoGroup userInfoGroup = queryById(e);
            if (Objects.isNull(userInfoGroup)) {
                notExistsUserGroup.add(e);
            } else {
                existsUserGroup.add(userInfoGroup);
            }
        });
        
        String sessionId = UUID.fastUUID().toString(true);
        BatchImportUserInfoVO batchImportUserInfoVO = new BatchImportUserInfoVO();
        batchImportUserInfoVO.setSessionId(sessionId);
        
        if (existsUserGroup.isEmpty() || existsPhone.isEmpty()) {
            batchImportUserInfoVO.setNotExistUserGroups(notExistsUserGroup);
            batchImportUserInfoVO.setNotExistPhones(notExistsPhone);
            batchImportUserInfoVO.setNotBoundFranchiseePhones(notBoundFranchiseePhone);
            batchImportUserInfoVO.setNotSameFranchiseePhones(notSameFranchiseePhone);
            batchImportUserInfoVO.setIsImported(false);
            
            return R.ok(batchImportUserInfoVO);
        }
        
        batchImportUserInfoVO.setIsImported(true);
        executorService.execute(() -> {
            handleBatchImportUserInfo(existsUserGroup, existsPhone, sessionId, franchiseeId, tenantId);
        });
        
        return R.ok(batchImportUserInfoVO);
    }
    
    private void handleBatchImportUserInfo(ConcurrentHashSet<UserInfoGroup> existsUserGroup, ConcurrentHashSet<UserInfo> existsPhone, String sessionId, Long franchiseeId,
            Integer tenantId) {
        // 遍历分组
        existsUserGroup.forEach(userGroup -> {
            List<UserInfoGroupDetail> detailList = new ArrayList<>();
            Iterator<UserInfo> iterator = existsPhone.iterator();
            long nowTime = System.currentTimeMillis();
            int maxSize = 300;
            int size = 0;
            
            while (iterator.hasNext()) {
                if (size >= maxSize) {
                    userInfoGroupDetailService.batchInsert(detailList);
                    detailList.clear();
                    size = 0;
                    continue;
                }
                
                UserInfo userInfo = iterator.next();
                
                UserInfoGroupDetail existDetail = userInfoGroupDetailService.queryByUid(userGroup.getGroupNo(), userInfo.getUid(), tenantId);
                if (Objects.nonNull(existDetail)) {
                    continue;
                }
                
                UserInfoGroupDetail detail = UserInfoGroupDetail.builder().groupNo(userGroup.getGroupNo()).uid(userInfo.getUid()).franchiseeId(franchiseeId).tenantId(tenantId)
                        .createTime(nowTime).updateTime(nowTime).build();
                
                detailList.add(detail);
                
                size++;
            }
            
            if (!detailList.isEmpty()) {
                userInfoGroupDetailService.batchInsert(detailList);
            }
        });
        
        redisService.set(CacheConstant.CACHE_USER_GROUP_BATCH_IMPORT + sessionId, "1", 60L, TimeUnit.SECONDS);
    }
    
    @Slave
    @Override
    public UserInfoGroup queryById(Long id) {
        return userInfoGroupMapper.selectById(id);
    }
    
    @Slave
    @Override
    public List<String> listGroupNameByUid(Long uid, Integer tenantId) {
        return userInfoGroupMapper.selectListGroupNameByUid(uid, tenantId);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupVO> listByIds(List<Long> ids) {
        return userInfoGroupMapper.selectListByIds(ids);
    }
    
}
