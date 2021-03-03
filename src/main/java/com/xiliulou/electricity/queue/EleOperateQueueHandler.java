package com.xiliulou.electricity.queue;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.service.PubHardwareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author: lxc
 * @Date: 2020/12/3 08:31
 * @Description:
 */

@Service
@Slf4j
public class EleOperateQueueHandler {

    ExecutorService executorService = Executors.newFixedThreadPool(20);
    ExecutorService startService = Executors.newFixedThreadPool(1);
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
    PubHardwareService pubHardwareService;

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
        log.info("finalOpenDTO is -->{}", finalOpenDTO);
        Integer status = finalOpenDTO.getStatus();
        String type = finalOpenDTO.getType();
        String orderId = finalOpenDTO.getOrderId();
        String msg = finalOpenDTO.getMsg();
        Integer orderStatus = finalOpenDTO.getOrderStatus();

        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(orderId);
        if (Objects.isNull(electricityCabinetOrder)) {
            return;
        }

        //若app订单状态大于云端订单状态则处理
        if (Objects.isNull(orderStatus)||electricityCabinetOrder.getStatus() <= orderStatus) {
            if (Objects.equals(type, HardwareCommand.ELE_COMMAND_ORDER_OLD_DOOR_OPEN)) {
                openOldBatteryDoor(electricityCabinetOrder, status, msg);
            }
            if (Objects.equals(type, HardwareCommand.ELE_COMMAND_ORDER_OLD_DOOR_CHECK)) {
                checkOldBattery(electricityCabinetOrder, status, msg);
            }
            if (Objects.equals(type, HardwareCommand.ELE_COMMAND_ORDER_NEW_DOOR_OPEN)) {
                openNewBatteryDoor(electricityCabinetOrder, status, msg);
            }
            if (Objects.equals(type, HardwareCommand.ELE_COMMAND_ORDER_NEW_DOOR_CHECK)) {
                checkNewBattery(electricityCabinetOrder, status, msg);
            }
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
            log.error("LOCKER OPERATE QUEUE ERROR!", e);
        }
    }


    //开旧门通知
    public void openOldBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Integer status, String msg) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, status, ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_OPEN_DOOR, msg)) {
            return;
        }

        //订单状态
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_OPEN_DOOR);
        electricityCabinetOrderService.update(electricityCabinetOrder);

        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(status)
                .type(ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_OPEN_DOOR)
                .uid(electricityCabinetOrder.getUid())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }


    //检查旧电池通知
    public void checkOldBattery(ElectricityCabinetOrder electricityCabinetOrder, Integer status, String msg) {
        //旧电池检测失败
        if (CheckDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, status, ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_CHECK, msg)) {
            return;
        }

        //分配新仓门 新仓门分配失败则弹开旧门
        String cellNo = findNewUsableCellNo(electricityCabinetOrder.getElectricityCabinetId(), electricityCabinetOrder.getOldCellNo().toString());
        if(Objects.isNull(cellNo)){
            return;
        }
        try {
            //根据换电柜id和仓门查出电池
            ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), cellNo);
            ElectricityBattery newElectricityBattery = electricityBatteryService.queryById(electricityCabinetBox.getElectricityBatteryId());
            if (Objects.nonNull(newElectricityBattery)) {
                electricityCabinetOrder.setNewElectricityBatterySn(newElectricityBattery.getSn());
            }
            electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_DEPOSITED);
            electricityCabinetOrder.setNewCellNo(Integer.valueOf(cellNo));
            electricityCabinetOrderService.update(electricityCabinetOrder);

            //修改旧仓门为有电池
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetOrder.getOldElectricityBatterySn());
            ElectricityCabinetBox OldElectricityCabinetBox = new ElectricityCabinetBox();
            if (Objects.nonNull(electricityBattery)) {
                OldElectricityCabinetBox.setElectricityBatteryId(electricityBattery.getId());
            }
            OldElectricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
            OldElectricityCabinetBox.setCellNo(String.valueOf(electricityCabinetOrder.getOldCellNo()));
            OldElectricityCabinetBox.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
            electricityCabinetBoxService.modifyByCellNo(OldElectricityCabinetBox);

            //旧电池改为在仓
            ElectricityBattery oldElectricityBattery = new ElectricityBattery();
            oldElectricityBattery.setId(electricityBattery.getId());
            oldElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
            oldElectricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.update(oldElectricityBattery);

            //用户解绑旧电池
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(electricityCabinetOrder.getUid());
            userInfo.setNowElectricityBatterySn(null);
            userInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfo);

            //加入操作记录表
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .cellNo(electricityCabinetOrder.getOldCellNo())
                    .createTime(System.currentTimeMillis())
                    .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                    .oId(electricityCabinetOrder.getId())
                    .status(ElectricityCabinetOrderOperHistory.STATUS_BATTERY_CHECK_SUCCESS)
                    .type(ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_CHECK)
                    .uid(electricityCabinetOrder.getUid())
                    .build();
            electricityCabinetOrderOperHistoryService.insert(history);

            //新电池改状态
            electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_WAIT_NEW_BATTERY);
            electricityCabinetOrderService.update(electricityCabinetOrder);


            //新电池开门
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", cellNo);
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("serial_number", electricityCabinetOrder.getNewElectricityBatterySn());
            dataMap.put("status", electricityCabinetOrder.getStatus().toString());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + userInfo.getUid() + "_" + electricityCabinetOrder.getOrderId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(HardwareCommand.ELE_COMMAND_ORDER_OPEN_NEW_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        } catch (Exception e) {
            log.error("e" , e);
        } finally {
            redisService.deleteKeys(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_" + cellNo);
        }
    }


    //开新门通知
    public void openNewBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Integer status, String msg) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, status, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR, msg)) {
            return;
        }

        //订单状态
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_NEW_BATTERY_OPEN_DOOR);
        electricityCabinetOrderService.update(electricityCabinetOrder);

        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(status)
                .type(ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR)
                .uid(electricityCabinetOrder.getUid())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }

    //检查新电池通知
    public void checkNewBattery(ElectricityCabinetOrder electricityCabinetOrder, Integer status, String msg) {
        //新电池检测失败
        if (CheckDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, status, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_CHECK, msg)) {
            return;
        }

        //修改仓门为无电池
        ElectricityCabinetBox newElectricityCabinetBox = new ElectricityCabinetBox();
        newElectricityCabinetBox.setCellNo(String.valueOf(electricityCabinetOrder.getNewCellNo()));
        newElectricityCabinetBox.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
        newElectricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
        newElectricityCabinetBox.setElectricityBatteryId(-1L);
        electricityCabinetBoxService.modifyByCellNo(newElectricityCabinetBox);

        //修改订单
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_COMPLETE);
        electricityCabinetOrderService.update(electricityCabinetOrder);

        //用户绑新电池
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(electricityCabinetOrder.getUid());
        userInfo.setNowElectricityBatterySn(electricityCabinetOrder.getNewElectricityBatterySn());
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(userInfo);

        //电池改为在用
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetOrder.getNewElectricityBatterySn());
        ElectricityBattery newElectricityBattery = new ElectricityBattery();
        newElectricityBattery.setId(electricityBattery.getId());
        newElectricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.updateStatus(newElectricityBattery);

        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(ElectricityCabinetOrderOperHistory.STATUS_BATTERY_CHECK_SUCCESS)
                .type(ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_CHECK)
                .uid(electricityCabinetOrder.getUid())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }


    private boolean OpenDoorFailAndSaveOpenDoorFailRecord(ElectricityCabinetOrder electricityCabinetOrder, Integer
            status, Integer type, String msg) {
        if (!status.equals(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_SUCCESS)) {
            Integer cellNo = null;
            if (Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR) || Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_CHECK)) {
                cellNo = electricityCabinetOrder.getNewCellNo();
            } else {
                cellNo = electricityCabinetOrder.getOldCellNo();
            }
            if (Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_CHECK) || Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_CHECK)) {
                status = ElectricityCabinetOrderOperHistory.STATUS_BATTERY_CHECK_ERROR;
            }
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .cellNo(cellNo)
                    .createTime(System.currentTimeMillis())
                    .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                    .oId(electricityCabinetOrder.getId())
                    .status(status)
                    .type(type)
                    .uid(electricityCabinetOrder.getUid())
                    .msg(msg)
                    .build();
            electricityCabinetOrderOperHistoryService.insert(history);
            return true;
        }
        return false;
    }

    private boolean CheckDoorFailAndSaveOpenDoorFailRecord(ElectricityCabinetOrder electricityCabinetOrder, Integer
            status, Integer type, String msg) {
        if (!status.equals(ElectricityCabinetOrderOperHistory.STATUS_BATTERY_CHECK_SUCCESS)) {
            Integer cellNo = null;
            if (Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR) || Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_CHECK)) {
                cellNo = electricityCabinetOrder.getNewCellNo();
            } else {
                cellNo = electricityCabinetOrder.getOldCellNo();
            }
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .cellNo(cellNo)
                    .createTime(System.currentTimeMillis())
                    .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                    .oId(electricityCabinetOrder.getId())
                    .status(status)
                    .type(type)
                    .uid(electricityCabinetOrder.getUid())
                    .msg(msg)
                    .build();
            electricityCabinetOrderOperHistoryService.insert(history);
            return true;
        }
        return false;
    }

    public String findNewUsableCellNo(Integer id, String cellNo) {
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxService.queryElectricityBatteryBox(id, cellNo);
        if (!DataUtil.collectionIsUsable(usableBoxes)) {
            return null;
        }

        List<Integer> boxes = usableBoxes.stream().map(ElectricityCabinetBox::getCellNo).map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());

        //查看有没有初始化过设备的上次操作过的格挡,这里不必关心线程安全，不需要保证原子性
        if (!redisService.hasKey(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id)) {
            redisService.setNx(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, boxes.get(0).toString());
        }

        String lastCellNo = redisService.get(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id);

        boxes = rebuildByCellCircleForDevice(boxes, Integer.parseInt(lastCellNo));

        for (Integer box : boxes) {
            if (redisService.setNx(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + id + "_" + box.toString(), "1", 300 * 1000L, false)) {
                redisService.set(ElectricityCabinetConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, box.toString());
                return box.toString();
            }
        }

        return null;
    }

    public static List<Integer> rebuildByCellCircleForDevice(List<Integer> cellNos, Integer lastCellNo) {

        if (cellNos.get(0) > lastCellNo) {
            return cellNos;
        }

        int index = 0;

        for (int i = 0; i < cellNos.size(); i++) {
            if (cellNos.get(i) > lastCellNo) {
                index = i;
                break;
            }

            if (cellNos.get(i).equals(lastCellNo)) {
                index = i + 1;
                break;
            }
        }

        List<Integer> firstSegmentList = cellNos.subList(0, index);
        List<Integer> twoSegmentList = cellNos.subList(index, cellNos.size());

        ArrayList<Integer> resultList = com.google.common.collect.Lists.newArrayList();
        resultList.addAll(twoSegmentList);
        resultList.addAll(firstSegmentList);

        return resultList;
    }

}
