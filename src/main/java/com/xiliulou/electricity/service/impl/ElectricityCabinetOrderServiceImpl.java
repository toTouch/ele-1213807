package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ExceptionCircuitBreaker;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderMapper;
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
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    UserInfoService userInfoService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    @Autowired
    ElectricityConfigService electricityConfigService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;

    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetOrder electricityCabinetOrder) {
        return this.electricityCabinetOrderMapper.updateById(electricityCabinetOrder);

    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param orderId 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrder queryByOrderId(String orderId) {
        return this.electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getOrderId, orderId));
    }

    /**
     * 新增订单
     *
     * @param electricityCabinetOrder
     */
    public void insertOrder(ElectricityCabinetOrder electricityCabinetOrder) {
        this.electricityCabinetOrderMapper.insert(electricityCabinetOrder);
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
    @Transactional(rollbackFor = Exception.class)
    public R order(OrderQuery orderQuery) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }

        //用户成功换电后才会添加缓存，用户换电周期限制
        String orderLimit = redisService.get(CacheConstant.ORDER_TIME_UID + user.getUid());
        if (StringUtils.isNotEmpty(orderLimit)) {
            return R.fail("ELECTRICITY.0061", "下单过于频繁");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderQuery.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("order  ERROR! not found electricityCabinet ！electricityCabinetId{}", orderQuery.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("order  ERROR!  electricityCabinet is offline ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

		/*//换电柜是否出现异常被锁住
		String isLock = redisService.get(ElectricityCabinetConstant.UNLOCK_CABINET_CACHE + electricityCabinet.getId());
		if (StringUtils.isNotEmpty(isLock)) {
			log.error("order  ERROR!  electricityCabinet is lock ！electricityCabinetId{}", electricityCabinet.getId());
			return R.fail("ELECTRICITY.0063", "换电柜出现异常，暂时不能下单");
		}*/

        //换电柜是否打烊
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

        //下单锁住柜机
        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 5 * 60 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.00105", "该柜机有人正在下单，请稍等片刻");
        }


        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("queryByDevice  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("queryByDevice  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("queryByDevice  ERROR! not found Franchisee ！storeId{}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! user not auth!  uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR!FranchiseeId is not equal!uid:{} , FranchiseeId1:{} ,FranchiseeId2:{}", user.getUid(), store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //用户是否开通月卡
        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
                || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! not found memberCard ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }

        //用户是否暂停月卡
        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            log.error("order ERROR! member card is disable ! uid:{}", user.getUid());
            return R.fail("ELECTRICITY.100002", "月卡已暂停");
        }

        //未租电池
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! user not rent battery! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        Long now = System.currentTimeMillis();
        ElectricityMemberCard electricityMemberCard = null;
        //如果用户不是送的卡
        if (!Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
            electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && franchiseeUserInfo.getMemberCardExpireTime() < now) {
                redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
                log.error("order  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
                return R.fail("ELECTRICITY.0023", "月卡已过期");
            }

            if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                if (franchiseeUserInfo.getRemainingNumber() < 0) {
                    //用户需购买相同套餐，补齐所欠换电次数
                    redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
                    log.error("order  ERROR! memberCard remainingNumber insufficient uid={}", user.getUid());
                    return R.fail("ELECTRICITY.00117", "套餐剩余次数为负", franchiseeUserInfo.getCardId());
                }

                if (franchiseeUserInfo.getMemberCardExpireTime() < now) {
                    redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
                    log.error("order  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
                    return R.fail("ELECTRICITY.0023", "月卡已过期");
                }
            }
        } else {
            if (franchiseeUserInfo.getMemberCardExpireTime() < now) {
                redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
                log.error("rentBattery  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
                return R.fail("ELECTRICITY.0023", "月卡已过期");
            }
        }

        //默认是小程序下单
        if (Objects.isNull(orderQuery.getSource())) {
            orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
        }

        //分配开门格挡
        Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNo(electricityCabinet.getId());

        if (Objects.isNull(usableEmptyCellNo.getRight())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            return R.fail("ELECTRICITY.0008", "换电柜暂无空仓");
        }

        String cellNo = usableEmptyCellNo.getRight().toString();
        try {
            if (Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
                Integer row = franchiseeUserInfoService.minCount(franchiseeUserInfo.getId());
                if (row < 1) {
                    redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
                    log.error("order  ERROR! not found memberCard uid={}", user.getUid());
                    return R.fail("ELECTRICITY.00118", "月卡可用次数已用完");
                }
            } else {
                if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                    Integer row = franchiseeUserInfoService.minCount(franchiseeUserInfo.getId());
                    if (row < 1) {
                        redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
                        log.error("order  ERROR! not found memberCard uid={}", user.getUid());
                        return R.fail("ELECTRICITY.00118", "月卡可用次数已用完");
                    }
                }
            }

            //3.根据用户查询旧电池
            ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                    .orderId(generateOrderId(orderQuery.getElectricityCabinetId(), cellNo, user.getUid()))
                    .uid(user.getUid())
                    .phone(userInfo.getPhone())
                    .electricityCabinetId(orderQuery.getElectricityCabinetId())
                    .oldCellNo(Integer.valueOf(cellNo))
                    .orderSeq(ElectricityCabinetOrder.STATUS_INIT)
                    .status(ElectricityCabinetOrder.INIT)
                    .source(orderQuery.getSource())
                    .paymentMethod(franchiseeUserInfo.getCardType())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(tenantId).build();
            electricityCabinetOrderMapper.insert(electricityCabinetOrder);


            //4.开旧电池门
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", cellNo);
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("status", electricityCabinetOrder.getStatus());

            //是否开启电池检测
            ElectricityConfig electricityConfig = electricityConfigService.queryOne(tenantId);
            if (Objects.nonNull(electricityConfig)) {
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", franchiseeUserInfo.getNowElectricityBatterySn());
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
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_OLD_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            return R.ok(electricityCabinetOrder.getOrderId());
        } catch (Exception e) {
            log.error("order is error" + e);
            return R.fail("ELECTRICITY.0025", "下单失败");
        } finally {
            redisService.delete(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + orderQuery.getElectricityCabinetId() + "_" + cellNo);
        }
    }

    @Override
    @Transactional
    public R openDoor(OpenDoorQuery openDoorQuery) {
        if (Objects.isNull(openDoorQuery.getOrderId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, openDoorQuery.getOrderId()));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", openDoorQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }


        //开新门开旧门不易前端为准，以订单状态为准
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW) {
            openDoorQuery.setOpenType(OpenDoorQuery.OLD_OPEN_TYPE);
        } else {
            openDoorQuery.setOpenType(OpenDoorQuery.NEW_OPEN_TYPE);
        }

        //旧电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_CHECK_FAIL)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_CHECK_BATTERY_EXISTS)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }

        //新电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_FAIL)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_BATTERY_NOT_EXISTS)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }

        //判断开门用户是否匹配
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(electricityCabinetOrder.getUid(), user.getUid())) {
            return R.fail("ELECTRICITY.0016", "订单用户不匹配，非法开门");
        }

        //查找换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId{}", electricityCabinetOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet{}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("order  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }


        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //旧电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", electricityCabinetOrder.getOldCellNo());
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("status", electricityCabinetOrder.getStatus());

            //是否开启电池检测
            ElectricityConfig electricityConfig = electricityConfigService.queryOne(userInfo.getTenantId());
            if (Objects.nonNull(electricityConfig)) {
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", franchiseeUserInfo.getNowElectricityBatterySn());
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
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_OLD_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        }

        //新电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", electricityCabinetOrder.getNewCellNo());
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("serial_number", electricityCabinetOrder.getNewElectricityBatterySn());
            dataMap.put("status", electricityCabinetOrder.getStatus().toString());
            dataMap.put("old_cell_no", electricityCabinetOrder.getOldCellNo());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_NEW_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        }
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + electricityCabinetOrder.getOrderId());
        return R.ok();
    }

    @Override
    public R queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {

        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = electricityCabinetOrderMapper.queryList(electricityCabinetOrderQuery);
        if (ObjectUtil.isEmpty(electricityCabinetOrderVOList)) {
            return R.ok(new ArrayList<>());
        }
        if (ObjectUtil.isNotEmpty(electricityCabinetOrderVOList)) {
            electricityCabinetOrderVOList.parallelStream().forEach(e -> {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(e.getElectricityCabinetId());
                if (Objects.nonNull(electricityCabinet)) {
                    e.setElectricityCabinetName(electricityCabinet.getName());
                }

                if (e.getStatus().equals(ElectricityCabinetOrder.ORDER_CANCEL)
                        || e.getStatus().equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {
                    ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(e.getOrderId());
                    if (Objects.nonNull(electricityExceptionOrderStatusRecord) && Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL) && Objects.equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.NOT_SELF_OPEN_CELL)) {
                        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(e.getElectricityCabinetId(), e.getOldCellNo() + "");
                        e.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                        if (Objects.nonNull(electricityCabinetBox) && Objects.equals(electricityCabinetBox.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
                            e.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY_UNUSABLE_CELL);
                        }
                        if (Objects.isNull(e.getOldElectricityBatterySn())) {
                            e.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY_NOT_BATTERY_SN);
                        }
                    }
                }

                UserInfo userInfo = userInfoService.selectUserByUid(e.getUid());
                if (Objects.nonNull(userInfo)) {
                    e.setUName(userInfo.getName());
                }
            });
        }

        return R.ok(electricityCabinetOrderVOList);
    }

    @Override
    public R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return R.ok(electricityCabinetOrderMapper.queryCount(electricityCabinetOrderQuery));
    }

    @Override
    public Integer queryCountForScreenStatistic(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return electricityCabinetOrderMapper.queryCount(electricityCabinetOrderQuery);
    }

    @Override
    public void exportExcel(ElectricityCabinetOrderQuery electricityCabinetOrderQuery, HttpServletResponse response) {
        electricityCabinetOrderQuery.setOffset(0L);
        electricityCabinetOrderQuery.setSize(2000L);
        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = electricityCabinetOrderMapper.queryList(electricityCabinetOrderQuery);
        if (ObjectUtil.isEmpty(electricityCabinetOrderVOList)) {
            throw new CustomBusinessException("查不到订单");
        }

        List<ElectricityCabinetOrderExcelVO> electricityCabinetOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (ElectricityCabinetOrderVO electricityCabinetOrderVO : electricityCabinetOrderVOList) {
            index++;
            ElectricityCabinetOrderExcelVO excelVo = new ElectricityCabinetOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(electricityCabinetOrderVO.getOrderId());
            excelVo.setPhone(electricityCabinetOrderVO.getPhone());
            excelVo.setOldElectricityBatterySn(electricityCabinetOrderVO.getOldElectricityBatterySn());
            excelVo.setNewElectricityBatterySn(electricityCabinetOrderVO.getNewElectricityBatterySn());

            if (Objects.nonNull(electricityCabinetOrderVO.getCreateTime())) {
                excelVo.setCreateTime(simpleDateFormat.format(new Date(electricityCabinetOrderVO.getCreateTime())));
            }
            if (Objects.nonNull(electricityCabinetOrderVO.getUpdateTime())) {
                excelVo.setUpdateTime(simpleDateFormat.format(new Date(electricityCabinetOrderVO.getUpdateTime())));
            }

            if (Objects.isNull(electricityCabinetOrderVO.getPaymentMethod())) {
                excelVo.setPaymentMethod("");
            }
            if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_MONTH_CARD)) {
                excelVo.setPaymentMethod("月卡");
            }
            if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_SEASON_CARD)) {
                excelVo.setPaymentMethod("季卡");
            }
            if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_YEAR_CARD)) {
                excelVo.setPaymentMethod("年卡");
            }

            //订单状态
            if (Objects.isNull(electricityCabinetOrderVO.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT)) {
                excelVo.setStatus("换电订单生成");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_CHECK_FAIL)) {
                excelVo.setStatus("换电过程放入没电电池检测失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_CHECK_BATTERY_EXISTS)) {
                excelVo.setStatus("换电柜放入没电电池开门发现有电池存在");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
                excelVo.setStatus("换电柜放入没电电池开门成功");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
                excelVo.setStatus("换电柜放入没电电池开门失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
                excelVo.setStatus("换电柜检测没电电池成功");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
                excelVo.setStatus("换电柜检测没电电池失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_TIMEOUT)) {
                excelVo.setStatus("换电柜检测没电电池超时");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_FAIL)) {
                excelVo.setStatus("换电柜开满电电池前置检测失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_BATTERY_NOT_EXISTS)) {
                excelVo.setStatus("换电柜开满电电池发现电池不存在");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
                excelVo.setStatus("换电柜开满电电池仓门成功");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
                excelVo.setStatus("换电柜开满电电池仓门失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
                excelVo.setStatus("换电柜满电电池成功取走，流程结束");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_TIMEOUT)) {
                excelVo.setStatus("换电柜取走满电电池超时");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.ORDER_CANCEL)) {
                excelVo.setStatus("订单取消");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {
                excelVo.setStatus("订单异常结束");
            }
            electricityCabinetOrderExcelVOS.add(excelVo);
        }

        String fileName = "换电订单报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ElectricityCabinetOrderExcelVO.class).sheet("sheet").doWrite(electricityCabinetOrderExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }


    @Override
    @Transactional
    public R endOrder(String orderId) {
        //结束异常订单只改订单状态，不用考虑其他
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId)
                .notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS, ElectricityCabinetOrder.ORDER_CANCEL, ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL);
        newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL);
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrderMapper.updateById(newElectricityCabinetOrder);

        //回退月卡
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.nonNull(userInfo)) {
            //
            //是否缴纳押金，是否绑定电池
            FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
            if (Objects.nonNull(franchiseeUserInfo)) {
                Long now = System.currentTimeMillis();
                if (Objects.nonNull(franchiseeUserInfo.getMemberCardExpireTime()) && Objects.nonNull(franchiseeUserInfo.getRemainingNumber())
                        && franchiseeUserInfo.getMemberCardExpireTime() > now && franchiseeUserInfo.getRemainingNumber() != -1) {
                    //回退月卡次数
                    franchiseeUserInfoService.plusCount(userInfo.getId());
                }
            }
        }

        //删除开门失败缓存
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);

        //结束订单锁
        redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinetOrder.getElectricityCabinetId());
        return R.ok();
    }


    @Override
    public Integer homeOneCount(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetOrderMapper.homeOneCount(first, now, eleIdList, tenantId);
    }

    @Override
    public BigDecimal homeOneSuccess(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        Integer countTotal = homeOneCount(first, now, eleIdList, tenantId);
        Integer successTotal = electricityCabinetOrderMapper.homeOneSuccess(first, now, eleIdList, tenantId);
        if (successTotal == 0 || countTotal == 0) {
            return BigDecimal.valueOf(0);
        }
        return BigDecimal.valueOf(successTotal).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(countTotal), BigDecimal.ROUND_HALF_EVEN);
    }

    @Override
    public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetOrderMapper.homeThree(startTimeMilliDay, endTimeMilliDay, eleIdList, tenantId);
    }

    @Override
    public Integer homeMonth(Long uid, Long first, Long now) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, first, now).eq(ElectricityCabinetOrder::getUid, uid));
    }

    @Override
    public Integer homeTotal(Long uid) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid));
    }


    @Override
    public ElectricityCabinetOrder queryByUid(Long uid) {
        return electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid)
                .notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS, ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL, ElectricityCabinetOrder.ORDER_CANCEL)
                .orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
    }

    @Override
    public ElectricityCabinetOrder queryByCellNoAndEleId(Integer eleId, Integer cellNo) {
        return electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>()
                .eq(ElectricityCabinetOrder::getElectricityCabinetId, eleId)
                .eq(ElectricityCabinetOrder::getOldCellNo, cellNo).or().eq(ElectricityCabinetOrder::getNewCellNo, cellNo)
                .orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
    }

    @Override
    public String findUsableCellNo(Integer id) {
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxService.queryNoElectricityBatteryBox(id);
        if (!DataUtil.collectionIsUsable(usableBoxes)) {
            return null;
        }

        List<Integer> boxes = usableBoxes.stream().map(ElectricityCabinetBox::getCellNo).map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());

        //查看有没有初始化过设备的上次操作过的格挡,这里不必关心线程安全，不需要保证原子性
        if (!redisService.hasKey(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id)) {
            redisService.setNx(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, boxes.get(0).toString());
        }

        String lastCellNo = redisService.get(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id);

        boxes = rebuildByCellCircleForDevice(boxes, Integer.parseInt(lastCellNo));

        for (Integer box : boxes) {
            if (redisService.setNx(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + id + "_" + box.toString(), "1", 300 * 1000L, false)) {
                redisService.set(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, box.toString());
                return box.toString();
            }
        }

        return null;
    }

    @Override
    public R queryNewStatus(String orderId) {

        Map<String, Object> map = new HashMap<>();
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId{} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        String status = electricityCabinetOrder.getStatus();

        //订单状态旧门开门中
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_INIT_BATTERY_CHECK_SUCCESS) {
            status = electricityCabinetOrder.getOldCellNo() + "号仓门开门中";
        }


        //订单状态新门开门中
        if (electricityCabinetOrder.getOrderSeq() > ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW
                && electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_COMPLETE_OPEN_SUCCESS) {
            status = electricityCabinetOrder.getNewCellNo() + "号仓门开门中";
        }


        //旧电池开门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
            status = electricityCabinetOrder.getOldCellNo() + "号仓门开门成功，电池检测中";
        }


        //旧电池检测成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            status = "旧电池已存入," + electricityCabinetOrder.getNewCellNo() + "号仓门开门中";
        }

        //订单状态新门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
            status = electricityCabinetOrder.getNewCellNo() + "号仓门开门成功，电池检测中";
        }

        //订单状态新电池取走
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            status = "新电池已取走,订单完成";
        }

        //订单状态
        map.put("status", status);


        //页面图片显示
        Integer picture = 0;

        //return
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW) {
            picture = 1;
        }


        //rent
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)
                || Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)
                || Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)
                || Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            picture = 2;
        }


        //error
        if (electricityCabinetOrder.getOrderSeq().equals(ElectricityCabinetOrder.STATUS_ORDER_CANCEL)
                || electricityCabinetOrder.getOrderSeq().equals(ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL)) {
            ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(orderId);
            if (Objects.nonNull(electricityExceptionOrderStatusRecord) && Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
                ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), electricityCabinetOrder.getOldCellNo() + "");
                map.put("selfOpenCell", ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                if (Objects.nonNull(electricityCabinetBox) && Objects.equals(electricityCabinetBox.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
                    map.put("selfOpenCell", ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY_UNUSABLE_CELL);
                }
            }

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

    @Override
    public R selfOpenCell(OrderSelfOpenCellQuery orderSelfOpenCellQuery) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("self open cell order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100023", "存在未完成租电订单，不能自助开仓");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100024", "存在未完成还电订单，不能自助开仓");
            }
        }

        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "100022", "存在未完成换电订单，不能自助开仓");
        }

        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(orderSelfOpenCellQuery.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
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
        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 60 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.00105", "该柜机有人正在下单，请稍等片刻");
        }

        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(orderSelfOpenCellQuery.getOrderId());

        Long now = System.currentTimeMillis();
        if (Objects.isNull(electricityExceptionOrderStatusRecord) || !Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
            log.error("self open cell ERROR! not old cell exception ！orderId{}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100020", "非旧仓门异常无法自主开仓");
        }

        if ((now - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 > 3) {
            log.error("self open cell ERROR! self open cell timeout ！orderId{}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100026", "自助开仓已超开仓时间");
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

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            log.error("self open cell order  ERROR! not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
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
                    .msg("旧电池检测失败，自助开仓")
                    .seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ)
                    .type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE)
                    .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
            electricityCabinetOrderOperHistoryService.insert(history);

            ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
            electricityCabinetOrderUpdate.setId(electricityCabinetOrder.getId());
            electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrderUpdate.setSource(ElectricityCabinetOrder.ORDER_SOURCE_FOR_SELF_OPEN_CELL);
            update(electricityCabinetOrderUpdate);

            //发送自助开仓命令
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("orderId", orderSelfOpenCellQuery.getOrderId());
            dataMap.put("cellNo", electricityExceptionOrderStatusRecord.getCellNo());
            dataMap.put("batteryName", electricityCabinetOrder.getOldElectricityBatterySn());

            String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId();

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

    @Override
    public R checkOpenSessionId(String sessionId) {
        String s = redisService.get(CacheConstant.ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY + sessionId);
        if (StrUtil.isEmpty(s)) {
            return R.ok("0001");
        }
        if ("true".equalsIgnoreCase(s)) {
            return R.ok("0002");
        } else {
            return R.ok("0003");
        }
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

    @Deprecated
    public String generateOrderId(Integer id, String cellNo, Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + id +
                cellNo + uid;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> orderV2(OrderQueryV2 orderQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ORDER ERROR!  not found user,eid={}", orderQuery.getEid());
            return Triple.of(false, "100001", "未能找到用户");
        }

        Triple<Boolean, String, String> checkExistsOrderResult = checkUserExistsUnFinishOrder(user.getUid());
        if (checkExistsOrderResult.getLeft()) {
            log.warn("ORDER WARN! user exists unFinishOrder! uid={}", user.getUid());
            return Triple.of(false, checkExistsOrderResult.getMiddle(), checkExistsOrderResult.getRight());
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderQuery.getEid());
        if (Objects.isNull(electricityCabinet)) {
            return Triple.of(false, "100003", "柜机不存在");
        }

        //换电柜是否打烊
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return Triple.of(false, "100203", "换电柜已打烊");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            return Triple.of(false, "100004", "柜机不在线");
        }

        //这里加柜机的缓存，为了限制不同时分配格挡
        if (!redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100214", "柜机正在被使用，请稍后");
        }

        if (!redisService.setNx(CacheConstant.ORDER_TIME_UID + user.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100002", "下单过于频繁");
        }

        try {
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("ORDER ERROR!  not found store ！uid={},eid={},storeId={}", user.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
                return Triple.of(false, "100204", "未找到门店");
            }

            //校验用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.error("ORDER ERROR! not found user info,uid={} ", user.getUid());
                return Triple.of(false, "100205", "未找到用户审核信息");
            }

            if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
                log.error("ORDER ERROR! userinfo is UN AUTH! uid={}", user.getUid());
                return Triple.of(false, "100206", "用户未审核");
            }

            //判断用户有没有条件下单（套餐，月卡）
            FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
            Triple<Boolean, String, Object> checkConditionResult = checkUserHasConditionOrder(franchiseeUserInfo, store, user);
            if (!checkConditionResult.getLeft()) {
                return checkConditionResult;
            }

            //默认是小程序下单
            if (Objects.isNull(orderQuery.getSource())) {
                orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
            }

            Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNo(electricityCabinet.getId());
            if (!usableEmptyCellNo.getLeft()) {
                return Triple.of(false, "100215", "换电柜暂无空仓");
            }

            Triple<Boolean, String, Object> usableBatteryCellNoResult = electricityCabinetService.findUsableBatteryCellNoV2(electricityCabinet.getId(), franchiseeUserInfo.getBatteryType(), electricityCabinet.getFullyCharged(), franchiseeUserInfo.getFranchiseeId());
            if (!usableBatteryCellNoResult.getLeft()) {
                return Triple.of(false, usableBatteryCellNoResult.getMiddle(), usableBatteryCellNoResult.getRight());
            }

            //修改按此套餐的次数
            Triple<Boolean, String, String> modifyResult = checkAndModifyMemberCardCount(franchiseeUserInfo, user);
            if (!modifyResult.getLeft()) {
                return Triple.of(false, modifyResult.getMiddle(), modifyResult.getRight());
            }

            ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                    .orderId(generateExchangeOrderId(user.getUid()))
                    .uid(user.getUid())
                    .phone(userInfo.getPhone())
                    .electricityCabinetId(orderQuery.getEid())
                    .oldCellNo(usableEmptyCellNo.getRight())
                    .newCellNo(Integer.parseInt(((ElectricityCabinetBox) usableBatteryCellNoResult.getRight()).getCellNo()))
                    .orderSeq(ElectricityCabinetOrder.STATUS_INIT)
                    .status(ElectricityCabinetOrder.INIT)
                    .source(orderQuery.getSource())
                    .paymentMethod(franchiseeUserInfo.getCardType())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(TenantContextHolder.getTenantId()).build();
            electricityCabinetOrderMapper.insert(electricityCabinetOrder);


            HashMap<String, Object> commandData = Maps.newHashMap();
            commandData.put("orderId", electricityCabinetOrder.getOrderId());
            commandData.put("placeCellNo", franchiseeUserInfo.getModelType());
            commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());

            ElectricityConfig electricityConfig = electricityConfigService.queryOne(TenantContextHolder.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                commandData.put("userBindingBatterySn", franchiseeUserInfo.getNowElectricityBatterySn());
            }

            if (Objects.equals(franchiseeUserInfo.getModelType(), FranchiseeUserInfo.NEW_MODEL_TYPE)) {
                commandData.put("multiBatteryModelName", franchiseeUserInfo.getModelType());
            }

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                    .data(commandData)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_OLD_DOOR).build();
            Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            if (!result.getLeft()) {
                return Triple.of(false, "100218", "下单消息发送失败");
            }
            return Triple.of(true, null, null);
        } finally {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            redisService.delete(CacheConstant.ORDER_TIME_UID + user.getUid());
        }
    }

    private String generateExchangeOrderId(Long uid) {
        return String.valueOf(uid) + System.currentTimeMillis() / 1000 + RandomUtil.randomNumbers(3);
    }

    private Triple<Boolean, String, String> checkAndModifyMemberCardCount(FranchiseeUserInfo franchiseeUserInfo, TokenUser user) {
        //这里的memberCard不能为空
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());
        if (Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT) || Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.LIMITED_COUNT_TYPE)) {
            Integer row = franchiseeUserInfoService.minCount(franchiseeUserInfo.getId());
            if (row < 1) {
                log.error("ORDER ERROR! memberCard's count modify fail, uid={} ,cardId={}", user.getUid(), franchiseeUserInfo.getCardId());
                return Triple.of(false, "100213", "套餐剩余次数不足");
            }
        }
        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> checkUserHasConditionOrder(FranchiseeUserInfo franchiseeUserInfo, Store store, TokenUser user) {
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("ORDER ERROR! not found franchiseeUser! uid={}", user.getUid());
            return Triple.of(false, "100207", "用户加盟商信息未找到");
        }

        if (!Objects.equals(store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId())) {
            log.error("ORDER ERROR! store's fId  is not equal franchieseeId uid={} , store's fid:{} ,fid:{}", user.getUid(), store.getFranchiseeId(), franchiseeUserInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }

        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)) {
            log.warn("ORDER WARN! user didn't pay a deposit,uid={},fid={}", user.getUid(), franchiseeUserInfo.getId());
            return Triple.of(false, "100209", "用户未缴纳押金");
        }

        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
            log.warn("ORDER WARN! user not rent battery! uid={}", user.getUid());
            return Triple.of(false, "100210", "用户还没有租借电池");
        }

        //判断套餐
        if (Objects.isNull(franchiseeUserInfo.getCardId())) {
            log.warn("ORDER WARN! user haven't memberCard uid={}", user.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }

        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", user.getUid());
            return Triple.of(false, "100211", "用户套餐已暂停");
        }

        //套餐是否可用
        long now = System.currentTimeMillis();
        if (franchiseeUserInfo.getMemberCardExpireTime() < now) {
            log.warn("ORDER WARN! user's member card is expire! uid={} cardId={}", user.getUid(), franchiseeUserInfo.getCardId());
            return Triple.of(false, "100212", "用户套餐已过期");
        }

        //如果用户不是送的套餐
        if (!Objects.equals(franchiseeUserInfo.getCardType(), ElectricityMemberCard.TYPE_COUNT)) {
            ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.LIMITED_COUNT_TYPE) && franchiseeUserInfo.getRemainingNumber() < 0) {
                log.warn("ORDER ERROR! user's count < 0 ,uid={},cardId={}", user.getUid(), franchiseeUserInfo.getCardType());
                return Triple.of(false, "100213", "用户套餐剩余次数不足");
            }
        }

        if (Objects.isNull(franchiseeUserInfo.getBatteryServiceFeeGenerateTime())) {
            return Triple.of(true, null, null);
        }

        long cardDays = (now - franchiseeUserInfo.getBatteryServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
        if (Objects.isNull(franchiseeUserInfo.getNowElectricityBatterySn()) || cardDays < 1) {
            return Triple.of(true, null, null);
        }

        //这里开始计费用户电池服务费
        Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeUserInfo.getFranchiseeId());
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            Integer model = BatteryConstant.acquireBattery(franchiseeUserInfo.getBatteryType());
            List<ModelBatteryDeposit> modelBatteryDepositList = JSONObject.parseArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);

            Optional<ModelBatteryDeposit> modelBatteryDepositOptional = modelBatteryDepositList.stream().filter(m -> model.equals(m.getModel())).findFirst();
            if (modelBatteryDepositOptional.isEmpty()) {
                log.error("ORDER ERROR! modelBattery is null ,uid={},cardId={}", user.getUid(), franchiseeUserInfo.getCardType());
                return Triple.of(true, null, null);
            }

            //计算服务费
            BigDecimal batteryServiceFee = modelBatteryDepositOptional.get().getBatteryServiceFee().multiply(new BigDecimal(cardDays));
            if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                return Triple.of(false, "100220", batteryServiceFee);
            }
        } else {
            BigDecimal franchiseeBatteryServiceFee = franchisee.getBatteryServiceFee();
            //计算服务费
            BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(new BigDecimal(cardDays));
            if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                return Triple.of(false, "100220", batteryServiceFee);
            }
        }

        return Triple.of(true, null, null);


    }

    private Triple<Boolean, String, String> checkUserExistsUnFinishOrder(Long uid) {
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(uid);
        if (Objects.nonNull(rentBatteryOrder) && Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            return Triple.of(true, "100200", "存在未完成租电订单，不能下单");
        } else if (Objects.nonNull(rentBatteryOrder) && Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            return Triple.of(true, "100202", "存在未完成租电订单，不能下单");
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(uid);
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return Triple.of(true, "100201", "存在未完成换电订单，不能下单");
        }

        return Triple.of(false, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> queryOrderStatusForShow(String orderId) {
        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(orderId);
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ORDER ERROR! query order not found,uid={},orderId={}", SecurityUtils.getUid(), orderId);
            return Triple.of(false, "100221", "未能查找到订单");
        }

        String status = electricityCabinetOrder.getStatus();
        ExchangeOrderMsgShowVO showVo = new ExchangeOrderMsgShowVO();

        if (isOpenPlaceCellStatus(status)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门中");
        }

        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门成功，电池检测中");
        }

        //旧电池检测成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            showVo.setStatus("旧电池已存入," + electricityCabinetOrder.getNewCellNo() + "号仓门开门中");
        }

        //订单状态新门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getNewCellNo() + "号仓门开门成功，电池检测中");
        }

        //订单状态新电池取走
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            showVo.setStatus("新电池已取走,订单完成");
        }

        if (isPlaceBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.PLACE_BATTERY_IMG);
        }

        if (isTakeBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.PLACE_BATTERY_IMG);
        }

        if (isExceptionOrder(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.EXCEPTION_IMG);
            //检查这里是否需要自助开仓
            checkIsNeedSelfOpenCell(electricityCabinetOrder, showVo);
        }


        return Triple.of(true, null, showVo);
    }

    private void checkIsNeedSelfOpenCell(ElectricityCabinetOrder electricityCabinetOrder, ExchangeOrderMsgShowVO showVo) {
        ElectricityExceptionOrderStatusRecord statusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(electricityCabinetOrder.getOrderId());
        if (Objects.isNull(statusRecord)) {
            return;
        }

        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), electricityCabinetOrder.getOldCellNo() + "");
        if (Objects.nonNull(electricityCabinetBox) && Objects.equals(electricityCabinetBox.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
            showVo.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY_UNUSABLE_CELL);
        } else {
            showVo.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
        }

    }

    private boolean isExceptionOrder(String status) {
        return status.equals(ElectricityCabinetOrder.ORDER_CANCEL)
                || status.equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL);
    }

    private boolean isTakeBatteryAllStatus(String status) {
        return false;
    }

    private boolean isPlaceBatteryAllStatus(String status) {
        return status.equals(ElectricityCabinetOrder.INIT)
                || status.equals(ElectricityCabinetOrder.INIT_OPEN_SUCCESS);
    }

    private boolean isOpenPlaceCellStatus(String status) {
        return status.equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)
                || status.equals(ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)
                || status.equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS);
    }
}
