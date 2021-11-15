package com.xiliulou.electricity.queue;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.*;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: lxc
 * @Date: 2020/12/3 08:31
 * @Description:
 */

@Service
@Slf4j
public class EleOperateQueueHandler {

    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("eleOperateQueueExecutor", 20, "ELE_OPERATE_QUEUE_EXECUTOR");
    ExecutorService startService = XllThreadPoolExecutors.newFixedThreadPool("eleOperateQueueStart", 1, "ELE_OPERATE_QUEUE_START");
    private volatile boolean shutdown = false;
    private final LinkedBlockingQueue<EleOpenDTO> queue = new LinkedBlockingQueue<>();

    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    ElectricityConfigService electricityConfigService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;

    @EventListener({WebServerInitializedEvent.class})
    public void startHandleElectricityCabinetOperate() {
        initElectricityCabinetOperate();
    }

    private void initElectricityCabinetOperate() {
        log.info("初始化换电柜操作响应处理器");
        startService.execute(() -> {
            while (!shutdown) {
                EleOpenDTO eleOpenDTO = null;
                try {
                    eleOpenDTO = queue.take();
                    log.info(" QUEUE get a message ={}", eleOpenDTO);

                    EleOpenDTO finalOpenDTO = eleOpenDTO;
                    executorService.execute(() -> {
                        handleOrderAfterOperated(finalOpenDTO);
                    });

                } catch (Exception e) {
                    log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR! ", e);
                }

            }
        });
    }

    /**
     * 接收到响应的操作信息
     *
     * @param finalOpenDTO
     */
    private void handleOrderAfterOperated(EleOpenDTO finalOpenDTO) {
        //参数
        String type = finalOpenDTO.getType();
        String orderId = finalOpenDTO.getOrderId();
        Double orderSeq = finalOpenDTO.getOrderSeq();

        //查找订单
        if (Objects.equals(type, HardwareCommand.ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP)
                || Objects.equals(type, HardwareCommand.ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP)) {

            ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(orderId);
            if (Objects.isNull(electricityCabinetOrder)) {
                return;
            }

            //若app订单状态大于云端订单状态则处理
            if (Objects.isNull(orderSeq) || orderSeq - electricityCabinetOrder.getOrderSeq() >= 1 || Math.abs(orderSeq - electricityCabinetOrder.getOrderSeq()) < 1) {
                if (Objects.equals(type, HardwareCommand.ELE_COMMAND_INIT_EXCHANGE_ORDER_RSP)) {
                    handelInitExchangeOrder(electricityCabinetOrder, finalOpenDTO);
                }

                if (Objects.equals(type, HardwareCommand.ELE_COMMAND_COMPLETE_EXCHANGE_ORDER_RSP)) {
                    handelCompleteExchangeOrder(electricityCabinetOrder, finalOpenDTO);
                }
            }
        } else {

            //租还电池订单
            RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByOrderId(orderId);
            if (Objects.isNull(rentBatteryOrder)) {
                return;
            }

            handleRentOrder(rentBatteryOrder, finalOpenDTO);

        }
    }

    public void shutdown() {
        shutdown = true;
        executorService.shutdown();
    }

    public void putQueue(EleOpenDTO eleOpenDTO) {
        try {
            queue.put(eleOpenDTO);
        } catch (InterruptedException e) {
            log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR!", e);
        }
    }

    //开旧门通知
    public void handelInitExchangeOrder(ElectricityCabinetOrder electricityCabinetOrder, EleOpenDTO finalOpenDTO) {

        //开门失败
        if (finalOpenDTO.getIsProcessFail()) {
            //取消订单
            if (finalOpenDTO.getIsNeedEndOrder()) {
                ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
                newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
                newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
                newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_CANCEL);
                newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_CANCEL);
                newElectricityCabinetOrder.setOldElectricityBatterySn(finalOpenDTO.getBatterySn());
                electricityCabinetOrderService.update(newElectricityCabinetOrder);

                //清除柜机锁定缓存
                redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
                return;
            }

            //修改订单
            ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
            newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
            newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
            newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
            newElectricityCabinetOrder.setOldElectricityBatterySn(finalOpenDTO.getBatterySn());
            electricityCabinetOrderService.update(newElectricityCabinetOrder);
            return;
        }

        //修改订单状态
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
        newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
        newElectricityCabinetOrder.setOldElectricityBatterySn(finalOpenDTO.getBatterySn());
        electricityCabinetOrderService.update(newElectricityCabinetOrder);

        //订单状态为旧电池检测成功则分配新仓门
        if (Objects.equals(newElectricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            String cellNo = null;
            try {//查找用户
                UserInfo userInfo = userInfoService.queryByUid(electricityCabinetOrder.getUid());
                if (Objects.isNull(userInfo)) {
                    log.error("userInfo is null!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }

                FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
                if (Objects.isNull(oldFranchiseeUserInfo)) {
                    log.error("franchiseeUserInfo is null!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }

                //用户解绑旧电池 旧电池到底是哪块，不确定
                FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
                franchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
                franchiseeUserInfo.setNowElectricityBatterySn(null);
                franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
                franchiseeUserInfoService.update(franchiseeUserInfo);


                //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
                ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
                if (Objects.nonNull(electricityBattery)) {
                    if (!Objects.equals(electricityBattery.getSn(), newElectricityCabinetOrder.getOldElectricityBatterySn())) {
                        ElectricityBattery newElectricityBattery = new ElectricityBattery();
                        newElectricityBattery.setId(electricityBattery.getId());
                        newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
                        newElectricityBattery.setUid(null);
                        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                        electricityBatteryService.updateByOrder(newElectricityBattery);
                    }
                }

                //放入电池改为在仓
                ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(newElectricityCabinetOrder.getOldElectricityBatterySn());
                if (Objects.nonNull(oldElectricityBattery)) {
                    ElectricityBattery newElectricityBattery = new ElectricityBattery();
                    newElectricityBattery.setId(oldElectricityBattery.getId());
                    newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
                    newElectricityBattery.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
                    newElectricityBattery.setUid(null);
                    newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                    electricityBatteryService.updateByOrder(newElectricityBattery);
                }

                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
                if (Objects.isNull(electricityCabinet)) {
                    log.error("handelInitExchangeOrder is error!not found electricityCabinet! electricityCabinetId:{}", electricityCabinetOrder.getElectricityCabinetId());
                    return;
                }

                //分配电池 --只分配满电电池
                Triple<Boolean, String, Object> tripleResult;
                if (Objects.equals(oldFranchiseeUserInfo.getModelType(), FranchiseeUserInfo.MEW_MODEL_TYPE)) {
                    tripleResult = rentBatteryOrderService.findUsableBatteryCellNo(electricityCabinet, electricityCabinetOrder.getOldCellNo().toString(), oldFranchiseeUserInfo.getBatteryType(), oldFranchiseeUserInfo.getFranchiseeId());
                } else {
                    tripleResult = rentBatteryOrderService.findUsableBatteryCellNo(electricityCabinet, electricityCabinetOrder.getOldCellNo().toString(), null, oldFranchiseeUserInfo.getFranchiseeId());
                }

                if (Objects.isNull(tripleResult)) {
                    log.error("check Old Battery not find fully battery1!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }

                if (!tripleResult.getLeft()) {
                    log.error("check Old Battery not find fully battery2!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }

                cellNo = tripleResult.getMiddle();

                if (Objects.isNull(cellNo)) {
                    log.error("check Old Battery not find fully battery3!orderId:{}", electricityCabinetOrder.getOrderId());
                    return;
                }

                //根据换电柜id和仓门查出电池
                ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), cellNo);
                if (Objects.isNull(electricityCabinetBox)) {
                    log.error("check Old Battery not find electricityCabinetBox! electricityCabinetId:{},cellNo:{}", electricityCabinetOrder.getElectricityCabinetId(), cellNo);
                    return;
                }
                ElectricityBattery newElectricityBattery = electricityBatteryService.queryBySn(electricityCabinetBox.getSn());
                if (Objects.isNull(newElectricityBattery)) {
                    log.error("check Old Battery not find electricityBattery! sn:{}", electricityCabinetBox.getSn());
                    return;
                }

                //修改订单状态
                ElectricityCabinetOrder innerElectricityCabinetOrder = new ElectricityCabinetOrder();
                innerElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
                innerElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
                innerElectricityCabinetOrder.setNewElectricityBatterySn(newElectricityBattery.getSn());
                innerElectricityCabinetOrder.setNewCellNo(Integer.valueOf(cellNo));
                electricityCabinetOrderService.update(innerElectricityCabinetOrder);


                //发送命令
                HashMap<String, Object> dataMap = Maps.newHashMap();
                dataMap.put("cell_no", cellNo);
                dataMap.put("order_id", electricityCabinetOrder.getOrderId());
                dataMap.put("serial_number", newElectricityCabinetOrder.getNewElectricityBatterySn());
                dataMap.put("status", electricityCabinetOrder.getStatus());
                dataMap.put("old_cell_no", electricityCabinetOrder.getOldCellNo());

                HardwareCommandQuery comm = HardwareCommandQuery.builder()
                        .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getUid() + "_" + electricityCabinetOrder.getOrderId())
                        .data(dataMap)
                        .productKey(electricityCabinet.getProductKey())
                        .deviceName(electricityCabinet.getDeviceName())
                        .command(HardwareCommand.ELE_COMMAND_ORDER_OPEN_NEW_DOOR).build();
                eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            } catch (Exception e) {
                log.error("e", e);
            } finally {
                redisService.delete(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_" + cellNo);
            }

        }
    }

    //开新门通知
    public void handelCompleteExchangeOrder(ElectricityCabinetOrder electricityCabinetOrder, EleOpenDTO finalOpenDTO) {
        //开门失败
        if (finalOpenDTO.getIsProcessFail()) {
            //取消订单
            if (finalOpenDTO.getIsNeedEndOrder()) {
                ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
                newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
                newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
                newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_CANCEL);
                newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_CANCEL);
                electricityCabinetOrderService.update(newElectricityCabinetOrder);

                //清除柜机锁定缓存
                redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
                return;
            }

            //修改订单
            ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
            newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
            newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
            newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
            electricityCabinetOrderService.update(newElectricityCabinetOrder);
            return;
        }

        //修改订单
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        newElectricityCabinetOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
        newElectricityCabinetOrder.setStatus(finalOpenDTO.getOrderStatus());
        electricityCabinetOrderService.update(newElectricityCabinetOrder);

        if (Objects.equals(newElectricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {

            UserInfo userInfo = userInfoService.queryByUid(electricityCabinetOrder.getUid());
            if (Objects.isNull(userInfo)) {
                return;
            }


            //用户绑新电池
            FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
            franchiseeUserInfo.setUserInfoId(userInfo.getId());
            franchiseeUserInfo.setNowElectricityBatterySn(electricityCabinetOrder.getNewElectricityBatterySn());
            franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

            //查看用户是否有以前绑定的电池
            ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(electricityCabinetOrder.getUid());
            if (Objects.nonNull(oldElectricityBattery)) {
                if (Objects.equals(oldElectricityBattery.getSn(), electricityCabinetOrder.getNewElectricityBatterySn())) {
                    //删除柜机被锁缓存
                    redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
                    return;
                }
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(oldElectricityBattery.getId());
                newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
                newElectricityBattery.setUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                electricityBatteryService.updateByOrder(newElectricityBattery);
            }


            //电池改为在用
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetOrder.getNewElectricityBatterySn());
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(electricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
            newElectricityBattery.setElectricityCabinetId(null);
            newElectricityBattery.setUid(electricityCabinetOrder.getUid());
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateByOrder(newElectricityBattery);


            //删除柜机被锁缓存
            redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
        }
    }

    //开租/还 电池门
    public void handleRentOrder(RentBatteryOrder rentBatteryOrder, EleOpenDTO finalOpenDTO) {

        //开门失败
        if (finalOpenDTO.getIsProcessFail()) {
            //取消订单
            if (finalOpenDTO.getIsNeedEndOrder()) {
                RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
                newRentBatteryOrder.setId(rentBatteryOrder.getId());
                newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
                newRentBatteryOrder.setOrderSeq(RentBatteryOrder.STATUS_ORDER_CANCEL);
                newRentBatteryOrder.setStatus(RentBatteryOrder.ORDER_CANCEL);
                rentBatteryOrderService.update(newRentBatteryOrder);

                //清除柜机锁定缓存
                redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
                return;
            }

            //订单状态
            RentBatteryOrder newRentBatteryOrder = new RentBatteryOrder();
            newRentBatteryOrder.setId(rentBatteryOrder.getId());
            newRentBatteryOrder.setUpdateTime(System.currentTimeMillis());
            newRentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
            newRentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
            rentBatteryOrderService.update(newRentBatteryOrder);
            return;
        }

        //订单状态
        rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
        rentBatteryOrder.setOrderSeq(finalOpenDTO.getOrderSeq());
        rentBatteryOrder.setStatus(finalOpenDTO.getOrderStatus());
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            rentBatteryOrder.setElectricityBatterySn(finalOpenDTO.getBatterySn());
        }
        rentBatteryOrderService.update(rentBatteryOrder);

        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)
                && Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)) {
            checkRentBatteryDoor(rentBatteryOrder);
        }

        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)
                && Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)) {
            checkReturnBatteryDoor(rentBatteryOrder);
        }

    }

    //检测租电池
    public void checkRentBatteryDoor(RentBatteryOrder rentBatteryOrder) {

        //查找用户
        UserInfo userInfo = userInfoService.queryByUid(rentBatteryOrder.getUid());
        if (Objects.isNull(userInfo)) {
            return;
        }

        //用户绑新电池
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setUserInfoId(userInfo.getId());
        franchiseeUserInfo.setInitElectricityBatterySn(rentBatteryOrder.getElectricityBatterySn());
        franchiseeUserInfo.setNowElectricityBatterySn(rentBatteryOrder.getElectricityBatterySn());
        franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_BATTERY);
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);


        //查看用户是否有以前绑定的电池
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUid(rentBatteryOrder.getUid());
        if (Objects.nonNull(oldElectricityBattery)) {
            if (Objects.equals(oldElectricityBattery.getSn(), rentBatteryOrder.getElectricityBatterySn())) {
                //删除柜机被锁缓存
                redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
                return;
            }
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(oldElectricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }

        //电池改为在用
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(rentBatteryOrder.getElectricityBatterySn());
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        newElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
        newElectricityBattery.setElectricityCabinetId(null);
        newElectricityBattery.setUid(rentBatteryOrder.getUid());
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.updateByOrder(newElectricityBattery);

        //删除柜机被锁缓存
        redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
    }

    //检测还电池
    public void checkReturnBatteryDoor(RentBatteryOrder rentBatteryOrder) {

        //查找用户
        UserInfo userInfo = userInfoService.queryByUid(rentBatteryOrder.getUid());
        if (Objects.isNull(userInfo)) {
            return;
        }

        //用户解绑电池
        FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
        franchiseeUserInfo.setUserInfoId(userInfo.getId());
        franchiseeUserInfo.setNowElectricityBatterySn(null);
        franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_DEPOSIT);
        franchiseeUserInfoService.updateByUserInfoId(franchiseeUserInfo);

        //查看用户是否有绑定的电池,绑定电池和放入电池不一致则绑定电池处于游离态
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(rentBatteryOrder.getUid());
        if (Objects.nonNull(electricityBattery)) {
            if (!Objects.equals(electricityBattery.getSn(), rentBatteryOrder.getElectricityBatterySn())) {
                ElectricityBattery newElectricityBattery = new ElectricityBattery();
                newElectricityBattery.setId(electricityBattery.getId());
                newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_FREE);
                newElectricityBattery.setUid(null);
                newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                electricityBatteryService.updateByOrder(newElectricityBattery);
            }
        }

        //放入电池改为在仓
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(rentBatteryOrder.getElectricityBatterySn());
        if (Objects.nonNull(oldElectricityBattery)) {
            ElectricityBattery newElectricityBattery = new ElectricityBattery();
            newElectricityBattery.setId(oldElectricityBattery.getId());
            newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
            newElectricityBattery.setElectricityCabinetId(rentBatteryOrder.getElectricityCabinetId());
            newElectricityBattery.setUid(null);
            newElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateByOrder(newElectricityBattery);
        }

        //删除柜机被锁缓存
        redisService.delete(ElectricityCabinetConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
    }

}
