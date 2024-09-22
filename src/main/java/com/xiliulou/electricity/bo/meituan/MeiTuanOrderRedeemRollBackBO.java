package com.xiliulou.electricity.bo.meituan;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @description 美团订单兑换套餐，用于执行回滚
 * @date 2024/9/2 13:35:10
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MeiTuanOrderRedeemRollBackBO {
    
    /**
     * 押金订单ID
     */
    private Long deleteDepositOrderById;
    
    /**
     * 换电套餐订单ID
     */
    private Long deleteMemberCardOrderById;
    
    /**
     * 换电套餐订单
     */
    private ElectricityMemberCardOrder rollBackElectricityMemberCardOrder;
    
    /**
     * userInfo
     */
    private UserInfo rollBackUserInfo;
    
    /**
     * 用户电池型号
     */
    private List<UserBatteryType> deleteUserBatteryTypeList;
    
    private List<UserBatteryType> insertUserBatteryTypeList;
    
    /**
     * 用户押金绑定ID
     */
    private Long deleteUserBatteryDepositById;
    
    /**
     * 用户押金绑定
     */
    private UserBatteryDeposit rollBackUserBatteryDeposit;
    
    /**
     * 用户套餐绑定ID
     */
    private Long deleteUserBatteryMemberCardById;
    
    /**
     * 用户套餐绑定
     */
    private UserBatteryMemberCard rollBackUserBatteryMemberCard;
    
    /**
     * 用户滞纳金绑定ID
     */
    private Long deleteServiceFeeUserInfoById;
    
    /**
     * 用户滞纳金绑定
     */
    private ServiceFeeUserInfo rollBackServiceFeeUserInfo;
    
    /**
     * 用户押金记录ID
     */
    private Long deleteEleUserOperateRecordDepositById;
    
    /**
     * 用户套餐记录ID
     */
    private Long deleteEleUserOperateRecordMemberCardById;
    
    /**
     * 套餐包ID
     */
    private Long deleteUserBatteryMemberCardPackageId;
}
