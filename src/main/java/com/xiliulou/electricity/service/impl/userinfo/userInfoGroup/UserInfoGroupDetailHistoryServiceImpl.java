package com.xiliulou.electricity.service.impl.userinfo.userInfoGroup;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailHistoryBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupIdAndNameBO;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.UserInfoGroupConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.mapper.userinfo.userInfoGroup.UserInfoGroupDetailHistoryMapper;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailHistoryQuery;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
 * @description 用户分组详情修改记录
 * @date 2024/4/15 09:21:47
 */

@Service
public class UserInfoGroupDetailHistoryServiceImpl implements UserInfoGroupDetailHistoryService {
    
    @Resource
    private UserInfoGroupDetailHistoryMapper userInfoGroupDetailHistoryMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserInfoGroupService userInfoGroupService;
    
    ;
    
    @Override
    public Integer batchInsert(List<UserInfoGroupDetailHistory> detailHistoryList) {
        return userInfoGroupDetailHistoryMapper.batchInsert(detailHistoryList);
    }
    
    @Slave
    @Override
    public List<UserInfoGroupDetailHistoryBO> listByPage(UserInfoGroupDetailHistoryQuery query) {
        
        List<UserInfoGroupDetailHistory> detailHistories = userInfoGroupDetailHistoryMapper.selectListByPage(query);
        if (detailHistories.isEmpty()) {
            return Collections.emptyList();
        }
        
        return detailHistories.stream().map(item -> {
            UserInfoGroupDetailHistoryBO bo = new UserInfoGroupDetailHistoryBO();
            BeanUtils.copyProperties(item, bo);
            bo.setOldGroupList(getGroupNames(item.getOldGroupIds()));
            bo.setNewGroupList(getGroupNames(item.getNewGroupIds()));
            
            if (Objects.equals(item.getType(), UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_REFUND_DEPOSIT)) {
                bo.setOperatorName(UserInfoGroupConstant.USER_GROUP_HISTORY_TYPE_REFUND_DEPOSIT_NAME);
            } else {
                bo.setOperatorName(Optional.ofNullable(userService.queryByUidFromCache(item.getOperator())).map(User::getName).orElse(""));
            }
            
            bo.setOperatorTime(item.getCreateTime());
            
            return bo;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(UserInfoGroupDetailHistoryQuery query) {
        return userInfoGroupDetailHistoryMapper.countTotal(query);
    }
    
    @Override
    public Integer insertOne(UserInfoGroupDetailHistory detail) {
        return userInfoGroupDetailHistoryMapper.insertOne(detail);
    }
    
    private List<UserInfoGroupIdAndNameBO> getGroupNames(String groupIds) {
        if (StringUtils.isBlank(groupIds)) {
            return Collections.emptyList();
        }
        
        List<UserInfoGroupIdAndNameBO> boList = new ArrayList<>();
        String[] split = groupIds.split(CommonConstant.STR_COMMA);
        for (String id : split) {
            UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(Long.valueOf(id));
            if (userInfoGroup != null) {
                UserInfoGroupIdAndNameBO bo = UserInfoGroupIdAndNameBO.builder().id(Long.valueOf(id)).name(userInfoGroup.getName()).build();
                boList.add(bo);
            }
        }
        
        if (CollectionUtils.isEmpty(boList)) {
            return Collections.emptyList();
        }
        
        return boList;
    }
}
