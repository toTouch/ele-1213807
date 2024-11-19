/**
 *  Create date: 2024/8/15
 */

package com.xiliulou.electricity.tx;

import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.mapper.BatteryMembercardRefundOrderMapper;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/15 11:17
 */
@Service
public class BatteryMembercardRefundOrderTxService {
    
    @Resource
    private BatteryMembercardRefundOrderMapper batteryMembercardRefundOrderMapper;
    
    @Resource
    private ElectricityMemberCardOrderMapper electricityMemberCardOrderMapper;
    
    
    @Transactional(rollbackFor = Exception.class)
    public void refund(ElectricityMemberCardOrder cardOrder, BatteryMembercardRefundOrder refundOrder) {
        batteryMembercardRefundOrderMapper.update(refundOrder);
        electricityMemberCardOrderMapper.updateById(cardOrder);
    }
    
}
