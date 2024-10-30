package com.xiliulou.electricity.service.userinfo.userInfoGroup;

import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroup;
import com.xiliulou.electricity.entity.userinfo.userInfoGroup.UserInfoGroupDetail;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/8 14:39
 */
public interface UserInfoGroupBizService {
    
    /**
     * 删除用户与用户分组的关联数据
     *
     * @param uid         用户uid
     * @param groupNoList 用户分组id
     * @return 删除结果
     */
    Integer deleteGroupDetailByUid(Long uid, List<String> groupNoList);
    
    Integer deleteGroupDetailByGroupNo(String groupNo, Integer tenantId);
    
    List<UserInfoGroupNamesBO> listGroupByUidList(List<Long> uidList);
    
    Integer batchInsertGroupDetail(List<UserInfoGroupDetail> detailList);
    
    List<UserInfoGroupBO> listUserInfoGroupByIds(List<Long> ids);
    
    UserInfoGroup queryUserInfoGroupByIdFromCache(Long id);
    
}
