package com.xiliulou.electricity.service.impl.userinfo.userInfoGroup;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailPageBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupIdAndNameBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.UserInfoGroupConstant;
import com.xiliulou.electricity.entity.Franchisee;
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
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:45:00
 */
@Slf4j
@Service
public class UserInfoGroupDetailServiceImpl implements UserInfoGroupDetailService {
    
    @Resource
    private UserInfoGroupDetailMapper userInfoGroupDetailMapper;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private UserInfoGroupService userInfoGroupService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private UserInfoGroupDetailHistoryService userInfoGroupDetailHistoryService;
    
    @Resource
    private UserBizService userBizService;
    
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
    
    @Override
    public Integer batchInsert(List<UserInfoGroupDetail> detailList) {
        return userInfoGroupDetailMapper.batchInsert(detailList);
    }
    
    @Slave
    @Override
    public Integer countUserByGroupId(Long id) {
        return userInfoGroupDetailMapper.countUserByGroupId(id);
    }
    
    @Slave
    @Override
    public Integer countGroupByUid(Long uid) {
        return userInfoGroupDetailMapper.countGroupByUid(uid);
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
            UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
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
            
            List<UserInfoGroupBO> groupList = userInfoGroupService.listByIds(groupIds);
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
                    UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(groupId);
                    
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
    
    private UserInfoGroupDetailHistory assembleDetailHistory(Long uid, String oldGroupIds, String newGroupIds, Long operator, Long franchiseeId, Integer tenantId, Integer type) {
        long nowTime = System.currentTimeMillis();
        
        return UserInfoGroupDetailHistory.builder().uid(uid).oldGroupIds(oldGroupIds).newGroupIds(newGroupIds).operator(operator).franchiseeId(franchiseeId).tenantId(tenantId)
                .createTime(nowTime).updateTime(nowTime).type(type).build();
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
            
            UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
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
                UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(groupId);
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
    public Integer deleteByUid(Long uid, List<String> groupNoList) {
        return userInfoGroupDetailMapper.deleteByUid(uid, groupNoList);
    }
    
    @Override
    public void handleAfterRefundDeposit(Long uid) {
        boolean depositStatus = userBizService.isBoundDeposit(uid);
        if (depositStatus) {
            return;
        }
    
        List<UserInfoGroupNamesBO> groupList = this.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(uid).build());
        Integer delete = 0;
        if (CollectionUtils.isNotEmpty(groupList)) {
            delete = this.deleteByUid(uid, null);
        }
    
        if (delete > 0) {
            AtomicReference<Long> franchiseeId = new AtomicReference<>(NumberConstant.ZERO_L);
            AtomicReference<Integer> tenantId = new AtomicReference<>(NumberConstant.ZERO);
        
            Optional<UserInfo> optionalUserInfo = Optional.ofNullable(userInfoService.queryByUidFromCache(uid));
            optionalUserInfo.ifPresent(u -> {
                franchiseeId.set(Objects.isNull(u.getFranchiseeId()) ? NumberConstant.ZERO_L : u.getFranchiseeId());
                tenantId.set(Objects.isNull(u.getTenantId()) ? NumberConstant.ZERO : u.getTenantId());
            });
        
            String oldGroupIds = groupList.stream().map(g -> g.getGroupId().toString()).collect(Collectors.joining(CommonConstant.STR_COMMA));
            UserInfoGroupDetailHistory detail = this.assembleDetailHistory(uid, oldGroupIds, "", uid, franchiseeId.get(), tenantId.get(),
                    UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_REFUND_DEPOSIT);
        
            userInfoGroupDetailHistoryService.insertOne(detail);
        }
    }
    
    @Override
    public Integer deleteByGroupNo(String groupNo, Integer tenantId) {
        return userInfoGroupDetailMapper.deleteByGroupNo(groupNo, tenantId);
    }
    
}
