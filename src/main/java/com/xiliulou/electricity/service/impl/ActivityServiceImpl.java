package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantInviterModifyRecordConstant;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.InvitationActivityUser;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityHistory;
import com.xiliulou.electricity.entity.JoinShareMoneyActivityRecord;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.UserInfoActivitySourceEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.model.BatteryMemberCardMerchantRebate;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.BatteryMemberCardOrderCouponService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InvitationActivityRecordService;
import com.xiliulou.electricity.service.InvitationActivityUserService;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.lostuser.LostUserBizService;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ActivityUserInfoVO;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-20-11:32
 */
@Slf4j
@Service("activityService")
public class ActivityServiceImpl implements ActivityService {
    
    protected XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("ACTIVITY_HANDLE_THREAD_POOL", 4, "activity_handle_thread");
    
    @Autowired
    private InvitationActivityUserService invitationActivityUserService;
    
    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    private ElectricityMemberCardOrderService eleMemberCardOrderService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    JoinShareActivityRecordService joinShareActivityRecordService;
    
    @Autowired
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    
    @Autowired
    ShareActivityRecordService shareActivityRecordService;
    
    @Autowired
    JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;
    
    @Autowired
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    
    @Autowired
    ShareMoneyActivityService shareMoneyActivityService;
    
    @Autowired
    UserAmountService userAmountService;
    
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
    
    @Autowired
    InvitationActivityRecordService invitationActivityRecordService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private ShareActivityService shareActivityService;
    
    @Autowired
    private CarRentalPackageOrderService carRentalPackageOrderService;
    
    @Autowired
    private UserBizService userBizService;
    
    @Autowired
    RedisService redisService;
    
    @Resource
    private UserInfoExtraService userInfoExtraService;
    
    @Resource
    private LostUserBizService lostUserBizService;

    @Resource
    private MerchantInviterModifyRecordService merchantInviterModifyRecordService;

    @Autowired
    RocketMqService rocketMqService;
    
    /**
     * 用户是否有权限参加此活动
     *
     * @return
     */
    @Slave
    @Override
    public Triple<Boolean, String, Object> userActivityInfo() {
        ActivityUserInfoVO activityUserInfoVO = new ActivityUserInfoVO();
        
        List<InvitationActivityUser> invitationActivityUserList = invitationActivityUserService.selectByUid(SecurityUtils.getUid());
        activityUserInfoVO.setInvitationActivity(CollectionUtils.isEmpty(invitationActivityUserList) ? Boolean.FALSE : Boolean.TRUE);
        
        return Triple.of(true, "", activityUserInfoVO);
    }
    
    @Deprecated
    @Override
    public Triple<Boolean, String, Object> updateCouponByPackage(String orderNo, Integer packageType) {
        
        if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)) {
            //获取套餐订单优惠券
            List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(orderNo);
            if (CollectionUtils.isEmpty(userCouponIds)) {
                return Triple.of(true, "", null);
            }
            
            //更新优惠券状态
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                Set<Integer> couponIds = userCouponIds.parallelStream().map(Long::intValue).collect(Collectors.toSet());
                userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_USED, orderNo));
            }
            
            return Triple.of(true, "", null);
        } else {
            //根据当前租车订单号获取 当前订单所使用的优惠券信息
            
        }
        
        return null;
    }
    
    /**
     * 购买套餐后处理活动业务
     *
     * @param activityProcessDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleActivityByPackage(ActivityProcessDTO activityProcessDTO) {
        
        String orderNo = activityProcessDTO.getOrderNo();
        Integer packageType = activityProcessDTO.getType();
        MDC.put(CommonConstant.TRACE_ID, activityProcessDTO.getTraceId());
        log.info("Activity by package flow start, orderNo = {}, package type = {}", orderNo, packageType);
        
        String value = orderNo + "_" + packageType;
        if (!redisService.setNx(CacheConstant.CACHE_HANDLE_ACTIVITY_PACKAGE_PURCHASE_KEY + value, value, TimeConstant.TEN_SECOND_MILLISECOND, false)) {
            log.error("Handle activity by purchase package error, operations frequently, order number = {}, package type = {}", orderNo, packageType);
        }
        
        try {
            
            if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(packageType)) {
                log.info("Activity flow for battery package, orderNo = {}", orderNo);
                ElectricityMemberCardOrder electricityMemberCardOrder = eleMemberCardOrderService.selectByOrderNo(orderNo);
                
                if (Objects.isNull(electricityMemberCardOrder)) {
                    log.info("Activity flow for battery package error, Not found for battery package order, order number = {}", orderNo);
                    return Triple.of(false, "110001", "当前换电套餐订单不存在");
                }
                
                Long uid = electricityMemberCardOrder.getUid();
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromDB(uid);
                
                if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                    log.info("handle activity error ! package invalid! uid = {}, order no = {}", uid, orderNo);
                    return Triple.of(false, "110002", "当前换电套餐已暂停");
                }
                
                UserInfo userInfo = userInfoService.queryByUidFromDbIncludeDelUser(uid);
                if (Objects.isNull(userInfo)) {
                    log.warn("handle activity error ! Not found userInfo, joinUid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
                }
                
                UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
                if (Objects.isNull(userInfoExtra)) {
                    log.warn("handle activity error ! Not found userInfoExtra, joinUid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
                }

                log.info("Activity flow for battery package, is old user= {}, userInfoExtra = {}", userInfo.getPayCount(), userInfoExtra);
                
                // 处理活动(530需求:以最新的参与活动为准)
                if (Objects.equals(userInfoExtra.getLostUserStatus(), YesNoEnum.YES.getCode())) {
                    // 流失用户电套餐活动处理
                    handleLostUserActivityForBatteryPackageType(userInfo, userInfoExtra, uid, electricityMemberCardOrder, packageType);
                } else {
                    handleActivityForBatteryPackageType(userInfo, userInfoExtra, uid, electricityMemberCardOrder, packageType);
                }
            } else {
                //开始处理租车，车电一体购买套餐后的活动
                log.info("Activity flow for car Rental or car with battery package, orderNo = {}", orderNo);
                CarRentalPackageOrderPo carRentalPackageOrderPo = carRentalPackageOrderService.selectByOrderNo(orderNo);
                if (Objects.isNull(carRentalPackageOrderPo)) {
                    log.info("Activity flow for car Rental or car with battery package error, Not found for car rental package, order number = {}", orderNo);
                    return Triple.of(false, "110003", "当前租车/车电一体套餐订单不存在");
                }
                
                Long uid = carRentalPackageOrderPo.getUid();
                UserInfo userInfo = userInfoService.queryByUidFromDbIncludeDelUser(uid);
                if (Objects.isNull(userInfo)) {
                    log.warn("Activity flow for car Rental or car with battery package error! Not found userInfo, joinUid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
                }
                
                UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
                if (Objects.isNull(userInfoExtra)) {
                    log.warn("Activity flow for car Rental or car with battery package error! Not found userInfoExtra, joinUid={}", uid);
                    return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
                }

                log.info("Activity flow for car Rental or car with battery package, is old user= {}, userInfoExtra = {}", userInfo.getPayCount(), userInfoExtra);
                
                // 处理活动(530需求:以最新的参与活动为准)
                if (Objects.equals(userInfoExtra.getLostUserStatus(), YesNoEnum.YES.getCode())) {
                    // 流失用户车活动处理
                    handleLostUserActivityForCarPackageType(userInfo, userInfoExtra, uid, carRentalPackageOrderPo, packageType, orderNo);
                } else {
                    handleActivityForCarPackageType(userInfo, userInfoExtra, uid, carRentalPackageOrderPo, packageType, orderNo);
                }
            }
            
        } catch (Exception e) {
            log.warn("handle activity for purchase package error, order number = {}, package type = {}", orderNo, packageType, e);
            throw new BizException("110004", e.getMessage());
        } finally {
            redisService.delete(CacheConstant.CACHE_HANDLE_ACTIVITY_PACKAGE_PURCHASE_KEY + value);
            MDC.clear();
        }
        
        log.info("Activity by package flow end, orderNo = {}, package type = {}", orderNo, packageType);
        
        return Triple.of(true, "", null);
    }
    
    /**
     * 处理流失用户电套餐活动
     * @param userInfo
     * @param userInfoExtra
     * @param uid
     * @param electricityMemberCardOrder
     * @param packageType
     */
    private void handleLostUserActivityForBatteryPackageType(UserInfo userInfo, UserInfoExtra userInfoExtra, Long uid, ElectricityMemberCardOrder electricityMemberCardOrder, Integer packageType) {
        // 查询骑手当前绑定活动的邀请信息
        MerchantInviterVO successInviterVO = userInfoExtraService.querySuccessInviter(uid);
        if (Objects.isNull(successInviterVO)) {
            log.info("HANDLE LOST USER ACTIVITY BATTERY PACKAGE TYPE INFO! inviter is empty, uid：{}",uid);
        }

        // 流失用户修改为老用户且解绑活动
        lostUserBizService.updateLostUserStatusAndUnbindActivity(userInfo.getTenantId(), uid, successInviterVO);

        Integer latestActivitySource = userInfoExtra.getLatestActivitySource();
        Integer tenantId = userInfo.getTenantId();

        Long oldInviterUid = null;
        String oldInviterName = null;
        Integer inviterSource = null;
        if (Objects.nonNull(successInviterVO)) {
            oldInviterUid = successInviterVO.getInviterUid();
            oldInviterName = successInviterVO.getInviterName();
            inviterSource = successInviterVO.getInviterSource();
        }

        boolean isSuccessModifyInviter = false;

        // 商户扫码线上支付
        if (Objects.equals(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode(), latestActivitySource) && Objects.equals(ElectricityMemberCardOrder.ONLINE_PAYMENT,
                electricityMemberCardOrder.getPayType())) {
            //用户绑定商户
            Triple<Boolean, String, Object> triple = userInfoExtraService.bindMerchantForLostUser(userInfo, electricityMemberCardOrder.getOrderId(),
                    electricityMemberCardOrder.getMemberCardId());

            //商户返利
            if (triple.getLeft()) {
                sendMerchantRebateMQ(uid, electricityMemberCardOrder.getOrderId(), YesNoEnum.YES.getCode());
                // 新租修改邀请记录

                Merchant merchant = (Merchant) triple.getRight();
                // 当原来有邀请人的时候则需添加邀请记录
                if (Objects.nonNull(successInviterVO)) {
                    // 新增修改记录
                    MerchantInviterModifyRecord merchantInviterModifyRecord = MerchantInviterModifyRecord.builder().uid(uid).inviterUid(merchant.getUid())
                            .inviterName(Optional.ofNullable(merchant).orElse(new Merchant()).getName()).oldInviterUid(oldInviterUid)
                            .oldInviterName(oldInviterName).oldInviterSource(inviterSource).merchantId(merchant.getId()).franchiseeId(merchant.getFranchiseeId()).tenantId(tenantId)
                            .operator(NumberConstant.ZERO_L).remark(MerchantInviterModifyRecordConstant.LOST_USER_MODIFY_INVITER_REMARK).delFlag(MerchantConstant.DEL_NORMAL)
                            .createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis()).build();

                    merchantInviterModifyRecordService.insertOne(merchantInviterModifyRecord);

                    isSuccessModifyInviter = true;
                } else {
                    // 新增修改记录
                    MerchantInviterModifyRecord merchantInviterModifyRecord = MerchantInviterModifyRecord.builder().uid(uid).inviterUid(merchant.getUid())
                            .inviterName(Optional.ofNullable(merchant).orElse(new Merchant()).getName()).oldInviterUid(NumberConstant.ZERO_L)
                            .oldInviterName("").oldInviterSource(0).merchantId(merchant.getId()).franchiseeId(merchant.getFranchiseeId()).tenantId(tenantId)
                            .operator(NumberConstant.ZERO_L).remark(MerchantInviterModifyRecordConstant.LOST_USER_MODIFY_INVITER_REMARK).delFlag(MerchantConstant.DEL_NORMAL)
                            .createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis()).build();

                    merchantInviterModifyRecordService.insertOne(merchantInviterModifyRecord);
                }
            }
        }

        // 如果没有成功的变更邀请人，且原先有绑定活动则需将原来活动的信息进行清空
        if (!isSuccessModifyInviter && Objects.nonNull(successInviterVO)) {
            // 修改用户为无参与活动记录
            lostUserBizService.updateLostUserNotActivity(uid);

            // 新租修改邀请记录
            MerchantInviterModifyRecord merchantInviterModifyRecord = MerchantInviterModifyRecord.builder().uid(uid).inviterUid(NumberConstant.ZERO_L)
                    .inviterName("").oldInviterUid(oldInviterUid)
                    .oldInviterName(oldInviterName).oldInviterSource(inviterSource).merchantId(NumberConstant.ZERO_L).franchiseeId(NumberConstant.ZERO_L).tenantId(tenantId)
                    .operator(NumberConstant.ZERO_L).remark(MerchantInviterModifyRecordConstant.LOST_USER_MODIFY_INVITER_CANCEL_REMARK).delFlag(MerchantConstant.DEL_NORMAL).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();

            merchantInviterModifyRecordService.insertOne(merchantInviterModifyRecord);
        }
    }

    /**
     * 流失用户活动处理逻辑
     * @param userInfo
     * @param userInfoExtra
     * @param uid
     * @param carRentalPackageOrderPo
     * @param packageType
     * @param orderNo
     */
    private void handleLostUserActivityForCarPackageType(UserInfo userInfo, UserInfoExtra userInfoExtra, Long uid, CarRentalPackageOrderPo carRentalPackageOrderPo,
            Integer packageType, String orderNo) {
        MerchantInviterVO successInviterVO = userInfoExtraService.querySuccessInviter(uid);
        if (Objects.isNull(successInviterVO)) {
            log.info("HANDLE LOST USER ACTIVITY CAR PACKAGE TYPE INFO! inviter is empty, uid：{}",uid);
        }

        Long oldInviterUid = null;
        String oldInviterName = null;
        Integer inviterSource = null;
        Integer tenantId = userInfo.getTenantId();
        if (Objects.nonNull(successInviterVO)) {
            oldInviterUid = successInviterVO.getInviterUid();
            oldInviterName = successInviterVO.getInviterName();
            inviterSource = successInviterVO.getInviterSource();
        }

        // 流失用户修改为老用户且解绑活动
        lostUserBizService.updateLostUserStatusAndUnbindActivity(userInfo.getTenantId(), uid, successInviterVO);

        // 如果用户之前绑定过活动则修改流失用户为无活动状态
        if (Objects.nonNull(successInviterVO)) {
            // 修改用户为无参与活动记录
            lostUserBizService.updateLostUserNotActivity(uid);

            // 新租修改邀请记录
            MerchantInviterModifyRecord merchantInviterModifyRecord = MerchantInviterModifyRecord.builder().uid(uid).inviterUid(NumberConstant.ZERO_L)
                    .inviterName("").oldInviterUid(oldInviterUid)
                    .oldInviterName(oldInviterName).oldInviterSource(inviterSource).merchantId(NumberConstant.ZERO_L).franchiseeId(NumberConstant.ZERO_L).tenantId(tenantId)
                    .operator(NumberConstant.ZERO_L).remark(MerchantInviterModifyRecordConstant.LOST_USER_MODIFY_INVITER_CANCEL_REMARK).delFlag(MerchantConstant.DEL_NORMAL).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();

            merchantInviterModifyRecordService.insertOne(merchantInviterModifyRecord);
        }


        // 暂时先不处理其他流失用户的活动场景

    }

    /**
     * 购买换电套餐后，处理活动
     */
    public void handleActivityForBatteryPackageType(UserInfo userInfo, UserInfoExtra userInfoExtra, Long uid, ElectricityMemberCardOrder electricityMemberCardOrder,
            Integer packageType) {
        Integer latestActivitySource = userInfoExtra.getLatestActivitySource();
        
        if (Objects.equals(UserInfoActivitySourceEnum.SUCCESS_MERCHANT_ACTIVITY.getCode(), latestActivitySource) && Objects.equals(ElectricityMemberCardOrder.ONLINE_PAYMENT,
                electricityMemberCardOrder.getPayType())) {
            //用户绑定商户
            userInfoExtraService.bindMerchant(userInfo, electricityMemberCardOrder.getOrderId(), electricityMemberCardOrder.getMemberCardId());
        
            //商户返利
            sendMerchantRebateMQ(uid, electricityMemberCardOrder.getOrderId(), YesNoEnum.NO.getCode());
        } else if (Objects.equals(UserInfoActivitySourceEnum.SUCCESS_INVITATION_ACTIVITY.getCode(), latestActivitySource)) {
            //处理套餐返现活动
            invitationActivityRecordService.handleInvitationActivityByPackage(userInfo, electricityMemberCardOrder.getOrderId(), packageType);
        }
        
        //是否是新用户
        if (NumberUtil.equals(NumberConstant.ONE, userInfo.getPayCount())) {
            switch (latestActivitySource) {
                case 1:
                    //处理邀请返券活动
                    userBizService.joinShareActivityProcess(uid, electricityMemberCardOrder.getMemberCardId());
                    break;
                case 2:
                    //处理邀请返现活动
                    userBizService.joinShareMoneyActivityProcess(uid, electricityMemberCardOrder.getMemberCardId(), electricityMemberCardOrder.getTenantId());
                    break;
                case 4:
                    //处理渠道活动
                    userBizService.joinChannelActivityProcess(uid);
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * 购买租车套餐后，处理活动
     */
    public void handleActivityForCarPackageType(UserInfo userInfo, UserInfoExtra userInfoExtra, Long uid, CarRentalPackageOrderPo carRentalPackageOrderPo, Integer packageType,
            String orderNo) {
        Integer latestActivitySource = userInfoExtra.getLatestActivitySource();
        
        //处理套餐返现活动
        if (Objects.equals(UserInfoActivitySourceEnum.SUCCESS_INVITATION_ACTIVITY.getCode(), latestActivitySource)) {
            invitationActivityRecordService.handleInvitationActivityByPackage(userInfo, orderNo, packageType);
        }
        
        //是否是新用户
        if (NumberUtil.equals(NumberConstant.ONE, userInfo.getPayCount())) {
            switch (latestActivitySource) {
                case 1:
                    //处理邀请返券活动
                    userBizService.joinShareActivityProcess(uid, carRentalPackageOrderPo.getRentalPackageId());
                    break;
                case 2:
                    //处理邀请返现活动
                    userBizService.joinShareMoneyActivityProcess(uid, carRentalPackageOrderPo.getRentalPackageId(), carRentalPackageOrderPo.getTenantId());
                    break;
                case 4:
                    //处理渠道活动
                    userBizService.joinChannelActivityProcess(uid);
                    break;
                default:
                    break;
            }
        }
    }
    
    private void sendMerchantRebateMQ(Long uid, String orderId, Integer lostUserType) {
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfoExtra)) {
            log.warn("BATTERY MERCHANT REBATE WARN!userInfoExtra is null,uid={}", uid);
            return;
        }
        
        if (Objects.isNull(userInfoExtra.getMerchantId())) {
            log.warn("BATTERY MERCHANT REBATE WARN!merchantId is null,uid={}", uid);
            return;
        }
        
        BatteryMemberCardMerchantRebate merchantRebate = new BatteryMemberCardMerchantRebate();
        merchantRebate.setUid(uid);
        merchantRebate.setOrderId(orderId);
        merchantRebate.setType(MerchantConstant.TYPE_PURCHASE);
        merchantRebate.setMerchantId(userInfoExtra.getMerchantId());
        merchantRebate.setMessageId(IdUtil.fastSimpleUUID());
        merchantRebate.setLostUserType(lostUserType);
        //续费成功  发送返利MQ
        rocketMqService.sendAsyncMsg(MqProducerConstant.BATTERY_MEMBER_CARD_MERCHANT_REBATE_TOPIC, JsonUtil.toJson(merchantRebate));
    }
    
    /**
     * 登录注册时 活动处理, 该方法在3.0版本中先不启用
     *
     * @param activityProcessDTO
     * @return
     */
    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleActivityByLogon(ActivityProcessDTO activityProcessDTO) {
        Long uid = activityProcessDTO.getUid();
        MDC.put(CommonConstant.TRACE_ID, activityProcessDTO.getTraceId());
        log.info("Activity by register flow start, uid = {}", uid);
        
        if (!redisService.setNx(CacheConstant.CACHE_HANDLE_ACTIVITY_USER_REGISTER_KEY + uid, String.valueOf(uid), 10 * 1000L, false)) {
            log.error("Handle activity for user register error, operations frequently, uid = {}", uid);
        }
        
        try {
            //TODO 如何判定当前用户属于新用户
            //获取参与邀请活动记录
            JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(uid);
            
            //是否有人邀请参与该活动
            if (Objects.nonNull(joinShareActivityRecord)) {
                //查询对应的活动邀请标准
                ShareActivity shareActivity = shareActivityService.queryByIdFromCache(joinShareActivityRecord.getActivityId());
                //检查该活动是否参与过, 没有参与过，并且活动邀请标准为登录注册，则处理该活动
                if (JoinShareActivityRecord.STATUS_INIT.equals(joinShareActivityRecord.getStatus()) && ActivityEnum.INVITATION_CRITERIA_LOGON.equals(
                        shareActivity.getInvitationCriteria())) {
                    
                    //修改邀请状态
                    joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                    joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareActivityRecordService.update(joinShareActivityRecord);
                    
                    //修改历史记录状态
                    JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndJoinUid(joinShareActivityRecord.getId(), uid);
                    if (Objects.nonNull(oldJoinShareActivityHistory)) {
                        oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_SUCCESS);
                        oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
                    }
                    
                    //给邀请人增加邀请成功人数
                    shareActivityRecordService.addCountByUid(joinShareActivityRecord.getUid(), joinShareActivityRecord.getActivityId());
                }
            }
            
            //是否有人返现邀请
            JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(uid);
            if (Objects.nonNull(joinShareMoneyActivityRecord)) {
                ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(joinShareMoneyActivityRecord.getActivityId());
                if (JoinShareMoneyActivityRecord.STATUS_INIT.equals(joinShareMoneyActivityRecord.getStatus()) && ActivityEnum.INVITATION_CRITERIA_LOGON.equals(
                        shareMoneyActivity.getInvitationCriteria())) {
                    //修改邀请状态
                    joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                    joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);
                    
                    //修改历史记录状态
                    JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndJoinUid(
                            joinShareMoneyActivityRecord.getId(), uid);
                    if (Objects.nonNull(oldJoinShareMoneyActivityHistory)) {
                        oldJoinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_SUCCESS);
                        oldJoinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareMoneyActivityHistoryService.update(oldJoinShareMoneyActivityHistory);
                    }
                    
                    if (Objects.nonNull(shareMoneyActivity)) {
                        //给邀请人增加邀请成功人数
                        shareMoneyActivityRecordService.addCountByUid(joinShareMoneyActivityRecord.getUid(), shareMoneyActivity.getMoney(),
                                joinShareMoneyActivityRecord.getActivityId());
                    }
                    
                    //返现
                    userAmountService.handleAmount(joinShareMoneyActivityRecord.getUid(), joinShareMoneyActivityRecord.getJoinUid(), shareMoneyActivity.getMoney(),
                            shareMoneyActivity.getTenantId());
                }
                
            }
        } catch (Exception e) {
            log.warn("handle activity for user register error, uid = {}", uid, e);
            throw new BizException("110005", e.getMessage());
        } finally {
            redisService.delete(CacheConstant.CACHE_HANDLE_ACTIVITY_USER_REGISTER_KEY + uid);
            MDC.clear();
        }
        
        return Triple.of(true, "", null);
    }
    
    
    /**
     * 实名认证后的 活动处理
     *
     * @param activityProcessDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleActivityByRealName(ActivityProcessDTO activityProcessDTO) {
        Long uid = activityProcessDTO.getUid();
        MDC.put(CommonConstant.TRACE_ID, activityProcessDTO.getTraceId());
        log.info("Activity by real name authentication flow start, uid = {}", uid);
        
        if (!redisService.setNx(CacheConstant.CACHE_HANDLE_ACTIVITY_REAL_NAME_AUTH_KEY + uid, String.valueOf(uid), 10 * 1000L, false)) {
            log.error("Handle activity for real name auth error, operations frequently, uid = {}", uid);
        }
        
        try {
            //判断该用户是否进行了实名认证
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            log.info("get user info by uid for activity, auth status = {}", userInfo.getAuthStatus());
            //未实名认证
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("not finished real name authentication! user not auth, user info = {}", JsonUtil.toJson(userInfo));
                return Triple.of(false, "110006", "未进行实名认证");
            }
            
            UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfoExtra)) {
                log.error("handle activity for real name auth error! Not found userInfoExtra, joinUid={}", uid);
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }
            
            // 530需求:以最新的参与活动为准
            Integer latestActivitySource = userInfoExtra.getLatestActivitySource();
            if (Objects.equals(UserInfoActivitySourceEnum.SUCCESS_SHARE_ACTIVITY.getCode(), latestActivitySource)) {
                //获取参与邀请活动记录
                JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(uid);
                
                //是否有人邀请参与该活动
                if (Objects.nonNull(joinShareActivityRecord)) {
                    //查询对应的活动邀请标准
                    ShareActivity shareActivity = shareActivityService.queryByIdFromCache(joinShareActivityRecord.getActivityId());
                    //检查该活动是否参与过, 没有参与过，并且活动邀请标准为登录注册，则处理该活动
                    if (JoinShareActivityRecord.STATUS_INIT.equals(joinShareActivityRecord.getStatus()) && ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode()
                            .equals(shareActivity.getInvitationCriteria())) {
                        
                        //修改邀请状态
                        joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                        joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                        joinShareActivityRecordService.update(joinShareActivityRecord);
                        
                        //修改历史记录状态
                        JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndJoinUid(joinShareActivityRecord.getId(), uid);
                        if (Objects.nonNull(oldJoinShareActivityHistory)) {
                            oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_SUCCESS);
                            oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                            joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
                        }
                        
                        //给邀请人增加邀请成功人数
                        shareActivityRecordService.addCountByUid(joinShareActivityRecord.getUid(), joinShareActivityRecord.getActivityId());
                        
                        //修改会员扩展表活动类型
                        userInfoExtraService.updateByUid(UserInfoExtra.builder().uid(uid).activitySource(UserInfoActivitySourceEnum.SUCCESS_SHARE_ACTIVITY.getCode())
                                .inviterUid(oldJoinShareActivityHistory.getUid()).build());
                    }
                }
            }
            
            if (Objects.equals(UserInfoActivitySourceEnum.SUCCESS_SHARE_MONEY_ACTIVITY.getCode(), latestActivitySource)) {
                //是否有人返现邀请
                JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(uid);
                if (Objects.nonNull(joinShareMoneyActivityRecord)) {
                    ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(joinShareMoneyActivityRecord.getActivityId());
                    if (JoinShareMoneyActivityRecord.STATUS_INIT.equals(joinShareMoneyActivityRecord.getStatus()) && ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode()
                            .equals(shareMoneyActivity.getInvitationCriteria())) {
                        //修改邀请状态
                        joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                        joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                        joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);
                        
                        //修改历史记录状态
                        JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndJoinUid(
                                joinShareMoneyActivityRecord.getId(), uid);
                        if (Objects.nonNull(oldJoinShareMoneyActivityHistory)) {
                            oldJoinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_SUCCESS);
                            oldJoinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
                            joinShareMoneyActivityHistoryService.update(oldJoinShareMoneyActivityHistory);
                        }
                        
                        if (Objects.nonNull(shareMoneyActivity)) {
                            //给邀请人增加邀请成功人数
                            shareMoneyActivityRecordService.addCountByUid(joinShareMoneyActivityRecord.getUid(), shareMoneyActivity.getMoney(),
                                    joinShareMoneyActivityRecord.getActivityId());
                        }
                        
                        //返现
                        userAmountService.handleAmount(joinShareMoneyActivityRecord.getUid(), joinShareMoneyActivityRecord.getJoinUid(), shareMoneyActivity.getMoney(),
                                shareMoneyActivity.getTenantId());
                        
                        //修改会员扩展表活动类型
                        userInfoExtraService.updateByUid(UserInfoExtra.builder().uid(uid).activitySource(UserInfoActivitySourceEnum.SUCCESS_SHARE_MONEY_ACTIVITY.getCode())
                                .inviterUid(oldJoinShareMoneyActivityHistory.getUid()).build());
                    }
                    
                }
            }
        } catch (Exception e) {
            log.warn("handle activity for real name auth error, uid = {}", uid, e);
            throw new BizException("110007", e.getMessage());
        } finally {
            redisService.delete(CacheConstant.CACHE_HANDLE_ACTIVITY_REAL_NAME_AUTH_KEY + uid);
            MDC.clear();
        }
        
        return Triple.of(true, "", null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void asyncProcessActivity(ActivityProcessDTO activityProcessDTO) {
        
        executorService.execute(() -> {
            if (ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(activityProcessDTO.getActivityType())) {
                //处理购买套餐后的活动
                handleActivityByPackage(activityProcessDTO);
            } else if (ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode().equals(activityProcessDTO.getActivityType())) {
                //处理实名认证后的活动
                handleActivityByRealName(activityProcessDTO);
            }
        });
        
    }
    
}
