package com.xiliulou.electricity.mapper.userInfo.userInfoGroup;

import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.query.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupDetailVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupNamesVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户详情
 * @date 2024/4/9 15:05:32
 */
public interface UserInfoGroupDetailMapper {
    
    UserInfoGroupDetail selectByUid(@Param("groupNo") String groupNo, @Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    List<UserInfoGroupDetailVO> selectPage(UserInfoGroupDetailQuery query);
    
    Integer countTotal(UserInfoGroupDetailQuery query);
    
    List<UserInfoGroupNamesVO> selectListGroupByUid(UserInfoGroupDetailQuery query);
    
    Integer batchInsert(@Param("detailList") List<UserInfoGroupDetail> detailList);
    
    Integer countUserByGroupId(Long id);
    
    Integer countGroupByUid(Long uid);
    
    Integer deleteByUidAndGroupNoList(@Param("uid") Long uid, @Param("groupNoList") List<String> groupNoList);
    
    List<UserInfoGroupNamesVO> selectListGroupByUidList(@Param("uidList") List<Long> uidList);
}
