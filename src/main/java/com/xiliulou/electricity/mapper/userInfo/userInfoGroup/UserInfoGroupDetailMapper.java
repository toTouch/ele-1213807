package com.xiliulou.electricity.mapper.userInfo.userInfoGroup;

import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.query.UserInfoGroupDetailQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户详情
 * @date 2024/4/9 15:05:32
 */
public interface UserInfoGroupDetailMapper {
    
    UserInfoGroupDetail selectByUid(@Param("groupNo") String groupNo, @Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    List<UserInfoGroupDetailBO> selectPage(UserInfoGroupDetailQuery query);
    
    Integer countTotal(UserInfoGroupDetailQuery query);
    
    List<UserInfoGroupNamesBO> selectListGroupByUid(UserInfoGroupDetailQuery query);
    
    List<UserInfoGroupNamesBO> selectListGroupByUidList(@Param("uidList") List<Long> uidList);
    
    Integer batchInsert(@Param("detailList") List<UserInfoGroupDetail> detailList);
    
    Integer countUserByGroupId(Long id);
    
    Integer countGroupByUid(Long uid);
    
    Integer deleteByUid(@Param("uid") Long uid, @Param("groupNoList") List<String> groupNoList);
    
}
