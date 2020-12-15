package com.xiliulou.electricity.queue;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.OperateResultDto;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
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
public class ElectricityCabinetOperateQueueHandler {

    ExecutorService executorService = Executors.newFixedThreadPool(20);
    ExecutorService startService = Executors.newFixedThreadPool(1);
    ExecutorService TerminalStartService = Executors.newFixedThreadPool(1);

    private volatile boolean shutdown = false;
    private final LinkedBlockingQueue<OperateResultDto> queue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<HardwareCommandQuery> TerminalQueue = new LinkedBlockingQueue<>();

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

    @EventListener({WebServerInitializedEvent.class})
    public void startHandleElectricityCabinetOperate() {
        initElectricityCabinetOperate();
        initElectricityCabinetTerminalOperate();
    }

    private void initElectricityCabinetOperate() {
        log.info("初始化换电柜操作响应处理器");
        startService.execute(() -> {
            while (!shutdown) {
                OperateResultDto operateResultDto = null;
                try {
                    operateResultDto = queue.take();
                    log.info(" QUEUE get a message ={}", operateResultDto);

                    OperateResultDto finalOperDTO = operateResultDto;
                    executorService.execute(() -> {
                        handleOrderAfterOperated(finalOperDTO);
                    });

                } catch (Exception e) {
                    log.error("ELECTRICITY CABINET OPERATE QUEUE ERROR! ", e);
                }

            }
        });
    }

    private void initElectricityCabinetTerminalOperate() {
        log.info("初始化换电柜終端操作响应处理器");
        TerminalStartService.execute(() -> {

            while (!shutdown) {
                HardwareCommandQuery commandQuery = null;
                try {
                    commandQuery = TerminalQueue.take();
                    log.info(" QUEUE get a message ={}", commandQuery);

                    HardwareCommandQuery finalCommandQuery = commandQuery;
                    executorService.execute(() -> {
                        handleOrderAfterTerminalOperated(finalCommandQuery);
                    });

                } catch (Exception e) {
                    log.error("ELECTRICITY CABINET TERMINAL OPERATE QUEUE ERROR! ", e);
                }

            }
        });

    }

    /**
     * 接收云端命令后的操作
     *
     * @param commandQuery
     */
    private void handleOrderAfterTerminalOperated(HardwareCommandQuery commandQuery) {

        if (commandQuery.getCommand().contains("replace_update_old")) {
            //换电命令第一步 换旧电池
            log.info("replaceOldBattery:{}", commandQuery);
            replaceOldBattery(commandQuery);
        } else if (commandQuery.getCommand().contains("replace_update_new")) {
            //换电命令第二步 ,换新电池
            log.info("replaceNewBattery:{}", commandQuery);
            replaceNewBattery(commandQuery);
        }

    }

    private void replaceNewBattery(HardwareCommandQuery commandQuery) {
        Random random = new Random();
        OperateResultDto operateResultDto = new OperateResultDto();
        operateResultDto.setSessionId(commandQuery.getSessionId());
        operateResultDto.setResult(true);
        int a = 4;
        while (a <= 6) {
            operateResultDto.setOperateFlowNum(a);
            boolean result = random.nextBoolean();
            operateResultDto.setResult(true);
            try {
                Long sleepMilli = (random.nextInt(3) + 3) * 1000L;
                Thread.sleep(sleepMilli);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("replaceNewBattery putQueue:{}", operateResultDto);
            putQueue(operateResultDto);
            a++;
            if (false) {
                break;
            }
        }

    }

    private void replaceOldBattery(HardwareCommandQuery commandQuery) {
        Random random = new Random();
        OperateResultDto operateResultDto = new OperateResultDto();
        operateResultDto.setSessionId(commandQuery.getSessionId());
        int a = 1;
        while (a <= 3) {
            operateResultDto.setOperateFlowNum(a);
            boolean result = random.nextBoolean();
            operateResultDto.setResult(true);
            try {
                Long sleepMilli = (random.nextInt(3) + 3) * 1000L;
                Thread.sleep(sleepMilli);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            putQueue(operateResultDto);
            a++;
            log.info("replaceOldBattery putQueue:{}", operateResultDto);
            if (false) {
                break;
            }
        }


    }


    /**
     * 接收到响应的操作信息
     *
     * @param finalOperDTO
     */
    private void handleOrderAfterOperated(OperateResultDto finalOperDTO) {
        log.info("finalOperDTO is -->{}"+finalOperDTO);
        String sessionId = finalOperDTO.getSessionId();
        Boolean result = finalOperDTO.getResult();
        Long oid = Long.parseLong(sessionId.substring(0, sessionId.indexOf("_")));
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByIdFromDB(oid);
        if (Objects.isNull(electricityCabinetOrder)) {
            return;
        }
        if (Objects.equals(finalOperDTO.getOperateFlowNum(), OperateResultDto.OPERATE_FLOW_NUM_OPEN_OLD)) {
            Integer openType = Integer.parseInt(sessionId.substring(sessionId.indexOf("_") + 1));
            if (Objects.equals(openType, 1)) {
                openOldBatteryDoor(electricityCabinetOrder, result);
            }
            if (Objects.equals(openType, 2)) {
                webOpenOldBatteryDoor(electricityCabinetOrder, result);
            }
        }
        if (Objects.equals(finalOperDTO.getOperateFlowNum(), OperateResultDto.OPERATE_FLOW_CLOSE_OLD)) {
            closeOldBatteryDoor(electricityCabinetOrder, result);
        }
        if (Objects.equals(finalOperDTO.getOperateFlowNum(), OperateResultDto.OPERATE_FLOW_CHECK_BATTERY)) {
            checkOldBattery(electricityCabinetOrder, result);
        }
        if (Objects.equals(finalOperDTO.getOperateFlowNum(), OperateResultDto.OPERATE_FLOW_NUM_OPEN_NEW)) {
            openNewBatteryDoor(electricityCabinetOrder, result);
        }
        if (Objects.equals(finalOperDTO.getOperateFlowNum(), OperateResultDto.OPERATE_FLOW_TAKE_BATTERY)) {
            closeNewBatteryDoor(electricityCabinetOrder, result);
        }
        if (Objects.equals(finalOperDTO.getOperateFlowNum(), OperateResultDto.OPERATE_FLOW_CLOSE_BOX)) {
            checkNewBattery(electricityCabinetOrder, result);
        }

    }


    public void shutdown() {
        shutdown = true;
        executorService.shutdown();
    }

    public void putQueue(OperateResultDto operateResultDto) {
        try {
            queue.put(operateResultDto);
        } catch (InterruptedException e) {
            log.error("LOCKER OPERATE QUEUE ERROR!", e);
        }
    }

    public void putTerminalQueue(HardwareCommandQuery hardwareCommandQuery) {
        try {
            TerminalQueue.put(hardwareCommandQuery);
        } catch (InterruptedException e) {
            log.error(" OPERATE TERMINAL QUEUE ERROR!", e);
        }
    }

    //开旧门通知
    public void openOldBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, operResult, ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_OPEN_DOOR)) {
            return;
        }
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_OPEN_DOOR);
        electricityCabinetOrderService.update(electricityCabinetOrder);
        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_SUCCESS)
                .type(ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_OPEN_DOOR)
                .uid(electricityCabinetOrder.getElectricityCabinetId())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }

    //弹出旧门通知
    public void webOpenOldBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, operResult, ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_WEB_OPEN_DOOR)) {
            return;
        }
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_OPEN_DOOR);
        electricityCabinetOrderService.update(electricityCabinetOrder);
        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_SUCCESS)
                .type(ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_WEB_OPEN_DOOR)
                .uid(electricityCabinetOrder.getElectricityCabinetId())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }

    //关旧门通知
    public void closeOldBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_DETECT);
        electricityCabinetOrderService.update(electricityCabinetOrder);
        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_SUCCESS)
                .type(ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_CLOSE_DOOR)
                .uid(electricityCabinetOrder.getElectricityCabinetId())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }


    //检查旧电池通知
    public void checkOldBattery(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        if (operResult) {
            //修改仓门为有电池
            //查找电池Id
            ElectricityCabinetBox electricityCabinetOldBox = new ElectricityCabinetBox();
            ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(electricityCabinetOrder.getOldElectricityBatterySn());
            if (Objects.nonNull(oldElectricityBattery)) {
                electricityCabinetOldBox.setElectricityBatteryId(oldElectricityBattery.getId());
            }
            electricityCabinetOldBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
            electricityCabinetOldBox.setCellNo(String.valueOf(electricityCabinetOrder.getOldCellNo()));
            electricityCabinetOldBox.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
            electricityCabinetBoxService.modifyByCellNo(electricityCabinetOldBox);
            //分配新仓门
            String cellNo = findNewUsableCellNo(electricityCabinetOrder.getElectricityCabinetId());
            try {
                //修改新仓门状态
                ElectricityCabinetBox electricityCabinetNewBox = new ElectricityCabinetBox();
                electricityCabinetNewBox.setCellNo(cellNo);
                electricityCabinetNewBox.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
                electricityCabinetNewBox.setStatus(ElectricityCabinetBox.STATUS_ORDER_OCCUPY);
                electricityCabinetBoxService.modifyByCellNo(electricityCabinetNewBox);
                //根据换电柜id和仓门查出电池 暂时写死
                ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetNewBox);
                if (Objects.isNull(electricityCabinetBox)) {
                    return;
                }
                ElectricityBattery newElectricityBattery = electricityBatteryService.queryById(electricityCabinetBox.getElectricityBatteryId());
                if (Objects.nonNull(newElectricityBattery)) {
                    electricityCabinetOrder.setNewElectricityBatterySn(newElectricityBattery.getSerialNumber());
                }
                electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
                electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_DEPOSITED);
                electricityCabinetOrder.setNewCellNo(Integer.valueOf(cellNo));
                electricityCabinetOrderService.update(electricityCabinetOrder);
                //新电池开门
                HashMap<String, Object> dataMap = Maps.newHashMap();
                dataMap.put("cell_no", electricityCabinetOrder.getOldCellNo());
                HardwareCommandQuery comm = HardwareCommandQuery.builder()
                        .sessionId(electricityCabinetOrder.getId() + "_" + 1)
                        .data(dataMap)
                        .productKey("11111")
                        .deviceName("222222")
                        .command("replace_update_new")
                        .build();
                putTerminalQueue(comm);
            } catch (Exception e) {
                log.error("e" + e);
            } finally {
                redisService.deleteKeys(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_" + cellNo);
            }
        } else {
            //弹出开门
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", electricityCabinetOrder.getOldCellNo());
            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(electricityCabinetOrder.getId() + "_" + 2)
                    .data(dataMap)
                    .productKey("11111")
                    .deviceName("222222")
                    .command("replace_update_old")
                    .build();
            putTerminalQueue(comm);
        }
    }

    //开新门通知
    public void openNewBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, operResult, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR)) {
            return;
        }
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_NEW_BATTERY_OPEN_DOOR);
        electricityCabinetOrderService.update(electricityCabinetOrder);
        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_SUCCESS)
                .type(ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR)
                .uid(electricityCabinetOrder.getElectricityCabinetId())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }

    //关新门通知
    public void closeNewBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_NEW_BATTERY_CLOSE_DOOR);
        electricityCabinetOrderService.update(electricityCabinetOrder);
        //加入操作记录表
        ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                .cellNo(electricityCabinetOrder.getOldCellNo())
                .createTime(System.currentTimeMillis())
                .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                .oId(electricityCabinetOrder.getId())
                .status(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_SUCCESS)
                .type(ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_CLOSE_DOOR)
                .uid(electricityCabinetOrder.getElectricityCabinetId())
                .build();
        electricityCabinetOrderOperHistoryService.insert(history);
    }

    //检查新电池通知
    public void checkNewBattery(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        if (operResult) {
            //修改仓门为无电池
            ElectricityCabinetBox electricityCabinetNewBox = new ElectricityCabinetBox();
            electricityCabinetNewBox.setCellNo(String.valueOf(electricityCabinetOrder.getNewCellNo()));
            electricityCabinetNewBox.setElectricityCabinetId(electricityCabinetOrder.getElectricityCabinetId());
            electricityCabinetNewBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
            electricityCabinetNewBox.setElectricityBatteryId(-1L);
            electricityCabinetBoxService.modifyByCellNo(electricityCabinetNewBox);
            //修改订单
            electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_COMPLETE);
            electricityCabinetOrderService.update(electricityCabinetOrder);
        }
    }

    private boolean OpenDoorFailAndSaveOpenDoorFailRecord(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult, Integer type) {
        if (!operResult) {
            Integer cellNo = null;
            if (Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR) || Objects.equals(type, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_CLOSE_DOOR)) {
                cellNo = electricityCabinetOrder.getNewCellNo();
            } else {
                cellNo = electricityCabinetOrder.getOldCellNo();
            }
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .cellNo(cellNo)
                    .createTime(System.currentTimeMillis())
                    .electricityCabinetId(electricityCabinetOrder.getElectricityCabinetId())
                    .oId(electricityCabinetOrder.getId())
                    .status(ElectricityCabinetOrderOperHistory.STATUS_OPEN_DOOR_FAIL)
                    .type(type)
                    .uid(electricityCabinetOrder.getElectricityCabinetId())
                    .build();
            electricityCabinetOrderOperHistoryService.insert(history);
            return true;
        }
        return false;
    }

    public String findNewUsableCellNo(Integer id) {
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxService.queryElectricityBatteryBox(id);
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
