/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/15
 */

package com.xiliulou.electricity.tx;

import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.service.EleRefundOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/15 11:12
 */
@Service
public class EleRefundOrderTxService {
    
    @Resource
    private EleRefundOrderService eleRefundOrderService;
    
    
    
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insert(EleRefundOrder eleRefundOrder) {
        eleRefundOrderService.insert(eleRefundOrder);
    }
}
