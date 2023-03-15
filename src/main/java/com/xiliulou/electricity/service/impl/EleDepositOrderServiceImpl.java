package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.EleBatteryServiceFeeOrderMapper;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Service("eleDepositOrderService")
@Slf4j
public class EleDepositOrderServiceImpl implements EleDepositOrderService {
    @Resource
    EleDepositOrderMapper eleDepositOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserOauthBindService userOauthBindService;
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Resource
    EleBatteryServiceFeeOrderMapper eleBatteryServiceFeeOrderMapper;
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    StoreGoodsService storeGoodsService;
    @Autowired
    ElectricityCarService electricityCarService;
    @Autowired
    EleUserOperateRecordService eleUserOperateRecordService;
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    @Autowired
    UserBatteryService userBatteryService;
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    UserCarService userCarService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    CarDepositOrderService carDepositOrderService;

    @Override
    public EleDepositOrder queryByOrderId(String orderNo) {
        return eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R payDeposit(String productKey, String deviceName, Long franchiseeId, Integer model, HttpServletRequest request) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_INTEGRATED_PAYMENT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("ELE DEPOSIT ERROR!not found electricityPayParams,uid={}", user.getUid());
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("ELE DEPOSIT ERROR!not found userOauthBind,uid={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE DEPOSIT ERROR! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("ELE DEPOSIT ERROR! user already rent deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0049", "已缴纳押金");
        }

        Long storeId = null;
        if (Objects.isNull(franchiseeId)) {
            Store store = storeService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
            if (Objects.isNull(store)) {
                log.error("ELE DEPOSIT ERROR! not found store,uid={},p={},d={}", user.getUid(), productKey, deviceName);
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("ELE DEPOSIT ERROR! not found franchiseeId,storeId={},uid={}", store.getId(), user.getUid());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            franchiseeId = store.getFranchiseeId();
            storeId = store.getId();
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("ELE DEPOSIT ERROR! franchisee is null,uid={},franchiseeId={}", user.getUid(), franchiseeId);
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        //根据加盟商计算押金
        Pair<Boolean, Object> depositPair = caculDepositByFranchisee(franchisee, model, userInfo);
        if (!Boolean.TRUE.equals(depositPair.getLeft()) || Objects.isNull(depositPair.getRight())) {
            log.error("ELE DEPOSIT ERROR! deposit is null,franchiseeId={},uid={}", franchiseeId, user.getUid());
            return R.fail("ELECTRICITY.00110", "未找到押金");
        }

        BigDecimal payAmount = (BigDecimal) depositPair.getRight();

        String batteryType = Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? BatteryConstant.acquireBatteryShort(model) : null;

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, user.getUid());

        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(orderId)
                .uid(user.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(payAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenantId)
                .franchiseeId(franchisee.getId())
                .payType(EleDepositOrder.ONLINE_PAYMENT)
                .storeId(storeId)
                .modelType(franchisee.getModelType())
                .batteryType(batteryType)
                .build();

        //支付零元
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
            int insert = eleDepositOrderMapper.insert(eleDepositOrder);
            DbUtils.dbOperateSuccessThen(insert, () -> {

                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(userInfo.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                updateUserInfo.setFranchiseeId(franchisee.getId());
                userInfoService.updateByUid(updateUserInfo);

                UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
                userBatteryDeposit.setUid(userInfo.getUid());
                userBatteryDeposit.setOrderId(orderId);
                userBatteryDeposit.setBatteryDeposit(payAmount);
                userBatteryDeposit.setDid(eleDepositOrder.getId());
                userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
                userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
                userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
                userBatteryDeposit.setCreateTime(System.currentTimeMillis());
                userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
                userBatteryDeposit.setTenantId(tenantId);
                userBatteryDepositService.insertOrUpdate(userBatteryDeposit);


                UserBattery userBattery = new UserBattery();
                userBattery.setUid(userInfo.getUid());
                userBattery.setBatteryType(batteryType);
                userBattery.setUpdateTime(System.currentTimeMillis());
                userBattery.setDelFlag(UserBattery.DEL_NORMAL);
                userBatteryService.insertOrUpdate(userBattery);

                return null;
            });

            return R.ok();
        }

        eleDepositOrderMapper.insert(eleDepositOrder);

        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .payAmount(payAmount)
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_DEPOSIT)
                    .attach(ElectricityTradeOrder.ATTACH_DEPOSIT)
                    .description("押金收费")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("ELE DEPOSIT ERROR! wechat v3 deposit order error,uid={}", user.getUid(), e);
        }

        return R.fail("ELECTRICITY.0099", "下单失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R returnDeposit(HttpServletRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.000000", "操作频繁,请稍后再试!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE DEPOSIT ERROR! user is disable! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());

        //是否存在换电次数欠费情况
        Integer packageOwe = null;
        //套餐欠费次数
        Integer memberCardOweNumber = null;
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.error("returnDeposit  ERROR! disable member card is reviewing userId={}", user.getUid());
                return R.fail("ELECTRICITY.100003", "停卡正在审核中");
            }

            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.error("returnDeposit  ERROR! member card is disable userId={}", user.getUid());
                return R.fail("ELECTRICITY.100004", "月卡已暂停");
            }

            ElectricityMemberCard bindElectricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
            if (Objects.nonNull(bindElectricityMemberCard)) {
                if (!Objects.equals(bindElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && Objects.nonNull(userBatteryMemberCard.getRemainingNumber()) && userBatteryMemberCard.getRemainingNumber() < 0) {
                    memberCardOweNumber = Math.abs(userBatteryMemberCard.getRemainingNumber());
                    packageOwe = UserBatteryMemberCard.MEMBER_CARD_OWE;
                }
            }
        }

        //判断是否缴纳押金
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || !Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("ELE DEPOSIT ERROR! not pay deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        if (Objects.equals(userBatteryDeposit.getOrderId(), "-1")) {
            return R.fail("ELECTRICITY.00115", "请线下退押");
        }

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
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }

        //查找缴纳押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, userBatteryDeposit.getOrderId()));
        if (Objects.isNull(eleDepositOrder)) {
            log.error("ELE DEPOSIT ERROR! not found eleDepositOrder! userId={}", user.getUid());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        BigDecimal deposit = userBatteryDeposit.getBatteryDeposit();
        if (!Objects.equals(eleDepositOrder.getPayAmount(), deposit)) {
            log.error("ELE DEPOSIT ERROR! illegal deposit! userId={}", user.getUid());
            return R.fail("ELECTRICITY.0044", "退款金额不符");
        }

        Long now = System.currentTimeMillis();
        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());

        long cardDays = 0;
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            BigDecimal serviceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
            userChangeServiceFee = serviceFee;
        }

        if (Objects.nonNull(userBatteryMemberCard)) {
            Long disableMemberCardTime = userBatteryMemberCard.getDisableMemberCardTime();

            //判断用户是否产生电池服务费
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {

                cardDays = (now - disableMemberCardTime) / 1000L / 60 / 60 / 24;

                //不足一天按一天计算
                double time = Math.ceil((now - disableMemberCardTime) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1;
                }
                BigDecimal serviceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, user.getUid(), cardDays, null, serviceFeeUserInfo);
                userChangeServiceFee = serviceFee;
            }
        }

        if (BigDecimal.valueOf(0).compareTo(userChangeServiceFee) != 0) {
            return R.fail("ELECTRICITY.100000", "存在电池服务费", userChangeServiceFee);
        }

        //判断是否退电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("ELE DEPOSIT ERROR! not return battery,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0046", "未退还电池");
        }

        if (Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.OFFLINE_PAYMENT)) {
            log.error("ELE DEPOSIT ERROR! travel to store,uid={}", user.getUid());
            return R.fail("ELECTRICITY.00115", "请前往门店退押金");
        }

        BigDecimal payAmount = eleDepositOrder.getPayAmount();

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId());
        if (refundCount > 0) {
            log.error("ELE DEPOSIT ERROR! have refunding order,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0047", "请勿重复退款");
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REFUND, user.getUid());

        //生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                .orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(orderId)
                .payAmount(payAmount)
                .refundAmount(payAmount)
                .status(EleRefundOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId())
                .memberCardOweNumber(memberCardOweNumber).build();

        //退款零元
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            eleRefundOrder.setStatus(EleRefundOrder.STATUS_SUCCESS);
            EleRefundOrder result = eleRefundOrderService.insert(eleRefundOrder);

            if (Objects.nonNull(result)) {
                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(userInfo.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);
    
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());

//                userBatteryDepositService.deleteByUid(userInfo.getUid());
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());

                userBatteryService.deleteByUid(userInfo.getUid());

                //退押金解绑用户所属加盟商
                userInfoService.unBindUserFranchiseeId(userInfo.getUid());

                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(user.getUid());
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }
            }

            return R.ok("SUCCESS");
        }

        eleRefundOrderService.insert(eleRefundOrder);

        //等到后台同意退款
        return R.ok(packageOwe);
    }

    @Override
    public R queryList(EleDepositOrderQuery eleDepositOrderQuery) {
        List<EleDepositOrderVO> eleDepositOrderVOS = null;
        if (Objects.equals(eleDepositOrderQuery.getDepositType(), EleDepositOrder.ELECTRICITY_DEPOSIT)) {
            eleDepositOrderVOS = eleDepositOrderMapper.queryList(eleDepositOrderQuery);
        } else {
            eleDepositOrderVOS = eleDepositOrderMapper.queryListForRentCar(eleDepositOrderQuery);
        }
        return R.ok(eleDepositOrderVOS);
    }

    @Override
    public R queryListToUser(EleDepositOrderQuery eleDepositOrderQuery) {
        return R.ok(eleDepositOrderMapper.queryListForUser(eleDepositOrderQuery));
    }

    @Override
    public R payDepositOrderList(EleDepositOrderQuery eleDepositOrderQuery) {
        List<PayDepositOrderVO> payDepositOrderVOList = eleDepositOrderMapper.payDepositOrderList(eleDepositOrderQuery);
        if (CollectionUtils.isEmpty(payDepositOrderVOList)) {
            return R.ok();
        }

        for (PayDepositOrderVO payDepositOrderVO : payDepositOrderVOList) {
            Long refundTime = eleRefundOrderService.queryRefundTime(payDepositOrderVO.getOrderId());
            payDepositOrderVO.setRefundTime(refundTime);
    
            payDepositOrderVO.setModel(BatteryConstant.acquireBattery(payDepositOrderVO.getBatteryType()));
        }

        return R.ok(payDepositOrderVOList);
    }

    @Override
    public void update(EleDepositOrder eleDepositOrderUpdate) {
        eleDepositOrderMapper.updateById(eleDepositOrderUpdate);
    }

    @Override
    public R queryUserDeposit() {
        //优化 TODO
        Map<String, String> map = new HashMap<>();

        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELE DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("ELE DEPOSIT ERROR! user not rent deposit,uid={}", user.getUid());
            return R.ok(null);
        }

        UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBattery)) {
            log.error("ELE DEPOSIT ERROR! not found userBattery,uid={}", user.getUid());
            return R.fail("100247", "用户信息不存在");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("ELE DEPOSIT ERROR! not found userBatteryDeposit,uid={}", user.getUid());
            return R.fail("100247", "用户信息不存在");
        }

        String batteryType = userBattery.getBatteryType();
        if (Objects.nonNull(batteryType)) {
            Integer acquireBattery = BatteryConstant.acquireBattery(batteryType);
            map.put("batteryType", Objects.isNull(acquireBattery) ? null : String.valueOf(acquireBattery));
        } else {
            map.put("batteryType", null);
        }

        if ((Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)
                && Objects.nonNull(userBatteryDeposit.getBatteryDeposit()) && Objects.nonNull(userBatteryDeposit.getOrderId()))) {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());

            if (Objects.equals(userBatteryDeposit.getOrderId(), "-1")) {
                map.put("refundStatus", null);
                map.put("deposit", userBatteryDeposit.getBatteryDeposit().toString());
                map.put("time", String.valueOf(System.currentTimeMillis()));
                map.put("franchiseeName", Objects.nonNull(franchisee) ? franchisee.getName() : "");
            } else {
                //是否退款
                Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(userBatteryDeposit.getOrderId());
                if (Objects.nonNull(refundStatus)) {
                    map.put("refundStatus", refundStatus.toString());
                } else {
                    map.put("refundStatus", null);
                }

                EleDepositOrder eleDepositOrder = queryByOrderId(userBatteryDeposit.getOrderId());
                if (Objects.isNull(eleDepositOrder)) {
                    map.put("store", null);
                } else {
                    map.put("time", eleDepositOrder.getCreateTime().toString());
                    Store store = storeService.queryByIdFromCache(eleDepositOrder.getStoreId());
                    if (Objects.nonNull(store)) {
                        map.put("store", store.getName());
                    } else {
                        map.put("store", null);
                    }
                }

                map.put("deposit", userBatteryDeposit.getBatteryDeposit().toString());
                //最后一次缴纳押金时间
//                map.put("time", this.queryByOrderId(userBatteryDeposit.getOrderId()).getUpdateTime().toString());

                map.put("franchiseeName", franchisee.getName());
                map.put("rentBatteryStatus", userInfo.getBatteryRentStatus().toString());
            }

            return R.ok(map);
        }
        return R.ok(null);
    }

    @Override
    public void exportExcel(EleDepositOrderQuery eleDepositOrderQuery, HttpServletResponse response) {
        eleDepositOrderQuery.setOffset(0L);
        eleDepositOrderQuery.setSize(2000L);
        List<EleDepositOrderVO> eleDepositOrderList = eleDepositOrderMapper.queryList(eleDepositOrderQuery);
        if (ObjectUtil.isEmpty(eleDepositOrderList)) {
            throw new CustomBusinessException("查不到订单");
        }

        List<EleDepositOrderExcelVO> eleDepositOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (EleDepositOrderVO eleDepositOrder : eleDepositOrderList) {
            index++;
            EleDepositOrderExcelVO excelVo = new EleDepositOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(eleDepositOrder.getOrderId());
            excelVo.setPhone(eleDepositOrder.getPhone());
            excelVo.setName(eleDepositOrder.getName());
            excelVo.setPayAmount(eleDepositOrder.getPayAmount());
            excelVo.setStoreName(eleDepositOrder.getStoreName());

            if (Objects.nonNull(eleDepositOrder.getCreateTime())) {
                excelVo.setCreatTime(simpleDateFormat.format(new Date(eleDepositOrder.getCreateTime())));
            }

            if (Objects.isNull(eleDepositOrder.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_INIT)) {
                excelVo.setStatus("未支付");
            }
            if (Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_SUCCESS)) {
                excelVo.setStatus("支付成功");
            }
            if (Objects.equals(eleDepositOrder.getStatus(), EleDepositOrder.STATUS_FAIL)) {
                excelVo.setStatus("支付失败");
            }

            eleDepositOrderExcelVOS.add(excelVo);
        }

        String fileName = "换电订单报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, EleDepositOrderExcelVO.class).sheet("sheet").doWrite(eleDepositOrderExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }

    @Override
    public R queryFranchiseeDeposit(String productKey, String deviceName, Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        // TODO: 2022/12/21 没有加盟商id什么情景下

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("ELE DEPOSIT ERROR! not found franchisee,franchiseeId={}", franchiseeId);
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        Map<String, Object> map = new HashMap();

        //根据类型分押金
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            //型号押金
            // TODO: 2022/12/21 bug
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            map.put("modelType", Franchisee.NEW_MODEL_TYPE);
            map.put("batteryDeposit", modelBatteryDepositList);
            return R.ok(map);
        }

        map.put("modelType", Franchisee.OLD_MODEL_TYPE);
        map.put("batteryDeposit", franchisee.getBatteryDeposit());
        return R.ok(map);
    }

    @Override
    public R queryDeposit(String productKey, String deviceName, Long franchiseeId) {

        if (Objects.isNull(franchiseeId)) {
            //换电柜
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
            if (Objects.isNull(electricityCabinet)) {
                log.error("queryDeposit  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
                return R.fail("ELECTRICITY.0005", "未找到换电柜");
            }

            //查询押金
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("queryDeposit  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("queryDeposit  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("queryDeposit  ERROR! not found Franchisee ！storeId{}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            franchiseeId = store.getFranchiseeId();
        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("queryDeposit  ERROR! not found Franchisee ！franchiseeId{}", franchiseeId);
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        //根据类型分押金
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            //型号押金
            List modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
            return R.ok(modelBatteryDepositList);
        }

        return R.ok(franchisee.getBatteryDeposit());
    }

    @Override
    public R queryCount(EleDepositOrderQuery eleDepositOrderQuery) {
        return R.ok(eleDepositOrderMapper.queryCount(eleDepositOrderQuery));
    }

    @Override
    public void insert(EleDepositOrder eleDepositOrder) {
        eleDepositOrderMapper.insert(eleDepositOrder);
    }

    @Override
    public R queryModelType(String productKey, String deviceName) {
        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE DEPOSIT ERROR! not found electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //查询押金
        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("ELE DEPOSIT ERROR! not found store,electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("ELE DEPOSIT ERROR! not found store,storeId={}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.error("ELE DEPOSIT ERROR! not found franchiseeId,storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(store.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE DEPOSIT ERROR! not found franchisee,franchiseeId={}", store.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        return R.ok(franchisee.getModelType());
    }

    @Override
    public R payBatteryServiceFee(HttpServletRequest request) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("pay battery service fee  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //限频
        Boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_BATTERY_SERVICE_FEE_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("pay battery service fee CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS UID={}", user.getUid());
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);

        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("pay battery service fee CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        //判断是否实名认证
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("pay battery service fee  ERROR! not found user UID={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("pay battery service fee  ERROR! not found user UID={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBattery)) {
            log.error("ELE ERROR! not found userBattery,uid={}", user.getUid());
//            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        BigDecimal payAmount = null;
        BigDecimal batteryServiceFee = null;
        Long now = System.currentTimeMillis();
        long cardDays = 0;
        Integer source = EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE;

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());

        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {

            BigDecimal chargeRate = electricityMemberCardOrderService.checkDifferentModelBatteryServiceFee(franchisee, userInfo, userBattery);
            batteryServiceFee = chargeRate;

            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            BigDecimal serviceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, franchisee, cardDays);
            payAmount = serviceFee;
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {
            source = EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD;
            cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
            //不足一天按一天计算
            double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }

            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(), userInfo.getTenantId());

            BigDecimal serviceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, user.getUid(), cardDays, eleDisableMemberCardRecord, serviceFeeUserInfo);
            payAmount = serviceFee;

            batteryServiceFee = eleDisableMemberCardRecord.getChargeRate();
        }


        String nowBattery = "";
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (!Objects.isNull(electricityBattery)) {
            nowBattery = electricityBattery.getSn();
        }


        String orderId = generateOrderId(user.getUid());
        //创建订单
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder()
                .orderId(orderId)
                .uid(user.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(payAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenantId)
                .source(source)
                .franchiseeId(franchisee.getId())
                .modelType(franchisee.getModelType())
                .batteryType(Objects.isNull(userBattery) ? "" : userBattery.getBatteryType())
                .sn(nowBattery)
                .batteryServiceFee(batteryServiceFee).build();
        eleBatteryServiceFeeOrderMapper.insert(eleBatteryServiceFeeOrder);

        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .payAmount(payAmount)
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_BATTERY_SERVICE_FEE)
                    .attach(ElectricityTradeOrder.ATTACH_BATTERY_SERVICE_FEE)
                    .description("电池服务费收费")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("payEleBatteryServiceFee ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return R.fail("ELECTRICITY.0099", "下单失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R adminPayRentCarDeposit(RentCarDepositAdd rentCarDepositAdd) {
        UserInfo userInfo = userInfoService.queryUserInfoByPhone(rentCarDepositAdd.getPhone(), rentCarDepositAdd.getTenantId());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR DEPOSIT ERROR! not found user! phone={}", rentCarDepositAdd.getPhone());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR DEPOSIT ERROR! user is rent car deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0049", "已缴纳押金");
        }

        BigDecimal payAmount = rentCarDepositAdd.getPayAmount();

        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, userInfo.getUid());

        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(orderId)
                .uid(userInfo.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(payAmount)
                .status(EleDepositOrder.STATUS_SUCCESS)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(rentCarDepositAdd.getTenantId())
                .depositType(EleDepositOrder.RENT_CAR_DEPOSIT)
                .storeId(rentCarDepositAdd.getStoreId())
                .carModelId(rentCarDepositAdd.getCarModelId())
                .franchiseeId(rentCarDepositAdd.getFranchiseeId())
                .payType(EleDepositOrder.OFFLINE_PAYMENT).build();
        int insert = eleDepositOrderMapper.insert(eleDepositOrder);

        DbUtils.dbOperateSuccessThen(insert, () -> {

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            UserCarDeposit userCarDeposit = new UserCarDeposit();
            userCarDeposit.setUid(userInfo.getUid());
            userCarDeposit.setOrderId(orderId);
            userCarDeposit.setCarDeposit(eleDepositOrder.getPayAmount());
            userCarDeposit.setDelFlag(UserCarDeposit.DEL_NORMAL);
            userCarDeposit.setApplyDepositTime(System.currentTimeMillis());
            userCarDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            userCarDeposit.setTenantId(userInfo.getTenantId());
            userCarDeposit.setCreateTime(System.currentTimeMillis());
            userCarDeposit.setUpdateTime(System.currentTimeMillis());
            userCarDepositService.insertOrUpdate(userCarDeposit);

            UserCar userCar = new UserCar();
            userCar.setUid(userInfo.getUid());
            userCar.setCarModel(rentCarDepositAdd.getCarModelId().longValue());
            userCar.setTenantId(userInfo.getTenantId());
            userCar.setCreateTime(System.currentTimeMillis());
            userCar.setUpdateTime(System.currentTimeMillis());
            userCarService.insertOrUpdate(userCar);

            return null;
        });

        return R.ok();
    }

    /**
     * 用户端缴纳租车押金
     *
     * @return
     */
    @Override
    @Deprecated
    public R payRentCarDeposit(Long storeId, Integer carModelId, HttpServletRequest request) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_CAR_DEPOSIT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false)) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("ELE CAR DEPOSIT ERROR!not found electricityPayParams,uid={}", user.getUid());
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("ELE CAR DEPOSIT ERROR!not found userOauthBind,uid={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE CAR DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE CAR DEPOSIT ERROR! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR DEPOSIT ERROR! user already rent deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0049", "已缴纳押金");
        }

        Store store = storeService.queryByIdFromCache(storeId);
        if (Objects.isNull(store)) {
            log.error("ELE CAR DEPOSIT ERROR! not found store,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carModelId);
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR DEPOSIT ERROR! not find carMode, carModelId={},uid={}", carModelId, user.getUid());
            return R.fail("100009", "未找到该型号车辆");
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, user.getUid());

        BigDecimal payAmount = electricityCarModel.getCarDeposit();

        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(orderId)
                .uid(user.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(payAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(tenantId)
                .franchiseeId(store.getFranchiseeId())
                .depositType(EleDepositOrder.RENT_CAR_DEPOSIT)
                .payType(EleDepositOrder.ONLINE_PAYMENT)
                .storeId(storeId)
                .carModelId(carModelId).build();

        // TODO: 2022/12/21 支付零元
        //支付零元
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
            int insert = eleDepositOrderMapper.insert(eleDepositOrder);

            DbUtils.dbOperateSuccessThen(insert, () -> {

                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(userInfo.getUid());
                updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);

                UserCarDeposit userCarDeposit = new UserCarDeposit();
                userCarDeposit.setUid(userInfo.getUid());
                userCarDeposit.setOrderId(orderId);
                userCarDeposit.setCarDeposit(eleDepositOrder.getPayAmount());
                userCarDeposit.setDelFlag(UserCarDeposit.DEL_NORMAL);
                userCarDeposit.setApplyDepositTime(System.currentTimeMillis());
                userCarDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
                userCarDeposit.setTenantId(userInfo.getTenantId());
                userCarDeposit.setCreateTime(System.currentTimeMillis());
                userCarDeposit.setUpdateTime(System.currentTimeMillis());
                userCarDepositService.insertOrUpdate(userCarDeposit);

                UserCar userCar = new UserCar();
                userCar.setUid(userInfo.getUid());
                userCar.setCarModel(carModelId.longValue());
                userCar.setTenantId(userInfo.getTenantId());
                userCar.setCreateTime(System.currentTimeMillis());
                userCar.setUpdateTime(System.currentTimeMillis());
                userCarService.insertOrUpdate(userCar);

                return null;
            });

            return R.ok();
        }


        eleDepositOrderMapper.insert(eleDepositOrder);
        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .payAmount(payAmount)
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_RENT_CAR_DEPOSIT)
                    .attach(ElectricityTradeOrder.ATTACH_RENT_CAR_DEPOSIT)
                    .description("租车押金收费")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("ELE CAR DEPOSIT ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R refundRentCarDeposit(HttpServletRequest request) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR REFUND ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_CAR_DEPOSIT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false)) {
            return R.fail("ELECTRICITY.000000", "操作频繁,请稍后再试!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR REFUND ERROR! not found user,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE CAR REFUND ERROR! user is disable,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR REFUND ERROR! user is not rent deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //是否归还车辆
        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_NO)) {
            log.error("ELE CAR REFUND ERROR! user is rent car,uid={}", user.getUid());
            return R.fail("100250", "用户未归还车辆");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE CAR REFUND ERROR! not found userCarDeposit! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户信息");
        }

        //查找缴纳押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, userCarDeposit.getOrderId()));
        if (Objects.isNull(eleDepositOrder)) {
            log.error("ELE CAR REFUND ERROR! not found eleDepositOrder! uid={},orderId={}", user.getUid(), userCarDeposit.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        BigDecimal deposit = userCarDeposit.getCarDeposit();
        if (eleDepositOrder.getPayAmount().compareTo(deposit) == 0) {
            log.error("ELE CAR REFUND ERROR! deposit not equals! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0044", "退款金额不符");
        }

        BigDecimal payAmount = eleDepositOrder.getPayAmount();

        //退款零元
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
            int insert = eleDepositOrderMapper.insert(eleDepositOrder);

            DbUtils.dbOperateSuccessThen(insert, () -> {

                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(userInfo.getId());
                updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);

                userCarDepositService.logicDeleteByUid(userInfo.getId());

                userCarService.deleteByUid(userInfo.getUid());

                return null;
            });

            return R.ok();
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId());
        if (refundCount > 0) {
            log.error("ELE CAR REFUND ERROR! have refunding order! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0047", "请勿重复退款");
        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_REFUND, user.getUid());

        //生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                .orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(orderId)
                .payAmount(payAmount)
                .refundAmount(payAmount)
                .status(EleRefundOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId())
                .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER).build();
        eleRefundOrderService.insert(eleRefundOrder);

        //等到后台同意退款
        return R.ok();
    }

    @Override
    public R queryRentCarDeposit() {
        Map<String, Object> map = new HashMap<>();
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //用户是否缴纳押金
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("queryRentCarDeposit  ERROR! not found userInfo! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserCar userCar = userCarService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCar)) {
            return R.ok();
//            log.error("ELE DEPOSIT ERROR! not found userCar,uid={}", user.getUid());
//            return R.fail("100247", "用户信息不存在");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarDeposit)) {
            return R.ok();
//            log.error("ELE DEPOSIT ERROR! not found userCarDeposit,uid={}", user.getUid());
//            return R.fail("100247", "用户信息不存在");
        }

        if (Objects.isNull(userCarDeposit.getCarDeposit())) {
            map.put("store", null);
            map.put("carModel", null);
            map.put("refundStatus", null);
            map.put("deposit", 0);
            map.put("time", String.valueOf(System.currentTimeMillis()));
        } else {
            //是否退款
            Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(userCarDeposit.getOrderId());
            if (Objects.nonNull(refundStatus)) {
                map.put("refundStatus", refundStatus.toString());
            } else {
                map.put("refundStatus", null);
            }

            EleDepositOrder eleDepositOrder = queryByOrderId(userCarDeposit.getOrderId());

            if (Objects.isNull(eleDepositOrder)) {
                map.put("store", null);
                map.put("carModel", null);
                map.put("payType", null);
            } else {
                map.put("payType", eleDepositOrder.getPayType().toString());
                Store store = storeService.queryByIdFromCache(eleDepositOrder.getStoreId());
                if (Objects.nonNull(store)) {
                    map.put("store", store.getName());
                } else {
                    map.put("store", null);
                }
                ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(userCar.getCarModel().intValue());
                if (Objects.nonNull(electricityCarModel)) {
                    map.put("carModel", electricityCarModel.getName());
                } else {
                    map.put("carModel", null);
                }
            }

            map.put("deposit", userCarDeposit.getCarDeposit().toString());
            //最后一次缴纳押金时间
            map.put("time", this.queryByOrderId(userCarDeposit.getOrderId()).getUpdateTime().toString());
        }

        return R.ok(map);

//        if ((Objects.equals(franchiseeUserInfo.getRentCarStatus(), FranchiseeUserInfo.RENT_CAR_STATUS_IS_DEPOSIT)
//                || Objects.equals(franchiseeUserInfo.getRentCarStatus(), FranchiseeUserInfo.RENT_CAR_STATUS_IS_RENT_CAR))
//                && Objects.nonNull(franchiseeUserInfo.getRentCarDeposit()) && Objects.nonNull(franchiseeUserInfo.getRentCarOrderId())) {
//
//            if (Objects.equals(franchiseeUserInfo.getRentCarOrderId(), "-1")) {
//                map.put("store", null);
//                map.put("carModel", null);
//                map.put("refundStatus", null);
//                map.put("deposit", franchiseeUserInfo.getRentCarDeposit().toString());
//                map.put("time", String.valueOf(System.currentTimeMillis()));
//            } else {
//                //是否退款
//                Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(franchiseeUserInfo.getRentCarOrderId());
//                if (Objects.nonNull(refundStatus)) {
//                    map.put("refundStatus", refundStatus.toString());
//                } else {
//                    map.put("refundStatus", null);
//                }
//
//                EleDepositOrder eleDepositOrder = queryByOrderId(franchiseeUserInfo.getRentCarOrderId());
//
//                if (Objects.isNull(eleDepositOrder)) {
//                    map.put("store", null);
//                    map.put("carModel", null);
//                    map.put("payType", null);
//                } else {
//                    map.put("payType", eleDepositOrder.getPayType().toString());
//                    Store store = storeService.queryByIdFromCache(eleDepositOrder.getStoreId());
//                    if (Objects.nonNull(store)) {
//                        map.put("store", store.getName());
//                    } else {
//                        map.put("store", null);
//                    }
//                    ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(franchiseeUserInfo.getBindCarModelId());
//                    if (Objects.nonNull(electricityCarModel)) {
//                        map.put("carModel", electricityCarModel.getName());
//                    } else {
//                        map.put("carModel", null);
//                    }
//                }
//
//                map.put("deposit", franchiseeUserInfo.getRentCarDeposit().toString());
//                //最后一次缴纳押金时间
//                map.put("time", this.queryByOrderId(franchiseeUserInfo.getRentCarOrderId()).getUpdateTime().toString());
//            }
//            return R.ok(map);
//
//        }
//        return R.ok(null);
    }

    @Override
    public EleDepositOrder queryLastPayDepositTimeByUid(Long uid, Long franchiseeId, Integer tenantId, Integer depositType) {
        return eleDepositOrderMapper.queryLastPayDepositTimeByUid(uid, franchiseeId, tenantId, depositType);
    }

    @Override
    public EleDepositOrder selectLatestByUid(Long uid) {
        return eleDepositOrderMapper.selectLatestByUid(uid);
    }

    @Override
    public BigDecimal queryTurnOver(Integer tenantId) {
        return Optional.ofNullable(eleDepositOrderMapper.queryTurnOver(tenantId)).orElse(new BigDecimal("0"));
    }

    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
                RandomUtil.randomNumbers(6);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R adminPayBatteryDeposit(BatteryDepositAdd batteryDepositAdd) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(batteryDepositAdd.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE DEPOSIT ERROR! not found user,uid={}", batteryDepositAdd.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        //若用户已绑定加盟商，判断选择的加盟商与用户加盟商是否一致
        if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L)) {
            if (!Objects.equals(userInfo.getFranchiseeId(), batteryDepositAdd.getFranchiseeId())) {
                log.error("ELE DEPOSIT ERROR! user bind franchisee not equals params franchisee,uid={}", userInfo.getUid());
                return R.fail("100252", "用户所属加盟商与选择的加盟商不符");
            }
        }

//        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
//        if (Objects.isNull(userBatteryDeposit)) {
//            log.error("ELE DEPOSIT ERROR! not found userBatteryDeposit! uid={}", userInfo.getUid());
//            return R.fail("100247", "未找到用户信息");
//        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(batteryDepositAdd.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE DEPOSIT ERROR! not found Franchisee,franchiseeId={},uid={}", batteryDepositAdd.getFranchiseeId(), userInfo.getUid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("ELE DEPOSIT ERROR! user is rent deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0049", "已缴纳押金");
        }

        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && Objects.isNull(batteryDepositAdd.getModel())) {
            log.error("ELE DEPOSIT ERROR! not select batteyType,franchiseeId={},uid={}", batteryDepositAdd.getFranchiseeId(), userInfo.getUid());
            return R.fail("100027", "未选择电池型号");
        }

        BigDecimal payAmount = batteryDepositAdd.getPayAmount();

        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }

        String batteryType = Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ?
                BatteryConstant.acquireBatteryShort(batteryDepositAdd.getModel()) : null;

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());

        //生成订单
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(orderId)
                .uid(batteryDepositAdd.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(payAmount)
                .status(EleDepositOrder.STATUS_SUCCESS)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId())
                .franchiseeId(batteryDepositAdd.getFranchiseeId())
                .payType(EleDepositOrder.OFFLINE_PAYMENT)
                .storeId(batteryDepositAdd.getStoreId())
                .modelType(batteryDepositAdd.getModelType())
                .batteryType(batteryType)
                .build();

        int insert = eleDepositOrderMapper.insert(eleDepositOrder);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                    .operateModel(EleUserOperateRecord.DEPOSIT_MODEL)
                    .operateContent(EleUserOperateRecord.DEPOSIT_MODEL)
                    .operateUid(user.getUid())
                    .uid(batteryDepositAdd.getUid())
                    .name(user.getUsername())
                    .oldBatteryDeposit(null)
                    .newBatteryDeposit(batteryDepositAdd.getPayAmount())
                    .tenantId(TenantContextHolder.getTenantId())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserOperateRecord);

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            updateUserInfo.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            userInfoService.updateByUid(updateUserInfo);

            UserBatteryDeposit updateUserBatteryDeposit = new UserBatteryDeposit();
            updateUserBatteryDeposit.setUid(userInfo.getUid());
            updateUserBatteryDeposit.setOrderId(orderId);
            updateUserBatteryDeposit.setDid(eleDepositOrder.getId());
            updateUserBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            updateUserBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            updateUserBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
            updateUserBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
            updateUserBatteryDeposit.setTenantId(userInfo.getTenantId());
            updateUserBatteryDeposit.setCreateTime(System.currentTimeMillis());
            updateUserBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            userBatteryDepositService.insertOrUpdate(updateUserBatteryDeposit);

            UserBattery userBattery = new UserBattery();
            userBattery.setUid(userInfo.getUid());
            userBattery.setBatteryType(batteryType);
            userBattery.setTenantId(userInfo.getTenantId());
            userBattery.setCreateTime(System.currentTimeMillis());
            userBattery.setUpdateTime(System.currentTimeMillis());
            userBattery.setDelFlag(UserBattery.DEL_NORMAL);
            userBatteryService.insertOrUpdate(userBattery);

            return null;
        });

        return R.ok();
    }

    @Override
    public BigDecimal queryDepositTurnOverByDepositType(Integer tenantId, Long todayStartTime, Integer depositType, List<Long> franchiseeIds) {
        return Optional.ofNullable(eleDepositOrderMapper.queryDepositTurnOverByDepositType(tenantId, todayStartTime, depositType, franchiseeIds)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryDepositTurnOverAnalysisByDepositType(Integer tenantId, Integer depositType, List<Long> franchiseeId, Long beginTime, Long enTime) {
        return eleDepositOrderMapper.queryDepositTurnOverAnalysisByDepositType(tenantId, depositType, franchiseeId, beginTime, enTime);
    }

    @Override
    public BigDecimal querySumDepositTurnOverAnalysis(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long enTime) {
        return eleDepositOrderMapper.querySumDepositTurnOverAnalysis(tenantId, franchiseeId, beginTime, enTime);
    }

    @Override
    public Triple<Boolean, String, Object> handleRentBatteryDeposit(Long franchiseeId, Integer memberCard ,Integer model, UserInfo userInfo) {
        EleDepositOrder eleDepositOrder = null;
        if (Objects.isNull(memberCard)) {
            return Triple.of(true, "", null);
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("BATTERY DEPOSIT ERROR! not found Franchisee ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }

        BigDecimal depositPayAmount = null;

        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }

        //型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(model)) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }

            // TODO: 2022/12/21 jsonArray  对象
            //型号押金
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("BATTERY DEPOSIT ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
            }

            // TODO: 2022/12/21 理解一下
            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                if ((double) (modelBatteryDeposit.getModel()) - model < 1 && (double) (modelBatteryDeposit.getModel()) - model >= 0) {
                    depositPayAmount = modelBatteryDeposit.getBatteryDeposit();
                    break;
                }
            }
        }

        if (Objects.isNull(depositPayAmount)) {
            log.error("BATTERY DEPOSIT ERROR! payAmount is null ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
        }

        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        eleDepositOrder = EleDepositOrder.builder()
                .orderId(depositOrderId)
                .uid(userInfo.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId())
                .franchiseeId(franchisee.getId())
                .payType(EleDepositOrder.ONLINE_PAYMENT)
                .batteryType(Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? BatteryConstant.acquireBatteryShort(model) : null)
                .storeId(null)
                .modelType(franchisee.getModelType()).build();

        return Triple.of(true, "", eleDepositOrder);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R adminPayCarDeposit(RentCarDepositAdd rentCarDepositAdd) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(rentCarDepositAdd.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR DEPOSIT ERROR! not found user,uid={}", rentCarDepositAdd.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //修改该用户不在当前租户下直接返回
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        //若用户已绑定加盟商，判断选择的加盟商与用户加盟商是否一致
        if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects
                .equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L)) {
            if (!Objects.equals(userInfo.getFranchiseeId(), rentCarDepositAdd.getFranchiseeId())) {
                log.error("ELE CAR DEPOSIT ERROR! user bind franchisee not equals params franchisee,uid={}",
                        userInfo.getUid());
                return R.fail("100252", "用户所属加盟商与选择的加盟商不符");
            }
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(rentCarDepositAdd.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE CAR DEPOSIT ERROR! not found Franchisee,franchiseeId={},uid={}",
                    rentCarDepositAdd.getFranchiseeId(), userInfo.getUid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR DEPOSIT ERROR! user is rent deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0049", "已缴纳押金");
        }
        
        Store store = storeService.queryByIdFromCache(rentCarDepositAdd.getStoreId());
        if (Objects.isNull(store)) {
            log.error("ELE CAR DEPOSIT ERROR! not found store!  uid={},sid={}", userInfo.getUid(),
                    rentCarDepositAdd.getStoreId());
            return R.fail("100204", "未找到门店");
        }
        
        ElectricityCarModel carModel = electricityCarModelService.queryByIdFromCache(rentCarDepositAdd.getCarModelId());
        if (Objects.isNull(carModel)) {
            log.error("ELE CAR DEPOSIT ERROR! not found carModel!  uid={},carModelId={}", userInfo.getUid(),
                    rentCarDepositAdd.getCarModelId());
            return R.fail("100005", "未找到车辆型号");
        }
        
        BigDecimal payAmount = rentCarDepositAdd.getPayAmount();
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, userInfo.getUid());
    
        CarDepositOrder carDepositOrder = new CarDepositOrder();
        carDepositOrder.setUid(userInfo.getUid());
        carDepositOrder.setOrderId(orderId);
        carDepositOrder.setPhone(userInfo.getPhone());
        carDepositOrder.setName(userInfo.getName());
        carDepositOrder.setPayAmount(payAmount);
        carDepositOrder.setDelFlag(CarDepositOrder.DEL_NORMAL);
        carDepositOrder.setStatus(CarDepositOrder.STATUS_SUCCESS);
        carDepositOrder.setTenantId(TenantContextHolder.getTenantId());
        carDepositOrder.setCreateTime(System.currentTimeMillis());
        carDepositOrder.setUpdateTime(System.currentTimeMillis());
        carDepositOrder.setFranchiseeId(store.getFranchiseeId());
        carDepositOrder.setStoreId(rentCarDepositAdd.getStoreId());
        carDepositOrder.setPayType(CarDepositOrder.OFFLINE_PAYTYPE);
        carDepositOrder.setCarModelId(carModel.getId().longValue());
        carDepositOrder.setRentBattery(CarDepositOrder.RENTBATTERY_NO);
        carDepositOrderService.insert(carDepositOrder);
    
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.CAR_DEPOSIT_MODEL)
                .operateContent(EleUserOperateRecord.CAR_DEPOSIT_EDIT_CONTENT).operateUid(user.getUid())
                .uid(userInfo.getUid()).name(user.getUsername()).oldCarDeposit(null).newCarDeposit(payAmount)
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
        
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        UserCarDeposit userCarDeposit = new UserCarDeposit();
        userCarDeposit.setUid(userInfo.getUid());
        userCarDeposit.setOrderId(orderId);
        userCarDeposit.setCarDeposit(carDepositOrder.getPayAmount());
        userCarDeposit.setTenantId(userInfo.getTenantId());
        userCarDeposit.setCreateTime(System.currentTimeMillis());
        userCarDeposit.setUpdateTime(System.currentTimeMillis());
        userCarDepositService.insertOrUpdate(userCarDeposit);
        
        UserCar userCar = new UserCar();
        userCar.setUid(userInfo.getUid());
        userCar.setCarModel(rentCarDepositAdd.getCarModelId().longValue());
        userCar.setTenantId(userInfo.getTenantId());
        userCar.setCreateTime(System.currentTimeMillis());
        userCar.setUpdateTime(System.currentTimeMillis());
        userCarService.insertOrUpdate(userCar);
    
        return R.ok();
    }
    
    @Override
    public R refundCarDeposit() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("CAR REFUND DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService
                .setNx(CacheConstant.CACHE_USER_CAR_RETURN_DEPOSIT_LOCK + user.getUid(), "ok", 3 * 1000L, false)) {
            return R.fail("ELECTRICITY.000000", "操作频繁,请稍后再试!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CAR REFUND DEPOSIT ERROR! not found user,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CAR REFUND DEPOSIT ERROR! user is disable,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("CAR REFUND DEPOSIT ERROR! user is not rent deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        //是否归还车辆
        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_NO)) {
            log.error("CAR REFUND DEPOSIT ERROR! user is rent car,uid={}", user.getUid());
            return R.fail("100250", "用户未归还车辆");
        }
        
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("CAR REFUND DEPOSIT ERROR! not found userCarDeposit! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户信息");
        }
        
        //查找缴纳押金订单
        EleDepositOrder eleDepositOrder = queryByOrderId(userCarDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("CAR REFUND DEPOSIT ERROR! not found eleDepositOrder! uid={},orderId={}", user.getUid(),
                    userCarDeposit.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        if (eleDepositOrder.getPayAmount().compareTo(userCarDeposit.getCarDeposit()) == 0) {
            log.error("CAR REFUND DEPOSIT ERROR! deposit not equals! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0044", "退款金额不符");
        }
        
        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId());
        if (refundCount > 0) {
            log.error("ELE CAR REFUND ERROR! have refunding order! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0047", "请勿重复退款");
        }
        
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_REFUND, user.getUid());
        
        //生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder().orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(orderId).payAmount(userCarDeposit.getCarDeposit())
                .refundAmount(eleDepositOrder.getPayAmount()).status(EleRefundOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId()).refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
                .build();
        eleRefundOrderService.insert(eleRefundOrder);
        
        //等到后台同意退款
        return R.ok();
        
    }
    
    /**
     * 根据型号计算押金
     *
     * @param franchisee
     * @param model
     * @param userInfo
     * @return
     */
    private Pair<Boolean, Object> caculDepositByFranchisee(Franchisee franchisee, Integer model, UserInfo userInfo) {
        //单型号
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            return Pair.of(Boolean.TRUE, franchisee.getBatteryDeposit());
        }

        //多型号押金
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(model)) {
                log.error("ELE DEPOSIT ERROR! illegal model,uid={},model={}", userInfo.getUid(), model);
                return Pair.of(Boolean.FALSE, null);
            }

            //型号押金
            List<Map> modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("ELE DEPOSIT ERROR! not found modelBatteryDepositList,uid={}", userInfo.getUid());
                return Pair.of(Boolean.FALSE, null);
            }

            //没看懂
            for (Map map : modelBatteryDepositList) {
                if ((double) (map.get("model")) - model < 1 && (double) (map.get("model")) - model >= 0) {
//                    payAmount = BigDecimal.valueOf((double) map.get("batteryDeposit"));
//                    break;
                    return Pair.of(Boolean.TRUE, BigDecimal.valueOf((double) map.get("batteryDeposit")));
                }
            }
        }

        return Pair.of(Boolean.FALSE, null);
    }
}
