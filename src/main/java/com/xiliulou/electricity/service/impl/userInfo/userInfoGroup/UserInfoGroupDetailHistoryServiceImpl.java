package com.xiliulou.electricity.service.impl.userInfo.userInfoGroup;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailHistoryBO;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.mapper.userInfo.userInfoGroup.UserInfoGroupDetailHistoryMapper;
import com.xiliulou.electricity.query.UserInfoGroupDetailHistoryQuery;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
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
            
            bo.setOldGroupNames(getGroupNames(item.getOldGroupIds()));
            bo.setNewGroupNames(getGroupNames(item.getNewGroupIds()));
            bo.setOperatorName(Optional.ofNullable(userService.queryByUidFromCache(item.getOperator())).map(User::getName).orElse(""));
            bo.setOperatorTime(item.getCreateTime());
            
            return bo;
        }).collect(Collectors.toList());
    }
    
    private String getGroupNames(String groupIds) {
        if (StringUtils.isBlank(groupIds)) {
            return "";
        }
        
        StringJoiner names = new StringJoiner(CommonConstant.STR_COMMA);
        
        String[] split = groupIds.split(CommonConstant.STR_COMMA);
        for (String id : split) {
            UserInfoGroup userInfoGroup = userInfoGroupService.queryByIdFromCache(Long.valueOf(id));
            if (userInfoGroup != null) {
                names.add(userInfoGroup.getName());
            }
        }
        
        if (names.length() == 0) {
            return "";
        }
        
        return names.toString();
    }
}
