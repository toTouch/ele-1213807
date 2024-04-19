package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/25 22:09
 * @desc 商户小程序 场地费详情
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantPlaceCabinetFeeDetailVO {
    /**
     * 柜机Id
     */
    private Long cabinetId;
    
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
