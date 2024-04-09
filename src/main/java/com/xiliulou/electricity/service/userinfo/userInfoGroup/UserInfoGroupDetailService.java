package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.query.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupDetailVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:31:22
 */
public interface UserInfoGroupDetailService {
    
    UserInfoGroupDetail queryByUid(String groupNo, Long uid, Integer tenantId);
    
    List<UserInfoGroupDetailVO> listByPage(UserInfoGroupDetailQuery query);
    
    Integer countTotal(UserInfoGroupDetailQuery query);
    
    Integer batchInsert(List<UserInfoGroupDetail> detailList);
}
