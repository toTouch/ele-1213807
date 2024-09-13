package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.meituan.MeiTuanOrderRedeemRollBackBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallOrderMapper;
import com.xiliulou.electricity.query.meituan.OrderQuery;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.meituan.LimitTradeRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.meituan.MeiTuanBatteryMemberCardOrderRedeemTxService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ThirdMallConfigHolder;
import com.xiliulou.electricity.vo.meituan.LimitTradeVO;
import com.xiliulou.electricity.vo.meituan.OrderVO;
import com.xiliulou.thirdmall.config.meituan.MeiTuanRiderMallHostConfig;
import com.xiliulou.thirdmall.entity.meituan.MeiTuanRiderMallApiConfig;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.DeliverRsp;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.service.meituan.virtualtrade.VirtualTradeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 18:06:11
 */
@Slf4j
@Service
public class MeiTuanRiderMallOrderServiceImpl implements MeiTuanRiderMallOrderService {
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("MEI-TUAN-ORDER-ROLLBACK-THREAD-POOL", 4, "meiTuanOrderThread:");
    
    @Resource
    private MeiTuanRiderMallOrderMapper meiTuanRiderMallOrderMapper;
    
    @Resource
    private MeiTuanRiderMallConfigService meiTuanRiderMallConfigService;
    
    @Resource
    private VirtualTradeService virtualTradeService;
    
    @Resource
    private MeiTuanRiderMallHostConfig meiTuanRiderMallHostConfig;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    private UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private EleRefundOrderService eleRefundOrderService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Resource
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Resource
    private UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private MeiTuanBatteryMemberCardOrderRedeemTxService meiTuanBatteryMemberCardOrderRedeemTxService;
    
    @Slave
    @Override
    public MeiTuanRiderMallOrder queryByOrderId(String orderId, String phone, Long uid, Integer tenantId) {
        return meiTuanRiderMallOrderMapper.selectByOrderId(orderId, phone, uid, tenantId);
    }
    
    @Slave
    @Override
    public List<MeiTuanRiderMallOrder> listOrdersByPhone(OrderQuery query) {
        return meiTuanRiderMallOrderMapper.selectByPhone(query);
    }
    
    /**
     * 1.创建套餐成功 2.通知美团发货 3.发货失败，回滚步骤1的数据
     */
    @Override
    public Triple<Boolean, String, Object> createBatteryMemberCardOrder(OrderQuery query) {
        Integer tenantId = query.getTenantId();
        Long uid = query.getUid();
        String meiTuanOrderId = query.getOrderId();
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.CACHE_MEI_TUAN_CREATE_BATTERY_MEMBER_CARD_ORDER_LOCK_KEY + uid, "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        MeiTuanOrderRedeemRollBackBO rollBackBO = null;
        try {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("MeiTuan order redeem fail! not found user,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            // 校验是否开启美团商城
            MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.checkEnableMeiTuanRiderMall(tenantId);
            if (Objects.isNull(meiTuanRiderMallConfig)) {
                log.warn("MeiTuan order redeem fail! not found meiTuanRiderMallConfig, uid={}, tenantId={}", uid, tenantId);
                return Triple.of(false, "120134", "兑换失败，请联系客服处理");
            }
            
            // 校验美团订单是否存在
            MeiTuanRiderMallOrder meiTuanRiderMallOrder = this.queryByOrderId(meiTuanOrderId, userInfo.getPhone(), null, tenantId);
            if (Objects.isNull(meiTuanRiderMallOrder)) {
                log.warn("MeiTuan order redeem fail! not found meiTuanOrderId, uid={}, meiTuanOrderId={}", uid, meiTuanOrderId);
                return Triple.of(false, "120131", "未能查询到该美团订单号码，请稍后再试");
            }
            
            if (Objects.equals(meiTuanRiderMallOrder.getOrderUseStatus(), VirtualTradeStatusEnum.ORDER_USE_STATUS_USED.getCode())) {
                log.warn("MeiTuan order redeem fail! meiTuanOrderId used, uid={}, meiTuanOrderId={}", uid, meiTuanOrderId);
                return Triple.of(false, "120141", "该订单已兑换，请勿重复兑换");
            }
            
            if (Objects.equals(meiTuanRiderMallOrder.getMeiTuanOrderStatus(), VirtualTradeStatusEnum.ORDER_STATUS_CANCELED.getCode())) {
                log.warn("MeiTuan order redeem fail! meiTuanOrderId canceled, uid={}, meiTuanOrderId={}", uid, meiTuanOrderId);
                return Triple.of(false, "120134", "兑换失败，请联系客服处理");
            }
            
            // 套餐ID
            Long memberCardId = meiTuanRiderMallOrder.getPackageId();
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
            if (Objects.isNull(batteryMemberCard) || Objects.isNull(batteryMemberCard.getTenantId()) || !Objects.equals(batteryMemberCard.getTenantId(), tenantId)) {
                log.warn("MeiTuan order redeem fail! not found batteryMemberCard,uid={}, memberCardId={}", uid, memberCardId);
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("MeiTuan order redeem fail! user is unUsable,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("MeiTuan order redeem fail! user not auth,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
            
            if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                log.warn("MeiTuan order redeem fail! batteryMemberCard is down,uid={},mid={}", uid, memberCardId);
                return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
            }
            
            if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                    batteryMemberCard.getFranchiseeId())) {
                log.warn("MeiTuan order redeem fail! batteryMemberCard franchiseeId not equals,uid={},mid={}", uid, memberCardId);
                return Triple.of(false, "120132", "美团商城订单与绑定加盟商不一致，请核实后操作");
            }
            
            // 检查是否为自主续费状态
            Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(uid);
            if (!userRenewalStatus) {
                log.warn("MeiTuan order redeem fail! user renewal status is false, uid={}, mid={}, meiTuanOrderId={}", uid, memberCardId, meiTuanOrderId);
                return Triple.of(false, "120133", "您是站点代付用户，无法使用美团商城换电卡");
            }
            
            // 判断套餐用户分组和用户的用户分组是否匹配
            List<UserInfoGroupNamesBO> userInfoGroups = userInfoGroupDetailService.listGroupByUid(
                    UserInfoGroupDetailQuery.builder().uid(userInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
            if (CollectionUtils.isNotEmpty(userInfoGroups)) {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                    log.warn("MeiTuan order redeem fail! batteryMemberCard down, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120138", "所属分组与套餐不匹配，无法兑换，请联系客服处理");
                }
                
                List<Long> userGroupIds = userInfoGroups.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
                userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
                if (CollectionUtils.isEmpty(userGroupIds)) {
                    log.warn("MeiTuan order redeem fail! UseInfoGroup not contain systemGroup, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120138", "所属分组与套餐不匹配，无法兑换，请联系客服处理");
                }
            } else {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                    log.warn("MeiTuan order redeem fail! SystemGroup cannot purchase useInfoGroup memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120138", "所属分组与套餐不匹配，无法兑换，请联系客服处理");
                }
                
                if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                    log.warn("MeiTuan order redeem fail! Old use cannot purchase new rentType memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120138", "所属分组与套餐不匹配，无法兑换，请联系客服处理");
                }
                
                if (Objects.equals(userInfo.getPayCount(), 0) && BatteryMemberCard.RENT_TYPE_OLD.equals(batteryMemberCard.getRentType())) {
                    log.warn("MeiTuan order redeem fail! New use cannot purchase old rentType memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120138", "所属分组与套餐不匹配，无法兑换，请联系客服处理");
                }
            }
            
            Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> pair;
            
            // 已缴纳车电一体押金，不可兑换换电套餐
            if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
                log.warn("MeiTuan order redeem fail! user has paid carBattery deposit,uid={}", uid);
                return Triple.of(false, "120144", "您当前绑定的为车电一体套餐，暂不支持兑换换电卡，请联系客服处理");
            }
            
            // 已缴纳押金
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
                if (Objects.isNull(userBatteryDeposit)) {
                    log.warn("MeiTuan order redeem fail! not found userBatteryDeposit,uid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0001", "用户押金信息不存在");
                }
                
                // 是否有正在进行中的退押
                Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
                if (refundCount > 0) {
                    log.warn("MeiTuan order redeem fail! have refunding order,uid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0047", "电池押金退款中");
                }
                
                // 是否有正在进行中的退租
                List<BatteryMembercardRefundOrder> batteryMemberCardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(uid);
                if (CollectionUtils.isNotEmpty(batteryMemberCardRefundOrders)) {
                    log.warn("MeiTuan order redeem fail! battery memberCard refund review,uid={}", uid);
                    return Triple.of(false, "100018", "套餐租金退款审核中");
                }
                
                // 用户绑定的套餐
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
                if (Objects.isNull(userBatteryMemberCard)) {
                    // 给用户绑定套餐
                    pair = meiTuanBatteryMemberCardOrderRedeemTxService.bindUserMemberCard(userInfo, batteryMemberCard, meiTuanRiderMallOrder);
                } else {
                    if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
                        log.warn("MeiTuan order redeem fail! userBatteryMemberCard disable,uid={},mid={}", uid, memberCardId);
                        return Triple.of(false, "120142", "用户套餐冻结中，不允许操作");
                    }
                    
                    if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW, userBatteryMemberCard.getMemberCardStatus())) {
                        log.warn("MeiTuan order redeem fail! userBatteryMemberCard freeze waiting approve, uid={}, mid={}", userInfo.getUid(), query.getPackageId());
                        return Triple.of(false, "120143", "用户套餐冻结审核中，不允许操作");
                    }
                    
                    // 是否有滞纳金
                    Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo,
                            userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
                    if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                        log.warn("MeiTuan order redeem fail! user exist battery service fee,uid={},mid={}", uid, userBatteryMemberCard.getMemberCardId());
                        return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
                    }
                    
                    // 绑定的套餐
                    BatteryMemberCard userBindBatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                    
                    // 电池型号是否匹配
                    List<String> userBindBatteryTypes = userBatteryTypeService.selectByUid(uid);
                    List<String> memberCardBatteryTypes = memberCardBatteryTypeService.selectBatteryTypeByMid(memberCardId);
                    Boolean matched = isBatteryTypeMatched(userInfo, userBindBatteryTypes, memberCardBatteryTypes);
                    if (!matched) {
                        log.warn("MeiTuan order redeem fail! batteryType not matched, uid={}, mid={}, meiTuanOrderId={}", uid, memberCardId, meiTuanOrderId);
                        return Triple.of(false, "120140", "美团商城订单与已使用的套餐电池型号不一致，请核实后操作");
                    }
                    
                    // 续费套餐
                    pair = meiTuanBatteryMemberCardOrderRedeemTxService.saveRenewalUserBatteryMemberCardOrder(userInfo, batteryMemberCard, userBatteryMemberCard,
                            userBindBatteryMemberCard, meiTuanRiderMallOrder, userBindBatteryTypes, memberCardBatteryTypes);
                }
            } else {
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
                if (Objects.nonNull(userBatteryMemberCard) && StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                    return Triple.of(false, "ELECTRICITY.00121", "用户已绑定电池套餐");
                }
                
                pair = meiTuanBatteryMemberCardOrderRedeemTxService.saveUserInfoAndOrder(userInfo, batteryMemberCard, userBatteryMemberCard, meiTuanRiderMallOrder);
            }
            
            if (Objects.isNull(pair) || Objects.isNull(pair.getLeft())) {
                log.warn("MeiTuan order redeem fail! pair is null, uid={}, mid={}, meiTuanOrderId={}", uid, memberCardId, meiTuanOrderId);
                return Triple.of(false, "120139", "订单兑换失败，请联系客服处理");
            }
            
            ElectricityMemberCardOrder electricityMemberCardOrder = pair.getLeft();
            rollBackBO = pair.getRight();
            
            // 通知美团发货
            Boolean deliverResult = notifyMeiTuanDeliver(meiTuanRiderMallConfig, meiTuanRiderMallOrder, electricityMemberCardOrder, uid);
            // 发货失败，执行回滚
            if (!deliverResult) {
                meiTuanBatteryMemberCardOrderRedeemTxService.rollback(rollBackBO);
                log.error("MeiTuan order redeem fail! notifyMeiTuanDeliver fail, uid={}, meiTuanOrderId={}", uid, meiTuanOrderId);
                return Triple.of(false, "120139", "订单兑换失败，请联系客服处理");
            }
            
            return Triple.of(true, "", null);
        } catch (Exception e) {
            // 异常，执行回滚
            meiTuanBatteryMemberCardOrderRedeemTxService.rollback(rollBackBO);
            log.error("MeiTuan order redeem fail! uid={}, meiTuanOrderId={}, e={}", uid, meiTuanOrderId, e);
            return Triple.of(false, "120139", "订单兑换失败，请联系客服处理");
        } finally {
            redisService.delete(CacheConstant.CACHE_MEI_TUAN_CREATE_BATTERY_MEMBER_CARD_ORDER_LOCK_KEY + uid);
        }
    }
    
    
    private Boolean isBatteryTypeMatched(UserInfo userInfo, List<String> userBindBatteryTypes, List<String> memberCardBatteryTypes) {
        // 用户绑定的电池型号串数
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.nonNull(franchisee) && Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (CollectionUtils.isNotEmpty(userBindBatteryTypes)) {
                userBindBatteryTypes = userBindBatteryTypes.stream().map(item -> item.substring(item.lastIndexOf("_") + 1)).collect(Collectors.toList());
            }
        }
        
        // 套餐电池型号串数 number
        List<String> number = memberCardBatteryTypes.stream().filter(StringUtils::isNotBlank).map(e -> e.substring(e.lastIndexOf("_") + 1)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userBindBatteryTypes)) {
            return CollectionUtils.isNotEmpty(number) && CollectionUtils.containsAll(number, userBindBatteryTypes);
        }
        
        return false;
    }
    
    private Boolean notifyMeiTuanDeliver(MeiTuanRiderMallConfig config, MeiTuanRiderMallOrder meiTuanRiderMallOrder, ElectricityMemberCardOrder electricityMemberCardOrder,
            Long uid) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("NotifyMeiTuanDeliver warn! notifyMeiTuanDeliver fail, userBatteryMemberCard is null, uid={}", uid);
            return Boolean.FALSE;
        }
        
        String orderId = meiTuanRiderMallOrder.getMeiTuanOrderId();
        DeliverRsp deliverRsp = virtualTradeService.deliverOrder(apiConfig, orderId, orderId, VirtualTradeStatusEnum.VP_RECHARGE_STATUS_SUCCESS.getCode(),
                userBatteryMemberCard.getOrderEffectiveTime() / 1000, userBatteryMemberCard.getOrderExpireTime() / 1000);
        if (Objects.isNull(deliverRsp)) {
            log.warn("NotifyMeiTuanDeliver warn! notifyMeiTuanDeliver fail, deliverRsp is null, uid={}, orderId={}", uid, orderId);
            return Boolean.FALSE;
        }
        
        // 更新订单
        MeiTuanRiderMallOrder meiTuanRiderMallOrderUpdate = new MeiTuanRiderMallOrder();
        meiTuanRiderMallOrderUpdate.setId(meiTuanRiderMallOrder.getId());
        
        // 发货失败
        Integer failReason = deliverRsp.getFailReason();
        if (!deliverRsp.getResult()) {
            if (Objects.nonNull(failReason) && Objects.equals(failReason, VirtualTradeStatusEnum.CANCEL_ED_DELIVER_ERROR.getCode())) {
                log.warn("NotifyMeiTuanDeliver warn! notifyMeiTuanDeliver fail, meiTuan order canceled, uid={}, orderId={}", uid, orderId);
                
                // 订单取消导致发货失败，“订单取消”
                meiTuanRiderMallOrderUpdate.setMeiTuanOrderStatus(VirtualTradeStatusEnum.ORDER_STATUS_CANCELED.getCode());
                // 修改同步对账状态为“已处理”
                meiTuanRiderMallOrderUpdate.setOrderSyncStatus(VirtualTradeStatusEnum.ORDER_HANDLE_REASON_STATUS_HANDLE.getCode());
                meiTuanRiderMallOrderUpdate.setUpdateTime(System.currentTimeMillis());
                
                meiTuanRiderMallOrderMapper.update(meiTuanRiderMallOrderUpdate);
            }
            return Boolean.FALSE;
        }
        
        // 发货成功
        // 关联套餐订单
        meiTuanRiderMallOrderUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
        // 修改订单状态为“已发货”
        meiTuanRiderMallOrderUpdate.setMeiTuanOrderStatus(VirtualTradeStatusEnum.ORDER_STATUS_DELIVERED.getCode());
        // 修改同步对账状态为“已处理”
        meiTuanRiderMallOrderUpdate.setOrderSyncStatus(VirtualTradeStatusEnum.ORDER_HANDLE_REASON_STATUS_HANDLE.getCode());
        // 修改订单状态为“已使用”
        meiTuanRiderMallOrderUpdate.setOrderUseStatus(VirtualTradeStatusEnum.ORDER_USE_STATUS_USED.getCode());
        meiTuanRiderMallOrderUpdate.setUpdateTime(System.currentTimeMillis());
        
        meiTuanRiderMallOrderMapper.update(meiTuanRiderMallOrderUpdate);
        
        return Boolean.TRUE;
    }
    
    @Override
    public List<OrderVO> listOrders(OrderQuery query) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("MeiTuanListOrders ERROR! userInfo is null");
            return Collections.emptyList();
        }
        
        // 根据手机号和租户查下订单，因为拉取的订单可能没有uid
        query.setPhone(userInfo.getPhone());
        
        List<MeiTuanRiderMallOrder> riderMallOrders = this.listOrdersByPhone(query);
        if (CollectionUtils.isEmpty(riderMallOrders)) {
            return Collections.emptyList();
        }
        
        return riderMallOrders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public LimitTradeVO meiTuanLimitTradeCheck(LimitTradeRequest request) {
        Integer tenantId = ThirdMallConfigHolder.getTenantId();
        Long memberCardId = Long.valueOf(request.getProviderSkuId());
        String phone = request.getAccount();
        LimitTradeVO noLimit = LimitTradeVO.builder().limitResult(Boolean.FALSE).limitType(VirtualTradeStatusEnum.LIMIT_TYPE_NO.getCode()).build();
        LimitTradeVO limit = LimitTradeVO.builder().limitResult(Boolean.TRUE).limitType(VirtualTradeStatusEnum.LIMIT_TYPE_OLD_USER.getCode()).build();
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
        // 套餐不存在：不限制
        if (Objects.isNull(batteryMemberCard)) {
            return noLimit;
        }
        
        // 用户不存在：不限制
        User user = userService.queryByUserPhone(phone, User.TYPE_USER_NORMAL_WX_PRO, tenantId);
        if (Objects.isNull(user)) {
            return noLimit;
        }
        
        // 用户不存在：不限制
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            return noLimit;
        }
        
        // 判断套餐用户分组和用户的用户分组是否匹配
        List<UserInfoGroupNamesBO> userInfoGroups = userInfoGroupDetailService.listGroupByUid(UserInfoGroupDetailQuery.builder().uid(userInfo.getUid()).tenantId(tenantId).build());
        
        if (CollectionUtils.isNotEmpty(userInfoGroups)) {
            // 自定义用户分组用户不可购买系统分组套餐：限制
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                log.warn("MeiTuanLimitTradeCheck warn! UseInfoGroup cannot purchase systemGroup memberCard, phone={}, uid={}, mid={}", phone, userInfo.getUid(), memberCardId);
                return limit;
            }
            
            List<Long> userGroupIds = userInfoGroups.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
            userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
            // 自定义用户分组中没有该用户，不可购买指定套餐：限制
            if (CollectionUtils.isEmpty(userGroupIds)) {
                log.warn("MeiTuanLimitTradeCheck warn! UseInfoGroup not contain systemGroup, phone={}, uid={}, mid={}", phone, userInfo.getUid(), memberCardId);
                return limit;
            }
        } else {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                log.warn("MeiTuanLimitTradeCheck warn! SystemGroup cannot purchase useInfoGroup memberCard, phone={}, uid={}, mid={}", phone, userInfo.getUid(), memberCardId);
                return limit;
            }
            
            // 老用户不可购买新套餐：限制
            if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                log.warn("MeiTuanLimitTradeCheck warn! Old use cannot purchase new rentType memberCard, phone={}, uid={}, mid={}", phone, userInfo.getUid(), memberCardId);
                return limit;
            }
            
            // 新用户不可购买续费套餐：限制
            if (Objects.equals(userInfo.getPayCount(), 0) && BatteryMemberCard.RENT_TYPE_OLD.equals(batteryMemberCard.getRentType())) {
                log.warn("MeiTuanLimitTradeCheck warn! New use cannot purchase old rentType memberCard, phone={}, uid={}, mid={}", phone, userInfo.getUid(), memberCardId);
                return limit;
            }
        }
        
        return noLimit;
    }
}
