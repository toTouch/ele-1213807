package com.xiliulou.electricity.service.impl.userinfo.userInfoGroup;

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
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupDetailUpdateRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.UserInfoGroupForUserVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:45:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoGroupDetailServiceImpl implements UserInfoGroupDetailService {
    
    private final UserInfoGroupDetailMapper userInfoGroupDetailMapper;
    
    private final UserInfoService userInfoService;
    
    private final FranchiseeService franchiseeService;
    
    private final UserInfoGroupService userInfoGroupService;
    
    private final RedisService redisService;
    
    private final UserInfoGroupDetailHistoryService userInfoGroupDetailHistoryService;
    
    
    @Slave
    @Override
    public UserInfoGroupDetail queryByUid(String groupNo, Long uid, Integer tenantId) {
        return userInfoGroupDetailMapper.selectByUid(groupNo, uid, tenantId);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupDetailPageBO> listByPage(UserInfoGroupDetailQuery query) {
        List<UserInfoGroupDetailBO> list = userInfoGroupDetailMapper.selectPage(query);
        
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        List<Long> uidList = list.stream().map(UserInfoGroupDetailBO::getUid).collect(Collectors.toList());
        List<UserInfoGroupNamesBO> listByUidList = this.listGroupByUidList(uidList);
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
    
    @Slave
    @Override
    public Integer countUserByGroupId(Long id) {
        return userInfoGroupDetailMapper.countUserByGroupId(id);
    }
    
    @Slave
    @Override
    public Integer countGroupByUidAndFranchisee(Long uid, Long franchiseeId) {
        return userInfoGroupDetailMapper.countGroupByUidAndFranchisee(uid, franchiseeId);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupNamesBO> listGroupByUid(UserInfoGroupDetailQuery query) {
        return userInfoGroupDetailMapper.selectListGroupByUid(query);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupNamesBO> listGroupByUidList(List<Long> uidList) {
        return userInfoGroupDetailMapper.selectListGroupByUidList(uidList);
    }
    
    @Override
    @Transactional
    public R update(UserInfoGroupDetailUpdateRequest request, TokenUser operator) {
        Long uid = request.getUid();
        
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_DETAIL_UPDATE_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
            if (Objects.isNull(userInfo)) {
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            // 租户校验
            Integer tenantId = TenantContextHolder.getTenantId();
            if (!Objects.equals(tenantId, userInfo.getTenantId())) {
                return R.ok();
            }
            
            // 加盟商用户修改
            if (CollectionUtils.isNotEmpty(request.getGroupIds())) {
                Franchisee franchisee = getFranchiseeForUser(operator, request.getFranchiseeId());
                if (Objects.isNull(franchisee)) {
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                Long franchiseeId = franchisee.getId();
                return doUpdate(userInfo, franchiseeId, request.getGroupIds(), operator.getUid());
            }
            
            // 租户修改
            for (Map.Entry<Long, List<Long>> entry : request.getFranchiseeIdAndGroupIds().entrySet()) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(entry.getKey());
                if (Objects.isNull(franchisee)) {
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                R<Object> doUpdateResult = doUpdate(userInfo, entry.getKey(), entry.getValue(), operator.getUid());
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
    public R<Object> bindGroup(UserInfoBindGroupRequest request, TokenUser operator) {
        Long uid = request.getUid();
        Long franchiseeId = request.getFranchiseeId();
        
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_GROUP_DETAIL_BIND_GROUP_LOCK + uid, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
            if (Objects.isNull(userInfo)) {
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            
            // 加盟商用户绑定
            if (CollectionUtils.isNotEmpty(request.getGroupIds())) {
                Franchisee franchisee = getFranchiseeForUser(operator, request.getFranchiseeId());
                if (Objects.isNull(franchisee)) {
                    return R.fail("ELECTRICITY.0038", "未找到加盟商");
                }
                
                return doBind(userInfo, franchiseeId, request.getGroupIds(), operator.getUid());
            }
            
            // 租户绑定
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
            UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(groupId);
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
                UserInfoGroupDetailHistory detailHistory = this.assembleDetailHistory(userInfo.getUid(), "", StringUtils.join(groupIds, CommonConstant.STR_COMMA), operatorId,
                        franchiseeId, tenantId);
                userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
            }
        }
        
        return R.ok();
    }
    
    private R<Object> doUpdate(UserInfo userInfo, Long franchiseeId, List<Long> groupIds, Long operatorId) {
        Long uid = userInfo.getUid();
        
        // 如果没有分组，则删除
        if (CollectionUtils.isEmpty(groupIds)) {
            List<UserInfoGroupNamesBO> existGroupList = this.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(uid).franchiseeId(franchiseeId).build());
            if (CollectionUtils.isNotEmpty(existGroupList)) {
                String oldGroupIds = existGroupList.stream().map(g -> g.getGroupId().toString()).collect(Collectors.joining(CommonConstant.STR_COMMA));
                UserInfoGroupDetailHistory detailHistory = assembleDetailHistory(uid, oldGroupIds, "", operatorId, franchiseeId, userInfo.getTenantId());
                
                Integer delete = userInfoGroupDetailMapper.deleteByUid(uid, null);
                if (delete > 0) {
                    // 新增历史记录
                    userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
                }
                return R.ok();
            }
        }
        
        List<UserInfoGroupBO> groupList = userInfoGroupService.listByIds(groupIds);
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
        
        List<UserInfoGroupNamesBO> existGroupList = this.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(uid).franchiseeId(franchiseeId).build());
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
                UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(groupId);
                
                UserInfoGroupDetail detail = null;
                if (Objects.nonNull(userInfoGroup)) {
                    detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(franchiseeId).tenantId(userInfo.getTenantId())
                            .createTime(nowTime).updateTime(nowTime).operator(operatorId).build();
                    
                    addGroupList.add(groupId);
                }
                
                return detail;
            }).collect(Collectors.toList());
            
            insertList = detailList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        
        // 持久化detail
        handleGroupDetailDb(uid, insertList, existGroupList, addGroupList, oldGroupIds, operatorId, franchiseeId,
                userInfo.getTenantId());
        return R.ok();
    }
    
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
            userInfoGroupDetailMapper.deleteByUid(uid, deleteGroupNoList);
            
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
                StringUtils.join(newGroupList, CommonConstant.STR_COMMA), operator, franchiseeId, tenantId);
        userInfoGroupDetailHistoryService.batchInsert(List.of(detailHistory));
    }
    
    private UserInfoGroupDetailHistory assembleDetailHistory(Long uid, String oldGroupIds, String newGroupIds, Long operator, Long franchiseeId, Integer tenantId) {
        long nowTime = System.currentTimeMillis();
        
        return UserInfoGroupDetailHistory.builder().uid(uid).oldGroupIds(oldGroupIds).newGroupIds(newGroupIds).operator(operator).franchiseeId(franchiseeId).tenantId(tenantId)
                .createTime(nowTime).updateTime(nowTime).type(UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_OTHER).build();
    }
    
    private Franchisee getFranchiseeForUser(TokenUser operator, Long franchiseeId) {
        Franchisee franchisee;
        if (Objects.equals(operator.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchisee = franchiseeService.queryByUid(operator.getUid());
        } else {
            franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        }
        return franchisee;
    }
}
