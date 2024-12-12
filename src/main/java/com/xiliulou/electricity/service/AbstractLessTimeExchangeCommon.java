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
    private ElectricityCabinetPhysicsOperRecordService electricityCabinetPhysicsOperRecordService;

    @Resource
    private ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;

    @Resource
    private EleHardwareHandlerManager eleHardwareHandlerManager;


    /**
     * 是否满足自主开仓
     *
     * @param orderId          orderId
     * @param eid              eid
     * @param startTime        订单的结束时间
     * @param cell             cell
     * @param newOrOldCellFlag newOrOldCellFlag
     * @return Boolean
     */
    public Boolean isSatisfySelfOpenCondition(String orderId, Integer eid, Long startTime, Integer cell, Integer newOrOldCellFlag) {
        if (Objects.isNull(cell)) {
            log.error("orderV3 Error! isSatisfySelfOpenCondition.params.cell is null");
            return false;
        }
        // 上个订单+5分钟是否存在换电、退电、操作记录
        Long endTime = startTime + 1000 * 60 * 5;
        List<ElectricityCabinetOrder> electricityCabinetOrders = electricityCabinetOrderService.existExchangeOrderInSameCabinetAndCell(startTime, endTime, eid);
        if (CollUtil.isNotEmpty(electricityCabinetOrders)) {
            // 新仓门
            if (Objects.equals(newOrOldCellFlag, ElectricityCabinetOrder.NEW_CELL)) {
                if (CollUtil.isNotEmpty(electricityCabinetOrders.stream().filter(e -> Objects.equals(e.getNewCellNo(), cell)).collect(Collectors.toList()))) {
                    log.warn("orderV3 warn! isSatisfySelfOpenCondition newCellNo existExchangeOrder, orderId:{}", orderId);
                    return false;
                }
            }
            if (Objects.equals(newOrOldCellFlag, ElectricityCabinetOrder.OLD_CELL)) {
                if (CollUtil.isNotEmpty(electricityCabinetOrders.stream().filter(e -> Objects.equals(e.getOldCellNo(), cell)).collect(Collectors.toList()))) {
                    log.warn("orderV3 warn! isSatisfySelfOpenCondition oldCellNo existExchangeOrder, orderId:{}", orderId);
                    return false;
                }
            }
            // 换电订单的新/旧仓门是否 存在上次退电的仓门
            if (Objects.equals(newOrOldCellFlag, ElectricityCabinetOrder.NEW_AND_OLD_CELL)) {
                if (CollUtil.isNotEmpty(electricityCabinetOrders.stream().filter(e -> Objects.equals(e.getOldCellNo(), cell) || Objects.equals(e.getNewCellNo(), cell))
                        .collect(Collectors.toList()))) {
                    log.warn("orderV3 warn! isSatisfySelfOpenCondition newAndOldCellNo existExchangeOrder, orderId:{}", orderId);
                    return false;
                }
            }
        }
        Integer existReturnOrder = rentBatteryOrderService.existReturnOrderInSameCabinetAndCell(startTime, endTime, eid, cell);
        if (Objects.nonNull(existReturnOrder)) {
            log.warn("orderV3 warn! isSatisfySelfOpenCondition.existReturnOrder, orderId:{}", orderId);
            return false;
        }
        Integer existOpenRecord = electricityCabinetPhysicsOperRecordService.existOpenRecordInSameCabinetAndCell(startTime, endTime, eid, cell);
        if (Objects.nonNull(existOpenRecord)) {
            log.warn("orderV3 warn! isSatisfySelfOpenCondition.existOpenRecord, orderId:{}", orderId);
            return false;
        }
        return true;
    }


    public String backSelfOpen(BackSelfOpenCellDTO dto) {
        ElectricityCabinet cabinet = dto.getCabinet();
        if (cabinet.getVersion().isBlank() || VersionUtil.compareVersion(cabinet.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis()).orderId(dto.getOrderId())
                    .tenantId(dto.getTenantId()).msg(dto.getMsg()).seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ)
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
