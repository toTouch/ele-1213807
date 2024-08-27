/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.query.profitsharing;

import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeOderProcessStateEnum;
import lombok.Data;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/26 16:53
 */
@Data
public class ProfitSharingTradeOrderQueryModel {
    
    /**
     * 处理状态
     *
     * @see ProfitSharingTradeOderProcessStateEnum
     */
    private Integer processState;
    
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 开始id
     */
    private Long startId;
    
    /**
     * 数量
     */
    private Integer size;
}
