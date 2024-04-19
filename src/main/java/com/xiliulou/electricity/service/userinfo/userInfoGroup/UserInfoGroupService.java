package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupIdAndNameBO;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupQuery;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupBatchImportRequest;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupSaveAndUpdateRequest;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 19:59:04
 */
public interface UserInfoGroupService {
    
    R save(UserInfoGroupSaveAndUpdateRequest request, Long operator);
    
    R update(UserInfoGroupSaveAndUpdateRequest request, Long operator, Franchisee franchisee);
    
    List<UserInfoGroupBO> listByPage(UserInfoGroupQuery query);
    
    Integer countTotal(UserInfoGroupQuery query);
    
    R batchImport(UserInfoGroupBatchImportRequest request, Long operator, Franchisee franchisee);
    
    UserInfoGroup queryById(Long id);
    
    UserInfoGroup queryByIdFromCache(Long id);
    
    List<UserInfoGroupBO> listByIds(List<Long> ids);
    
    R remove(Long id, Long operator, Franchisee franchisee);
    
    List<UserInfoGroupIdAndNameBO> listAllGroup(UserInfoGroupQuery query);
    
    Integer batchUpdateByIds(List<Long> groupIds, Long updateTime, Long operator, Integer delFlag);
}
