package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 22:36
 * @desc 商户提现流程VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantWithdrawProcessVO {
    /**
     * 商户提现额度
     */
    private BigDecimal withdrawAmountLimit  ;
    
    /**
     * 类型：0-旧，1-新
     */
    private Integer type;
}
