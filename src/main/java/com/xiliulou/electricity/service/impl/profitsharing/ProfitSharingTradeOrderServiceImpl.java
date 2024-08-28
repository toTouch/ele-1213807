package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingOrder;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingTradeOrder;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingTradeOrderMapper;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingTradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/8/23 17:04
 * @desc
 */
@Service
@Slf4j
public class ProfitSharingTradeOrderServiceImpl implements ProfitSharingTradeOrderService {
    
    @Resource
    private ProfitSharingTradeOrderMapper profitSharingTradeOrderMapper;
    
    @Override
    public int batchInsert(List<ProfitSharingTradeOrder> profitSharingTradeOrderList) {
        return profitSharingTradeOrderMapper.batchInsert(profitSharingTradeOrderList);
    }
    
    @Override
    @Slave
    public ProfitSharingTradeOrder queryByOrderNo(String orderNo) {
        return profitSharingTradeOrderMapper.selectByOrderNo(orderNo);
    }
    
    @Override
    public int updateById(ProfitSharingTradeOrder profitSharingUpdate) {
        return profitSharingTradeOrderMapper.updateById(profitSharingUpdate);
    }
    
    @Override
    @Slave
    public boolean existsNotRefundByThirdOrderNo(String thirdOrderNo, String orderNo) {
        Integer count  =  profitSharingTradeOrderMapper.existsNotRefundByThirdOrderNo(thirdOrderNo, orderNo);
        if (Objects.nonNull(count)) {
            return true;
        }
        
        return false;
    }
    
    @Slave
    @Override
    public List<ProfitSharingTradeOrder> queryListByThirdOrderNosAndChannelAndProcessState(Integer tenantId, Integer processState, String channel, List<String> thirdOrderNos) {
        return profitSharingTradeOrderMapper.selectListByThirdOrderNosAndChannelAndProcessState(tenantId, processState, channel, thirdOrderNos);
    }
}
