package com.xiliulou.electricity.query.installment;

import lombok.Data;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/5 12:23
 */
@Data
public class InstallmentDeductNotifyQuery {
    
    /**
     * 资金处理订单号
     */
    private String payNo;
    
    /**
     * 扣款交易单号
     */
    private String tradeNo;
    
    /**
     * 交易时间，格式：20220615121314
     */
    private String tradeTime;
    
    /**
     * 扣款金额，单位分
     */
    private Integer payAmount;
}
