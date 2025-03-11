package com.xiliulou.electricity.mapper.userinfo.userInfoGroup;

import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailHistoryQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情修改记录
 * @date 2024/4/15 09:22:56
 */
public interface UserInfoGroupDetailHistoryMapper {
    
    Integer batchInsert(@Param("detailList") List<UserInfoGroupDetailHistory> detailHistoryList);
    
    List<UserInfoGroupDetailHistory> selectListByPage(UserInfoGroupDetailHistoryQuery query);
    
    Integer countTotal(UserInfoGroupDetailHistoryQuery query);
    
    Integer insertOne(UserInfoGroupDetailHistory detail);
    
    List<UserInfoGroupDetailHistory> selectListFranchiseeLatestHistory(@Param("uid")Long uid, @Param("tenantId")Integer tenantId);
}
