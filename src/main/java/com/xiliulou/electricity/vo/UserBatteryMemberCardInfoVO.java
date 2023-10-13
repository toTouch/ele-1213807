package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-29-14:42
 */
@Data
public class UserBatteryMemberCardInfoVO {

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
    private  InsuranceUserInfoVo insuranceUserInfoVo;
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
    
    /**
     * 套餐业务类型：0，换电套餐；1，车电一体套餐, 2. 企业渠道换电套餐
     * @see BatteryMemberCardBusinessTypeEnum
     */
    private Integer businessType;

    private Long franchiseeId;

    private Integer modelType;

    private Long storeId;

    /**
     * 冻结申请后，若被拒绝，显示的拒绝原因
     */
    private String rejectReason;

    public static final Integer NO = 0;
    public static final Integer YES = 1;

}
