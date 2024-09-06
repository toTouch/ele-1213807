package com.xiliulou.electricity.service.impl;
import com.xiliulou.electricity.bo.base.BasePayConfig;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.converter.PayConfigConverter;
import com.xiliulou.electricity.converter.model.OrderRefundParamConverterModel;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.entity.MqNotifyCommon;
import com.xiliulou.electricity.entity.RentRefundAuditMessageNotify;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.CheckPayParamsResultEnum;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.RefundPayOptTypeEnum;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.event.publish.SiteMessagePublish;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.BatteryMembercardRefundOrderMapper;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.BatteryMemberCardMerchantRebate;
import com.xiliulou.electricity.query.BatteryMembercardRefundOrderQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.DivisionAccountRecordService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.ttl.ChannelSourceContextHolder;
import com.xiliulou.electricity.tx.BatteryMembercardRefundOrderTxService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.BatteryMembercardRefundOrderDetailVO;
import com.xiliulou.electricity.vo.BatteryMembercardRefundOrderVO;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.base.PayServiceDispatcher;
import com.xiliulou.pay.base.dto.BasePayOrderRefundDTO;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.weixinv3.v2.service.WechatV3JsapiInvokeService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    PayConfigBizService payConfigBizService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    ElectricityMemberCardOrderService batteryMemberCardOrderService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private BatteryMembercardRefundOrderTxService batteryMembercardRefundOrderTxService;
    
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
    WechatConfig wechatConfig;
    
    @Resource
    private PayConfigConverter payConfigConverter;
    
    @Autowired
    DivisionAccountRecordService divisionAccountRecordService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    @Autowired
    RocketMqService rocketMqService;
    
    @Autowired
    UserInfoExtraService userInfoExtraService;
    
    @Autowired
    private FranchiseeServiceImpl franchiseeService;
    
    
    @Resource
    private PayServiceDispatcher payServiceDispatcher;
    
    
    @Resource
    private ApplicationContext applicationContext;
    
    @Resource
    private TenantService tenantService;
    
    @Autowired
    private SiteMessagePublish siteMessagePublish;
    
    @Override
    public BasePayOrderRefundDTO handleRefundOrderV2(BatteryMembercardRefundOrder batteryMembercardRefundOrder, BasePayConfig basePayConfig,
            HttpServletRequest request) throws PayException {
        // 看不懂  抄的退电池押金 @See EleRefundOrderServiceImpl#commonCreateRefundOrder()
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
    
        OrderRefundParamConverterModel model = new OrderRefundParamConverterModel();
    
    
    
        // 退款
        model.setRefundId(batteryMembercardRefundOrder.getRefundOrderNo());
        model.setOrderId(tradeOrderNo);
        model.setReason("退款");
        model.setRefund(batteryMembercardRefundOrder.getRefundAmount());
        model.setPayConfig(basePayConfig);
        model.setTotal(total);
        model.setRefundType(RefundPayOptTypeEnum.BATTERY_RENT_REFUND_CALL_BACK.getCode());
        model.setTenantId(basePayConfig.getTenantId());
        model.setFranchiseeId(basePayConfig.getFranchiseeId());
        BasePayRequest basePayRequest = payConfigConverter
                .converterOrderRefund(model);
    
        log.info("WECHAT INFO! wechatv3 refund query={}", JsonUtil.toJson(basePayRequest));
        return payServiceDispatcher.refund(basePayRequest);
        
    }
    
    @Override
    @Slave
    public List<BatteryMembercardRefundOrderVO> selectByPage(BatteryMembercardRefundOrderQuery query) {
        List<BatteryMembercardRefundOrder> list = this.batteryMembercardRefundOrderMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list.parallelStream().map(item -> {
            BatteryMembercardRefundOrderVO batteryMembercardRefundOrderVO = new BatteryMembercardRefundOrderVO();
            BeanUtils.copyProperties(item, batteryMembercardRefundOrderVO);
            
            UserInfo userInfo = userInfoService.queryByUidFromDb(item.getUid());
            batteryMembercardRefundOrderVO.setName(Objects.isNull(userInfo) ? "" : userInfo.getName());
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMid());
            batteryMembercardRefundOrderVO.setMemberCardName(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
            batteryMembercardRefundOrderVO.setRentUnit(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentUnit());
            batteryMembercardRefundOrderVO.setLimitCount(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getLimitCount());
            batteryMembercardRefundOrderVO.setRentPriceUnit(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentPriceUnit());
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            batteryMembercardRefundOrderVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
            
            return batteryMembercardRefundOrderVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Slave
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
    public Integer update(BatteryMembercardRefundOrder batteryMembercardRefundOrder) {
        return this.batteryMembercardRefundOrderMapper.update(batteryMembercardRefundOrder);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return this.batteryMembercardRefundOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    @Override
    public Integer insert(BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert) {
        return this.batteryMembercardRefundOrderMapper.insert(batteryMembercardRefundOrderInsert);
    }
    
    @Override
    public Boolean deleteById(Long id) {
        return this.batteryMembercardRefundOrderMapper.deleteById(id) > 0;
    }
    
    @Override
    public BatteryMembercardRefundOrder selectByRefundOrderNo(String orderNo) {
        return this.batteryMembercardRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<BatteryMembercardRefundOrder>().eq(BatteryMembercardRefundOrder::getRefundOrderNo, orderNo));
    }
    
    @Override
    public BatteryMembercardRefundOrder selectLatestByMembercardOrderNo(String orderNo) {
        return this.batteryMembercardRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<BatteryMembercardRefundOrder>().eq(BatteryMembercardRefundOrder::getMemberCardOrderNo, orderNo)
                        .orderByDesc(BatteryMembercardRefundOrder::getId).last("limit 0,1"));
    }
    
    @Override
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
            UserOauthBind userOauthBind = userOauthBindService.queryByUidAndTenantAndChannel(user.getUid(), TenantContextHolder.getTenantId(), ChannelSourceContextHolder.get());
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
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.warn("ELE DEPOSIT WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
            }
            
            // 是否有正在进行中的退押
            Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            if (refundCount > 0) {
                log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0047", "电池押金退款中");
            }
            
            ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderNo);
            if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,uid={},orderNo={}", user.getUid(), orderNo);
                return Triple.of(false, "100281", "电池套餐订单不存在");
            }
            
            if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_EXPIRE)) {
                return Triple.of(false, "100285", "电池套餐已失效");
            }
            
            List<UserCoupon> userCoupons = userCouponService.selectListBySourceOrderId(electricityMemberCardOrder.getOrderId());
            if (!CollectionUtils.isEmpty(userCoupons)) {
                for (UserCoupon userCoupon : userCoupons) {
                    if (Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_DESTRUCTION) || Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_USED)) {
                        log.warn("BATTERY MEMBERCARD REFUND WARN! battery memberCard binding coupon already used,uid={}", userInfo.getUid());
                        return Triple.of(false, "100291", "套餐绑定的优惠券已使用，无法退租");
                    }
                }
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
            
            // 是否超过套餐退租时间
            if (System.currentTimeMillis() > electricityMemberCardOrder.getCreateTime() + batteryMemberCard.getRefundLimit() * 24 * 60 * 60 * 1000) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not allow refund,uid={},mid={}", user.getUid(), electricityMemberCardOrder.getMemberCardId());
                return Triple.of(false, "100287", "电池套餐订单已超过退租时间");
            }
            
            BatteryMembercardRefundOrder batteryMembercardRefundOrder = this.selectLatestByMembercardOrderNo(orderNo);
            if (Objects.nonNull(batteryMembercardRefundOrder) && Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_AUDIT)) {
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
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.equals(
                    userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", user.getUid());
                return Triple.of(false, "100289", "电池套餐暂停中");
            }
            
            if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.nonNull(
                    userBatteryMemberCardPackageService.checkUserBatteryMemberCardPackageByUid(userInfo.getUid()))) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! exist not use batteryMemberCard,uid={}", userInfo.getUid());
                return Triple.of(false, "100296", "请先退未使用的套餐");
            }
            
            if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! user batteryMemberCard already expire,uid={}", userInfo.getUid());
                return Triple.of(false, "100374", "换电套餐已过期");
            }
            
            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.isNull(serviceFeeUserInfo)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not found serviceFeeUserInfo,uid={}", user.getUid());
                return Triple.of(false, "100247", "用户信息不存在");
            }
            
            Triple<Boolean, Integer, BigDecimal> checkUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                    batteryMemberCard, serviceFeeUserInfo);
            if (Boolean.TRUE.equals(checkUserBatteryServiceFeeResult.getLeft())) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! user exit battery service fee,uid={}", user.getUid());
                return Triple.of(false, "100220", "用户存在电池服务费");
            }
            
            List<UserBatteryMemberCardPackage> userBatteryMemberCardPackages = userBatteryMemberCardPackageService.selectByUid(userInfo.getUid());
            if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)
                    && CollectionUtils.isEmpty(userBatteryMemberCardPackages)) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! not return battery,uid={}", user.getUid());
                return Triple.of(false, "100284", "未归还电池");
            }
            
            BigDecimal refundAmount = calculateRefundAmount(userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
            if (refundAmount.compareTo(electricityMemberCardOrder.getPayAmount()) > 0) {
                log.warn("BATTERY MEMBERCARD REFUND WARN! refundAmount illegal,refundAmount={},uid={}", refundAmount.doubleValue(), user.getUid());
                return Triple.of(false, "100294", "退租金额不合法");
            }
            String generateBusinessOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.REFUND_BATTERY_MEMBERCARD, userInfo.getUid());
            BatteryMembercardRefundOrder batteryMembercardRefundOrderInsert = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderInsert.setUid(userInfo.getUid());
            batteryMembercardRefundOrderInsert.setPhone(userInfo.getPhone());
            batteryMembercardRefundOrderInsert.setMid(electricityMemberCardOrder.getMemberCardId());
            batteryMembercardRefundOrderInsert.setRefundOrderNo(generateBusinessOrderId);
            batteryMembercardRefundOrderInsert.setMemberCardOrderNo(electricityMemberCardOrder.getOrderId());
            batteryMembercardRefundOrderInsert.setPayAmount(electricityMemberCardOrder.getPayAmount());
            batteryMembercardRefundOrderInsert.setRefundAmount(refundAmount);
            batteryMembercardRefundOrderInsert.setPayType(electricityMemberCardOrder.getPayType());
            batteryMembercardRefundOrderInsert.setStatus(BatteryMembercardRefundOrder.STATUS_AUDIT);
            batteryMembercardRefundOrderInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            batteryMembercardRefundOrderInsert.setStoreId(electricityMemberCardOrder.getStoreId());
            batteryMembercardRefundOrderInsert.setTenantId(electricityMemberCardOrder.getTenantId());
            batteryMembercardRefundOrderInsert.setCreateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderInsert.setUpdateTime(System.currentTimeMillis());
//            batteryMembercardRefundOrderInsert.setPaymentChannel(electricityMemberCardOrder.getPaymentChannel());
            assignOtherAttr(batteryMembercardRefundOrderInsert, userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
            
            this.insert(batteryMembercardRefundOrderInsert);
            
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
            electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_AUDIT);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdate);
            
            // 发送退租审核通知
            sendAuditNotify(userInfo);
            // 发送站内信
            siteMessagePublish.publish(
                    SiteMessageEvent.builder(this).tenantId(TenantContextHolder.getTenantId().longValue()).code(SiteMessageType.REPLACING_THE_BATTERY_AND_TERMINATING_THE_LEASE)
                            .notifyTime(System.currentTimeMillis()).addContext("name", userInfo.getName()).addContext("phone", userInfo.getPhone())
                            .addContext("refundOrderNo", generateBusinessOrderId).build());
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBERCARD_REFUND_LOCK_KEY + user.getUid());
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> batteryMembercardRefundForAdmin(String orderNo, BigDecimal refundAmount, HttpServletRequest request, Integer offlineRefund) {
        
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderNo);
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,orderNo={}", orderNo);
            return Triple.of(false, "100281", "电池套餐订单不存在");
        }
        
        List<UserCoupon> userCoupons = userCouponService.selectListBySourceOrderId(electricityMemberCardOrder.getOrderId());
        if (!CollectionUtils.isEmpty(userCoupons)) {
            for (UserCoupon userCoupon : userCoupons) {
                if (Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_DESTRUCTION) || Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_USED)) {
                    log.warn("BATTERY MEMBERCARD REFUND WARN! battery memberCard binding coupon already used,uid={}", electricityMemberCardOrder.getUid());
                    return Triple.of(false, "100291", "套餐绑定的优惠券已使用，无法退租");
                }
            }
        }
        
        BatteryMembercardRefundOrder batteryMembercardRefundOrder = applicationContext.getBean(BatteryMembercardRefundOrderService.class).selectLatestByMembercardOrderNo(orderNo);
        if (Objects.nonNull(batteryMembercardRefundOrder)) {
            if (Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_SUCCESS)) {
                return Triple.of(false, "", "电池套餐订单已退款");
            }
            
            if (Objects.equals(batteryMembercardRefundOrder.getStatus(), BatteryMembercardRefundOrder.STATUS_AUDIT)) {
                return Triple.of(false, "", "电池套餐订单退款审核中");
            }
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userInfo,uid={}", electricityMemberCardOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryDeposit,uid={}", electricityMemberCardOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "用户信息不存在");
        }
        
        // 是否有正在进行中的退押
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0047", "电池押金退款中");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.nonNull(
                userBatteryMemberCardPackageService.checkUserBatteryMemberCardPackageByUid(userInfo.getUid()))) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! exist not use batteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100296", "请先退未使用的套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! user batteryMemberCard already expire,uid={}", userInfo.getUid());
            return Triple.of(false, "100374", "换电套餐已过期");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100289", "电池套餐暂停中");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrder.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), electricityMemberCardOrder.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        Triple<Boolean, Integer, BigDecimal> checkUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(checkUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! user exit battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "100220", "用户存在电池服务费");
        }
        
        // 是否超过套餐退租时间
        if (System.currentTimeMillis() > electricityMemberCardOrder.getCreateTime() + batteryMemberCard.getRefundLimit() * 24 * 60 * 60 * 1000) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not allow refund,uid={},mid={}", userInfo.getUid(), electricityMemberCardOrder.getMemberCardId());
            return Triple.of(false, "100287", "电池套餐订单已超过退租时间");
        }
        
        List<UserBatteryMemberCardPackage> userBatteryMemberCardPackages = userBatteryMemberCardPackageService.selectByUid(userInfo.getUid());
        if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)
                && CollectionUtils.isEmpty(userBatteryMemberCardPackages)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not return battery,uid={}", userInfo.getUid());
            return Triple.of(false, "100284", "未归还电池");
        }
        
        //        BigDecimal refundAmount = calculateRefundAmount(userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
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
        
        // 若传递强制线下退款标识，将退款类型修改为线下退款
        batteryMembercardRefundOrderInsert.setPayType(
                Objects.equals(offlineRefund, CheckPayParamsResultEnum.FAIL.getCode()) ? ElectricityMemberCardOrder.OFFLINE_PAYMENT : electricityMemberCardOrder.getPayType());
        
        batteryMembercardRefundOrderInsert.setStatus(BatteryMembercardRefundOrder.STATUS_INIT);
        batteryMembercardRefundOrderInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        batteryMembercardRefundOrderInsert.setStoreId(electricityMemberCardOrder.getStoreId());
        batteryMembercardRefundOrderInsert.setTenantId(electricityMemberCardOrder.getTenantId());
        batteryMembercardRefundOrderInsert.setCreateTime(System.currentTimeMillis());
        batteryMembercardRefundOrderInsert.setUpdateTime(System.currentTimeMillis());
        assignOtherAttr(batteryMembercardRefundOrderInsert, userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
        
        applicationContext.getBean(BatteryMembercardRefundOrderService.class).insert(batteryMembercardRefundOrderInsert);
        
        if (Objects.equals(batteryMembercardRefundOrderInsert.getPayType(), ElectricityMemberCardOrder.OFFLINE_PAYMENT)
                || batteryMembercardRefundOrderInsert.getRefundAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return handleBatteryOfflineRefundOrder(userBatteryMemberCard, batteryMembercardRefundOrderInsert, electricityMemberCardOrder, userInfo, refundAmount, null);
        }
        
        // 后续操作与handleBatteryOnlineRefundOrder方法一致，由于this.insert(batteryMembercardRefundOrderInsert);未能回滚，暂时将逻辑复制到此处，待优化
        // TODO 待优化
        BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
        batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrderInsert.getId());
        batteryMembercardRefundOrderUpdate.setMsg(null);
        batteryMembercardRefundOrderUpdate.setRefundAmount(refundAmount);
        batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
    
        BasePayConfig basePayConfig = null;
        try {
            basePayConfig = payConfigBizService.queryPayParams(electricityMemberCardOrder.getPaymentChannel(),electricityMemberCardOrder.getTenantId(),
                    electricityMemberCardOrder.getParamFranchiseeId());
        }catch (PayException e) {
            log.warn("BATTERY DEPOSIT WARN!not found pay params,refundOrderNo={}", batteryMembercardRefundOrderInsert.getRefundOrderNo());
            throw new BizException("PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
        }
        if (Objects.isNull(basePayConfig)) {
            log.warn("BATTERY DEPOSIT WARN!not found pay params,refundOrderNo={}", batteryMembercardRefundOrderInsert.getRefundOrderNo());
            throw new BizException("100307", "未配置支付参数!");
        }
        
        try {
            batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_REFUND);
            batteryMembercardRefundOrderUpdate.setPaymentChannel(electricityMemberCardOrder.getPaymentChannel());
            batteryMembercardRefundOrderTxService.refund(electricityMemberCardOrderUpdate,batteryMembercardRefundOrderUpdate);
            
            applicationContext.getBean(BatteryMembercardRefundOrderService.class).handleRefundOrderV2(batteryMembercardRefundOrderInsert, basePayConfig, request);
            
            batteryMembercardRefundOrderInsert.setRefundAmount(refundAmount);
            
            
            return Triple.of(true, "", null);
        } catch (Exception e) {
            log.error("BATTERY MEMBERCARD REFUND ERROR! wechat v3 refund error! ", e);
        }
        
        batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_FAIL);
        batteryMembercardRefundOrderUpdate.setPaymentChannel(electricityMemberCardOrder.getPaymentChannel());
        applicationContext.getBean(BatteryMembercardRefundOrderService.class).update(batteryMembercardRefundOrderUpdate);
        
        electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_FAIL);
        batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        
        return Triple.of(false, "PAY_TRANSFER.0020", "支付调用失败，请检查相关配置");
    }
    
    @Override
    public Triple<Boolean, String, Object> batteryMembercardRefundAudit(String refundOrderNo, String msg, BigDecimal refundAmount, Integer status, HttpServletRequest request,
            Integer offlineRefund) {
        BatteryMembercardRefundOrder batteryMembercardRefundOrder = this.batteryMembercardRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<BatteryMembercardRefundOrder>().eq(BatteryMembercardRefundOrder::getRefundOrderNo, refundOrderNo)
                        .eq(BatteryMembercardRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .in(BatteryMembercardRefundOrder::getStatus, BatteryMembercardRefundOrder.STATUS_INIT, BatteryMembercardRefundOrder.STATUS_REFUSE_REFUND,
                                BatteryMembercardRefundOrder.STATUS_FAIL, BatteryMembercardRefundOrder.STATUS_AUDIT));
        if (Objects.isNull(batteryMembercardRefundOrder)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found batteryMembercardRefundOrder,refoundOrderNo={}", refundOrderNo);
            return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(batteryMembercardRefundOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userInfo,uid={}", batteryMembercardRefundOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(batteryMembercardRefundOrder.getMemberCardOrderNo());
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,uid={},orderNo={}", userInfo.getUid(),
                    batteryMembercardRefundOrder.getMemberCardOrderNo());
            return Triple.of(false, "100281", "电池套餐订单不存在");
        }
        
        // 拒绝退款
        if (Objects.equals(status, BatteryMembercardRefundOrder.STATUS_REFUSE_REFUND)) {
            BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
            batteryMembercardRefundOrderUpdate.setMsg(msg);
            batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_REFUSE_REFUND);
            batteryMembercardRefundOrderUpdate.setPaymentChannel(electricityMemberCardOrder.getPaymentChannel());
            this.update(batteryMembercardRefundOrderUpdate);
            
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_REFUSED);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
            return Triple.of(true, "", null);
        }
        
        if (refundAmount.compareTo(electricityMemberCardOrder.getPayAmount()) > 0) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! refundAmount illegal,refundAmount={},uid={}", refundAmount.doubleValue(), userInfo.getUid());
            return Triple.of(false, "100294", "退租金额不合法");
        }
        
        // 套餐是否过期
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
            batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
            batteryMembercardRefundOrderUpdate.setMsg(msg);
            batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_FAIL);
            batteryMembercardRefundOrderUpdate.setPaymentChannel(batteryMembercardRefundOrder.getPaymentChannel());
            this.update(batteryMembercardRefundOrderUpdate);
            
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_FAIL);
            electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
            
            return Triple.of(true, "", null);
        }
        
        // 套餐未过期，若支付配置校验未通过，强制线下退款，修改套餐退款订单的退款支付类型
        if (Objects.equals(offlineRefund, CheckPayParamsResultEnum.FAIL.getCode())) {
            batteryMembercardRefundOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
        }
        
        if (Objects.equals(batteryMembercardRefundOrder.getPayType(), ElectricityMemberCardOrder.OFFLINE_PAYMENT) || refundAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return handleBatteryOfflineRefundOrder(userBatteryMemberCard, batteryMembercardRefundOrder, electricityMemberCardOrder, userInfo, refundAmount, msg);
        } else {
            return handleBatteryOnlineRefundOrder(batteryMembercardRefundOrder, electricityMemberCardOrder, refundAmount, msg, request);
        }
    }
    
    public Triple<Boolean, String, Object> handleBatteryOnlineRefundOrder(BatteryMembercardRefundOrder batteryMembercardRefundOrder,
            ElectricityMemberCardOrder electricityMemberCardOrder, BigDecimal refundAmount, String msg, HttpServletRequest request) {
        BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
        batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
        batteryMembercardRefundOrderUpdate.setMsg(msg);
        batteryMembercardRefundOrderUpdate.setRefundAmount(refundAmount);
        batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
    
        BasePayConfig basePayConfig = null;
        try {
            basePayConfig = payConfigBizService.queryPayParams(electricityMemberCardOrder.getPaymentChannel(),electricityMemberCardOrder.getTenantId(),
                    electricityMemberCardOrder.getParamFranchiseeId());
        }catch (PayException e) {
            log.warn("BATTERY DEPOSIT WARN!not found pay params,refundOrderNo={}", batteryMembercardRefundOrder.getRefundOrderNo());
            return Triple.of(false, "PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
        }
        if (Objects.isNull(basePayConfig)) {
            log.warn("BATTERY DEPOSIT WARN!not found pay params,refundOrderNo={}", batteryMembercardRefundOrder.getRefundOrderNo());
            return Triple.of(false, "100307", "未配置支付参数!");
        }
        
        try {
            batteryMembercardRefundOrderUpdate.setPaymentChannel(electricityMemberCardOrder.getPaymentChannel());
            batteryMembercardRefundOrderTxService.refund(electricityMemberCardOrderUpdate,batteryMembercardRefundOrderUpdate);
            
            batteryMembercardRefundOrder.setRefundAmount(refundAmount);
            this.handleRefundOrderV2(batteryMembercardRefundOrder, basePayConfig, request);
            
            return Triple.of(true, "", null);
        } catch (Exception e) {
            log.error("BATTERY MEMBERCARD REFUND ERROR! wechat v3 refund error! ", e);
        }
        
        batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_FAIL);
        batteryMembercardRefundOrderUpdate.setPaymentChannel(electricityMemberCardOrder.getPaymentChannel());
        this.update(batteryMembercardRefundOrderUpdate);
        
        electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_FAIL);
        batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        
        return Triple.of(false, "PAY_TRANSFER.0020", "支付调用失败，请检查相关配置");
    }
    
    public Triple<Boolean, String, Object> handleBatteryOfflineRefundOrder(UserBatteryMemberCard userBatteryMemberCard, BatteryMembercardRefundOrder batteryMembercardRefundOrder,
            ElectricityMemberCardOrder electricityMemberCardOrder, UserInfo userInfo, BigDecimal refundAmount, String msg) {
        if (Objects.equals(userBatteryMemberCard.getOrderId(), electricityMemberCardOrder.getOrderId())) {
            // 使用中
            List<UserBatteryMemberCardPackage> userBatteryMemberCardPackages = userBatteryMemberCardPackageService.selectByUid(userBatteryMemberCard.getUid());
            if (CollectionUtils.isEmpty(userBatteryMemberCardPackages)) {
                // 退最后一个套餐
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                serviceFeeUserInfoService.unbindServiceFeeInfoByUid(userInfo.getUid());
            } else {
                UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setOrderRemainingNumber(NumberConstant.ZERO_L);
                userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() - userBatteryMemberCard.getOrderRemainingNumber());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(
                        userBatteryMemberCard.getMemberCardExpireTime() - (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()));
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
                
                ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCard.getUid());
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userBatteryMemberCard.getUid());
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(
                        serviceFeeUserInfo.getServiceFeeGenerateTime() - (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()));
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfo);
            }
        } else {
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(batteryMembercardRefundOrder.getMid());
            if (Objects.isNull(batteryMemberCard)) {
                log.error("ELE REFUND RENT ERROR!not found batteryMemberCard,mid={},refundOrderNo={}", batteryMembercardRefundOrder.getMid(),
                        batteryMembercardRefundOrder.getRefundOrderNo());
                return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
            }
            
            long deductionExpireTime = 0L;
            if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
                deductionExpireTime = electricityMemberCardOrder.getValidDays() * 24 * 60 * 60 * 1000L;
            } else {
                deductionExpireTime = electricityMemberCardOrder.getValidDays() * 60 * 1000L;
            }
            
            // 未使用
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() - electricityMemberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() - deductionExpireTime);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            userBatteryMemberCardPackageService.deleteByOrderId(electricityMemberCardOrder.getOrderId());
        }
        
        BatteryMembercardRefundOrder batteryMembercardRefundOrderUpdate = new BatteryMembercardRefundOrder();
        batteryMembercardRefundOrderUpdate.setId(batteryMembercardRefundOrder.getId());
        batteryMembercardRefundOrderUpdate.setStatus(BatteryMembercardRefundOrder.STATUS_SUCCESS);
        batteryMembercardRefundOrderUpdate.setRefundAmount(refundAmount);
        batteryMembercardRefundOrderUpdate.setMsg(msg);
        batteryMembercardRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        batteryMembercardRefundOrderUpdate.setPayType(batteryMembercardRefundOrder.getPayType());
        this.update(batteryMembercardRefundOrderUpdate);
        
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setRefundStatus(ElectricityMemberCardOrder.REFUND_STATUS_SUCCESS);
        electricityMemberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_REFUND);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateByID(electricityMemberCardOrderUpdate);
        
        // 更新套餐绑定的优惠券为已失效
        updateUserCouponStatus(electricityMemberCardOrder.getOrderId());
        
        // 8. 处理分账
        DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
        divisionAccountOrderDTO.setOrderNo(batteryMembercardRefundOrder.getRefundOrderNo());
        divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_REFUND.getCode());
        divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
        divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
        
        // 如果是线上支付，0元退租
        if (Objects.equals(electricityMemberCardOrder.getPayType(), ElectricityMemberCardOrder.ONLINE_PAYMENT)) {
            this.sendMerchantRebateRefundMQ(batteryMembercardRefundOrder.getUid(), batteryMembercardRefundOrder.getRefundOrderNo());
        }
        
        return Triple.of(true, "", null);
    }
    
    @Override
    public void sendMerchantRebateRefundMQ(Long uid, String orderId) {
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            log.warn("sendMerchantRebateRefundMQ is exception，userInfoExtra is null,uid={}", uid);
            return;
        }
        
        if (Objects.isNull(userInfoExtra.getMerchantId())) {
            log.warn("sendMerchantRebateRefundMQ is exception，merchantId is null,uid={}", uid);
            return;
        }
        
        BatteryMemberCardMerchantRebate merchantRebate = new BatteryMemberCardMerchantRebate();
        merchantRebate.setUid(uid);
        merchantRebate.setOrderId(orderId);
        merchantRebate.setType(MerchantConstant.TYPE_REFUND);
        // 续费成功  发送返利退费MQ
        rocketMqService.sendAsyncMsg(MqProducerConstant.BATTERY_MEMBER_CARD_MERCHANT_REBATE_TOPIC, JsonUtil.toJson(merchantRebate));
    }
    
    @Slave
    @Override
    public List<BatteryMembercardRefundOrder> selectRefundingOrderByUid(Long uid) {
        return this.batteryMembercardRefundOrderMapper.selectList(new LambdaQueryWrapper<BatteryMembercardRefundOrder>().eq(BatteryMembercardRefundOrder::getUid, uid)
                .in(BatteryMembercardRefundOrder::getStatus, BatteryMembercardRefundOrder.STATUS_AUDIT, BatteryMembercardRefundOrder.STATUS_REFUND));
    }
    
    @Slave
    @Override
    public BigDecimal selectUserTotalRefund(Integer tenantId, Long uid) {
        return Optional.ofNullable(batteryMembercardRefundOrderMapper.selectUserTotalRefund(tenantId, uid)).orElse(BigDecimal.ZERO);
    }
    
    @Override
    public void updateUserCouponStatus(String orderId) {
        List<UserCoupon> userCoupons = userCouponService.selectListBySourceOrderId(orderId);
        if (!CollectionUtils.isEmpty(userCoupons)) {
            userCoupons.forEach(userCoupon -> {
                UserCoupon userCouponUpdate = new UserCoupon();
                userCouponUpdate.setId(userCoupon.getId());
                userCouponUpdate.setStatus(UserCoupon.STATUS_IS_INVALID);
                userCouponUpdate.setUpdateTime(System.currentTimeMillis());
                userCouponService.update(userCouponUpdate);
            });
        }
    }
    
    @Override
    public void sendAuditNotify(UserInfo userInfo) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(userInfo.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.warn("ELE RENT REFUND WARN! not found maintenanceUserNotifyConfig,tenantId={}", userInfo.getTenantId());
            return;
        }
        
        if ((notifyConfig.getPermissions() & MaintenanceUserNotifyConfig.TYPE_RENT_REFUND) != MaintenanceUserNotifyConfig.TYPE_RENT_REFUND) {
            return;
        }
        
        List<String> phones = JsonUtil.fromJsonArray(notifyConfig.getPhones(), String.class);
        if (CollectionUtils.isEmpty(phones)) {
            log.warn("ELE RENT REFUND WARN! phones is empty,tenantId={}", userInfo.getTenantId());
            return;
        }
        
        phones.parallelStream().forEach(item -> {
            RentRefundAuditMessageNotify abnormalMessageNotify = new RentRefundAuditMessageNotify();
            abnormalMessageNotify.setUserName(userInfo.getName());
            abnormalMessageNotify.setBusinessCode(StringUtils.isBlank(userInfo.getIdNumber()) ? "/" : userInfo.getIdNumber().substring(userInfo.getIdNumber().length() - 6));
            abnormalMessageNotify.setApplyTime(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_FORMAT));
            
            MqNotifyCommon<RentRefundAuditMessageNotify> abnormalMessageNotifyCommon = new MqNotifyCommon<>();
            abnormalMessageNotifyCommon.setTime(System.currentTimeMillis());
            abnormalMessageNotifyCommon.setType(MqNotifyCommon.TYPE_RENT_REFUND_AUDIT);
            abnormalMessageNotifyCommon.setPhone(item);
            abnormalMessageNotifyCommon.setData(abnormalMessageNotify);
            
            rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(abnormalMessageNotifyCommon), "", "", 0);
        });
    }
    
    @Override
    public Triple<Boolean, String, Object> batteryMembercardRefundOrderDetail(String orderNo, Integer confirm) {
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderNo);
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not found electricityMemberCardOrder,orderNo={}", orderNo);
            return Triple.of(false, "100281", "电池套餐订单不存在");
        }
        
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_EXPIRE)) {
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
        
        // 若退用户最后一个套餐用户绑定有资产提示先归还资产再退租金
        if (Objects.equals(userBatteryMemberCard.getOrderId(), orderNo) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! not return battery,uid={}", userInfo.getUid());
            return Triple.of(false, "100295", "请先归还资产再退租金");
        }
        
        BigDecimal refundAmount = calculateRefundAmount(userBatteryMemberCard, batteryMemberCard, electricityMemberCardOrder);
        if (refundAmount.compareTo(electricityMemberCardOrder.getPayAmount()) > 0) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! refundAmount illegal,refundAmount={},uid={}", refundAmount.doubleValue(), userInfo.getUid());
            return Triple.of(false, "100294", "退租金额不合法");
        }
        
        // 校验套餐赠送的优惠券
        if (Objects.nonNull(confirm)) {
            List<UserCoupon> userCoupons = userCouponService.selectListBySourceOrderId(electricityMemberCardOrder.getOrderId());
            if (!CollectionUtils.isEmpty(userCoupons)) {
                for (UserCoupon userCoupon : userCoupons) {
                    if (Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_DESTRUCTION) || Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_USED)) {
                        log.warn("BATTERY MEMBERCARD REFUND WARN! battery memberCard binding coupon already used,uid={}", userInfo.getUid());
                        return Triple.of(false, "100291", "套餐绑定的优惠券已使用，无法退租");
                    }
                }
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
    
    @Override
    public R checkPayParamsDetails(String orderNo) {
        ElectricityMemberCardOrder electricityMemberCardOrder = batteryMemberCardOrderService.selectByOrderNo(orderNo);
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.warn("CHECK PAY PARAMS DETAILS WARN! not found electricityMemberCardOrder,orderNo={}", orderNo);
            return R.fail("100281", "电池套餐订单不存在");
        }
        boolean configConsistency = payConfigBizService
                .checkConfigConsistency(electricityMemberCardOrder.getPaymentChannel(), electricityMemberCardOrder.getTenantId(), electricityMemberCardOrder.getParamFranchiseeId(),
                        electricityMemberCardOrder.getWechatMerchantId());
        
        if (!configConsistency) {
            return R.ok(CheckPayParamsResultEnum.FAIL.getCode());
        }
        return R.ok(CheckPayParamsResultEnum.SUCCESS.getCode());
    }
    
    @Override
    @Slave
    public List<BatteryMembercardRefundOrderVO> listSuperAdminPage(BatteryMembercardRefundOrderQuery query) {
        List<BatteryMembercardRefundOrder> list = this.batteryMembercardRefundOrderMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        
        return list.parallelStream().map(item -> {
            BatteryMembercardRefundOrderVO batteryMembercardRefundOrderVO = new BatteryMembercardRefundOrderVO();
            BeanUtils.copyProperties(item, batteryMembercardRefundOrderVO);
            
            if (Objects.nonNull(item.getTenantId())) {
                Tenant tenant = tenantService.queryByIdFromCache(item.getTenantId());
                batteryMembercardRefundOrderVO.setTenantName(Objects.isNull(tenant) ? null : tenant.getName());
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromDb(item.getUid());
            batteryMembercardRefundOrderVO.setName(Objects.isNull(userInfo) ? "" : userInfo.getName());
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMid());
            batteryMembercardRefundOrderVO.setMemberCardName(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
            batteryMembercardRefundOrderVO.setRentUnit(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentUnit());
            batteryMembercardRefundOrderVO.setLimitCount(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getLimitCount());
            batteryMembercardRefundOrderVO.setRentPriceUnit(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentPriceUnit());
            
            return batteryMembercardRefundOrderVO;
        }).collect(Collectors.toList());
    }
    
    private void assignOtherAttr(BatteryMembercardRefundOrderDetailVO refundOrderDetailVO, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard,
            ElectricityMemberCardOrder electricityMemberCardOrder) {
        // 未使用
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_NOT_USE)) {
            UserBatteryMemberCardPackage userBatteryMemberCardPackage = userBatteryMemberCardPackageService.selectByOrderNo(electricityMemberCardOrder.getOrderId());
            if (Objects.isNull(userBatteryMemberCardPackage)) {
                return;
            }
            
            refundOrderDetailVO.setRemainingNumber(userBatteryMemberCardPackage.getRemainingNumber());
            refundOrderDetailVO.setRemainingTime(
                    Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? userBatteryMemberCardPackage.getMemberCardExpireTime() / 24 / 60 / 60 / 1000
                            : userBatteryMemberCardPackage.getMemberCardExpireTime() / 60 / 1000);
        }
        
        // 使用中
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
            refundOrderDetailVO.setRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
            refundOrderDetailVO.setRemainingTime(
                    Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis())
                            / 24 / 60 / 60 / 1000 : (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()) / 60 / 1000);
        }
    }
    
    private void assignOtherAttr(BatteryMembercardRefundOrder batteryMembercardRefundOrder, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard,
            ElectricityMemberCardOrder electricityMemberCardOrder) {
        // 未使用
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_NOT_USE)) {
            UserBatteryMemberCardPackage userBatteryMemberCardPackage = userBatteryMemberCardPackageService.selectByOrderNo(electricityMemberCardOrder.getOrderId());
            if (Objects.isNull(userBatteryMemberCardPackage)) {
                return;
            }
            
            batteryMembercardRefundOrder.setRemainingNumber(userBatteryMemberCardPackage.getRemainingNumber());
            batteryMembercardRefundOrder.setRemainingTime(
                    Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? userBatteryMemberCardPackage.getMemberCardExpireTime() / 24 / 60 / 60 / 1000
                            : userBatteryMemberCardPackage.getMemberCardExpireTime() / 60 / 1000);
        }
        
        // 使用中
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
            batteryMembercardRefundOrder.setRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
            batteryMembercardRefundOrder.setRemainingTime(
                    Objects.equals(BatteryMemberCard.RENT_UNIT_DAY, batteryMemberCard.getRentUnit()) ? (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis())
                            / 24 / 60 / 60 / 1000 : (userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis()) / 60 / 1000);
        }
    }
    
    private BigDecimal calculateRefundAmount(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard,
            ElectricityMemberCardOrder electricityMemberCardOrder) {
        BigDecimal result = BigDecimal.valueOf(0);
        
        // 未使用
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_NOT_USE)) {
            result = electricityMemberCardOrder.getPayAmount();
        }
        
        // 使用中
        if (Objects.equals(electricityMemberCardOrder.getUseStatus(), ElectricityMemberCardOrder.USE_STATUS_USING)) {
            if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                // 限次
                long useCount = electricityMemberCardOrder.getMaxUseCount() - userBatteryMemberCard.getOrderRemainingNumber();
                result = useCount > 0 ? electricityMemberCardOrder.getPayAmount().subtract(batteryMemberCard.getRentPriceUnit().multiply(BigDecimal.valueOf(useCount)))
                        : electricityMemberCardOrder.getPayAmount();
            } else {
                // 不限次
                long usedTime = System.currentTimeMillis() - userBatteryMemberCard.getOrderEffectiveTime();
                if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
                    result = electricityMemberCardOrder.getPayAmount()
                            .subtract(batteryMemberCard.getRentPriceUnit().multiply(BigDecimal.valueOf(Math.ceil(usedTime / 1000.0 / 60 / 60 / 24))));
                } else {
                    result = electricityMemberCardOrder.getPayAmount()
                            .subtract(batteryMemberCard.getRentPriceUnit().multiply(BigDecimal.valueOf(Math.ceil(usedTime / 1000.0 / 60))));
                }
            }
        }
        
        if (result.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            result = BigDecimal.ZERO;
        }
        
        return result;
    }
    
}
