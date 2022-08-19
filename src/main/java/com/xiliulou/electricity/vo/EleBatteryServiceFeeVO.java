package com.xiliulou.electricity.vo;


import com.xiliulou.electricity.query.ModelBatteryDeposit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 电池服务费视图对象
 *
 * @author makejava
 * @since 2022-04-22 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleBatteryServiceFeeVO {

    /**
     * 电池服务费
     */
    private BigDecimal batteryServiceFee;

    /**
     * 用户所产生的电池服务费
     */
    private BigDecimal userBatteryServiceFee;

    /**
     * 分型号电池服务费
     */
    private List<ModelBatteryDeposit> modelBatteryServiceFeeList;

    private Integer modelType;

    private Integer model;

    private Integer memberCardStatus;

}
