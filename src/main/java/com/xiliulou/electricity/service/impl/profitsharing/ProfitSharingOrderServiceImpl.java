package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderDetailMapper;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingOrderMapper;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * 分账订单表(profitSharingOrder)表服务实现类
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:57
 */
@Service
public class ProfitSharingOrderServiceImpl implements ProfitSharingOrderService {
    @Resource
    private ProfitSharingOrderMapper profitSharingOrderMapper;
    
    @Resource
    private ProfitSharingOrderDetailMapper profitSharingOrderDetailMapper;
    
    
}
