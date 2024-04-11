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
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.mapper.userInfo.userInfoGroup.UserInfoGroupMapper;
import com.xiliulou.electricity.query.UserInfoGroupQuery;
import com.xiliulou.electricity.request.user.UserInfoGroupBatchImportRequest;
import com.xiliulou.electricity.request.user.UserInfoGroupSaveAndUpdateRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.userinfo.BatchImportUserInfoVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupIdAndNameVO;
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
    public R save(UserInfoGroupSaveAndUpdateRequest request, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_SAVE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Long franchiseeId = request.getFranchiseeId();
            String userGroupName = request.getName();
            
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
    
    @Override
    public R remove(Long id, Long uid) {
        UserInfoGroup userInfoGroup = this.queryByIdFromCache(id);
        if (Objects.isNull(userInfoGroup)) {
            return R.fail("120112", "未找到用户分组");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (!Objects.equals(tenantId, userInfoGroup.getTenantId())) {
            return R.ok();
        }
        
        Integer count = userInfoGroupDetailService.countUserByGroupId(id);
        if (Objects.nonNull(count) && count > NumberConstant.ZERO) {
            return R.fail("120113", "该分组中存在用户，请先移除用户后再操作");
        }
        
        UserInfoGroup delUserInfoGroup = UserInfoGroup.builder().id(id).updateTime(System.currentTimeMillis()).delFlag(CommonConstant.DEL_Y).operator(uid).build();
        
        int update = userInfoGroupMapper.update(delUserInfoGroup);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_GROUP + id);
        });
        
        return R.ok(update);
    }
    
    @Override
    public R update(UserInfoGroupSaveAndUpdateRequest request, Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_UPDATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfoGroup oldUserInfo = this.queryByIdFromCache(request.getId());
            if (Objects.isNull(oldUserInfo)) {
                return R.fail("120112", "未找到用户分组");
            }
            
            String name = request.getName();
            Integer tenantId = TenantContextHolder.getTenantId();
            
            if (!Objects.equals(tenantId, oldUserInfo.getTenantId())) {
                return R.ok();
            }
            
            if (Objects.equals(oldUserInfo.getName(), name)) {
                return R.ok();
            } else {
                UserInfoGroup userInfoGroup = userInfoGroupMapper.queryByName(name, tenantId);
                if (Objects.nonNull(userInfoGroup)) {
                    return R.fail("120110", "分组名称已存在");
                }
            }
            
            oldUserInfo.setName(name);
            oldUserInfo.setOperator(uid);
            oldUserInfo.setUpdateTime(System.currentTimeMillis());
            
            int update = userInfoGroupMapper.update(oldUserInfo);
            
            DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
                redisService.delete(CacheConstant.CACHE_USER_GROUP + oldUserInfo.getId());
            });
            
            return R.ok(update);
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_UPDATE_LOCK + uid);
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
            userInfoGroupVO.setOperatorName(Optional.ofNullable(userService.queryByUidFromCache(userInfoGroupVO.getOperator())).orElse(new User()).getName());
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(UserInfoGroupQuery query) {
        return userInfoGroupMapper.selectCount(query);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupIdAndNameVO> listAllGroup(UserInfoGroupQuery query) {
        List<UserInfoGroupIdAndNameVO> pageList = userInfoGroupMapper.selectAllGroup(query);
    
        if (CollectionUtils.isEmpty(pageList)) {
            return Collections.emptyList();
        }
        
        return pageList;
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
        
        ConcurrentHashSet<UserInfoGroupIdAndNameVO> notExistsUserGroup = new ConcurrentHashSet<>();
        ConcurrentHashSet<UserInfoGroupIdAndNameVO> notBoundFranchiseeUserGroup = new ConcurrentHashSet<>();
        ConcurrentHashSet<UserInfoGroupIdAndNameVO> notSameFranchiseeUserGroup = new ConcurrentHashSet<>();
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
            UserInfoGroup userInfoGroup = this.queryByIdFromCache(e);
            if (Objects.isNull(userInfoGroup)) {
                notExistsUserGroup.add(UserInfoGroupIdAndNameVO.builder().id(e).build());
            } else {
                Long bindFranchiseeId = userInfoGroup.getFranchiseeId();
                
                if (Objects.isNull(bindFranchiseeId) || Objects.equals(bindFranchiseeId, NumberConstant.ZERO_L)) {
                    notBoundFranchiseeUserGroup.add(UserInfoGroupIdAndNameVO.builder().id(e).name(userInfoGroup.getName()).groupNo(userInfoGroup.getGroupNo()).build());
                } else {
                    if (Objects.equals(bindFranchiseeId, franchiseeId)) {
                        existsUserGroup.add(userInfoGroup);
                    } else {
                        notSameFranchiseeUserGroup.add(
                                UserInfoGroupIdAndNameVO.builder().id(e).name(userInfoGroup.getName()).groupNo(userInfoGroup.getGroupNo()).build());
                    }
                }
            }
        });
        
        BatchImportUserInfoVO batchImportUserInfoVO = new BatchImportUserInfoVO();
        String sessionId = UUID.fastUUID().toString(true);
        batchImportUserInfoVO.setSessionId(sessionId);
        batchImportUserInfoVO.setNotExistUserGroups(CollectionUtils.isEmpty(notExistsUserGroup) ? Collections.emptySet() : notExistsUserGroup);
        batchImportUserInfoVO.setNotBoundFranchiseeUserGroups(CollectionUtils.isEmpty(notBoundFranchiseeUserGroup) ? Collections.emptySet() : notBoundFranchiseeUserGroup);
        batchImportUserInfoVO.setNotSameFranchiseeUserGroups(CollectionUtils.isEmpty(notSameFranchiseeUserGroup) ? Collections.emptySet() : notSameFranchiseeUserGroup);
        batchImportUserInfoVO.setNotExistPhones(CollectionUtils.isEmpty(notExistsPhone) ? Collections.emptySet() : notExistsPhone);
        batchImportUserInfoVO.setNotBoundFranchiseePhones(CollectionUtils.isEmpty(notBoundFranchiseePhone) ? Collections.emptySet() : notBoundFranchiseePhone);
        batchImportUserInfoVO.setNotSameFranchiseePhones(CollectionUtils.isEmpty(notSameFranchiseePhone) ? Collections.emptySet() : notSameFranchiseePhone);
        
        if (existsUserGroup.isEmpty() || existsPhone.isEmpty()) {
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
    
    @Override
    public UserInfoGroup queryByIdFromCache(Long id) {
        //先查缓存
        UserInfoGroup cacheUserInfoGroup = redisService.getWithHash(CacheConstant.CACHE_USER_GROUP + id, UserInfoGroup.class);
        if (Objects.nonNull(cacheUserInfoGroup)) {
            return cacheUserInfoGroup;
        }
        //缓存没有再查数据库
        UserInfoGroup userInfoGroup = userInfoGroupMapper.selectById(id);
        if (Objects.isNull(userInfoGroup)) {
            return null;
        }
        
        //放入缓存
        redisService.saveWithHash(CacheConstant.CACHE_USER_GROUP + id, userInfoGroup);
        return userInfoGroup;
    }
    
    @Slave
    @Override
    public List<UserInfoGroupVO> listByIds(List<Long> ids) {
        return userInfoGroupMapper.selectListByIds(ids);
    }
    
}
