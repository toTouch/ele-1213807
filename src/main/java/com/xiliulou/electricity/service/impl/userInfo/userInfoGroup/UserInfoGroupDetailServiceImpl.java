package com.xiliulou.electricity.service.impl.userInfo.userInfoGroup;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.mapper.userInfo.userInfoGroup.UserInfoGroupDetailMapper;
import com.xiliulou.electricity.query.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupDetailVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupNamesVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:45:00
 */
@Service
public class UserInfoGroupDetailServiceImpl implements UserInfoGroupDetailService {
    
    @Resource
    private UserInfoGroupDetailMapper userInfoGroupDetailMapper;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Slave
    @Override
    public UserInfoGroupDetail queryByUid(String groupNo, Long uid, Integer tenantId) {
        return userInfoGroupDetailMapper.selectByUid(groupNo, uid, tenantId);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupDetailVO> listByPage(UserInfoGroupDetailQuery query) {
        List<UserInfoGroupDetail> list = userInfoGroupDetailMapper.selectPage(query);
        
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list.stream().map(item -> {
            UserInfoGroupDetailVO detailVO = new UserInfoGroupDetailVO();
            Long uid = item.getUid();
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            
            detailVO.setId(item.getId());
            detailVO.setUid(uid);
            detailVO.setUserName(Optional.ofNullable(userInfo).orElse(new UserInfo()).getName());
            detailVO.setPhone(Optional.ofNullable(userInfo).orElse(new UserInfo()).getPhone());
            detailVO.setFranchiseeId(item.getFranchiseeId());
            detailVO.setFranchiseeName(Optional.ofNullable(franchiseeService.queryByIdFromCache(item.getFranchiseeId())).orElse(new Franchisee()).getName());
            detailVO.setUpdateTime(item.getUpdateTime());
            
            Set<String> groupNames = new HashSet<>();
            List<UserInfoGroupNamesVO> groupNameDetails = userInfoGroupDetailMapper.selectListByUid(uid, query.getTenantId());
            if (CollectionUtils.isNotEmpty(groupNameDetails)) {
                List<UserInfoGroupNamesVO> nameDetail = groupNameDetails.stream().filter(detail -> detail.getGroupNo().equals(item.getGroupNo())).collect(Collectors.toList());
                Optional.of(nameDetail.get(0)).ifPresent(d -> {
                    groupNames.add(d.getGroupName());
                });
                
                groupNameDetails.removeAll(nameDetail);
                
                Set<String> nameSet = groupNameDetails.stream().map(UserInfoGroupNamesVO::getGroupName).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(nameSet)) {
                    groupNames.addAll(nameSet);
                }
            }
            
            detailVO.setGroupNames(List.copyOf(groupNames));
            
            if (CollectionUtils.isEmpty(groupNames)) {
                detailVO.setGroupNames(Collections.emptyList());
            }
            
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
    
}
