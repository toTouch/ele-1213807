package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleUserEsignRecord;
import com.xiliulou.electricity.entity.EleUserIdentityAuthRecord;
import org.apache.ibatis.annotations.Param;

/**
 * @author: Kenneth
 * @Date: 2023/7/9 19:14
 * @Description:
 */
public interface EleUserIdentityAuthRecordMapper extends BaseMapper<EleUserIdentityAuthRecord> {

    int insertUserIdentityAuthRecord(EleUserIdentityAuthRecord eleUserIdentityAuthRecord);

    int updateUserIdentityAuthRecord(EleUserIdentityAuthRecord eleUserIdentityAuthRecord);

    EleUserIdentityAuthRecord selectLatestAuthRecordByUser(@Param("uid") Long uid, @Param("tenantId") Long tenantId);

    /*EleUserIdentityAuthRecord selectAuthRecordByUserInfo(Long uid, Long tenantId, String authFlowId);*/

}
