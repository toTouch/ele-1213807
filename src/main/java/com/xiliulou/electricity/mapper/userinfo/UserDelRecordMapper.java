package com.xiliulou.electricity.mapper.userinfo;

import com.xiliulou.electricity.entity.UserDelRecord;
import com.xiliulou.electricity.queryModel.ClearUserDelMarkQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2025/1/10 15:51:36
 */
public interface UserDelRecordMapper {
    
    Boolean existsByDelPhone(@Param("phone") String phone, @Param("tenantId") Integer tenantId);
    
    Boolean existsByDelIdNumber(@Param("idNumber") String idNumber, @Param("tenantId") Integer tenantId);
    
    Boolean existsByDelPhoneAndDelIdNumber(@Param("phone") String phone, @Param("idNumber") String idNumber, @Param("tenantId") Integer tenantId);
    
    UserDelRecord selectByUidAndStatus(@Param("uid") Long uid, @Param("statusList") List<Integer> statusList);
    
    Integer insert(UserDelRecord userDelRecord);
    
    Integer updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") Long updateTime);
    
    Integer deleteById(Long id);
    
    List<UserDelRecord> selectListByUidListAndStatus(@Param("uidList") List<Long> uidList, @Param("statusList") List<Integer> statusList);

    Integer clearUserDelMark(ClearUserDelMarkQueryModel phoneQueryModel);

    Integer update(UserDelRecord userDelRecord);

    UserDelRecord selectDelUidByDelIdNumber(@Param("idNumber") String idNumber, @Param("tenantId") Integer tenantId);
}
