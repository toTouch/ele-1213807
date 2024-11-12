package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDisableMemberCardRecord;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;

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
    
    /**
     * 冻结申请免审核与审核通过通用处理
     *
     * @param userInfo                   用户
     * @param userBatteryMemberCard      用户套餐关联数据
     * @param eleDisableMemberCardRecord 套餐冻结记录
     * @param franchisee                 加盟商
     * @param batteryMemberCard          换电套餐
     * @param sendOperateRecordOrNot     是否需要保存操作记录
     * @return 处理结果
     */
    R<Object> handleDisableMemberCard(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard, EleDisableMemberCardRecord eleDisableMemberCardRecord, Franchisee franchisee,
            BatteryMemberCard batteryMemberCard, Boolean sendOperateRecordOrNot);
    
    /**
     * 统计一个用户本月内申请冻结并通过的次数
     *
     * @param uid 用户uid
     * @return 次数
     */
    Integer countDisabledRecordThisMonth(Long uid);
}
