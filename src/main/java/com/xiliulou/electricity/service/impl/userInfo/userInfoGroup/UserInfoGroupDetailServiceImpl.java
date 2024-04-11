package com.xiliulou.electricity.service.impl.userInfo.userInfoGroup;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.UserGroupConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.mapper.userInfo.userInfoGroup.UserInfoGroupDetailMapper;
import com.xiliulou.electricity.query.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.user.UserInfoBindGroupRequest;
import com.xiliulou.electricity.request.user.UserInfoGroupDetailUpdateRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupDetailPageVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupDetailVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupIdAndNameVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupNamesVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
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
    
    @Slave
    @Override
    public UserInfoGroupDetail queryByUid(String groupNo, Long uid, Integer tenantId) {
        return userInfoGroupDetailMapper.selectByUid(groupNo, uid, tenantId);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupDetailPageVO> listByPage(UserInfoGroupDetailQuery query) {
        List<UserInfoGroupDetailVO> list = userInfoGroupDetailMapper.selectPage(query);
        
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        List<Long> uidList = list.stream().map(UserInfoGroupDetailVO::getUid).collect(Collectors.toList());
        List<UserInfoGroupNamesVO> listByUidList = this.listGroupByUidList(uidList);
        // 根据uid进行分组
        Map<Long, List<UserInfoGroupNamesVO>> groupMap = listByUidList.stream().collect(Collectors.groupingBy(UserInfoGroupNamesVO::getUid));
        
        return list.stream().filter(Objects::nonNull).map(item -> {
            UserInfoGroupDetailPageVO detailVO = new UserInfoGroupDetailPageVO();
            Long uid = item.getUid();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            
            detailVO.setId(item.getId());
            detailVO.setUid(uid);
            detailVO.setUserName(Optional.ofNullable(userInfo).orElse(new UserInfo()).getName());
            detailVO.setPhone(Optional.ofNullable(userInfo).orElse(new UserInfo()).getPhone());
            detailVO.setFranchiseeId(item.getFranchiseeId());
            detailVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getFranchiseeId())).orElse(new Franchisee()).getName());
            detailVO.setUpdateTime(item.getUpdateTime());
            
            List<UserInfoGroupIdAndNameVO> groups = new ArrayList<>();
            groupMap.forEach((k, v) -> {
                if (k.equals(uid)) {
                    v.forEach(i -> {
                        UserInfoGroupIdAndNameVO idAndNameVO = UserInfoGroupIdAndNameVO.builder().id(i.getGroupId()).name(i.getGroupName()).groupNo(i.getGroupNo()).build();
                        groups.add(idAndNameVO);
                    });
                }
            });
            
            detailVO.setGroups(groups);
            
            return detailVO;
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
    public List<UserInfoGroupNamesVO> listGroupByUid(UserInfoGroupDetailQuery query) {
        return userInfoGroupDetailMapper.selectListGroupByUid(query);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupNamesVO> listGroupByUidList(List<Long> uidList) {
        return userInfoGroupDetailMapper.selectListGroupByUidList(uidList);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R update(UserInfoGroupDetailUpdateRequest request) {
        Long uid = request.getUid();
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
            
            Integer tenantId = TenantContextHolder.getTenantId();
            
            if (!Objects.equals(tenantId, userInfo.getTenantId())) {
                return R.ok();
            }
            
            // 如果没有分组，则删除
            if (CollectionUtils.isEmpty(groupIds)) {
                userInfoGroupDetailMapper.deleteByUidAndGroupNoList(uid, null);
                return R.ok();
            }
            
            List<UserInfoGroupVO> groupList = userInfoGroupService.listByIds(groupIds);
            if (!Objects.equals(groupList.size(), groupIds.size())) {
                log.warn("Update userInfoGroupDetail error! groupList is empty or size not equal, groupIds={}", groupIds);
                return R.fail("120112", "未找到用户分组");
            }
            
            List<UserInfoGroupVO> notSameFranchiseeGroups = groupList.stream().filter(item -> !Objects.equals(item.getFranchiseeId(), userInfo.getFranchiseeId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(notSameFranchiseeGroups)) {
                List<Long> differentGroupIds = notSameFranchiseeGroups.stream().map(UserInfoGroupVO::getId).collect(Collectors.toList());
                log.warn("Update userInfoGroupDetail error! has different franchisee, different groupIds={}", differentGroupIds);
                return R.fail("120112", "未找到用户分组");
            }
            
            UserInfoGroupDetailQuery query = UserInfoGroupDetailQuery.builder().uid(uid).tenantId(tenantId).build();
            List<UserInfoGroupNamesVO> userInfoGroupNamesVOList = this.listGroupByUid(query);
            List<Long> intersection = new ArrayList<>(groupIds);
            if (CollectionUtils.isNotEmpty(userInfoGroupNamesVOList)) {
                // 获取交集
                intersection.retainAll(userInfoGroupNamesVOList.stream().map(UserInfoGroupNamesVO::getGroupId).collect(Collectors.toList()));
                
                // 去除交集后，剩下的就是需要新增的
                groupIds.removeAll(intersection);
                
                // 去除交集后，剩下的是需要删除的
                userInfoGroupNamesVOList.removeAll(userInfoGroupNamesVOList.stream().filter(item -> intersection.contains(item.getGroupId())).collect(Collectors.toList()));
            }
            
            // 处理新增
            List<UserInfoGroupDetail> insertList = null;
            if (CollectionUtils.isNotEmpty(groupIds)) {
                if ((intersection.size() + groupIds.size()) > UserGroupConstant.USER_GROUP_LIMIT) {
                    return R.fail("120114", "用户绑定的分组数量已达上限10个");
                }
                
                long nowTime = System.currentTimeMillis();
                
                List<UserInfoGroupDetail> detailList = groupIds.stream().map(groupId -> {
                    UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(groupId);
                    
                    UserInfoGroupDetail detail = null;
                    if (Objects.nonNull(userInfoGroup)) {
                        detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(userInfoGroup.getFranchiseeId()).tenantId(tenantId)
                                .createTime(nowTime).updateTime(nowTime).build();
                    }
                    
                    return detail;
                }).collect(Collectors.toList());
                
                insertList = detailList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            
            // 处理持久化
            handleGroupDetailDb(uid, insertList, userInfoGroupNamesVOList);
            
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.CACHE_USER_GROUP_DETAIL_UPDATE_LOCK + uid);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void handleGroupDetailDb(Long uid, List<UserInfoGroupDetail> insertList, List<UserInfoGroupNamesVO> userInfoGroupNamesVOList) {
        // 新增
        if (CollectionUtils.isNotEmpty(insertList)) {
            userInfoGroupDetailMapper.batchInsert(insertList);
        }
        
        // 删除
        if (CollectionUtils.isNotEmpty(userInfoGroupNamesVOList)) {
            List<String> deleteGroupNoList = userInfoGroupNamesVOList.stream().map(UserInfoGroupNamesVO::getGroupNo).collect(Collectors.toList());
            userInfoGroupDetailMapper.deleteByUidAndGroupNoList(uid, deleteGroupNoList);
        }
    }
    
    @Override
    public R bindGroup(UserInfoBindGroupRequest request) {
        Long uid = request.getUid();
        List<Long> groupIds = request.getGroupIds();
        Integer tenantId = TenantContextHolder.getTenantId();
        
        UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!Objects.equals(userInfo.getTenantId(), tenantId)) {
            return R.ok();
        }
        
        // 超限判断
        if (groupIds.size() > UserGroupConstant.USER_GROUP_LIMIT) {
            return R.fail("120114", "用户绑定的分组数量已达上限10个");
        }
    
        List<Long> filterGroupIds = null;
        UserInfoGroupDetailQuery query = UserInfoGroupDetailQuery.builder().tenantId(tenantId).uid(uid).build();
        List<UserInfoGroupNamesVO> userInfoGroupNamesList = userInfoGroupDetailMapper.selectListGroupByUid(query);
        if (CollectionUtils.isNotEmpty(userInfoGroupNamesList)) {
            List<Long> existGroupIds = userInfoGroupNamesList.stream().map(UserInfoGroupNamesVO::getGroupId).collect(Collectors.toList());
            filterGroupIds = groupIds.stream().filter(groupId -> !existGroupIds.contains(groupId)).collect(Collectors.toList());
        }
    
        List<UserInfoGroupDetail> list = new ArrayList<>();
        
        if (CollectionUtils.isNotEmpty(filterGroupIds)) {
            // 超限判断
            Integer limitGroupNum = userInfoGroupDetailMapper.countGroupByUid(uid);
            if (Objects.nonNull(limitGroupNum) && ((limitGroupNum + filterGroupIds.size()) > UserGroupConstant.USER_GROUP_LIMIT)) {
                return R.fail("120114", "用户绑定的分组数量已达上限10个");
            }
            
            long nowTime = System.currentTimeMillis();
            
            groupIds.parallelStream().forEach(groupId -> {
                UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(groupId);
                if (Objects.nonNull(userInfoGroup)) {
                    UserInfoGroupDetail detail = UserInfoGroupDetail.builder().groupNo(userInfoGroup.getGroupNo()).uid(uid).franchiseeId(userInfoGroup.getFranchiseeId())
                            .tenantId(tenantId).createTime(nowTime).updateTime(nowTime).build();
            
                    list.add(detail);
                }
            });
        }
        
        if (CollectionUtils.isNotEmpty(list)) {
            this.batchInsert(list);
        }
        
        return R.ok();
    }
    
}
