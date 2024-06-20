package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailPageBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoBindGroupRequest;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupDetailUpdateRequest;

import java.util.List;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/9 14:31:22
 */
public interface UserInfoGroupDetailService {
    
    UserInfoGroupDetail queryByUid(String groupNo, Long uid, Integer tenantId);
    
    List<UserInfoGroupDetailPageBO> listByPage(UserInfoGroupDetailQuery query);
    
    Integer countTotal(UserInfoGroupDetailQuery query);
    
    Integer batchInsert(List<UserInfoGroupDetail> detailList);
    
    Integer countUserByGroupId(Long id);
    
    Integer countGroupByUid(Long uid);
    
    List<UserInfoGroupNamesBO> listGroupByUid(UserInfoGroupDetailQuery query);
    
    List<UserInfoGroupNamesBO> listGroupByUidList(List<Long> uidList);
    
    R update(UserInfoGroupDetailUpdateRequest request, Long operator);
    
    R bindGroup(UserInfoBindGroupRequest request, Long operator);
    
    Integer deleteByUid(Long uid, List<String> groupNoList);
    
    void handleAfterRefundDeposit(Long uid);
    
    Integer deleteByGroupNo(String groupNo, Integer tenantId);
}
