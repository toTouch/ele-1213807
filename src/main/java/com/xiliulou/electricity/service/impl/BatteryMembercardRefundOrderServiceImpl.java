package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.BatteryMembercardRefundOrderMapper;
import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMembercardRefundOrderDetailVO;
import com.xiliulou.electricity.vo.BatteryMembercardRefundOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (BatteryMembercardRefundOrder)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-12 15:56:43
 */
@Service("batteryMembercardRefundOrderService")
@Slf4j
public class BatteryMembercardRefundOrderServiceImpl implements BatteryMembercardRefundOrderService {
    @Resource
    private BatteryMembercardRefundOrderMapper batteryMembercardRefundOrderMapper;

    @Autowired
    EleRefundOrderService eleRefundOrderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    @Autowired
    UserOauthBindService userOauthBindService;

    @Autowired
    ElectricityMemberCardOrderService batteryMemberCardOrderService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;

    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;

    @Autowired
    UserCouponService userCouponService;

    @Autowired
    MemberCardBatteryTypeService memberCardBatteryTypeService;

    @Autowired
    UserBatteryTypeService userBatteryTypeService;

    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;

    @Autowired
    UnionTradeOrderService unionTradeOrderService;

    @Autowired
    WechatV3JsapiService wechatV3JsapiService;

    @Autowired
    WechatConfig wechatConfig;

    @Override
    public WechatJsapiRefundResultDTO handleRefundOrder(BatteryMembercardRefundOrder batteryMembercardRefundOrder, HttpServletRequest request) throws WechatPayException{

        //看不懂  抄的退电池押金 @See EleRefundOrderServiceImpl#commonCreateRefundOrder()
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(batteryMembercardRefundOrder.getMemberCardOrderNo());
        String tradeOrderNo = null;
        Integer total = null;
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("BATTERY MEMBERCARD REFUND ERROR!not found electricityTradeOrder,memberCardOrderNo={}", batteryMembercardRefundOrder.getMemberCardOrderNo());
            throw new CustomBusinessException("未找到交易订单!");
        }
        tradeOrderNo = electricityTradeOrder.getTradeOrderNo();
        total = batteryMembercardRefundOrder.getPayAmount().multiply(new BigDecimal(100)).intValue();

        if (Objects.nonNull(electricityTradeOrder.getParentOrderId())) {
            UnionTradeOrder unionTradeOrder = unionTradeOrderService.selectTradeOrderById(electricityTradeOrder.getParentOrderId());
            if (Objects.nonNull(unionTradeOrder)) {
                tradeOrderNo = unionTradeOrder.getTradeOrderNo();
                total = unionTradeOrder.getTotalFee().multiply(new BigDecimal(100)).intValue();
            }
        }

        //退款
        WechatV3RefundQuery wechatV3RefundQuery = new WechatV3RefundQuery();
        wechatV3RefundQuery.setTenantId(electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setTotal(total);
        wechatV3RefundQuery.setRefund(batteryMembercardRefundOrder.getRefundAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundQuery.setReason("退款");
        wechatV3RefundQuery.setOrderId(tradeOrderNo);
        wechatV3RefundQuery.setNotifyUrl(wechatConfig.getBatteryRentRefundCallBackUrl() + electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setCurrency("CNY");
        wechatV3RefundQuery.setRefundId(batteryMembercardRefundOrder.getRefundOrderNo());

        return wechatV3JsapiService.refund(wechatV3RefundQuery);
    }

    @Override
    public List<BatteryMembercardRefundOrderVO> selectByPage(BatteryMembercardRefundOrderQuery query) {
        List<BatteryMembercardRefundOrder> list = this.batteryMembercardRefundOrderMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.parallelStream().map(item -> {
            BatteryMembercardRefundOrderVO batteryMembercardRefundOrderVO = new BatteryMembercardRefundOrderVO();
            BeanUtils.copyProperties(item, batteryMembercardRefundOrderVO);

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            batteryMembercardRefundOrderVO.setName(Objects.isNull(userInfo) ? "" : userInfo.getName());

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMid());
            batteryMembercardRefundOrderVO.setMemberCardName(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
            batteryMembercardRefundOrderVO.setRentUnit(Objects.isNull(batteryMemberCard) ? null :batteryMemberCard.getRentUnit());
            batteryMembercardRefundOrderVO.setLimitCount(Objects.isNull(batteryMemberCard) ? null :batteryMemberCard.getLimitCount());

            return batteryMembercardRefundOrderVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectPageCount(BatteryMembercardRefundOrderQuery query) {
        return this.batteryMembercardRefundOrderMapper.selectPageCount(query);
    }

    @Override
    public BatteryMembercardRefundOrder queryByIdFromDB(Long id) {
        return this.batteryMembercardRefundOrderMapper.queryById(id);
    }

    @Override
    public BatteryMembercardRefundOrder queryByIdFromCache(Long id) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryMembercardRefundOrder batteryMembercardRefundOrder) {
        return this.batteryMembercardRefundOrderMapper.update(batteryMembercardRefundOrder);
    }

    @Override
    public Integer insert(BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert) {
        return this.batteryMembercardRefundOrderMapper.insert(batteryMembercardRefundOrderInsert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.batteryMembercardRefundOrderMapper.deleteById(id) > 0;
    }

    @Override
    public BatteryMembercardRefundOrder selectByRefundOrderNo(String orderNo) {
        return this.batteryMembercardRefundOrderMapper.selectOne(new LambdaQueryWrapper<BatteryMembercardRefundOrder>().eq(BatteryMembercardRefundOrder::getRefundOrderNo, orderNo));
    }

    @Override
    public BatteryMembercardRefundOrder selectByMembercardOrderNo(String orderNo) {
        return this.batteryMembercardRefundOrderMapper.selectOne(new LambdaQueryWrapper<BatteryMembercardRefundOrder>().eq(BatteryMembercardRefundOrder::getMemberCardOrderNo, orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> batteryMembercardRefund(String orderNo) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBERCARD_REFUND_LOCK_KEY + user.getUid(), "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        try {
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(electricityPayParams)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN!not found electricityPayParams,uid={}", user.getUid());
                return Triple.of(false, "", "未配置支付参数!");
            }

            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), TenantContextHolder.getTenantId());
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("BATTERY MEMBERCARD REFUND WARN!not found userOauthBind,uid={}", user.getUid());
                return Triple.of(false, "", "未找到用户的第三方授权信息!");
            }

            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found userInfo,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }

            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! user not auth,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }

            if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! user not deposit,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0049", "未缴纳电池押金");
            }

            ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderNo);
            if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,uid={},orderNo={}", user.getUid(), orderNo);
                return Triple.of(false, "100281", "电池套餐订单不存在");
            }

            if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USED)) {
                return Triple.of(false, "100285", "电池套餐已使用");
            }

            //如果套餐使用中,判断是否有未使用的套餐
            if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING) && CollectionUtils.isNotEmpty(userBatteryMemberCardPackageService.selectByUid(userInfo.getUid()))) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! user has not used membercard,uid={}", user.getUid());
                return Triple.of(false, "100290", "请先退租未使用的套餐");
            }

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found batteryMemberCard,uid={},mid={}", user.getUid(), electricityMemberCardOrder.getMemberCardId());
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }

            if (!Objects.equals(BatteryMemberCard.YES, batteryMemberCard.getIsRefund())) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not allow refund,uid={},mid={}", user.getUid(), electricityMemberCardOrder.getMemberCardId());
                return Triple.of(false, "100286", "电池套餐不允许退租");
            }

            //是否超过套餐退租时间
            if (System.currentTimeMillis() > electricityMemberCardOrder.getCreateTime() + batteryMemberCard.getRefundLimit() * 24 * 60 * 60 * 1000) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not allow refund,uid={},mid={}", user.getUid(), electricityMemberCardOrder.getMemberCardId());
                return Triple.of(false, "100287", "电池套餐订单已超过退租时间");
            }

            BatteryMembercardRefundOrder batteryMembercardRefundOrder = this.selectByMembercardOrderNo(orderNo);
            if (Objects.nonNull(batteryMembercardRefundOrder) && Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_INIT)) {
                return Triple.of(false, "100282", "电池套餐退租审核中");
            }

            if (Objects.nonNull(batteryMembercardRefundOrder) && Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_SUCCESS)) {
                return Triple.of(false, "100283", "电池套餐已退租");
            }

            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", user.getUid());
                return Triple.of(false, "100247", "用户信息不存在");
            }

            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", user.getUid());
                return Triple.of(false, "100289", "电池套餐暂停中");
            }

            if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.nonNull(userBatteryMemberCardPackageService.checkUserBatteryMemberCardPackageByUid(userInfo.getUid()))) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! exist not use batteryMemberCard,uid={}", userInfo.getUid());
                return Triple.of(false, "100296", "请先退未使用的套餐");
            }

            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.isNull(serviceFeeUserInfo)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found serviceFeeUserInfo,uid={}", user.getUid());
                return Triple.of(false, "100247", "用户信息不存在");
            }

            Triple<Boolean, Integer, BigDecimal> checkUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
            if (Boolean.TRUE.equals(checkUserBatteryServiceFeeResult.getLeft())) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! user exit battery service fee,uid={}", user.getUid());
                return Triple.of(false, "100220", "用户存在电池服务费");
            }

            if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not return battery,uid={}", user.getUid());
                return Triple.of(false, "100284", "未归还电池");
            }

            BigDecimal refundAmount = calculateRefundAmount(userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
            if (refundAmount.compareTo(electricityMemberCardOrder.getPayAmount()) > 0) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! refundAmount illegal,refundAmount={},uid={}", refundAmount.doubleValue(), user.getUid());
                return Triple.of(false, "100294", "退租金额不合法");
            }

            BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderInsert.setUid(userInfo.getUid());
            batteryMembercardRefundOrderInsert.setPhone(userInfo.getPhone());
            batteryMembercardRefundOrderInsert.setMid(electricityMemberCardOrder.getMemberCardId());
            batteryMembercardRefundOrderInsert.setRefundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.REFUND_BATTERY_MEMBERCARD, userInfo.getUid()));
            batteryMembercardRefundOrderInsert.setMemberCardOrderNo(electricityMemberCardOrder.getOrderId());
            batteryMembercardRefundOrderInsert.setPayAmount(electricityMemberCardOrder.getPayAmount());
            batteryMembercardRefundOrderInsert.setRefundAmount(refundAmount);
            batteryMembercardRefundOrderInsert.setPayType(electricityMemberCardOrder.getPayType());
            batteryMembercardRefundOrderInsert.setStatus(BatteryMembercardRefundOrder.STATUS_REFUND);
            batteryMembercardRefundOrderInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            batteryMembercardRefundOrderInsert.setStoreId(electricityMemberCardOrder.getStoreId());
            batteryMembercardRefundOrderInsert.setTenantId(electricityMemberCardOrder.getTenantId());
            batteryMembercardRefundOrderInsert.setCreateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderInsert.setUpdateTime(System.currentTimeMillis());
            assignOtherAttr(batteryMembercardRefundOrderInsert, userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);

            this.insert(batteryMembercardRefundOrderInsert);

            ElectricityMemberCardOrder electricityMemberCardOrderUpdate=new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
            electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.USE_STATUS_REFUNDING);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdate);

            //若套餐使用中,发起退租不能再使用
            if (Objects.equals(orderNo, userBatteryMemberCard.getOrderId())) {
                userBatteryMemberCard.setUid(userInfo.getUid());
                userBatteryMemberCard.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_REFUND);
                userBatteryMemberCard.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardService.updateByUid(userBatteryMemberCard);
            }

        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBERCARD_REFUND_LOCK_KEY + user.getUid());
        }

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> batteryMembercardRefundForAdmin(String orderNo, HttpServletRequest request) {
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderNo);
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,orderNo={}", orderNo);
            return Triple.of(false, "100281", "电池套餐订单不存在");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userInfo,uid={}", electricityMemberCardOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if(Objects.isNull(userBatteryMemberCard)){
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }

        if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.nonNull(userBatteryMemberCardPackageService.checkUserBatteryMemberCardPackageByUid(userInfo.getUid()))) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! exist not use batteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100296", "请先退未使用的套餐");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), electricityMemberCardOrder.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }

        BigDecimal refundAmount = calculateRefundAmount(userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
        if (refundAmount.compareTo(electricityMemberCardOrder.getPayAmount()) > 0) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! refundAmount illegal,refundAmount={},uid={}", refundAmount.doubleValue(), userInfo.getUid());
            return Triple.of(false, "100294", "退租金额不合法");
        }

        BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert = new BatteryMembercardRefundOrder();
        batteryMembercardRefundOrderInsert.setUid(userInfo.getUid());
        batteryMembercardRefundOrderInsert.setPhone(userInfo.getPhone());
        batteryMembercardRefundOrderInsert.setMid(electricityMemberCardOrder.getMemberCardId());
        batteryMembercardRefundOrderInsert.setRefundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.REFUND_BATTERY_MEMBERCARD, userInfo.getUid()));
        batteryMembercardRefundOrderInsert.setMemberCardOrderNo(electricityMemberCardOrder.getOrderId());
        batteryMembercardRefundOrderInsert.setPayAmount(electricityMemberCardOrder.getPayAmount());
        batteryMembercardRefundOrderInsert.setRefundAmount(refundAmount);
        batteryMembercardRefundOrderInsert.setPayType(electricityMemberCardOrder.getPayType());
        batteryMembercardRefundOrderInsert.setStatus(BatteryMembercardRefundOrder.STATUS_INIT);
        batteryMembercardRefundOrderInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        batteryMembercardRefundOrderInsert.setStoreId(electricityMemberCardOrder.getStoreId());
        batteryMembercardRefundOrderInsert.setTenantId(electricityMemberCardOrder.getTenantId());
        batteryMembercardRefundOrderInsert.setCreateTime(System.currentTimeMillis());
        batteryMembercardRefundOrderInsert.setUpdateTime(System.currentTimeMillis());
        assignOtherAttr(batteryMembercardRefundOrderInsert, userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);

        this.insert(batteryMembercardRefundOrderInsert);

        ElectricityMemberCardOrder electricityMemberCardOrderUpdate=new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
        electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.USE_STATUS_REFUNDING);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdate);

        //若套餐使用中,发起退租不能再使用
        if (Objects.equals(orderNo, userBatteryMemberCard.getOrderId())) {
            userBatteryMemberCard.setUid(userInfo.getUid());
            userBatteryMemberCard.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_REFUND);
            userBatteryMemberCard.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCard);
        }

        if (Objects.equals(electricityMemberCardOrder.getPayType(), ElectricityMemberCardOrder.OFFLINE_PAYMENT) || batteryMembercardRefundOrderInsert.getRefundAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return handleBatteryOfflineRefundOrder(userBatteryMemberCard, batteryMembercardRefundOrderInsert, electricityMemberCardOrder, userInfo);
        } else {
            return handleBatteryOnlineRefundOrder(batteryMembercardRefundOrderInsert, electricityMemberCardOrder, request);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> batteryMembercardRefundAudit(String refundOrderNo, String msg, Integer status, HttpServletRequest request) {
        BatteryMembercardRefundOrder batteryMembercardRefundOrder = this.batteryMembercardRefundOrderMapper.selectOne(new LambdaQueryWrapper<BatteryMembercardRefundOrder>().eq(BatteryMembercardRefundOrder::getRefundOrderNo, refundOrderNo)
                .eq(BatteryMembercardRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                .in(BatteryMembercardRefundOrder::getStatus, BatteryMembercardRefundOrder.STATUS_INIT, BatteryMembercardRefundOrder.STATUS_REFUSE_REFUND, BatteryMembercardRefundOrder.STATUS_FAIL));
        if (Objects.isNull(batteryMembercardRefundOrder)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found batteryMembercardRefundOrder,refoundOrderNo={}", refundOrderNo);
            return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(batteryMembercardRefundOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userInfo,uid={}", batteryMembercardRefundOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if(Objects.isNull(userBatteryMemberCard)){
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(batteryMembercardRefundOrder.getMemberCardOrderNo());
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,uid={},orderNo={}", userInfo.getUid(), batteryMembercardRefundOrder.getMemberCardOrderNo());
            return Triple.of(false, "100281", "电池套餐订单不存在");
        }

        //拒绝退款
        if (Objects.equals(status, BatteryMembercardRefundOrder.STATUS_REFUSE_REFUND)) {
            BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
            batteryMembercardRefundOrderUpdate.setMsg(msg);
            batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_REFUSE_REFUND);
            this.update(batteryMembercardRefundOrderUpdate);
            return Triple.of(true, "", null);
        }

        if (Objects.equals(electricityMemberCardOrder.getPayType(), ElectricityMemberCardOrder.OFFLINE_PAYMENT) || batteryMembercardRefundOrder.getRefundAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return handleBatteryOfflineRefundOrder( userBatteryMemberCard, batteryMembercardRefundOrder, electricityMemberCardOrder, userInfo);
        } else {
            return handleBatteryOnlineRefundOrder(batteryMembercardRefundOrder, electricityMemberCardOrder, request);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleBatteryOnlineRefundOrder(BatteryMembercardRefundOrder batteryMembercardRefundOrder, ElectricityMemberCardOrder electricityMemberCardOrder, HttpServletRequest request) {
        BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
        batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
        batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());

        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());

        try {
            this.handleRefundOrder(batteryMembercardRefundOrder, request);

            batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_REFUND);
            this.update(batteryMembercardRefundOrderUpdate);

            electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_REFUNDING);
            batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);

            return Triple.of(true, "", null);
        } catch (WechatPayException e) {
            log.error("BATTERY MEMBERCARD REFUND ERROR! wechat v3 refund error! ", e);
        }

        batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_FAIL);
        this.update(batteryMembercardRefundOrderUpdate);

        electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_REFUND_FAIL);
        batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);

        return Triple.of(false, "ELECTRICITY.00100", "退租失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleBatteryOfflineRefundOrder(UserBatteryMemberCard userBatteryMemberCard, BatteryMembercardRefundOrder batteryMembercardRefundOrder, ElectricityMemberCardOrder electricityMemberCardOrder, UserInfo userInfo) {

        if (Objects.equals(userBatteryMemberCard.getOrderId(), electricityMemberCardOrder.getOrderId())) {
            //使用中
            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
        }else{
            //未使用
            userBatteryMemberCardService.deductionExpireTime(userInfo.getUid(), electricityMemberCardOrder.getValidDays().longValue(), System.currentTimeMillis());
            userBatteryMemberCardPackageService.deleteByOrderId(electricityMemberCardOrder.getOrderId());
        }

        BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
        batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
        batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_SUCCESS);
        batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(batteryMembercardRefundOrderUpdate);

        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_REFUND);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);

        return Triple.of(true, "", null);
    }

    @Override
    public Triple<Boolean, String, Object> batteryMembercardRefundOrderDetail(String orderNo, Integer confirm) {
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderNo);
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,orderNo={}", orderNo);
            return Triple.of(false, "100281", "电池套餐订单不存在");
        }

        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USED)) {
            return Triple.of(false, "100285", "电池套餐已使用");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100001", "未能找到用户");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), electricityMemberCardOrder.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }

        //若退用户最后一个套餐用户绑定有资产提示先归还资产再退租金
        if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not return battery,uid={}", userInfo.getUid());
            return Triple.of(false, "100295", "请先归还资产再退租金");
        }

        BigDecimal refundAmount = calculateRefundAmount(userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
        if (refundAmount.compareTo(electricityMemberCardOrder.getPayAmount()) > 0) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! refundAmount illegal,refundAmount={},uid={}", refundAmount.doubleValue(), userInfo.getUid());
            return Triple.of(false, "100294", "退租金额不合法");
        }

        //校验套餐赠送的优惠券
        if (Objects.nonNull(confirm)) {
            UserCoupon userCoupon = userCouponService.selectBySourceOrderId(electricityMemberCardOrder.getOrderId());
            if (Objects.nonNull(userCoupon) && (Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_DESTRUCTION) || Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_USED))) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! battery memberCard binding coupon already used,uid={}", userInfo.getUid());
                return Triple.of(false, "100291", "套餐绑定的优惠券已使用，无法退租");
            }
        }

        BatteryMembercardRefundOrderDetailVO refundOrderDetailVO = new BatteryMembercardRefundOrderDetailVO();
        refundOrderDetailVO.setPayAmount(electricityMemberCardOrder.getPayAmount());
        refundOrderDetailVO.setRefundAmount(refundAmount);
        refundOrderDetailVO.setRentUnit(batteryMemberCard.getRentUnit());
        refundOrderDetailVO.setLimitCount(batteryMemberCard.getLimitCount());
        assignOtherAttr(refundOrderDetailVO, userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);

        return Triple.of(true, null, refundOrderDetailVO);
    }

    private void assignOtherAttr(BatteryMembercardRefundOrderDetailVO refundOrderDetailVO, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder electricityMemberCardOrder) {
        //未使用
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_NON)) {
            UserBatteryMemberCardPackage userBatteryMemberCardPackage = userBatteryMemberCardPackageService.selectByOrderNo(electricityMemberCardOrder.getOrderId());
            if (Objects.isNull(userBatteryMemberCardPackage)) {
                return;
            }

            refundOrderDetailVO.setRemainingNumber(userBatteryMemberCardPackage.getRemainingNumber());
            refundOrderDetailVO.setRemainingTime(Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? userBatteryMemberCardPackage.getMemberCardExpireTime() / 24 / 60 / 60 / 1000 : userBatteryMemberCardPackage.getMemberCardExpireTime() / 60 / 1000);
        }

        //使用中
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
            refundOrderDetailVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
            refundOrderDetailVO.setRemainingTime(Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? userBatteryMemberCard.getMemberCardExpireTime() / 24 / 60 / 60 / 1000 : userBatteryMemberCard.getMemberCardExpireTime() / 60 / 1000);
        }
    }

    private void assignOtherAttr(BatteryMembercardRefundOrder batteryMembercardRefundOrder, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder electricityMemberCardOrder) {
        //未使用
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_NON)) {
            UserBatteryMemberCardPackage userBatteryMemberCardPackage = userBatteryMemberCardPackageService.selectByOrderNo(electricityMemberCardOrder.getOrderId());
            if (Objects.isNull(userBatteryMemberCardPackage)) {
                return;
            }

            batteryMembercardRefundOrder.setRemainingNumber(userBatteryMemberCardPackage.getRemainingNumber());
            batteryMembercardRefundOrder.setRemainingTime(Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? userBatteryMemberCardPackage.getMemberCardExpireTime() / 24 / 60 / 60 / 1000 : userBatteryMemberCardPackage.getMemberCardExpireTime() / 60 / 1000);
        }

        //使用中
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
            batteryMembercardRefundOrder.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
            batteryMembercardRefundOrder.setRemainingTime(Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? userBatteryMemberCard.getMemberCardExpireTime() / 24 / 60 / 60 / 1000 : userBatteryMemberCard.getMemberCardExpireTime() / 60 / 1000);
        }
    }

    private BigDecimal calculateRefundAmount(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder electricityMemberCardOrder) {
        BigDecimal result = BigDecimal.valueOf(0);

        //未使用
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_NON)) {
            result = electricityMemberCardOrder.getPayAmount();
        }

        //使用中
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
            if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.YES)) {
                //限次
                result = batteryMemberCard.getRentPriceUnit().multiply(BigDecimal.valueOf(electricityMemberCardOrder.getMaxUseCount() - userBatteryMemberCard.getOrderRemainingNumber()));
            } else {
                //不限次
                long usedTime = System.currentTimeMillis() - userBatteryMemberCard.getOrderEffectiveTime();
                if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
                    result = batteryMemberCard.getRentPriceUnit().multiply(BigDecimal.valueOf(Math.ceil(usedTime / 1000.0 / 60 / 60 / 24)));
                } else {
                    result = batteryMemberCard.getRentPriceUnit().multiply(BigDecimal.valueOf(Math.ceil(usedTime / 1000.0 / 60)));
                }
            }
        }

        if (result.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            result = BigDecimal.ZERO;
        }

        return result;
    }

}
