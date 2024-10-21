package com.xiliulou.electricity.mapper.userinfo.userInfoGroup;

import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户详情
 * @date 2024/4/9 15:05:32
 */
public interface UserInfoGroupDetailMapper {
    
    List<UserInfoGroupDetailBO> selectPage(UserInfoGroupDetailQuery query);
    
    Integer countTotal(UserInfoGroupDetailQuery query);
    
    List<UserInfoGroupNamesBO> selectListGroupByUid(UserInfoGroupDetailQuery query);
    
    List<UserInfoGroupNamesBO> selectListGroupByUidList(@Param("uidList") List<Long> uidList);
    
    Integer batchInsert(@Param("detailList") List<UserInfoGroupDetail> detailList);
    
    @Deprecated
    Integer countGroupByUid(Long uid);
    
    Integer countGroupByUidAndFranchisee(@Param("uid") Long uid, @Param("franchiseeId") Long franchiseeId);
    
    Integer deleteByUid(@Param("uid") Long uid, @Param("groupNoList") List<String> groupNoList);
    
    Integer deleteByGroupNo(@Param("groupNo") String groupNo, @Param("tenantId") Integer tenantId);
    
    Integer deleteForUpdate(@Param("uid") Long uid, @Param("tenantId") Long tenantId, @Param("franchiseeId") Long franchiseeId);
    
    List<Long> selectListFranchiseeForUpdate(@Param("uid") Long uid);
}
