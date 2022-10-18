package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.RentBatteryOrderMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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
import java.util.*;
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
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    StoreService storeService;
    @Autowired
    FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;
    @Autowired
    ElectricityConfigService electricityConfigService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;


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
        List<RentBatteryOrderVO> rentBatteryOrderVOList = rentBatteryOrderMapper.queryList(rentBatteryOrderQuery);
        if (ObjectUtil.isEmpty(rentBatteryOrderVOList)) {
            return R.ok(new ArrayList<>());
        }
        if (ObjectUtil.isNotEmpty(rentBatteryOrderVOList)) {
            rentBatteryOrderVOList.parallelStream().forEach(e -> {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    e.setElectricityCabinetName(electricityCabinet.getName());
                }
            });
        }

        return R.ok(rentBatteryOrderVOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R rentBattery(RentBatteryQuery rentBatteryQuery) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("RENTBATTERY ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder oldRentBatteryOrder = queryByUidAndType(user.getUid());
        if (Objects.nonNull(oldRentBatteryOrder)) {
            if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                log.error("RENTBATTERY ERROR! exits unfinished rent battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                log.error("RENTBATTERY ERROR! exits unfinished return battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            log.error("RENTBATTERY ERROR! exits unfinished exchange battery order,uid={}", user.getUid());
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(rentBatteryQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("RENTBATTERY ERROR! not found electricityCabinet,electricityCabinetId={}", rentBatteryQuery.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("RENTBATTERY ERROR! electricityCabinet is offline,electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            log.error("RENTBATTERY ERROR! electricityCabinet is not business,electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //下单锁住柜机
        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 3 * 60 * 1000L, false);
        if (!result) {
            log.error("RENTBATTERY ERROR! someone is using electricityCabinet,electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.00105", "该柜机有人正在下单，请稍等片刻");
        }

        Boolean eleLockFlag = Boolean.TRUE;

        try {
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found store,electricityCabinetId={}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }

            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found store,storeId={}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found franchisee,storeId={}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            //判断用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found user,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }

            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! user is unUsable,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }

            //未实名认证
            if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not auth,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0041", "未实名认证");
            }


            FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
            if (Objects.isNull(franchiseeUserInfo)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found user,userId={}", user.getUid());
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }

            //判断该换电柜加盟商和用户加盟商是否一致
            if (!Objects.equals(store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR!FranchiseeId is not equal,uid={}, FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId());
                return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
            }

            //判断是否缴纳押金
            if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                    || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not pay deposit,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0042", "未缴纳押金");
            }

            //用户是否开通月卡
            if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
                    || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found memberCard,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0022", "未开通月卡");
            }

            //已绑定电池
            if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! user rent battery,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0045", "已绑定电池");
            }

            //是否有正在退款中的退款
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(franchiseeUserInfo.getOrderId());
            if (refundCount > 0) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! deposit is being refunded,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0051", "押金正在退款中，请勿租电池");
            }

            Long now = System.currentTimeMillis();
            //月卡是否过期
            if (!Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
                ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());
                if (Objects.isNull(electricityMemberCard)) {
                    eleLockFlag = Boolean.FALSE;
                    log.error("RENTBATTERY ERROR! memberCard  is not exit,uid={}", user.getUid());
                    return R.fail("ELECTRICITY.00121", "套餐不存在");
                }

                if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && franchiseeUserInfo.getMemberCardExpireTime() < now) {
                    eleLockFlag = Boolean.FALSE;
                    log.error("RENTBATTERY ERROR! memberCard  is Expire,uid={}", user.getUid());
                    return R.fail("ELECTRICITY.0023", "月卡已过期");
                }

                if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                    if (franchiseeUserInfo.getRemainingNumber() < 0) {
                        //用户需购买相同套餐，补齐所欠换电次数
                        eleLockFlag = Boolean.FALSE;
                        log.error("RENTBATTERY ERROR! memberCard remainingNumber insufficient,uid={}", user.getUid());
                        return R.fail("ELECTRICITY.00117", "套餐剩余次数为负", franchiseeUserInfo.getCardId());
                    }

                    if (franchiseeUserInfo.getMemberCardExpireTime() < now || franchiseeUserInfo.getRemainingNumber() == 0) {
                        eleLockFlag = Boolean.FALSE;
                        log.error("RENTBATTERY ERROR! memberCard  is Expire,uid={}", user.getUid());
                        return R.fail("ELECTRICITY.0023", "月卡已过期");
                    }
                }
            } else {
                if (franchiseeUserInfo.getMemberCardExpireTime() < now || franchiseeUserInfo.getRemainingNumber() == 0) {
                    eleLockFlag = Boolean.FALSE;
                    log.error("RENTBATTERY ERROR! memberCard  is Expire,uid={}", user.getUid());
                    return R.fail("ELECTRICITY.0023", "月卡已过期");
                }
            }

            //分配电池 --只分配满电电池
            Triple<Boolean, String, Object> tripleResult;
            if (Objects.equals(franchiseeUserInfo.getModelType(), FranchiseeUserInfo.NEW_MODEL_TYPE)) {
                tripleResult = findUsableBatteryCellNo(electricityCabinet, null, franchiseeUserInfo.getBatteryType(), franchiseeUserInfo.getFranchiseeId(), null);
            } else {
                tripleResult = findUsableBatteryCellNo(electricityCabinet, null, null, franchiseeUserInfo.getFranchiseeId(), null);
            }

            if (Objects.isNull(tripleResult) || !tripleResult.getLeft()) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found full charge battery,electricityCabinetId={}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
            }


            String cellNo = tripleResult.getMiddle();

            if (Objects.isNull(cellNo)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR!cellNo is empty,electricityCabinetId={}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
            }

            //根据换电柜id和仓门查出电池
            ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(rentBatteryQuery.getElectricityCabinetId(), cellNo);
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetBox.getSn());
            if (Objects.isNull(electricityBattery)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RENTBATTERY ERROR! not found battery,batteryName={}", electricityCabinetBox.getSn());
                return R.fail("ELECTRICITY.0026", "换电柜暂无满电电池");
            }

            String orderId = generateOrderId(user.getUid(), cellNo);

            //生成订单
            RentBatteryOrder rentBatteryOrder = RentBatteryOrder.builder()
                    .orderId(orderId)
                    .electricityBatterySn(electricityBattery.getSn())
                    .uid(user.getUid())
                    .phone(userInfo.getPhone())
                    .name(userInfo.getName())
                    .batteryDeposit(franchiseeUserInfo.getBatteryDeposit())
                    .type(RentBatteryOrder.TYPE_USER_RENT)
                    .orderSeq(RentBatteryOrder.STATUS_INIT)
                    .status(RentBatteryOrder.INIT)
                    .electricityCabinetId(electricityCabinet.getId())
                    .cellNo(Integer.valueOf(cellNo))
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(TenantContextHolder.getTenantId()).build();
            rentBatteryOrderMapper.insert(rentBatteryOrder);

            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", cellNo);
            dataMap.put("orderId", orderId);
            dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_RENT_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            return R.ok(orderId);
        } finally {
            if (!eleLockFlag) {
                redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R returnBattery(Integer electricityCabinetId) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("RETURNBATTERY ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder oldRentBatteryOrder = queryByUidAndType(user.getUid());
        if (Objects.nonNull(oldRentBatteryOrder)) {
            if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                log.error("RENTBATTERY ERROR! exits unfinished rent battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                log.error("RENTBATTERY ERROR! exits unfinished return battery order,uid={}", user.getUid());
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            log.error("RENTBATTERY ERROR! exits unfinished exchange battery order,uid={}", user.getUid());
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetId);
        if (Objects.isNull(electricityCabinet)) {
            log.error("RETURNBATTERY ERROR! not found electricityCabinet,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("RETURNBATTERY ERROR! electricityCabinet is offline,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            log.error("RENTBATTERY ERROR! electricityCabinet is not business,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //下单锁住柜机
        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 3 * 60 * 1000L, false);
        if (!result) {
            log.error("RENTBATTERY ERROR! someone is using electricityCabinet,electricityCabinetId={}", electricityCabinetId);
            return R.fail("ELECTRICITY.00105", "该柜机有人正在下单，请稍等片刻");
        }

        Boolean eleLockFlag = Boolean.TRUE;

        try {
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! not found store,electricityCabinetId={}", electricityCabinetId);
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }

            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! not found store,storeId={}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! not found franchisee,storeId={}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            //用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! not found user,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }

            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! user is unUsable,uid={}", user.getUid());
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }


            FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
            if (Objects.isNull(franchiseeUserInfo)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! not found user,userId={}", user.getUid());
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }

            //判断该换电柜加盟商和用户加盟商是否一致
            if (!Objects.equals(store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId());
                return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
            }

            //判断是否缴纳押金
            if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                    || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! not pay deposit! uid={}", user.getUid());
                return R.fail("ELECTRICITY.0042", "未缴纳押金");
            }

            //未绑定电池
            if (!Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! not rent battery,uid={} ", user.getUid());
                return R.fail("ELECTRICITY.0033", "用户未绑定电池");
            }

            //分配开门格挡
            Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNo(electricityCabinet.getId());

            if (Objects.isNull(usableEmptyCellNo.getRight())) {
                eleLockFlag = Boolean.FALSE;
                log.error("RETURNBATTERY ERROR! electricityCabinet not empty cell,electricityCabinetId={} ", electricityCabinetId);
                return R.fail("ELECTRICITY.0008", "换电柜暂无空仓");
            }


        String cellNo = usableEmptyCellNo.getRight().toString();

            String orderId = generateOrderId(user.getUid(), cellNo);

            //生成订单
            RentBatteryOrder rentBatteryOrder = RentBatteryOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .phone(userInfo.getPhone())
                    .name(userInfo.getName())
                    .batteryDeposit(franchiseeUserInfo.getBatteryDeposit())
                    .type(RentBatteryOrder.TYPE_USER_RETURN)
                    .orderSeq(RentBatteryOrder.STATUS_INIT)
                    .status(RentBatteryOrder.INIT)
                    .electricityCabinetId(electricityCabinet.getId())
                    .cellNo(Integer.valueOf(cellNo))
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(TenantContextHolder.getTenantId()).build();
            rentBatteryOrderMapper.insert(rentBatteryOrder);

            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", cellNo);
            dataMap.put("orderId", orderId);


            //是否开启电池检测
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
            if (Objects.nonNull(electricityConfig)) {
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {

                    ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());

                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", Objects.nonNull(electricityBattery)?electricityBattery.getSn():"");
                } else {
                    dataMap.put("is_checkBatterySn", false);
                }
            }

            if (Objects.equals(franchiseeUserInfo.getModelType(), FranchiseeUserInfo.OLD_MODEL_TYPE)) {
                dataMap.put("model_type", false);
            } else {
                dataMap.put("model_type", true);
                dataMap.put("multiBatteryModelName", franchiseeUserInfo.getBatteryType());
            }

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_RETURN_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            return R.ok(orderId);
        } finally {
            if (!eleLockFlag) {
                redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            }
//            redisService.delete(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetId + "_" + cellNo);
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
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, rentOpenDoorQuery.getOrderId()));
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", rentOpenDoorQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //租电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RENT_OPEN_TYPE)) {
            if (!Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)
                    || (!Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.INIT)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_INIT_CHECK)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_BATTERY_NOT_EXISTS)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_OPEN_FAIL))) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }

        //还电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RETURN_OPEN_TYPE)) {
            if (!Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)
                    || (!Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.INIT)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_INIT_CHECK)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_BATTERY_EXISTS)
                    && !Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_OPEN_FAIL))) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }

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

        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.isNull(electricityBattery)) {
            log.error("RENT BATTERY ERROR! not found user bind battery,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }

        //租电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RENT_OPEN_TYPE)) {
            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", rentBatteryOrder.getCellNo());
            dataMap.put("orderId", rentBatteryOrder.getOrderId());
            dataMap.put("serialNumber", rentBatteryOrder.getElectricityBatterySn());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_RENT_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        }

        //还电池开门
        if (Objects.equals(rentOpenDoorQuery.getOpenType(), RentOpenDoorQuery.RETURN_OPEN_TYPE)) {
            //发送开门命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cellNo", rentBatteryOrder.getCellNo());
            dataMap.put("orderId", rentBatteryOrder.getOrderId());

            //是否开启电池检测
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(user.getTenantId());
            if (Objects.nonNull(electricityConfig)) {
                UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
                FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", electricityBattery.getSn());
                } else {
                    dataMap.put("is_checkBatterySn", false);
                }
            }

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_RETURN_OPEN_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);

        }
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + rentBatteryOrder.getOrderId());
        return R.ok(rentBatteryOrder.getOrderId());
    }

    @Override
    public RentBatteryOrder queryByOrderId(String orderId) {
        return rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId));
    }

    @Override
    public R endOrder(String orderId) {
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId));
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("endOrder  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //租电池
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)
                    || Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.ORDER_CANCEL)
                    || Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.ORDER_EXCEPTION_CANCEL)) {
                log.error("endOrder  ERROR! not found order,orderId{} ", orderId);
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }

        //还电池
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)
                    || Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.ORDER_CANCEL)
                    || Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.ORDER_EXCEPTION_CANCEL)) {
                log.error("endOrder  ERROR! not found order,orderId{} ", orderId);
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }

        RentBatteryOrder rentBatteryOrderUpdate = new RentBatteryOrder();
        rentBatteryOrderUpdate.setId(rentBatteryOrder.getId());
        rentBatteryOrderUpdate.setStatus(RentBatteryOrder.ORDER_EXCEPTION_CANCEL);
        rentBatteryOrderUpdate.setOrderSeq(RentBatteryOrder.STATUS_ORDER_EXCEPTION_CANCEL);
        rentBatteryOrderUpdate.setUpdateTime(System.currentTimeMillis());
        rentBatteryOrderMapper.updateById(rentBatteryOrderUpdate);

        //删除开门失败缓存
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);

        //结束订单锁
        redisService.delete(CacheConstant.ORDER_ELE_ID + rentBatteryOrder.getElectricityCabinetId());
        return R.ok();
    }

    @Override
    public RentBatteryOrder queryByUidAndType(Long uid) {
        return rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getUid, uid)
                .notIn(RentBatteryOrder::getStatus, RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS, RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS, RentBatteryOrder.ORDER_EXCEPTION_CANCEL, RentBatteryOrder.ORDER_CANCEL)
                .orderByDesc(RentBatteryOrder::getCreateTime).last("limit 0,1"));
    }

    @Override
    public void exportExcel(RentBatteryOrderQuery rentBatteryOrderQuery, HttpServletResponse response) {
        rentBatteryOrderQuery.setOffset(0L);
        rentBatteryOrderQuery.setSize(2000L);
        List<RentBatteryOrderVO> rentBatteryOrderVOList = rentBatteryOrderMapper.queryList(rentBatteryOrderQuery);
        if (ObjectUtil.isEmpty(rentBatteryOrderVOList)) {
            throw new CustomBusinessException("查不到订单");
        }

        List<RentBatteryOrderExcelVO> rentBatteryOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (RentBatteryOrderVO rentBatteryOrderVO : rentBatteryOrderVOList) {
            index++;
            RentBatteryOrderExcelVO excelVo = new RentBatteryOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(rentBatteryOrderVO.getOrderId());
            excelVo.setPhone(rentBatteryOrderVO.getPhone());
            excelVo.setName(rentBatteryOrderVO.getName());
            excelVo.setCellNo(rentBatteryOrderVO.getCellNo());
            excelVo.setElectricityBatterySn(rentBatteryOrderVO.getElectricityBatterySn());
            excelVo.setBatteryDeposit(rentBatteryOrderVO.getBatteryDeposit());

            if (Objects.nonNull(rentBatteryOrderVO.getCreateTime())) {
                excelVo.setCreatTime(simpleDateFormat.format(new Date(rentBatteryOrderVO.getCreateTime())));
            }

            if (Objects.isNull(rentBatteryOrderVO.getType())) {
                excelVo.setType("");
            }
            if (Objects.equals(rentBatteryOrderVO.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                excelVo.setType("租电池");
            }
            if (Objects.equals(rentBatteryOrderVO.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                excelVo.setType("还电池");
            }

            //订单状态
            if (Objects.isNull(rentBatteryOrderVO.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.INIT)) {
                excelVo.setStatus("初始化");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_INIT_CHECK)) {
                excelVo.setStatus("租电池前置检测");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_BATTERY_NOT_EXISTS)) {
                excelVo.setStatus("租电池格挡是空仓");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_OPEN_SUCCESS)) {
                excelVo.setStatus("租电池开门成功");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_OPEN_FAIL)) {
                excelVo.setStatus("租电池开门失败");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_SUCCESS)) {
                excelVo.setStatus("租电池成功取走");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_TIMEOUT)) {
                excelVo.setStatus("租电池超时");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_INIT_CHECK)) {
                excelVo.setStatus("还电池前置检测");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_EXISTS)) {
                excelVo.setStatus("还电池仓内有电池");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_OPEN_SUCCESS)) {
                excelVo.setStatus("还电池开门成功");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_OPEN_FAIL)) {
                excelVo.setStatus("还电池开门失败");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_SUCCESS)) {
                excelVo.setStatus("还电池成功");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_FAIL)) {
                excelVo.setStatus("还电池检测失败");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_TIMEOUT)) {
                excelVo.setStatus("还电池检测超时");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.ORDER_EXCEPTION_CANCEL)) {
                excelVo.setStatus("订单异常结束");
            }
            if (Objects.equals(rentBatteryOrderVO.getStatus(), RentBatteryOrder.ORDER_CANCEL)) {
                excelVo.setStatus("订单取消");
            }

            rentBatteryOrderExcelVOS.add(excelVo);
        }

        String fileName = "换电订单报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, RentBatteryOrderExcelVO.class).sheet("sheet").doWrite(rentBatteryOrderExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }

    @Override
    public R queryNewStatus(String orderId) {
        Map<String, Object> map = new HashMap<>();
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderMapper.selectOne(Wrappers.<RentBatteryOrder>lambdaQuery().eq(RentBatteryOrder::getOrderId, orderId));
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        String status = rentBatteryOrder.getStatus();

        //开门中
        if (rentBatteryOrder.getOrderSeq() < RentBatteryOrder.STATUS_OPEN_SUCCESS
                || rentBatteryOrder.getOrderSeq().equals(RentBatteryOrder.STATUS_OPEN_FAIL)) {
            status = rentBatteryOrder.getCellNo() + "号仓门开门中";
        }

        //开门成功
        if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_OPEN_SUCCESS)
                || Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RETURN_OPEN_SUCCESS)) {
            status = rentBatteryOrder.getCellNo() + "号仓门开门成功，电池检测中";
        }


        //订单状态
        map.put("status", status);

        //页面图片显示
        Integer picture = 0;

        //rent
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            picture = 2;
        }

        //return
        if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            picture = 1;
        }

        //error
        if (rentBatteryOrder.getOrderSeq().equals(RentBatteryOrder.STATUS_ORDER_CANCEL)
                || rentBatteryOrder.getOrderSeq().equals(RentBatteryOrder.STATUS_ORDER_EXCEPTION_CANCEL)) {

            picture = 3;
        }

        //订单状态
        map.put("picture", picture);

        //是否出错 0--未出错 1--出错
        Integer type = 0;
        //是否重试 0--重试  1--不能重试
        Integer isTry = 1;

        String result = redisService.get(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
        if (StringUtils.isNotEmpty(result)) {
            WarnMsgVo warnMsgVo = JsonUtil.fromJson(result, WarnMsgVo.class);
            boolean isNeedEndOrder = warnMsgVo.getIsNeedEndOrder();
            if (!isNeedEndOrder) {
                isTry = 0;
            }

            if (Objects.equals(rentBatteryOrder.getStatus(), RentBatteryOrder.RENT_BATTERY_TAKE_TIMEOUT)) {
                isTry = 1;
            }

            String msg = warnMsgVo.getMsg();

            //出错信息
            map.put("queryStatus", msg);
            type = 1;
        }

        map.put("type", type);
        map.put("isTry", isTry);
        log.info("map is -->{}", map);
        return R.ok(map);
    }

    //分配满仓
    @Override
    public Triple<Boolean, String, Object> findUsableBatteryCellNo(ElectricityCabinet electricityCabinet, String cellNo, String batteryType, Long franchiseeId, Integer orderSource) {


        Integer box = null;

        //低电量换电
        Double fullCharged = electricityCabinet.getFullyCharged();
        if (Objects.nonNull(orderSource) && Objects.equals(orderSource, OrderQuery.LOW_BATTERY_ORDER)) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinet.getTenantId());
            List<LowBatteryExchangeModel> list = JsonUtil.fromJsonArray(electricityConfig.getLowBatteryExchangeModel(), LowBatteryExchangeModel.class);
            if (Objects.nonNull(list) && list.size() > 0) {
                fullCharged = list.get(0).getBatteryPowerStandard();
            }
        }
    
        BigEleBatteryVo bigEleBatteryVo = electricityBatteryService
                .queryMaxPowerByElectricityCabinetId(electricityCabinet.getId());
        
        if (Objects.nonNull(bigEleBatteryVo)) {
            if (Objects.isNull(batteryType)) {
                String newCellNo = bigEleBatteryVo.getCellNo();
                Double power = bigEleBatteryVo.getPower();
                if (Objects.nonNull(newCellNo) && Objects.nonNull(power)
                        && !Objects.equals(cellNo, newCellNo) && power > fullCharged) {

                    //1、查仓门
                    ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), bigEleBatteryVo.getCellNo());
                    if (Objects.nonNull(electricityCabinetBox) && Objects.nonNull(electricityCabinetBox.getSn())) {

                        //2、查电池
                        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetBox.getSn());
                        if (Objects.nonNull(electricityBattery)) {

                            //3、查加盟商是否绑定电池
//                            FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryIdAndFranchiseeId(electricityBattery.getId(), franchiseeId);
//                            if (Objects.nonNull(franchiseeBindElectricityBattery)) {
//                                box = Integer.valueOf(newCellNo);
//                            }

                            ElectricityBattery battery = electricityBatteryService.selectByBatteryIdAndFranchiseeId(electricityBattery.getId(), franchiseeId);
                            if (Objects.nonNull(battery)) {
                                box = Integer.valueOf(newCellNo);
                            }
                        }

                    }

                }
            }
        }

        if (Objects.isNull(box)) {
            List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxService
                    .queryElectricityBatteryBox(electricityCabinet, cellNo, batteryType);
            if (ObjectUtil.isEmpty(electricityCabinetBoxList)) {
                log.error("ELE ERROR! electricityCabinet not has full charge battery,electricityCabinetId={}",electricityCabinet.getId());
                return Triple.of(false, "0", "换电柜暂无满电电池");
            }

            List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = new ArrayList<>();

            Integer count = 0;
            for (ElectricityCabinetBox electricityCabinetBox : electricityCabinetBoxList) {
                //是否满电
                ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetBox.getSn());
                if (Objects.nonNull(electricityBattery)) {
                    if (electricityBattery.getPower() >= fullCharged) {

                        ElectricityCabinetBoxVO electricityCabinetBoxVO = new ElectricityCabinetBoxVO();
                        BeanUtil.copyProperties(electricityCabinetBox, electricityCabinetBoxVO);
                        electricityCabinetBoxVO.setPower(electricityBattery.getPower());
                        count++;

//                        FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryIdAndFranchiseeId(electricityBattery.getId(), franchiseeId);
//                        if (Objects.nonNull(franchiseeBindElectricityBattery)) {
//                            electricityCabinetBoxVOList.add(electricityCabinetBoxVO);
//                        }

                        ElectricityBattery battery = electricityBatteryService.selectByBatteryIdAndFranchiseeId(electricityBattery.getId(), franchiseeId);
                        if (Objects.nonNull(battery)) {
                            electricityCabinetBoxVOList.add(electricityCabinetBoxVO);
                        }
                    }
                }
            }

            if (count < 1) {
                log.error("ELE ERROR! not found full charge battery,electricityCabinetId={}",electricityCabinet.getId());
                return Triple.of(false, "0", "换电柜暂无满电电池");
            }

            if (ObjectUtil.isEmpty(electricityCabinetBoxVOList)) {
                log.error("ELE ERROR! franchisee not bind battery,electricityCabinetId={}",electricityCabinet.getId());
                return Triple.of(false, "0", "加盟商未绑定满电电池");
            }

            List<ElectricityCabinetBoxVO> usableBoxes = electricityCabinetBoxVOList.stream().sorted(Comparator.comparing(ElectricityCabinetBoxVO::getPower).reversed()).collect(Collectors.toList());

            box = Integer.valueOf(usableBoxes.get(0).getCellNo());
        }

        return Triple.of(true, box.toString(), null);
    }

    @Override
    public R queryCount(RentBatteryOrderQuery rentBatteryOrderQuery) {
        return R.ok(rentBatteryOrderMapper.queryCount(rentBatteryOrderQuery));
    }

    @Override
    public Integer queryCountForScreenStatistic(RentBatteryOrderQuery rentBatteryOrderQuery) {
        return rentBatteryOrderMapper.queryCount(rentBatteryOrderQuery);
    }

    @Override
    public R selfOpenCell(OrderSelfOpenCellQuery orderSelfOpenCellQuery) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("self open cell order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder oldRentBatteryOrder = queryByUidAndType(user.getUid());
        if (Objects.nonNull(oldRentBatteryOrder)) {
            if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "100023", "存在未完成租电订单，不能自助开仓");
            } else if (Objects.equals(oldRentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) oldRentBatteryOrder.getOrderId(), "100024", "存在未完成还电订单，不能自助开仓");
            }
        }

        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "100022", "存在未完成换电订单，不能自助开仓");
        }

        RentBatteryOrder rentBatteryOrder = queryByOrderId(orderSelfOpenCellQuery.getOrderId());
        if (Objects.isNull(rentBatteryOrder)) {
            log.error("self open cell ERROR! not found order,orderId{} ", orderSelfOpenCellQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderSelfOpenCellQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("self open cell ERROR! not found electricityCabinet ！electricityCabinetId{}", orderSelfOpenCellQuery.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("self open cell ERROR!  electricityCabinet is offline ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //下单锁住柜机
        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 3 * 60 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.00105", "该柜机有人正在下单，请稍等片刻");
        }

        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(orderSelfOpenCellQuery.getOrderId());

        Long now = System.currentTimeMillis();
        if (Objects.isNull(electricityExceptionOrderStatusRecord) || !Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), RentBatteryOrder.RETURN_BATTERY_CHECK_TIMEOUT)) {
            log.error("self open cell ERROR! not old cell exception ！orderId{}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100020", "非退电池仓门异常无法自主开仓");
        }

        if ((now - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 > 3) {
            log.error("self open cell ERROR! self open cell timeout ！orderId{}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100026", "自助开仓超时");
        }

        if (Objects.equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL)) {
            log.error("self open cell ERROR! is self open cell exception ！orderId{}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100021", "该订单已进行自助开仓");
        }

        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! not found Franchisee ！storeId{}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order ERROR! user not auth!  uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR!FranchiseeId is not equal!uid:{} , FranchiseeId1:{} ,FranchiseeId2:{}", user.getUid(), store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }


        //未租电池
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! user not rent battery! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(orderSelfOpenCellQuery.getElectricityCabinetId(), electricityExceptionOrderStatusRecord.getCellNo() + "");
        if (Objects.isNull(electricityCabinetBox)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! not find cellNO! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0006", "未找到此仓门");
        }

        if (Objects.equals(electricityCabinetBox.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! cellNO unUsable! uid:{} ", user.getUid());
            return R.fail("100025", "此仓门已被禁用");
        }

        try {

            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .createTime(System.currentTimeMillis())
                    .orderId(orderSelfOpenCellQuery.getOrderId())
                    .tenantId(electricityCabinet.getTenantId())
                    .msg("退电池检测失败，自助开仓")
                    .seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_BY_RETURN_BATTERY)
                    .type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_RENT_BACK)
                    .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
            electricityCabinetOrderOperHistoryService.insert(history);

            RentBatteryOrder rentBatteryOrderUpdate = new RentBatteryOrder();
            rentBatteryOrderUpdate.setId(rentBatteryOrder.getId());
            rentBatteryOrderUpdate.setUpdateTime(System.currentTimeMillis());
            rentBatteryOrderUpdate.setRemark("自助开仓");
            update(rentBatteryOrderUpdate);

            //发送自助开仓命令
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("orderId", orderSelfOpenCellQuery.getOrderId());
            dataMap.put("cellNo", electricityExceptionOrderStatusRecord.getCellNo());
            dataMap.put("batteryName", rentBatteryOrder.getElectricityBatterySn());

            String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + rentBatteryOrder.getId();

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(sessionId)
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.SELF_OPEN_CELL).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            return R.ok(sessionId);
        } catch (Exception e) {
            log.error("order is error" + e);
            return R.fail("ELECTRICITY.0025", "自助开仓失败");
        } finally {
            redisService.delete(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + orderSelfOpenCellQuery.getElectricityCabinetId() + "_" + electricityExceptionOrderStatusRecord.getCellNo());
        }
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
