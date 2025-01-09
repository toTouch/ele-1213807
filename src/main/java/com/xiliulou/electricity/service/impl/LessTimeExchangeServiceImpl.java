package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.ExchangeConfig;
import com.xiliulou.electricity.constant.*;
import com.xiliulou.electricity.dto.BackSelfOpenCellDTO;
import com.xiliulou.electricity.dto.LessTimeExchangeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.query.OrderQueryV3;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.service.exchange.AbstractOrderHandler;
import com.xiliulou.electricity.service.exchange.fail.OrderStatusStrategy;
import com.xiliulou.electricity.service.exchange.success.OrderProcessingStrategy;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import com.xiliulou.electricity.vo.ReturnBatteryLessTimeScanVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


/**
 * @ClassName: LessTimeExchangeServiceImpl
 * @description: 短时间换电处理
 * @author: renhang
 * @create: 2024-11-05 17:14
 */
@Service
@Slf4j
//@SuppressWarnings("all")
public class LessTimeExchangeServiceImpl extends AbstractOrderHandler implements LessTimeExchangeService {

    @Resource
    private RentBatteryOrderService rentBatteryOrderService;

    @Resource
    private ElectricityCabinetOrderService electricityCabinetOrderService;

    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;

    @Resource
    private ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;

    @Resource
    private ElectricityBatteryService electricityBatteryService;

    @Resource
    private ExchangeConfig exchangeConfig;

    @Resource
    private ElectricityCabinetService electricityCabinetService;

    @Resource
    private ReturnBatterySuccessHandlerService returnBatterySuccessHandlerService;

    @Resource
    private EnterpriseRentRecordService enterpriseRentRecordService;

    @Resource
    private EnterpriseUserCostRecordService enterpriseUserCostRecordService;


    @Resource
    ApplicationContext applicationContext;

    /**
     * 多次扫码退电
     *
     * @param userInfo userInfo
     * @param cabinet  cabinet
     * @return Pair
     */
    @Override
    public Pair<Boolean, Object> lessTimeReturnBatteryHandler(UserInfo userInfo, ElectricityCabinet cabinet) {
        // 5分钟内是否有退电订单
        Long scanTime = StrUtil.isEmpty(exchangeConfig.getScanTime()) ? 180000L : Long.parseLong(exchangeConfig.getScanTime());

        RentBatteryOrder lastRentBatteryOrder = rentBatteryOrderService.queryLatelyRentReturnOrder(userInfo.getUid(), System.currentTimeMillis() - scanTime, System.currentTimeMillis(),
                RentBatteryOrder.TYPE_USER_RETURN);
        if (Objects.isNull(lastRentBatteryOrder)) {
            log.info("ReturnBattery Check Info! lastRentBatteryOrder is null, uid is {},scanTime is {}, startTime is {} , currentTime is {}", userInfo.getUid(), scanTime, System.currentTimeMillis() - scanTime,
                    System.currentTimeMillis());
            return Pair.of(false, null);
        }

        ReturnBatteryLessTimeScanVo vo = new ReturnBatteryLessTimeScanVo();
        vo.setIsEnterMoreExchange(LessScanConstant.ENTER_MORE_EXCHANGE);
        if (!isSatisfySelfOpenCondition(lastRentBatteryOrder.getOrderId(), lastRentBatteryOrder.getElectricityCabinetId(), lastRentBatteryOrder.getUpdateTime(),
                lastRentBatteryOrder.getCellNo())) {
            // 仓门不满足开仓条件
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            log.warn("ReturnBattery Check Warn! cell is not SatisfySelfOpen, orderId is{}", lastRentBatteryOrder.getOrderId());
            return Pair.of(true, vo);
        }

        vo.setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN).setCell(lastRentBatteryOrder.getCellNo()).setOrderId(lastRentBatteryOrder.getOrderId());

        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        // 用户绑定电池为空，返回自主开仓
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("ReturnBattery Check WARN! userBindingBatterySn is null, uid is {}", userInfo.getUid());
            // 没有在仓，需要返回前端仓门号
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }

        String userBindingBatterySn = electricityBattery.getSn();
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(userBindingBatterySn, cabinet.getId());
        // 用户电池是否在仓: 在仓（上一个订单旧仓门内），仓门锁状态：关闭
        if (Objects.nonNull(cabinetBox) && Objects.equals(cabinetBox.getIsLock(), ElectricityCabinetBox.CLOSE_DOOR) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(
                Integer.valueOf(cabinetBox.getCellNo()), lastRentBatteryOrder.getCellNo())) {
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL);
            // 补充旧订单
            fixLastReturnOrderIsSuccess(lastRentBatteryOrder, cabinetBox);
            return Pair.of(true, vo);
        } else {
            // 前端自主开仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }

    }

    private void fixLastReturnOrderIsSuccess(RentBatteryOrder lastRentBatteryOrder, ElectricityCabinetBox cabinetBox) {
        // 修改退电流程，补充操作记录
        RentBatteryOrder updateRentBattery = new RentBatteryOrder();
        updateRentBattery.setId(lastRentBatteryOrder.getId());
        updateRentBattery.setCellNo(Integer.parseInt(cabinetBox.getCellNo()));
        updateRentBattery.setUpdateTime(System.currentTimeMillis());
        updateRentBattery.setRemark(ExchangeRemarkConstant.TWO_SCAN_RENT_BATTERY_SUCCESS);
        updateRentBattery.setStatus(RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS);
        rentBatteryOrderService.update(updateRentBattery);

        // 补充操作记录
        List<ElectricityCabinetOrderOperHistory> list = CollUtil.newArrayList();
        list.add(ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis())
                .orderId(lastRentBatteryOrder.getOrderId()).tenantId(lastRentBatteryOrder.getTenantId()).msg(cabinetBox.getCellNo() + LessScanSeqEnum.RETURN_BATTERY_TWO_SCAN_CLOSE_CELL.getDesc())
                .seq(LessScanSeqEnum.RETURN_BATTERY_TWO_SCAN_CLOSE_CELL.getSeq()).type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_RENT_BACK)
                .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build());
        list.add(ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis())
                .orderId(lastRentBatteryOrder.getOrderId()).tenantId(lastRentBatteryOrder.getTenantId()).msg(cabinetBox.getCellNo() + LessScanSeqEnum.RETURN_BATTERY_TWO_SCAN_CHECK.getDesc())
                .seq(LessScanSeqEnum.RETURN_BATTERY_TWO_SCAN_CHECK.getSeq()).type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_RENT_BACK)
                .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build());
        electricityCabinetOrderOperHistoryService.batchInsert(list);

        // 解绑电池等后续操作
        returnBatterySuccessHandlerService.checkReturnBatteryDoor(lastRentBatteryOrder);

        enterpriseRentRecordService.saveEnterpriseReturnRecord(lastRentBatteryOrder.getUid());
        //记录企业用户还电池记录
        enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(UserCostTypeEnum.COST_TYPE_RETURN_BATTERY.getCode(), lastRentBatteryOrder);
    }


    /**
     * 多次扫码换电
     *
     * @param userInfo           userInfo
     * @param cabinet            cabinet
     * @param electricityBattery electricityBattery
     * @param exchangeDTO        exchangeDTO
     * @return Boolean=false继续走正常换电
     */
    @Override
    public Pair<Boolean, ExchangeUserSelectVO> lessTimeExchangeTwoCountAssert(UserInfo userInfo, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
                                                                              LessTimeExchangeDTO exchangeDTO) {
        if (Objects.isNull(exchangeDTO)) {
            log.error("lessTimeExchangeTwoCountAssert Error! exchangeDTO is null");
            return Pair.of(false, null);
        }
        log.info("lessTimeExchangeTwoCountAssert Info! version is {}", exchangeDTO.getVersion());

        // 兼容以前的小程序旧版本
        if (StrUtil.isEmpty(exchangeDTO.getVersion()) || VersionUtil.compareVersion(exchangeDTO.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
            return lessTimeExchangeTwoCountAssertOldVersion(userInfo, cabinet, electricityBattery, exchangeDTO);
        }

        if (Objects.equals(exchangeDTO.getIsReScanExchange(), OrderQueryV3.RESCAN_EXCHANGE)) {
            log.info("OrderV3 INFO! not same cabinet, normal exchange");
            return Pair.of(false, null);
        }
        Long uid = userInfo.getUid();

        // 默认取5分钟的订单，可选择配置
        Long scanTime = StrUtil.isEmpty(exchangeConfig.getScanTime()) ? 180000L : Long.parseLong(exchangeConfig.getScanTime());

        // 上次5分钟内的换电订单
        ElectricityCabinetOrder lastOrder = electricityCabinetOrderService.selectLatelyExchangeOrder(uid, System.currentTimeMillis() - scanTime, System.currentTimeMillis());
        // 上次5分钟内的租电订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryLatelyRentReturnOrder(uid, System.currentTimeMillis() - scanTime, System.currentTimeMillis(),
                RentBatteryOrder.TYPE_USER_RENT);

        if (Objects.isNull(lastOrder) && Objects.isNull(rentBatteryOrder)) {
            log.warn("OrderV3 WARN! lessTimeExchangeTwoCountAssert.lastOrder and rentBatteryOrder is null, uid is {}, scanTime is {}, startTime is {}, currentTime is {}", scanTime, uid, System.currentTimeMillis() - scanTime,
                    System.currentTimeMillis());
            return Pair.of(false, null);
        }

        // 5分钟内既有租电也有换电，需要判断最近的一次到底是换电还是租电
        Integer lastOrderType = lastOrderType(lastOrder, rentBatteryOrder);
        if (Objects.isNull(lastOrderType)) {
            log.warn("OrderV3 WARN! lastOrderType is null, uid is {}", uid);
            return Pair.of(false, null);
        }

        log.info("OrderV3 INFO! uid is {} , lastOrderType is {}", userInfo.getUid(), lastOrderType);
        // 上次订单的eid
        Integer electricityCabinetId =
                Objects.equals(lastOrderType, LastOrderTypeEnum.LAST_RENT_ORDER.getCode()) ? rentBatteryOrder.getElectricityCabinetId() : lastOrder.getElectricityCabinetId();

        // 扫码柜机和订单不是同一个柜机进行处理
        if (!Objects.equals(electricityCabinetId, exchangeDTO.getEid())) {
            log.warn("OrderV3 WARN! scan eid not equal order eid, eid is {}, scanEid is {}", electricityCabinetId, exchangeDTO.getEid());
            return scanCabinetNotEqualOrderCabinetHandler(userInfo, electricityBattery, electricityCabinetId);
        }

        if (lastOrderIsSuccess(lastOrderType, lastOrder, rentBatteryOrder)) {
            // 上一个 换电成功或者租电成功
            return lastExchangeSuccessHandler(lastOrder, cabinet, electricityBattery, rentBatteryOrder, userInfo, lastOrderType);
        } else {
            // 上一个失败，必然是换电，租电失败也走不到这里
            return lastExchangeFailHandler(lastOrder, cabinet, electricityBattery, userInfo, exchangeDTO.getCode(), exchangeDTO.getSecondFlexibleRenewal());
        }
    }


    private Pair<Boolean, ExchangeUserSelectVO> lastExchangeFailHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo, Integer code, Integer secondFlexibleRenewal) {
        String orderStatus = lastOrder.getOrderStatus();
        if (StrUtil.isEmpty(orderStatus)) {
            log.info("OrderV3 INFO! lastExchangeFailHandler.orderStatus is null, orderId is {}", lastOrder.getOrderId());
            return Pair.of(false, null);
        }

        log.info("OrderV3 INFO! lastExchangeFailHandler.orderStatus is {}", orderStatus);
        String service = OrderStatusEnum.getService(orderStatus);
        if (StrUtil.isEmpty(service)) {
            return Pair.of(false, null);
        }
        OrderStatusStrategy orderStatusStrategy = (OrderStatusStrategy) applicationContext.getBean(service);
        return orderStatusStrategy.process(lastOrder, cabinet, electricityBattery, userInfo, code, secondFlexibleRenewal);
    }

    private Pair<Boolean, ExchangeUserSelectVO> lastExchangeSuccessHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
                                                                           RentBatteryOrder rentBatteryOrder, UserInfo userInfo, Integer lastOrderType) {
        String service = LastOrderTypeEnum.getService(lastOrderType);
        if (StrUtil.isEmpty(service)) {
            log.error("OrderV3 ERROR! lastExchangeSuccessHandler.service is null, lastOrderType is {}", lastOrderType);
            return Pair.of(false, null);
        }
        OrderProcessingStrategy successHandler = applicationContext.getBean(service, OrderProcessingStrategy.class);
        return successHandler.processOrder(cabinet, electricityBattery, userInfo, rentBatteryOrder, lastOrder);
    }


    private Boolean lastOrderIsSuccess(Integer lastOrderType, ElectricityCabinetOrder lastOrder, RentBatteryOrder rentBatteryOrder) {
        return (Objects.equals(lastOrderType, LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode()) && Objects.nonNull(lastOrder) && Objects.equals(lastOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS))
                || (Objects.equals(lastOrderType, LastOrderTypeEnum.LAST_RENT_ORDER.getCode()) && Objects.nonNull(rentBatteryOrder) && Objects.equals(rentBatteryOrder.getStatus(),
                RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS));
    }

    private static Integer lastOrderType(ElectricityCabinetOrder lastOrder, RentBatteryOrder rentBatteryOrder) {
        // 判断上次到底是租电还是换电
        Integer lastOrderType = null;
        if (Objects.isNull(rentBatteryOrder)) {
            lastOrderType = LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode();
        } else if (Objects.isNull(lastOrder)) {
            lastOrderType = LastOrderTypeEnum.LAST_RENT_ORDER.getCode();
        } else {
            // 需要根据创建时间判断最新的,到底最后一次是租电还是换电
            lastOrderType = rentBatteryOrder.getCreateTime().compareTo(lastOrder.getCreateTime()) > 0 ? LastOrderTypeEnum.LAST_RENT_ORDER.getCode()
                    : LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode();
        }
        return lastOrderType;
    }

    private Pair<Boolean, ExchangeUserSelectVO> scanCabinetNotEqualOrderCabinetHandler(UserInfo userInfo, ElectricityBattery electricityBattery, Integer electricityCabinetId) {
        // 用户绑定的电池为空，走正常换电
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderV3 WARN! scan eid not equal order eid, userBindingBatterySn is null, uid is {}", userInfo.getUid());
            return Pair.of(false, null);
        }

        //  用户绑定的电池是在上一个订单的柜机中
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), electricityCabinetId);
        if (Objects.isNull(cabinetBox)) {
            log.warn("OrderV3 WARN! userBindingBatterySnEid not equal orderEid , sn is {}, eid is {}", electricityBattery.getSn(), electricityCabinetId);
            return Pair.of(false, null);
        }

        // 返回柜机名称和重新扫码标识
        ElectricityCabinet orderCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(orderCabinet)) {
            log.error("OrderV3 ERROR! cabinet is null, eid is {}", electricityCabinetId);
            return Pair.of(false, null);
        }
        ExchangeUserSelectVO vo = ExchangeUserSelectVO.builder().isTheSameCabinet(LessScanConstant.NOT_SAME_CABINET).cabinetName(orderCabinet.getName()).build();
        return Pair.of(true, vo);
    }


    @Override
    public String openFullBatteryCellHandlerService(ElectricityCabinetOrder cabinetOrder, ElectricityCabinet cabinet, Integer cellNo, String batteryName, String oldCell) {
        return openFullBatteryCellHandler(cabinetOrder, cabinet, cellNo, batteryName, oldCell);
    }

    @Override
    public Triple<Boolean, String, Object> allocateFullBatteryBoxService(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee) {
        return allocateFullBatteryBox(electricityCabinet, userInfo, franchisee);
    }

    @Override
    public Boolean isSatisfySelfOpenConditionService(String orderId, Integer eid, Long startTime, Integer cell) {
        return isSatisfySelfOpenCondition(orderId, eid, startTime, cell);
    }


    // ------------------旧版本

    /**
     * @return Boolean=false继续走正常换电
     */
    private Pair<Boolean, ExchangeUserSelectVO> lessTimeExchangeTwoCountAssertOldVersion(UserInfo userInfo, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
                                                                                         LessTimeExchangeDTO exchangeDTO) {
        if (Objects.equals(exchangeDTO.getIsReScanExchange(), OrderQueryV3.RESCAN_EXCHANGE)) {
            log.info("OrderOldV3 INFO! not same cabinet, normal exchange");
            return Pair.of(false, null);
        }

        Long uid = userInfo.getUid();
        ElectricityCabinetOrder lastOrder = electricityCabinetOrderService.selectLatelyExchangeOrderByDate(uid, System.currentTimeMillis());
        if (Objects.isNull(lastOrder)) {
            log.warn("OrderOldV3 WARN! lowTimeExchangeTwoCountAssert.lastOrder is null, currentUid is {}", uid);
            return Pair.of(false, null);
        }

        // 默认取5分钟的订单，可选择配置
        Long scanTime = StrUtil.isEmpty(exchangeConfig.getScanTime()) ? 180000L : Long.parseLong(exchangeConfig.getScanTime());
        log.info("OrderOldV3 INFO! lessTimeExchangeTwoCountAssert.scanTime is {} ,currentTime is {}", scanTime, System.currentTimeMillis());

        if (System.currentTimeMillis() - lastOrder.getCreateTime() > scanTime) {
            log.warn("OrderOldV3 WARN! lowTimeExchangeTwoCountAssert.lastOrder over 5 minutes,lastOrderId is {} ", lastOrder.getOrderId());
            return Pair.of(false, null);
        }

        // 扫码柜机和订单不是同一个柜机进行处理
        if (!Objects.equals(lastOrder.getElectricityCabinetId(), exchangeDTO.getEid())) {
            log.warn("OrderOldV3 WARN! scan eid not equal order eid, orderEid is {}, scanEid is {}", lastOrder.getElectricityCabinetId(), exchangeDTO.getEid());
            return scanCabinetNotEqualOrderCabinetHandlerOldVersion(userInfo, electricityBattery, lastOrder);
        }

        if (Objects.equals(lastOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            // 上一个成功
            return lastExchangeSuccessHandlerOldVersion(lastOrder, cabinet, electricityBattery, userInfo);
        } else {
            // 上一个失败
            return lastExchangeFailHandler(lastOrder, cabinet, electricityBattery, userInfo, exchangeDTO.getCode(), exchangeDTO.getSecondFlexibleRenewal());
        }
    }


    private Pair<Boolean, ExchangeUserSelectVO> scanCabinetNotEqualOrderCabinetHandlerOldVersion(UserInfo userInfo, ElectricityBattery electricityBattery,
                                                                                                 ElectricityCabinetOrder lastOrder) {
        // 用户绑定的电池为空，走正常换电
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderOldV3 WARN! scan eid not equal order eid, userBindingBatterySn is null, uid is {}", userInfo.getUid());
            return Pair.of(false, null);
        }

        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), lastOrder.getElectricityCabinetId());
        if (Objects.isNull(cabinetBox)) {
            log.warn("OrderOldV3 WARN! userBindingBatterySn.cabinetBox is null, sn is {}", electricityBattery.getSn());
            return Pair.of(false, null);
        }

        // 用户电池在上一个柜机，并且仓门关闭
        if (Objects.equals(lastOrder.getElectricityCabinetId(), cabinetBox.getElectricityCabinetId())) {
            // 返回柜机名称和重新扫码标识
            ElectricityCabinet orderCabinet = electricityCabinetService.queryByIdFromCache(lastOrder.getElectricityCabinetId());
            if (Objects.isNull(orderCabinet)) {
                log.error("OrderOldV3 ERROR! lastOrder.cabinet is null, eid is {}", lastOrder.getElectricityCabinetId());
                return Pair.of(false, null);
            }
            ExchangeUserSelectVO vo = ExchangeUserSelectVO.builder().isTheSameCabinet(LessScanConstant.NOT_SAME_CABINET).cabinetName(orderCabinet.getName()).build();
            return Pair.of(true, vo);
        } else {
            return Pair.of(false, null);
        }
    }

    private Pair<Boolean, ExchangeUserSelectVO> lastExchangeSuccessHandlerOldVersion(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
                                                                                     UserInfo userInfo) {
        ExchangeUserSelectVO vo = new ExchangeUserSelectVO();
        vo.setIsEnterMoreExchange(LessScanConstant.ENTER_MORE_EXCHANGE);
        vo.setLastExchangeIsSuccess(LessScanConstant.LAST_EXCHANGE_SUCCESS);
        // 上次换电成功，绑定不可能为空
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.error("OrderOldV3 Error! lastExchangeSuccessHandler.userBindBattery is null, lastOrderId is {}", lastOrder.getOrderId());
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            return Pair.of(true, vo);
        }

        // 自主开仓条件校验
        if (!this.isSatisfySelfOpenCondition(lastOrder.getOrderId(), lastOrder.getElectricityCabinetId(), lastOrder.getUpdateTime(), lastOrder.getNewCellNo())) {
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderOldV3 WARN! lastExchangeSuccessHandler is not satisfySelfOpenCondition, orderId is{}", lastOrder.getOrderId());
            return Pair.of(true, vo);
        }

        vo.setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);
        vo.setCabinetName(cabinet.getName());
        vo.setOrderId(lastOrder.getOrderId());
        // 上一次成功，这次只能返回新仓门
        vo.setCell(lastOrder.getNewCellNo());

        // 用户电池是否在仓
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), cabinet.getId());

        log.info("OrderOldV3 INFO! lastExchangeSuccessHandler.cabinetBox is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                JsonUtil.toJson(lastOrder));
        // 电池在仓，并且电池所在仓门=上个订单的新仓门
        if (Objects.nonNull(cabinetBox) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getNewCellNo())) {
            // 在仓内，分配上一个订单的新仓门
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL);
            // 换电成功，后台为用户打开上次成功的新仓门
            BackSelfOpenCellDTO openCellDTO = BackSelfOpenCellDTO.builder().id(lastOrder.getId()).userBindingBatterySn(electricityBattery.getSn())
                    .cell(lastOrder.getNewCellNo()).orderId(lastOrder.getOrderId()).cabinet(cabinet).msg(ExchangeRemarkConstant.EXCHANGE_SUCCESS_SYSTEM_SELF_CELL)
                    .tenantId(lastOrder.getTenantId()).lastOrderType(LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode()).build();
            String sessionId = this.backSelfOpen(openCellDTO);
            vo.setSessionId(sessionId);

            return Pair.of(true, vo);
        } else {
            // 没有在仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }
    }


}
