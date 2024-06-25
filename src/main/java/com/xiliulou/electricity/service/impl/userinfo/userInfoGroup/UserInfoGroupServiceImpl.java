package com.xiliulou.electricity.service.impl.userinfo.userInfoGroup;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupIdAndNameBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.UserInfoGroupConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.mapper.userinfo.userInfoGroup.UserInfoGroupMapper;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupQuery;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupBatchImportRequest;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupSaveAndUpdateRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.BatchImportUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
    
    @Resource
    private UserInfoGroupDetailHistoryService userInfoGroupDetailHistoryService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Override
    public R save(UserInfoGroupSaveAndUpdateRequest request, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_SAVE_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            Long franchiseeId = request.getFranchiseeId();
            String userGroupName = request.getName();
            Integer tenantId = TenantContextHolder.getTenantId();
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            if (Objects.isNull(franchisee)) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            UserInfoGroup userInfoGroup = userInfoGroupMapper.queryByName(userGroupName, tenantId);
            if (Objects.nonNull(userInfoGroup)) {
                return R.fail("120110", "分组名称已存在");
            }
            
            long nowTime = System.currentTimeMillis();
            
            userInfoGroup = UserInfoGroup.builder().groupNo(IdUtil.simpleUUID()).name(userGroupName).operator(operator).franchiseeId(franchiseeId).tenantId(tenantId)
                    .createTime(nowTime).updateTime(nowTime).build();
            
            return R.ok(userInfoGroupMapper.insertOne(userInfoGroup));
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_SAVE_LOCK + operator);
        }
    }
    
    @Override
    public Integer update(UserInfoGroup userInfoGroup) {
        return userInfoGroupMapper.update(userInfoGroup);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R remove(Long id, Long operator) {
        UserInfoGroup userInfoGroup = this.queryByIdFromCache(id);
        if (Objects.isNull(userInfoGroup)) {
            return R.fail("120112", "未找到用户分组");
        }
        
        // 租户校验
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!Objects.equals(tenantId, userInfoGroup.getTenantId())) {
            return R.ok();
        }
        
        // 物理删除分组绑定的用户
        userInfoGroupDetailService.deleteByGroupNo(userInfoGroup.getGroupNo(), tenantId);
        
        // 逻辑删除用户分组
        UserInfoGroup delUserInfoGroup = UserInfoGroup.builder().id(id).updateTime(System.currentTimeMillis()).delFlag(CommonConstant.DEL_Y).operator(operator).build();
        Integer update = update(delUserInfoGroup);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            // 删除用户分组缓存
            redisService.delete(CacheConstant.CACHE_USER_GROUP + id);
            
            // 系统操作记录
            Map<Object, Object> groupNameMap = MapBuilder.create().put("groupName", userInfoGroup.getName()).build();
            operateRecordUtil.record(groupNameMap, null);
        });
        
        return R.ok();
    }
    
    @Override
    public R edit(UserInfoGroupSaveAndUpdateRequest request, Long operator) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_UPDATE_LOCK + operator, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfoGroup oldUserInfo = this.queryByIdFromCache(request.getId());
            if (Objects.isNull(oldUserInfo)) {
                return R.fail("120112", "未找到用户分组");
            }
            
            String name = request.getName();
            Long franchiseeId = request.getFranchiseeId();
            Integer tenantId = TenantContextHolder.getTenantId();
            
            // 租户校验
            if (!Objects.equals(tenantId, oldUserInfo.getTenantId())) {
                return R.ok();
            }
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            if (Objects.isNull(franchisee)) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
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
            oldUserInfo.setOperator(operator);
            oldUserInfo.setUpdateTime(System.currentTimeMillis());
            
            int update = userInfoGroupMapper.update(oldUserInfo);
            
            DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
                redisService.delete(CacheConstant.CACHE_USER_GROUP + oldUserInfo.getId());
            });
            
            return R.ok(update);
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_UPDATE_LOCK + operator);
        }
    }
    
    @Slave
    @Override
    public List<UserInfoGroupBO> listByPage(UserInfoGroupQuery query) {
        List<UserInfoGroupBO> pageList = userInfoGroupMapper.selectPage(query);
        
        if (CollectionUtils.isEmpty(pageList)) {
            return Collections.emptyList();
        }
        
        return pageList.stream().peek(userInfoGroupBo -> {
            userInfoGroupBo.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(userInfoGroupBo.getFranchiseeId())).orElse(new Franchisee()).getName());
            userInfoGroupBo.setOperatorName(Optional.ofNullable(userService.queryByUidFromCache(userInfoGroupBo.getOperator())).orElse(new User()).getName());
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(UserInfoGroupQuery query) {
        return userInfoGroupMapper.selectCount(query);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupIdAndNameBO> listAllGroup(UserInfoGroupQuery query) {
        List<UserInfoGroupIdAndNameBO> pageList = userInfoGroupMapper.selectAllGroup(query);
        
        if (CollectionUtils.isEmpty(pageList)) {
            return Collections.emptyList();
        }
        
        return pageList;
    }
    
    @Override
    public Integer batchUpdateByIds(List<Long> groupIds, Long updateTime, Long operator, Integer delFlag) {
        return userInfoGroupMapper.batchUpdateByIds(groupIds, updateTime, operator, delFlag);
    }
    
    @Slave
    @Override
    public List<UserInfoGroup> listByIdsFromDB(List<Long> groupIds) {
        return userInfoGroupMapper.selectListByIdsFromDB(groupIds);
    }
    
    @Override
    public R batchImport(UserInfoGroupBatchImportRequest request, Long operator) {
        Long franchiseeId = request.getFranchiseeId();
        Long groupId = request.getGroupId();
        Set<String> phones = new HashSet<>(JsonUtil.fromJsonArray(request.getJsonPhones(), String.class));
        
        if (CollectionUtils.isEmpty(phones)) {
            return R.fail("120111", "手机号不可以为空");
        }
        
        UserInfoGroup userInfoGroup = this.queryByIdFromCache(groupId);
        if (Objects.isNull(userInfoGroup)) {
            return R.fail("120115", "分组名称不存在");
        }
        
        // 租户校验
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!Objects.equals(tenantId, userInfoGroup.getTenantId())) {
            return R.ok();
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
    
        ConcurrentHashSet<String> notExistsPhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<String> notBoundFranchiseePhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<String> notSameFranchiseePhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<String> overLimitGroupNumPhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<UserInfo> existsPhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<UserInfo> sameFranchiseeUserInfos = new ConcurrentHashSet<>();
        ConcurrentHashMap<Long, UserInfo> userInfoMap = new ConcurrentHashMap<>();
        
        List<List<String>> partition = ListUtils.partition(new ArrayList<>(phones), 500);
        partition.parallelStream().forEach(phoneList -> {
            List<User> userList = userService.listByPhones(phoneList, tenantId, User.TYPE_USER_NORMAL_WX_PRO);
            if (CollectionUtils.isEmpty(userList)) {
                notExistsPhone.addAll(phoneList);
                return;
            }
            
            List<User> notExistUsers = userList.parallelStream().filter(u -> !phoneList.contains(u.getPhone())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(notExistUsers)) {
                List<String> notExistPhones1 = notExistUsers.parallelStream().map(User::getPhone).collect(Collectors.toList());
                notExistsPhone.addAll(notExistPhones1);
                userList.removeAll(notExistUsers);
            }
            
            List<Long> uidList = userList.parallelStream().map(User::getUid).collect(Collectors.toList());
            List<UserInfo> userInfoList = userInfoService.listByUids(uidList, tenantId);
            if (CollectionUtils.isEmpty(userInfoList)) {
                List<String> notExistsPhones2 = userList.parallelStream().map(User::getPhone).collect(Collectors.toList());
                notExistsPhone.addAll(notExistsPhones2);
                return;
            }
            
            List<UserInfo> notExistUserInfos = userInfoList.parallelStream().filter(userInfo -> !uidList.contains(userInfo.getUid())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(notExistUserInfos)) {
                List<String> notExistsPhones3 = notExistUserInfos.parallelStream().map(UserInfo::getPhone).collect(Collectors.toList());
                notExistsPhone.addAll(notExistsPhones3);
                userInfoList.removeAll(notExistUserInfos);
            }
            
            userInfoList.parallelStream().forEach(userInfo -> {
                Long bindFranchiseeId = userInfo.getFranchiseeId();
                if (Objects.isNull(bindFranchiseeId) || Objects.equals(bindFranchiseeId, NumberConstant.ZERO_L)) {
                    notBoundFranchiseePhone.add(userInfo.getPhone());
                } else {
                    if (Objects.equals(bindFranchiseeId, franchiseeId)) {
                        sameFranchiseeUserInfos.add(userInfo);
                        userInfoMap.put(userInfo.getUid(), userInfo);
                    } else {
                        notSameFranchiseePhone.add(userInfo.getPhone());
                    }
                }
            });
        });
        
        AtomicReference<Map<Long, List<UserInfoGroupNamesBO>>> userGroupMap = new AtomicReference<>();
        // 判断绑定分组数量是否超限
        if (CollectionUtils.isNotEmpty(sameFranchiseeUserInfos)) {
            existsPhone.addAll(sameFranchiseeUserInfos);
            
            List<Long> uidList = sameFranchiseeUserInfos.stream().map(UserInfo::getUid).collect(Collectors.toList());
            List<List<Long>> partition1 = ListUtils.partition(uidList, 500);
            
            partition1.parallelStream().forEach(uidList1 -> {
                List<UserInfoGroupNamesBO> listByUidList = userInfoGroupDetailService.listGroupByUidList(uidList1);
                if (CollectionUtils.isEmpty(listByUidList)) {
                    return;
                }
                
                // 根据uid进行分组
                userGroupMap.set(listByUidList.stream().collect(Collectors.groupingBy(UserInfoGroupNamesBO::getUid)));
                if (MapUtils.isEmpty(userGroupMap.get())) {
                    return;
                }
                
                userGroupMap.get().forEach((uid, v) -> {
                    if (CollectionUtils.isEmpty(v)) {
                        return;
                    }
                    
                    UserInfo userInfo = userInfoMap.get(uid);
                    if (Objects.nonNull(userInfo)) {
                        if (v.size() >= UserInfoGroupConstant.USER_GROUP_LIMIT) {
                            overLimitGroupNumPhone.add(userInfo.getPhone());
                            existsPhone.removeIf(e -> Objects.equals(e.getUid(), uid));
                        }
                    }
                });
            });
        }
        
        BatchImportUserInfoVO batchImportUserInfoVO = new BatchImportUserInfoVO();
        String sessionId = UUID.fastUUID().toString(true);
        batchImportUserInfoVO.setSessionId(sessionId);
        batchImportUserInfoVO.setNotExistPhones(CollectionUtils.isEmpty(notExistsPhone) ? Collections.emptySet() : notExistsPhone);
        batchImportUserInfoVO.setNotBoundFranchiseePhones(CollectionUtils.isEmpty(notBoundFranchiseePhone) ? Collections.emptySet() : notBoundFranchiseePhone);
        batchImportUserInfoVO.setNotSameFranchiseePhones(CollectionUtils.isEmpty(notSameFranchiseePhone) ? Collections.emptySet() : notSameFranchiseePhone);
        batchImportUserInfoVO.setOverLimitGroupNumPhones(CollectionUtils.isEmpty(overLimitGroupNumPhone) ? Collections.emptySet() : overLimitGroupNumPhone);
        
        if (existsPhone.isEmpty()) {
            batchImportUserInfoVO.setIsImported(false);
            return R.ok(batchImportUserInfoVO);
        }
        
        batchImportUserInfoVO.setIsImported(true);
        executorService.execute(() -> {
            handleBatchImportUserInfo(userInfoGroup, existsPhone, sessionId, franchiseeId, tenantId, operator, userGroupMap.get());
        });
        
        return R.ok(batchImportUserInfoVO);
    }
    
    private void handleBatchImportUserInfo(UserInfoGroup userInfoGroup, ConcurrentHashSet<UserInfo> existsPhone, String sessionId, Long franchiseeId, Integer tenantId,
            Long operator, Map<Long, List<UserInfoGroupNamesBO>> userGroupMap) {
        List<UserInfoGroupDetail> detailList = new ArrayList<>();
        List<UserInfoGroupDetailHistory> detailHistoryList = new ArrayList<>();
        Iterator<UserInfo> iterator = existsPhone.iterator();
        long nowTime = System.currentTimeMillis();
        int maxSize = 300;
        int size = 0;
        
        while (iterator.hasNext()) {
            if (size >= maxSize) {
                Integer insert = userInfoGroupDetailService.batchInsert(detailList);
                
                if (insert > 0 && CollectionUtils.isNotEmpty(detailHistoryList)) {
                    // 新增修改记录
                    userInfoGroupDetailHistoryService.batchInsert(detailHistoryList);
                }
                
                detailList.clear();
                detailHistoryList.clear();
                size = 0;
                continue;
            }
            
            UserInfo userInfo = iterator.next();
            Long uid = userInfo.getUid();
            
            String oldGroupIds = "";
            if (MapUtils.isNotEmpty(userGroupMap)) {
                // 该用户已绑定的所有分组
                List<UserInfoGroupNamesBO> existGroupList = userGroupMap.get(uid);
                
                // 如果已经绑定该分组，则跳过
                if (CollectionUtils.isNotEmpty(existGroupList)) {
                    if (existGroupList.stream().anyMatch(v -> Objects.equals(v.getGroupNo(), userInfoGroup.getGroupNo()))) {
                        continue;
                    }
                    
                    // 分组ids
                    oldGroupIds = existGroupList.stream().map(g -> g.getGroupId().toString()).collect(Collectors.joining(CommonConstant.STR_COMMA));
                }
            }
            
            // 构建detail
            UserInfoGroupDetail detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(franchiseeId).tenantId(tenantId)
                    .createTime(nowTime).updateTime(nowTime).operator(operator).build();
            detailList.add(detail);
            
            // 构建detailHistory
            String newGroupIds = userInfoGroup.getId().toString();
            if (StringUtils.isNotBlank(oldGroupIds)) {
                newGroupIds = oldGroupIds + CommonConstant.STR_COMMA + userInfoGroup.getId();
            }
            
            UserInfoGroupDetailHistory detailHistory = UserInfoGroupDetailHistory.builder().uid(uid).oldGroupIds(oldGroupIds).newGroupIds(newGroupIds).operator(operator)
                    .franchiseeId(franchiseeId).tenantId(tenantId).createTime(nowTime).updateTime(nowTime).type(UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_OTHER).build();
            detailHistoryList.add(detailHistory);
            
            size++;
        }
        if (!detailList.isEmpty()) {
            Integer insert = userInfoGroupDetailService.batchInsert(detailList);
            
            if (insert > 0 && CollectionUtils.isNotEmpty(detailHistoryList)) {
                // 新增修改记录
                userInfoGroupDetailHistoryService.batchInsert(detailHistoryList);
            }
        }
        
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
    public List<UserInfoGroupBO> listByIds(List<Long> ids) {
        return userInfoGroupMapper.selectListByIds(ids);
    }
    
}
