/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/23
 */

package com.xiliulou.electricity.tx.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingConfigMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingReceiverConfigMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 15:20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProfitSharingConfigTxService {
    
    
    @Resource
    private ProfitSharingConfigMapper profitSharingConfigMapper;
    
    @Resource
    private ProfitSharingReceiverConfigMapper profitSharingReceiverConfigMapper;
    
    public void remove(Integer tenantId, Long profitSharingConfigId, List<Long> receiverConfigIds) {
        long time = System.currentTimeMillis();
        profitSharingConfigMapper.removeById(tenantId, profitSharingConfigId,time);
        if (CollectionUtils.isNotEmpty(receiverConfigIds)) {
            profitSharingReceiverConfigMapper.removeByIds(tenantId, receiverConfigIds,time);
        }
    }
    
}
