package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.ExchangeConfig;
import com.xiliulou.electricity.constant.*;
import com.xiliulou.electricity.dto.BackSelfOpenCellDTO;
import com.xiliulou.electricity.dto.LessTimeExchangeDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.CellTypeEnum;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.enums.LastOrderTypeEnum;
import com.xiliulou.electricity.enums.OrderCheckEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.OrderQueryV3;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import com.xiliulou.electricity.vo.ReturnBatteryLessTimeScanVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName: LessTimeExchangeServiceImpl
 * @description: 短时间换电处理
 * @author: renhang
 * @create: 2024-11-05 17:14
 */
@Service
@Slf4j
public class LessTimeExchangeServiceImpl extends AbstractLessTimeExchangeCommon implements LessTimeExchangeService {

    @Resource
    private RentBatteryOrderService rentBatteryOrderService;

    @Resource
    private ElectricityCabinetOrderService electricityCabinetOrderService;

    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;

    @Resource
    private ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;

    @Resource
    private EleHardwareHandlerManager eleHardwareHandlerManager;

    @Resource
    private ElectricityBatteryService electricityBatteryService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private RedisService redisService;

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
    private UserBatteryTypeService userBatteryTypeService;

    @Resource
    private ElectricityConfigService electricityConfigService;

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
                lastRentBatteryOrder.getCellNo(), ElectricityCabinetOrder.NEW_AND_OLD_CELL)) {
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

            // 修改退电流程，补充操作记录
            RentBatteryOrder updateRentBattery = new RentBatteryOrder();
            updateRentBattery.setId(lastRentBatteryOrder.getId());
            updateRentBattery.setUpdateTime(System.currentTimeMillis());
            updateRentBattery.setRemark(RentReturnRemarkConstant.TWO_SCAN_RENT_BATTERY_SUCCESS);
            updateRentBattery.setStatus(RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS);
            rentBatteryOrderService.update(updateRentBattery);

            // 补充操作记录
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis())
                    .orderId(lastRentBatteryOrder.getOrderId()).tenantId(lastRentBatteryOrder.getTenantId()).msg("归还电池成功")
                    .seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_BY_RETURN_BATTERY).type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_RENT_BACK)
                    .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
            electricityCabinetOrderOperHistoryService.insert(history);

            // 解绑电池等后续操作
            returnBatterySuccessHandlerService.checkReturnBatteryDoor(lastRentBatteryOrder);

            enterpriseRentRecordService.saveEnterpriseReturnRecord(lastRentBatteryOrder.getUid());
            //记录企业用户还电池记录
            enterpriseUserCostRecordService.asyncSaveUserCostRecordForRentalAndReturnBattery(UserCostTypeEnum.COST_TYPE_RETURN_BATTERY.getCode(), lastRentBatteryOrder);
            return Pair.of(true, vo);
        } else {
            // 前端自主开仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            return Pair.of(true, vo);
        }

    }


    /**
     * 多次扫码换电
     *
     * @param userInfo           userInfo
     * @param cabinet            cabinet
     * @param electricityBattery electricityBattery
     * @param exchangeDTO        exchangeDTO
     * @param code               code
     * @return Boolean=false继续走正常换电
     */
    @Override
    public Pair<Boolean, ExchangeUserSelectVO> lessTimeExchangeTwoCountAssert(UserInfo userInfo, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
                                                                              LessTimeExchangeDTO exchangeDTO, Integer code) {
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
            log.warn("OrderV3 WARN! lessTimeExchangeTwoCountAssert.lastOrder and rentBatteryOrder is null, uid is {},scanTime is {}, startTime is {}, currentTime is {}", scanTime, uid, System.currentTimeMillis() - scanTime,
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

        if (Objects.equals(lastOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS) || Objects.equals(rentBatteryOrder.getStatus(),
                RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)) {
            // 上一个 换电成功或者租电成功
            return lastExchangeSuccessHandler(lastOrder, cabinet, electricityBattery, rentBatteryOrder, userInfo, lastOrderType);
        } else {
            // 上一个失败，必然是换电，租电失败也走不到这里
            return lastExchangeFailHandler(lastOrder, cabinet, electricityBattery, userInfo, code);
        }
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


    public Pair<Boolean, ExchangeUserSelectVO> lastExchangeFailHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo,
                                                                       Integer code) {
        ExchangeUserSelectVO vo = new ExchangeUserSelectVO();
        vo.setIsEnterMoreExchange(LessScanConstant.ENTER_MORE_EXCHANGE);
        vo.setLastExchangeIsSuccess(LessScanConstant.LAST_EXCHANGE_FAIL);

        String orderStatus = lastOrder.getOrderStatus();
        if (StrUtil.isEmpty(orderStatus)) {
            log.info("OrderV3 INFO! lastExchangeFailHandler.orderStatus is null, orderId is {}", lastOrder.getOrderId());
            return Pair.of(false, null);
        }

        log.info("OrderV3 INFO! lastExchangeFailHandler.orderStatus is {}", orderStatus);
        //  旧仓门电池检测失败或超时 或者 旧仓门开门失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_OPEN_FAIL) || Objects.equals(orderStatus, ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
            return oldCellCheckFail(lastOrder, electricityBattery, vo, cabinet, userInfo, code);
        }

        //  新仓门开门失败
        if (Objects.equals(orderStatus, ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
            return newCellOpenFail(lastOrder, electricityBattery, vo, cabinet);
        }

        return Pair.of(false, null);
    }

    public Pair<Boolean, ExchangeUserSelectVO> lastExchangeSuccessHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery,
                                                                          RentBatteryOrder rentBatteryOrder, UserInfo userInfo, Integer lastOrderType) {
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.error("OrderV3 Error! lastExchangeSuccessHandler.userBindBattery is null, uid is {}", userInfo.getUid());
            throw new CustomBusinessException("上次换电成功，用户绑定电池为空");
        }

        ExchangeUserSelectVO vo = new ExchangeUserSelectVO();
        vo.setIsEnterMoreExchange(LessScanConstant.ENTER_MORE_EXCHANGE).setLastExchangeIsSuccess(LessScanConstant.LAST_EXCHANGE_SUCCESS).setCabinetName(cabinet.getName());

        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(electricityBattery.getSn(), cabinet.getId());

        // 上次是租电
        if (Objects.equals(lastOrderType, LastOrderTypeEnum.LAST_RENT_ORDER.getCode())) {
            if (Objects.isNull(rentBatteryOrder)) {
                log.error("OrderV3 Error! lastExchangeSuccessHandler.order is rentOrder, but rentOrder is null, uid is {}", userInfo.getUid());
                return Pair.of(false, null);
            }

            if (!isSatisfySelfOpenCondition(rentBatteryOrder.getOrderId(), rentBatteryOrder.getElectricityCabinetId(), rentBatteryOrder.getUpdateTime(),
                    rentBatteryOrder.getCellNo(), ElectricityCabinetOrder.NEW_AND_OLD_CELL)) {
                vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
                log.warn("OrderV3 WARN! LastExchangeSuccessHandler RentOrderCell is not SatisfySelfOpen, orderId is{}", rentBatteryOrder.getOrderId());
                return Pair.of(true, vo);
            }

            vo.setOrderId(rentBatteryOrder.getOrderId()).setCell(rentBatteryOrder.getCellNo()).setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);

            log.info("OrderV3 INFO! LastRentSuccessHandler RentOrder cabinetBox is {}, rentBatteryOrder is {}",
                    Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null", JsonUtil.toJson(rentBatteryOrder));

            // 上次租电成功，电池在仓，并且电池所在仓门=上个订单的新仓门
            if (Objects.nonNull(cabinetBox) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(Integer.valueOf(cabinetBox.getCellNo()),
                    rentBatteryOrder.getCellNo())) {
                // 租电成功，后台为用户打开租电成功的仓门
                BackSelfOpenCellDTO openCellDTO = BackSelfOpenCellDTO.builder().id(rentBatteryOrder.getId()).userBindingBatterySn(electricityBattery.getSn())
                        .cell(rentBatteryOrder.getCellNo()).orderId(rentBatteryOrder.getOrderId()).cabinet(cabinet).msg(RentReturnRemarkConstant.RENT_SUCCESS_SYSTEM_SELF_CELL)
                        .tenantId(rentBatteryOrder.getTenantId()).lastOrderType(LastOrderTypeEnum.LAST_RENT_ORDER.getCode()).build();
                // 自主开仓
                vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL).setSessionId(this.backSelfOpen(openCellDTO));
                return Pair.of(true, vo);
            }
        }

        // 上次换电电成功
        if (Objects.equals(lastOrderType, LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode())) {
            if (Objects.isNull(rentBatteryOrder)) {
                log.error("OrderV3 Error! lastExchangeSuccessHandler.order is exchangeOrder, but exchangeOrder is null, uid is {}", userInfo.getUid());
                return Pair.of(false, null);
            }

            if (!isSatisfySelfOpenCondition(lastOrder.getOrderId(), lastOrder.getElectricityCabinetId(), lastOrder.getUpdateTime(), lastOrder.getNewCellNo(),
                    ElectricityCabinetOrder.NEW_CELL)) {
                vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
                log.warn("OrderV3 WARN! LastExchangeSuccessHandler ExchangeOrder is not SatisfySelfOpen, orderId is{}", lastOrder.getOrderId());
                return Pair.of(true, vo);
            }

            vo.setOrderId(lastOrder.getOrderId())
                    .setCell(lastOrder.getNewCellNo())
                    .setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);

            log.info("OrderV3 INFO! LastExchangeSuccessHandler ExchangeOrder cabinetBox is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
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
        }

        // 没有在仓
        vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
        return Pair.of(true, vo);
    }


    private Pair<Boolean, ExchangeUserSelectVO> newCellOpenFail(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ExchangeUserSelectVO vo, ElectricityCabinet cabinet) {
        if (!isSatisfySelfOpenCondition(lastOrder.getOrderId(), lastOrder.getElectricityCabinetId(), lastOrder.getUpdateTime(), lastOrder.getNewCellNo(),
                ElectricityCabinetOrder.NEW_CELL)) {
            // 新仓门不满足开仓条件
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderV3 WARN!newCellOpenFail is not SatisfySelfOpen, orderId is{}", lastOrder.getOrderId());
            return Pair.of(true, vo);
        }

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


    private Pair<Boolean, ExchangeUserSelectVO> oldCellCheckFail(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ExchangeUserSelectVO vo, ElectricityCabinet cabinet,
                                                                 UserInfo userInfo, Integer code) {

        if (!isSatisfySelfOpenCondition(lastOrder.getOrderId(), lastOrder.getElectricityCabinetId(), lastOrder.getUpdateTime(), lastOrder.getOldCellNo(),
                ElectricityCabinetOrder.OLD_CELL)) {
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderV3 WARN! oldCellCheckFail is not SatisfySelfOpen, orderId is{}", lastOrder.getOrderId());
            return Pair.of(true, vo);
        }

        vo.setOrderId(lastOrder.getOrderId());
        vo.setIsSatisfySelfOpen(LessScanConstant.IS_SATISFY_SELF_OPEN);

        // 用户绑定电池为空，返回自主开仓
        if (Objects.isNull(electricityBattery) || StrUtil.isEmpty(electricityBattery.getSn())) {
            log.warn("OrderV3 WARN!oldCellCheckFail.userBindingBatterySn  is null, uid is {}", lastOrder.getUid());
            // 不在仓，前端会自主开仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            vo.setCell(lastOrder.getOldCellNo());
            return Pair.of(true, vo);
        }

        String userBindingBatterySn = electricityBattery.getSn();
        // 用户电池是否在仓
        ElectricityCabinetBox cabinetBox = electricityCabinetBoxService.queryBySn(userBindingBatterySn, cabinet.getId());

        log.info("OrderV3 INFO! oldCellCheckFail.cabinetBox is {}, lastOrder is {}", Objects.nonNull(cabinetBox) ? JsonUtil.toJson(cabinetBox) : "null",
                JsonUtil.toJson(lastOrder));

        // 判断灵活续费场景下，二次扫码是走去电流程还是自主开仓，true为取电，false为自主开仓
        List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        boolean exchangeBatteryOrNot = checkExchangeOrSelfOpen(userBatteryTypes, electricityBattery, electricityConfig);


        // 租借在仓（上一个订单旧仓门内），仓门锁状态：关闭
        if (Objects.nonNull(cabinetBox) && Objects.equals(cabinetBox.getIsLock(), ElectricityCabinetBox.CLOSE_DOOR) && StrUtil.isNotBlank(cabinetBox.getCellNo()) && Objects.equals(
                Integer.valueOf(cabinetBox.getCellNo()), lastOrder.getOldCellNo())) {

            // 灵活续费
            if (!exchangeBatteryOrNot) {
                BackSelfOpenCellDTO openCellDTO = BackSelfOpenCellDTO.builder().id(lastOrder.getId()).userBindingBatterySn(electricityBattery.getSn())
                        .cell(lastOrder.getOldCellNo()).orderId(lastOrder.getOrderId()).cabinet(cabinet).msg(ExchangeRemarkConstant.FLEXIBLE_RENEWAL_SYSTEM_SELF_CELL)
                        .tenantId(lastOrder.getTenantId()).lastOrderType(LastOrderTypeEnum.LAST_EXCHANGE_ORDER.getCode()).build();
                backSelfOpen(openCellDTO);
                vo.setBeginSelfOpen(ExchangeUserSelectVO.BEGIN_SELF_OPEN);
                vo.setCell(lastOrder.getOldCellNo());
                return Pair.of(true, vo);
            }

            vo.setIsBatteryInCell(LessScanConstant.BATTERY_IN_CELL);
            vo.setIsEnterTakeBattery(LessScanConstant.ENTER_TAKE_BATTERY);
            vo.setCellType(CellTypeEnum.OLD_CELL.getCode());

            // 这里为了兼容选仓换电：只有换电，才去获取满电仓，而选仓取电不走这里
            if (Objects.equals(code, OrderCheckEnum.ORDER.getCode())) {
                // 获取满电仓
                Integer cellNo = this.getFullCellHandler(cabinet, userInfo);
                vo.setCell(cellNo);
                String sessionId = this.openFullBatteryCellHandler(lastOrder, cabinet, cellNo, userBindingBatterySn, cabinetBox.getCellNo());
                vo.setSessionId(sessionId);
            }

            return Pair.of(true, vo);
        } else {
            // 不在仓，前端会自主开仓
            vo.setIsBatteryInCell(LessScanConstant.BATTERY_NOT_CELL);
            vo.setCell(lastOrder.getOldCellNo());
            return Pair.of(true, vo);
        }
    }

    private boolean checkExchangeOrSelfOpen(List<String> userBatteryTypes, ElectricityBattery electricityBattery, ElectricityConfig electricityConfig) {
        // 不分型号，和灵活续费没关系，走取电流程
        if (org.apache.commons.collections.CollectionUtils.isEmpty(userBatteryTypes)) {
            return true;
        }

        if (Objects.isNull(electricityBattery.getModel())) {
            return false;
        }

        // 用户还进来的电池和当前套餐匹配，和灵活续费没关系
        if (userBatteryTypes.contains(electricityBattery.getModel())) {
            return true;
        }

        // 用户还进来的电池和当前套餐不匹配，当租户的配置是“灵活续费——换电”的时候才能走取电流程
        return Objects.nonNull(electricityConfig.getIsEnableFlexibleRenewal()) && Objects.equals(electricityConfig.getIsEnableFlexibleRenewal(),
                FlexibleRenewalEnum.EXCHANGE_BATTERY.getCode());
    }


    private Integer getFullCellHandler(ElectricityCabinet cabinet, UserInfo userInfo) {
        // 执行取电流程，下发开满电仓指令， 按照租电分配满电仓走
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        // 分配满电仓
        Triple<Boolean, String, Object> getFullCellResult = allocateFullBatteryBox(cabinet, userInfo, franchisee);
        if (Boolean.FALSE.equals(getFullCellResult.getLeft())) {
            throw new BizException(getFullCellResult.getMiddle(), "换电柜暂无满电电池");
        }
        return Integer.valueOf((String) getFullCellResult.getRight());
    }


    /**
     * 开满电仓命令下发
     *
     * @param cabinetOrder cabinetOrder
     * @param cabinet      cabinet
     * @param cellNo       cellNo
     * @param batteryName  batteryName
     * @return String
     */
    @Override
    public String openFullBatteryCellHandler(ElectricityCabinetOrder cabinetOrder, ElectricityCabinet cabinet, Integer cellNo, String batteryName, String oldCell) {

        if (cabinet.getVersion().isBlank() || VersionUtil.compareVersion(cabinet.getVersion(), ElectricityCabinetOrderOperHistory.THREE_PERIODS_SUCCESS_RATE_VERSION) < 0) {
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(System.currentTimeMillis()).orderId(cabinetOrder.getOrderId())
                    .tenantId(cabinet.getTenantId()).msg("电池检测成功").seq(ElectricityCabinetOrderOperHistory.OPEN_FULL_CELL_BATTERY)
                    .type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE).result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();

            electricityCabinetOrderOperHistoryService.insert(history);
        }

        ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
        electricityCabinetOrderUpdate.setId(cabinetOrder.getId());
        electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrderUpdate.setNewCellNo(cellNo);
        electricityCabinetOrderUpdate.setRemark(ExchangeRemarkConstant.TAKE_FULL_BATTER);
        electricityCabinetOrderService.update(electricityCabinetOrderUpdate);

        //发送命令
        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderId", cabinetOrder.getOrderId());
        dataMap.put("placeCellNo", oldCell);
        dataMap.put("takeCellNo", cellNo);
        dataMap.put("batteryName", batteryName);

        String sessionId = CacheConstant.OPEN_FULL_CELL + "_" + cabinetOrder.getOrderId();

        HardwareCommandQuery comm = HardwareCommandQuery.builder().sessionId(sessionId).data(dataMap).productKey(cabinet.getProductKey()).deviceName(cabinet.getDeviceName())
                .command(ElectricityIotConstant.OPEN_FULL_CELL).build();
        eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm, cabinet);

        // 设置状态redis
        redisService.set(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + cabinetOrder.getOrderId(), "取电中，请稍后", 5L, TimeUnit.MINUTES);

        return sessionId;
    }


    @Override
    public Triple<Boolean, String, Object> allocateFullBatteryBox(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee) {
        // 满电标准的电池
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryElectricityBatteryBox(electricityCabinet, null, null,
                electricityCabinet.getFullyCharged());

        // 过滤掉电池名称不符合标准的
        List<ElectricityCabinetBox> exchangeableList = electricityCabinetBoxList.stream().filter(item -> filterNotExchangeable(item)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(exchangeableList)) {
            log.info("RENT BATTERY INFO!not found electricityCabinetBoxList,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
        }

        String fullBatteryCell = null;

        for (int i = 0; i < exchangeableList.size(); i++) {
            // 20240614修改：过滤掉电池不符合标准的电池
            fullBatteryCell = rentBatteryOrderService.acquireFullBatteryBox(exchangeableList, userInfo, franchisee, electricityCabinet.getFullyCharged());
            if (StringUtils.isBlank(fullBatteryCell)) {
                log.info("RENT BATTERY INFO!not found fullBatteryCell,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
            }

            if (redisService.setNx(CacheConstant.CACHE_LAST_ALLOCATE_FULLY_BATTERY_CELL + electricityCabinet.getId() + ":" + fullBatteryCell, "1", 4 * 1000L, false)) {
                return Triple.of(true, null, fullBatteryCell);
            }
        }

        return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
    }

    private boolean filterNotExchangeable(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.nonNull(electricityCabinetBox) && Objects.nonNull(electricityCabinetBox.getPower()) && StringUtils.isNotBlank(electricityCabinetBox.getSn())
                && !StringUtils.startsWithIgnoreCase(electricityCabinetBox.getSn(), "UNKNOW");
    }
}
