package com.xiliulou.electricity.service;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.BackSelfOpenCellDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.enums.LastOrderTypeEnum;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName: AbstractLessTimeExchangeCommon
 * @description:
 * @author: renhang
 * @create: 2024-11-06 14:32
 */
@Slf4j
public class AbstractLessTimeExchangeCommon {

    @Resource
    private ElectricityCabinetOrderService electricityCabinetOrderService;

    @Resource
    private RentBatteryOrderService rentBatteryOrderService;


    @Resource
    private ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;

    @Resource
    private EleHardwareHandlerManager eleHardwareHandlerManager;


    public String backSelfOpen(BackSelfOpenCellDTO dto) {
        ElectricityCabinet cabinet = dto.getCabinet();
        if (cabinet.getVersion().isBlank() || VersionUtil.compareVersion(cabinet.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis()).orderId(dto.getOrderId())
                    .tenantId(dto.getTenantId()).msg("后台自主开仓").seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ)
                    .type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE).result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();

            electricityCabinetOrderOperHistoryService.insert(history);
        }

        if (Objects.equals(dto.getLastOrderType(), LastOrderTypeEnum.LAST_RENT_ORDER.getCode())) {
            // 租电
            RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
            rentBatteryOrder.setId(dto.getId());
            rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
            rentBatteryOrder.setRemark(dto.getMsg());
            rentBatteryOrderService.update(rentBatteryOrder);
        } else {
            // 换电
            ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
            electricityCabinetOrderUpdate.setId(dto.getId());
            electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrderUpdate.setRemark(dto.getMsg());
            electricityCabinetOrderService.update(electricityCabinetOrderUpdate);
        }

        //发送自助开仓命令
        HashMap<String, Object> dataMap = new HashMap<>(5);
        dataMap.put("orderId", dto.getOrderId());
        dataMap.put("cellNo", dto.getCell());
        dataMap.put("batteryName", dto.getUserBindingBatterySn());
        dataMap.put("userSelfOpenCell", false);

        String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + dto.getOrderId();
        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(cabinet.getProductKey()).deviceName(cabinet.getDeviceName())
                .command(ElectricityIotConstant.SELF_OPEN_CELL).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, cabinet);
        return sessionId;
    }


}
