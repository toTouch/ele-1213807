package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityBattery;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-01-03-9:50
 */
@Data
public class UserBatteryDetail {
    /**
     * 是否缴纳电池押金 0：是，1：否
     */
    private Integer  isBatteryDeposit;
    /**
     * 是否购买租电池套餐 0：是，1：否
     */
    private Integer  isBatteryMemberCard;
    /**
     * 租电池套餐是否过期 0：是，1：否
     */
    private Integer  isBatteryMemberCardExpire;
    /**
     * 租电池套餐是否暂停 0：是，1：否
     */
    private Integer  isBatteryMemberCardDisable;
    /**
     * 是否绑定的有电池 0：是，1：否
     */
    private Integer isBindBattery;
    /**
     * 电池详情
     */
    private ElectricityBattery batteryInfo;
    /**
     * 是否产生电池服务费 0：是，1：否
     */
    private Integer  isBatteryServiceFee;
    /**
     * 电池服务费
     */
//    private EleBatteryServiceFeeVO  batteryServiceFee;
    private BigDecimal batteryServiceFee;

    /**
     * 套餐过期时间
     */
    private Long memberCardExpireTime;

    /**
     * 车辆套餐滞纳金是否存在
     * <pre>
     *     0-是
     *     1-否
     * </pre>
     * @see com.xiliulou.electricity.enums.YesNoEnum
     */
    private Integer carRentalPackageSlippage;

}
