package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.query.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.user.UserInfoGroupDetailUpdateRequest;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupDetailPageVO;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupNamesVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:31:22
 */
public interface UserInfoGroupDetailService {
    
    UserInfoGroupDetail queryByUid(String groupNo, Long uid, Integer tenantId);
    
    List<UserInfoGroupDetailPageVO> listByPage(UserInfoGroupDetailQuery query);
    
    Integer countTotal(UserInfoGroupDetailQuery query);
    
    Integer batchInsert(List<UserInfoGroupDetail> detailList);
    
    Integer countUserByGroupId(Long id);
    
    List<UserInfoGroupNamesVO> listGroupByUid(UserInfoGroupDetailQuery query);
    
    R update(UserInfoGroupDetailUpdateRequest request);
}
