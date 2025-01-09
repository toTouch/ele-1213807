package com.xiliulou.electricity.service.exchange;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.ExchangeRemarkConstant;
import com.xiliulou.electricity.dto.BackSelfOpenCellDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.enums.LastOrderTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: 抽象公共类
 * @Author: renhang
 * @Date: 2025/01/07
 */

@Slf4j
public class AbstractOrderHandler {
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
    private TenantFranchiseeMutualExchangeService mutualExchangeService;

    @Resource
    private ElectricityCabinetPhysicsOperRecordService electricityCabinetPhysicsOperRecordService;


    public Boolean isSatisfySelfOpenCondition(String orderId, Integer eid, Long startTime, Integer cell) {
        if (Objects.isNull(cell)) {
            log.error("orderV3 Error! isSatisfySelfOpenCondition.params.cell is null");
            return false;
        }
        // 上个订单+5分钟是否存在换电、退电、操作记录
        Long endTime = startTime + 1000 * 60 * 5;
        List<ElectricityCabinetOrder> electricityCabinetOrders = electricityCabinetOrderService.existExchangeOrderInSameCabinetAndCell(startTime, endTime, eid);
        if (CollUtil.isNotEmpty(electricityCabinetOrders)) {
            if (CollUtil.isNotEmpty(electricityCabinetOrders.stream().filter(e -> Objects.equals(e.getOldCellNo(), cell) || Objects.equals(e.getNewCellNo(), cell))
                    .collect(Collectors.toList()))) {
                log.warn("orderV3 warn! isSatisfySelfOpenCondition newAndOldCellNo existExchangeOrder, orderId:{}", orderId);
                return false;
            }
        }
        Integer existReturnRentOrder = rentBatteryOrderService.existReturnRentOrderInSameCabinetAndCell(startTime, endTime, eid, cell);
        if (Objects.nonNull(existReturnRentOrder)) {
            log.warn("orderV3 warn! isSatisfySelfOpenCondition.existReturnOrder, eid:{}, cell is {}", eid, cell);
            return false;
        }
        Integer existOpenRecord = electricityCabinetPhysicsOperRecordService.existOpenRecordInSameCabinetAndCell(startTime, endTime, eid, cell);
        if (Objects.nonNull(existOpenRecord)) {
            log.warn("orderV3 warn! isSatisfySelfOpenCondition.existOpenRecord, eid:{}, cell is {}", eid, cell);
            return false;
        }
        return true;
    }


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


    public Integer getFullCellHandler(ElectricityCabinet cabinet, UserInfo userInfo) {
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


    public Triple<Boolean, String, Object> allocateFullBatteryBox(ElectricityCabinet electricityCabinet, UserInfo userInfo, Franchisee franchisee) {
        // 满电标准的电池
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService.queryElectricityBatteryBox(electricityCabinet, null, null,
                electricityCabinet.getFullyCharged());

        // 过滤掉电池名称不符合标准的
        List<ElectricityCabinetBox> exchangeableList = electricityCabinetBoxList.stream().filter(this::filterNotExchangeable).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(exchangeableList)) {
            log.info("Take Full BATTERY INFO !not found electricityCabinetBoxList,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0026", "换电柜暂无满电电池");
        }

        List<Long> batteryIds = exchangeableList.stream().map(ElectricityCabinetBox::getBId).collect(Collectors.toList());

        List<ElectricityBattery> electricityBatteries = electricityBatteryService.selectByBatteryIds(batteryIds);
        if (CollUtil.isEmpty(electricityBatteries)) {
            return Triple.of(false, "100225", "电池不存在");
        }

        // 判断互通加盟商，并且获取加盟商集合
        Set<Long> franchiseeIdList = null;
        try {
            Triple<Boolean, String, Object> isSameFranchiseeTriple = mutualExchangeService.orderExchangeMutualFranchiseeCheck(userInfo.getTenantId(), userInfo.getFranchiseeId(),
                    electricityCabinet.getFranchiseeId());
            if (!isSameFranchiseeTriple.getLeft()) {
                return isSameFranchiseeTriple;
            }
            franchiseeIdList = (Set<Long>) isSameFranchiseeTriple.getRight();
        } catch (Exception e) {
            log.error("ORDER Error! orderExchangeMutualFranchiseeCheck is error", e);
            franchiseeIdList = CollUtil.newHashSet(userInfo.getFranchiseeId());
        }

        Set<Long> finalFranchiseeIdList = franchiseeIdList;
        electricityBatteries = electricityBatteries.stream().filter(e -> finalFranchiseeIdList.contains(e.getFranchiseeId())).collect(Collectors.toList());
        if (!DataUtil.collectionIsUsable(electricityBatteries)) {
            log.warn("Take Full BATTERY WARN!battery not bind franchisee,eid={}, franchiseeIdList is {}", electricityCabinet.getId(),
                    CollUtil.isEmpty(franchiseeIdList) ? "null" : JsonUtil.toJson(franchiseeIdList));
            return Triple.of(false, "100219", "您的加盟商与电池加盟商不匹配，请更换柜机或联系客服处理。");
        }

        // 获取全部可用电池id
        List<Long> bindingBatteryIds = electricityBatteries.stream().map(ElectricityBattery::getId).collect(Collectors.toList());

        // 把加盟商绑定的电池过滤出来
        exchangeableList = exchangeableList.stream().filter(e -> bindingBatteryIds.contains(e.getBId())).collect(Collectors.toList());

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

    public boolean filterNotExchangeable(ElectricityCabinetBox electricityCabinetBox) {
        return Objects.nonNull(electricityCabinetBox) && Objects.nonNull(electricityCabinetBox.getPower()) && StringUtils.isNotBlank(electricityCabinetBox.getSn())
                && !StringUtils.startsWithIgnoreCase(electricityCabinetBox.getSn(), "UNKNOW");
    }

    public Integer checkExchangeOrSelfOpen(List<String> userBatteryTypes, ElectricityBattery electricityBattery, ElectricityConfig electricityConfig) {
        // 不分型号，和灵活续费没关系；电池没有型号无法做灵活续费校验；电池型号匹配；放行，走取电流程
        if (CollectionUtils.isEmpty(userBatteryTypes) || Objects.isNull(electricityBattery.getModel()) || userBatteryTypes.contains(electricityBattery.getModel())) {
            return FlexibleRenewalEnum.NORMAL.getCode();
        }

        // 到此处后电池与用户的电池型号一定不匹配，灵活续费换电时，才能换电，其他场景均为先退后租
        return Objects.equals(electricityConfig.getIsEnableFlexibleRenewal(), FlexibleRenewalEnum.EXCHANGE_BATTERY.getCode()) ? FlexibleRenewalEnum.EXCHANGE_BATTERY.getCode()
                : FlexibleRenewalEnum.RETURN_BEFORE_RENT.getCode();
    }

}
