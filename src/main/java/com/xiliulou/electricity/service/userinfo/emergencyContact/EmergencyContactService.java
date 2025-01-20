package com.xiliulou.electricity.service.userinfo.emergencyContact;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.userinfo.EmergencyContact;
import com.xiliulou.electricity.request.userinfo.emergencyContact.EmergencyContactRequest;
import com.xiliulou.electricity.utils.ValidList;
import com.xiliulou.electricity.vo.userinfo.emergencyContact.EmergencyContactVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/11/11 10:52:46
 */
public interface EmergencyContactService {
    
    List<EmergencyContact> listByUidFromCache(Long uid);
    
    List<EmergencyContact> listByUid(Long uid);
    
    List<EmergencyContactVO> listVOByUid(Long uid);
    
    Triple<Boolean, String, Object> checkEmergencyContact(List<EmergencyContactRequest> emergencyContactList, UserInfo mainUserInfo);
    
    Integer batchSave(List<EmergencyContactRequest> emergencyContactList);
    
    R insertOrUpdate(ValidList<EmergencyContactRequest> emergencyContactList);
}
