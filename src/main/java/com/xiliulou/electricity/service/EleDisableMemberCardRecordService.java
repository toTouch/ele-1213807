package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;

import java.util.List;

public interface EleDisableMemberCardRecordService {
    
    
    int save(EleDisableMemberCardRecord eleDisableMemberCardRecord);
    
    R list(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);
    
    EleDisableMemberCardRecord queryCreateTimeMaxEleDisableMemberCardRecord(Long uid, Integer tenantId);
    
    R reviewDisableMemberCard(String disableMemberCardNo, String errMsg, Integer status);
    
    R queryCount(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);
    
    int updateBYId(EleDisableMemberCardRecord eleDisableMemberCardRecord);
    
    EleDisableMemberCardRecord queryByDisableMemberCardNo(String disableMemberCardNo, Integer tenantId);
    
    EleDisableMemberCardRecord selectByDisableMemberCardNo(String disableMemberCardNo);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    R listSuperAdminPage(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery);
    
    R<Object> handleDisableMemberCard(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard, EleDisableMemberCardRecord eleDisableMemberCardRecord, Franchisee franchisee,
            BatteryMemberCard batteryMemberCard, Boolean sendOperateRecordOrNot);
}
