package com.xiliulou.electricity.service.process;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.LessTimeExchangeService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.exchange.AbstractOrderHandler;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: SelfOpenCellAssertProcess
 * @description: 自主开仓条件校验
 * @author: renhang
 * @create: 2024-11-12 14:55
 */
@Service("selfOpenCellAssertProcess")
@Slf4j
public class SelfOpenCellAssertProcess extends AbstractExchangeCommonHandler implements ExchangeAssertProcess<ExchangeAssertProcessDTO> {

    @Resource
    private RedisService redisService;


    @Resource
    LessTimeExchangeService lessTimeExchangeService;

    @Resource
    private RentBatteryOrderService rentBatteryOrderService;

    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;

    @Override
    public void process(ProcessContext<ExchangeAssertProcessDTO> context) {
        ExchangeAssertProcessDTO dto = context.getProcessModel();

        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(dto.getEid(), String.valueOf(dto.getCellNo()));
        if (Objects.isNull(electricityCabinetBox)) {
            log.warn("selfOpenCellAssertProcess  WARN! not find cellNO! eid is {}, cellNo is {} ", dto.getEid(), dto.getCellNo());
            breakChain(context, "ELECTRICITY.0006", "未找到此仓门");
            return;
        }

        RentBatteryOrder lastRentBatteryOrder = rentBatteryOrderService.queryByOrderId(dto.getOrderId());
        if (Objects.isNull(lastRentBatteryOrder)) {
            log.warn("selfOpenCellAssertProcess WARN! not found order,orderId={} ", dto.getOrderId());
            breakChain(context, "ELECTRICITY.0015", "未找到订单");
            return;
        }

        // 自主开仓时间校验
        String orderExceptionStartTime = redisService.get(context.getProcessModel().getSelfOpenCellKey() + dto.getOrderId());
        log.info("selfOpenCellAssertProcess Info! orderExceptionStartTime is {}", orderExceptionStartTime);

        if (StrUtil.isEmpty(orderExceptionStartTime)) {
            breakChain(context, "100667", "自主开仓超时");
            return;
        }
        if (Double.valueOf(System.currentTimeMillis() - Long.valueOf(orderExceptionStartTime)) / 1000 / 60 > 5) {
            log.warn("selfOpenCellAssertProcess WARN! self open cell timeout,orderId={}", dto.getOrderId());
            breakChain(context, "100667", "自主开仓超时");
            return;
        }

        // 自主开仓条件校验
        if (!lessTimeExchangeService.isSatisfySelfOpenConditionService(dto.getOrderId(), dto.getEid(), lastRentBatteryOrder.getCreateTime(), dto.getCellNo())) {
            log.warn("selfOpenCellAssertProcess WARN! not found order,orderId={} ", dto.getOrderId());
            breakChain(context, "100667", "用户自主开仓，系统识别归还仓门内电池为新订单，无法执行自助开仓操作");
            return;
        }

        context.getProcessModel().getChainObject().setRentBatteryOrder(lastRentBatteryOrder);
    }
}
