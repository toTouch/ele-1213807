package com.xiliulou.electricity.service.exchange.success;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ExchangeRemarkConstant;
import com.xiliulou.electricity.constant.LessScanConstant;
import com.xiliulou.electricity.dto.BackSelfOpenCellDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.LastOrderTypeEnum;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * @Description: LastRentOrderSuccessHandler
 * @Author: renhang
 * @Date: 2025/01/07
 */

@Service("lastRentOrderSuccessHandler")
@Slf4j
public class LastRentOrderSuccessHandler extends AbstractSuccessOrderHandler {

    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;


    @Override
    public Pair<Boolean, ExchangeUserSelectVO> lastExchangeHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, RentBatteryOrder rentBatteryOrder, UserInfo userInfo, ExchangeUserSelectVO vo) {
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("OrderV3 Error! lastExchangeSuccessHandler.order is rentOrder, but rentOrder is null, uid is {}", userInfo.getUid());
            return Pair.of(false, null);
        }

        vo.setOrderId(rentBatteryOrder.getOrderId()).setCell(rentBatteryOrder.getCellNo()).setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);

        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), cabinet.getId());
        log.info("OrderV3 INFO! LastRentSuccessHandler RentOrder cabinetBox is {}, rentBatteryOrder is {}",
                Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null", JsonUtil.toJson(rentBatteryOrder));

        // 上次租电成功，电池在仓，并且电池所在仓门=上个订单的新仓门
        if (Objects.nonNull(cabinetBox) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(Integer.valueOf(cabinetBox.getCellNo()),
                rentBatteryOrder.getCellNo())) {
            // 租电成功，后台为用户打开租电成功的仓门
            BackSelfOpenCellDTO openCellDTO = BackSelfOpenCellDTO.builder().id(rentBatteryOrder.getId()).userBindingBatterySn(electricityBattery.getSn())
                    .cell(rentBatteryOrder.getCellNo()).orderId(rentBatteryOrder.getOrderId()).cabinet(cabinet).msg(ExchangeRemarkConstant.RENT_SUCCESS_SYSTEM_SELF_CELL)
                    .tenantId(rentBatteryOrder.getTenantId()).lastOrderType(LastOrderTypeEnum.LAST_RENT_ORDER.getCode()).build();
            // 自主开仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL).setSessionId(this.backSelfOpen(openCellDTO));
            return Pair.of(true, vo);
        }

        // 没有在仓
        vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
        return Pair.of(true, vo);
    }
}
