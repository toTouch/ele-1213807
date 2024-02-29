package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/25 16:00
 * @desc 商户场地费本月详情vo
 */
@Data
public class MerchantPlaceFeeCurMonthVO {
    /**
     * 本月场地费
     */
    private BigDecimal currentMonthFee;
    /**
     * 上月场地费
     */
    private BigDecimal lastMonthFee;
    /**
     * 累计场地费
     */
    private BigDecimal monthFee;
    
    /**
     * 设备数量
     */
    private Integer cabinetCount;
}
