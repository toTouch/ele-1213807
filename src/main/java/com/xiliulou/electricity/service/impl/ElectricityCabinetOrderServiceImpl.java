package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.OpenDoorQuery;
import com.xiliulou.electricity.query.OrderQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 订单表(TElectricityCabinetOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@Service("electricityCabinetOrderService")
@Slf4j
public class ElectricityCabinetOrderServiceImpl implements ElectricityCabinetOrderService {
    @Resource
    private ElectricityCabinetOrderMapper electricityCabinetOrderMapper;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    @Autowired
    UserInfoService userInfoService;


    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrder queryByIdFromDB(Long id) {
        return this.electricityCabinetOrderMapper.queryById(id);
    }


    /**
     * 新增数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetOrder insert(ElectricityCabinetOrder electricityCabinetOrder) {
        this.electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        return electricityCabinetOrder;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetOrder electricityCabinetOrder) {
        return this.electricityCabinetOrderMapper.update(electricityCabinetOrder);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.electricityCabinetOrderMapper.deleteById(id) > 0;
    }


    /*
      1.判断参数
      2.判断用户是否有电池是否有月卡
      3.生成订单
      4.开旧电池门
      5.旧电池门开回调
      6.旧电池门关回调
      7.旧电池检测回调
      8.检测失败重复开门
      9.检测成功开新电池门
      10.新电池开门回调
      11.新电池关门回调
      */
    @Override
    public R order(OrderQuery orderQuery) {
        if (Objects.isNull(orderQuery.getElectricityCabinetId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinet{}", orderQuery.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        //TODO 换电柜是否在线
        //营业时间
        if (Objects.nonNull(electricityCabinet.getBusinessTime())) {
            String businessTime = electricityCabinet.getBusinessTime();
            if (!Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                Long now = System.currentTimeMillis();
                Long beginTime = Long.valueOf(businessTime.substring(0, businessTime.indexOf("-") - 1));
                Long endTime = Long.valueOf(businessTime.substring(businessTime.indexOf("-"), businessTime.length() - 1));
                if (firstToday + beginTime > now || firstToday + endTime < now) {
                    return R.fail("ELECTRICITY.0017", "换电柜已打烊");
                }
            }
        }
        if (Objects.isNull(orderQuery.getSource())) {
            orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
        }
        //分配开门格挡
        String cellNo = findOldUsableCellNo(electricityCabinet.getId());
        //2.判断用户是否有电池是否有月卡
        //判断是否开通服务
        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
        if (Objects.isNull(userInfo)||Objects.equals(userInfo.getServiceStatus(),UserInfo.IS_SERVICE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo ");
            return R.fail("ELECTRICITY.0021", "未开通服务");
        }
        //判断用户是否开通月卡
        if (Objects.isNull(userInfo.getMemberCardExpireTime()) || Objects.isNull(userInfo.getRemainingNumber())) {
            log.error("ELECTRICITY  ERROR! not found memberCard ");
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }
        Long now = System.currentTimeMillis();
        if (userInfo.getMemberCardExpireTime() < now || userInfo.getRemainingNumber() == 0) {
            log.error("ELECTRICITY  ERROR! not found memberCard ");
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }
        //扣除月卡
        int row = userInfoService.minCount(userInfo.getId());
        if (row < 1) {
            log.error("ELECTRICITY  ERROR! not found memberCard ");
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }
        //3.根据用户查询旧电池
        String oldElectricityBatterySn = userInfo.getNowElectricityBatterySn();
        ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                .orderId(generateOrderId(orderQuery.getElectricityCabinetId(), user.getUid(), cellNo))
                .uid(user.getUid())
                .phone(user.getPhone())
                .electricityCabinetId(orderQuery.getElectricityCabinetId())
                .oldElectricityBatterySn(oldElectricityBatterySn)
                .oldCellNo(Integer.valueOf(cellNo))
                .status(ElectricityCabinetOrder.STATUS_ORDER_PAY)
                .source(orderQuery.getSource()).build();
        electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        //修改仓门为订单中状态
        ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
        electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ORDER_OCCUPY);
        electricityCabinetBoxService.modify(electricityCabinetBox);
        //TODO 4.开旧电池门
        return null;
    }

    @Override
    public R queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = electricityCabinetOrderMapper.queryList(electricityCabinetOrderQuery);
        return R.ok(electricityCabinetOrderVOList);
    }

    @Override
    public R openDoor(OpenDoorQuery openDoorQuery) {
        if (Objects.isNull(openDoorQuery.getOrderId()) || Objects.isNull(openDoorQuery.getOpenType())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, openDoorQuery.getOrderId()));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", openDoorQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        //旧电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_PAY)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }
        //新电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_DEPOSITED)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }
        //判断开门用户是否匹配
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(electricityCabinetOrder.getUid(), user.getUid())) {
            return R.fail("ELECTRICITY.0016", "订单用户不匹配，非法开门");
        }
        //查找换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinet{}", electricityCabinetOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        //TODO 发送命令
        return null;
    }

    @Override
    public Integer homeOneCount(Long first, Long now) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, first, now));
    }

    @Override
    public BigDecimal homeOneSuccess(Long first, Long now) {
        Integer countTotal = homeOneCount(first, now);
        Integer SuccessTotal = electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, first, now).eq(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.STATUS_ORDER_COMPLETE));
        return BigDecimal.valueOf(SuccessTotal).divide(BigDecimal.valueOf(countTotal));
    }

    @Override
    public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay) {
        return electricityCabinetOrderMapper.homeThree(startTimeMilliDay,endTimeMilliDay);
    }


    @Override
    public Integer homeMonth(Long uid, Long first, Long now) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, first, now).eq(ElectricityCabinetOrder::getUid,uid));
    }

    @Override
    public Integer homeTotal(Long uid) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid,uid));
    }

    @Override
    public R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        Integer count=electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime,electricityCabinetOrderQuery.getBeginTime(),electricityCabinetOrderQuery.getEndTime()));
        return R.ok(count);
    }

    public String findOldUsableCellNo(Integer id) {
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxService.queryNoElectricityBatteryBox(id);
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

    public String generateOrderId(Integer id, Long userId, String cellNo) {
        return String.valueOf(System.currentTimeMillis() / 1000) + id +
                cellNo + userId +
                RandomUtil.randomNumbers(2);
    }

    //开旧门通知 TODO
    public void openOldBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, operResult, ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_OPEN_DOOR)) {
            return;
        }
        //TODO 加入定时任务
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_OPEN_DOOR);
        electricityCabinetOrderMapper.update(electricityCabinetOrder);
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

    //弹出旧门通知 TODO
    public void webOpenOldBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, operResult, ElectricityCabinetOrderOperHistory.TYPE_OLD_BATTERY_WEB_OPEN_DOOR)) {
            return;
        }
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_OPEN_DOOR);
        electricityCabinetOrderMapper.update(electricityCabinetOrder);
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

    //开新门通知 TODO
    public void openNewBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        //开门失败
        if (OpenDoorFailAndSaveOpenDoorFailRecord(electricityCabinetOrder, operResult, ElectricityCabinetOrderOperHistory.TYPE_NEW_BATTERY_OPEN_DOOR)) {
            return;
        }
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_NEW_BATTERY_OPEN_DOOR);
        electricityCabinetOrderMapper.update(electricityCabinetOrder);
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

    //关旧门通知 TODO
    public void closeOldBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_DETECT);
        electricityCabinetOrderMapper.update(electricityCabinetOrder);
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

    //关新门通知 TODO
    public void closeNewBatteryDoor(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_COMPLETE);
        electricityCabinetOrderMapper.update(electricityCabinetOrder);
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

    //检查电池通知 TODO
    public void checkBattery(ElectricityCabinetOrder electricityCabinetOrder, Boolean operResult) {
        if (operResult) {
            //修改仓门为有电池
            ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
            electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);
            //TODO 查找旧电池Id 暂时写死
            electricityCabinetBox.setElectricityBatteryId(-1L);
            electricityCabinetBoxService.modify(electricityCabinetBox);
            String cellNo = findNewUsableCellNo(electricityCabinetOrder.getElectricityCabinetId());
            // TODO 根据换电柜id和仓门查出电池 暂时写死
            electricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrder.setStatus(ElectricityCabinetOrder.STATUS_ORDER_OLD_BATTERY_DEPOSITED);
            electricityCabinetOrder.setNewCellNo(Integer.valueOf(cellNo));
            electricityCabinetOrder.setNewElectricityBatterySn("-1");
            electricityCabinetOrderMapper.update(electricityCabinetOrder);
        } else {
            //TODO 弹开老仓门
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

}