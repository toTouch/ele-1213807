package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailPageBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetailHistory;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoBindGroupRequest;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoBindGroupRequestV2;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupDetailUpdateRequest;
import com.xiliulou.security.bean.TokenUser;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:31:22
 */
public interface UserInfoGroupDetailService {
    
    List<UserInfoGroupDetailPageBO> listByPage(UserInfoGroupDetailQuery query);
    
    Integer countTotal(UserInfoGroupDetailQuery query);
    
    Integer batchInsert(List<UserInfoGroupDetail> detailList);
    
    List<UserInfoGroupNamesBO> listGroupByUid(UserInfoGroupDetailQuery query);
    
    List<UserInfoGroupNamesBO> listGroupByUidList(List<Long> uidList);
    
    @Deprecated
    R update(UserInfoGroupDetailUpdateRequest request, Long operator);
    
    R updateV2(UserInfoGroupDetailUpdateRequest request, TokenUser operator);
    
    R bindGroup(UserInfoBindGroupRequest request, Long operator);
    
    R<Object> bindGroupV2(UserInfoBindGroupRequest request, TokenUser operator);
    
    Integer deleteByUid(Long uid, List<String> groupNoList);
    
    /**
     * 查询用户全部自定义分组
     *
     * @param query 请求参数
     * @return 查询结果
     */
    R<Object> selectAll(UserInfoGroupDetailQuery query);
    
    Integer deleteForUpdate(Long uid, Long tenantId, Long franchiseeId);
    
    Integer deleteForUpdateUids(List<Long> uids, Long tenantId, Long franchiseeId, String groupNo);
    
    List<Long> listFranchiseeForUpdate(Long uid);
    
    R unbindUserGroupsInBatches(UserInfoBindGroupRequestV2 request, TokenUser user);
    
    List<UserInfoGroupNamesBO> listGroupByUserGroups(List<Long> uids, Long groupId, Long franchiseeId);
    
    
    Integer existsByUid(Long uid);
    
    UserInfoGroupDetailHistory assembleDetailHistory(Long uid, String oldGroupIds, String newGroupIds, Long operator, Long franchiseeId, Integer tenantId, Integer type);
}
