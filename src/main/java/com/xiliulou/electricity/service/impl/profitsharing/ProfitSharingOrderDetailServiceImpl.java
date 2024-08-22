package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderDetailMapper;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 分账订单明细表(TRofitSharingOrderDetail)表服务实现类
 *
 * @author makejava
 * @since 2024-08-22 17:00:36
 */
@Service
public class ProfitSharingOrderDetailServiceImpl implements ProfitSharingOrderDetailService {
    @Resource
    private ProfitSharingOrderDetailMapper profitSharingOrderDetailMapper;

    
}
