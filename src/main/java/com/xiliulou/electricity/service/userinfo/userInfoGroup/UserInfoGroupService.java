package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.userInfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.query.UserInfoGroupQuery;
import com.xiliulou.electricity.request.user.UserInfoGroupBatchImportRequest;
import com.xiliulou.electricity.request.user.UserInfoGroupSaveRequest;
import com.xiliulou.electricity.vo.userinfo.UserInfoGroupVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 19:59:04
 */
public interface UserInfoGroupService {
    
    R save(UserInfoGroupSaveRequest userInfoGroupSaveRequest, Long uid);
    
    List<UserInfoGroupVO> listByPage(UserInfoGroupQuery query);
    
    Integer countTotal(UserInfoGroupQuery query);
    
    R batchImport(UserInfoGroupBatchImportRequest request, Long uid);
    
    UserInfoGroup queryById(Long id);
    
    UserInfoGroup queryByIdFromCache(Long id);
    
    List<UserInfoGroupVO> listGroupByUid(Long uid, Integer tenantId);
    
    List<UserInfoGroupVO> listByIds(List<Long> ids);
}
