package com.xiliulou.electricity.bo.profitsharing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/8/29 11:06
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfitSharingOrderTypeUnfreezeBO {
    /**
     * 微信支付订单号
     */
    private String thirdTradeOrderNo;
    
    /**
     * 分账单号
     */
    private String orderNo;
    
    /**
     * 分账明细表id
     */
    private Long detailId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 分账订单主表id
     */
    private Long id;
}
