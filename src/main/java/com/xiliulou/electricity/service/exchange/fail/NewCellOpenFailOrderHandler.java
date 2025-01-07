package com.xiliulou.electricity.service.exchange.fail;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.LessScanConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.CellTypeEnum;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Description: NewCellOpenFailOrderHandler
 * @Author: renhang
 * @Date: 2025/01/07
 */

@Slf4j
@Service
public class NewCellOpenFailOrderHandler extends AbstractFailOrderHandler {

    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;

    @Resource
    private ElectricityBatteryService electricityBatteryService;

    @Override
    Pair<Boolean, ExchangeUserSelectVO> lastExchangeFailHandler(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ElectricityCabinet cabinet, UserInfo userInfo, Integer code, Integer secondFlexibleRenewal, ExchangeUserSelectVO vo) {
        vo.setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);
        vo.setCell(lastOrder.getNewCellNo());
        vo.setOrderId(lastOrder.getOrderId());

        // 用户绑定电池为空，返回自主开仓
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderV3 WARN!newCellOpenFail.userBindingBatterySn  is null, uid is {}", lastOrder.getUid());
            // 没有在仓，需要返回前端仓门号
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }

        String userBindingBatterySn = electricityBattery.getSn();
        // 用户电池是否在仓
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(userBindingBatterySn, cabinet.getId());

        ElectricityBattery battery = electricityBatteryService.queryBySnFromDb(userBindingBatterySn);

        log.info("OrderV3 INFO! newCellOpenFail.cabinetBox is {}, battery is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                Objects.nonNull(battery) ? JsonUtil.toJson(battery) : "null", JsonUtil.toJson(lastOrder));

        // 用户绑定的电池状态是否为租借状态 && 用户绑定的电池在仓 & 电池所在的仓门=上个订单的旧仓门；开新仓门
        if (Objects.nonNull(cabinetBox) && Objects.nonNull(battery) && Objects.equals(battery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE) && StrUtil.isNotBlank(
                cabinetBox.getCellNo()) && Objects.equals(Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getOldCellNo())) {
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL);
            vo.setIsEnterTakeBattery(LessScanConstant.ENTER_TAKE_BATTERY);
            vo.setCellType(CellTypeEnum.NEW_CELL.getCode());
            // 新仓门取电
            vo.setSessionId(this.openFullBatteryCellHandler(lastOrder, cabinet, lastOrder.getNewCellNo(), userBindingBatterySn, cabinetBox.getCellNo()));
            return Pair.of(true, vo);
        } else {
            // 没有在仓，需要返回前端仓门号
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }
    }
}
