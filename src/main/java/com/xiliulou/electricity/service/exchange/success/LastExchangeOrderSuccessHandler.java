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
 * @Description: LastExchangeOrderSuccessHandler
 * @Author: renhang
 * @Date: 2025/01/07
 */

@Service("lastExchangeOrderSuccessHandler")
@Slf4j
public class LastExchangeOrderSuccessHandler extends AbstractSuccessOrderHandler {

    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;


    @Override
    public Pair<Boolean, ExchangeUserSelectVO> lastExchangeHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, RentBatteryOrder rentBatteryOrder, UserInfo userInfo) {
        if (Objects.isNull(lastOrder)) {
            log.error("OrderV3 Error! LastExchangeOrderSuccessHandler.order is exchangeOrder, but exchangeOrder is null, uid is {}", userInfo.getUid());
            return Pair.of(false, null);
        }

        ExchangeUserSelectVO vo = new ExchangeUserSelectVO();
        vo.setIsEnterMoreExchange(LessScanConstant.ENTER_MORE_EXCHANGE).setLastExchangeIsSuccess(LessScanConstant.LAST_EXCHANGE_SUCCESS).setCabinetName(cabinet.getName())
                .setLastOrderType(LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode());

        // 上次换电成功，用户绑定电池不可能为空
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.error("OrderV3 Error! LastExchangeOrderSuccessHandler userBindBattery is null, lastOrderId is {}", lastOrder.getOrderId());
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            return Pair.of(true, vo);
        }

        if (!isSatisfySelfOpenCondition(lastOrder.getOrderId(), lastOrder.getElectricityCabinetId(), lastOrder.getUpdateTime(),
                lastOrder.getNewCellNo())) {
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderV3 WARN! LastExchangeOrderSuccessHandler ElectricityCabinetOrder is not SatisfySelfOpen, orderId is{}", lastOrder.getOrderId());
            return Pair.of(true, vo);
        }

        vo.setOrderId(lastOrder.getOrderId())
                .setCell(lastOrder.getNewCellNo())
                .setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);

        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), cabinet.getId());
        log.info("OrderV3 INFO! LastExchangeOrderSuccessHandler ExchangeOrder cabinetBox is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                JsonUtil.toJson(lastOrder));

        // 电池在仓，并且电池所在仓门=上个订单的新仓门
        if (Objects.nonNull(cabinetBox) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getNewCellNo())) {
            // 换电成功，后台为用户打开上次成功的新仓门
            BackSelfOpenCellDTO openCellDTO = BackSelfOpenCellDTO.builder().id(lastOrder.getId()).userBindingBatterySn(electricityBattery.getSn())
                    .cell(lastOrder.getNewCellNo()).orderId(lastOrder.getOrderId()).cabinet(cabinet).msg(ExchangeRemarkConstant.EXCHANGE_SUCCESS_SYSTEM_SELF_CELL)
                    .tenantId(lastOrder.getTenantId()).lastOrderType(LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode()).build();
            // 后台自主
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL).setSessionId(this.backSelfOpen(openCellDTO));
            return Pair.of(true, vo);
        }

        // 没有在仓
        vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
        return Pair.of(true, vo);
    }
}
