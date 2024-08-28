/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/26
 */

package com.xiliulou.electricity.query.profitsharing;

import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeMixedOrderStateEnum;
import lombok.Data;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/26 16:53
 */
@Data
public class ProfitSharingTradeMixedOrderQueryModel {
    
    /**
     * 处理状态
     *
     * @see ProfitSharingTradeMixedOrderStateEnum
     */
    private Integer state;
    
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 开始id
     */
    private Long startId;
    
    /**
     * 渠道 WECHAT-微信
     */
    private String channel;
    
    /**
     * 数量
     */
    private Integer size;
}
