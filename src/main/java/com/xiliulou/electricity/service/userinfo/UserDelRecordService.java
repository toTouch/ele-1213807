package com.xiliulou.electricity.service.userinfo;

import com.xiliulou.electricity.dto.UserDelStatusDTO;
import com.xiliulou.electricity.entity.UserDelRecord;
import com.xiliulou.electricity.entity.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * @author HeYafeng
 * @date 2025/1/10 15:45:45
 */
public interface UserDelRecordService {
    
    /**
     * 根据手机号查询用户是否被删除过
     */
    Boolean existsByDelPhone(String phone, Integer tenantId);
    
    /**
     * 根据身份证号查询用户是否被删除过
     */
    Boolean existsByDelIdNumber(String idNumber, Integer tenantId);
    
    /**
     * 根据身手机号和份证号查询用户是否被删除过
     */
    Boolean existsByDelPhoneAndDelIdNumber(String phone, String idNumber, Integer tenantId);
    
    UserDelRecord getRemarkPhoneAndIdNumber(UserInfo userInfo, Integer tenantId);
    
    UserDelRecord queryByUidAndStatus(Long uid, List<Integer> statusList);
    
    Integer insert(Long uid, String delPhone, String delIdNumber, Integer status, Integer tenantId, Long franchiseeId, Integer delayDay,  Long userLastPayTime);
    
    void asyncRecoverUserInfoGroup(Long uid);
    
    Integer updateStatusById(Long id, Integer status, Long updateTime);
    
    void asyncRecoverCommonUser(Long uid, Integer type);
    
    Map<Long, UserDelStatusDTO> listUserStatus(List<Long> uidList, List<Integer> status);
    
    Integer getUserStatus(Long uid, Map<Long, UserDelStatusDTO> userStatusMap);

    UserDelRecord queryDelUidByDelIdNumber(String idNumber, Integer tenantId);
}
