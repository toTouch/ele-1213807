package com.xiliulou.electricity.service.process;

import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @ClassName: ExchangeUnFinishedOrderAssertProcess
 * @description: 是否存在未完成的订单校验处理器
 * @author: renhang
 * @create: 2024-11-12 14:55
 */
@Service("exchangeUnFinishedOrderAssertProcess")
@Slf4j
public class ExchangeUnFinishedOrderAssertProcess extends AbstractExchangeCommonHandler implements ExchangeAssertProcess<ExchangeAssertProcessDTO> {


    @Resource
    private ElectricityCabinetOrderService electricityCabinetOrderService;

    @Override
    public void process(ProcessContext<ExchangeAssertProcessDTO> context) {
        UserInfo userInfo = context.getProcessModel().getUserInfo();

        RentBatteryOrder rentBatteryOrder = context.getProcessModel().getChainObject().getRentBatteryOrder();
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                breakChain(context, "100023", "存在未完成租电订单，不能自助开仓");
                return;
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                breakChain(context, "100023", "存在未完成租电订单，不能自助开仓");
                return;
            }
        }

        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            breakChain(context, "100022", "存在未完成换电订单，不能自助开仓");
            return;
        }
    }
}
