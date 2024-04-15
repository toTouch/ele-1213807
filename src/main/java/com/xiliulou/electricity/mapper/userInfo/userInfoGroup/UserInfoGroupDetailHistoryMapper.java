package com.xiliulou.electricity.mapper.userInfo.userInfoGroup;

import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.query.UserInfoGroupDetailHistoryQuery;
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
}
