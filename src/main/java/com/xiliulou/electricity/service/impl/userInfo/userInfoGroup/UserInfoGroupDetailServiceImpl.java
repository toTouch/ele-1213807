package com.xiliulou.electricity.service.impl.userInfo.userInfoGroup;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.mapper.userInfo.userInfoGroup.UserInfoGroupDetailMapper;
import com.xiliulou.electricity.query.UserInfoGroupDetailQuery;
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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            
            UserInfoGroupIdAndNameVO groupVo1 = UserInfoGroupIdAndNameVO.builder().id(item.getGroupId()).name(item.getGroupName()).groupNo(item.getGroupNo()).build();
            List<UserInfoGroupIdAndNameVO> groups = new ArrayList<>(List.of(groupVo1));
            
            UserInfoGroupDetailQuery detailQuery = UserInfoGroupDetailQuery.builder().uid(uid).tenantId(query.getTenantId()).build();
            List<UserInfoGroupNamesVO> groupNameDetails = userInfoGroupDetailMapper.selectListGroupByUid(detailQuery);
            if (CollectionUtils.isNotEmpty(groupNameDetails)) {
                List<UserInfoGroupNamesVO> filterGroups = groupNameDetails.stream().filter(i -> !Objects.equals(i.getGroupId(), item.getGroupId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterGroups)) {
                    filterGroups.parallelStream().forEach(group -> {
                        UserInfoGroupIdAndNameVO groupVO = UserInfoGroupIdAndNameVO.builder().id(group.getGroupId()).name(group.getGroupName()).groupNo(group.getGroupNo()).build();
                        groups.add(groupVO);
                    });
                }
            }
            
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
    public List<UserInfoGroupNamesVO> listGroupByUid(UserInfoGroupDetailQuery query) {
        return userInfoGroupDetailMapper.selectListGroupByUid(query);
    }
    
    @Override
    public R update(UserInfoGroupDetailUpdateRequest request) {
        Long uid = request.getUid();
        List<Long> groupIds = request.getGroupIds();
        
        UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (!Objects.equals(tenantId, userInfo.getTenantId())) {
            return R.ok();
        }
        
        List<UserInfoGroupVO> groupList = userInfoGroupService.listByIds(groupIds);
        if (CollectionUtils.isEmpty(groupList) || !Objects.equals(groupList.size(), groupIds.size())) {
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
        if (CollectionUtils.isNotEmpty(userInfoGroupNamesVOList)) {
            // 获取交集
            List<Long> intersection = new ArrayList<>(groupIds);
            intersection.retainAll(userInfoGroupNamesVOList.stream().map(UserInfoGroupNamesVO::getGroupId).collect(Collectors.toList()));
            
            // 去除交集后，剩下的就是需要新增的
            groupIds.removeAll(intersection);
            
            // 去除交集后，剩下的是需要删除的
            userInfoGroupNamesVOList.removeAll(userInfoGroupNamesVOList.stream().filter(item -> intersection.contains(item.getGroupId())).collect(Collectors.toList()));
        }
        
        // 新增
        if (CollectionUtils.isNotEmpty(groupIds)) {
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
            
            List<UserInfoGroupDetail> insertList = detailList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            
            if (CollectionUtils.isNotEmpty(insertList)) {
                userInfoGroupDetailMapper.batchInsert(insertList);
            }
        }
        
        // 删除
        if (CollectionUtils.isNotEmpty(userInfoGroupNamesVOList)) {
            List<String> deleteGroupNoList = userInfoGroupNamesVOList.stream().map(UserInfoGroupNamesVO::getGroupNo).collect(Collectors.toList());
            userInfoGroupDetailMapper.deleteByUidAndGroupNoList(uid, deleteGroupNoList);
        }
        
        return R.ok();
    }
    
}
