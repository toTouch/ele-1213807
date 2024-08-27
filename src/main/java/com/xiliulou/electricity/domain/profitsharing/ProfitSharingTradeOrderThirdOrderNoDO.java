/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.domain.profitsharing;

import lombok.Data;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/26 17:22
 */
@Data
public class ProfitSharingTradeOrderThirdOrderNoDO {
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 第三方订单号
     */
    private String thirdOrderNo;
    
    
}
