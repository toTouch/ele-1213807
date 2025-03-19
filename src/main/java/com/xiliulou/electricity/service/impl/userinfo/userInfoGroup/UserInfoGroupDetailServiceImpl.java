package com.xiliulou.electricity.service.impl.userinfo.userInfoGroup;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.google.common.collect.Sets;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.annotation.ProcessParameter;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailPageBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupIdAndNameBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.UserInfoGroupConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.mapper.userinfo.userInfoGroup.UserInfoGroupDetailMapper;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoBindGroupRequest;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoBindGroupRequestV2;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupDetailUpdateRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupBizService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.BatchUnbindGroupVO;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.UserInfoGroupForUserVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:45:00
 */
@Slf4j
@Service
public class UserInfoGroupDetailServiceImpl implements UserInfoGroupDetailService {
    
    @Autowired
    UserService userService;
    
    @Resource
    private UserInfoGroupDetailMapper userInfoGroupDetailMapper;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private UserInfoGroupDetailHistoryService userInfoGroupDetailHistoryService;
    
    @Resource
    private UserInfoGroupBizService userInfoGroupBizService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @Slave
    @Override
    public List<UserInfoGroupDetailPageBO> listByPage(UserInfoGroupDetailQuery query) {
        List<UserInfoGroupDetailBO> list = userInfoGroupDetailMapper.selectPage(query);
        
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        List<Long> uidList = list.stream().map(UserInfoGroupDetailBO::getUid).collect(Collectors.toList());
        List<UserInfoGroupNamesBO> listByUidList = this.listGroupByUidList(uidList);
        
        // 加盟商用户过滤数据
        if (CollectionUtils.isNotEmpty(query.getFranchiseeIds())) {
            listByUidList = listByUidList.stream().filter(userInfoGroupNamesBO -> {
                return query.getFranchiseeIds().contains(userInfoGroupNamesBO.getFranchiseeId());
            }).collect(Collectors.toList());
        }
        
        // 根据uid进行分组
        Map<Long, List<UserInfoGroupNamesBO>> groupMap = null;
        if (CollectionUtils.isNotEmpty(listByUidList)) {
            groupMap = listByUidList.stream().collect(Collectors.groupingBy(UserInfoGroupNamesBO::getUid));
        }
        
        Map<Long, List<UserInfoGroupNamesBO>> finalGroupMap = groupMap;
        return list.stream().filter(Objects::nonNull).map(item -> {
            UserInfoGroupDetailPageBO detailBO = new UserInfoGroupDetailPageBO();
            Long uid = item.getUid();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            
            detailBO.setId(item.getId());
            detailBO.setUid(uid);
            detailBO.setUserName(Optional.ofNullable(userInfo).orElse(new UserInfo()).getName());
            detailBO.setPhone(Optional.ofNullable(userInfo).orElse(new UserInfo()).getPhone());
            detailBO.setFranchiseeId(item.getFranchiseeId());
            detailBO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getFranchiseeId())).orElse(new Franchisee()).getName());
            detailBO.setCreateTime(item.getCreateTime());
            detailBO.setUpdateTime(item.getUpdateTime());
            
            List<UserInfoGroupIdAndNameBO> groups = new ArrayList<>();
            if (MapUtils.isNotEmpty(finalGroupMap)) {
                finalGroupMap.forEach((k, v) -> {
                    if (k.equals(uid)) {
                        v.forEach(i -> {
                            UserInfoGroupIdAndNameBO idAndNameBO = UserInfoGroupIdAndNameBO.builder().id(i.getGroupId()).name(i.getGroupName()).groupNo(i.getGroupNo()).build();
                            groups.add(idAndNameBO);
                        });
                    }
                });
            }
            
            detailBO.setGroups(groups);
            
            return detailBO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(UserInfoGroupDetailQuery query) {
        return userInfoGroupDetailMapper.countTotal(query);
    }
    
    @Override
    public Integer batchInsert(List<UserInfoGroupDetail> detailList) {
        return userInfoGroupDetailMapper.batchInsert(detailList);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupNamesBO> listGroupByUid(UserInfoGroupDetailQuery query) {
        return userInfoGroupDetailMapper.selectListGroupByUid(query);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupNamesBO> listGroupByUserGroups(List<Long> uids, Long groupId, Long franchiseeId) {
        return userInfoGroupDetailMapper.selectListGroupByUserGroups(uids, groupId, franchiseeId);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupNamesBO> listGroupByUidList(List<Long> uidList) {
        return userInfoGroupDetailMapper.selectListGroupByUidList(uidList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(UserInfoGroupDetailUpdateRequest request, Long operator) {
        Long uid = request.getUid();
        Long franchiseeId = request.getFranchiseeId();
        List<Long> groupIds = request.getGroupIds();
        
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_DETAIL_UPDATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            // 租户校验
            Integer tenantId = TenantContextHolder.getTenantId();
            if (!Objects.equals(tenantId, userInfo.getTenantId())) {
                return R.ok();
            }
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            if (Objects.isNull(franchisee)) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            // 如果没有分组，则删除
            if (CollectionUtils.isEmpty(groupIds)) {
                List<UserInfoGroupNamesBO> existGroupList = this.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(uid).tenantId(tenantId).build());
                if (CollectionUtils.isNotEmpty(existGroupList)) {
                    String oldGroupIds = existGroupList.stream().map(g -> g.getGroupId().toString()).collect(Collectors.joining(CommonConstant.STR_COMMA));
                    UserInfoGroupDetailHistory detailHistory = assembleDetailHistory(uid, oldGroupIds, "", operator, userInfo.getFranchiseeId(), tenantId,
                            UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_OTHER);
                    
                    Integer delete = userInfoGroupDetailMapper.deleteByUid(uid, null);
                    if (delete > 0) {
                        // 新增历史记录
                        userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
                    }
                    return R.ok();
                }
            }
            
            List<UserInfoGroupBO> groupList = userInfoGroupBizService.listUserInfoGroupByIds(groupIds);
            if (!Objects.equals(groupList.size(), groupIds.size())) {
                log.warn("Update userInfoGroupDetail error! groupList is empty or size not equal, groupIds={}", groupIds);
                return R.fail("120112", "未找到用户分组");
            }
            
            // 加盟商校验
            List<UserInfoGroupBO> notSameFranchiseeGroups = groupList.stream().filter(item -> !Objects.equals(item.getFranchiseeId(), userInfo.getFranchiseeId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(notSameFranchiseeGroups)) {
                List<Long> differentGroupIds = notSameFranchiseeGroups.stream().map(UserInfoGroupBO::getId).collect(Collectors.toList());
                log.warn("Update userInfoGroupDetail error! has different franchisee, different groupIds={}", differentGroupIds);
                return R.fail("120112", "未找到用户分组");
            }
            
            List<UserInfoGroupNamesBO> existGroupList = this.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(uid).tenantId(tenantId).build());
            List<Long> intersection = new ArrayList<>(groupIds);
            List<Long> oldGroupIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(existGroupList)) {
                // 分组ids
                oldGroupIds = existGroupList.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
                
                // 获取交集
                intersection.retainAll(existGroupList.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList()));
                
                if (CollectionUtils.isNotEmpty(intersection)) {
                    // 去除交集后，剩下的就是需要新增的
                    groupIds.removeAll(intersection);
                    
                    // 去除交集后，剩下的是需要删除的
                    existGroupList.removeAll(existGroupList.stream().filter(item -> intersection.contains(item.getGroupId())).collect(Collectors.toList()));
                }
            }
            
            // 处理新增
            List<UserInfoGroupDetail> insertList = null;
            List<Long> addGroupList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(groupIds)) {
                if ((intersection.size() + groupIds.size()) > UserInfoGroupConstant.USER_GROUP_LIMIT) {
                    return R.fail("120114", "用户绑定的分组数量已达上限10个");
                }
                
                long nowTime = System.currentTimeMillis();
                
                List<UserInfoGroupDetail> detailList = groupIds.stream().map(groupId -> {
                    UserInfoGroup userInfoGroup = userInfoGroupBizService.queryUserInfoGroupByIdFromCache(groupId);
                    
                    UserInfoGroupDetail detail = null;
                    if (Objects.nonNull(userInfoGroup)) {
                        detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(userInfoGroup.getFranchiseeId()).tenantId(tenantId)
                                .createTime(nowTime).updateTime(nowTime).operator(operator).build();
                        
                        addGroupList.add(groupId);
                    }
                    
                    return detail;
                }).collect(Collectors.toList());
                
                insertList = detailList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            
            // 持久化detail
            handleGroupDetailDb(uid, insertList, existGroupList, addGroupList, oldGroupIds, operator, userInfo.getFranchiseeId(), tenantId);
            
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_DETAIL_UPDATE_LOCK + uid);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleGroupDetailDb(Long uid, List<UserInfoGroupDetail> insertList, List<UserInfoGroupNamesBO> delGroupList, List<Long> addGroupList, List<Long> oldGroupIds,
            Long operator, Long franchiseeId, Integer tenantId) {
        
        // 新增
        if (CollectionUtils.isNotEmpty(insertList)) {
            userInfoGroupDetailMapper.batchInsert(insertList);
        }
        
        // 删除
        List<Long> deleteGroupList = null;
        if (CollectionUtils.isNotEmpty(delGroupList)) {
            List<String> deleteGroupNoList = delGroupList.stream().map(UserInfoGroupNamesBO::getGroupNo).collect(Collectors.toList());
            this.deleteByUid(uid, deleteGroupNoList);
            
            deleteGroupList = delGroupList.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
        }
        
        List<Long> newGroupList = new ArrayList<>(oldGroupIds);
        if (CollectionUtils.isNotEmpty(deleteGroupList)) {
            // 删除记录
            newGroupList.removeAll(deleteGroupList);
        }
        
        // 新增记录
        if (CollectionUtils.isNotEmpty(addGroupList)) {
            newGroupList.addAll(addGroupList);
        }
        
        // 升序
        oldGroupIds.sort(Comparator.comparing(Long::intValue));
        newGroupList.sort(Comparator.comparing(Long::intValue));
        
        // 修改前后分组相同，则不新增记录
        if (oldGroupIds.equals(newGroupList)) {
            return;
        }
        
        // 新增历史记录
        UserInfoGroupDetailHistory detailHistory = this.assembleDetailHistory(uid, StringUtils.join(oldGroupIds, CommonConstant.STR_COMMA),
                StringUtils.join(newGroupList, CommonConstant.STR_COMMA), operator, franchiseeId, tenantId, UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_OTHER);
        userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
    }
    
    @Override
    public UserInfoGroupDetailHistory assembleDetailHistory(Long uid, String oldGroupIds, String newGroupIds, Long operator, Long franchiseeId, Integer tenantId, Integer type) {
        long nowTime = System.currentTimeMillis();
        
        return UserInfoGroupDetailHistory.builder().uid(uid).oldGroupIds(oldGroupIds).newGroupIds(newGroupIds).operator(operator).franchiseeId(franchiseeId).tenantId(tenantId)
                .createTime(nowTime).updateTime(nowTime).type(type).build();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateV2(UserInfoGroupDetailUpdateRequest request, TokenUser operator) {
        Long uid = request.getUid();
        
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_DETAIL_UPDATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            // 租户校验
            Integer tenantId = TenantContextHolder.getTenantId();
            if (!Objects.equals(tenantId, userInfo.getTenantId())) {
                return R.ok();
            }
            
            HashMap<Long, List<Long>> franchiseeIdAndGroupIds = request.getFranchiseeIdAndGroupIds();
            
            // 加盟商用户修改
            if (Objects.equals(operator.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
                List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(operator.getUid());
                if (CollectionUtils.isEmpty(franchiseeIds)) {
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeIds.get(0));
                if (Objects.isNull(franchisee)) {
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                Long franchiseeId = franchisee.getId();
                List<Long> groupIds = MapUtils.isEmpty(franchiseeIdAndGroupIds) ? null : franchiseeIdAndGroupIds.get(franchiseeId);
                
                return doUpdate(userInfo, franchiseeId, groupIds, operator.getUid());
            }
            
            // 租户修改，需要查询出原来绑定过多少中加盟商的，对原绑定的每个加盟商都需要处理
            List<Long> franchiseeIds = listFranchiseeForUpdate(uid);
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
            
            Set<Long> franchiseeSet = new HashSet<>(franchiseeIds);
            if (MapUtils.isNotEmpty(franchiseeIdAndGroupIds)) {
                franchiseeSet.addAll(franchiseeIdAndGroupIds.keySet());
            }
            
            for (Long franchiseeId : franchiseeSet) {
                if (MapUtils.isNotEmpty(franchiseeIdAndGroupIds) && franchiseeIdAndGroupIds.containsKey(franchiseeId)) {
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
                    if (Objects.isNull(franchisee)) {
                        return R.fail("ELECTRICITY.0038", "未找到加盟商");
                    }
                }
                
                List<Long> groupIds = MapUtils.isEmpty(franchiseeIdAndGroupIds) ? null : franchiseeIdAndGroupIds.get(franchiseeId);
                
                R<Object> doUpdateResult = doUpdate(userInfo, franchiseeId, groupIds, operator.getUid());
                if (!doUpdateResult.isSuccess()) {
                    return doUpdateResult;
                }
            }
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_DETAIL_UPDATE_LOCK + uid);
        }
    }
    
    @Override
    public R unbindUserGroupsInBatches(UserInfoBindGroupRequestV2 request, TokenUser operator) {
        BatchUnbindGroupVO batchUnbindGroupVO = new BatchUnbindGroupVO();
        batchUnbindGroupVO.setNotExistsFranchisee(Collections.emptyList());
        batchUnbindGroupVO.setNotExistsGroups(Collections.emptyList());
        batchUnbindGroupVO.setNotExistPhones(Collections.emptySet());
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_BATCH_UPDATE_LOCK + request.getGroupId(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        List<String> userPhones = request.getUserPhones();
        
        ConcurrentHashSet<String> notExistsPhone = new ConcurrentHashSet<>();
        ConcurrentHashSet<UserInfo> existsPhone = new ConcurrentHashSet<>();
        
        userPhones.parallelStream().forEach(e -> {
            User user = userService.queryByUserPhone(e, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
            if (Objects.isNull(user)) {
                notExistsPhone.add(e);
            } else {
                UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
                existsPhone.add(userInfo);
            }
        });
        
        if (CollectionUtils.isNotEmpty(notExistsPhone)) {
            batchUnbindGroupVO.setNotExistPhones(notExistsPhone);
            //            return R.fail("ELECTRICITY.0001", "未找到用户", notExistsPhone);
        }
        if (CollectionUtils.isEmpty(existsPhone)) {
            return R.ok(batchUnbindGroupVO);
        }
        
        try {
            
            List<UserInfo> userInfos = new ArrayList<>(existsPhone);
            
            // 租户校验（使用全匹配优化）
            List<Integer> userTenantIds = userInfos.stream().map(UserInfo::getTenantId).distinct().collect(Collectors.toList());
            
            if (userTenantIds.size() != 1 || !Objects.equals(tenantId, userTenantIds.get(0))) {
                return R.fail("ELECTRICITY.0001", "租户下未找到用户");
            }
            
            // 统一处理加盟商校验
            Franchisee targetFranchisee = getTargetFranchisee(operator, request.getFranchiseeId(), tenantId);
            if (Objects.isNull(targetFranchisee)) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            // 处理有效用户解绑
            return unbindUserGroups(request, operator, batchUnbindGroupVO, userInfos);
            
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_BATCH_UPDATE_LOCK + request.getGroupId());
        }
    }
    
    // 新增辅助方法
    private Franchisee getTargetFranchisee(TokenUser operator, Long requestFranchiseeId, Integer tenantId) {
        if (operator.getDataType().equals(User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(operator.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return null;
            }
            return franchiseeService.queryByIdFromCache(franchiseeIds.get(0));
        }
        // 租户 加盟商归属校验
        Franchisee byIdFromCache = franchiseeService.queryByIdFromCache(requestFranchiseeId);
        if (!Objects.equals(byIdFromCache.getTenantId(), tenantId)) {
            return null;
        }
        return byIdFromCache;
    }
    
    private R unbindUserGroups(UserInfoBindGroupRequestV2 request, TokenUser operator, BatchUnbindGroupVO batchUnbindGroupVO, List<UserInfo> userInfos) {
        Long franchiseeId = request.getFranchiseeId();
        
        if (CollectionUtils.isEmpty(userInfos)) {
            return R.fail("300850", "该类型用户不存在");
        }
        
        // 分组校验
        UserInfoGroup userInfoGroup = userInfoGroupBizService.queryUserInfoGroupByIdFromCache(request.getGroupId());
        if (Objects.isNull(userInfoGroup)) {
            return R.fail("300850", "该分组不存在");
        }
        if (Objects.nonNull(userInfoGroup) && !Objects.equals(userInfoGroup.getFranchiseeId(), franchiseeId)) {
            log.warn("unbind UserGroups error! groupList is empty or size not equal, groupIds={}", request.getGroupId());
            return R.fail("300850", "该类型用户不存在");
        }
        
        // 创建UID到UserInfo的映射
        Map<Long, UserInfo> userInfoMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getUid, Function.identity()));
        List<Long> uids = new ArrayList<>(userInfoMap.keySet());
        
        List<String> notExistsGroups = new ArrayList<>();
        
        // 查询目前修改 用户下 已存在的用户分组
        List<UserInfoGroupNamesBO> existingUserGroup = this.listGroupByUidList(uids);
        if (CollectionUtils.isEmpty(existingUserGroup)) {
            batchUnbindGroupVO.setNotExistsGroups(userInfos.stream().map(UserInfo::getPhone).collect(Collectors.toList()));
            return R.ok(batchUnbindGroupVO);
        }
        Set<Long> collect = existingUserGroup.stream().map(UserInfoGroupNamesBO::getUid).collect(Collectors.toSet());
        Set<Long> difference = Sets.difference(userInfoMap.keySet(), collect);
        notExistsGroups = userInfos.stream().filter(e -> difference.contains(e.getUid())).map(UserInfo::getPhone).collect(Collectors.toList());
        
        //---- 分组处理
        // 根据用户和groupId分组 uid 为 key
        List<Long> validUids = new ArrayList<>();
        Map<Long, List<Long>> uid2GrepIdsMap = existingUserGroup.stream()
                .collect(Collectors.groupingBy(UserInfoGroupNamesBO::getUid, Collectors.mapping(UserInfoGroupNamesBO::getGroupId, Collectors.toList())));
        List<String> finalNotExistsGroups = notExistsGroups;
        uid2GrepIdsMap.forEach((k, v) -> {
            if (!v.contains(request.getGroupId())) {
                // 非同组
                finalNotExistsGroups.add(userInfoMap.get(k).getPhone());
            }
            validUids.add(k);
            
        });
        log.info("zbzdbug notExistsGroups:{}  ,uid2GrepIdsMap:{}", notExistsGroups, uid2GrepIdsMap);
        if (CollectionUtils.isNotEmpty(notExistsGroups)) {
            batchUnbindGroupVO.setNotExistsGroups(notExistsGroups);
        }
        if (CollectionUtils.isEmpty(validUids)) {
            return R.ok(batchUnbindGroupVO);
        }
        
        Integer deleteCount = deleteForUpdateUids(validUids, Long.valueOf(operator.getTenantId()), franchiseeId, userInfoGroup.getGroupNo());
        
        // 新增历史记录
        ArrayList<UserInfoGroupDetailHistory> UserInfoGroupDetailHistorys = new ArrayList<>();
        for (Long uid : validUids) {
            List<Long> oldGroupIds = uid2GrepIdsMap.get(uid);
            
            List<Long> groupIds = oldGroupIds.stream().filter(e -> !Objects.equals(e, request.getGroupId())).collect(Collectors.toList());
            log.info("zbzdbug oldGroupIds:{}  ,groupIds:{}", oldGroupIds, groupIds);
            UserInfoGroupDetailHistory detailHistory = this.assembleDetailHistoryV2(uid, StringUtils.join(oldGroupIds, CommonConstant.STR_COMMA),
                    StringUtils.join(groupIds, CommonConstant.STR_COMMA), operator.getUid(), franchiseeId, operator.getTenantId());
            UserInfoGroupDetailHistorys.add(detailHistory);
        }
        userInfoGroupDetailHistoryService.batchInsert(UserInfoGroupDetailHistorys);
        return R.ok(batchUnbindGroupVO);
    }
    
    
    @Override
    public R bindGroup(UserInfoBindGroupRequest request, Long operator) {
        Long uid = request.getUid();
        Long franchiseeId = request.getFranchiseeId();
        
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_DETAIL_BIND_GROUP_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            List<Long> groupIds = request.getGroupIds();
            Integer tenantId = TenantContextHolder.getTenantId();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            // 租户校验
            if (!Objects.equals(userInfo.getTenantId(), tenantId)) {
                return R.ok();
            }
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            if (Objects.isNull(franchisee)) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            
            // 超限判断
            if (groupIds.size() > UserInfoGroupConstant.USER_GROUP_LIMIT) {
                return R.fail("120114", "用户绑定的分组数量已达上限10个");
            }
            
            List<UserInfoGroupDetail> list = new ArrayList<>();
            // 超限判断
            Integer limitGroupNum = userInfoGroupDetailMapper.countGroupByUid(uid);
            if (Objects.nonNull(limitGroupNum) && ((limitGroupNum + groupIds.size()) > UserInfoGroupConstant.USER_GROUP_LIMIT)) {
                return R.fail("120114", "用户绑定的分组数量已达上限10个");
            }
            
            long nowTime = System.currentTimeMillis();
            
            groupIds.forEach(groupId -> {
                UserInfoGroup userInfoGroup = userInfoGroupBizService.queryUserInfoGroupByIdFromCache(groupId);
                if (Objects.nonNull(userInfoGroup)) {
                    UserInfoGroupDetail detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(userInfoGroup.getFranchiseeId())
                            .tenantId(tenantId).createTime(nowTime).updateTime(nowTime).operator(operator).build();
                    
                    list.add(detail);
                }
            });
            
            if (CollectionUtils.isNotEmpty(list)) {
                Integer integer = this.batchInsert(list);
                if (integer > 0) {
                    // 新增历史记录
                    UserInfoGroupDetailHistory detailHistory = this.assembleDetailHistory(uid, "", StringUtils.join(groupIds, CommonConstant.STR_COMMA), operator,
                            userInfo.getFranchiseeId(), tenantId, UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_OTHER);
                    userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
                }
            }
            
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_DETAIL_BIND_GROUP_LOCK + uid);
        }
    }
    
    @Override
    public R<Object> bindGroupV2(UserInfoBindGroupRequest request, TokenUser operator) {
        Long uid = request.getUid();
        
        if (MapUtils.isEmpty(request.getFranchiseeIdAndGroupIds())) {
            return R.ok();
        }
        
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_DETAIL_BIND_GROUP_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            for (Map.Entry<Long, List<Long>> entry : request.getFranchiseeIdAndGroupIds().entrySet()) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(entry.getKey());
                if (Objects.isNull(franchisee)) {
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                R<Object> doBindResult = doBind(userInfo, entry.getKey(), entry.getValue(), operator.getUid());
                if (!doBindResult.isSuccess()) {
                    return doBindResult;
                }
            }
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_DETAIL_BIND_GROUP_LOCK + uid);
        }
    }
    
    @Override
    public Integer deleteByUid(Long uid, List<String> groupNoList) {
        return userInfoGroupDetailMapper.deleteByUid(uid, groupNoList);
    }
    
    @Override
    @ProcessParameter(type = 1)
    public R<Object> selectAll(UserInfoGroupDetailQuery query) {
        List<UserInfoGroupNamesBO> userInfoGroupNamesBos = userInfoGroupDetailMapper.selectListGroupByUid(query);
        if (CollectionUtils.isEmpty(userInfoGroupNamesBos)) {
            return R.ok();
        }
        
        HashMap<Long, List<UserInfoGroupNamesBO>> map = new HashMap<>(5);
        List<UserInfoGroupNamesBO> groupBos;
        for (UserInfoGroupNamesBO userInfoGroupNamesBo : userInfoGroupNamesBos) {
            if (!map.containsKey(userInfoGroupNamesBo.getFranchiseeId())) {
                groupBos = new ArrayList<>();
                groupBos.add(userInfoGroupNamesBo);
                map.put(userInfoGroupNamesBo.getFranchiseeId(), groupBos);
            } else {
                map.get(userInfoGroupNamesBo.getFranchiseeId()).add(userInfoGroupNamesBo);
            }
        }
        
        List<UserInfoGroupForUserVO> userInfoGroupForUserVos = new ArrayList<>();
        map.forEach((key, value) -> {
            UserInfoGroupForUserVO userInfoGroupForUserVO = new UserInfoGroupForUserVO();
            userInfoGroupForUserVO.setFranchiseeId(key);
            Franchisee franchisee = franchiseeService.queryByIdFromCache(key);
            userInfoGroupForUserVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            userInfoGroupForUserVO.setUserInfoGroupNames(value);
            
            userInfoGroupForUserVos.add(userInfoGroupForUserVO);
        });
        
        return R.ok(userInfoGroupForUserVos);
    }
    
    @Override
    public Integer deleteForUpdate(Long uid, Long tenantId, Long franchiseeId) {
        return userInfoGroupDetailMapper.deleteForUpdate(uid, tenantId, franchiseeId);
    }
    
    @Override
    public Integer deleteForUpdateUids(List<Long> uids, Long tenantId, Long franchiseeId, String groupNo) {
        return userInfoGroupDetailMapper.deleteForUpdateUids(uids, tenantId, franchiseeId, groupNo);
    }
    
    @Override
    public List<Long> listFranchiseeForUpdate(Long uid) {
        return userInfoGroupDetailMapper.selectListFranchiseeForUpdate(uid);
    }
    
    private R<Object> doBind(UserInfo userInfo, Long franchiseeId, List<Long> groupIds, Long operatorId) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 租户校验
        if (!Objects.equals(userInfo.getTenantId(), tenantId)) {
            return R.ok();
        }
        
        // 超限判断
        if (groupIds.size() > UserInfoGroupConstant.USER_GROUP_LIMIT) {
            return R.fail("120114", "用户绑定的分组数量已达上限10个");
        }
        
        List<UserInfoGroupDetail> list = new ArrayList<>();
        // 超限判断
        Integer limitGroupNum = userInfoGroupDetailMapper.countGroupByUidAndFranchisee(userInfo.getUid(), franchiseeId);
        if (Objects.nonNull(limitGroupNum) && ((limitGroupNum + groupIds.size()) > UserInfoGroupConstant.USER_GROUP_LIMIT)) {
            return R.fail("120114", "用户绑定的分组数量已达上限10个");
        }
        
        long nowTime = System.currentTimeMillis();
        
        groupIds.forEach(groupId -> {
            UserInfoGroup userInfoGroup = userInfoGroupBizService.queryUserInfoGroupByIdFromCache(groupId);
            if (Objects.nonNull(userInfoGroup)) {
                UserInfoGroupDetail detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(userInfo.getUid()).franchiseeId(franchiseeId).tenantId(tenantId)
                        .createTime(nowTime).updateTime(nowTime).operator(operatorId).build();
                
                list.add(detail);
            }
        });
        
        if (CollectionUtils.isNotEmpty(list)) {
            Integer integer = userInfoGroupDetailMapper.batchInsert(list);
            if (integer > 0) {
                // 新增历史记录
                UserInfoGroupDetailHistory detailHistory = this.assembleDetailHistoryV2(userInfo.getUid(), "", StringUtils.join(groupIds, CommonConstant.STR_COMMA), operatorId,
                        franchiseeId, tenantId);
                userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
            }
        }
        
        return R.ok();
    }
    
    private R<Object> doUpdate(UserInfo userInfo, Long franchiseeId, List<Long> groupIds, Long operatorId) {
        Long uid = userInfo.getUid();
        
        // 查询目前已存在的用户分组
        List<UserInfoGroupNamesBO> existGroupList = this.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(uid).franchiseeId(franchiseeId).build());
        List<Long> oldGroupIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(existGroupList)) {
            // 分组ids
            oldGroupIds = existGroupList.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
        }
        
        // 如果没有新的分组，则直接保存历史记录并返回
        if (CollectionUtils.isEmpty(groupIds)) {
            if (CollectionUtils.isNotEmpty(existGroupList)) {
                // 删除旧绑定数据
                Integer delete = deleteForUpdate(uid, null, franchiseeId);
                
                UserInfoGroupDetailHistory detailHistory = assembleDetailHistoryV2(uid, StringUtils.join(oldGroupIds, CommonConstant.STR_COMMA), "", operatorId, franchiseeId,
                        userInfo.getTenantId());
                
                if (delete > 0) {
                    // 新增历史记录
                    userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
                }
            }
            return R.ok();
        }
        
        List<UserInfoGroupBO> groupList = userInfoGroupBizService.listUserInfoGroupByIds(groupIds);
        if (!Objects.equals(groupList.size(), groupIds.size())) {
            log.warn("Update userInfoGroupDetail error! groupList is empty or size not equal, groupIds={}", groupIds);
            return R.fail("120112", "未找到用户分组");
        }
        
        // 加盟商校验
        List<UserInfoGroupBO> notSameFranchiseeGroups = groupList.stream().filter(item -> !Objects.equals(item.getFranchiseeId(), franchiseeId)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notSameFranchiseeGroups)) {
            List<Long> differentGroupIds = notSameFranchiseeGroups.stream().map(UserInfoGroupBO::getId).collect(Collectors.toList());
            log.warn("Update userInfoGroupDetail error! has different franchisee, different groupIds={}", differentGroupIds);
            return R.fail("120112", "未找到用户分组");
        }
        
        // 升序
        oldGroupIds.sort(Comparator.comparing(Long::intValue));
        groupIds.sort(Comparator.comparing(Long::intValue));
        
        // 修改前后分组相同，则不新增记录
        if (oldGroupIds.equals(groupIds)) {
            return R.ok();
        }
        
        // 删除旧绑定数据
        Integer delete = deleteForUpdate(uid, null, franchiseeId);
        
        // 保存新的用户分组
        long nowTime = System.currentTimeMillis();
        List<UserInfoGroupDetail> detailList = groupIds.stream().map(groupId -> {
            UserInfoGroup userInfoGroup = userInfoGroupBizService.queryUserInfoGroupByIdFromCache(groupId);
            
            UserInfoGroupDetail detail = null;
            if (Objects.nonNull(userInfoGroup)) {
                detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(userInfoGroup.getFranchiseeId()).tenantId(userInfo.getTenantId())
                        .createTime(nowTime).updateTime(nowTime).operator(operatorId).build();
            }
            
            return detail;
        }).collect(Collectors.toList());
        
        List<UserInfoGroupDetail> insertList = detailList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        userInfoGroupDetailMapper.batchInsert(insertList);
        
        // 新增历史记录
        UserInfoGroupDetailHistory detailHistory = this.assembleDetailHistoryV2(uid, StringUtils.join(oldGroupIds, CommonConstant.STR_COMMA),
                StringUtils.join(groupIds, CommonConstant.STR_COMMA), operatorId, franchiseeId, userInfo.getTenantId());
        userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
        
        return R.ok();
    }
    
    private UserInfoGroupDetailHistory assembleDetailHistoryV2(Long uid, String oldGroupIds, String newGroupIds, Long operator, Long franchiseeId, Integer tenantId) {
        long nowTime = System.currentTimeMillis();
        
        return UserInfoGroupDetailHistory.builder().uid(uid).oldGroupIds(oldGroupIds).newGroupIds(newGroupIds).operator(operator).franchiseeId(franchiseeId).tenantId(tenantId)
                .createTime(nowTime).updateTime(nowTime).type(UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_OTHER).build();
    }
    
    @Slave
    @Override
    public Integer existsByUid(Long uid) {
        return userInfoGroupDetailMapper.existsByUid(uid);
    }
}
