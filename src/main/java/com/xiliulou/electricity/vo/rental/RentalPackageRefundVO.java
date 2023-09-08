package com.xiliulou.electricity.vo.rental;

import com.xiliulou.electricity.enums.RenalPackageConfineEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RentalPackageRefundVO implements Serializable {

    private String orderNo;

    /**
     * 套餐剩余次数
     */
    private Long residueCount;

    /**
     * 套餐剩余时间
     */
    private Long residueTime;

    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;

    /**
     * 套餐限制
     * <pre>
     *     0-不限制
     *     1-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    private Integer confine;

    /**
     * 租期单位
     * <pre>
     *     1-天
     *     0-分钟
     * </pre>
     * @see RentalUnitEnum
     */
    private Integer tenancyUnit;


}
