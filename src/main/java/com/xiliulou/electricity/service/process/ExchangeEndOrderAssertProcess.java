package com.xiliulou.electricity.service.process;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: ExchangeEndOrderAssertProcess
 * @description: 结束异常订单校验处理器
 * @author: renhang
 * @create: 2024-11-12 14:55
 */
@Service("exchangeEndOrderAssertProcess")
@Slf4j
public class ExchangeEndOrderAssertProcess extends AbstractExchangeCommonHandler implements ExchangeAssertProcess<ExchangeAssertProcessDTO> {
    
    @Resource
    private RentBatteryOrderService rentBatteryOrderService;
    
    @Resource
    private ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Override
    public void process(ProcessContext<ExchangeAssertProcessDTO> context) {
        Long uid = context.getProcessModel().getUserInfo().getUid();
        //  是否存在未完成的订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(uid);
        if (Objects.nonNull(rentBatteryOrder)) {
            log.warn("ExchangeEndOrderAssertProcess Warn! Exist Uncompleted RentBatterOrder, RentOrderId is {}", rentBatteryOrder.getOrderId());
            // 结束异常租退订单
            R endRentOrder = rentBatteryOrderService.endOrder(rentBatteryOrder.getOrderId());
            if (!endRentOrder.isSuccess()) {
                log.warn("ExchangeEndOrderAssertProcess Warn! End Uncompleted RentBatterOrder is fail, reason is {}", endRentOrder.getErrMsg());
            }
        }
        
        //是否存在未完成的换电订单
        ElectricityCabinetOrder cabinetOrder = electricityCabinetOrderService.queryByUid(uid);
        if (Objects.nonNull(cabinetOrder)) {
            log.warn("ExchangeEndOrderAssertProcess Warn! Exist Uncompleted ExchangeOrder, ExchangeOrderId is {}", cabinetOrder.getOrderId());
            // 结束异常换电订单
            R endExchangeOrder = electricityCabinetOrderService.endOrder(cabinetOrder.getOrderId());
            if (!endExchangeOrder.isSuccess()) {
                log.warn("ExchangeEndOrderAssertProcess Warn! End Uncompleted ExchangeOrder is fail, Reason is {}", endExchangeOrder.getErrMsg());
            }
        }
    }
}
