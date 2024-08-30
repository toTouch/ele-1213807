package com.xiliulou.electricity.service.impl.meituan;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCardPackage;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.meituan.MeiTuanRiderMallOrderMapper;
import com.xiliulou.electricity.query.UserBatteryDepositAndMembercardQuery;
import com.xiliulou.electricity.query.meituan.OrderQuery;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.meituan.LimitTradeRequest;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
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
import com.xiliulou.thirdmall.entity.meituan.request.virtualtrade.SyncOrderReq;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrderRsp;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.OrdersDataRsp;
import com.xiliulou.thirdmall.entity.meituan.response.virtualtrade.SkuRsp;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import com.xiliulou.thirdmall.service.meituan.virtualtrade.VirtualTradeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 18:06:11
 */
@Slf4j
@Service
public class MeiTuanRiderMallOrderServiceImpl implements MeiTuanRiderMallOrderService {
    
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
    
    @Slave
    @Override
    public MeiTuanRiderMallOrder queryByOrderId(String orderId, String phone, Long uid) {
        return meiTuanRiderMallOrderMapper.selectByOrderId(orderId, phone, uid);
    }
    
    @Slave
    @Override
    public List<MeiTuanRiderMallOrder> listOrdersByUidAndPhone(OrderQuery query) {
        return meiTuanRiderMallOrderMapper.selectByUidAndPhone(query);
    }
    
    @Override
    public List<MeiTuanRiderMallOrder> listAllUnSyncedOrder(Integer tenantId) {
        List<MeiTuanRiderMallOrder> list = new ArrayList<>();
        int offset = 0;
        int size = 200;
        
        while (true) {
            List<MeiTuanRiderMallOrder> orders = this.listUnSyncedOrder(tenantId, offset, size);
            if (CollectionUtils.isEmpty(orders)) {
                break;
            }
            
            list.addAll(orders);
            offset += size;
        }
        return list;
    }
    
    @Slave
    @Override
    public List<MeiTuanRiderMallOrder> listUnSyncedOrder(Integer tenantId, Integer offset, Integer size) {
        return meiTuanRiderMallOrderMapper.selectListUnSyncedOrder(tenantId, offset, size);
    }
    
    /**
     * 1.创建套餐成功 2.通知美团发货 3.发货失败，回滚步骤1的数据
     */
    @Override
    public Triple<Boolean, String, Object> createBatteryMemberCardOrder(OrderQuery query) {
        Integer tenantId = query.getTenantId();
        Long uid = query.getUid();
        Long memberCardId = query.getPackageId();
        String meiTuanOrderId = query.getOrderId();
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.CACHE_MEI_TUAN_CREATE_BATTERY_MEMBER_CARD_ORDER_LOCK_KEY + uid, "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            // 校验是否开启美团商城
            MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.checkEnableMeiTuanRiderMall(tenantId);
            if (Objects.isNull(meiTuanRiderMallConfig)) {
                log.warn("MeiTuan preCheckForCreateOrder fail! not found meiTuanRiderMallConfig, uid={}, tenantId={}", uid, tenantId);
                return Triple.of(false, "120135", "兑换失败，请联系客服处理");
            }
            
            // 校验美团订单是否存在
            MeiTuanRiderMallOrder meiTuanRiderMallOrder = this.queryByOrderId(meiTuanOrderId, null, uid);
            if (Objects.isNull(meiTuanRiderMallOrder)) {
                log.warn("MeiTuan preCheckForCreateOrder fail! not found meiTuanOrderId, uid={}, meiTuanOrderId={}", uid, meiTuanOrderId);
                return Triple.of(false, "120131", "未能查询到该美团订单号码，请再次确认后操作");
            }
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("MeiTuan preCheckForCreateOrder fail! not found user,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(memberCardId);
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("MeiTuan preCheckForCreateOrder fail! not found batteryMemberCard,uid={}, memberCardId={}", uid, memberCardId);
                return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("MeiTuan preCheckForCreateOrder fail! user is unUsable,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("MeiTuan preCheckForCreateOrder fail! user not auth,uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }
            
            if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                log.warn("MeiTuan preCheckForCreateOrder fail! batteryMemberCard is down,uid={},mid={}", uid, memberCardId);
                return Triple.of(false, "120136", "兑换套餐已下架，兑换失败，请联系客服处理");
            }
            
            if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                    batteryMemberCard.getFranchiseeId())) {
                log.warn("MeiTuan preCheckForCreateOrder fail! batteryMemberCard franchiseeId not equals,uid={},mid={}", uid, memberCardId);
                return Triple.of(false, "120132", "美团商城订单与绑定加盟商不一致，请核实后操作");
            }
            
            // 检查是否为自主续费状态
            Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(uid);
            if (!userRenewalStatus) {
                log.warn("MeiTuan preCheckForCreateOrder fail! user renewal status is false, uid={}, mid={}, meiTuanOrderId={}", uid, memberCardId, meiTuanOrderId);
                return Triple.of(false, "120133", "您是站点代付用户，无法使用美团商城换电卡");
            }
            
            // 判断套餐用户分组和用户的用户分组是否匹配
            List<UserInfoGroupNamesBO> userInfoGroups = userInfoGroupDetailService.listGroupByUid(
                    UserInfoGroupDetailQuery.builder().uid(userInfo.getUid()).tenantId(TenantContextHolder.getTenantId()).build());
            if (CollectionUtils.isNotEmpty(userInfoGroups)) {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_SYSTEM)) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! batteryMemberCard down, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120136", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
                
                List<Long> userGroupIds = userInfoGroups.stream().map(UserInfoGroupNamesBO::getGroupId).collect(Collectors.toList());
                userGroupIds.retainAll(JsonUtil.fromJsonArray(batteryMemberCard.getUserInfoGroupIds(), Long.class));
                if (CollectionUtils.isEmpty(userGroupIds)) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! UseInfoGroup not contain systemGroup, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120136", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
            } else {
                if (Objects.equals(batteryMemberCard.getGroupType(), BatteryMemberCard.GROUP_TYPE_USER)) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! SystemGroup cannot purchase useInfoGroup memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120136", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
                
                if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! Old use cannot purchase new rentType memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120136", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
                
                if (Objects.equals(userInfo.getPayCount(), 0) && BatteryMemberCard.RENT_TYPE_OLD.equals(batteryMemberCard.getRentType())) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! New use cannot purchase old rentType memberCard, uid={}, mid={}", uid, memberCardId);
                    return Triple.of(false, "120136", "兑换套餐已下架，兑换失败，请联系客服处理");
                }
            }
            
            // 押金缴纳状态
            if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
                if (Objects.isNull(userBatteryDeposit)) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! not found userBatteryDeposit,uid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0001", "用户押金信息不存在");
                }
                
                // 是否有正在进行中的退押
                Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
                if (refundCount > 0) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! have refunding order,uid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0047", "电池押金退款中");
                }
                
                // 是否有正在进行中的退租
                List<BatteryMembercardRefundOrder> batteryMemberCardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(uid);
                if (CollectionUtils.isNotEmpty(batteryMemberCardRefundOrders)) {
                    log.warn("MeiTuan preCheckForCreateOrder fail! battery memberCard refund review,uid={}", uid);
                    return Triple.of(false, "100018", "套餐租金退款审核中");
                }
                
                // 用户绑定的套餐
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
                if (Objects.nonNull(userBatteryMemberCard)) {
                    if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
                        log.warn("MeiTuan preCheckForCreateOrder fail! userBatteryMemberCard disable,uid={},mid={}", uid, memberCardId);
                        return Triple.of(false, "100247", "用户套餐冻结中，不允许操作");
                    }
                    
                    // 是否有滞纳金
                    Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo,
                            userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
                    if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                        log.warn("MeiTuan preCheckForCreateOrder fail! user exist battery service fee,uid={},mid={}", uid, userBatteryMemberCard.getMemberCardId());
                        return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
                    }
                    
                    // 绑定的套餐
                    BatteryMemberCard userBindBatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                    
                    // 续费套餐
                    saveRenewalUserBatteryMemberCardOrder(userInfo, batteryMemberCard, userBatteryMemberCard, userBindBatteryMemberCard);
                }
            } else {
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
                saveUserInfoAndOrder(userInfo, batteryMemberCard, userBatteryMemberCard, query);
            }
            
            // 通知美团发货
            notifyMeiTuanDeliver(meiTuanRiderMallConfig, meiTuanOrderId);
            // 发货失败，回滚
            
            return null;
        } finally {
            redisService.delete(CacheConstant.CACHE_MEI_TUAN_CREATE_BATTERY_MEMBER_CARD_ORDER_LOCK_KEY + uid);
        }
    }
    
    
    @Transactional(rollbackFor = Exception.class)
    public ElectricityMemberCardOrder saveRenewalUserBatteryMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, UserBatteryMemberCard userBatteryMemberCard,
            BatteryMemberCard userBindBatteryMemberCard) {
        
        ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
        memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        memberCardOrder.setMemberCardId(batteryMemberCard.getId());
        memberCardOrder.setUid(userInfo.getUid());
        memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        memberCardOrder.setCardName(batteryMemberCard.getName());
        memberCardOrder.setPayAmount(batteryMemberCard.getRentPrice());
        memberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
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
            
            // 如果用户原来绑定的有套餐 套餐过期了，需要把原来绑定的套餐订单状态更新为已过期
            if (StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                ElectricityMemberCardOrder electricityMemberCardOrderUpdateUseStatus = new ElectricityMemberCardOrder();
                electricityMemberCardOrderUpdateUseStatus.setOrderId(userBatteryMemberCard.getOrderId());
                electricityMemberCardOrderUpdateUseStatus.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                electricityMemberCardOrderUpdateUseStatus.setUpdateTime(System.currentTimeMillis());
                electricityMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdateUseStatus);
            }
            
            // 更新用户电池型号
            userBatteryTypeService.updateUserBatteryType(memberCardOrder, userInfo);
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
        }
        
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
        
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        if (Objects.nonNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
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
        
        this.insert(memberCardOrder);
        
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
        
        return memberCardOrder;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public ElectricityMemberCardOrder saveUserInfoAndOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, UserBatteryMemberCard userBatteryMemberCard,
            UserBatteryDepositAndMembercardQuery query) {
        BigDecimal deposit = query.getBatteryDeposit();
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid()))
                .uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(deposit).status(EleDepositOrder.STATUS_SUCCESS)
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId())
                .payType(EleDepositOrder.OFFLINE_PAYMENT).storeId(query.getStoreId()).mid(batteryMemberCard.getId()).modelType(0).build();
        depositOrderService.insert(eleDepositOrder);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = ElectricityMemberCardOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid())).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).status(ElectricityMemberCardOrder.STATUS_SUCCESS).memberCardId(batteryMemberCard.getId()).uid(userInfo.getUid())
                .maxUseCount(batteryMemberCard.getUseCount()).cardName(batteryMemberCard.getName()).payAmount(batteryMemberCard.getRentPrice()).userName(userInfo.getName())
                .validDays(batteryMemberCard.getValidDays()).tenantId(batteryMemberCard.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId())
                .payCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1).payType(ElectricityMemberCardOrder.OFFLINE_PAYMENT).refId(null)
                .sendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null)
                .useStatus(ElectricityMemberCardOrder.USE_STATUS_USING).source(ElectricityMemberCardOrder.SOURCE_NOT_SCAN).storeId(query.getStoreId())
                .couponIds(batteryMemberCard.getCouponIds()).build();
        this.baseMapper.insert(electricityMemberCardOrder);
        
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
        
        List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
        if (CollectionUtils.isNotEmpty(batteryTypeList)) {
            userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo));
        }
        
        UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
        userBatteryDeposit.setUid(userInfo.getUid());
        userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
        userBatteryDeposit.setDid(eleDepositOrder.getMid());
        userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
        userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT);
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setCreateTime(System.currentTimeMillis());
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        if (!Objects.equals(batteryMemberCard.getDeposit(), deposit)) {
            userBatteryDeposit.setDepositModifyFlag(UserBatteryDeposit.DEPOSIT_MODIFY_YES);
            userBatteryDeposit.setBeforeModifyDeposit(batteryMemberCard.getDeposit());
        }
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);
        
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
        userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
        userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard) + 1);
        if (Objects.isNull(userBatteryMemberCard)) {
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
        } else {
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
        ServiceFeeUserInfo serviceFeeUserInfoInsert = new ServiceFeeUserInfo();
        serviceFeeUserInfoInsert.setServiceFeeGenerateTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
        serviceFeeUserInfoInsert.setUid(userBatteryMemberCardUpdate.getUid());
        serviceFeeUserInfoInsert.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        serviceFeeUserInfoInsert.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoInsert.setTenantId(electricityMemberCardOrder.getTenantId());
        serviceFeeUserInfoInsert.setCreateTime(System.currentTimeMillis());
        serviceFeeUserInfoInsert.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
        serviceFeeUserInfoInsert.setDisableMemberCardNo("");
        if (Objects.nonNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsert);
        } else {
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
        
        EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                .uid(electricityMemberCardOrder.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername())
                .oldValidDays((int) oldValidDays).newValidDays((int) newValidDays).oldMaxUseCount(oldMaxUseCount).newMaxUseCount(newMaxUseCount)
                .tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);
        
        return electricityMemberCardOrder;
    }
    
    private EleDepositOrder generateDepositOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, ElectricityPayParams electricityPayParams) {
        // 生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        
        return EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(new BigDecimal(0))
                .status(EleDepositOrder.STATUS_SUCCESS).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId())
                .franchiseeId(batteryMemberCard.getFranchiseeId()).payType(EleDepositOrder.MEITUAN_DEPOSIT_PAYMENT).storeId(userInfo.getStoreId()).mid(batteryMemberCard.getId())
                .modelType(0).paramFranchiseeId(electricityPayParams.getFranchiseeId()).wechatMerchantId(electricityPayParams.getWechatMerchantId()).build();
    }
    
    private ElectricityMemberCardOrder generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, ElectricityPayParams electricityPayParams,
            BigDecimal payAmount) {
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(batteryMemberCard.getId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        electricityMemberCardOrder.setCardName(batteryMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(batteryMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(batteryMemberCard.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);
        electricityMemberCardOrder.setRefId(NumberConstant.ZERO_L);
        electricityMemberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        electricityMemberCardOrder.setStoreId(userInfo.getStoreId());
        electricityMemberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        electricityMemberCardOrder.setParamFranchiseeId(electricityPayParams.getFranchiseeId());
        electricityMemberCardOrder.setWechatMerchantId(electricityPayParams.getWechatMerchantId());
        
        return electricityMemberCardOrder;
    }
    
    private Triple<Boolean, String, Object> notifyMeiTuanDeliver(MeiTuanRiderMallConfig config, String orderId, Integer env) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        
        virtualTradeService.deliverOrder(apiConfig, orderId, null, VirtualTradeStatusEnum.VP_RECHARGE_STATUS_SUCCESS.getCode());
    }
    
    private Triple<Boolean, String, Object> handleRollback() {
    
    }
    
    @Override
    public List<OrderVO> listOrders(OrderQuery query) {
        Long uid = query.getUid();
        Integer tenantId = query.getTenantId();
        
        // 判断是否需要从美团拉取订单
        Boolean needFetchOrders = this.ifNeedFetchOrders(tenantId, query.getOrderId(), uid, query.getGapSecond());
        if (needFetchOrders) {
            MeiTuanRiderMallConfig config = meiTuanRiderMallConfigService.queryByTenantIdFromCache(tenantId);
            if (Objects.isNull(config)) {
                log.warn("ListOrders warn! MeiTuanRiderMallConfig is null, uid={}", uid);
                return Collections.emptyList();
            }
            
            MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                    .host(meiTuanRiderMallHostConfig.getHost()).build();
            // 分页拉取近N分钟的订单，默认近5分钟
            long endTime = System.currentTimeMillis() / 1000;
            Integer recentMinute = query.getRecentMinute();
            recentMinute = Objects.isNull(recentMinute) ? 5 : recentMinute;
            Long startTime = endTime - recentMinute * 60;
            
            // 从美团拉取订单
            List<OrderRsp> orderRspList = this.fetchOrders(apiConfig, startTime, endTime);
            if (CollectionUtils.isNotEmpty(orderRspList)) {
                // 持久化
                this.handleBatchInsert(orderRspList, config.getTenantId());
            }
        }
        
        List<MeiTuanRiderMallOrder> riderMallOrders = this.listOrdersByUidAndPhone(query);
        if (CollectionUtils.isEmpty(riderMallOrders)) {
            return Collections.emptyList();
        }
        
        return riderMallOrders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    private Boolean ifNeedFetchOrders(Integer tenantId, String orderId, Long uid, Integer gapSecond) {
        // 数据库中如果没有该订单，则需要拉取
        MeiTuanRiderMallOrder riderMallOrder = this.queryByOrderId(orderId, null, uid);
        if (Objects.isNull(riderMallOrder)) {
            return Boolean.TRUE;
        }
        
        // 获取定时任务上次执行时间
        String lastTaskTime = redisService.get(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_ORDER_FETCH_TIME + tenantId);
        // 如果查不到场次执行时间，则需要拉取
        if (StringUtils.isBlank(lastTaskTime)) {
            return Boolean.TRUE;
        }
        
        // 判断当前时间与定时任务上次执行时间间隔是否大于指定秒数，默认30秒，如果大于等于则需要拉取
        gapSecond = Objects.isNull(gapSecond) ? 30 : gapSecond;
        return (System.currentTimeMillis() - Long.parseLong(lastTaskTime)) / 1000 >= gapSecond;
    }
    
    /**
     * 定时任务：从美团拉取订单
     */
    @Override
    public void handelFetchOrderTask(String sessionId, Long startTime, Integer recentDay) {
        List<MeiTuanRiderMallConfig> configs = meiTuanRiderMallConfigService.listAll();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        
        // 遍历租户
        configs.forEach(config -> handleFetchOrdersByTenant(config, recentDay));
        
        Long costTime = System.currentTimeMillis() - startTime;
        log.info("MeiTuanRiderMallFetchOrderTask end! sessionId={}, costTime={}", sessionId, costTime);
    }
    
    @Override
    public void handelSyncOrderStatusTask(String sessionId, long startTime) {
        List<MeiTuanRiderMallConfig> configs = meiTuanRiderMallConfigService.listAll();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        
        // 遍历租户
        configs.forEach(this::handleSyncOrderStatusByTenant);
        
        Long costTime = System.currentTimeMillis() - startTime;
        log.info("MeiTuanRiderMallSyncOrderStatusTask end! sessionId={}, costTime={}", sessionId, costTime);
    }
    
    private void handleSyncOrderStatusByTenant(MeiTuanRiderMallConfig config) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        
        List<MeiTuanRiderMallOrder> riderMallOrders = this.listAllUnSyncedOrder(config.getTenantId());
        if (CollectionUtils.isEmpty(riderMallOrders)) {
            return;
        }
        
        List<MeiTuanRiderMallOrder> updateList = new ArrayList<>();
        List<List<MeiTuanRiderMallOrder>> partition = ListUtils.partition(riderMallOrders, 20);
        partition.forEach(orders -> {
            List<SyncOrderReq> syncOrderReqOrderList = orders.stream()
                    .map(order -> SyncOrderReq.builder().orderId(order.getMeiTuanOrderId()).orderHandleResonStatus(order.getOrderSyncStatus()).build())
                    .collect(Collectors.toList());
            
            // 调用美团接口
            Boolean result = virtualTradeService.syncOrderResult(apiConfig, syncOrderReqOrderList);
            if (result) {
                updateList.addAll(orders);
            }
        });
        
        if (CollectionUtils.isNotEmpty(updateList)) {
            // 对”已处理“的订单，修改状态为”已对账“；”未处理“的不做更改
            meiTuanRiderMallOrderMapper.batchUpdateSyncOrderStatus(updateList);
        }
    }
    
    private void handleFetchOrdersByTenant(MeiTuanRiderMallConfig config, Integer recentDay) {
        MeiTuanRiderMallApiConfig apiConfig = MeiTuanRiderMallApiConfig.builder().appId(config.getAppId()).appKey(config.getAppKey()).secret(config.getSecret())
                .host(meiTuanRiderMallHostConfig.getHost()).build();
        
        // 分页拉取最近N天的订单
        long endTime = System.currentTimeMillis() / 1000;
        Long startTime = endTime - recentDay * 24 * 60 * 60;
        
        // 从美团拉取订单
        List<OrderRsp> orderRspList = this.fetchOrders(apiConfig, startTime, endTime);
        if (CollectionUtils.isEmpty(orderRspList)) {
            return;
        }
        
        // 持久化
        Integer tenantId = config.getTenantId();
        this.handleBatchInsert(orderRspList, config.getTenantId());
        
        // redis记录租户本次定时任务执行时间
        redisService.saveWithString(CacheConstant.CACHE_MEI_TUAN_RIDER_MALL_ORDER_FETCH_TIME + tenantId, System.currentTimeMillis());
    }
    
    private List<OrderRsp> fetchOrders(MeiTuanRiderMallApiConfig apiConfig, Long startTime, Long endTime) {
        Long cursor = null;
        Integer pageSize = 100;
        List<OrderRsp> list = new ArrayList<>();
        
        while (true) {
            OrdersDataRsp ordersDataRsp = virtualTradeService.listAllOrders(apiConfig, cursor, pageSize, startTime, endTime);
            if (Objects.isNull(ordersDataRsp)) {
                break;
            }
            
            if (ordersDataRsp.getHasNext()) {
                cursor += ordersDataRsp.getCursor();
                ordersDataRsp = virtualTradeService.listAllOrders(apiConfig, cursor, pageSize, startTime, endTime);
                
                list.addAll(ordersDataRsp.getList());
            }
        }
        
        return list;
    }
    
    private void handleBatchInsert(List<OrderRsp> list, Integer tenantId) {
        List<MeiTuanRiderMallOrder> insertList = new ArrayList<>();
        
        List<List<OrderRsp>> partition = ListUtils.partition(list, 200);
        partition.forEach(orders -> orders.forEach(order -> {
            String orderId = order.getOrderId();
            SkuRsp skuRsp = order.getSkuList().get(0);
            String phone = skuRsp.getAccount();
            
            MeiTuanRiderMallOrder existOrder = this.queryByOrderId(orderId, phone, null);
            if (Objects.nonNull(existOrder)) {
                return;
            }
            
            UserInfo userInfo = userInfoService.queryUserInfoByPhone(phone, tenantId);
            
            MeiTuanRiderMallOrder meiTuanRiderMallOrder = MeiTuanRiderMallOrder.builder().meiTuanOrderId(orderId).meiTuanOrderTime(order.getOrderTime() * 1000)
                    .meiTuanOrderStatus(order.getOrderStatus()).meiTuanActuallyPayPrice(new BigDecimal(order.getActuallyPayPrice()))
                    .meiTuanVirtualRechargeType(skuRsp.getVirtualRechargeType()).meiTuanAccount(phone).orderId(StringUtils.EMPTY)
                    .orderSyncStatus(VirtualTradeStatusEnum.ORDER_HANDLE_REASON_STATUS_UNHANDLED.getCode()).orderUseStatus(VirtualTradeStatusEnum.ORDER_USE_STATUS_UNUSED.getCode())
                    .packageId(skuRsp.getSkuId()).packageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()).uid(Optional.ofNullable(userInfo.getUid()).orElse(0L))
                    .tenantId(tenantId).delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            
            insertList.add(meiTuanRiderMallOrder);
        }));
        
        // 批量入库
        if (CollectionUtils.isNotEmpty(insertList)) {
            meiTuanRiderMallOrderMapper.batchInsert(insertList);
        }
    }
    
    @Override
    public LimitTradeVO meiTuanLimitTradeCheck(LimitTradeRequest request) {
        Long timestamp = request.getTimestamp();
        String sign = request.getSign();
        Long memberCardId = request.getProviderSkuId();
        String phone = request.getAccount();
        LimitTradeVO noLimit = LimitTradeVO.builder().limitResult(Boolean.FALSE).build();
        LimitTradeVO limit = LimitTradeVO.builder().limitResult(Boolean.TRUE).limitType(VirtualTradeStatusEnum.LIMIT_TRADE_OLD_USER.getCode()).build();
        
        MeiTuanRiderMallConfig meiTuanRiderMallConfig = meiTuanRiderMallConfigService.queryByConfig(
                MeiTuanRiderMallConfig.builder().appId(request.getAppId()).appKey(request.getAppKey()).build());
        // 配置不存在：不限制
        if (Objects.isNull(meiTuanRiderMallConfig)) {
            return noLimit;
        }
        
        Integer tenantId = meiTuanRiderMallConfig.getTenantId();
        
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
