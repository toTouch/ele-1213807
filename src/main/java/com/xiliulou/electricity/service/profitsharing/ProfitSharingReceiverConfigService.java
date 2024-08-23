/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/23
 */

package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverOptConfigRequest;

import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 14:00
 */
public interface ProfitSharingReceiverConfigService {
    
    
    /**
     * 租户id+分账主表配置id
     *
     * @param tenantId
     * @param profitSharingConfigId
     * @author caobotao.cbt
     * @date 2024/8/23 14:40
     */
    List<ProfitSharingReceiverConfig> queryListByProfitSharingConfigId(Integer tenantId, Long profitSharingConfigId);
    
    /**
     * insert
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/23 16:28
     */
    void insert(ProfitSharingReceiverOptConfigRequest request);
}
