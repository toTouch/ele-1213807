package com.xiliulou.electricity.mapper.userinfo.emergencyContact;

import com.xiliulou.electricity.entity.userinfo.EmergencyContact;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/11/11 10:58:52
 */
public interface EmergencyContactMapper {
    
    List<EmergencyContact> selectListByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);
    
    Integer batchInsert(@Param("list") List<EmergencyContact> list);
    
    Integer updateById(EmergencyContact emergencyContact);
    
    Integer deleteByUid(@Param("uid") Long uid, @Param("tenantId") Integer tenantId);
}
