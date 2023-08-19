package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-08-18-20:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserServiceFeeDetail {

    /**
     * 滞纳金金额
     */
    private BigDecimal batteryServiceFee;
    /**
     * 套餐名称
     */
    private String batteryMembercardName;
    /**
     * 服务费来源 0--月卡过期 1--暂停套餐
     */
    private Integer source;
    /**
     * 电池服务费产生时间
     */
    private Long batteryServiceFeeGenerateTime;
}
