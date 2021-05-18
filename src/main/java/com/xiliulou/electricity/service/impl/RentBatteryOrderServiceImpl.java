package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.RentBatteryOrderMapper;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.query.RentBatteryQuery;
import com.xiliulou.electricity.query.RentOpenDoorQuery;
import com.xiliulou.electricity.query.ReturnBatteryQuery;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderExcelVO;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.RentBatteryOrderExcelVO;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租电池记录(TRentBatteryOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
@Service("rentBatteryOrderService")
@Slf4j
public class RentBatteryOrderServiceImpl implements RentBatteryOrderService {
    @Resource
    RentBatteryOrderMapper rentBatteryOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;


    /**
     * 新增数据
     *
     * @param rentBatteryOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentBatteryOrder insert(RentBatteryOrder rentBatteryOrder) {
        this.rentBatteryOrderMapper.insert(rentBatteryOrder);
        return rentBatteryOrder;
    }


    @Override
    public R queryList(RentBatteryOrderQuery rentBatteryOrderQuery) {
        Page page = PageUtil.getPage(rentBatteryOrderQuery.getOffset(), rentBatteryOrderQuery.getSize());
        return R.ok(rentBatteryOrderMapper.queryList(page, rentBatteryOrderQuery));
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R rentBattery(RentBatteryQuery rentBatteryQuery) {
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //限频
        Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_RENT_BATTERY_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId{}", rentBatteryQuery.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜是否出现异常被锁住
        String isLock = redisService.get(ElectricityCabinetConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
        if (StringUtils.isNotEmpty(isLock)) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is lock ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
        }

        //营业时间
        Boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //判断用户
        UserInfo userInfo = userInfoService.queryByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unusable! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("ELECTRICITY  ERROR! not auth! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
        //未缴纳押金
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_AUTH)) {
            log.error("ELECTRICITY  ERROR! not pay deposit! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
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

        //已绑定电池
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY)) {
            log.error("ELECTRICITY  ERROR!  rent battery! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0045", "已绑定电池");
        }

        //是否存在未完成的租电池订单
        Integer count = queryByUidAndType(uid, RentBatteryOrder.TYPE_USER_RENT);
        if (count > 0) {
            return R.fail("ELECTRICITY.0013", "存在未完成订单，不能下单");
        }

        //是否有正在退款中的退款
        Integer refundCount=eleRefundOrderService.queryCountByOrderId(userInfo.getOrderId());
        if(refundCount>0){
            return R.fail("ELECTRICITY.0051","押金正在退款中，请勿租电池");
        }


        //分配电池 --只分配满电电池
        String cellNo = findUsableBatteryCellNo(rentBatteryQuery.getElectricityCabinetId(), null);
        try {
            if (Objects.isNull(cellNo)) {
                return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
            }

            //根据换电柜id和仓门查出电池
            ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(rentBatteryQuery.getElectricityCabinetId(), cellNo);
            ElectricityBattery electricityBattery = electricityBatteryService.queryById(electricityCabinetBox.getElectricityBatteryId());
            if (Objects.isNull(electricityBattery)) {
                return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
            }

            String orderId = generateOrderId(uid, cellNo);

            //生成订单
            RentBatteryOrder rentBatteryOrder = RentBatteryOrder.builder()
                    .orderId(orderId)
                    .electricityBatterySn(electricityBattery.getSn())
                    .uid(uid)
                    .phone(userInfo.getPhone())
                    .name(userInfo.getName())
                    .batteryDeposit(userInfo.getBatteryDeposit())
                    .type(RentBatteryOrder.TYPE_USER_RENT)
                    .status(RentBatteryOrder.STATUS_INIT)
                    .electricityCabinetId(electricityCabinet.getId())
                    .cellNo(Integer.valueOf(cellNo))
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            rentBatteryOrderMapper.insert(rentBatteryOrder);

            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", cellNo);
            dataMap.put("orderId", orderId);
            dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(HardwareCommand.ELE_COMMAND_RENT_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            return R.ok(orderId);
        } catch (Exception e) {
            log.error("order is error" + e);
            return R.fail("ELECTRICITY.0025", "下单失败");
        } finally {
            redisService.delete(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + rentBatteryQuery.getElectricityCabinetId() + "_" + cellNo);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R returnBattery(ReturnBatteryQuery returnBatteryQuery) {
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //限频
        Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_RETURN_BATTERY_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(returnBatteryQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId{}", returnBatteryQuery.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜是否出现异常被锁住
        String isLock = redisService.get(ElectricityCabinetConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
        if (StringUtils.isNotEmpty(isLock)) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is lock ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
        }

        //营业时间
        Boolean result = this.isBusiness(electricityCabinet);
        if (result) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //判断是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unusable! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //用户状态异常
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY)&&Objects.isNull(userInfo.getNowElectricityBatterySn())) {
            log.error("ELECTRICITY  ERROR! userInfo is error!userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0052", "用户状态异常，请联系管理员");
        }

        if (!Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY) || Objects.isNull(userInfo.getNowElectricityBatterySn())) {
            log.error("ELECTRICITY  ERROR! not  rent battery!  userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        //是否存在未完成的租电池订单
        Integer count = rentBatteryOrderMapper.selectCount(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getUid, uid).eq(RentBatteryOrder::getType, RentBatteryOrder.TYPE_USER_RETURN)
                .in(RentBatteryOrder::getStatus, RentBatteryOrder.STATUS_INIT, RentBatteryOrder.STATUS_RENT_BATTERY_OPEN_DOOR));
        if (count > 0) {
            return R.fail("ELECTRICITY.0013", "存在未完成订单，不能下单");
        }

        //分配开门格挡
        String cellNo = electricityCabinetOrderService.findUsableCellNo(electricityCabinet.getId());
        try {
            if (Objects.isNull(cellNo)) {
                return R.fail("ELECTRICITY.0008", "换电柜暂无空仓");
            }

            String orderId = generateOrderId(uid, cellNo);

            //生成订单
            RentBatteryOrder rentBatteryOrder = RentBatteryOrder.builder()
                    .orderId(orderId)
                    .electricityBatterySn(userInfo.getNowElectricityBatterySn())
                    .uid(uid)
                    .phone(userInfo.getPhone())
                    .name(userInfo.getName())
                    .batteryDeposit(userInfo.getBatteryDeposit())
                    .type(RentBatteryOrder.TYPE_USER_RETURN)
                    .status(RentBatteryOrder.STATUS_INIT)
                    .electricityCabinetId(electricityCabinet.getId())
                    .cellNo(Integer.valueOf(cellNo))
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            rentBatteryOrderMapper.insert(rentBatteryOrder);

            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", cellNo);
            dataMap.put("orderId", orderId);
            dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(HardwareCommand.ELE_COMMAND_RETURN_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            return R.ok(orderId);

        } finally {
            redisService.delete(ElectricityCabinetConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + returnBatteryQuery.getElectricityCabinetId() + "_" + cellNo);
        }
    }

    @Override
    public void update(RentBatteryOrder rentBatteryOrder) {
        rentBatteryOrderMapper.updateById(rentBatteryOrder);
    }

    @Override
    public R openDoor(RentOpenDoorQuery rentOpenDoorQuery) {
        if (Objects.isNull(rentOpenDoorQuery.getOrderId()) || Objects.isNull(rentOpenDoorQuery.getOpenType())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, rentOpenDoorQuery.getOrderId()).eq(RentBatteryOrder::getStatus, RentBatteryOrder.STATUS_INIT));
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", rentOpenDoorQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //租电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RENT_OPEN_TYPE)
                && !Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //还电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RETURN_OPEN_TYPE)
                && !Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //判断开门用户是否匹配
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(rentBatteryOrder.getUid(), user.getUid())) {
            return R.fail("ELECTRICITY.0016", "订单用户不匹配，非法开门");
        }

        //查找换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId{}", rentBatteryOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //租电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RENT_OPEN_TYPE)) {
            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", rentBatteryOrder.getCellNo());
            dataMap.put("orderId", rentBatteryOrder.getOrderId());
            dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(HardwareCommand.ELE_COMMAND_RENT_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        }

        //还电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RETURN_OPEN_TYPE)) {
            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", rentBatteryOrder.getCellNo());
            dataMap.put("orderId", rentBatteryOrder.getOrderId());
            dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(ElectricityCabinetConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(HardwareCommand.ELE_COMMAND_RETURN_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);

        }
        redisService.delete(ElectricityCabinetConstant.ELE_ORDER_OPERATOR_CACHE_KEY + rentBatteryOrder.getOrderId());
        return R.ok(rentBatteryOrder.getOrderId());
    }

    @Override
    public R queryStatus(String orderId) {
        Map<String, String> map = new HashMap<>();
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId));
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        Integer queryStatus = 0;
        String s = redisService.get(ElectricityCabinetConstant.ELE_ORDER_OPERATOR_CACHE_KEY + orderId);
        if (StrUtil.isNotEmpty(s)) {
            queryStatus = 1;
        }
        map.put("status", rentBatteryOrder.getStatus().toString());
        map.put("queryStatus", queryStatus.toString());
        return R.ok(map);
    }

    @Override
    public RentBatteryOrder queryByOrderId(String orderId) {
        return rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId));
    }

    @Override
    public R endOrder(String orderId) {
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId)
                .in(RentBatteryOrder::getStatus, RentBatteryOrder.STATUS_INIT, RentBatteryOrder.STATUS_RENT_BATTERY_OPEN_DOOR));
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        RentBatteryOrder rentBatteryOrderUpdate = new RentBatteryOrder();
        rentBatteryOrderUpdate.setId(rentBatteryOrder.getId());
        rentBatteryOrderUpdate.setStatus(RentBatteryOrder.STATUS_ORDER_EXCEPTION_CANCEL);
        rentBatteryOrderUpdate.setUpdateTime(System.currentTimeMillis());
        rentBatteryOrderMapper.updateById(rentBatteryOrderUpdate);
        return R.ok();
    }

    @Override
    public Integer queryByUidAndType(Long uid, Integer type) {
        return rentBatteryOrderMapper.selectCount(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getUid, uid).eq(RentBatteryOrder::getType,type)
                .in(RentBatteryOrder::getStatus, RentBatteryOrder.STATUS_INIT, RentBatteryOrder.STATUS_RENT_BATTERY_OPEN_DOOR));
    }

    @Override
    public void exportExcel(RentBatteryOrderQuery rentBatteryOrderQuery, HttpServletResponse response) {
        Page page = PageUtil.getPage(0L,2000L);
        rentBatteryOrderMapper.queryList(page, rentBatteryOrderQuery);
        if (ObjectUtil.isEmpty(page.getRecords())) {
            return ;
        }


        List<RentBatteryOrder> rentBatteryOrderList = page.getRecords();
        if (!DataUtil.collectionIsUsable(rentBatteryOrderList)) {
            return;
        }


        List<RentBatteryOrderExcelVO> rentBatteryOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (RentBatteryOrder rentBatteryOrder : rentBatteryOrderList) {
            index++;
            RentBatteryOrderExcelVO excelVo = new RentBatteryOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(rentBatteryOrder.getOrderId());
            excelVo.setPhone(rentBatteryOrder.getPhone());
            excelVo.setName(rentBatteryOrder.getName());
            excelVo.setCellNo(rentBatteryOrder.getCellNo());
            excelVo.setElectricityBatterySn(rentBatteryOrder.getElectricityBatterySn());
            excelVo.setBatteryDeposit(rentBatteryOrder.getBatteryDeposit());


            if (Objects.nonNull(rentBatteryOrder.getCreateTime())) {
                excelVo.setCreatTime(simpleDateFormat.format(new Date(rentBatteryOrder.getCreateTime())));
            }


            if (Objects.isNull(rentBatteryOrder.getType())) {
                excelVo.setType("");
            }
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                excelVo.setType("租电池");
            }
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                excelVo.setType("还电池");
            }
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_WEB_BIND)) {
                excelVo.setType("后台绑电池");
            }
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_WEB_UNBIND)) {
                excelVo.setType("后台解绑电池");
            }



            if (Objects.isNull(rentBatteryOrder.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.STATUS_INIT)) {
                excelVo.setStatus("初始化");
            }
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.STATUS_RENT_BATTERY_OPEN_DOOR)) {
                excelVo.setStatus("开门");
            }
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.STATUS_RENT_BATTERY_DEPOSITED)) {
                excelVo.setStatus("订单完成");
            }
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.STATUS_ORDER_EXCEPTION_CANCEL)) {
                excelVo.setStatus("订单异常结束");
            }
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.STATUS_ORDER_CANCEL)) {
                excelVo.setStatus("订单取消");
            }

            rentBatteryOrderExcelVOS.add(excelVo);


            String fileName = "换电订单报表.xlsx";
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                // 告诉浏览器用什么软件可以打开此文件
                response.setHeader("content-Type", "application/vnd.ms-excel");
                // 下载文件的默认名称
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
                EasyExcel.write(outputStream, ElectricityCabinetOrderExcelVO.class).sheet("sheet").doWrite(rentBatteryOrderExcelVOS);
                return;
            } catch (IOException e) {
                log.error("导出报表失败！", e);
            }
        }
    }

    //分配满仓
    public String findUsableBatteryCellNo(Integer id, String cellNo) {
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

    public boolean isBusiness(ElectricityCabinet electricityCabinet) {
        //营业时间
        if (Objects.nonNull(electricityCabinet.getBusinessTime())) {
            String businessTime = electricityCabinet.getBusinessTime();
            if (!Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    long now = System.currentTimeMillis();
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Long getTime(Long time) {
        Date date1 = new Date(time);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(date1);
        Date date2 = null;
        try {
            date2 = dateFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Long ts = date2.getTime();
        return time - ts;
    }

    public String generateOrderId(Long uid, String cellNo) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid + cellNo +
                RandomUtil.randomNumbers(4);
    }
}
