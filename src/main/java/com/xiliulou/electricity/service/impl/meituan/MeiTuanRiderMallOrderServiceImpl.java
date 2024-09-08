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
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallOrderMapper;
import com.xiliulou.electricity.query.meituan.OrderQuery;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.meituan.LimitTradeRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallOrderService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Resource
    private EleDepositOrderService depositOrderService;
    
    @Resource
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Resource
    private UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    private EleUserOperateRecordService eleUserOperateRecordService;
    
    @Resource
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Slave
    @Override
    public MeiTuanRiderMallOrder queryByOrderId(String orderId, String phone, Long uid) {
        return meiTuanRiderMallOrderMapper.selectByOrderId(orderId, phone, uid);
    }
    
    @Slave
    @Override
    public List<MeiTuanRiderMallOrder> listOrdersByUid(OrderQuery query) {
        return meiTuanRiderMallOrderMapper.selectByUid(query);
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
        
        try {
            // 校验是否开启美团商城
            MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.checkEnableMeiTuanRiderMall(tenantId);
            if (Objects.isNull(meiTuanRiderMallConfig)) {
                log.warn("MeiTuan order redeem fail! not found meiTuanRiderMallConfig, uid={}, tenantId={}", uid, tenantId);
                return Triple.of(false, "120134", "兑换失败，请联系客服处理");
            }
            
            // 校验美团订单是否存在
            MeiTuanRiderMallOrder meiTuanRiderMallOrder = this.queryByOrderId(meiTuanOrderId, null, uid);
            if (Objects.isNull(meiTuanRiderMallOrder)) {
                log.warn("MeiTuan order redeem fail! not found meiTuanOrderId, uid={}, meiTuanOrderId={}", uid, meiTuanOrderId);
                return Triple.of(false, "120131", "未能查询到该美团订单号码，请稍后再试");
            }
            
            if (Objects.equals(meiTuanRiderMallOrder.getOrderUseStatus(), VirtualTradeStatusEnum.ORDER_USE_STATUS_USED.getCode())) {
                log.warn("MeiTuan order redeem fail! meiTuanOrderId used, uid={}, meiTuanOrderId={}", uid, meiTuanOrderId);
                return Triple.of(false, "120138", "该订单已兑换，请勿重复兑换");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("MeiTuan order redeem fail! not found user,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            // 套餐ID
            Long memberCardId = meiTuanRiderMallOrder.getPackageId();
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
            if (Objects.isNull(batteryMemberCard)) {
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
                    return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
                
                List<Long> userGroupIds = userInfoGroups.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
                userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
                if (CollectionUtils.isEmpty(userGroupIds)) {
                    log.warn("MeiTuan order redeem fail! UseInfoGroup not contain systemGroup, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
            } else {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                    log.warn("MeiTuan order redeem fail! SystemGroup cannot purchase useInfoGroup memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
                
                if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                    log.warn("MeiTuan order redeem fail! Old use cannot purchase new rentType memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
                
                if (Objects.equals(userInfo.getPayCount(), 0) && BatteryMemberCard.RENT_TYPE_OLD.equals(batteryMemberCard.getRentType())) {
                    log.warn("MeiTuan order redeem fail! New use cannot purchase old rentType memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
            }
            
            Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> pair = null;
            
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
                    pair = bindUserMemberCard(userInfo, batteryMemberCard, meiTuanRiderMallOrder);
                } else {
                    if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
                        log.warn("MeiTuan order redeem fail! userBatteryMemberCard disable,uid={},mid={}", uid, memberCardId);
                        return Triple.of(false, "100247", "用户套餐冻结中，不允许操作");
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
                    
                    // 续费套餐
                    pair = saveRenewalUserBatteryMemberCardOrder(userInfo, batteryMemberCard, userBatteryMemberCard, userBindBatteryMemberCard, meiTuanRiderMallOrder);
                }
            } else {
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
                if (Objects.nonNull(userBatteryMemberCard) && StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                    return Triple.of(false, "ELECTRICITY.00121", "用户已绑定电池套餐");
                }
                
                pair = saveUserInfoAndOrder(userInfo, batteryMemberCard, userBatteryMemberCard, meiTuanRiderMallOrder);
            }
            
            if (Objects.isNull(pair)) {
                log.warn("MeiTuan order redeem fail! pair is null, uid={}, mid={}", uid, memberCardId);
                return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
            }
            
            ElectricityMemberCardOrder electricityMemberCardOrder = pair.getLeft();
            MeiTuanOrderRedeemRollBackBO rollBackBO = pair.getRight();
            
            // 通知美团发货
            Triple<Boolean, String, Object> deliverTriple = notifyMeiTuanDeliver(meiTuanRiderMallConfig, meiTuanRiderMallOrder, electricityMemberCardOrder, uid);
            // 发货失败，执行回滚
            if (!deliverTriple.getLeft()) {
                this.asyncRollback(rollBackBO);
            }
            
            return Triple.of(true, "", null);
        } finally {
            redisService.delete(CacheConstant.CACHE_MEI_TUAN_CREATE_BATTERY_MEMBER_CARD_ORDER_LOCK_KEY + uid);
        }
    }
    
    private Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> bindUserMemberCard(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            MeiTuanRiderMallOrder meiTuanRiderMallOrder) {
        ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
        memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        memberCardOrder.setMemberCardId(batteryMemberCard.getId());
        memberCardOrder.setUid(userInfo.getUid());
        memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        memberCardOrder.setCardName(batteryMemberCard.getName());
        memberCardOrder.setPayAmount(meiTuanRiderMallOrder.getMeiTuanActuallyPayPrice());
        memberCardOrder.setPayType(ElectricityMemberCardOrder.MEITUAN_PAYMENT);
        memberCardOrder.setPayCount(1);
        memberCardOrder.setUserName(userInfo.getName());
        memberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        memberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        memberCardOrder.setStoreId(userInfo.getStoreId());
        memberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        memberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        memberCardOrder.setTenantId(userInfo.getTenantId());
        memberCardOrder.setCreateTime(System.currentTimeMillis());
        memberCardOrder.setUpdateTime(System.currentTimeMillis());
        memberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
        memberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        electricityMemberCardOrderService.insert(memberCardOrder);
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(memberCardOrder.getUid());
        userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
        userBatteryMemberCardUpdate.setOrderExpireTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
        userBatteryMemberCardUpdate.setOrderRemainingNumber(batteryMemberCard.getUseCount());
        userBatteryMemberCardUpdate.setRemainingNumber(batteryMemberCard.getUseCount());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUpdate.setMemberCardId(memberCardOrder.getMemberCardId());
        userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
        userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setTenantId(memberCardOrder.getTenantId());
        userBatteryMemberCardUpdate.setCardPayCount(1);
        userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
        
        List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
        List<UserBatteryType> userBatteryTypes = null;
        if (CollectionUtils.isNotEmpty(batteryTypeList)) {
            userBatteryTypes = userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo);
            userBatteryTypeService.batchInsert(userBatteryTypes);
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
        ServiceFeeUserInfo serviceFeeUserInfoInsert = new ServiceFeeUserInfo();
        serviceFeeUserInfoInsert.setServiceFeeGenerateTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
        serviceFeeUserInfoInsert.setUid(userBatteryMemberCardUpdate.getUid());
        serviceFeeUserInfoInsert.setFranchiseeId(memberCardOrder.getFranchiseeId());
        serviceFeeUserInfoInsert.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoInsert.setTenantId(memberCardOrder.getTenantId());
        serviceFeeUserInfoInsert.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
        serviceFeeUserInfoInsert.setDisableMemberCardNo("");
        
        ServiceFeeUserInfo rollBackServiceFeeUserInfo = null;
        if (Objects.nonNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsert);
            
            rollBackServiceFeeUserInfo = ServiceFeeUserInfo.builder().uid(serviceFeeUserInfo.getUid()).serviceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime())
                    .franchiseeId(serviceFeeUserInfo.getFranchiseeId()).updateTime(serviceFeeUserInfo.getUpdateTime()).tenantId(serviceFeeUserInfo.getTenantId())
                    .delFlag(serviceFeeUserInfo.getDelFlag()).disableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo()).build();
        } else {
            serviceFeeUserInfoInsert.setCreateTime(System.currentTimeMillis());
            serviceFeeUserInfoService.insert(serviceFeeUserInfoInsert);
        }
        
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(userInfo.getUid());
        userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
        userInfoUpdate.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(userInfoUpdate);
        
        //封装UserInfo回滚
        UserInfo rollBackUserInfo = UserInfo.builder().uid(userInfo.getUid()).payCount(userInfo.getPayCount()).updateTime(userInfo.getUpdateTime()).build();
        
        double newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
        
        EleUserOperateRecord eleUserMemberCardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                .uid(userInfo.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldValidDays(0)
                .newValidDays((int) newValidDays).oldMaxUseCount(0L).newMaxUseCount(userBatteryMemberCardUpdate.getRemainingNumber()).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMemberCardOperateRecord);
        
        // 封装回滚数据
        MeiTuanOrderRedeemRollBackBO rollBackBO = buildRollBackData(null, memberCardOrder.getId(), null, rollBackUserInfo, userBatteryTypes, null, null,
                userBatteryMemberCardUpdate.getId(), null, serviceFeeUserInfo.getId(), rollBackServiceFeeUserInfo, null, eleUserMemberCardOperateRecord.getId().longValue(), null);
        
        return Pair.of(memberCardOrder, rollBackBO);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> saveRenewalUserBatteryMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard userBindBatteryMemberCard, MeiTuanRiderMallOrder meiTuanRiderMallOrder) {
        ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
        memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        memberCardOrder.setMemberCardId(batteryMemberCard.getId());
        memberCardOrder.setUid(userInfo.getUid());
        memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        memberCardOrder.setCardName(batteryMemberCard.getName());
        memberCardOrder.setPayAmount(meiTuanRiderMallOrder.getMeiTuanActuallyPayPrice());
        memberCardOrder.setPayType(ElectricityMemberCardOrder.MEITUAN_PAYMENT);
        memberCardOrder.setPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
        memberCardOrder.setUserName(userInfo.getName());
        memberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        memberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        memberCardOrder.setStoreId(userInfo.getStoreId());
        memberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        memberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        memberCardOrder.setTenantId(userInfo.getTenantId());
        memberCardOrder.setCreateTime(System.currentTimeMillis());
        memberCardOrder.setUpdateTime(System.currentTimeMillis());
        memberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_NOT_USE);
        memberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        
        UserBatteryMemberCard existUserBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        // 封装回滚数据
        UserBatteryMemberCard rollBackUserBatteryMemberCard = new UserBatteryMemberCard();
        ElectricityMemberCardOrder rollBackElectricityMemberCardOrder = null;
        List<UserBatteryType> insertUserBatteryTypeListForRollBack = null;
        List<UserBatteryType> userBatteryTypes = null;
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)
                || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || Objects.isNull(userBindBatteryMemberCard) || (
                Objects.equals(userBindBatteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            memberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
            
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
            userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setOrderExpireTime(
                    System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setOrderRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setTenantId(userInfo.getTenantId());
            userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
            
            // 封装UserBatteryMemberCard回滚数据
            rollBackUserBatteryMemberCard.setUid(userInfo.getUid());
            rollBackUserBatteryMemberCard.setMemberCardId(existUserBatteryMemberCard.getMemberCardId());
            rollBackUserBatteryMemberCard.setOrderId(existUserBatteryMemberCard.getOrderId());
            rollBackUserBatteryMemberCard.setMemberCardExpireTime(existUserBatteryMemberCard.getMemberCardExpireTime());
            rollBackUserBatteryMemberCard.setOrderExpireTime(existUserBatteryMemberCard.getOrderExpireTime());
            rollBackUserBatteryMemberCard.setOrderEffectiveTime(existUserBatteryMemberCard.getOrderEffectiveTime());
            rollBackUserBatteryMemberCard.setOrderRemainingNumber(existUserBatteryMemberCard.getOrderRemainingNumber());
            rollBackUserBatteryMemberCard.setRemainingNumber(existUserBatteryMemberCard.getRemainingNumber());
            rollBackUserBatteryMemberCard.setMemberCardStatus(existUserBatteryMemberCard.getMemberCardStatus());
            rollBackUserBatteryMemberCard.setDisableMemberCardTime(existUserBatteryMemberCard.getDisableMemberCardTime());
            rollBackUserBatteryMemberCard.setDelFlag(existUserBatteryMemberCard.getDelFlag());
            rollBackUserBatteryMemberCard.setCreateTime(existUserBatteryMemberCard.getCreateTime());
            rollBackUserBatteryMemberCard.setUpdateTime(existUserBatteryMemberCard.getUpdateTime());
            rollBackUserBatteryMemberCard.setTenantId(existUserBatteryMemberCard.getTenantId());
            rollBackUserBatteryMemberCard.setCardPayCount(existUserBatteryMemberCard.getCardPayCount());
            
            // 如果用户原来绑定的有套餐 套餐过期了，需要把原来绑定的套餐订单状态更新为已过期
            if (StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                ElectricityMemberCardOrder existElectricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
                
                ElectricityMemberCardOrder electricityMemberCardOrderUpdateUseStatus = new ElectricityMemberCardOrder();
                electricityMemberCardOrderUpdateUseStatus.setOrderId(userBatteryMemberCard.getOrderId());
                electricityMemberCardOrderUpdateUseStatus.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                electricityMemberCardOrderUpdateUseStatus.setUpdateTime(System.currentTimeMillis());
                electricityMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdateUseStatus);
                
                // 封装ElectricityMemberCardOrder回滚数据
                rollBackElectricityMemberCardOrder = new ElectricityMemberCardOrder();
                rollBackElectricityMemberCardOrder.setId(existElectricityMemberCardOrder.getId());
                rollBackElectricityMemberCardOrder.setOrderId(userBatteryMemberCard.getOrderId());
                rollBackElectricityMemberCardOrder.setUseStatus(existElectricityMemberCardOrder.getUseStatus());
                rollBackElectricityMemberCardOrder.setUpdateTime(existElectricityMemberCardOrder.getUpdateTime());
            }
            
            // 更新用户电池型号
            Set<String> totalBatteryTypes = new HashSet<>();
            List<String> userBindBatteryTypes = userBatteryTypeService.selectByUid(memberCardOrder.getUid());
            List<String> memberCardBatteryTypes = memberCardBatteryTypeService.selectBatteryTypeByMid(memberCardOrder.getMemberCardId());
            if (CollectionUtils.isNotEmpty(userBindBatteryTypes)) {
                totalBatteryTypes.addAll(userBindBatteryTypes);
            }
            if (CollectionUtils.isNotEmpty(memberCardBatteryTypes)) {
                totalBatteryTypes.addAll(memberCardBatteryTypes);
            }
            if (CollectionUtils.isNotEmpty(totalBatteryTypes)) {
                // 封装UserBatteryType回滚数据
                insertUserBatteryTypeListForRollBack = userBatteryTypeService.listByUid(memberCardOrder.getUid());
                
                userBatteryTypeService.deleteByUid(memberCardOrder.getUid());
                
                userBatteryTypes = userBatteryTypeService.buildUserBatteryType(new ArrayList<>(totalBatteryTypes), userInfo);
                userBatteryTypeService.batchInsert(userBatteryTypes);
            }
        } else {
            UserBatteryMemberCardPackage userBatteryMemberCardPackage = new UserBatteryMemberCardPackage();
            userBatteryMemberCardPackage.setUid(userInfo.getUid());
            userBatteryMemberCardPackage.setMemberCardId(memberCardOrder.getMemberCardId());
            userBatteryMemberCardPackage.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardPackage.setRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardPackage.setMemberCardExpireTime(batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardPackage.setTenantId(userInfo.getTenantId());
            userBatteryMemberCardPackage.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardPackage.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardPackageService.insert(userBatteryMemberCardPackage);
            
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    userBatteryMemberCard.getMemberCardExpireTime() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            
            // 封装UserBatteryMemberCard回滚数据
            rollBackUserBatteryMemberCard.setUid(userInfo.getUid());
            rollBackUserBatteryMemberCard.setMemberCardExpireTime(existUserBatteryMemberCard.getMemberCardExpireTime());
            rollBackUserBatteryMemberCard.setRemainingNumber(existUserBatteryMemberCard.getRemainingNumber());
            rollBackUserBatteryMemberCard.setCardPayCount(existUserBatteryMemberCard.getCardPayCount());
            rollBackUserBatteryMemberCard.setUpdateTime(existUserBatteryMemberCard.getUpdateTime());
        }
        
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
        
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 封装ServiceFeeUserInfo回滚数据
        ServiceFeeUserInfo rollBackServiceFeeUserInfo = new ServiceFeeUserInfo();
        if (Objects.nonNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
            
            rollBackServiceFeeUserInfo.setUid(userInfo.getUid());
            rollBackServiceFeeUserInfo.setTenantId(serviceFeeUserInfo.getTenantId());
            rollBackServiceFeeUserInfo.setServiceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime());
            rollBackServiceFeeUserInfo.setUpdateTime(serviceFeeUserInfo.getUpdateTime());
        } else {
            serviceFeeUserInfoUpdate.setFranchiseeId(memberCardOrder.getFranchiseeId());
            serviceFeeUserInfoUpdate.setCreateTime(System.currentTimeMillis());
            serviceFeeUserInfoUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
            serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
            serviceFeeUserInfoService.insert(serviceFeeUserInfoUpdate);
        }
        
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(userInfo.getUid());
        userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
        userInfoUpdate.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(userInfoUpdate);
        
        electricityMemberCardOrderService.insert(memberCardOrder);
        
        //封装UserInfo回滚
        UserInfo rollBackUserInfo = UserInfo.builder().uid(userInfo.getUid()).payCount(userInfo.getPayCount()).updateTime(userInfo.getUpdateTime()).build();
        
        double oldValidDays = 0.0;
        double newValidDays = 0.0;
        Long oldMaxUseCount = 0L;
        Long newMaxUseCount = 0L;
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && !Objects.equals(userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                // oldValidDays = Math.toIntExact(((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                // newValidDays = Math.toIntExact(((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                oldValidDays = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
                newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
            }
            oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
            newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
        }
        
        EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                .uid(userInfo.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldValidDays((int) oldValidDays)
                .newValidDays((int) newValidDays).oldMaxUseCount(oldMaxUseCount).newMaxUseCount(newMaxUseCount).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);
        
        // 封装回滚数据
        MeiTuanOrderRedeemRollBackBO rollBackBO = buildRollBackData(null, memberCardOrder.getId(), rollBackElectricityMemberCardOrder, rollBackUserInfo, userBatteryTypes, null,
                null, null, rollBackUserBatteryMemberCard, serviceFeeUserInfoUpdate.getId(), rollBackServiceFeeUserInfo, null, eleUserMembercardOperateRecord.getId().longValue(),
                insertUserBatteryTypeListForRollBack);
        
        return Pair.of(memberCardOrder, rollBackBO);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Pair<ElectricityMemberCardOrder, MeiTuanOrderRedeemRollBackBO> saveUserInfoAndOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            UserBatteryMemberCard userBatteryMemberCard, MeiTuanRiderMallOrder meiTuanRiderMallOrder) {
        
        BigDecimal deposit = new BigDecimal(0);
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid()))
                .uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(deposit).status(EleDepositOrder.STATUS_SUCCESS)
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId())
                .payType(EleDepositOrder.MEITUAN_DEPOSIT_PAYMENT).storeId(null).mid(batteryMemberCard.getId()).modelType(0).build();
        depositOrderService.insert(eleDepositOrder);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = ElectricityMemberCardOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid())).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).status(ElectricityMemberCardOrder.STATUS_SUCCESS).memberCardId(batteryMemberCard.getId()).uid(userInfo.getUid())
                .maxUseCount(batteryMemberCard.getUseCount()).cardName(batteryMemberCard.getName()).payAmount(meiTuanRiderMallOrder.getMeiTuanActuallyPayPrice())
                .userName(userInfo.getName()).validDays(batteryMemberCard.getValidDays()).tenantId(batteryMemberCard.getTenantId())
                .franchiseeId(batteryMemberCard.getFranchiseeId()).payCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1)
                .payType(ElectricityMemberCardOrder.MEITUAN_PAYMENT).refId(null)
                .sendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null)
                .useStatus(ElectricityMemberCardOrder.USE_STATUS_USING).source(ElectricityMemberCardOrder.SOURCE_NOT_SCAN).storeId(null).couponIds(batteryMemberCard.getCouponIds())
                .build();
        electricityMemberCardOrderService.insert(electricityMemberCardOrder);
        
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(userInfo.getUid());
        userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
        if (Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L)) {
            userInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
        }
        if (Objects.equals(userInfo.getStoreId(), NumberConstant.ZERO_L)) {
            userInfoUpdate.setStoreId(eleDepositOrder.getStoreId());
        }
        userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
        userInfoUpdate.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(userInfoUpdate);
        
        //封装UserInfo回滚
        UserInfo rollBackUserInfo = UserInfo.builder().uid(userInfo.getUid()).batteryDepositStatus(userInfo.getBatteryDepositStatus()).franchiseeId(userInfo.getFranchiseeId())
                .storeId(userInfo.getStoreId()).payCount(userInfo.getPayCount()).updateTime(userInfo.getUpdateTime()).build();
        
        List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
        List<UserBatteryType> userBatteryTypes = null;
        if (CollectionUtils.isNotEmpty(batteryTypeList)) {
            userBatteryTypes = userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo);
            userBatteryTypeService.batchInsert(userBatteryTypes);
        }
        
        UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
        userBatteryDeposit.setUid(userInfo.getUid());
        userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
        userBatteryDeposit.setDid(eleDepositOrder.getMid());
        userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
        userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        if (!Objects.equals(batteryMemberCard.getDeposit(), deposit)) {
            userBatteryDeposit.setDepositModifyFlag(UserBatteryDeposit.DEPOSIT_MODIFY_YES);
            userBatteryDeposit.setBeforeModifyDeposit(batteryMemberCard.getDeposit());
        }
        
        UserBatteryDeposit existUserBatteryDeposit = userBatteryDepositService.queryByUid(userInfo.getUid());
        UserBatteryDeposit rollBackUserBatteryDeposit = null;
        if (Objects.nonNull(existUserBatteryDeposit)) {
            userBatteryDepositService.update(userBatteryDeposit);
            
            // 封装UserBatteryDeposit回滚
            rollBackUserBatteryDeposit = UserBatteryDeposit.builder().uid(userBatteryDeposit.getUid()).orderId(userBatteryDeposit.getOrderId()).did(userBatteryDeposit.getDid())
                    .batteryDeposit(userBatteryDeposit.getBatteryDeposit()).applyDepositTime(userBatteryDeposit.getApplyDepositTime())
                    .depositType(userBatteryDeposit.getDepositType()).delFlag(userBatteryDeposit.getDelFlag()).updateTime(userBatteryDeposit.getUpdateTime())
                    .depositModifyFlag(userBatteryDeposit.getDepositModifyFlag()).beforeModifyDeposit(userBatteryDeposit.getBeforeModifyDeposit()).build();
        } else {
            userBatteryDeposit.setCreateTime(System.currentTimeMillis());
            userBatteryDepositService.insert(userBatteryDeposit);
        }
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(electricityMemberCardOrder.getUid());
        userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
        userBatteryMemberCardUpdate.setOrderExpireTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
        userBatteryMemberCardUpdate.setOrderRemainingNumber(batteryMemberCard.getUseCount());
        userBatteryMemberCardUpdate.setRemainingNumber(batteryMemberCard.getUseCount());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId());
        userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
        userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
        
        UserBatteryMemberCard rollBackUserBatteryMemberCard = null;
        if (Objects.isNull(userBatteryMemberCard)) {
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
        } else {
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            
            // 封装UserBatteryMemberCard的回滚
            rollBackUserBatteryMemberCard = UserBatteryMemberCard.builder().uid(userBatteryMemberCard.getUid()).orderId(userBatteryMemberCard.getOrderId())
                    .orderExpireTime(userBatteryMemberCard.getOrderExpireTime()).orderEffectiveTime(userBatteryMemberCard.getOrderEffectiveTime())
                    .memberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime()).orderRemainingNumber(userBatteryMemberCard.getRemainingNumber())
                    .remainingNumber(userBatteryMemberCard.getRemainingNumber()).memberCardStatus(userBatteryMemberCard.getMemberCardStatus())
                    .memberCardId(userBatteryMemberCard.getMemberCardId()).delFlag(userBatteryMemberCard.getDelFlag()).updateTime(userBatteryMemberCard.getUpdateTime())
                    .tenantId(userBatteryMemberCard.getTenantId()).cardPayCount(userBatteryMemberCard.getCardPayCount()).build();
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
        ServiceFeeUserInfo serviceFeeUserInfoInsert = new ServiceFeeUserInfo();
        serviceFeeUserInfoInsert.setServiceFeeGenerateTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
        serviceFeeUserInfoInsert.setUid(userBatteryMemberCardUpdate.getUid());
        serviceFeeUserInfoInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        serviceFeeUserInfoInsert.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoInsert.setTenantId(electricityMemberCardOrder.getTenantId());
        serviceFeeUserInfoInsert.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
        serviceFeeUserInfoInsert.setDisableMemberCardNo("");
        
        ServiceFeeUserInfo rollBackServiceFeeUserInfo = null;
        if (Objects.nonNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsert);
            
            // 封装ServiceFeeUserInfo的回滚
            rollBackServiceFeeUserInfo = ServiceFeeUserInfo.builder().uid(serviceFeeUserInfo.getUid()).serviceFeeGenerateTime(serviceFeeUserInfo.getServiceFeeGenerateTime())
                    .franchiseeId(serviceFeeUserInfo.getFranchiseeId()).updateTime(serviceFeeUserInfo.getUpdateTime()).tenantId(serviceFeeUserInfo.getTenantId())
                    .delFlag(serviceFeeUserInfo.getDelFlag()).disableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo()).build();
        } else {
            serviceFeeUserInfoInsert.setCreateTime(System.currentTimeMillis());
            serviceFeeUserInfoService.insert(serviceFeeUserInfoInsert);
        }
        
        EleUserOperateRecord eleUserDepositOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.DEPOSIT_MODEL)
                .operateContent(EleUserOperateRecord.DEPOSIT_MODEL).operateUid(SecurityUtils.getUid()).uid(eleDepositOrder.getUid())
                .name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldBatteryDeposit(null)
                .newBatteryDeposit(eleDepositOrder.getPayAmount()).tenantId(TenantContextHolder.getTenantId()).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY)
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserDepositOperateRecord);
        
        double oldValidDays = 0.0;
        double newValidDays = 0.0;
        Long oldMaxUseCount = null;
        Long newMaxUseCount = null;
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && !Objects.equals(userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                // oldValidDays = Math.toIntExact(((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                oldValidDays = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
            }
            
            // 设置限次 不限次
            if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
                oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
            } else {
                oldMaxUseCount = UserOperateRecordConstant.UN_LIMIT_COUNT_REMAINING_NUMBER;
            }
        }
        
        // newValidDays = Math.toIntExact(((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
        newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
        
        // 设置限次 不限次
        if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
            newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
        } else {
            newMaxUseCount = UserOperateRecordConstant.UN_LIMIT_COUNT_REMAINING_NUMBER;
        }
        
        EleUserOperateRecord eleUserMemberCardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                .uid(electricityMemberCardOrder.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername())
                .oldValidDays((int) oldValidDays).newValidDays((int) newValidDays).oldMaxUseCount(oldMaxUseCount).newMaxUseCount(newMaxUseCount)
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMemberCardOperateRecord);
        
        // 封装回滚数据
        MeiTuanOrderRedeemRollBackBO rollBackBO = buildRollBackData(eleDepositOrder.getId(), electricityMemberCardOrder.getId(), null, rollBackUserInfo, userBatteryTypes,
                userBatteryDeposit.getId(), rollBackUserBatteryDeposit, userBatteryMemberCardUpdate.getId(), rollBackUserBatteryMemberCard, serviceFeeUserInfo.getId(),
                rollBackServiceFeeUserInfo, eleUserDepositOperateRecord.getId().longValue(), eleUserMemberCardOperateRecord.getId().longValue(), null);
        
        return Pair.of(electricityMemberCardOrder, rollBackBO);
    }
    
    private MeiTuanOrderRedeemRollBackBO buildRollBackData(Long eleDepositOrderById, Long electricityMemberCardOrderById,
            ElectricityMemberCardOrder rollBackElectricityMemberCardOrder, UserInfo rollBackUserInfo, List<UserBatteryType> userBatteryTypes, Long userBatteryDepositById,
            UserBatteryDeposit rollBackUserBatteryDeposit, Long userBatteryMemberCardUpdateById, UserBatteryMemberCard rollBackUserBatteryMemberCard, Long serviceFeeUserInfoById,
            ServiceFeeUserInfo rollBackServiceFeeUserInfo, Long eleUserDepositOperateRecordById, Long eleUserMemberCardOperateRecordById,
            List<UserBatteryType> insertUserBatteryTypeList) {
        // 封装用户电池型号回滚
        List<UserBatteryType> deleteUserBatteryTypeList = null;
        if (Objects.nonNull(userBatteryTypes)) {
            deleteUserBatteryTypeList = new ArrayList<>(userBatteryTypes.size());
            
            for (UserBatteryType userBatteryType : userBatteryTypes) {
                UserBatteryType rollBackUserBatteryType = UserBatteryType.builder().uid(userBatteryType.getUid()).batteryType(userBatteryType.getBatteryType())
                        .tenantId(userBatteryType.getTenantId()).delFlag(userBatteryType.getDelFlag()).createTime(userBatteryType.getCreateTime())
                        .updateTime(userBatteryType.getUpdateTime()).build();
                
                deleteUserBatteryTypeList.add(rollBackUserBatteryType);
            }
        }
        
        MeiTuanOrderRedeemRollBackBO rollBackBO = new MeiTuanOrderRedeemRollBackBO();
        if (Objects.nonNull(eleDepositOrderById)) {
            rollBackBO.setDeleteDepositOrderById(eleDepositOrderById);
        }
        if (Objects.nonNull(electricityMemberCardOrderById)) {
            rollBackBO.setDeleteMemberCardOrderById(electricityMemberCardOrderById);
        }
        if (Objects.nonNull(rollBackElectricityMemberCardOrder)) {
            rollBackBO.setRollBackElectricityMemberCardOrder(rollBackElectricityMemberCardOrder);
        }
        if (Objects.nonNull(rollBackUserInfo)) {
            rollBackBO.setRollBackUserInfo(rollBackUserInfo);
        }
        if (CollectionUtils.isNotEmpty(deleteUserBatteryTypeList)) {
            rollBackBO.setDeleteUserBatteryTypeList(deleteUserBatteryTypeList);
        }
        if (Objects.nonNull(userBatteryDepositById)) {
            rollBackBO.setDeleteUserBatteryDepositById(userBatteryDepositById);
        }
        if (Objects.nonNull(rollBackUserBatteryDeposit)) {
            rollBackBO.setRollBackUserBatteryDeposit(rollBackUserBatteryDeposit);
        }
        if (Objects.nonNull(userBatteryMemberCardUpdateById)) {
            rollBackBO.setDeleteUserBatteryMemberCardById(userBatteryMemberCardUpdateById);
        }
        if (Objects.nonNull(rollBackUserBatteryMemberCard)) {
            rollBackBO.setRollBackUserBatteryMemberCard(rollBackUserBatteryMemberCard);
        }
        if (Objects.nonNull(serviceFeeUserInfoById)) {
            rollBackBO.setDeleteServiceFeeUserInfoById(serviceFeeUserInfoById);
        }
        if (Objects.nonNull(rollBackServiceFeeUserInfo)) {
            rollBackBO.setRollBackServiceFeeUserInfo(rollBackServiceFeeUserInfo);
        }
        if (Objects.nonNull(eleUserDepositOperateRecordById)) {
            rollBackBO.setDeleteEleUserOperateRecordDepositById(eleUserDepositOperateRecordById);
        }
        if (Objects.nonNull(eleUserMemberCardOperateRecordById)) {
            rollBackBO.setDeleteEleUserOperateRecordMemberCardById(eleUserMemberCardOperateRecordById);
        }
        if (CollectionUtils.isNotEmpty(insertUserBatteryTypeList)) {
            rollBackBO.setInsertUserBatteryTypeList(insertUserBatteryTypeList);
        }
        
        return rollBackBO;
    }
    
    private Triple<Boolean, String, Object> notifyMeiTuanDeliver(MeiTuanRiderMallConfig config, MeiTuanRiderMallOrder meiTuanRiderMallOrder,
            ElectricityMemberCardOrder electricityMemberCardOrder, Long uid) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("NotifyMeiTuanDeliver warn! notifyMeiTuanDeliver fail, userBatteryMemberCard is null, uid={}", uid);
            return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
        }
        
        String orderId = meiTuanRiderMallOrder.getMeiTuanOrderId();
        DeliverRsp deliverRsp = null;
        try {
            deliverRsp = virtualTradeService.deliverOrder(apiConfig, orderId, orderId, VirtualTradeStatusEnum.VP_RECHARGE_STATUS_SUCCESS.getCode(),
                    userBatteryMemberCard.getOrderEffectiveTime() / 1000, userBatteryMemberCard.getOrderExpireTime() / 1000);
        } catch (Exception e) {
            log.error("NotifyMeiTuanDeliver error! notifyMeiTuanDeliver fail, uid={}, orderId={}", uid, orderId, e);
            return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
        }
        
        if (Objects.isNull(deliverRsp)) {
            log.warn("NotifyMeiTuanDeliver warn! notifyMeiTuanDeliver fail, deliverRsp is null, uid={}, orderId={}", uid, orderId);
            return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
        }
        
        // 修改同步对账状态为:已处理
        meiTuanRiderMallOrder.setOrderSyncStatus(VirtualTradeStatusEnum.ORDER_HANDLE_REASON_STATUS_HANDLE.getCode());
        meiTuanRiderMallOrder.setUpdateTime(System.currentTimeMillis());
        
        // 发货失败-美团订单已取消，修改订单状态为“已取消”
        if (!deliverRsp.getResult()) {
            meiTuanRiderMallOrder.setMeiTuanOrderStatus(VirtualTradeStatusEnum.ORDER_STATUS_CANCELED.getCode());
            
            log.warn("NotifyMeiTuanDeliver warn! notifyMeiTuanDeliver fail, meiTuan order canceled, uid={}, orderId={}", uid, orderId);
            return Triple.of(false, "120135", "兑换套餐已下架，兑换失败，请联系客服处理");
        }
        
        // 发货成功-修改订单状态为“已发货”，使用状态为：已使用
        meiTuanRiderMallOrder.setOrderId(electricityMemberCardOrder.getOrderId());
        meiTuanRiderMallOrder.setMeiTuanOrderStatus(VirtualTradeStatusEnum.ORDER_STATUS_DELIVERED.getCode());
        meiTuanRiderMallOrder.setOrderUseStatus(VirtualTradeStatusEnum.ORDER_USE_STATUS_USED.getCode());
        
        meiTuanRiderMallOrderMapper.update(meiTuanRiderMallOrder);
        
        return Triple.of(true, "", "");
    }
    
    private void asyncRollback(MeiTuanOrderRedeemRollBackBO rollBackBO) {
        if (Objects.isNull(rollBackBO)) {
            return;
        }
        
        CompletableFuture<Void> deleteDepositOrder = CompletableFuture.runAsync(() -> {
            Long id = rollBackBO.getDeleteDepositOrderById();
            if (Objects.nonNull(id)) {
                depositOrderService.deleteById(id);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete depositOrder error!", e);
            return null;
        });
        
        CompletableFuture<Void> deleteMemberCardOrder = CompletableFuture.runAsync(() -> {
            Long id = rollBackBO.getDeleteMemberCardOrderById();
            if (Objects.nonNull(id)) {
                electricityMemberCardOrderService.deleteById(id);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete memberCardOrder error!", e);
            return null;
        });
        
        CompletableFuture<Void> deleteUserBatteryType = CompletableFuture.runAsync(() -> {
            List<UserBatteryType> deleteUserBatteryTypeList = rollBackBO.getDeleteUserBatteryTypeList();
            if (CollectionUtils.isNotEmpty(deleteUserBatteryTypeList)) {
                List<Long> ids = deleteUserBatteryTypeList.stream().map(UserBatteryType::getId).collect(Collectors.toList());
                userBatteryTypeService.batchDeleteByIds(ids);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete userBatteryType error!", e);
            return null;
        });
        
        CompletableFuture<Void> deleteUserBatteryDeposit = CompletableFuture.runAsync(() -> {
            Long id = rollBackBO.getDeleteUserBatteryDepositById();
            if (Objects.nonNull(id)) {
                userBatteryDepositService.deleteById(id);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete userBatteryType error!", e);
            return null;
        });
        
        CompletableFuture<Void> deleteUserBatteryMemberCard = CompletableFuture.runAsync(() -> {
            Long id = rollBackBO.getDeleteUserBatteryMemberCardById();
            if (Objects.nonNull(id)) {
                userBatteryMemberCardService.deleteById(id);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete userBatteryMemberCard error!", e);
            return null;
        });
        
        CompletableFuture<Void> deleteServiceFeeUserInfo = CompletableFuture.runAsync(() -> {
            Long id = rollBackBO.getDeleteServiceFeeUserInfoById();
            if (Objects.nonNull(id)) {
                serviceFeeUserInfoService.deleteById(id);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete serviceFeeUserInfo error!", e);
            return null;
        });
        
        CompletableFuture<Void> deleteEleUserOperateRecordDeposit = CompletableFuture.runAsync(() -> {
            Long id = rollBackBO.getDeleteEleUserOperateRecordDepositById();
            if (Objects.nonNull(id)) {
                eleUserOperateRecordService.deleteById(id);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete eleUserOperateRecordDeposit error!", e);
            return null;
        });
        
        CompletableFuture<Void> deleteEleUserOperateRecordMemberCard = CompletableFuture.runAsync(() -> {
            Long id = rollBackBO.getDeleteEleUserOperateRecordMemberCardById();
            if (Objects.nonNull(id)) {
                eleUserOperateRecordService.deleteById(id);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, delete eleUserOperateRecordMemberCard error!", e);
            return null;
        });
        
        CompletableFuture<Void> insertUserBatteryType = CompletableFuture.runAsync(() -> {
            List<UserBatteryType> insertUserBatteryTypeList = rollBackBO.getInsertUserBatteryTypeList();
            if (CollectionUtils.isNotEmpty(insertUserBatteryTypeList)) {
                userBatteryTypeService.batchInsert(insertUserBatteryTypeList);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, insert userBatteryType error!", e);
            return null;
        });
        
        CompletableFuture<Void> rollBackElectricityMemberCardOrder = CompletableFuture.runAsync(() -> {
            ElectricityMemberCardOrder updateMemberCardOrder = rollBackBO.getRollBackElectricityMemberCardOrder();
            if (Objects.nonNull(updateMemberCardOrder)) {
                electricityMemberCardOrderService.updateByID(updateMemberCardOrder);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, rollBack electricityMemberCardOrder error!", e);
            return null;
        });
        
        CompletableFuture<Void> rollBackUserInfo = CompletableFuture.runAsync(() -> {
            UserInfo updateUserInfo = rollBackBO.getRollBackUserInfo();
            if (Objects.nonNull(updateUserInfo)) {
                userInfoService.updateByUid(updateUserInfo);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, rollBack userInfo error!", e);
            return null;
        });
        
        CompletableFuture<Void> rollBackUserBatteryDeposit = CompletableFuture.runAsync(() -> {
            UserBatteryDeposit updateUserBatteryDeposit = rollBackBO.getRollBackUserBatteryDeposit();
            if (Objects.nonNull(updateUserBatteryDeposit)) {
                userBatteryDepositService.update(updateUserBatteryDeposit);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, rollBack userBatteryDeposit error!", e);
            return null;
        });
        
        CompletableFuture<Void> rollBackUserBatteryMemberCard = CompletableFuture.runAsync(() -> {
            UserBatteryMemberCard updateUserBatteryMemberCard = rollBackBO.getRollBackUserBatteryMemberCard();
            if (Objects.nonNull(updateUserBatteryMemberCard)) {
                userBatteryMemberCardService.updateByUid(updateUserBatteryMemberCard);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, rollBack userBatteryMemberCard error!", e);
            return null;
        });
        
        CompletableFuture<Void> rollBackServiceFeeUserInfo = CompletableFuture.runAsync(() -> {
            ServiceFeeUserInfo serviceFeeUserInfo = rollBackBO.getRollBackServiceFeeUserInfo();
            if (Objects.nonNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfo);
            }
        }, threadPool).exceptionally(e -> {
            log.error("NotifyMeiTuanDeliver warn! asyncRollback fail, rollBack serviceFeeUserInfo error!", e);
            return null;
        });
        
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(deleteDepositOrder, deleteMemberCardOrder, deleteUserBatteryType, deleteUserBatteryDeposit,
                deleteUserBatteryMemberCard, deleteServiceFeeUserInfo, deleteEleUserOperateRecordDeposit, deleteEleUserOperateRecordMemberCard, insertUserBatteryType,
                rollBackElectricityMemberCardOrder, rollBackUserInfo, rollBackUserBatteryDeposit, rollBackUserBatteryMemberCard, rollBackServiceFeeUserInfo);
        
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("NotifyMeiTuanDeliver ERROR! asyncRollback fail", e);
        }
        
    }
    
    @Override
    public List<OrderVO> listOrders(OrderQuery query) {
        List<MeiTuanRiderMallOrder> riderMallOrders = this.listOrdersByUid(query);
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
    public LimitTradeVO meiTuanLimitTradeCheck(LimitTradeRequest request, MeiTuanRiderMallConfig meiTuanRiderMallConfig) {
        Integer tenantId = meiTuanRiderMallConfig.getTenantId();
        Long timestamp = request.getTimestamp();
        String sign = request.getSign();
        Long memberCardId = request.getProviderSkuId();
        String phone = request.getAccount();
        LimitTradeVO noLimit = LimitTradeVO.builder().limitResult(Boolean.FALSE).limitType(VirtualTradeStatusEnum.LIMIT_TYPE_NO.getCode()).build();
        LimitTradeVO limit = LimitTradeVO.builder().limitResult(Boolean.TRUE).limitType(VirtualTradeStatusEnum.LIMIT_TYPE_OLD_USER.getCode()).build();
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
        // 套餐不存在：不限制
        if (Objects.isNull(batteryMemberCard)) {
            return noLimit;
        }
        
        // 用户不存在：不限制
        UserInfo userInfo = userInfoService.queryUserInfoByPhone(phone, tenantId);
        if (Objects.isNull(userInfo)) {
            return noLimit;
        }
        
        // 判断套餐用户分组和用户的用户分组是否匹配
        List<UserInfoGroupNamesBO> userInfoGroups = userInfoGroupDetailService.listGroupByUid(
                UserInfoGroupDetailQuery.builder().uid(userInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
        
        if (CollectionUtils.isNotEmpty(userInfoGroups)) {
            // 自定义用户分组用户不可购买系统分组套餐：限制
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                log.warn("MeiTuanLimitTradeCheck warn! UseInfoGroup cannot purchase systemGroup memberCard, uid={}, mid={}, timestamp={}, sign={}", userInfo.getUid(), memberCardId,
                        timestamp, sign);
                return limit;
            }
            
            List<Long> userGroupIds = userInfoGroups.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
            userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
            // 自定义用户分组中没有该用户，不可购买指定套餐：限制
            if (CollectionUtils.isEmpty(userGroupIds)) {
                log.warn("MeiTuanLimitTradeCheck warn! UseInfoGroup not contain systemGroup, uid={}, mid={}, timestamp={}, sign={}", userInfo.getUid(), memberCardId, timestamp,
                        sign);
                return limit;
            }
        } else {
            if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                log.warn("MeiTuanLimitTradeCheck warn! SystemGroup cannot purchase useInfoGroup memberCard, uid={}, mid={}, timestamp={}, sign={}", userInfo.getUid(), memberCardId,
                        timestamp, sign);
                return limit;
            }
            
            // 老用户不可购买新套餐：限制
            if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                log.warn("MeiTuanLimitTradeCheck warn! Old use cannot purchase new rentType memberCard, uid={}, mid={}, timestamp={}, sign={}", userInfo.getUid(), memberCardId,
                        timestamp, sign);
                return limit;
            }
            
            // 新用户不可购买续费套餐：限制
            if (Objects.equals(userInfo.getPayCount(), 0) && BatteryMemberCard.RENT_TYPE_OLD.equals(batteryMemberCard.getRentType())) {
                log.warn("MeiTuanLimitTradeCheck warn! New use cannot purchase old rentType memberCard, uid={}, mid={}, timestamp={}, sign={}", userInfo.getUid(), memberCardId,
                        timestamp, sign);
                return limit;
            }
        }
        
        return noLimit;
    }
}
