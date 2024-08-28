package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeMixedOrder;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingTradeMixedStateEnum;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingTradeMixedOrderMapper;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeMixedOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 分账交易混合订单(TProfitSharingTradeMixedOrder)表服务实现类
 *
 * @author makejava
 * @since 2024-08-27 19:19:18
 */
@Service
public class ProfitSharingTradeMixedOrderServiceImpl implements ProfitSharingTradeMixedOrderService {
    
    @Resource
    private ProfitSharingTradeMixedOrderMapper profitSharingTradeMixedOrderMapper;
    
    
    @Override
    public void insert(ProfitSharingTradeMixedOrder profitSharingTradeMixedOrder) {
        long time = System.currentTimeMillis();
        profitSharingTradeMixedOrder.setCreateTime(time);
        profitSharingTradeMixedOrder.setUpdateTime(time);
        profitSharingTradeMixedOrder.setState(ProfitSharingTradeMixedStateEnum.COMPLETE.getCode());
        profitSharingTradeMixedOrderMapper.insert(profitSharingTradeMixedOrder);
    }
    
    @Override
    @Slave
    public ProfitSharingTradeMixedOrder queryByThirdOrderNo(String thirdOrderNo) {
        return profitSharingTradeMixedOrderMapper.selectByThirdOrderNo(thirdOrderNo);
    }
}
