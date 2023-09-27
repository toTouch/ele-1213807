package com.xiliulou.electricity.vo.enterprise;

import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/25 20:04
 */

@Data
public class EnterpriseUserPackageDetailsVO {

    private Long uid;

    private String name;

    private String phone;

    private Integer renewalStatus;

    private Integer batteryRentStatus;

    private Integer batteryDepositStatus;

    private BigDecimal batteryDeposit;

    private BigDecimal batteryMembercardPayAmount;
    /**
     * 是否购买套餐
     */
    private Integer isExistMemberCard;
    /**
     * 租期
     */
    private Integer validDays;
    /**
     * 是否购买保险
     */
    private InsuranceUserInfoVo insuranceUserInfoVo;
    /**
     * 套餐id
     */
    private Long memberCardId;
    /**
     * 套餐名称
     */
    private String memberCardName;
    /**
     * 套餐订单
     */
    private String orderId;
    /**
     * 套餐到期时间
     */
    private Long memberCardExpireTime;

    /**
     * 套餐购买时间
     */
    private Long memberCardPayTime;
    /**
     * 套餐总剩余次数
     */
    private Long remainingNumber;
    /**
     * 套餐状态
     */
    private Integer memberCardStatus;
    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;

    /**
     * 租期单位 0：分钟，1：天
     */
    private Integer rentUnit;
    /**
     * 用户电池型号
     */
    private String userBatterySimpleType;

    private Long franchiseeId;

    private Integer modelType;

    private Long storeId;

    /**
     * 冻结申请后，若被拒绝，显示的拒绝原因
     */
    private String rejectReason;

}
