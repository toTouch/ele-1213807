package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailHistoryBO;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailHistoryQuery;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情修改记录
 * @date 2024/4/15 09:21:02
 */
public interface UserInfoGroupDetailHistoryService {
    
    Integer batchInsert(List<UserInfoGroupDetailHistory> detailHistoryList);
    
    List<UserInfoGroupDetailHistoryBO> listByPage(UserInfoGroupDetailHistoryQuery query);
    
    Integer countTotal(UserInfoGroupDetailHistoryQuery query);
    
    Integer insertOne(UserInfoGroupDetailHistory detail);
    
    List<UserInfoGroupDetailHistory> listFranchiseeLatestHistory(Long uid, Integer tenantId);
}
