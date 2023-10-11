package com.xiliulou.electricity.vo.rental;

import com.xiliulou.electricity.enums.RenalPackageConfineEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退租提示数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class RefundRentOrderHintVo implements Serializable {
    
    /**
     * 租金(支付价格)
     */
    private BigDecimal rentPayment;
    
    /**
     * 预估可退金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 租期余量
     */
    private Long tenancyResidue;
    
    /**
     * 租期余量单位
     * <pre>
     *     1-天
     *     0-分钟
     * </pre>
     *
     * @see RentalUnitEnum
     */
    private Integer tenancyResidueUnit;
    
    /**
     * 套餐限制
     * <pre>
     *     0-不限制
     *     1-次数
     * </pre>
     *
     * @see RenalPackageConfineEnum
     */
    private Integer confine;
    
    /**
     * 限制余量
     */
    private Long confineResidue;
    
}
