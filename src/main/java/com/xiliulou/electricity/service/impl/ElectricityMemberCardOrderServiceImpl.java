package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Sets;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.UserInfoExtraConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.dto.*;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUserExit;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.CheckFreezeDaysSourceEnum;
import com.xiliulou.electricity.enums.CouponTypeEnum;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.OverdueType;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.UserStatusEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.enums.enterprise.UserCostTypeEnum;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import com.xiliulou.electricity.enums.notify.SendMessageTypeEnum;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.event.publish.OverdueUserRemarkPublish;
import com.xiliulou.electricity.event.publish.SiteMessagePublish;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.manager.CalcRentCarPriceFactory;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseChannelUserExitMapper;
import com.xiliulou.electricity.mq.producer.ActivityProducer;
import com.xiliulou.electricity.mq.producer.DivisionAccountProducer;
import com.xiliulou.electricity.mq.producer.MessageSendProducer;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardRecordQuery;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.query.UserBatteryDepositAndMembercardQuery;
import com.xiliulou.electricity.query.UserBatteryMembercardQuery;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.queryModel.enterprise.EnterpriseChannelUserExitQueryModel;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseUserCostRecordService;
import com.xiliulou.electricity.service.impl.car.biz.CarRentalPackageOrderBizServiceImpl;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.template.MiniTemplateMsgBizService;
import com.xiliulou.electricity.service.userinfo.UserDelRecordService;
import com.xiliulou.electricity.task.BatteryMemberCardExpireReminderTask;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.BigDecimalUtil;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.security.bean.TokenUser;
import jodd.util.ArraysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.BatteryMemberCardConstants.CHECK_USERINFO_GROUP_ADMIN;
import static com.xiliulou.electricity.entity.ElectricityMemberCardOrder.INSTALLMENT_PAYMENT;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:54
 **/
@Service
@Slf4j
public class ElectricityMemberCardOrderServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements ElectricityMemberCardOrderService {
    
    /**
     * excel导出每次查询条数
     */
    private static final Long EXPORT_LIMIT = 2000L;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    UserCouponService userCouponService;
    
    @Autowired
    CouponService couponService;
    
    @Autowired
    JoinShareActivityRecordService joinShareActivityRecordService;
    
    @Autowired
    ShareActivityRecordService shareActivityRecordService;
    
    @Autowired
    JoinShareActivityHistoryService joinShareActivityHistoryService;
    
    @Autowired
    JoinShareMoneyActivityRecordService joinShareMoneyActivityRecordService;
    
    @Autowired
    ShareMoneyActivityRecordService shareMoneyActivityRecordService;
    
    @Autowired
    JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;
    
    @Autowired
    ShareMoneyActivityService shareMoneyActivityService;
    
    @Autowired
    OldUserActivityService oldUserActivityService;
    
    @Autowired
    UserAmountService userAmountService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    EleUserOperateRecordService eleUserOperateRecordService;
    
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    
    
    @Autowired
    WeChatAppTemplateService weChatAppTemplateService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    EnableMemberCardRecordService enableMemberCardRecordService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    @Autowired
    RocketMqService rocketMqService;
    
    @Autowired
    MessageSendProducer messageSendProducer;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    
    @Autowired
    CalcRentCarPriceFactory calcRentCarPriceFactory;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    ChannelActivityHistoryService channelActivityHistoryService;
    
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    @Autowired
    DivisionAccountRecordService divisionAccountRecordService;
    
    @Autowired
    InvitationActivityRecordService invitationActivityRecordService;
    
    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    
    @Autowired
    ShareActivityMemberCardService shareActivityMemberCardService;
    
    
    @Autowired
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Autowired
    private UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    private EleDepositOrderService depositOrderService;
    
    @Autowired
    private UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    private EleBatteryServiceFeeOrderService batteryServiceFeeOrderService;
    
    @Autowired
    DivisionAccountProducer divisionAccountProducer;
    
    @Autowired
    ActivityProducer activityProducer;
    
    @Autowired
    ActivityService activityService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Autowired
    CarRentalPackageOrderBizService carRentalPackageOrderBizService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    EnterpriseUserCostRecordService enterpriseUserCostRecordService;
    
    @Resource
    EnterpriseChannelUserExitMapper channelUserExitMapper;
    
    @Resource
    AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Autowired
    private OverdueUserRemarkPublish overdueUserRemarkPublish;
    
    @Resource
    private MiniTemplateMsgBizService miniTemplateMsgBizService;
    
    @Resource
    private TenantService tenantService;
    
    @Autowired
    private SiteMessagePublish siteMessagePublish;
    
    @Resource
    private CouponActivityPackageService couponActivityPackageService;
    
    @Autowired
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    @Autowired
    private ElectricityMemberCardOrderMapper electricityMemberCardOrderMapper;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private InstallmentRecordService installmentRecordService;
    
    @Autowired
    private UserInfoExtraService userInfoExtraService;
    
    @Resource
    private UserDelRecordService userDelRecordService;

    @Resource
    private FreeServiceFeeOrderService freeServiceFeeOrderService;

    /**
     * 根据用户ID查询对应状态的记录
     *
     * @param tenantId
     * @param uid
     * @param status
     * @return
     */
    @Override
    public Integer selectCountByUid(Integer tenantId, Long uid, Integer status) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        LambdaQueryWrapper<ElectricityMemberCardOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ElectricityMemberCardOrder::getTenantId, tenantId).eq(ElectricityMemberCardOrder::getUid, uid);
        if (!ObjectUtils.isEmpty(status)) {
            queryWrapper.eq(ElectricityMemberCardOrder::getStatus, status);
        }
        return baseMapper.selectCount(queryWrapper);
    }
    
    @Slave
    @Override
    public BigDecimal homeOne(Long first, Long now, List<Integer> cardIdList, Integer tenantId) {
        return baseMapper.homeOne(first, now, cardIdList, tenantId);
    }
    
    @Slave
    @Override
    public List<HashMap<String, String>> homeTwo(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> cardIdList, Integer tenantId) {
        return baseMapper.homeTwo(startTimeMilliDay, endTimeMilliDay, cardIdList, tenantId);
    }
    
    @Override
    public List<ElectricityMemberCardOrder> selectUserMemberCardOrderList(ElectricityMemberCardOrderQuery orderQuery) {
        List<ElectricityMemberCardOrder> orderList = this.baseMapper.selectUserMemberCardOrderList(orderQuery);
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.EMPTY_LIST;
        }
        
        return orderList;
    }
    
    @Override
    public List<ElectricityMemberCardOrderVO> selectElectricityMemberCardOrderList(ElectricityMemberCardOrderQuery orderQuery) {
        List<ElectricityMemberCardOrder> orderList = this.baseMapper.selectUserMemberCardOrderList(orderQuery);
        if (CollectionUtils.isEmpty(orderList)) {
            return Collections.EMPTY_LIST;
        }
        
        return orderList.parallelStream().map(item -> {
            ElectricityMemberCardOrderVO vo = new ElectricityMemberCardOrderVO();
            BeanUtils.copyProperties(item, vo);
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
            Optional.ofNullable(batteryMemberCard).ifPresent(memCard -> {
                vo.setLimitCount(batteryMemberCard.getLimitCount());
                vo.setRentType(batteryMemberCard.getRentType());
                vo.setRentUnit(batteryMemberCard.getRentUnit());
                vo.setUseCount(batteryMemberCard.getUseCount());
                if (Objects.equals(BatteryMemberCard.YES, batteryMemberCard.getIsRefund()) && System.currentTimeMillis() < (item.getCreateTime()
                        + batteryMemberCard.getRefundLimit() * 24 * 60 * 60 * 1000L)) {
                    vo.setIsRefund(BatteryMemberCard.YES);
                } else {
                    vo.setIsRefund(BatteryMemberCard.NO);
                }
                vo.setRefundLimit(batteryMemberCard.getRefundLimit());
                vo.setSimpleBatteryType(acquireBatteryMembercardOrderSimpleBatteryType(memberCardBatteryTypeService.selectBatteryTypeByMid(item.getMemberCardId())));
                
                BatteryMembercardRefundOrder batteryMembercardRefundOrder = batteryMembercardRefundOrderService.selectLatestByMembercardOrderNo(item.getOrderId());
                if (Objects.nonNull(batteryMembercardRefundOrder)) {
                    vo.setRentRefundStatus(batteryMembercardRefundOrder.getStatus());
                    vo.setRejectReason(batteryMembercardRefundOrder.getMsg());
                }
            });
            
            if (Objects.isNull(item.getExternalAgreementNo())) {
                return vo;
            }
            
            // 套餐订单绑定了分期签约号即为分期代扣生成的套餐订单，若对应的签约记录状态为未支付不于小程序订单中心展示
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNoWithoutUnpaid(item.getExternalAgreementNo());
            if (Objects.isNull(installmentRecord)) {
                return null;
            }
            vo.setInstallmentRecordStatus(installmentRecord.getStatus());
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        
    }
    
    private String acquireBatteryMembercardOrderSimpleBatteryType(List<String> batteryTypes) {
        String result = "";
        
        try {
            if (CollectionUtils.isEmpty(batteryTypes)) {
                return result;
            }
            
            String batteryModel = batteryTypes.get(0);
            
            return batteryModel.substring(batteryModel.indexOf("_") + 1).substring(0, batteryModel.substring(batteryModel.indexOf("_") + 1).indexOf("_"));
        } catch (Exception e) {
            log.error("ELE ERROR!acquire Battery Membercard Order simpleBatteryType");
        }
        
        return result;
    }
    
    @Override
    public Integer selectUserMemberCardOrderCount(ElectricityMemberCardOrderQuery orderQuery) {
        return this.baseMapper.selectUserMemberCardOrderCount(orderQuery);
    }
    
    @Override
    @Slave
    public R queryList(MemberCardOrderQuery memberCardOrderQuery) {

        queryConditions(memberCardOrderQuery);

        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = baseMapper.queryList(memberCardOrderQuery);
        if (CollectionUtils.isEmpty(electricityMemberCardOrderVOList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        Set<Long> uidSet = new HashSet<>();
        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
            uidSet.add(electricityMemberCardOrderVO.getUid());
        }

        List<UserInfo> userInfos = userInfoService.listByUidList(new ArrayList<>(uidSet));
        Map<Long, UserInfo> userInfoMap = new HashMap<>(userInfos.size());
        for (UserInfo userInfo : userInfos) {
            userInfoMap.put(userInfo.getUid(), userInfo);
        }

        // 查询电池型号
        List<Long> memberCardId = electricityMemberCardOrderVOList.stream().map(ElectricityMemberCardOrderVO::getMemberCardId).collect(Collectors.toList());
        List<MemberCardBatteryType> memberCardBatteryTypes = memberCardBatteryTypeService.listByMemberCardIds(memberCardOrderQuery.getTenantId(), memberCardId);
        Map<Long, List<String>> midBatteryTypeMap = new HashMap<>(10);
        Map<String, String> batteryShortMap = new HashMap<>(10);
        if (CollUtil.isNotEmpty(memberCardBatteryTypes)) {
            midBatteryTypeMap = memberCardBatteryTypes.stream().filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(MemberCardBatteryType::getMid, Collectors.mapping(MemberCardBatteryType::getBatteryType, Collectors.toList())));

            List<String> list = memberCardBatteryTypes.stream().map(MemberCardBatteryType::getBatteryType).collect(Collectors.toList());
            List<BatteryModel> batteryModels = batteryModelService.listBatteryModelByBatteryTypeList(list, memberCardOrderQuery.getTenantId());
            batteryShortMap = batteryModels.stream().collect(Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getBatteryVShort, (item1, item2) -> item2));
        }

        // 查询已删除/已注销
        Map<Long, UserDelStatusDTO> userStatusMap = userDelRecordService.listUserStatus(new ArrayList<>(uidSet),
                List.of(UserStatusEnum.USER_STATUS_DELETED.getCode(), UserStatusEnum.USER_STATUS_CANCELLED.getCode()));

        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOs = new ArrayList<>();
        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
            
            if (Objects.equals(electricityMemberCardOrderVO.getIsBindActivity(), ElectricityMemberCardOrder.BIND_ACTIVITY) && Objects.nonNull(
                    electricityMemberCardOrderVO.getActivityId())) {
                OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCardOrderVO.getActivityId());
                if (Objects.nonNull(oldUserActivity)) {
                    
                    OldUserActivityVO oldUserActivityVO = new OldUserActivityVO();
                    BeanUtils.copyProperties(oldUserActivity, oldUserActivityVO);
                    
                    if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {
                        
                        Coupon coupon = couponService.queryByIdFromCache(oldUserActivity.getCouponId());
                        if (Objects.nonNull(coupon)) {
                            oldUserActivityVO.setCoupon(coupon);
                        }
                        
                    }
                    electricityMemberCardOrderVO.setOldUserActivityVO(oldUserActivityVO);
                }
            }
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getRefId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityMemberCardOrderVO.getRefId().intValue());
                electricityMemberCardOrderVO.setElectricityCabinetName(Objects.nonNull(electricityCabinet) ? electricityCabinet.getName() : "");
            }
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getFranchiseeId())) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(electricityMemberCardOrderVO.getFranchiseeId());
                electricityMemberCardOrderVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            }
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getSendCouponId())) {
                Coupon coupon = couponService.queryByIdFromCache(electricityMemberCardOrderVO.getSendCouponId().intValue());
                electricityMemberCardOrderVO.setSendCouponName(Objects.isNull(coupon) ? "" : coupon.getName());
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrderVO.getMemberCardId());
            electricityMemberCardOrderVO.setRentType(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentType());
            electricityMemberCardOrderVO.setRentUnit(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentUnit());
            electricityMemberCardOrderVO.setIsRefund(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getIsRefund());
            electricityMemberCardOrderVO.setLimitCount(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getLimitCount());
            
            // 美团订单不允许退租
            if (Objects.equals(electricityMemberCardOrderVO.getPayType(), ElectricityMemberCardOrder.MEITUAN_PAYMENT)) {
                electricityMemberCardOrderVO.setIsRefund(BatteryMemberCard.NO);
            }
            
            // 设置优惠券
            List<CouponSearchVo> coupons = new ArrayList<>();
            HashSet<Integer> couponIdsSet = new HashSet<>();
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getSendCouponId())) {
                couponIdsSet.add(Integer.parseInt(electricityMemberCardOrderVO.getSendCouponId().toString()));
            }
            if (StringUtils.isNotBlank(electricityMemberCardOrderVO.getCouponIds())) {
                couponIdsSet.addAll(JsonUtil.fromJsonArray(electricityMemberCardOrderVO.getCouponIds(), Integer.class));
            }
            
            if (!CollectionUtils.isEmpty(couponIdsSet)) {
                couponIdsSet.forEach(couponId -> {
                    CouponSearchVo couponSearchVo = new CouponSearchVo();
                    Coupon coupon = couponService.queryByIdFromCache(couponId);
                    if (Objects.nonNull(coupon)) {
                        BeanUtils.copyProperties(coupon, couponSearchVo);
                        couponSearchVo.setId(coupon.getId().longValue());
                        coupons.add(couponSearchVo);
                    }
                });
            }
            electricityMemberCardOrderVO.setCoupons(coupons);

            // 设置用户信息
            UserInfo userInfo = userInfoMap.get(electricityMemberCardOrderVO.getUid());
            if (Objects.nonNull(userInfo)) {
                electricityMemberCardOrderVO.setUserName(userInfo.getName());
                electricityMemberCardOrderVO.setPhone(userInfo.getPhone());
            }
    
            // 查询已删除/已注销
            electricityMemberCardOrderVO.setUserStatus(userDelRecordService.getUserStatus(electricityMemberCardOrderVO.getUid(), userStatusMap));

            List<String> list = midBatteryTypeMap.get(electricityMemberCardOrderVO.getMemberCardId());
            if (CollUtil.isNotEmpty(list)) {
                electricityMemberCardOrderVO.setModel(list.stream().map(batteryShortMap::get).collect(Collectors.toList()));
            }

            electricityMemberCardOrderVOs.add(electricityMemberCardOrderVO);
        }
        
        return R.ok(electricityMemberCardOrderVOs);
    }
    
    @Slave
    @Override
    public R queryCount(MemberCardOrderQuery memberCardOrderQuery) {
        queryConditions(memberCardOrderQuery);
        return R.ok(baseMapper.queryCount(memberCardOrderQuery));
    }

    private void queryConditions(MemberCardOrderQuery memberCardOrderQuery) {
        // 判断是否需要查选电池model
        if (StrUtil.isBlank(memberCardOrderQuery.getModel())) {
            return;
        }
        // 标准型号
        if (Objects.equals(memberCardOrderQuery.getModel(), NumberConstant.ONE.toString())) {
            // 区分租户和加盟商权限
            List<Long> franchiseeIds = franchiseeService.queryOldByTenantId(memberCardOrderQuery.getTenantId());
            if (Objects.equals(SecurityUtils.getUserInfo().getDataType(), User.DATA_TYPE_OPERATE)) {
                // 租户级别的查询下面的 单加盟商
                memberCardOrderQuery.setFranchiseeIds(franchiseeIds);
            }
            if (Objects.equals(SecurityUtils.getUserInfo().getDataType(), User.DATA_TYPE_FRANCHISEE)) {
                // 判断当前加盟商是否是单加盟商
                List<Long> currentId = franchiseeIds.stream().filter(item -> Objects.equals(item, memberCardOrderQuery.getFranchiseeId())).collect(Collectors.toList());
                memberCardOrderQuery.setFranchiseeIds(currentId);
            }
        } else {
            // 电池型号
            List<Long> memberCardIds = memberCardBatteryTypeService.queryMemberCardIdsByBatteryType(memberCardOrderQuery.getTenantId(), memberCardOrderQuery.getModel());
            memberCardOrderQuery.setMemberCardIds(memberCardIds);
        }

    }

    @Slave
    @Override
    public Integer queryCountForScreenStatistic(MemberCardOrderQuery memberCardOrderQuery) {
        return baseMapper.queryCount(memberCardOrderQuery);
    }
    
    @Slave
    @Override
    public BigDecimal queryTurnOver(Integer tenantId, Long uid) {
        return Optional.ofNullable(baseMapper.queryTurnOver(tenantId, uid)).orElse(BigDecimal.valueOf(0));
    }
    
    /**
     * 限制时间停卡
     *
     * @param disableCardDays
     * @param disableDeadline
     * @param applyReason
     * @return
     */
    @Override
    public R disableMemberCardForLimitTime(Integer disableCardDays, Long disableDeadline, String applyReason) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DISABLE_MEMBER_CARD_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁,请稍后再试!");
        }
        
        // 申请冻结次数校验
        R<Object> checkR = userInfoExtraService.checkFreezeCount(user.getTenantId(), user.getUid());
        if (!checkR.isSuccess()) {
            return checkR;
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(user.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("DISABLE MEMBER CARD WARN! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        boolean hasAssets = carRentalPackageOrderBizService.checkUserHasAssets(userInfo, user.getTenantId(), CarRentalPackageOrderBizServiceImpl.ELE);
        if (Objects.equals(ElectricityConfig.NOT_DISABLE_MEMBER_CARD, electricityConfig.getDisableMemberCard()) && Objects.equals(ElectricityConfig.ALLOW_FREEZE_ASSETS,
                electricityConfig.getAllowFreezeWithAssets()) && hasAssets) {
            throw new BizException("300060", "套餐冻结服务，需提前退还租赁的资产，请重新操作");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.warn("DISABLE MEMBER CARD WARN! user is rent deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("DISABLE MEMBER CARD WARN! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        // 是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }
        
        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
            log.warn("PAUSE BATTERY MEMBERCARD WARN! battery membercard refund review ,uid={}", userInfo.getUid());
            return R.fail("100018", "套餐租金退款审核中");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("DISABLE MEMBER CARD WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        String orderId = userBatteryMemberCard.getOrderId();
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("DISABLE MEMBER CARD WARN! not found electricityMemberCardOrder,uid={},orderNo={}", user.getUid(), orderId);
            return R.fail("100281", "电池套餐订单不存在");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("DISABLE MEMBER CARD WARN! memberCard is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(batteryMemberCard.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return R.fail("120203", "加盟商不存在");
        }
        
        if (Boolean.TRUE.equals(userBatteryMemberCardService.verifyUserBatteryMembercardEffective(batteryMemberCard, userBatteryMemberCard))) {
            log.warn("DISABLE MEMBER CARD WARN! userBatteryMemberCard expire,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("100298", "换电套餐已过期，无法进行暂停操作");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("BATTERY SERVICE FEE WARN! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("DISABLE MEMBER CARD WARN! disable review userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("DISABLE MEMBER CARD WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费", acquireUserBatteryServiceFeeResult.getRight());
        }
        
        // 用户申请冻结次数校验及修改次数时需要使用，提前校验
        UserInfoExtra userInfoExtra = userInfoExtraService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userInfoExtra)) {
            log.warn("DISABLE MEMBER CARD WARN! not found userInfo extra, uid={}", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        // 判断用户是否存在与企业渠道用户然后站长退出的表中，类型未未处理或者是处理失败
        List<Integer> typeList = new ArrayList<>();
        typeList.add(EnterpriseChannelUserExit.TYPE_INIT);
        typeList.add(EnterpriseChannelUserExit.TYPE_FAIL);
        List<Long> uidList = new ArrayList<>();
        uidList.add(user.getUid());
        EnterpriseChannelUserExitQueryModel queryModel = EnterpriseChannelUserExitQueryModel.builder().uidList(uidList).typeList(typeList).build();
        List<EnterpriseChannelUserExit> channelUserList = channelUserExitMapper.list(queryModel);
        if (ObjectUtils.isNotEmpty(channelUserList)) {
            log.warn("DISABLE MEMBER CARD WARN! channel user exit,uid={}", userInfo.getUid());
            return R.fail("120308", "企业用户无法申请冻结套餐", acquireUserBatteryServiceFeeResult.getRight());
        }
        
        // 校验冻结申请是否可自动审核及申请天数是否合规
        Boolean autoReviewOrNot = electricityConfigService.checkFreezeAutoReviewAndDays(userInfo.getTenantId(), disableCardDays, userInfo.getUid(), hasAssets,
                CheckFreezeDaysSourceEnum.TINY_APP.getCode());
        
        String generateOrderId = generateOrderId(user.getUid());
        EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder().disableMemberCardNo(generateOrderId)
                .memberCardName(batteryMemberCard.getName()).phone(userInfo.getPhone()).userName(userInfo.getName())
                .status(autoReviewOrNot ? EleDisableMemberCardRecord.MEMBER_CARD_DISABLE : EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW).uid(userInfo.getUid())
                .tenantId(userInfo.getTenantId()).uid(user.getUid()).franchiseeId(userInfo.getFranchiseeId()).storeId(userInfo.getStoreId())
                .batteryMemberCardId(userBatteryMemberCard.getMemberCardId()).chooseDays(disableCardDays)
                .cardDays(userBatteryMemberCardService.transforRemainingTime(userBatteryMemberCard, batteryMemberCard)).disableDeadline(disableDeadline)
                .disableCardTimeType(EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME).chargeRate(batteryMemberCard.getFreezeServiceCharge()).applyReason(applyReason)
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        
        ServiceFeeUserInfo insertOrUpdateServiceFeeUserInfo = ServiceFeeUserInfo.builder().disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .uid(user.getUid()).updateTime(System.currentTimeMillis()).build();
        serviceFeeUserInfoService.updateByUid(insertOrUpdateServiceFeeUserInfo);
        
        // 增加用户套餐冻结次数，自动审核与人工审核通过均走同一处逻辑，handleDisableMemberCard()，次数增加需要置于自动审核之前，若审核拒绝再予以扣减
        userInfoExtraService.changeFreezeCountForUser(userInfo.getUid(), UserInfoExtraConstant.ADD_FREEZE_COUNT);
        
        // 自动审核后续处理
        if (autoReviewOrNot) {
            eleDisableMemberCardRecord.setDisableMemberCardTime(System.currentTimeMillis());
            eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);
            
            return eleDisableMemberCardRecordService.handleDisableMemberCard(userInfo, userBatteryMemberCard, eleDisableMemberCardRecord, franchisee, batteryMemberCard, false);
        }
        
        eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        sendDisableMemberCardMessage(userInfo);
        try {
            
            // 发送站内信
            siteMessagePublish.publish(SiteMessageEvent.builder(this).tenantId(TenantContextHolder.getTenantId().longValue()).code(SiteMessageType.BATTERY_SWAPPING_FREEZE)
                    .notifyTime(System.currentTimeMillis()).addContext("name", userInfo.getName()).addContext("phone", userInfo.getPhone())
                    .addContext("disableMemberCardNo", generateOrderId).build());
            Map<String, Object> map = new HashMap<>();
            map.put("username", eleDisableMemberCardRecord.getUserName());
            map.put("phone", eleDisableMemberCardRecord.getPhone());
            map.put("packageName", eleDisableMemberCardRecord.getMemberCardName());
            map.put("residue", eleDisableMemberCardRecord.getChooseDays());
            operateRecordUtil.record(null, map);
        } catch (Throwable e) {
            log.error("Recording user operation records failed because:", e);
        }
        return R.ok();
    }
    
    @Override
    public R enableMemberCardForLimitTime() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELECTRICITY  warn! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DISABLE_MEMBER_CARD_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁,请稍后再试!");
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(user.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("ENABLE MEMBER CARD warn! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.warn("ENABLE MEMBER CARD warn! user is rent deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("ENABLE MEMBER CARD warn! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("ENABLE MEMBER CARD warn! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("ENABLE MEMBER CARD warn! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        // 是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("ENABLE MEMBER CARD warn! disable review userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) || Objects.equals(
                userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE)) {
            log.warn("ENABLE MEMBER CARD warn! member card not disable userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户未停卡");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.warn("ENABLE MEMBER CARD warn! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("ENABLE MEMBER CARD warn! not found user,uid={} ", user.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("DISABLE MEMBER CARD warn! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费", acquireUserBatteryServiceFeeResult.getRight());
        }
        
        int disableCardDays = (int) Math.ceil((System.currentTimeMillis() - (userBatteryMemberCard.getDisableMemberCardTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
        
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(), user.getTenantId());
        
        Integer serviceFeeStatus = EnableMemberCardRecord.STATUS_INIT;
        
        EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder().disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .memberCardName(batteryMemberCard.getName()).memberCardId(batteryMemberCard.getId()).enableTime(System.currentTimeMillis())
                .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE).batteryServiceFeeStatus(serviceFeeStatus).disableDays(disableCardDays)
                .disableTime(eleDisableMemberCardRecord.getUpdateTime()).franchiseeId(userInfo.getFranchiseeId()).storeId(userInfo.getStoreId()).phone(user.getPhone())
                .orderId(userBatteryMemberCard.getOrderId()).createTime(System.currentTimeMillis()).tenantId(user.getTenantId()).uid(user.getUid()).userName(userInfo.getName())
                .updateTime(System.currentTimeMillis()).build();
        
        enableMemberCardRecordService.insert(enableMemberCardRecord);
        
        // 处理企业用户对应的支付记录时间
        anotherPayMembercardRecordService.enableMemberCardHandler(userBatteryMemberCard.getUid());
        
        UserBatteryMemberCard userBatteryMemberCardUdpate = new UserBatteryMemberCard();
        
        // 兼容2.0冻结不限制天数 冻结天数为空的场景
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = null;
        if (Objects.isNull(eleDisableMemberCardRecord.getChooseDays())) {
            userBatteryMemberCardUdpate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUdpate.setOrderExpireTime(
                    System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUdpate.setMemberCardExpireTime(
                    System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUdpate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUdpate.setUpdateTime(System.currentTimeMillis());
            
            serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder().disableMemberCardNo("").serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime())
                    .franchiseeId(userInfo.getFranchiseeId()).tenantId(eleDisableMemberCardRecord.getTenantId()).uid(user.getUid()).createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            if (Boolean.FALSE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                userBatteryMemberCardUdpate.setDisableMemberCardTime(null);
                serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
            }
        } else {
            // 申请冻结的天数
            Long chooseTime = eleDisableMemberCardRecord.getChooseDays() * TimeConstant.DAY_MILLISECOND;
            // 实际的冻结时间
            Long realTime = System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime();
            
            // Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
            userBatteryMemberCardUdpate.setUid(userBatteryMemberCard.getUid());
       /* userBatteryMemberCardUdpate.setOrderExpireTime(
                System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
        userBatteryMemberCardUdpate.setMemberCardExpireTime(memberCardExpireTime);*/
            userBatteryMemberCardUdpate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime() - (chooseTime - realTime));
            userBatteryMemberCardUdpate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() - (chooseTime - realTime));
            userBatteryMemberCardUdpate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUdpate.setUpdateTime(System.currentTimeMillis());
            
            serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder().disableMemberCardNo("")
                    .serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime() - (chooseTime - realTime)).franchiseeId(userInfo.getFranchiseeId())
                    .tenantId(eleDisableMemberCardRecord.getTenantId()).uid(user.getUid()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            
            if (Boolean.FALSE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                userBatteryMemberCardUdpate.setDisableMemberCardTime(null);
                serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
            }
        }
        
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        
        userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUdpate);
        
        // 记录企业用户冻结后启用套餐记录
        enterpriseUserCostRecordService.asyncSaveUserCostRecordForBattery(userInfo.getUid(), enableMemberCardRecord.getId() + "_" + enableMemberCardRecord.getDisableMemberCardNo(),
                UserCostTypeEnum.COST_TYPE_ENABLE_PACKAGE.getCode(), enableMemberCardRecord.getEnableTime());
        
        return R.ok();
    }
    
    @Override
    public R disableMemberCardForRollback() {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(tokenUser.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("ELE WARN! not found user,uid={} ", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("ENABLE MEMBER CARD ERROR! user is rent deposit,uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("ENABLE MEMBER CARD ERROR! not pay deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("ENABLE MEMBER CARD ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("ENABLE MEMBER CARD ERROR! memberCard  is not exit,uid={},memberCardId={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryByDisableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo(),
                TenantContextHolder.getTenantId());
        if (Objects.isNull(eleDisableMemberCardRecord) || !Objects.equals(eleDisableMemberCardRecord.getStatus(), EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW)) {
            return R.fail("100370", "停卡记录不存在或不为审核中状态");
        }
        
        EleDisableMemberCardRecord updateEleDisableMemberCardRecord = new EleDisableMemberCardRecord();
        updateEleDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
        updateEleDisableMemberCardRecord.setStatus(EleDisableMemberCardRecord.STATUS_MEMBER_CARD_DISABLE_ROLLBACK);
        updateEleDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
        eleDisableMemberCardRecordService.updateBYId(updateEleDisableMemberCardRecord);
        
        ServiceFeeUserInfo updateServiceFeeUserInfo = new ServiceFeeUserInfo();
        updateServiceFeeUserInfo.setUid(userInfo.getUid());
        updateServiceFeeUserInfo.setUpdateTime(System.currentTimeMillis());
        updateServiceFeeUserInfo.setDisableMemberCardNo("");
        updateServiceFeeUserInfo.setPauseOrderNo("");
        serviceFeeUserInfoService.updateByUid(updateServiceFeeUserInfo);
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userInfo.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        // 撤销申请时，扣减用户冻结次数
        userInfoExtraService.changeFreezeCountForUser(userInfo.getUid(), UserInfoExtraConstant.SUBTRACT_FREEZE_COUNT);
        
        return R.ok();
    }
    
    @Override
    public R enableOrDisableMemberCardIsLimitTime() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("ENABLE MEMBER CARD ERROR! user is rent deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                log.error("DISABLE MEMBER CARD ERROR！franchiseeId={}", userInfo.getFranchiseeId());
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            return R.ok(franchisee.getDisableCardTimeType());
        } else {
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(),
                    user.getTenantId());
            return R.ok(eleDisableMemberCardRecord.getDisableCardTimeType());
        }
    }
    
    @Override
    public R adminDisableMemberCard(Long uid, Integer days) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_MEMBER_CARD_DISABLE_LOCK + uid, "1", 2 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin saveUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("admin saveUserMemberCard  WARN! not found user! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        boolean hasAssets = carRentalPackageOrderBizService.checkUserHasAssets(userInfo, user.getTenantId(), CarRentalPackageOrderBizServiceImpl.ELE);
        try {
            electricityConfigService.checkFreezeAutoReviewAndDays(userInfo.getTenantId(), days, uid, hasAssets, CheckFreezeDaysSourceEnum.BACK.getCode());
        } catch (BizException e) {
            return R.fail(e.getErrCode(), e.getErrMsg());
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.warn("admin saveUserMemberCard  WARN! user is rent deposit,uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
    
        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(userInfo.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            return R.fail("120163", "账号处于注销缓冲期内，无法操作");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("admin saveUserMemberCard  WARN! not pay deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("admin saveUserMemberCard  WARN! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        String orderId = userBatteryMemberCard.getOrderId();
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(orderId);
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("admin saveUserMemberCard  WARN! not found electricityMemberCardOrder,uid={},orderNo={}", user.getUid(), orderId);
            return R.fail("100281", "电池套餐订单不存在");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("admin saveUserMember Card  WARN! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        if (Boolean.TRUE.equals(userBatteryMemberCardService.verifyUserBatteryMembercardEffective(batteryMemberCard, userBatteryMemberCard))) {
            log.warn("PAUSE BATTERY MEMBER CARD WARN! userBatteryMemberCard expire,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("100298", "换电套餐已过期，无法进行暂停操作");
        }
        
        // 是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }
        
        // 是否有正在进行的退租
        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
            log.warn("PAUSE BATTERY MEMBER CARD WARN! battery membercard refund review userId={}", userInfo.getUid());
            return R.fail("100018", "套餐租金退款审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("PAUSE BATTERY MEMBER CARD WARN! disable review userId={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.warn("PAUSE BATTERY MEMBER CARD WARN!not found franchisee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("PAUSE BATTERY MEMBER CARD WARN! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("PAUSE BATTERY MEMBER CARD WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费");
        }
        
        R<Object> checkR = userInfoExtraService.checkFreezeCount(TenantContextHolder.getTenantId(), uid);
        if (!checkR.isSuccess()) {
            return checkR;
        }
        
        EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder().disableMemberCardNo(generateOrderId(uid))
                .memberCardName(batteryMemberCard.getName()).batteryMemberCardId(userBatteryMemberCard.getMemberCardId()).phone(userInfo.getPhone()).userName(userInfo.getName())
                .status(UserBatteryMemberCard.MEMBER_CARD_DISABLE).tenantId(userInfo.getTenantId()).uid(uid).franchiseeId(userInfo.getFranchiseeId()).storeId(userInfo.getStoreId())
                .chargeRate(batteryMemberCard.getFreezeServiceCharge()).chooseDays(days).disableCardTimeType(EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)
                .cardDays(userBatteryMemberCardService.transforRemainingTime(userBatteryMemberCard, batteryMemberCard)).disableMemberCardTime(System.currentTimeMillis())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).auditorId(SecurityUtils.getUserInfo().getUid()).build();
        eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);
        
        // 更新用户套餐状态为暂停
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE);
        userBatteryMemberCardUpdate.setDisableMemberCardTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 套餐过期时间需要加上冻结的时间
        Long frozenTime = days * TimeConstant.DAY_MILLISECOND;
        userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime() + frozenTime);
        userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + frozenTime);
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(uid);
        serviceFeeUserInfoUpdate.setDisableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 3.0新需求  暂停套餐生成滞纳金订单
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            // 获取用户绑定的电池
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
            // 用户绑定的电池型号
            List<String> userBatteryType = userBatteryTypeService.selectByUid(userInfo.getUid());
            Set<String> batteryTypeSet = null;
            if (CollectionUtils.isNotEmpty(userBatteryType)) {
                batteryTypeSet = new HashSet<>(userBatteryType);
            }
            
            // 需求上线时避免重启中未删除缓存导致报错，以后无用
            BigDecimal freezeServiceCharge = batteryMemberCard.getFreezeServiceCharge();
            if (Objects.isNull(freezeServiceCharge)) {
                freezeServiceCharge = batteryMemberCard.getServiceCharge();
            }
            
            // 套餐的套餐冻结服务费大于0，在保存套餐冻结滞纳金订单
            if (Objects.nonNull(freezeServiceCharge) && freezeServiceCharge.compareTo(BigDecimal.ZERO) > 0) {
                EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder()
                        .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid())).uid(userInfo.getUid()).phone(userInfo.getPhone())
                        .name(userInfo.getName()).payAmount(BigDecimal.ZERO).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis()).batteryServiceFeeGenerateTime(System.currentTimeMillis()).franchiseeId(userInfo.getFranchiseeId())
                        .storeId(userInfo.getStoreId()).tenantId(userInfo.getTenantId()).source(EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD).modelType(franchisee.getModelType())
                        .batteryType(CollectionUtils.isEmpty(batteryTypeSet) ? "" : JsonUtil.toJson(batteryTypeSet))
                        .sn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn()).batteryServiceFee(freezeServiceCharge)
                        .expiredProtectionTime(EleBatteryServiceFeeOrder.EXPIRED_PROTECTION_TIME_DISABLE).build();
                eleBatteryServiceFeeOrderService.insert(eleBatteryServiceFeeOrder);
                
                serviceFeeUserInfoUpdate.setPauseOrderNo(eleBatteryServiceFeeOrder.getOrderId());
            }
        }
        
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        
        // 增加用户冻结次数
        userInfoExtraService.changeFreezeCountForUser(uid, UserInfoExtraConstant.ADD_FREEZE_COUNT);
        
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_DISABLE).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(user.getUid()).uid(uid)
                .name(user.getUsername()).memberCardDisableStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
        
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("username", eleDisableMemberCardRecord.getUserName());
            map.put("phone", eleDisableMemberCardRecord.getPhone());
            map.put("packageName", eleDisableMemberCardRecord.getMemberCardName());
            map.put("residue", eleDisableMemberCardRecord.getChooseDays());
            operateRecordUtil.record(null, map);
        } catch (Throwable e) {
            log.error("Recording user operation records failed because:", e);
        }
        return R.ok();
    }
    
    @Override
    public R adminEnableMemberCard(Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_MEMBER_CARD_ENABLE_LOCK + uid, "1", 2 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! not found userInfo! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! user is rent deposit,uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId())) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! disable review userId={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE) || Objects.equals(
                userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE)) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! member card not disable userId={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100001", "用户未停卡");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! battery memberCard is not exit,uid={},memberCardId={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费");
        }
        
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryByDisableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo(),
                userInfo.getTenantId());
        if (Objects.isNull(eleDisableMemberCardRecord)) {
            log.warn("ADMIN ENABLE BATTERY MEMBER CARD WARN! not found eleDisableMemberCardRecord,uid={},disableMemberCardNo={}", userInfo.getUid(),
                    serviceFeeUserInfo.getDisableMemberCardNo());
            return R.fail("100370", "停卡记录不存在");
        }
        
        int realDisableCardDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
        
        EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder().uid(uid).userName(userInfo.getName())
                .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo()).memberCardName(batteryMemberCard.getName()).enableTime(System.currentTimeMillis())
                .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE).batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT).disableDays(realDisableCardDays)
                .disableTime(eleDisableMemberCardRecord.getUpdateTime()).phone(userInfo.getPhone()).createTime(System.currentTimeMillis()).serviceFee(BigDecimal.ZERO)
                .orderId(userBatteryMemberCard.getOrderId()).storeId(userInfo.getStoreId()).franchiseeId(userInfo.getFranchiseeId()).tenantId(userInfo.getTenantId())
                .updateTime(System.currentTimeMillis()).build();
        enableMemberCardRecordService.insert(enableMemberCardRecord);
        
        // 处理企业用户对应的支付记录时间
        anotherPayMembercardRecordService.enableMemberCardHandler(userBatteryMemberCard.getUid());
        
        // 更新用户套餐过期时间
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        
        // 兼容2.0冻结不限制天数 冻结天数为空的场景
        if (Objects.isNull(eleDisableMemberCardRecord.getChooseDays())) {
            userBatteryMemberCardUpdate.setOrderExpireTime(
                    System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            
            // 更新用户服务费产生时间 解绑用户停卡单号和滞纳金单号
            ServiceFeeUserInfo serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder().disableMemberCardNo("").pauseOrderNo("").expireOrderNo("")
                    .franchiseeId(userInfo.getFranchiseeId()).serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime())
                    .tenantId(eleDisableMemberCardRecord.getTenantId()).uid(uid).updateTime(System.currentTimeMillis()).build();
            
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        } else {
            // 申请冻结的天数
            Long chooseTime = eleDisableMemberCardRecord.getChooseDays() * TimeConstant.DAY_MILLISECOND;
            // 实际的冻结时间
            Long realTime = System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime();
  
       /* userBatteryMemberCardUpdate.setOrderExpireTime(
                userBatteryMemberCard.getOrderExpireTime() + (System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()));
        userBatteryMemberCardUpdate.setMemberCardExpireTime(
                userBatteryMemberCard.getMemberCardExpireTime() + (System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()));*/
            userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime() - (chooseTime - realTime));
            userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() - (chooseTime - realTime));
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            
            // 更新用户服务费产生时间 解绑用户停卡单号和滞纳金单号
            ServiceFeeUserInfo serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder().disableMemberCardNo("").pauseOrderNo("").expireOrderNo("")
                    .franchiseeId(userInfo.getFranchiseeId()).serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime() - (chooseTime - realTime))
                    .tenantId(eleDisableMemberCardRecord.getTenantId()).uid(uid).updateTime(System.currentTimeMillis()).build();
            
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        }
        
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_DISABLE).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid()).uid(uid)
                .name(Objects.isNull(SecurityUtils.getUserInfo()) ? null : SecurityUtils.getUserInfo().getUsername())
                .memberCardDisableStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE).tenantId(TenantContextHolder.getTenantId()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
        
        return R.ok();
    }
    
    @Override
    public R cleanBatteryServiceFee(Long uid) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_CLEAN_BATTERY_SERVICE_FEE_LOCK + uid, "1", 2 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin saveUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("admin saveUserMemberCard  WARN! not found user! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(
                userBatteryMemberCard.getRemainingNumber())) {
            log.warn("admin saveUserMemberCard  ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.warn("admin clean service fee ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("admin saveUserMemberCard  WARN! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.FALSE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("admin clean service fee ERROR! user not exist service fee,uid={}", user.getUid());
            overdueUserRemarkPublish.publish(uid, OverdueType.BATTERY.getCode(), TenantContextHolder.getTenantId());
            return R.ok();
        }
        
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        
        // 套餐过期时间
        Long memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime();
        // 当前套餐过期时间
        Long orderExpireTime = userBatteryMemberCard.getOrderExpireTime();
        // 电池服务费产生时间
        Long serviceFeeGenerateTime = userBatteryMemberCard.getMemberCardExpireTime();
        // 套餐过期滞纳金
        BigDecimal expireBatteryServiceFee = BigDecimal.ZERO;
        // 暂停套餐滞纳金
        BigDecimal pauseBatteryServiceFee = BigDecimal.ZERO;
        
        // 获取过期滞纳金起算时间
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(serviceFeeUserInfo.getExpireOrderNo());
        Integer expiredProtectionTime = eleBatteryServiceFeeOrderService.getExpiredProtectionTime(eleBatteryServiceFeeOrder, userInfo.getTenantId());
        
        // 1.如果是套餐过期
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            memberCardExpireTime = System.currentTimeMillis();
            orderExpireTime = System.currentTimeMillis();
            serviceFeeGenerateTime = System.currentTimeMillis();
            
            int batteryMemebercardExpireDays = (int) Math.ceil(
                    (System.currentTimeMillis() - (userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
            expireBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
            log.info("ADMIN CLEAN BATTERY SERVICE FEE INFO!user exist expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());
        }
        
        // 2.如果套餐暂停，则提前启用
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            // 更新停卡记录
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(),
                    user.getTenantId());
            if (Objects.isNull(eleDisableMemberCardRecord)) {
                return R.fail("100370", "停卡记录不存在");
            }
            
            // 兼容2.0冻结不限制天数 冻结天数为空的场景
            if (Objects.isNull(eleDisableMemberCardRecord.getChooseDays())) {
                memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                orderExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                serviceFeeGenerateTime = userBatteryMemberCard.getMemberCardExpireTime();
            } else {
                // 申请冻结的天数
                Long chooseTime = eleDisableMemberCardRecord.getChooseDays() * TimeConstant.DAY_MILLISECOND;
                // 实际的冻结时间
                Long realTime = System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime();
                
                memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime() - (chooseTime - realTime);
                orderExpireTime = userBatteryMemberCard.getOrderExpireTime() - (chooseTime - realTime);
                serviceFeeGenerateTime = userBatteryMemberCard.getMemberCardExpireTime() - (chooseTime - realTime);
            }
            
            // 需求上线时避免重启中未删除缓存导致报错，以后无用
            BigDecimal freezeServiceCharge = batteryMemberCard.getFreezeServiceCharge();
            if (Objects.isNull(freezeServiceCharge)) {
                freezeServiceCharge = batteryMemberCard.getServiceCharge();
            }
            
            int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
            pauseBatteryServiceFee = freezeServiceCharge.multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
            log.info("ADMIN CLEAN BATTERY SERVICE FEE INFO!user exist pause fee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());
            
            // 生成启用记录
            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder().disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(batteryMemberCard.getName()).enableTime(System.currentTimeMillis()).enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_CLEAN).serviceFee(pauseBatteryServiceFee).disableDays(batteryMembercardDisableDays)
                    .disableTime(userBatteryMemberCard.getDisableMemberCardTime()).enableTime(System.currentTimeMillis()).franchiseeId(userInfo.getFranchiseeId())
                    .storeId(userInfo.getStoreId()).phone(userInfo.getPhone()).createTime(System.currentTimeMillis()).tenantId(user.getTenantId()).uid(uid)
                    .userName(userInfo.getName()).updateTime(System.currentTimeMillis()).orderId(userBatteryMemberCard.getOrderId()).build();
            enableMemberCardRecordService.insert(enableMemberCardRecord);
            
            // 处理企业用户对应的支付记录时间
            anotherPayMembercardRecordService.enableMemberCardHandler(userBatteryMemberCard.getUid());
            
            serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
        }
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
        userBatteryMemberCardUpdate.setOrderExpireTime(orderExpireTime);
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        // 更新滞纳金订单状态
        if (StringUtils.isNotBlank(serviceFeeUserInfo.getPauseOrderNo())) {
            EleBatteryServiceFeeOrder disableMembercardServiceFeeOrder = new EleBatteryServiceFeeOrder();
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                disableMembercardServiceFeeOrder.setPayAmount(pauseBatteryServiceFee);
            }
            disableMembercardServiceFeeOrder.setOrderId(serviceFeeUserInfo.getPauseOrderNo());
            disableMembercardServiceFeeOrder.setStatus(EleBatteryServiceFeeOrder.STATUS_CLEAN);
            disableMembercardServiceFeeOrder.setBatteryServiceFeeEndTime(System.currentTimeMillis());
            disableMembercardServiceFeeOrder.setUpdateTime(System.currentTimeMillis());
            disableMembercardServiceFeeOrder.setPayTime(System.currentTimeMillis());
            batteryServiceFeeOrderService.updateByOrderNo(disableMembercardServiceFeeOrder);
            
            serviceFeeUserInfoUpdate.setPauseOrderNo("");
        }
        if (StringUtils.isNotBlank(serviceFeeUserInfo.getExpireOrderNo())) {
            EleBatteryServiceFeeOrder expireMembercardServiceFeeOrder = new EleBatteryServiceFeeOrder();
            expireMembercardServiceFeeOrder.setOrderId(serviceFeeUserInfo.getExpireOrderNo());
            expireMembercardServiceFeeOrder.setStatus(EleBatteryServiceFeeOrder.STATUS_CLEAN);
            expireMembercardServiceFeeOrder.setPayAmount(expireBatteryServiceFee);
            expireMembercardServiceFeeOrder.setBatteryServiceFeeEndTime(System.currentTimeMillis());
            expireMembercardServiceFeeOrder.setUpdateTime(System.currentTimeMillis());
            expireMembercardServiceFeeOrder.setPayTime(System.currentTimeMillis());
            batteryServiceFeeOrderService.updateByOrderNo(expireMembercardServiceFeeOrder);
            
            serviceFeeUserInfoUpdate.setExpireOrderNo("");
        }
        
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(serviceFeeGenerateTime);
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        
        // 兼容套餐过期，定时任务还未生成滞纳金订单的场景
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() && StringUtils.isBlank(serviceFeeUserInfo.getExpireOrderNo())) {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
            List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
            
            // 计算得到的服务费大于0再保存记录
            if (expireBatteryServiceFee.compareTo(BigDecimal.ZERO) > 0) {
                EleBatteryServiceFeeOrder expireMembercardServiceFeeOrderInsert = new EleBatteryServiceFeeOrder();
                expireMembercardServiceFeeOrderInsert.setUid(userInfo.getUid());
                expireMembercardServiceFeeOrderInsert.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid()));
                expireMembercardServiceFeeOrderInsert.setStatus(EleBatteryServiceFeeOrder.STATUS_CLEAN);
                expireMembercardServiceFeeOrderInsert.setName(userInfo.getName());
                expireMembercardServiceFeeOrderInsert.setPhone(userInfo.getPhone());
                expireMembercardServiceFeeOrderInsert.setTenantId(userInfo.getTenantId());
                expireMembercardServiceFeeOrderInsert.setStoreId(userInfo.getStoreId());
                expireMembercardServiceFeeOrderInsert.setFranchiseeId(userInfo.getFranchiseeId());
                expireMembercardServiceFeeOrderInsert.setModelType(Objects.isNull(franchisee) ? 0 : franchisee.getModelType());
                expireMembercardServiceFeeOrderInsert.setBatteryType(CollectionUtils.isEmpty(userBatteryTypes) ? "" : JsonUtil.toJson(userBatteryTypes));
                expireMembercardServiceFeeOrderInsert.setSn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn());
                expireMembercardServiceFeeOrderInsert.setPayAmount(expireBatteryServiceFee);
                expireMembercardServiceFeeOrderInsert.setBatteryServiceFee(batteryMemberCard.getServiceCharge());
                expireMembercardServiceFeeOrderInsert.setBatteryServiceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime() + expiredProtectionTime * 60 * 60 * 1000);
                expireMembercardServiceFeeOrderInsert.setBatteryServiceFeeEndTime(System.currentTimeMillis());
                expireMembercardServiceFeeOrderInsert.setSource(EleBatteryServiceFeeOrder.MEMBER_CARD_OVERDUE);
                expireMembercardServiceFeeOrderInsert.setPayTime(System.currentTimeMillis());
                expireMembercardServiceFeeOrderInsert.setCreateTime(System.currentTimeMillis());
                expireMembercardServiceFeeOrderInsert.setUpdateTime(System.currentTimeMillis());
                expireMembercardServiceFeeOrderInsert.setExpiredProtectionTime(expiredProtectionTime);
                batteryServiceFeeOrderService.insert(expireMembercardServiceFeeOrderInsert);
            }
        }
        
        if (!Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) && StringUtils.isNotBlank(
                serviceFeeUserInfo.getPauseOrderNo())) {
            EnableMemberCardRecord enableMemberCardRecord = enableMemberCardRecordService.selectLatestByUid(userInfo.getUid());
            if (Objects.nonNull(enableMemberCardRecord)) {
                EnableMemberCardRecord enableMemberCardRecordUpdate = new EnableMemberCardRecord();
                enableMemberCardRecordUpdate.setId(enableMemberCardRecord.getId());
                enableMemberCardRecordUpdate.setBatteryServiceFeeStatus(EnableMemberCardRecord.STATUS_CLEAN);
                enableMemberCardRecordUpdate.setUpdateTime(System.currentTimeMillis());
                enableMemberCardRecordService.update(enableMemberCardRecordUpdate);
            }
        }
        
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.CLEAN_BATTERY_SERVICE_FEE).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(user.getUid()).uid(uid)
                .name(user.getUsername()).batteryServiceFee(expireBatteryServiceFee.add(pauseBatteryServiceFee)).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
        
        // 清除逾期用户备注
        overdueUserRemarkPublish.publish(uid, OverdueType.BATTERY.getCode(), TenantContextHolder.getTenantId());
        
        return R.ok();
    }
    
    @Override
    public R getDisableMemberCardList(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery) {
        return eleDisableMemberCardRecordService.list(electricityMemberCardRecordQuery);
    }
    
    @Override
    public ElectricityMemberCardOrder queryLastPayMemberCardTimeByUid(Long uid, Long franchiseeId, Integer tenantId) {
        return baseMapper.queryLastPayMemberCardTimeByUid(uid, franchiseeId, tenantId);
    }
    
    @Override
    public ElectricityMemberCardOrder selectLatestByUid(Long uid) {
        return baseMapper.selectLatestByUid(uid);
    }
    
    @Override
    public void batteryMemberCardExpireReminder(BatteryMemberCardExpireReminderTask.TaskParam param) {
        if (!redisService.setNx(CacheConstant.CACHE_ELE_BATTERY_MEMBER_CARD_EXPIRED_LOCK, "ok", 120000L, false)) {
            log.warn("batteryMemberCardExpireReminder in execution...");
            return;
        }
        
        if (null == param.getSize()) {
            param.setSize(300);
        }
        
        if (CollectionUtils.isNotEmpty(param.getTenantIds())) {
            param.getTenantIds().forEach(tid -> batteryMemberCardExpireReminderByTenant(tid, param.getSize()));
            return;
        }
        
        Integer startTenantId = 0;
        while (true) {
            // 查询租户
            List<Integer> queryTenantIds = tenantService.queryIdListByStartId(startTenantId, param.getSize());
            
            if (CollectionUtils.isEmpty(queryTenantIds)) {
                break;
            }
            startTenantId = queryTenantIds.get(queryTenantIds.size() - 1);
            
            // 根据租户处理
            queryTenantIds.forEach(tid -> this.batteryMemberCardExpireReminderByTenant(tid, param.getSize()));
        }
        
        
    }
    
    private void batteryMemberCardExpireReminderByTenant(Integer tenantId, Integer size) {
        int offset = 0;
        
        Date date = new Date();
        // 当前时间
        long firstTime = System.currentTimeMillis();
        // 往后3天
        long lastTime = System.currentTimeMillis() + 3 * 3600000 * 24;
        
        SimpleDateFormat simp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        while (true) {
            
            List<UserBatteryMemberCard> memberCards = userBatteryMemberCardService.batteryMemberCardExpire(tenantId, offset, size, firstTime, lastTime);
            if (CollectionUtils.isEmpty(memberCards)) {
                return;
            }
            List<Long> memberCardIds = memberCards.stream().map(UserBatteryMemberCard::getMemberCardId).distinct().collect(Collectors.toList());
            
            List<BatteryMemberCard> batteryMemberCards = batteryMemberCardService.queryListByIdList(memberCardIds);
            if (CollectionUtils.isEmpty(batteryMemberCards)) {
                return;
            }
            Map<Long, BatteryMemberCard> idMap = batteryMemberCards.stream().collect(Collectors.toMap(BatteryMemberCard::getId, v -> v, (k1, k2) -> k1));
            
            memberCards.parallelStream().forEach(item -> {
                if (!idMap.containsKey(item.getMemberCardId())) {
                    log.warn("ElectricityMemberCardOrderServiceImpl.batteryMemberCardExpireReminder WARN! id:{}, memberCardId:{} , not exist!", item.getId(),
                            item.getMemberCardId());
                    return;
                }
                
                BatteryMemberCard batteryMemberCard = idMap.get(item.getMemberCardId());
                
                date.setTime(item.getMemberCardExpireTime());
                miniTemplateMsgBizService.sendBatteryMemberCardExpiring(item.getTenantId(), item.getUid(), batteryMemberCard.getName(), simp.format(date));
            });
            offset += size;
        }
    }
    
    
    public static void main(String[] args) {
        System.out.println(DateUtil.format(new Date(1726303200186L), "yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateUtil.format(new Date(1726303484455L), "yyyy-MM-dd HH:mm:ss"));
    }
    
    @Override
    public void systemEnableMemberCardTask() {
        int offset = 0;
        int size = 300;
        
        while (true) {
            
            // 获取停卡用户
            List<ServiceFeeUserInfo> userDisableMembercardList = serviceFeeUserInfoService.selectDisableMembercardList(offset, size);
            if (CollectionUtils.isEmpty(userDisableMembercardList)) {
                return;
            }
            
            userDisableMembercardList.parallelStream().forEach(item -> {
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(item.getUid());
                if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getDisableMemberCardTime())) {
                    log.error("ELE ENABLE BATTERY MEMBERCARD ERROR! not found userBatteryMemberCard,uid={}", item.getUid());
                    return;
                }
                
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    return;
                }
                
                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                if (Objects.isNull(userInfo)) {
                    log.error("ELE ENABLE BATTERY MEMBERCARD ERROR! not found userInfo,uid={}", item.getUid());
                    return;
                }
                
                // 获取停卡记录
                EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.selectByDisableMemberCardNo(item.getDisableMemberCardNo());
                if (Objects.isNull(eleDisableMemberCardRecord)) {
                    log.error("ELE ENABLE BATTERY MEMBERCARD ERROR! not found eleDisableMemberCardRecord,uid={},orderId={}", item.getUid(), item.getDisableMemberCardNo());
                    return;
                }
                
                if (!Objects.equals(eleDisableMemberCardRecord.getDisableCardTimeType(), EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)) {
                    return;
                }
                
                if (userBatteryMemberCard.getDisableMemberCardTime() + eleDisableMemberCardRecord.getChooseDays() * 24 * 60 * 60 * 1000L > System.currentTimeMillis()) {
                    return;
                }
                
                // 处理企业用户对应的支付记录时间
                anotherPayMembercardRecordService.systemEnableMemberCardHandler(userBatteryMemberCard.getUid(), eleDisableMemberCardRecord);
                
                // 更新用户套餐到期时间，启用用户套餐
                UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);
                
                // 更新电池服务费产生时间
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userBatteryMemberCard.getUid());
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
                
                // 需求上线时避免重启中未删除缓存导致报错，以后无用
                BigDecimal freezeServiceCharge = batteryMemberCard.getFreezeServiceCharge();
                if (Objects.isNull(freezeServiceCharge)) {
                    freezeServiceCharge = batteryMemberCard.getServiceCharge();
                }
                
                BigDecimal serviceFee = BigDecimal.ZERO;
                if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && Objects.nonNull(freezeServiceCharge)) {
                    serviceFee = freezeServiceCharge.multiply(BigDecimal.valueOf(eleDisableMemberCardRecord.getChooseDays()));
                }
                
                // 生成启用记录
                EnableMemberCardRecord enableMemberCardRecordInsert = EnableMemberCardRecord.builder().disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                        .memberCardName(eleDisableMemberCardRecord.getMemberCardName()).memberCardId(eleDisableMemberCardRecord.getBatteryMemberCardId())
                        .enableTime(System.currentTimeMillis()).enableType(EnableMemberCardRecord.SYSTEM_ENABLE).batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT)
                        .disableDays(eleDisableMemberCardRecord.getChooseDays()).disableTime(eleDisableMemberCardRecord.getCreateTime()).storeId(userInfo.getStoreId())
                        .franchiseeId(userInfo.getFranchiseeId()).phone(userInfo.getPhone()).serviceFee(serviceFee).createTime(System.currentTimeMillis())
                        .tenantId(userInfo.getTenantId()).uid(userInfo.getUid()).userName(userInfo.getName()).updateTime(System.currentTimeMillis())
                        .orderId(userBatteryMemberCard.getOrderId()).build();
                enableMemberCardRecordService.insert(enableMemberCardRecordInsert);
                
                // 获取滞纳金订单
                EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(item.getPauseOrderNo());
                if (Objects.nonNull(eleBatteryServiceFeeOrder)) {
                    EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
                    eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
                    eleBatteryServiceFeeOrderUpdate.setPayAmount(freezeServiceCharge.multiply(BigDecimal.valueOf(eleDisableMemberCardRecord.getChooseDays())));
                    eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeEndTime(
                            userBatteryMemberCard.getDisableMemberCardTime() + eleDisableMemberCardRecord.getChooseDays() * 24 * 60 * 60 * 1000L);
                    eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
                    eleBatteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);
                }
                
                // 记录企业用户冻结后启用套餐记录
                enterpriseUserCostRecordService.asyncSaveUserCostRecordForBattery(userInfo.getUid(),
                        enableMemberCardRecordInsert.getId() + "_" + enableMemberCardRecordInsert.getDisableMemberCardNo(), UserCostTypeEnum.COST_TYPE_ENABLE_PACKAGE.getCode(),
                        enableMemberCardRecordInsert.getEnableTime());
                
            });
            
            offset += size;
        }
    }
    
    
    private String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid + RandomUtil.randomNumbers(6);
    }
    
    /**
     * 区分单型号或者多型号的服务费收费标准
     *
     * @param franchisee
     * @param userBattery
     * @param userInfo
     * @return
     */
    @Override
    public BigDecimal checkDifferentModelBatteryServiceFee(Franchisee franchisee, UserInfo userInfo, UserBattery userBattery) {
        
        BigDecimal batteryServiceFee = BigDecimal.valueOf(0);
        if (Objects.equals(franchisee.getIsOpenServiceFee(), Franchisee.CLOSE_SERVICE_FEE)) {
            return batteryServiceFee;
        }
        
        if (Objects.isNull(userBattery)) {
            userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
        }
        
        if (Objects.isNull(userBattery)) {
            return batteryServiceFee;
        }
        
        Integer modelType = franchisee.getModelType();
        
        if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
            Integer model = batteryModelService.acquireBatteryModel(null, userInfo.getTenantId());
            List<ModelBatteryDeposit> list = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            for (ModelBatteryDeposit modelBatteryDeposit : list) {
                if (Objects.equals(model, modelBatteryDeposit.getModel())) {
                    batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee();
                    return batteryServiceFee;
                }
            }
        } else {
            batteryServiceFee = franchisee.getBatteryServiceFee();
        }
        return batteryServiceFee;
    }
    
    /**
     * 计算停卡用户电池服务费
     *
     * @param userInfo
     * @param uid
     * @param cardDays
     * @param eleDisableMemberCardRecord
     * @param serviceFeeUserInfo
     * @return
     */
    @Override
    @Deprecated
    public BigDecimal checkUserDisableCardBatteryService(UserInfo userInfo, Long uid, Long cardDays, EleDisableMemberCardRecord eleDisableMemberCardRecord,
            ServiceFeeUserInfo serviceFeeUserInfo) {
        
        if (Objects.isNull(serviceFeeUserInfo)) {
            serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(uid);
        }
        //        if (Objects.isNull(serviceFeeUserInfo) || Objects.equals(serviceFeeUserInfo.getExistBatteryServiceFee(), ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE)) {
        //            return BigDecimal.valueOf(0);
        //        }
        if (Objects.isNull(eleDisableMemberCardRecord)) {
            eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(uid, userInfo.getTenantId());
        }
        
        // 判断服务费
        //        if (Objects.nonNull(eleDisableMemberCardRecord) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && Objects.equals(serviceFeeUserInfo.getExistBatteryServiceFee(), ServiceFeeUserInfo.EXIST_SERVICE_FEE)) {
        //            BigDecimal franchiseeBatteryServiceFee = eleDisableMemberCardRecord.getChargeRate();
        //            //计算服务费
        //            BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(BigDecimal.valueOf(cardDays));
        //            return batteryServiceFee;
        //        } else {
        return BigDecimal.valueOf(0);
        //        }
    }
    
    /**
     * 计算套餐过期用户电池服务费
     *
     * @param userInfo
     * @param franchisee
     * @param cardDays
     * @return
     */
    @Override
    @Deprecated
    public BigDecimal checkUserMemberCardExpireBatteryService(UserInfo userInfo, Franchisee franchisee, Long cardDays) {
        
        if (Objects.isNull(franchisee)) {
            franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        }
        
        if (Objects.isNull(franchisee)) {
            return BigDecimal.valueOf(0);
        }
        
        BigDecimal batteryServiceFee = checkDifferentModelBatteryServiceFee(franchisee, userInfo, null);
        
        // 判断服务费
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && cardDays >= 1) {
            // 计算服务费
            BigDecimal userMemberCardExpireBatteryServiceFee = batteryServiceFee.multiply(BigDecimal.valueOf(cardDays));
            return userMemberCardExpireBatteryServiceFee;
        } else {
            return BigDecimal.valueOf(0);
        }
    }
    
    // 停卡审核通知
    private void sendDisableMemberCardMessage(UserInfo userInfo) {
        List<MqNotifyCommon<AuthenticationAuditMessageNotify>> messageNotifyList = this.buildDisableMemberCardMessageNotify(userInfo);
        if (CollectionUtils.isEmpty(messageNotifyList)) {
            return;
        }
        
        messageNotifyList.forEach(i -> {
            messageSendProducer.sendAsyncMsg(i, "", "", 0);
            log.info("ELE INFO! user authentication audit notify,msg={},uid={}", JsonUtil.toJson(i), userInfo.getUid());
        });
    }
    
    
    private List<MqNotifyCommon<AuthenticationAuditMessageNotify>> buildDisableMemberCardMessageNotify(UserInfo userInfo) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(userInfo.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.warn("ELE WARN! not found maintenanceUserNotifyConfig,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
        
        if ((notifyConfig.getPermissions() & MaintenanceUserNotifyConfig.TYPE_DISABLE_MEMBER_CARD) != MaintenanceUserNotifyConfig.TYPE_DISABLE_MEMBER_CARD) {
            log.info("ELE INFO! not maintenance permission,permissions={},uid={}", notifyConfig.getPermissions(), userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
        
        List<String> phones = JsonUtil.fromJsonArray(notifyConfig.getPhones(), String.class);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(phones)) {
            log.warn("ELE WARN! phones is empty,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
            return Collections.EMPTY_LIST;
        }
        
        return phones.parallelStream().map(item -> {
            AuthenticationAuditMessageNotify messageNotify = new AuthenticationAuditMessageNotify();
            messageNotify.setBusinessCode(StringUtils.isBlank(userInfo.getIdNumber()) ? "/" : userInfo.getIdNumber().substring(userInfo.getIdNumber().length() - 6));
            messageNotify.setUserName(userInfo.getName());
            messageNotify.setAuthTime(DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
            
            MqNotifyCommon<AuthenticationAuditMessageNotify> authMessageNotifyCommon = new MqNotifyCommon<>();
            authMessageNotifyCommon.setTime(System.currentTimeMillis());
            authMessageNotifyCommon.setType(SendMessageTypeEnum.RENTAL_PACKAGE_FREEZE_AUDIT_NOTIFY.getType());
            authMessageNotifyCommon.setPhone(item);
            authMessageNotifyCommon.setData(messageNotify);
            authMessageNotifyCommon.setTenantId(userInfo.getTenantId());
            return authMessageNotifyCommon;
        }).collect(Collectors.toList());
    }
    
    @Override
    public int insert(ElectricityMemberCardOrder electricityMemberCardOrder) {
        return baseMapper.insert(electricityMemberCardOrder);
    }
    
    @Override
    public int updateByID(ElectricityMemberCardOrder electricityMemberCardOrder) {
        return baseMapper.updateById(electricityMemberCardOrder);
    }
    
    @Override
    public ElectricityMemberCardOrder selectByOrderNo(String orderNo) {
        return baseMapper.selectByOrderNo(orderNo);
    }
    
    @Override
    public R queryUserExistMemberCard() {
        
        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        Map<String, Boolean> map = new HashMap<>();
        map.put("isExistBatteryMemberCard", false);
        map.put("isExistCarMemberCard", false);
        
        Long now = System.currentTimeMillis();
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && userBatteryMemberCard.getMemberCardExpireTime() > now) {
            map.put("isExistBatteryMemberCard", true);
        }
        
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getMemberCardExpireTime()) && userCarMemberCard.getMemberCardExpireTime() < now) {
            map.put("isExistCarMemberCard", true);
        }
        
        return R.ok(map);
    }
    
    @Override
    public Triple<Boolean, String, Object> handleRentBatteryMemberCard(String productKey, String deviceName, Set<Integer> userCouponIds, Integer memberCardId, Long franchiseeId,
            UserInfo userInfo) {
        if (Objects.isNull(memberCardId)) {
            return Triple.of(true, "", null);
        }
        
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(memberCardId);
        if (Objects.isNull(electricityMemberCard)) {
            log.error("BATTERY MEMBER ORDER ERROR!not found battery membercard,membercardId={},uid={}", memberCardId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("BATTERY MEMBER ORDER ERROR!battery membercard is un_usable membercardId={},uid={}", memberCardId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0088", "月卡已禁用!");
        }
        
        // 购买套餐扫码的柜机
        Long refId = null;
        // 购买套餐来源
        Integer source = ElectricityMemberCardOrder.SOURCE_NOT_SCAN;
        if (StringUtils.isNotBlank(productKey) && StringUtils.isNotBlank(deviceName)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
            if (Objects.isNull(electricityCabinet)) {
                log.error("BATTERY MEMBER ORDER ERROR! not found electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
                return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
            }
            
            // 查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,eid={},uid={}", electricityCabinet.getId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,storeId={},uid={}", electricityCabinet.getStoreId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
            }
            
            // 查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found Franchisee,storeId={},uid={}", store.getId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }
            
            // 换电柜加盟商和套餐加盟商一致  则保存套餐来源
            if (Objects.equals(store.getFranchiseeId(), electricityMemberCard.getFranchiseeId())) {
                source = ElectricityMemberCardOrder.SOURCE_SCAN;
                refId = electricityCabinet.getId().longValue();
            }
        }
        
        // 多加盟商版本增加：加盟商一致性校验
        // 查找计算优惠券
        // 计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = calculatePayAmount(electricityMemberCard.getHolidayPrice(), userCouponIds,
                electricityMemberCard.getFranchiseeId());
        if (Boolean.FALSE.equals(calculatePayAmountResult.getLeft())) {
            return calculatePayAmountResult;
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        Integer payCount = this.queryMaxPayCount(userBatteryMemberCard);
        
        // 支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(memberCardId.longValue());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(electricityMemberCard.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setSource(source);
        electricityMemberCardOrder.setRefId(refId);
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setCouponIds(CollectionUtils.isEmpty(userCouponIds) ? null : JsonUtil.toJson(userCouponIds));
        
        return Triple.of(true, null, electricityMemberCardOrder);
    }
    
    @Override
    public R cancelPayMemberCard() {
        
        // 用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("cancel MEMBER CARD ERROR! not found user ");
            return R.ok();
        }
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_BATTERY_CANCELL_PAYMENT_LOCK_KEY + user.getUid(), "1", 5 * 1000L, false)) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        // 校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("cancel MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.ok();
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = baseMapper.queryCreateTimeMaxMemberCardOrder(userInfo.getUid(), userInfo.getTenantId());
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getStatus(), ElectricityMemberCardOrder.STATUS_INIT)) {
            return R.ok();
        }
        
        // 取消支付  清除套餐来源
        ElectricityMemberCardOrder memberCardOrderUpdate = new ElectricityMemberCardOrder();
        memberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        memberCardOrderUpdate.setSource(NumberConstant.ZERO);
        memberCardOrderUpdate.setRefId(NumberConstant.ZERO_L);
        memberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_CANCEL);// 更新订单状态 为取消支付
        memberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        memberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.baseMapper.updateById(memberCardOrderUpdate);
        
        // 获取套餐订单优惠券
        List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());
        if (CollectionUtils.isEmpty(userCouponIds)) {
            return R.ok();
        }
        
        Set<Integer> couponIds = userCouponIds.parallelStream().map(item -> userCouponService.queryByIdFromDB(item.intValue())).filter(Objects::nonNull)
                .filter(e -> Objects.equals(e.getStatus(), UserCoupon.STATUS_IS_BEING_VERIFICATION)).map(i -> i.getId().intValue()).collect(Collectors.toSet());
        
        userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_UNUSED, null));
        
        redisService.delete(WechatPayConstant.PAY_ORDER_ID_CALL_BACK + electricityMemberCardOrder.getOrderId());
        
        return R.ok();
    }
    
    /**
     * 结束订单
     */
    @Override
    public Triple<Boolean, String, Object> endOrder(String orderNo, Long uid) {
        
        if (!redisService.setNx(CacheConstant.ELE_CACHE_BATTERY_CANCELL_PAYMENT_LOCK_KEY + uid, "1", 5 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = this.baseMapper.selectOne(
                new LambdaQueryWrapper<ElectricityMemberCardOrder>().eq(ElectricityMemberCardOrder::getOrderId, orderNo).eq(ElectricityMemberCardOrder::getUid, uid)
                        .in(ElectricityMemberCardOrder::getStatus, ElectricityMemberCardOrder.STATUS_INIT, ElectricityMemberCardOrder.STATUS_FAIL));
        
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.error("BATTERY MEMBERCARD ERROR!not found electricityMemberCardOrder,uid={},orderId={}", uid, orderNo);
            return Triple.of(false, "ELECTRICITY.0015", "订单不存在！");
        }
        
        // 取消支付  清除套餐来源
        ElectricityMemberCardOrder memberCardOrderUpdate = new ElectricityMemberCardOrder();
        memberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        memberCardOrderUpdate.setSource(NumberConstant.ZERO);
        memberCardOrderUpdate.setRefId(NumberConstant.ZERO_L);
        memberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_CANCEL);// 更新订单状态 为取消支付
        memberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.baseMapper.updateById(memberCardOrderUpdate);
        
        // 获取套餐订单优惠券
        List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());
        if (CollectionUtils.isEmpty(userCouponIds)) {
            return Triple.of(true, "", null);
        }
        
        Set<Integer> couponIds = userCouponIds.parallelStream().map(item -> userCouponService.queryByIdFromDB(item.intValue())).filter(Objects::nonNull)
                .filter(e -> Objects.equals(e.getStatus(), UserCoupon.STATUS_IS_BEING_VERIFICATION)).map(i -> i.getId().intValue()).collect(Collectors.toSet());
        
        userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_UNUSED, null));
        
        return Triple.of(true, "", null);
    }
    
    @Override
    public Triple<Boolean, String, Object> addUserDepositAndMemberCard(UserBatteryDepositAndMembercardQuery query) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_ADD_USER_DEPOSIT_MEMBER_CARD_LOCK + query.getUid(), "1", 2 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        // 参数不可为空
        if (Objects.isNull(query) || Objects.isNull(query.getBatteryDeposit()) || BigDecimalUtil.smallerThanZero(query.getBatteryDeposit())) {
            return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getMembercardId());
        if (Objects.isNull(batteryMemberCard)) {
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
        
        // 判断套餐用户分组和用户的用户分组是否匹配
        Triple<Boolean, String, Object> checkTriple = batteryMemberCardService.checkUserInfoGroupWithMemberCard(userInfo, batteryMemberCard.getFranchiseeId(), batteryMemberCard,
                CHECK_USERINFO_GROUP_ADMIN);
        
        if (Boolean.FALSE.equals(checkTriple.getLeft())) {
            return checkTriple;
        }
        
        if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                batteryMemberCard.getFranchiseeId())) {
            return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
        }
        
        if (Objects.nonNull(query.getStoreId())) {
            Store store = storeService.queryByIdFromCache(query.getStoreId());
            if (Objects.isNull(store) || !Objects.equals(batteryMemberCard.getFranchiseeId(), store.getFranchiseeId())) {
                return Triple.of(false, "100464", "加盟商与门店不匹配，请重新选择门店与套餐");
            }
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
    
        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(userInfo.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            return Triple.of(false, "120163", "账号处于注销缓冲期内，无法操作");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0042", "用户已缴纳押金");
        }
        
        if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            return Triple.of(false, "110211", "用户已缴纳车电一体押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
            return Triple.of(false, "ELECTRICITY.00121", "用户已绑定电池套餐");
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = saveUserInfoAndOrder(userInfo, batteryMemberCard, userBatteryMemberCard, query);
        
        // 8. 处理分账
        DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
        divisionAccountOrderDTO.setOrderNo(electricityMemberCardOrder.getOrderId());
        divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode());
        divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
        divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
        
        // 9. 处理活动
        ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
        activityProcessDTO.setOrderNo(electricityMemberCardOrder.getOrderId());
        activityProcessDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
        activityProcessDTO.setTraceId(IdUtil.simpleUUID());
        activityService.asyncProcessActivity(activityProcessDTO);
        
        // 赠送优惠券
        sendUserCoupon(batteryMemberCard, electricityMemberCardOrder);
        
        // 添加用户操作记录失败
        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("packageName", batteryMemberCard.getName());
            map.put("phone", userInfo.getPhone());
            map.put("name", userInfo.getName());
            map.put("businessType", batteryMemberCard.getBusinessType());
            operateRecordUtil.record(null, map);
        } catch (Exception e) {
            log.error("Failed to add user action record: ", e);
        }
        return Triple.of(true, null, null);
    }
    
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
                .payCount(queryMaxPayCount(userBatteryMemberCard) + 1).payType(ElectricityMemberCardOrder.OFFLINE_PAYMENT).refId(null)
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
        userBatteryMemberCardUpdate.setCardPayCount(queryMaxPayCount(userBatteryMemberCard) + 1);
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
    
    @Override
    public Triple<Boolean, String, Object> editUserBatteryMemberCard(UserBatteryMembercardQuery query) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_MEMBER_CARD_EDIT_LOCK + query.getUid(), "1", 2 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        User user = userService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(user)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getMembercardId());
        if (Objects.isNull(batteryMemberCard)) {
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
        
        if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
            return Triple.of(false, "100275", "电池套餐不可用");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
            return Triple.of(false, "100247", "用户套餐冻结中，不允许操作");
        }
        
        BatteryMemberCard userBindbatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                userBindbatteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBER ORDER WARN! user exist battery service fee,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            return Triple.of(false, "302003", "运营商配置异常，请联系客服");
        }
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        if (Objects.nonNull(query.getMemberCardExpireTime()) || Objects.nonNull(query.getValidDays()) || Objects.nonNull(query.getUseCount())) {
            if (Objects.isNull(query.getUseCount())) {
                // 不限次套餐
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
                    userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime()
                            : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime()
                            : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                } else {
                    Long tempTime = Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() - userBatteryMemberCard.getOrderExpireTime()
                            : (System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()))
                                    - userBatteryMemberCard.getOrderExpireTime();
                    
                    userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime()
                            : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + tempTime);
                }
            } else {
                // 限次套餐
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || userBatteryMemberCard.getRemainingNumber() <= 0) {
                    userBatteryMemberCardUpdate.setOrderRemainingNumber(query.getUseCount());
                    userBatteryMemberCardUpdate.setRemainingNumber(query.getUseCount());
                    
                    if (Objects.nonNull(query.getMemberCardExpireTime()) || Objects.nonNull(query.getValidDays())) {
                        userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime()
                                : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                        userBatteryMemberCardUpdate.setMemberCardExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime()
                                : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                    } else {
                        userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime());
                        userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
                    }
                } else {
                    Long tempUseCount = query.getUseCount() - userBatteryMemberCard.getOrderRemainingNumber();
                    userBatteryMemberCardUpdate.setOrderRemainingNumber(query.getUseCount());
                    userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + tempUseCount);
                    
                    if (Objects.nonNull(query.getMemberCardExpireTime()) || Objects.nonNull(query.getValidDays())) {
                        Long tempTime = Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() - userBatteryMemberCard.getOrderExpireTime()
                                : (System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()))
                                        - userBatteryMemberCard.getOrderExpireTime();
                        userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime()
                                : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                        userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + tempTime);
                    } else {
                        userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime());
                        userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
                    }
                }
                
                //                if (Objects.nonNull(query.getUseCount()) && query.getUseCount() == 0) {
                //                    userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis());
                //                    Long tempTime = Math.abs(userBatteryMemberCard.getOrderExpireTime() - System.currentTimeMillis());
                //                    userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() - tempTime);
                //                }
            }
        }
        
        Integer i = userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
        
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        
        if (Objects.nonNull(query.getMemberCardExpireTime()) || Objects.nonNull(query.getValidDays()) || Objects.nonNull(query.getUseCount())) {
            double oldValidDays = 0.0;
            double newValidDays = 0.0;
            Long oldMaxUseCount = 0L;
            Long newMaxUseCount = 0L;
            
            if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && Objects.nonNull(userBatteryMemberCardUpdate.getMemberCardExpireTime()) && !Objects.equals(
                    userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                // oldValidDays = Math.toIntExact(((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                // newValidDays = Math.toIntExact(((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000));
                oldValidDays = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
                newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
            }
            
            oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
            newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
            
            EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                    .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                    .uid(userInfo.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldValidDays((int) oldValidDays)
                    .newValidDays((int) newValidDays).oldMaxUseCount(oldMaxUseCount).newMaxUseCount(newMaxUseCount).tenantId(TenantContextHolder.getTenantId())
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);
        }
        
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects.equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }
        // 添加用户操作记录
        try {
            BatteryMemberCard card = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            Map<String, Object> map = BeanUtil.beanToMap(userBatteryMemberCardUpdate, false, true);
            map.put("packageName", card.getName());
            map.put("phone", userInfo.getPhone());
            map.put("name", userInfo.getName());
            operateRecordUtil.record(userBatteryMemberCard, map);
        } catch (Exception e) {
            log.error("The user failed to modify the battery plan record because: ", e);
        }
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> renewalUserBatteryMemberCard(UserBatteryMembercardQuery query) {
        boolean result = redisService.setNx(CacheConstant.CACHE_USER_BATTERY_MEMBER_CARD_RENEWAL_LOCK + query.getUid(), "1", 2 * 1000L, false);
        if (!result) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        User user = userService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(user)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        BatteryMemberCard batteryMemberCardToBuy = batteryMemberCardService.queryByIdFromCache(query.getMembercardId());
        if (Objects.isNull(batteryMemberCardToBuy)) {
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
        
        if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCardToBuy.getStatus())) {
            return Triple.of(false, "100275", "电池套餐不可用");
        }
        
        // 检查用户与套餐的用户分组是否匹配
        Triple<Boolean, String, Object> checkTriple = batteryMemberCardService.checkUserInfoGroupWithMemberCard(userInfo, batteryMemberCardToBuy.getFranchiseeId(),
                batteryMemberCardToBuy, CHECK_USERINFO_GROUP_ADMIN);
        
        if (Boolean.FALSE.equals(checkTriple.getLeft())) {
            return checkTriple;
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
    
        // 是否为"注销中"
        UserDelRecord userDelRecord = userDelRecordService.queryByUidAndStatus(userInfo.getUid(), List.of(UserStatusEnum.USER_STATUS_CANCELLING.getCode()));
        if (Objects.nonNull(userDelRecord)) {
            return Triple.of(false, "120163", "账号处于注销缓冲期内，无法操作");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("ELE DEPOSIT WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        // 判断套餐是否为免押套餐
        if (Objects.equals(userBatteryDeposit.getDepositType(), UserBatteryDeposit.DEPOSIT_TYPE_FREE) && !Objects.equals(batteryMemberCardToBuy.getFreeDeposite(),
                BatteryMemberCard.YES)) {
            log.warn("ELE DEPOSIT WARN! batteryMemberCard is illegal,uid={},mid={}", userInfo.getUid(), query.getMembercardId());
            return Triple.of(false, "100483", "电池套餐不合法");
        }
        
        // 是否有正在进行中的退押
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0047", "电池押金退款中");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            // 免押后给用户绑定套餐
            return freeDepositBindUserMembercerd(userInfo, batteryMemberCardToBuy, userBatteryDeposit.getOrderId());
        }
        
        if (Objects.equals(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
            return Triple.of(false, "100247", "用户套餐冻结中，不允许操作");
        }
        
        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
            log.warn("ELE DEPOSIT WARN! battery membercard refund review,uid={}", userInfo.getUid());
            return Triple.of(false, "100018", "套餐租金退款审核中");
        }
        
        BatteryMemberCard userBindbatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                userBindbatteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBER ORDER WARN! user exist battery service fee,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig)) {
            return Triple.of(false, "302003", "运营商配置异常，请联系客服");
        }
        
        List<String> userBatteryTypes = userBatteryTypeService.selectByUid(userInfo.getUid());
        boolean matchOrNot = memberCardBatteryTypeService.checkBatteryTypeAndDepositWithUser(userBatteryTypes, batteryMemberCardToBuy, userBatteryDeposit, electricityConfig,
                userInfo);
        if (!matchOrNot) {
            return Triple.of(false, "302004", "灵活续费已禁用，请刷新后重新购买");
        }
        
        BigDecimal deposit =
                (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES) || Objects.equals(userBatteryDeposit.getDepositModifyFlag(),
                        UserBatteryDeposit.DEPOSIT_MODIFY_SPECIAL)) ? userBatteryDeposit.getBeforeModifyDeposit() : userBatteryDeposit.getBatteryDeposit();
        if (batteryMemberCardToBuy.getDeposit().compareTo(deposit) > 0) {
            return Triple.of(false, "100033", "套餐押金金额与缴纳押金不匹配，请刷新重试");
        }

        //  免押服务费判断
        IsSupportFreeServiceFeeDTO supportFreeServiceFee = freeServiceFeeOrderService.isSupportFreeServiceFee(userInfo, userBatteryDeposit.getOrderId());
        if (supportFreeServiceFee.getSupportFreeServiceFee()) {
            CreateFreeServiceFeeOrderDTO createFreeServiceFeeOrderDTO = CreateFreeServiceFeeOrderDTO.builder()
                    .userInfo(userInfo)
                    .depositOrderId(userBatteryDeposit.getOrderId())
                    .freeServiceFee(supportFreeServiceFee.getFreeServiceFee())
                    .status(FreeServiceFeeStatusEnum.STATUS_SUCCESS.getStatus())
                    .payTime(System.currentTimeMillis())
                    .paymentChannel(null)
                    .build();
            freeServiceFeeOrderService.insertOrder(freeServiceFeeOrderService.createFreeServiceFeeOrder(createFreeServiceFeeOrderDTO));
        }

        ElectricityMemberCardOrder memberCardOrder = saveRenewalUserBatteryMemberCardOrder(user, userInfo, batteryMemberCardToBuy, userBatteryMemberCard, userBindbatteryMemberCard,
                null, null, null,null);
        
        // 8. 处理分账
        DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
        divisionAccountOrderDTO.setOrderNo(memberCardOrder.getOrderId());
        divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode());
        divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
        divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
        
        // 9. 处理活动
        ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
        activityProcessDTO.setOrderNo(memberCardOrder.getOrderId());
        activityProcessDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
        activityProcessDTO.setTraceId(IdUtil.simpleUUID());
        activityService.asyncProcessActivity(activityProcessDTO);


        
        sendUserCoupon(batteryMemberCardToBuy, memberCardOrder);
        Map<String, Object> map = new HashMap<>();
        map.put("username", userInfo.getName());
        map.put("phone", userInfo.getPhone());
        map.put("packageName", batteryMemberCardToBuy.getName());
        operateRecordUtil.record(null, map);
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> freeDepositBindUserMembercerd(UserInfo userInfo, BatteryMemberCard batteryMemberCard, String depoisitOrderId) {
        ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
        memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        memberCardOrder.setMemberCardId(batteryMemberCard.getId());
        memberCardOrder.setUid(userInfo.getUid());
        memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        memberCardOrder.setCardName(batteryMemberCard.getName());
        memberCardOrder.setPayAmount(batteryMemberCard.getRentPrice());
        memberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
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
        this.insert(memberCardOrder);

        //  免押服务费判断
        IsSupportFreeServiceFeeDTO supportFreeServiceFee = freeServiceFeeOrderService.isSupportFreeServiceFee(userInfo, depoisitOrderId);
        if (supportFreeServiceFee.getSupportFreeServiceFee()) {
            CreateFreeServiceFeeOrderDTO createFreeServiceFeeOrderDTO = CreateFreeServiceFeeOrderDTO.builder()
                    .userInfo(userInfo)
                    .depositOrderId(depoisitOrderId)
                    .freeServiceFee(supportFreeServiceFee.getFreeServiceFee())
                    .status(FreeServiceFeeStatusEnum.STATUS_SUCCESS.getStatus())
                    .payTime(System.currentTimeMillis())
                    .paymentChannel(null)
                    .build();
            freeServiceFeeOrderService.insertOrder(freeServiceFeeOrderService.createFreeServiceFeeOrder(createFreeServiceFeeOrderDTO));
        }


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
        
        // 更新用户绑定的电池型号
        userBatteryTypeService.updateUserBatteryType(memberCardOrder, userInfo);
        
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardUpdate.getUid());
        ServiceFeeUserInfo serviceFeeUserInfoInsert = new ServiceFeeUserInfo();
        serviceFeeUserInfoInsert.setServiceFeeGenerateTime(
                System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
        serviceFeeUserInfoInsert.setUid(userBatteryMemberCardUpdate.getUid());
        serviceFeeUserInfoInsert.setFranchiseeId(memberCardOrder.getFranchiseeId());
        serviceFeeUserInfoInsert.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoInsert.setTenantId(memberCardOrder.getTenantId());
        serviceFeeUserInfoInsert.setCreateTime(System.currentTimeMillis());
        serviceFeeUserInfoInsert.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
        serviceFeeUserInfoInsert.setDisableMemberCardNo("");
        if (Objects.nonNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsert);
        } else {
            serviceFeeUserInfoService.insert(serviceFeeUserInfoInsert);
        }
        
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(userInfo.getUid());
        userInfoUpdate.setPayCount(userInfo.getPayCount() + 1);
        userInfoUpdate.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(userInfoUpdate);
        
        // 8. 处理分账
        DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
        divisionAccountOrderDTO.setOrderNo(memberCardOrder.getOrderId());
        divisionAccountOrderDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode());
        divisionAccountOrderDTO.setTraceId(IdUtil.simpleUUID());
        divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
        
        // 9. 处理活动
        ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
        activityProcessDTO.setOrderNo(memberCardOrder.getOrderId());
        activityProcessDTO.setType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
        activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
        activityProcessDTO.setTraceId(IdUtil.simpleUUID());
        activityService.asyncProcessActivity(activityProcessDTO);



        // 赠送优惠券
        sendUserCoupon(batteryMemberCard, memberCardOrder);
        
        double newValidDays = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 3600000 / 24.0);
        
        EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                .uid(userInfo.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldValidDays(0)
                .newValidDays((int) newValidDays).oldMaxUseCount(0L).newMaxUseCount(userBatteryMemberCardUpdate.getRemainingNumber()).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public ElectricityMemberCardOrder saveRenewalUserBatteryMemberCardOrder(User user, UserInfo userInfo, BatteryMemberCard batteryMemberCard,
            UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard userBindbatteryMemberCard, InstallmentRecord installmentRecord, Integer source,
            List<InstallmentDeductionPlan> deductionPlans, Integer type) {
        
        // 分期套餐由此接入，若不传递代扣记录则为普通的后台续费套餐
        ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
        if (Objects.isNull(installmentRecord)) {
            memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
            memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            memberCardOrder.setMemberCardId(batteryMemberCard.getId());
            memberCardOrder.setUid(userInfo.getUid());
            memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
            memberCardOrder.setCardName(batteryMemberCard.getName());
            memberCardOrder.setPayAmount(batteryMemberCard.getRentPrice());
            memberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
            memberCardOrder.setPayCount(queryMaxPayCount(userBatteryMemberCard) + 1);
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
        } else {
            // 传递代扣记录则为续费分期套餐子订单
            memberCardOrder = applicationContext.getBean(ElectricityMemberCardOrderServiceImpl.class)
                    .generateInstallmentMemberCardOrder(userInfo, batteryMemberCard, null, installmentRecord, deductionPlans).getRight();
            memberCardOrder.setSource(source);
        }
        
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)
                || userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || Objects.isNull(userBindbatteryMemberCard) || (
                Objects.equals(userBindbatteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
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
            userBatteryMemberCardUpdate.setCardPayCount(queryMaxPayCount(userBatteryMemberCard) + 1);
            
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
            userBatteryMemberCardUpdate.setCardPayCount(queryMaxPayCount(userBatteryMemberCard) + 1);
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

        if (Objects.equals(type, InstallmentConstants.DEDUCTION_PLAN_OFFLINE_AGREEMENT)){
            memberCardOrder.setPaymentChannel(null);
        }
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
        
        // 分期套餐不需要保存操作记录
        if (Objects.nonNull(installmentRecord)) {
            return memberCardOrder;
        }
        
        EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(SecurityUtils.getUid())
                .uid(userInfo.getUid()).name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername()).oldValidDays((int) oldValidDays)
                .newValidDays((int) newValidDays).oldMaxUseCount(oldMaxUseCount).newMaxUseCount(newMaxUseCount).tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);
        
        return memberCardOrder;
    }
    
    @Slave
    @Override
    public List<ElectricityMemberCardOrder> listOrderByExternalAgreementNo(String externalAgreementNo) {
        return electricityMemberCardOrderMapper.selectListOrderByExternalAgreementNo(externalAgreementNo);
    }
    
    @Override
    public void sendUserCoupon(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder) {
        if (StringUtils.isBlank(memberCardOrder.getCouponIds()) && Objects.isNull(memberCardOrder.getSendCouponId())) {
            return;
        }
        
        // 新旧数据兼容处理，防止优惠券重复发放
        HashSet<Long> couponIdSet = new HashSet<>();
        if (Objects.nonNull(memberCardOrder.getSendCouponId()) && !Objects.equals(memberCardOrder.getSendCouponId(), ElectricityMemberCardOrder.SEND_COUPON_ID_DEFAULT_VALUE)) {
            couponIdSet.add(memberCardOrder.getSendCouponId());
        }
        if (StringUtils.isNotBlank(memberCardOrder.getCouponIds())) {
            couponIdSet.addAll(JsonUtil.fromJsonArray(memberCardOrder.getCouponIds(), Long.class));
        }
        
        // 发送优惠券
        couponIdSet.forEach(couponId -> {
            UserCouponDTO userCouponDTO = new UserCouponDTO();
            userCouponDTO.setCouponId(couponId);
            userCouponDTO.setPackageId(batteryMemberCard.getId());
            userCouponDTO.setUid(memberCardOrder.getUid());
            userCouponDTO.setSourceOrderNo(memberCardOrder.getOrderId());
            userCouponDTO.setTraceId(IdUtil.simpleUUID());
            userCouponDTO.setCouponType(CouponTypeEnum.BATTERY_BUY_PACKAGE.getCode());
            userCouponService.asyncSendCoupon(userCouponDTO);
        });
        
    }
    
    @Override
    public Integer batchUpdateChannelOrderStatusByOrderNo(List<String> orderIds, Integer useStatus) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return NumberConstant.ZERO;
        }
        return this.baseMapper.batchUpdateChannelOrderStatusByOrderNo(orderIds, useStatus);
    }
    
    @Override
    public Triple<Boolean, String, Object> userBatteryMembercardInfo(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            return Triple.of(true, null, null);
        }
        
        BeanUtils.copyProperties(batteryMemberCard, batteryMemberCardVO);
        
        ElectricityMemberCardOrder userMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(userBatteryMemberCard.getOrderId());
        if (Objects.isNull(userMemberCardOrder)) {
            return Triple.of(true, null, batteryMemberCardVO);
        }
        
        if (Objects.equals(batteryMemberCard.getIsRefund(), BatteryMemberCard.YES)) {
            batteryMemberCardVO.setEditUserMembercard(
                    System.currentTimeMillis() > (userMemberCardOrder.getCreateTime() + batteryMemberCard.getRefundLimit() * 24 * 60 * 60 * 1000L));
        }
        
        return Triple.of(true, null, batteryMemberCardVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> userBatteryDepositAndMembercardInfo() {
        UserBatteryMemberCardInfoVO userBatteryMemberCardInfoVO = new UserBatteryMemberCardInfoVO();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("ELE WARN!not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        userBatteryMemberCardInfoVO.setModelType(Objects.isNull(franchisee) ? null : franchisee.getModelType());
        userBatteryMemberCardInfoVO.setBatteryRentStatus(userInfo.getBatteryRentStatus());
        userBatteryMemberCardInfoVO.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());
        userBatteryMemberCardInfoVO.setFranchiseeId(userInfo.getFranchiseeId());
        userBatteryMemberCardInfoVO.setStoreId(userInfo.getStoreId());
        userBatteryMemberCardInfoVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.NO);
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        // 设置用户剩余冻结次数
        try {
            userBatteryMemberCardInfoVO.setUnusedFreezeCount(userInfoExtraService.getUnusedFreezeCount(electricityConfig, userInfo.getUid()));
        } catch (BizException e) {
            return Triple.of(false, e.getErrCode(), e.getErrMsg());
        }
        
        // 设置用户可申请冻结的最大天数
        if (Objects.nonNull(electricityConfig)) {
            boolean hasAssets = carRentalPackageOrderBizService.checkUserHasAssets(userInfo, userInfo.getTenantId(), CarRentalPackageOrderBizServiceImpl.ELE);
            userBatteryMemberCardInfoVO.setMaxFreezeDays(hasAssets ? electricityConfig.getPackageFreezeDaysWithAssets() : electricityConfig.getPackageFreezeDays());
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || StringUtils.isBlank(userBatteryDeposit.getOrderId())) {
            log.warn("ELE WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        
        userBatteryMemberCardInfoVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            log.warn("ELE WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        
        userBatteryMemberCardInfoVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.YES);
        userBatteryMemberCardInfoVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        userBatteryMemberCardInfoVO.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        userBatteryMemberCardInfoVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        userBatteryMemberCardInfoVO.setOrderRemainingNumber(userBatteryMemberCard.getOrderRemainingNumber());
        userBatteryMemberCardInfoVO.setMemberCardId(userBatteryMemberCard.getMemberCardId());
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("ELE WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }
        if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY)) {
            userBatteryMemberCardInfoVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 24 / 60 / 60 / 1000.0) : 0);
        } else {
            userBatteryMemberCardInfoVO.setValidDays(userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() ? (int) Math.ceil(
                    (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 60 / 1000.0) : 0);
        }
        
        // 套餐订单金额
        ElectricityMemberCardOrder electricityMemberCardOrder = this.selectByOrderNo(userBatteryMemberCard.getOrderId());
        userBatteryMemberCardInfoVO.setBatteryMembercardPayAmount(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getPayAmount());
        userBatteryMemberCardInfoVO.setMemberCardPayTime(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());
        userBatteryMemberCardInfoVO.setMemberCardName(batteryMemberCard.getName());
        userBatteryMemberCardInfoVO.setRentUnit(batteryMemberCard.getRentUnit());
        userBatteryMemberCardInfoVO.setLimitCount(batteryMemberCard.getLimitCount());
        userBatteryMemberCardInfoVO.setBusinessType(batteryMemberCard.getBusinessType());
        userBatteryMemberCardInfoVO.setPayType(electricityMemberCardOrder.getPayType());
        
        // 用户电池型号
        userBatteryMemberCardInfoVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()));
        
        // 查询当前用户是否存在最新的冻结订单信息
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(SecurityUtils.getUid(),
                TenantContextHolder.getTenantId());
        if (Objects.nonNull(eleDisableMemberCardRecord) && UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW_REFUSE.equals(eleDisableMemberCardRecord.getStatus())) {
            userBatteryMemberCardInfoVO.setRejectReason(eleDisableMemberCardRecord.getErrMsg());
        }
        
        // 检查用户自主续费状态
        Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(userInfo.getUid());
        if (userRenewalStatus) {
            userBatteryMemberCardInfoVO.setRenewalStatus(RenewalStatusEnum.RENEWAL_STATUS_BY_SELF.getCode());
        }
        
        // 判断用户是否存在与企业渠道用户然后站长退出的表中，类型未未处理或者是处理失败
        List<Integer> typeList = new ArrayList<>();
        typeList.add(EnterpriseChannelUserExit.TYPE_INIT);
        typeList.add(EnterpriseChannelUserExit.TYPE_FAIL);
        List<Long> uidList = new ArrayList<>();
        uidList.add(userInfo.getUid());
        EnterpriseChannelUserExitQueryModel queryModel = EnterpriseChannelUserExitQueryModel.builder().uidList(uidList).typeList(typeList).build();
        List<EnterpriseChannelUserExit> channelUserList = channelUserExitMapper.list(queryModel);
        if (ObjectUtils.isNotEmpty(channelUserList)) {
            userBatteryMemberCardInfoVO.setChannelUserExit(UserBatteryMemberCardInfoVO.YES);
        } else {
            userBatteryMemberCardInfoVO.setChannelUserExit(UserBatteryMemberCardInfoVO.NO);
        }
        
        // 剩余 x天 x时
        userBatteryMemberCardInfoVO.setExpireTimeStr(DateUtils.convertExpireTime(userBatteryMemberCardInfoVO.getMemberCardExpireTime()));
        
        return Triple.of(true, null, userBatteryMemberCardInfoVO);
    }
    
    @Override
    public Integer queryMaxPayCount(UserBatteryMemberCard userBatteryMemberCard) {
        return Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) ? 0 : userBatteryMemberCard.getCardPayCount();
    }
    
    @Override
    public ElectricityMemberCardOrder selectFirstMemberCardOrder(Long uid) {
        return baseMapper.selectFirstMemberCardOrder(uid);
    }
    
    @Override
    public List<BatteryMemberCardOrderCoupon> buildMemberCardOrderCoupon(String orderId, Set<Integer> couponSet) {
        
        List<BatteryMemberCardOrderCoupon> list = new ArrayList<>(couponSet.size());
        for (Integer id : couponSet) {
            BatteryMemberCardOrderCoupon entity = new BatteryMemberCardOrderCoupon();
            entity.setOrderId(orderId);
            entity.setCouponId(id.longValue());
            entity.setCreateTime(System.currentTimeMillis());
            entity.setUpdateTime(System.currentTimeMillis());
            entity.setTenantId(TenantContextHolder.getTenantId());
            list.add(entity);
        }
        
        return list;
    }
    
    /**
     * 为了兼容旧版小程序
     */
    @Override
    public Set<Integer> generateUserCouponIds(Integer userCouponId, List<Integer> userCouponIds) {
        Set<Integer> couponSet = Sets.newHashSet();
        if (Objects.nonNull(userCouponId)) {
            couponSet.add(userCouponId);
        }
        
        if (CollectionUtils.isNotEmpty(userCouponIds)) {
            couponSet.addAll(userCouponIds);
        }
        
        return couponSet;
    }
    
    @Override
    public List<UserCoupon> buildUserCouponList(Set<Integer> userCouponIds, Integer status, String orderId) {
        List<UserCoupon> list = new ArrayList<>(userCouponIds.size());
        for (Integer userCouponId : userCouponIds) {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setId(userCouponId.longValue());
            userCoupon.setOrderId(orderId);
            userCoupon.setStatus(status);
            userCoupon.setUpdateTime(System.currentTimeMillis());
            list.add(userCoupon);
        }
        return list;
    }
    
    /**
     * 处理套餐绑定的活动
     */
    @Deprecated
    public Long handlerMembercardBindActivity(ElectricityMemberCard electricityMemberCard, UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo, Long remainingNumber) {
        if (Objects.isNull(electricityMemberCard) || Objects.isNull(electricityMemberCard.getActivityId()) || !Objects.equals(electricityMemberCard.getIsBindActivity(),
                ElectricityMemberCard.BIND_ACTIVITY)) {
            return remainingNumber;
        }
        
        OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCard.getActivityId());
        if (Objects.isNull(oldUserActivity)) {
            log.error("MEMBERCARD ACTIVITY ERROR!oldUserActivity is null,uid={},activityId={}", userInfo.getUid(), electricityMemberCard.getActivityId());
            return remainingNumber;
        }
        
        // 判断是否能够参与套餐活动
        if (!isCanJoinMembercardActivity(userBatteryMemberCard, oldUserActivity)) {
            return remainingNumber;
        }
        
        // 送次数
        if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUNT) && Objects.nonNull(oldUserActivity.getCount())) {
            remainingNumber = remainingNumber + oldUserActivity.getCount();
        }
        
        // 送优惠券
        if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {
            userCouponService.batchRelease(oldUserActivity.getCouponId(), ArraysUtil.array(userInfo.getUid()), null);
        }
        
        return remainingNumber;
    }
    
    private boolean isCanJoinMembercardActivity(UserBatteryMemberCard userBatteryMemberCard, OldUserActivity oldUserActivity) {
        
        if (Objects.equals(oldUserActivity.getUserScope(), OldUserActivity.USER_SCOPE_ALL)) {// 套餐活动 用户范围为全部用户
            return Boolean.TRUE;
        } else if (Objects.equals(oldUserActivity.getUserScope(), OldUserActivity.USER_SCOPE_NEW) &&// 套餐活动 用户范围为新用户
                (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() == 0)) {
            return Boolean.TRUE;
        } else if (Objects.equals(oldUserActivity.getUserScope(), OldUserActivity.USER_SCOPE_OLD) &&// 套餐活动 用户范围为老用户
                Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getCardPayCount()) && userBatteryMemberCard.getCardPayCount() > 0) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
    
    @Override
    public Triple<Boolean, String, Object> calculatePayAmount(BigDecimal price, Set<Integer> userCouponIds, Long franchiseeId) {
        BigDecimal payAmount = price;
        
        if (CollectionUtils.isEmpty(userCouponIds)) {
            return Triple.of(true, null, payAmount);
        }
        
        for (Integer userCouponId : userCouponIds) {
            UserCoupon userCoupon = userCouponService.queryByIdFromDB(userCouponId);
            if (Objects.isNull(userCoupon)) {
                log.warn("ELE WARN! not found userCoupon,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }
            
            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.warn("ELE WARN! not found coupon,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }
            
            // 多加盟商版本增加：加盟商一致性校验
            if (!couponService.isSameFranchisee(coupon.getFranchiseeId(), franchiseeId)) {
                log.warn("ELE WARN! coupon is not same franchisee,couponId={},franchiseeId={}", userCouponId, franchiseeId);
                return Triple.of(false, "120126", "此优惠券暂不可用，请选择其他优惠券");
            }
            
            // 优惠券是否使用
            if (!Objects.equals(UserCoupon.STATUS_UNUSED, userCoupon.getStatus())) {
                log.warn("ELE WARN! userCoupon is used,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0090", "优惠券不可用");
            }
            
            // 优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.warn("ELE WARN! userCoupon is deadline,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0091", "您的优惠券已过期");
            }
            
            // 使用折扣劵  折扣券作废，不处理——>产品提的需求
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.DISCOUNT)) {
                log.info("ELE INFO! not found coupon,userCouponId={}", userCouponId);
            }
            
            // 使用满减劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.FULL_REDUCTION)) {
                payAmount = payAmount.subtract(coupon.getAmount());
            }
        }
        
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        return Triple.of(true, null, payAmount);
    }
    
    @Override
    public Triple<Boolean, String, Object> calculatePayAmount(BatteryMemberCard batteryMemberCard, Set<Integer> userCouponIds) {
        BigDecimal payAmount = batteryMemberCard.getRentPrice();
        Long franchiseeId = batteryMemberCard.getFranchiseeId();
        
        if (CollectionUtils.isEmpty(userCouponIds)) {
            return Triple.of(true, null, payAmount);
        }
        
        for (Integer userCouponId : userCouponIds) {
            UserCoupon userCoupon = userCouponService.queryByIdFromDB(userCouponId);
            if (Objects.isNull(userCoupon)) {
                log.warn("ELE WARN! not found userCoupon,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }
            
            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.warn("ELE WARN! not found coupon,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }
            
            // 优惠券为天数券时，从集合内移除避免外部接口核销天数券，同时跳过后续逻辑
            if (Objects.equals(coupon.getDiscountType(), UserCoupon.DAYS)) {
                log.warn("ELE WARN! can not use day coupon,userCouponId={}", userCouponId);
                userCouponIds.remove(userCouponId);
                continue;
            }
            
            // 多加盟商版本增加：加盟商一致性校验
            if (!couponService.isSameFranchisee(coupon.getFranchiseeId(), franchiseeId)) {
                log.warn("ELE WARN! coupon is not same franchisee,couponId={},franchiseeId={}", userCouponId, franchiseeId);
                return Triple.of(false, "120126", "此优惠券暂不可用，请选择其他优惠券");
            }
            
            // 优惠券是否使用
            if (!Objects.equals(UserCoupon.STATUS_UNUSED, userCoupon.getStatus())) {
                log.warn("ELE WARN! userCoupon is used,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0090", "优惠券不可用");
            }
            
            // 优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.warn("ELE WARN! userCoupon is deadline,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0091", "您的优惠券已过期");
            }
            
            // 校验优惠券的使用，是否指定这个套餐
            // 1-租电 2-租车 3-车电一体
            Boolean valid = couponActivityPackageService.checkPackageIsValid(coupon, batteryMemberCard.getId(), PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode());
            if (!valid) {
                log.info("ELE WARN! check the package is valid, couponId={}, userCouponId={}, batteryMemberCardId={}", coupon.getId(), userCouponId, batteryMemberCard.getId());
                return Triple.of(false, "300034", "使用优惠券有误");
            }
            
            // 使用折扣劵  折扣券作废，不处理——>产品提的需求
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.DISCOUNT)) {
                log.info("ELE INFO! not found coupon,userCouponId={}", userCouponId);
            }
            
            // 使用满减劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.FULL_REDUCTION)) {
                payAmount = payAmount.subtract(coupon.getAmount());
            }
        }
        
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        return Triple.of(true, null, payAmount);
    }
    
    @Override
    public Integer updateStatusByOrderNo(ElectricityMemberCardOrder memberCardOrder) {
        return this.baseMapper.updateStatusByOrderNo(memberCardOrder);
    }
    
    @Override
    public Integer batchUpdateStatusByOrderNo(List<String> orderIds, Integer useStatus) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return NumberConstant.ZERO;
        }
        return this.baseMapper.batchUpdateStatusByOrderNo(orderIds, useStatus);
    }
    
    @Override
    public Integer checkOrderByMembercardId(Long membercardId) {
        return baseMapper.checkOrderByMembercardId(membercardId);
    }
    
    private List<String> acquireUserBatteryType(List<String> userBatteryTypeList, List<String> membercardBatteryTypeList) {
        if (CollectionUtils.isEmpty(membercardBatteryTypeList)) {
            return userBatteryTypeList;
        }
        
        if (CollectionUtils.isEmpty(userBatteryTypeList)) {
            return Collections.emptyList();
        }
        
        Set<String> result = new HashSet<>();
        result.addAll(userBatteryTypeList);
        result.addAll(userBatteryTypeList);
        
        return new ArrayList<>(result);
    }
    
    @Override
    public void handlerBatteryMembercardPaymentNotify(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder, UserBatteryMemberCard userBatteryMemberCard,
            UserInfo userInfo) {
        // 用户未绑定套餐
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(),
                NumberConstant.ZERO_L)) {
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
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
            userBatteryMemberCardUpdate.setCardPayCount(Objects.isNull(userBatteryMemberCard) ? 1 : userBatteryMemberCard.getCardPayCount() + 1);
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            if (Objects.isNull(userBatteryMemberCard)) {
                userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);
            } else {
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            }
            
        } else {
            // 用户已绑定套餐
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
            
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setCardPayCount(userBatteryMemberCard.getCardPayCount() + 1);
            userBatteryMemberCardUpdate.setMemberCardExpireTime(
                    userBatteryMemberCard.getMemberCardExpireTime() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);
            
            // 获取用户电池型号
            List<String> userBatteryTypes = acquireUserBatteryType(userBatteryTypeService.selectByUid(userInfo.getUid()),
                    memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId()));
            if (CollectionUtils.isNotEmpty(userBatteryTypes)) {
                // 更新用户电池型号
                userBatteryTypeService.deleteByUid(userInfo.getUid());
                userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(userBatteryTypes, userInfo));
            }
        }
    }
    
    private Triple<Boolean, String, Object> assignOrderSource(ElectricityMemberCardOrderQuery query, ElectricityMemberCardOrder memberCardOrder) {
        if (StringUtils.isBlank(query.getProductKey()) || StringUtils.isBlank(query.getDeviceName())) {
            return Triple.of(true, null, null);
        }
        
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.warn("BATTERY MEMBER ORDER WARN!not found electricityCabinet,p={},d={}", query.getProductKey(), query.getDeviceName());
            return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
        }
        
        memberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_SCAN);
        memberCardOrder.setStoreId(electricityCabinet.getStoreId());
        memberCardOrder.setRefId(electricityCabinet.getId().longValue());
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> handlerNonFirstBuyBatteryMemberCard(ElectricityMemberCardOrderQuery query, UserBatteryMemberCard userBatteryMemberCard,
            BatteryMemberCard batteryMemberCard, UserInfo userInfo, Franchisee franchisee) {
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("BATTERY MEMBER ORDER WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("BATTERY MEMBER ORDER WARN! userBatteryMemberCard is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100241", "当前套餐暂停中，请先启用套餐");
        }
        
        if (!(Objects.equals(BatteryMemberCard.RENT_TYPE_OLD, batteryMemberCard.getRentType()) || Objects.equals(BatteryMemberCard.RENT_TYPE_UNLIMIT,
                batteryMemberCard.getRentType()))) {
            log.warn("BATTERY MEMBER ORDER WARN! new batteryMemberCard not available,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100275", "换电套餐不可用");
        }
        
        BatteryMemberCard userBindBatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(userBindBatteryMemberCard)) {
            log.warn("BATTERY MEMBER ORDER WARN! userBindBatteryMemberCard is null,uid={}", userBatteryMemberCard.getUid());
            return Triple.of(false, "ELECTRICITY.0087", "套餐不存在");
        }
        
        if (!Objects.equals(userBindBatteryMemberCard.getLimitCount(), batteryMemberCard.getLimitCount())) {
            log.warn("BATTERY MEMBER ORDER WARN! batteryMemberCard limitCount inconformity,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100276", "换电套餐类型不一致");
        }
        
        boolean flag = batteryMemberCard.getDeposit().compareTo(userBindBatteryMemberCard.getDeposit()) == 0;
        if (!flag) {
            log.warn("BATTERY MEMBER ORDER WARN! batteryMemberCard deposit inconformity,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100277", "换电套餐押金不一致");
        }
        
        if (Objects.equals(Franchisee.OLD_MODEL_TYPE, franchisee.getModelType())) {
            return Triple.of(true, null, null);
        }
        
        List<String> oldMembercardBatteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(userBindBatteryMemberCard.getId());
        if (CollectionUtils.isEmpty(oldMembercardBatteryTypeList)) {
            log.warn("BATTERY MEMBER ORDER WARN! old batteryMemberCard batteryType illegal,uid={},mid={}", userBatteryMemberCard.getUid(), userBindBatteryMemberCard.getId());
            return Triple.of(false, "100279", "换电套餐电池型号不存在");
        }
        
        List<String> newMembercardBatteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
        if (CollectionUtils.isEmpty(newMembercardBatteryTypeList)) {
            log.warn("BATTERY MEMBER ORDER WARN! new batteryMemberCard batteryType illegal,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100279", "换电套餐电池型号不存在");
        }
        
        if (!CollectionUtils.containsAll(newMembercardBatteryTypeList, oldMembercardBatteryTypeList)) {
            log.warn("BATTERY MEMBER ORDER WARN! batteryType illegal,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100278", "换电套餐电池型号不一致");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> handlerFirstBuyBatteryMemberCard(ElectricityMemberCardOrderQuery query, UserBatteryMemberCard userBatteryMemberCard,
            BatteryMemberCard batteryMemberCard, UserInfo userInfo, Franchisee franchisee) {
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.warn("BATTERY MEMBER ORDER WARN! not allow buy,uid={}", userInfo.getUid());
            return Triple.of(false, "100274", "赠送套餐不允许续费");
        }
        
        if (!(Objects.equals(BatteryMemberCard.RENT_TYPE_NEW, batteryMemberCard.getRentType()) || Objects.equals(BatteryMemberCard.RENT_TYPE_UNLIMIT,
                batteryMemberCard.getRentType()))) {
            log.warn("BATTERY MEMBER ORDER WARN! new batteryMemberCard not available,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100275", "换电套餐不可用");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("BATTERY MEMBER ORDER WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        boolean flag = batteryMemberCard.getDeposit().compareTo(userBatteryDeposit.getBatteryDeposit()) == 0;
        if (!flag) {
            log.warn("BATTERY MEMBER ORDER WARN! batteryMemberCard deposit not equale user battery deposit,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100277", "换电套餐押金不一致");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> verifyUserBatteryInsurance(UserInfo userInfo, Franchisee franchisee, BatteryMemberCard batteryMemberCard) {
        
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
        if (Objects.isNull(electricityConfig) || !Objects.equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.ENABLE_INSURANCE)) {
            return Triple.of(true, null, null);
        }
        
        String batteryV = "";
        if (!Objects.equals(Franchisee.OLD_MODEL_TYPE, franchisee.getModelType())) {
            List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId());
            if (CollectionUtils.isEmpty(batteryTypeList)) {
                log.warn("BATTERY MEMBER ORDER WARN! not found batteryTypeList,mid={}", batteryMemberCard.getId());
                return Triple.of(false, "100279", "换电套餐电池型号不存在");
            }
            
            String batteryType = batteryTypeList.get(0);
            
            batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
        }
        
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.selectByFranchiseeIdAndType(userInfo.getFranchiseeId(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY,
                batteryV);
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getIsConstraint(), FranchiseeInsurance.CONSTRAINT_FORCE)) {
            return Triple.of(true, null, null);
        }
        
        // 用户是否没有保险信息或已过期
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(insuranceUserInfo) || !Objects.equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.NOT_USE)
                || insuranceUserInfo.getInsuranceExpireTime() < System.currentTimeMillis()) {
            log.warn("BATTERY MEMBER ORDER WARN! not pay battery insurance,uid={}", userInfo.getUid());
            return Triple.of(false, "100309", "未购买保险或保险已过期");
        }
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public Integer countRefundOrderByUid(Long uid) {
        return baseMapper.countRefundOrderByUid(uid);
    }
    
    
    @Slave
    @Override
    public Integer countSuccessOrderByUid(Long uid) {
        return baseMapper.countSuccessOrderByUid(uid);
    }
    
    @Slave
    @Override
    public List<ElectricityMemberCardOrder> queryListByOrderIds(List<String> orderIdList) {
        return baseMapper.selectListByOrderIds(orderIdList);
    }
    
    @Override
    @Slave
    public R listSuperAdminPage(MemberCardOrderQuery memberCardOrderQuery) {
        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = baseMapper.selectListSuperAdminPage(memberCardOrderQuery);
        if (CollectionUtils.isEmpty(electricityMemberCardOrderVOList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        Set<Long> uidSet = new HashSet<>();
        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
            uidSet.add(electricityMemberCardOrderVO.getUid());
        }

        List<UserInfo> userInfos = userInfoService.listByUidList(new ArrayList<>(uidSet));
        Map<Long, UserInfo> userInfoMap = new HashMap<>(userInfos.size());
        for (UserInfo userInfo : userInfos) {
            userInfoMap.put(userInfo.getUid(), userInfo);
        }
        
        List<ElectricityMemberCardOrderVO> ElectricityMemberCardOrderVOs = new ArrayList<>();
        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
            // 设置租户名称
            if (Objects.nonNull(electricityMemberCardOrderVO.getTenantId())) {
                Tenant tenant = tenantService.queryByIdFromCache(electricityMemberCardOrderVO.getTenantId());
                electricityMemberCardOrderVO.setTenantName(Objects.nonNull(tenant) ? tenant.getName() : null);
            }
            
            if (Objects.equals(electricityMemberCardOrderVO.getIsBindActivity(), ElectricityMemberCardOrder.BIND_ACTIVITY) && Objects.nonNull(
                    electricityMemberCardOrderVO.getActivityId())) {
                OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCardOrderVO.getActivityId());
                if (Objects.nonNull(oldUserActivity)) {
                    
                    OldUserActivityVO oldUserActivityVO = new OldUserActivityVO();
                    BeanUtils.copyProperties(oldUserActivity, oldUserActivityVO);
                    
                    if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {
                        
                        Coupon coupon = couponService.queryByIdFromCache(oldUserActivity.getCouponId());
                        if (Objects.nonNull(coupon)) {
                            oldUserActivityVO.setCoupon(coupon);
                        }
                        
                    }
                    electricityMemberCardOrderVO.setOldUserActivityVO(oldUserActivityVO);
                }
            }
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getRefId())) {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityMemberCardOrderVO.getRefId().intValue());
                electricityMemberCardOrderVO.setElectricityCabinetName(Objects.nonNull(electricityCabinet) ? electricityCabinet.getName() : "");
            }
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getFranchiseeId())) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(electricityMemberCardOrderVO.getFranchiseeId());
                electricityMemberCardOrderVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            }
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getSendCouponId())) {
                Coupon coupon = couponService.queryByIdFromCache(electricityMemberCardOrderVO.getSendCouponId().intValue());
                electricityMemberCardOrderVO.setSendCouponName(Objects.isNull(coupon) ? "" : coupon.getName());
            }
            
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(electricityMemberCardOrderVO.getMemberCardId());
            electricityMemberCardOrderVO.setRentType(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentType());
            electricityMemberCardOrderVO.setRentUnit(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getRentUnit());
            electricityMemberCardOrderVO.setIsRefund(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getIsRefund());
            electricityMemberCardOrderVO.setLimitCount(Objects.isNull(batteryMemberCard) ? null : batteryMemberCard.getLimitCount());
            
            // 设置优惠券
            List<CouponSearchVo> coupons = new ArrayList<>();
            HashSet<Integer> couponIdsSet = new HashSet<>();
            
            if (Objects.nonNull(electricityMemberCardOrderVO.getSendCouponId())) {
                couponIdsSet.add(Integer.parseInt(electricityMemberCardOrderVO.getSendCouponId().toString()));
            }
            if (StringUtils.isNotBlank(electricityMemberCardOrderVO.getCouponIds())) {
                couponIdsSet.addAll(JsonUtil.fromJsonArray(electricityMemberCardOrderVO.getCouponIds(), Integer.class));
            }
            
            if (!CollectionUtils.isEmpty(couponIdsSet)) {
                couponIdsSet.forEach(couponId -> {
                    CouponSearchVo couponSearchVo = new CouponSearchVo();
                    Coupon coupon = couponService.queryByIdFromCache(couponId);
                    if (Objects.nonNull(coupon)) {
                        BeanUtils.copyProperties(coupon, couponSearchVo);
                        couponSearchVo.setId(coupon.getId().longValue());
                        coupons.add(couponSearchVo);
                    }
                });
            }
            electricityMemberCardOrderVO.setCoupons(coupons);

            // 设置用户信息
            UserInfo userInfo = userInfoMap.get(electricityMemberCardOrderVO.getUid());
            if (Objects.nonNull(userInfo)) {
                electricityMemberCardOrderVO.setUserName(userInfo.getName());
                electricityMemberCardOrderVO.setPhone(userInfo.getPhone());
            }

            ElectricityMemberCardOrderVOs.add(electricityMemberCardOrderVO);
        }
        
        return R.ok(ElectricityMemberCardOrderVOs);
    }
    
    @Override
    @Slave
    public List<ElectricityMemberCardOrder> queryListByCreateTime(Long buyStartTime, Long buyEndTime) {
        return baseMapper.selectListByCreateTime(buyStartTime, buyEndTime);
    }
    
    /**
     * 检测用户是否存在使用中，未使用的套餐
     * @param uid
     * @return
     */
    @Override
    @Slave
    public boolean existNotFinishOrderByUid(Long uid) {
        Integer count = baseMapper.existNotFinishOrderByUid(uid);
        if (Objects.nonNull(count)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public Integer deleteById(Long id) {
        return baseMapper.deleteById(id);
    }
    
    @Override
    public Triple<Boolean, String, ElectricityMemberCardOrder> generateInstallmentMemberCardOrder(UserInfo userInfo, BatteryMemberCard memberCard, ElectricityCabinet cabinet,
            InstallmentRecord installmentRecord, List<InstallmentDeductionPlan> deductionPlans) {
        
        if (Objects.isNull(installmentRecord) || Objects.equals(installmentRecord.getInstallmentNo(), installmentRecord.getPaidInstallment())) {
            return Triple.of(false, "分期订单已代扣完成", null);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        int payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        // 迭代过一次，一期与其他期代扣成功续费套餐的代码逻辑顺序差别较大
        // 生成第一期套餐订单时，代扣计划还未生成，金额与租期需要计算，后面的都从代扣计划中取
        BigDecimal payAmount = BigDecimal.ZERO;
        Integer validDays;
        if (installmentRecord.getPaidInstallment() == 0) {
            // 首期，计算子套餐订单金额，租期
            payAmount = InstallmentUtil.calculateSuborderAmount(1, installmentRecord, memberCard);
            validDays = calculateSuborderValidDays(installmentRecord);
        } else {
            // 非首期，根据代扣计划设置子订单金额与租期
            for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
                payAmount = payAmount.add(deductionPlan.getAmount());
            }
            validDays = deductionPlans.get(0).getRentTime();
        }
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(installmentRecord.getPaidInstallment() > 0 ? ElectricityMemberCardOrder.STATUS_SUCCESS : ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(memberCard.getId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(memberCard.getUseCount());
        electricityMemberCardOrder.setCardName(memberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(validDays);
        electricityMemberCardOrder.setTenantId(memberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(memberCard.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount + 1);
        electricityMemberCardOrder.setSendCouponId(Objects.nonNull(memberCard.getCouponId()) ? memberCard.getCouponId().longValue() : null);
        electricityMemberCardOrder.setRefId(Objects.nonNull(cabinet) ? cabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(Objects.nonNull(cabinet) ? ElectricityMemberCardOrder.SOURCE_SCAN : ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        electricityMemberCardOrder.setStoreId(Objects.nonNull(cabinet) ? cabinet.getStoreId() : userInfo.getStoreId());
        electricityMemberCardOrder.setCouponIds(memberCard.getCouponIds());
        electricityMemberCardOrder.setParamFranchiseeId(null);
        electricityMemberCardOrder.setWechatMerchantId(null);
        electricityMemberCardOrder.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        electricityMemberCardOrder.setIssue(installmentRecord.getPaidInstallment() + 1);
        electricityMemberCardOrder.setPayType(INSTALLMENT_PAYMENT);
        // 分期代扣是固定支付宝
        electricityMemberCardOrder.setPaymentChannel(ChannelEnum.ALIPAY.getCode());
        
        return Triple.of(true, null, electricityMemberCardOrder);
    }
    
    @Override
    public ElectricityMemberCardOrder queryOrderByAgreementNoAndIssue(String externalAgreementNo, Integer issue) {
        return electricityMemberCardOrderMapper.selectOrderByAgreementNoAndIssue(externalAgreementNo, issue);
    }
    
    /**
     * 计算分期套餐子套餐订单有效时间
     */
    private Integer calculateSuborderValidDays(InstallmentRecord installmentRecord) {
        Integer validDays = null;
        if (Objects.equals(installmentRecord.getPaidInstallment(), 0)) {
            // 由于首期订单生成时还没有代扣成功，不设置有效天数，当代扣成功时再设置
            validDays = -1;
        } else {
            InstallmentDeductionPlanQuery query = new InstallmentDeductionPlanQuery();
            query.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
            
            List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(query).getData();
            
            for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
                if (Objects.equals(installmentRecord.getPaidInstallment(), deductionPlan.getIssue())) {
                    validDays = deductionPlan.getRentTime();
                }
            }
        }
        return validDays;
    }

    @Override
    public void updatePayChannelById(ElectricityMemberCardOrder memberCardOrder){
        baseMapper.updatePayChannelById(memberCardOrder);
    }

    @Override
    @Slave
    public ElectricityMemberCardOrder queryUserLastPaySuccessByUid(Long uid) {
        return baseMapper.selectUserLastPaySuccessByUid(uid);
    }

    @Override
    public Integer deactivateUsingOrder(Long uid) {
        Long updateTime = System.currentTimeMillis();
        return electricityMemberCardOrderMapper.deactivateUsingOrder(uid, updateTime);
    }


    @Override
    public List<BatteryModelItem> getBatteryMode(String model) {

        List<Long> memberCardIds = baseMapper.selectMemberCardId(TenantContextHolder.getTenantId());
        if (CollUtil.isEmpty(memberCardIds)) {
            return CollUtil.newArrayList();
        }

        List<MemberCardBatteryType> batteryTypeList = memberCardBatteryTypeService.listByMemberCardIdsAndModel(TenantContextHolder.getTenantId(), memberCardIds, model);
        if (CollUtil.isEmpty(batteryTypeList)) {
            return CollUtil.newArrayList();
        }
        List<String> modelList = batteryTypeList.stream().map(MemberCardBatteryType::getBatteryType).collect(Collectors.toList());
        Map<String, String> map = batteryModelService.listBatteryModelByBatteryTypeList(modelList, TenantContextHolder.getTenantId()).stream().collect(Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getBatteryVShort, (k1, k2) -> k1));

        List<BatteryModelItem> items = modelList.stream().map(s -> BatteryModelItem.builder().key(s).value(map.get(s)).build()).collect(Collectors.toList());
        items.add(0, BatteryModelItem.builder().key(NumberConstant.ONE.toString()).value("标准型号").build());
        return items;
    }
}
