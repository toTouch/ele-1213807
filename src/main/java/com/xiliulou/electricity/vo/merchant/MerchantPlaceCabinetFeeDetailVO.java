package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/25 22:09
 * @desc 商户小程序 场地费详情
 */
@Data
public class MerchantPlaceCabinetFeeDetailVO {
    /**
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 本月场地费
     */
    private BigDecimal currentMonthFee;
    
    /**
     * 累计场地费
     */
    private BigDecimal monthFeeSum;
    
    private Long time;
    
}
