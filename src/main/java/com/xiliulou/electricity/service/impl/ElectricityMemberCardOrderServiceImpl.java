package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Sets;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.dto.UserCouponDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.manager.CalcRentCarPriceFactory;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.producer.ActivityProducer;
import com.xiliulou.electricity.mq.producer.DivisionAccountProducer;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.excel.AutoHeadColumnWidthStyleStrategy;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import jodd.util.ArraysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    TemplateConfigService templateConfigService;
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
    UserBatteryService userBatteryService;
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
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
    private BatteryMemberCardService batteryMemberCardService;

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
        queryWrapper.eq(ElectricityMemberCardOrder::getTenantId, tenantId)
                .eq(ElectricityMemberCardOrder::getUid, uid);
        if (!ObjectUtils.isEmpty(status)) {
            queryWrapper.eq(ElectricityMemberCardOrder::getStatus, status);
        }
        return baseMapper.selectCount(queryWrapper);
    }

    /**
     * 创建月卡订单
     *
     * @param
     * @param electricityMemberCardOrderQuery
     * @return
     */
    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createOrder(ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("CREATE MEMBER_ORDER ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY + user.getUid(), "1", 3 * 1000L, false)) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS UID={}", user.getUid());
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        //用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE MEMBERCARD ERROR! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("rentBattery  ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //判断是否缴纳押金
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.error("CREATE MEMBER_ORDER ERROR! not pay deposit! uid={} ", user.getUid());
            return R.fail("100241", "当前套餐暂停中，请先启用套餐");
        }
    
        UserBattery userBattery = userBatteryService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBattery)) {
            log.error("ELECTRICITY  ERROR! not found userBattery,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
    
        //是否开启购买保险（是进入）
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenInsurance(), ElectricityConfig.ENABLE_INSURANCE)) {
            //保险是否强制购买（是进入）
            FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService
                    .queryByFranchiseeId(userInfo.getFranchiseeId(), null,
                            userInfo.getTenantId());
            long now = System.currentTimeMillis();
            if (Objects.nonNull(franchiseeInsurance) && Objects
                    .equals(franchiseeInsurance.getIsConstraint(), FranchiseeInsurance.CONSTRAINT_FORCE)) {
                //用户是否没有保险信息或已过期（是进入）
                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
                if (Objects.isNull(insuranceUserInfo) || Objects
                        .equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.IS_USE)
                        || insuranceUserInfo.getInsuranceExpireTime() < now) {
                    log.error("CREATE MEMBER_ORDER ERROR! not pay insurance! uid={} ", user.getUid());
                    return R.fail("100309", "未购买保险或保险已过期");
                }
            }
        }

        Long now = System.currentTimeMillis();

        //判断用户电池服务费
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());

        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);

        long cardDays = 0;
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            BigDecimal serviceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
            userChangeServiceFee = serviceFee;
        }

        //判断用户是否产生电池服务费
        if ((Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) || (Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime()))) {

            Long disableMemberCardTime = userBatteryMemberCard.getDisableMemberCardTime();

            cardDays = (now - disableMemberCardTime) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - disableMemberCardTime) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }
            BigDecimal serviceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, null, serviceFeeUserInfo);
            userChangeServiceFee = serviceFee;
        }

        if (BigDecimal.valueOf(0).compareTo(userChangeServiceFee) != 0) {
            log.error("DISABLE MEMBER CARD ERROR! user exist battery service fee ! uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费", userChangeServiceFee);
        }


        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(electricityMemberCardOrderQuery.getMemberId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID={}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID={}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        //判断是否已绑定限次数套餐并且换电次数为负
        ElectricityMemberCard bindElectricityMemberCard = null;
        if (Objects.nonNull(userBatteryMemberCard)) {
            bindElectricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        }

        if (Objects.nonNull(bindElectricityMemberCard) && !Objects.equals(bindElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && Objects.nonNull(userBatteryMemberCard.getRemainingNumber()) && userBatteryMemberCard.getRemainingNumber() < 0) {
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                log.error("payDeposit  ERROR! not buy same memberCard uid={}", user.getUid());
                return R.fail("ELECTRICITY.00119", "套餐剩余次数为负,应购买相同类型套餐抵扣");
            }
        }

        Long franchiseeId = userInfo.getFranchiseeId();
        //购买套餐扫码的柜机
        Long refId = null;
        //购买套餐来源
        Integer source = ElectricityMemberCardOrder.SOURCE_NOT_SCAN;
        if (StringUtils.isNotBlank(electricityMemberCardOrderQuery.getProductKey()) && StringUtils.isNotBlank(electricityMemberCardOrderQuery.getDeviceName())) {

            //换电柜
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(electricityMemberCardOrderQuery.getProductKey(), electricityMemberCardOrderQuery.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.error("BATTERY MEMBER ORDER ERROR!not found electricityCabinet ！p={},d={}", electricityMemberCardOrderQuery.getProductKey(), electricityMemberCardOrderQuery.getDeviceName());
                return R.fail("ELECTRICITY.0005", "未找到换电柜");
            }

            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,eid={},uid={}", electricityCabinet.getId(), userInfo.getUid());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,storeId={},uid={}", electricityCabinet.getStoreId(), userInfo.getUid());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found Franchisee,storeId={},uid={}", store.getId(), userInfo.getUid());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            //换电柜加盟商和用户加盟商一致  则保存套餐来源
            if (Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
                source = ElectricityMemberCardOrder.SOURCE_SCAN;
                refId = electricityCabinet.getId().longValue();
            }
        }

        //查找计算优惠券
        Set<Integer> userCouponIds = generateUserCouponIds(electricityMemberCardOrderQuery.getUserCouponId(), electricityMemberCardOrderQuery.getUserCouponIds());

        //计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = calculatePayAmount(electricityMemberCard.getHolidayPrice(), userCouponIds);
        if(Boolean.FALSE.equals(calculatePayAmountResult.getLeft())){
            return R.fail(calculatePayAmountResult.getMiddle(), (String) calculatePayAmountResult.getRight());
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();

        //支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }

        Long remainingNumber = electricityMemberCard.getMaxUseCount();

        Long oldRemainingNumber = 0L;
        if (Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && userBatteryMemberCard.getMemberCardExpireTime() > now) {
            oldRemainingNumber = userBatteryMemberCard.getRemainingNumber().longValue();
        }

        //同一个套餐可以续费
        if ((Objects.nonNull(userBatteryMemberCard) && (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER))) || (Objects.nonNull(bindElectricityMemberCard) && Objects.equals(bindElectricityMemberCard.getLimitCount(), electricityMemberCard.getLimitCount()))) {
            if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && now < userBatteryMemberCard.getMemberCardExpireTime()) {
                now = userBatteryMemberCard.getMemberCardExpireTime();
            }
            //TODO 使用次数暂时叠加
            if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER) || !Objects.equals(bindElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                remainingNumber = remainingNumber + oldRemainingNumber;
            }
        } else {
            if (Objects.nonNull(bindElectricityMemberCard) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime())
                    && Objects.nonNull(userBatteryMemberCard.getRemainingNumber()) &&
                    userBatteryMemberCard.getMemberCardExpireTime() > now &&
                    (ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, userBatteryMemberCard.getRemainingNumber().longValue()) || userBatteryMemberCard.getRemainingNumber() > 0)) {
                log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS NOT EXPIRED USERINFO={}", userInfo);
                return R.fail("ELECTRICITY.0089", "您的套餐未过期，只能购买相同类型的套餐!");
            }
        }
    
        //获取用户购买套餐次数，，如果为空为零
        Integer payCount = this.queryMaxPayCount(userBatteryMemberCard);
    
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(electricityMemberCardOrderQuery.getMemberId());
        electricityMemberCardOrder.setUid(user.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(franchiseeId);
        electricityMemberCardOrder.setStoreId(userInfo.getStoreId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setRefId(refId);
        electricityMemberCardOrder.setSource(source);
        baseMapper.insert(electricityMemberCardOrder);

        //保存订单所使用的优惠券
        if (CollectionUtils.isNotEmpty(userCouponIds)) {
            memberCardOrderCouponService.batchInsert(buildMemberCardOrderCoupon(electricityMemberCardOrder.getOrderId(), userCouponIds));
        }

        //支付零元
        if (electricityMemberCardOrder.getPayAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {

            //月卡是否绑定活动
            remainingNumber = handlerMembercardBindActivity(electricityMemberCard, userBatteryMemberCard, userInfo, remainingNumber);

            //用户套餐
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            Long memberCardExpireTime = now + electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
            userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
            userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber);
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId().longValue());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setTenantId(userInfo.getTenantId());
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCardPayCount(payCount + 1);
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);

            ServiceFeeUserInfo serviceFeeUserInfoInsertOrUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoInsertOrUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
            serviceFeeUserInfoInsertOrUpdate.setUid(userBatteryMemberCardUpdate.getUid());
            serviceFeeUserInfoInsertOrUpdate.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
            serviceFeeUserInfoInsertOrUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsertOrUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
            if (Objects.isNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoInsertOrUpdate.setCreateTime(System.currentTimeMillis());
                serviceFeeUserInfoInsertOrUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
                serviceFeeUserInfoInsertOrUpdate.setDisableMemberCardNo("");
//                serviceFeeUserInfoInsertOrUpdate.setExistBatteryServiceFee(ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE);
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsertOrUpdate);
            } else {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsertOrUpdate);
            }


            //月卡订单
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityMemberCardOrderUpdate.setPayCount(payCount + 1);
            baseMapper.updateById(electricityMemberCardOrderUpdate);

            //修改优惠券状态为已使用
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                userCouponService.batchUpdateUserCoupon(buildUserCouponList(userCouponIds, UserCoupon.STATUS_USED, electricityMemberCardOrder.getOrderId()));
            }

            //被邀请新买月卡用户
            //是否是新用户
            if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() == 0) {
                //是否有人邀请
                JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(user.getUid());
                if (Objects.nonNull(joinShareActivityRecord)) {
                    //是否购买的是活动指定的套餐
                    List<Long> memberCardIds = shareActivityMemberCardService.selectMemberCardIdsByActivityId(joinShareActivityRecord.getActivityId());
                    if (CollectionUtils.isNotEmpty(memberCardIds) && memberCardIds.contains(electricityMemberCardOrder.getMemberCardId().longValue())) {
                        //修改邀请状态
                        joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                        joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                        joinShareActivityRecordService.update(joinShareActivityRecord);

                        //修改历史记录状态
                        JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndJoinUid(joinShareActivityRecord.getId(), user.getUid());
                        if (Objects.nonNull(oldJoinShareActivityHistory)) {
                            oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_SUCCESS);
                            oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                            joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
                        }

                        //给邀请人增加邀请成功人数
                        shareActivityRecordService.addCountByUid(joinShareActivityRecord.getUid(), joinShareActivityRecord.getActivityId());
                    } else {
                        log.info("SHARE ACTIVITY INFO!invite fail,activityId={},membercardId={},memberCardIds={}", joinShareActivityRecord.getActivityId(), electricityMemberCardOrder.getMemberCardId(), JsonUtil.toJson(memberCardIds));
                    }
                }

                //是否有人返现邀请
                JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(user.getUid());
                if (Objects.nonNull(joinShareMoneyActivityRecord)) {
                    //修改邀请状态
                    joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                    joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);

                    //修改历史记录状态
                    JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndJoinUid(joinShareMoneyActivityRecord.getId(), user.getUid());
                    if (Objects.nonNull(oldJoinShareMoneyActivityHistory)) {
                        oldJoinShareMoneyActivityHistory.setStatus(JoinShareMoneyActivityHistory.STATUS_SUCCESS);
                        oldJoinShareMoneyActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareMoneyActivityHistoryService.update(oldJoinShareMoneyActivityHistory);
                    }

                    ShareMoneyActivity shareMoneyActivity = shareMoneyActivityService.queryByIdFromCache(joinShareMoneyActivityRecord.getActivityId());

                    if (Objects.nonNull(shareMoneyActivity)) {
                        //给邀请人增加邀请成功人数
                        shareMoneyActivityRecordService.addCountByUid(joinShareMoneyActivityRecord.getUid(), shareMoneyActivity.getMoney());
                    }

                    //返现
                    userAmountService.handleAmount(joinShareMoneyActivityRecord.getUid(), joinShareMoneyActivityRecord.getJoinUid(), shareMoneyActivity.getMoney(), electricityMemberCardOrder.getTenantId());
                }
            }

            //处理拉新返现活动
            invitationActivityRecordService.handleInvitationActivity(userInfo, electricityMemberCardOrder.getOrderId());

            //套餐分帐
            divisionAccountRecordService.handleBatteryMembercardDivisionAccount(electricityMemberCardOrder);
    
            //如果后台有记录那么一定是用户没购买过套餐时添加，如果为INIT就修改
            ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(user.getUid());
            if (Objects.nonNull(channelActivityHistory) && Objects
                    .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
                ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
                updateChannelActivityHistory.setId(channelActivityHistory.getId());
                updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
                updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
                channelActivityHistoryService.update(updateChannelActivityHistory);
            }
    
            return R.ok();
        }

        //修改优惠券状态为正在核销中
        if (CollectionUtils.isNotEmpty(userCouponIds)) {
            userCouponService.batchUpdateUserCoupon(buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, electricityMemberCardOrder.getOrderId()));
        }

        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(electricityMemberCardOrder.getOrderId())
                    .uid(user.getUid())
                    .payAmount(electricityMemberCardOrder.getPayAmount())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_MEMBER_CARD)
                    .attach(String.valueOf(electricityMemberCardOrderQuery.getUserCouponId()))
                    .description("月卡收费")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("CREATE MEMBER_ORDER ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return R.fail("ELECTRICITY.0099", "下单失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> buyBatteryMemberCard(ElectricityMemberCardOrderQuery query, HttpServletRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BATTERY MEMBER ORDER ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        try {

            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.error("BATTERY MEMBER ORDER ERROR! not found user,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
            }

            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("BATTERY MEMBER ORDER WARN! user is unUsable,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }

            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("BATTERY MEMBER ORDER WARN! user not auth,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
            }

            if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
                log.warn("BATTERY MEMBER ORDER WARN! not pay deposit,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
            }

            Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                log.warn("BATTERY MEMBER ORDER WARN! not found franchisee,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
            }

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getMemberId());
            if (Objects.isNull(batteryMemberCard) || Objects.equals(batteryMemberCard.getStatus(), BatteryMemberCard.STATUS_DOWN)) {
                log.warn("BATTERY MEMBER ORDER WARN! not found batteryMemberCard,uid={},mid={}", user.getUid(), query.getMemberId());
                return Triple.of(false, "ELECTRICITY.0087", "套餐不存在");
            }

            Boolean isFirstBuyMemberCard = Boolean.FALSE;
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard) || Objects.equals(NumberConstant.ZERO, userBatteryMemberCard.getCardPayCount())) {
                isFirstBuyMemberCard = Boolean.TRUE;
            }

            Triple<Boolean, String, Object> verifyResult;
            if (Boolean.TRUE.equals(isFirstBuyMemberCard)) {
                verifyResult= handlerFirstBuyBatteryMemberCard(query,userBatteryMemberCard, batteryMemberCard, userInfo,franchisee);
            } else {
                verifyResult= handlerNonFirstBuyBatteryMemberCard(query,userBatteryMemberCard, batteryMemberCard, userInfo,franchisee);
            }

            if(Boolean.FALSE.equals(verifyResult.getLeft())){
                return verifyResult;
            }

            Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("BATTERY MEMBER ORDER WARN! user exist battery service fee,uid={},mid={}", user.getUid(), query.getMemberId());
                return Triple.of(false,"ELECTRICITY.100000", "存在电池服务费");
            }

            Triple<Boolean, String, Object> verifyUserBatteryInsuranceResult = verifyUserBatteryInsurance(userInfo, franchisee,batteryMemberCard);
            if (Boolean.FALSE.equals(verifyUserBatteryInsuranceResult.getLeft())) {
                return verifyUserBatteryInsuranceResult;
            }

            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
            if (Objects.isNull(electricityPayParams)) {
                log.error("BATTERY MEMBER ORDER ERROR!not found pay params,uid={}", user.getUid());
                return Triple.of(false, "", "未配置支付参数!");
            }

            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found userOauthBind,uid={}", user.getUid());
                return Triple.of(false, "", "未找到用户的第三方授权信息!");
            }

            //兼容旧版小程序
            Set<Integer> userCouponIds = generateUserCouponIds(query.getUserCouponId(), query.getUserCouponIds());

            //计算优惠后支付金额
            Triple<Boolean, String, Object> calculatePayAmountResult = calculatePayAmount(batteryMemberCard.getRentPrice(), userCouponIds);
            if (Boolean.FALSE.equals(calculatePayAmountResult.getLeft())) {
                return calculatePayAmountResult;
            }

            BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();

            ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
            memberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
            memberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
            memberCardOrder.setMemberCardId(batteryMemberCard.getId());
            memberCardOrder.setUid(userInfo.getUid());
            memberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
            memberCardOrder.setCardName(batteryMemberCard.getName());
            memberCardOrder.setPayAmount(payAmount);
            memberCardOrder.setUserName(userInfo.getName());
            memberCardOrder.setValidDays(batteryMemberCard.getValidDays());
            memberCardOrder.setTenantId(TenantContextHolder.getTenantId());
            memberCardOrder.setFranchiseeId(franchisee.getId());
            memberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
            memberCardOrder.setCreateTime(System.currentTimeMillis());
            memberCardOrder.setUpdateTime(System.currentTimeMillis());
            memberCardOrder.setSendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null);

            Triple<Boolean, String, Object> assignOrderSourceResult = assignOrderSource(query, memberCardOrder);
            if (Boolean.FALSE.equals(assignOrderSourceResult.getLeft())) {
                return assignOrderSourceResult;
            }

            baseMapper.insert(memberCardOrder);

            //保存订单所使用的优惠券
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                memberCardOrderCouponService.batchInsert(buildMemberCardOrderCoupon(memberCardOrder.getOrderId(), userCouponIds));
            }

            //支付0元
            if (memberCardOrder.getPayAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {
                handlerBatteryMembercardZeroPayment(batteryMemberCard, memberCardOrder, userBatteryMemberCard, userInfo);
                return Triple.of(true, null, null);
            }

            //修改优惠券状态为正在核销中
            if (CollectionUtils.isNotEmpty(userCouponIds)) {
                userCouponService.batchUpdateUserCoupon(buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, memberCardOrder.getOrderId()));
            }

            try {
                CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                        .orderId(memberCardOrder.getOrderId())
                        .uid(userInfo.getUid())
                        .payAmount(memberCardOrder.getPayAmount())
                        .orderType(ElectricityTradeOrder.ORDER_TYPE_MEMBER_CARD)
                        .attach(String.valueOf(query.getUserCouponId()))
                        .description("换电套餐订单收费")
                        .tenantId(tenantId).build();

                WechatJsapiOrderResultDTO resultDTO = electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
                return Triple.of(true, null, resultDTO);
            } catch (WechatPayException e) {
                log.error("BATTERY MEMBER ORDER ERROR! wechat v3 order error,uid={}", user.getUid(), e);
            }
        } finally {
            redisService.delete(CacheConstant.ELE_CACHE_USER_BATTERY_MEMBER_CARD_LOCK_KEY + user.getUid());
        }

        return Triple.of(false, "ELECTRICITY.0099", "下单失败");
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
            vo.setLimitCount(batteryMemberCard.getLimitCount());
            vo.setRentType(batteryMemberCard.getRentType());
            vo.setRentUnit(batteryMemberCard.getRentUnit());
            vo.setValidDays(batteryMemberCard.getValidDays());
            vo.setUseCount(batteryMemberCard.getUseCount());
            vo.setIsRefund(batteryMemberCard.getIsRefund());
            vo.setSimpleBatteryType(acquireBatteryMembercardOrderSimpleBatteryType(memberCardBatteryTypeService.selectBatteryTypeByMid(item.getMemberCardId())));

            return vo;
        }).collect(Collectors.toList());

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
        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = baseMapper.queryList(memberCardOrderQuery);
        if (CollectionUtils.isEmpty(electricityMemberCardOrderVOList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        List<ElectricityMemberCardOrderVO> ElectricityMemberCardOrderVOs = new ArrayList<>();
        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {

            if (Objects.equals(electricityMemberCardOrderVO.getIsBindActivity(), ElectricityMemberCardOrder.BIND_ACTIVITY) && Objects.nonNull(electricityMemberCardOrderVO.getActivityId())) {
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

            ElectricityMemberCardOrderVOs.add(electricityMemberCardOrderVO);
        }

        return R.ok(ElectricityMemberCardOrderVOs);
    }

    @Slave
    @Override
    public void exportExcel(MemberCardOrderQuery memberCardOrderQuery, HttpServletResponse response) {

        List<ElectricityMemberCardOrderVO> electricityMemberCardOrders = Lists.newArrayList();
        Long offset = 0L;
        while (true) {
            memberCardOrderQuery.setOffset(offset);
            memberCardOrderQuery.setSize(EXPORT_LIMIT);
            List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = baseMapper.queryList(memberCardOrderQuery);
            offset += EXPORT_LIMIT;

            if (CollectionUtils.isEmpty(electricityMemberCardOrderVOList)) {
                break;
            }

            electricityMemberCardOrders.addAll(electricityMemberCardOrderVOList);
        }

        if (ObjectUtil.isEmpty(electricityMemberCardOrders)) {
            throw new CustomBusinessException("订单不存在！");
        }

        List<ElectricityMemberCardOrderExcelVO> electricityMemberCardOrderExcelVOS = new ArrayList();
        for (int i = 0; i < electricityMemberCardOrders.size(); i++) {
            ElectricityMemberCardOrderExcelVO excelVo = new ElectricityMemberCardOrderExcelVO();
            excelVo.setId(i + 1);
            excelVo.setOrderId(electricityMemberCardOrders.get(i).getOrderId());
            excelVo.setName(electricityMemberCardOrders.get(i).getUserName());
            excelVo.setPhone(electricityMemberCardOrders.get(i).getPhone());

            if (Objects.nonNull(electricityMemberCardOrders.get(i).getFranchiseeId())) {
                Franchisee franchisee = franchiseeService
                        .queryByIdFromCache(electricityMemberCardOrders.get(i).getFranchiseeId());
                excelVo.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");
            }

            excelVo.setFranchiseeName(electricityMemberCardOrders.get(i).getFranchiseeName());
            excelVo.setMemberCardName(electricityMemberCardOrders.get(i).getCardName());
            excelVo.setMaxUseCount(Objects.equals(electricityMemberCardOrders.get(i).getMaxUseCount(), -1L) ? "不限次" : String.valueOf(electricityMemberCardOrders.get(i).getMaxUseCount()));
//TODO            excelVo.setValidDays(electricityMemberCardOrders.get(i).getValidDays());
            excelVo.setStatus(Objects.equals(electricityMemberCardOrders.get(i).getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS) ? "已支付" : "未支付");
            excelVo.setPayAmount(electricityMemberCardOrders.get(i).getPayAmount());
            excelVo.setPayType(Objects.equals(electricityMemberCardOrders.get(i).getPayType(), ElectricityMemberCardOrder.ONLINE_PAYMENT) ? "线上支付" : "线下支付");
            excelVo.setBeginningTime(DateUtil.format(DateUtil.date(electricityMemberCardOrders.get(i).getCreateTime()), DatePattern.NORM_DATETIME_PATTERN));
            excelVo.setPayCount(electricityMemberCardOrders.get(i).getPayCount());
            
            electricityMemberCardOrderExcelVOS.add(excelVo);
        }

        String fileName = "套餐订单报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ElectricityMemberCardOrderExcelVO.class).registerWriteHandler(new AutoHeadColumnWidthStyleStrategy()).sheet("sheet").doWrite(electricityMemberCardOrderExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }

    @Slave
    @Override
    public R queryCount(MemberCardOrderQuery memberCardOrderQuery) {
        return R.ok(baseMapper.queryCount(memberCardOrderQuery));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R openOrDisableMemberCard(Integer usableStatus) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DISABLE_MEMBER_CARD_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁,请稍后再试!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("DISABLE MEMBER CARD ERROR! user is rent deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("DISABLE MEMBER CARD ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("DISABLE MEMBER CARD ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("DISABLE MEMBER CARD ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        if (Boolean.TRUE.equals(userBatteryMemberCardService.verifyUserBatteryMembercardEffective(batteryMemberCard,userBatteryMemberCard))) {
            log.error("DISABLE MEMBER CARD ERROR! userBatteryMemberCard expire,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("100298","换电套餐已过期，无法进行暂停操作");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if(CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)){
            log.error("DISABLE BATTERY MEMBERCARD ERROR! battery membercard refund review,uid={}", userInfo.getUid());
            return R.fail("100018", "套餐租金退款审核中");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("DISABLE MEMBER CARD ERROR! disable review userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(user.getUid());
        if(Objects.isNull(serviceFeeUserInfo)){
            log.warn("BATTERY SERVICE FEE WARN! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return R.fail("100247","用户信息不存在");
        }

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.error("DISABLE MEMBER CARD ERROR! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费");
        }

        Long memberCardExpireTime = null;

        if (Objects.equals(usableStatus, EleDisableMemberCardRecord.MEMBER_CARD_DISABLE)) {
            usableStatus = EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW;
            EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                    .disableMemberCardNo(generateOrderId(user.getUid()))
                    .memberCardName(batteryMemberCard.getName())
                    .phone(userInfo.getPhone())
                    .userName(userInfo.getName())
                    .status(usableStatus)
                    .tenantId(userInfo.getTenantId())
                    .uid(user.getUid())
                    .cardDays(userBatteryMemberCardService.transforRemainingTime(userBatteryMemberCard,batteryMemberCard))
                    .storeId(userInfo.getStoreId())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .batteryMemberCardId(userBatteryMemberCard.getMemberCardId())
                    .disableCardTimeType(EleDisableMemberCardRecord.DISABLE_CARD_NOT_LIMIT_TIME)
                    .chargeRate(batteryMemberCard.getServiceCharge())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

            ServiceFeeUserInfo serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder()
                    .uid(user.getUid())
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .updateTime(System.currentTimeMillis()).build();

            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

            sendDisableMemberCardMessage(userInfo);
        } else {

            int disableCardDays=(int) Math.ceil((System.currentTimeMillis() - (userBatteryMemberCard.getDisableMemberCardTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);

            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(), user.getTenantId());
            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(batteryMemberCard.getName())
                    .enableTime(System.currentTimeMillis())
                    .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .disableDays(disableCardDays)
                    .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .storeId(userInfo.getStoreId())
                    .phone(user.getPhone())
                    .createTime(System.currentTimeMillis())
                    .tenantId(user.getTenantId())
                    .uid(user.getUid())
                    .userName(userInfo.getName())
                    .updateTime(System.currentTimeMillis()).build();
            enableMemberCardRecordService.insert(enableMemberCardRecord);

            memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
            ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoUpdate.setUid(serviceFeeUserInfo.getUid());
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
            serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoUpdate.setTenantId(serviceFeeUserInfo.getTenantId());
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardStatus(usableStatus);
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);
        }
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R disableMemberCardForLimitTime(Integer disableCardDays, Long disableDeadline,String applyReason) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
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

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("DISABLE MEMBER CARD ERROR! user is rent deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("DISABLE MEMBER CARD ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if(CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)){
            log.error("PAUSE BATTERY MEMBERCARD ERROR! battery membercard refund review ,uid={}", userInfo.getUid());
            return R.fail("100018", "套餐租金退款审核中");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("DISABLE MEMBER CARD ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("DISABLE MEMBER CARD ERROR! memberCard is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        if (Boolean.TRUE.equals(userBatteryMemberCardService.verifyUserBatteryMembercardEffective(batteryMemberCard,userBatteryMemberCard))) {
            log.error("DISABLE MEMBER CARD ERROR! userBatteryMemberCard expire,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("100298","换电套餐已过期，无法进行暂停操作");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if(Objects.isNull(serviceFeeUserInfo)){
            log.warn("BATTERY SERVICE FEE WARN! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("DISABLE MEMBER CARD ERROR! disable review userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.error("DISABLE MEMBER CARD ERROR! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费", acquireUserBatteryServiceFeeResult.getRight());
        }

        EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                .disableMemberCardNo(generateOrderId(user.getUid()))
                .memberCardName(batteryMemberCard.getName())
                .phone(userInfo.getPhone())
                .userName(userInfo.getName())
                .status(EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW)
                .uid(userInfo.getUid())
                .tenantId(userInfo.getTenantId())
                .uid(user.getUid())
                .franchiseeId(userInfo.getFranchiseeId())
                .storeId(userInfo.getStoreId())
                .batteryMemberCardId(userBatteryMemberCard.getMemberCardId())
                .chooseDays(disableCardDays)
                .cardDays(userBatteryMemberCardService.transforRemainingTime(userBatteryMemberCard,batteryMemberCard))
                .disableDeadline(disableDeadline)
                .disableCardTimeType(EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)
                .chargeRate(batteryMemberCard.getServiceCharge())
                .applyReason(applyReason)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

        ServiceFeeUserInfo insertOrUpdateServiceFeeUserInfo = ServiceFeeUserInfo.builder()
                .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .uid(user.getUid())
                .updateTime(System.currentTimeMillis()).build();

        serviceFeeUserInfoService.updateByUid(insertOrUpdateServiceFeeUserInfo);

        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        return R.ok();
    }

    @Override
    public R enableMemberCardForLimitTime() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
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

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ENABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("ENABLE MEMBER CARD ERROR! user is rent deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("ENABLE MEMBER CARD ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("ENABLE MEMBER CARD ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("ENABLE MEMBER CARD ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("ENABLE MEMBER CARD ERROR! disable review userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.error("ENABLE MEMBER CARD ERROR! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.error("ENABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("100247", "用户信息不存在");
        }

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.error("DISABLE MEMBER CARD ERROR! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费");
        }

        int disableCardDays=(int) Math.ceil((System.currentTimeMillis() - (userBatteryMemberCard.getDisableMemberCardTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);

        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(), user.getTenantId());

        Integer serviceFeeStatus = EnableMemberCardRecord.STATUS_INIT;

        EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .memberCardName(batteryMemberCard.getName())
                .enableTime(System.currentTimeMillis())
                .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                .batteryServiceFeeStatus(serviceFeeStatus)
                .disableDays(disableCardDays)
                .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                .franchiseeId(userInfo.getFranchiseeId())
                .storeId(userInfo.getStoreId())
                .phone(user.getPhone())
                .createTime(System.currentTimeMillis())
                .tenantId(user.getTenantId())
                .uid(user.getUid())
                .userName(userInfo.getName())
                .updateTime(System.currentTimeMillis()).build();
        enableMemberCardRecordService.insert(enableMemberCardRecord);

        UserBatteryMemberCard userBatteryMemberCardUdpate = new UserBatteryMemberCard();
        Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
        userBatteryMemberCardUdpate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUdpate.setOrderExpireTime(System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
        userBatteryMemberCardUdpate.setMemberCardExpireTime(memberCardExpireTime);
        userBatteryMemberCardUdpate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUdpate.setUpdateTime(System.currentTimeMillis());

        ServiceFeeUserInfo serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder()
                .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .serviceFeeGenerateTime(memberCardExpireTime)
                .franchiseeId(userInfo.getFranchiseeId())
                .tenantId(eleDisableMemberCardRecord.getTenantId())
                .uid(user.getUid())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();


        if (Boolean.FALSE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            userBatteryMemberCardUdpate.setDisableMemberCardTime(null);
            serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
        }

        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

        userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUdpate);

        return R.ok();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
            log.error("ELE ERROR! not found user,uid={} ", userInfo.getUid());
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
    
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                .selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("ENABLE MEMBER CARD ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("ENABLE MEMBER CARD ERROR! memberCard  is not exit,uid={},memberCardId={}", userInfo.getUid(),
                    userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }
        
        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService
                .queryByDisableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo(),
                        TenantContextHolder.getTenantId());
        if (Objects.isNull(eleDisableMemberCardRecord) || !Objects.equals(eleDisableMemberCardRecord.getStatus(),
                EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW)) {
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
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
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
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(), user.getTenantId());
            return R.ok(eleDisableMemberCardRecord.getDisableCardTimeType());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R adminDisableMemberCard(Long uid, Integer days) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin saveUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin saveUserMemberCard  ERROR! not found user! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("admin saveUserMemberCard  ERROR! user is rent deposit,uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("admin saveUserMemberCard  ERROR! not pay deposit,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("admin saveUserMemberCard  ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("admin saveUserMemberCard  ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        if (Boolean.TRUE.equals(userBatteryMemberCardService.verifyUserBatteryMembercardEffective(batteryMemberCard,userBatteryMemberCard))) {
            log.error("PAUSE BATTERY MEMBERCARD ERROR! userBatteryMemberCard expire,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("100298","换电套餐已过期，无法进行暂停操作");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        //是否有正在进行的退租
        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if(CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)){
            log.error("PAUSE BATTERY MEMBERCARD ERROR! battery membercard refund review userId={}", userInfo.getUid());
            return R.fail("100018", "套餐租金退款审核中");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("PAUSE BATTERY MEMBERCARD ERROR! disable review userId={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("PAUSE BATTERY MEMBERCARD ERROR!not found franchisee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.error("PAUSE BATTERY MEMBERCARD ERROR! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.error("PAUSE BATTERY MEMBERCARD ERROR! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费");
        }

        EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                .disableMemberCardNo(generateOrderId(uid))
                .memberCardName(batteryMemberCard.getName())
                .batteryMemberCardId(userBatteryMemberCard.getMemberCardId())
                .phone(userInfo.getPhone())
                .userName(userInfo.getName())
                .status(UserBatteryMemberCard.MEMBER_CARD_DISABLE)
                .tenantId(userInfo.getTenantId())
                .uid(uid)
                .franchiseeId(userInfo.getFranchiseeId())
                .storeId(userInfo.getStoreId())
                .chargeRate(batteryMemberCard.getServiceCharge())
                .chooseDays(days)
                .disableCardTimeType(EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)
                .cardDays(userBatteryMemberCardService.transforRemainingTime(userBatteryMemberCard,batteryMemberCard))
                .disableMemberCardTime(System.currentTimeMillis())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

        //更新用户套餐状态为暂停
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE);
        userBatteryMemberCardUpdate.setDisableMemberCardTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(uid);
        serviceFeeUserInfoUpdate.setDisableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());

        //3.0新需求  暂停套餐生成滞纳金订单
        if(Objects.equals(userInfo.getBatteryRentStatus(),UserInfo.BATTERY_RENT_STATUS_YES)){
            //获取用户绑定的电池
            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
            //用户绑定的电池型号
            List<String> userBatteryType = userBatteryTypeService.selectByUid(userInfo.getUid());

            EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = EleBatteryServiceFeeOrder.builder()
                    .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_STAGNATE, userInfo.getUid()))
                    .uid(userInfo.getUid())
                    .phone(userInfo.getPhone())
                    .name(userInfo.getName())
                    .payAmount(BigDecimal.ZERO)
                    .status(EleDepositOrder.STATUS_INIT)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .batteryServiceFeeGenerateTime(System.currentTimeMillis())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .storeId(userInfo.getStoreId())
                    .tenantId(userInfo.getTenantId())
                    .source(EleBatteryServiceFeeOrder.DISABLE_MEMBER_CARD)
                    .modelType(franchisee.getModelType())
                    .batteryType(CollectionUtils.isEmpty(userBatteryType) ? "" : JsonUtil.toJson(userBatteryType))
                    .sn(Objects.isNull(electricityBattery) ? "" : electricityBattery.getSn())
                    .batteryServiceFee(batteryMemberCard.getServiceCharge()).build();
            eleBatteryServiceFeeOrderService.insert(eleBatteryServiceFeeOrder);

            serviceFeeUserInfoUpdate.setPauseOrderNo(eleBatteryServiceFeeOrder.getOrderId());
        }

        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_DISABLE)
                .operateUid(user.getUid())
                .uid(uid)
                .name(user.getUsername())
                .memberCardDisableStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
/*
        //启用月卡时判断用户是否有电池，收取服务费
        if (Objects.equals(usableStatus, UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(), user.getTenantId());
            if(Objects.isNull(eleDisableMemberCardRecord)){
                return R.fail("100370","停卡记录不存在");
            }

            int disableCardDays=(int) Math.ceil((System.currentTimeMillis() - (userBatteryMemberCard.getDisableMemberCardTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);

            EleDisableMemberCardRecord updateDisableMemberCardRecord = new EleDisableMemberCardRecord();
            updateDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
            updateDisableMemberCardRecord.setRealDays(disableCardDays);
            updateDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
            eleDisableMemberCardRecordService.updateBYId(updateDisableMemberCardRecord);

            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(batteryMemberCard.getName())
                    .enableTime(System.currentTimeMillis())
                    .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT)
                    .disableDays(disableCardDays)
                    .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .phone(userInfo.getPhone())
                    .createTime(System.currentTimeMillis())
                    .tenantId(user.getTenantId())
                    .uid(uid)
                    .userName(userInfo.getName())
                    .updateTime(System.currentTimeMillis()).build();
            enableMemberCardRecordService.insert(enableMemberCardRecord);

            ServiceFeeUserInfo serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime())
                    .tenantId(eleDisableMemberCardRecord.getTenantId())
                    .uid(uid)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();

            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        } else {
            EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                    .disableMemberCardNo(generateOrderId(uid))
                    .memberCardName(batteryMemberCard.getName())
                    .phone(userInfo.getPhone())
                    .userName(userInfo.getName())
                    .status(usableStatus)
                    .tenantId(userInfo.getTenantId())
                    .uid(uid)
                    .franchiseeId(userInfo.getFranchiseeId())
                    .storeId(userInfo.getStoreId())
                    .chargeRate(batteryMemberCard.getServiceCharge())
                    .chooseDays(days)
                    .cardDays((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();

            eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

            ServiceFeeUserInfo serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime())
                    .tenantId(eleDisableMemberCardRecord.getTenantId())
                    .uid(uid)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();

            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        }

        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        if (Objects.equals(usableStatus, UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
        } else {
            userBatteryMemberCardUpdate.setDisableMemberCardTime(System.currentTimeMillis());
        }
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setMemberCardStatus(usableStatus);
        userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);

        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_DISABLE)
                .operateUid(user.getUid())
                .uid(uid)
                .name(user.getUsername())
                .memberCardDisableStatus(usableStatus)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
*/
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R adminEnableMemberCard(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ADMIN ENABLE BATTERY MEMBERCARD ERROR! not found userInfo! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("ADMIN ENABLE BATTERY MEMBERCARD ERROR! user is rent deposit,uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.error("ADMIN ENABLE BATTERY MEMBERCARD ERROR! not found serviceFeeUserInfo,uid={}", userInfo.getUid());
            return R.fail("100247", "用户信息不存在");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId())) {
            log.warn("ADMIN ENABLE BATTERY MEMBERCARD ERROR! user haven't memberCard uid={}", userInfo.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("ADMIN ENABLE BATTERY MEMBERCARD ERROR! battery memberCard is not exit,uid={},memberCardId={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.error("ADMIN ENABLE BATTERY MEMBERCARD ERROR! user exist battery service fee,uid={}", userInfo.getUid());
            return R.fail("ELECTRICITY.100000", "存在电池服务费");
        }

        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryByDisableMemberCardNo(serviceFeeUserInfo.getDisableMemberCardNo(), userInfo.getTenantId());
        if(Objects.isNull(eleDisableMemberCardRecord)){
            log.error("ADMIN ENABLE BATTERY MEMBERCARD ERROR! not found eleDisableMemberCardRecord,uid={},disableMemberCardNo={}", userInfo.getUid(), serviceFeeUserInfo.getDisableMemberCardNo());
            return R.fail("100370","停卡记录不存在");
        }

        int realDisableCardDays=(int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);

        EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                .uid(uid)
                .userName(userInfo.getName())
                .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .memberCardName(batteryMemberCard.getName())
                .enableTime(System.currentTimeMillis())
                .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT)
                .disableDays(realDisableCardDays)
                .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                .phone(userInfo.getPhone())
                .createTime(System.currentTimeMillis())
                .serviceFee(BigDecimal.ZERO)
                .storeId(userInfo.getStoreId())
                .franchiseeId(userInfo.getFranchiseeId())
                .tenantId(userInfo.getTenantId())
                .updateTime(System.currentTimeMillis()).build();
        enableMemberCardRecordService.insert(enableMemberCardRecord);

        //更新用户套餐过期时间
        UserBatteryMemberCard userBatteryMemberCardUpdate =  new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime()+(System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()));
        userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime()+(System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()));
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

         //更新用户服务费产生时间 解绑用户停卡单号和滞纳金单号
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = ServiceFeeUserInfo.builder()
                .disableMemberCardNo("")
                .pauseOrderNo("")
                .expireOrderNo("")
                .franchiseeId(userInfo.getFranchiseeId())
                .serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime())
                .tenantId(eleDisableMemberCardRecord.getTenantId())
                .uid(uid)
                .updateTime(System.currentTimeMillis()).build();

        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_DISABLE)
                .operateUid(SecurityUtils.getUid())
                .uid(uid)
                .name(Objects.isNull(SecurityUtils.getUserInfo()) ? null : SecurityUtils.getUserInfo().getUsername())
                .memberCardDisableStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R cleanBatteryServiceFee(Long uid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin saveUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)|| !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin saveUserMemberCard  ERROR! not found user! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("admin saveUserMemberCard  ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if(Objects.isNull(serviceFeeUserInfo)){
            log.warn("admin clean service fee ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100247", "用户信息不存在");
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.error("admin saveUserMemberCard  ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        //套餐过期时间
        Long memberCardExpireTime=userBatteryMemberCard.getMemberCardExpireTime();
        //当前套餐过期时间
        Long orderExpireTime=userBatteryMemberCard.getOrderExpireTime();
        //电池服务费产生时间
        Long serviceFeeGenerateTime=userBatteryMemberCard.getMemberCardExpireTime();
        //套餐过期滞纳金
        BigDecimal expireBatteryServiceFee = BigDecimal.ZERO;
        //暂停套餐滞纳金
        BigDecimal pauseBatteryServiceFee = BigDecimal.ZERO;

        //1.如果是套餐过期
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            memberCardExpireTime = System.currentTimeMillis();
            orderExpireTime = System.currentTimeMillis();
            serviceFeeGenerateTime = System.currentTimeMillis();

            int batteryMemebercardExpireDays = (int) Math.ceil((System.currentTimeMillis() - (serviceFeeUserInfo.getServiceFeeGenerateTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);
            expireBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMemebercardExpireDays));
            log.info("ADMIN CLEAN BATTERY SERVICE FEE INFO!user exist expire fee,uid={},fee={}", userInfo.getUid(), expireBatteryServiceFee.doubleValue());
        }

        //2.如果套餐暂停，则提前启用
        if(Objects.equals(userBatteryMemberCard.getMemberCardStatus(),UserBatteryMemberCard.MEMBER_CARD_DISABLE)){
            memberCardExpireTime=System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
            orderExpireTime=System.currentTimeMillis() + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
            serviceFeeGenerateTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());


            int batteryMembercardDisableDays = (int) Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000.0 / 60 / 60 / 24);
            pauseBatteryServiceFee = batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(batteryMembercardDisableDays));
            log.info("ADMIN CLEAN BATTERY SERVICE FEE INFO!user exist pause fee,uid={},fee={}", userInfo.getUid(), pauseBatteryServiceFee.doubleValue());


            //更新停卡记录
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(), user.getTenantId());
            if(Objects.isNull(eleDisableMemberCardRecord)){
                return R.fail("100370","停卡记录不存在");
            }

            //生成启用记录
            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(batteryMemberCard.getName())
                    .enableTime(System.currentTimeMillis())
                    .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_CLEAN)
                    .serviceFee(pauseBatteryServiceFee)
                    .disableDays(batteryMembercardDisableDays)
                    .disableTime(userBatteryMemberCard.getDisableMemberCardTime())
                    .enableTime(System.currentTimeMillis())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .storeId(userInfo.getStoreId())
                    .phone(userInfo.getPhone())
                    .createTime(System.currentTimeMillis())
                    .tenantId(user.getTenantId())
                    .uid(uid)
                    .userName(userInfo.getName())
                    .updateTime(System.currentTimeMillis()).build();
            enableMemberCardRecordService.insert(enableMemberCardRecord);
        }

        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
        userBatteryMemberCardUpdate.setOrderExpireTime(orderExpireTime);
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(serviceFeeGenerateTime);
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
        serviceFeeUserInfoUpdate.setExpireOrderNo("");
        serviceFeeUserInfoUpdate.setPauseOrderNo("");
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

        //更新滞纳金订单状态
        if (StringUtils.isNotBlank(serviceFeeUserInfo.getPauseOrderNo())) {
            EleBatteryServiceFeeOrder disableMembercardServiceFeeOrder = new EleBatteryServiceFeeOrder();
            if(Objects.equals(userBatteryMemberCard.getMemberCardStatus(),UserBatteryMemberCard.MEMBER_CARD_DISABLE)){
                disableMembercardServiceFeeOrder.setPayAmount(pauseBatteryServiceFee);
            }
            disableMembercardServiceFeeOrder.setOrderId(serviceFeeUserInfo.getPauseOrderNo());
            disableMembercardServiceFeeOrder.setStatus(EleBatteryServiceFeeOrder.STATUS_CLEAN);
            disableMembercardServiceFeeOrder.setUpdateTime(System.currentTimeMillis());
            batteryServiceFeeOrderService.updateByOrderNo(disableMembercardServiceFeeOrder);
        }
        if (StringUtils.isNotBlank(serviceFeeUserInfo.getExpireOrderNo())) {
            EleBatteryServiceFeeOrder expireMembercardServiceFeeOrder = new EleBatteryServiceFeeOrder();
            expireMembercardServiceFeeOrder.setOrderId(serviceFeeUserInfo.getExpireOrderNo());
            expireMembercardServiceFeeOrder.setStatus(EleBatteryServiceFeeOrder.STATUS_CLEAN);
            expireMembercardServiceFeeOrder.setPayAmount(expireBatteryServiceFee);
            expireMembercardServiceFeeOrder.setUpdateTime(System.currentTimeMillis());
            batteryServiceFeeOrderService.updateByOrderNo(expireMembercardServiceFeeOrder);
        }

        if(!Objects.equals(userBatteryMemberCard.getMemberCardStatus(),UserBatteryMemberCard.MEMBER_CARD_DISABLE) && StringUtils.isNotBlank(serviceFeeUserInfo.getPauseOrderNo())){
            EnableMemberCardRecord enableMemberCardRecord = enableMemberCardRecordService.selectLatestByUid(userInfo.getUid());
            if(Objects.nonNull(enableMemberCardRecord)){
                EnableMemberCardRecord enableMemberCardRecordUpdate =new EnableMemberCardRecord();
                enableMemberCardRecordUpdate.setId(enableMemberCardRecord.getId());
                enableMemberCardRecordUpdate.setBatteryServiceFeeStatus(EnableMemberCardRecord.STATUS_CLEAN);
                enableMemberCardRecordUpdate.setUpdateTime(System.currentTimeMillis());
                enableMemberCardRecordService.update(enableMemberCardRecordUpdate);
            }
        }

        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.CLEAN_BATTERY_SERVICE_FEE)
                .operateUid(user.getUid())
                .uid(uid)
                .name(user.getUsername())
                .batteryServiceFee(expireBatteryServiceFee.add(pauseBatteryServiceFee))
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);

/*

        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = serviceFeeUserInfoService.queryUserBatteryServiceFee(uid);

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);

            int disableCardDays=(int) Math.ceil((System.currentTimeMillis() - (userBatteryMemberCard.getDisableMemberCardTime() + 24 * 60 * 60 * 1000L)) / 1000.0 / 60 / 60 / 24);

            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(), user.getTenantId());
            if(Objects.isNull(eleDisableMemberCardRecord)){
                return R.fail("100370","停卡记录不存在");
            }

            EleDisableMemberCardRecord updateDisableMemberCardRecord = new EleDisableMemberCardRecord();
            updateDisableMemberCardRecord.setId(eleDisableMemberCardRecord.getId());
            updateDisableMemberCardRecord.setRealDays(disableCardDays);
            updateDisableMemberCardRecord.setUpdateTime(System.currentTimeMillis());
            eleDisableMemberCardRecordService.updateBYId(updateDisableMemberCardRecord);
            
            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(batteryMemberCard.getName())
                    .enableTime(System.currentTimeMillis())
                    .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT)
                    .disableDays(disableCardDays)
                    .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .phone(userInfo.getPhone())
                    .createTime(System.currentTimeMillis())
                    .tenantId(user.getTenantId())
                    .uid(uid)
                    .userName(userInfo.getName())
                    .updateTime(System.currentTimeMillis()).build();
            enableMemberCardRecordService.insert(enableMemberCardRecord);
        }

        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);


        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setDisableMemberCardNo("");
        serviceFeeUserInfoUpdate.setOrderNo("");
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
        } else {
            if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(System.currentTimeMillis());
            } else {
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime());
            }
        }

        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

        //更新滞纳金订单状态
        EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = new EleBatteryServiceFeeOrder();
        eleBatteryServiceFeeOrder.setOrderId(serviceFeeUserInfo.getOrderNo());
        eleBatteryServiceFeeOrder.setStatus(EleBatteryServiceFeeOrder.STATUS_CLEAN);
        eleBatteryServiceFeeOrder.setUpdateTime(System.currentTimeMillis());
        batteryServiceFeeOrderService.updateByOrderNo(eleBatteryServiceFeeOrder);


        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.CLEAN_BATTERY_SERVICE_FEE)
                .operateUid(user.getUid())
                .uid(uid)
                .name(user.getUsername())
                .batteryServiceFee(eleBatteryServiceFeeVO.getUserBatteryServiceFee())
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);

*/

        return R.ok();
    }

    @Override
    public R getDisableMemberCardList(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery) {
        return eleDisableMemberCardRecordService.list(electricityMemberCardRecordQuery);
    }

    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate) {

        if (Objects.nonNull(memberCardOrderAddAndUpdate.getValidDays()) && memberCardOrderAddAndUpdate.getValidDays() > 65535) {
            log.error("admin editUserMemberCard ERROR! not found user ");
            return R.fail("100029", "输入的天数过大");
        }

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin saveUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(memberCardOrderAddAndUpdate.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("admin saveUserMemberCard  ERROR! not found user! uid={}", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("admin saveUserMemberCard not pay deposit,uid={} ", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(memberCardOrderAddAndUpdate.getMemberCardId());
        if (Objects.isNull(electricityMemberCard) || !Objects.equals(electricityMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin saveUserMemberCard ERROR ,NOT FOUND MEMBER_CARD BY ID:{},uid:{}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("admin saveUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID:{},uid:{}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        Integer payCount = this.queryMaxPayCount(userBatteryMemberCard);

        //套餐订单
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        electricityMemberCardOrder.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
        electricityMemberCardOrder.setUid(memberCardOrderAddAndUpdate.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(0);
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setStoreId(userInfo.getStoreId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setPayCount(payCount + 1);
        electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
        electricityMemberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_ARTIFICIAL);
        //计算套餐剩余天数
        if (memberCardOrderAddAndUpdate.getMemberCardExpireTime() > System.currentTimeMillis()) {
            Double validDays = Math.ceil((memberCardOrderAddAndUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24.0);
            electricityMemberCardOrder.setValidDays(validDays.intValue());
        }

        baseMapper.insert(electricityMemberCardOrder);

        divisionAccountRecordService.handleBatteryMembercardDivisionAccount(electricityMemberCardOrder);

        UserBatteryMemberCard userBatteryMemberCardAddAndUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardAddAndUpdate.setUid(userInfo.getUid());
        userBatteryMemberCardAddAndUpdate.setTenantId(userInfo.getTenantId());
        userBatteryMemberCardAddAndUpdate.setMemberCardExpireTime(memberCardOrderAddAndUpdate.getMemberCardExpireTime());
        userBatteryMemberCardAddAndUpdate.setRemainingNumber(memberCardOrderAddAndUpdate.getMaxUseCount());
        userBatteryMemberCardAddAndUpdate.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
        userBatteryMemberCardAddAndUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardAddAndUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardAddAndUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
        userBatteryMemberCardAddAndUpdate.setCardPayCount(electricityMemberCardOrder.getPayCount());
        if (Objects.isNull(userBatteryMemberCard)) {
            userBatteryMemberCardAddAndUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardService.insert(userBatteryMemberCardAddAndUpdate);
        } else {
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardAddAndUpdate);
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userBatteryMemberCardAddAndUpdate.getUid());
        ServiceFeeUserInfo serviceFeeUserInfoInsertOrUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoInsertOrUpdate.setServiceFeeGenerateTime(memberCardOrderAddAndUpdate.getMemberCardExpireTime());
        serviceFeeUserInfoInsertOrUpdate.setUid(userBatteryMemberCardAddAndUpdate.getUid());
        serviceFeeUserInfoInsertOrUpdate.setFranchiseeId(electricityMemberCardOrder.getFranchiseeId());
        serviceFeeUserInfoInsertOrUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoInsertOrUpdate.setTenantId(electricityMemberCardOrder.getTenantId());
        if (Objects.isNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoInsertOrUpdate.setCreateTime(System.currentTimeMillis());
            serviceFeeUserInfoInsertOrUpdate.setDelFlag(ServiceFeeUserInfo.DEL_NORMAL);
            serviceFeeUserInfoInsertOrUpdate.setDisableMemberCardNo("");
            serviceFeeUserInfoService.insert(serviceFeeUserInfoInsertOrUpdate);
        } else {
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsertOrUpdate);
        }


        Double carDayTemp = Math.ceil((memberCardOrderAddAndUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24.0);


        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                .operateUid(user.getUid())
                .uid(memberCardOrderAddAndUpdate.getUid())
                .name(user.getUsername())
                .oldValidDays(MemberCardOrderAddAndUpdate.ZERO_VALIdDAY_MEMBER_CARD)
                .newValidDays(carDayTemp.intValue())
                .oldMaxUseCount(Objects.isNull(userBatteryMemberCard) ? userBatteryMemberCardAddAndUpdate.getRemainingNumber().longValue() : UserBatteryMemberCard.MEMBER_CARD_ZERO_REMAINING)
                .newMaxUseCount(memberCardOrderAddAndUpdate.getMaxUseCount())
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
    
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }
        return R.ok();
    }

    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R editUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate) {

        if (Objects.nonNull(memberCardOrderAddAndUpdate.getValidDays()) && memberCardOrderAddAndUpdate.getValidDays() > 65535) {
            log.error("admin editUserMemberCard ERROR! not found user ");
            return R.fail("100029", "输入的天数过大");
        }

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin editUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(memberCardOrderAddAndUpdate.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("admin editUserMemberCard ERROR! not found user! uid={}", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }


        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("admin editUserMemberCard ERROR! not pay deposit,uid={}", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(memberCardOrderAddAndUpdate.getMemberCardId());
        if (Objects.isNull(electricityMemberCard) || !Objects.equals(electricityMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin editUserMemberCard ERROR ,NOT FOUND MEMBER_CARD BY ID={},uid={}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("admin editUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID={},uid={}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        ElectricityMemberCard oldElectricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(oldElectricityMemberCard) || !Objects.equals(electricityMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin editUserMemberCard ERROR ,NOT FOUND MEMBER_CARD BY ID={},uid={}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("admin editUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID={},uid={}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());

        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);

        Long now = System.currentTimeMillis();
        long cardDays = 0;
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            BigDecimal serviceFee = checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
            userChangeServiceFee = serviceFee;
        }

        Long disableMemberCardTime = userBatteryMemberCard.getDisableMemberCardTime();

        //判断用户是否产生停卡电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {

            cardDays = (now - disableMemberCardTime) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - disableMemberCardTime) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }

            BigDecimal serviceFee = checkUserDisableCardBatteryService(userInfo, memberCardOrderAddAndUpdate.getUid(), cardDays, null, serviceFeeUserInfo);
            userChangeServiceFee = serviceFee;
        }

        if (BigDecimal.valueOf(0).compareTo(userChangeServiceFee) != 0) {
            return R.fail("ELECTRICITY.100000", "用户存在电池服务费", userChangeServiceFee);
        }

        if (ObjectUtil.equal(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
            log.error("admin editUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID:{},uid:{}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("100028", "月卡暂停状态，不能修改套餐过期时间!");
        }
    
        Integer payCount = this.queryMaxPayCount(userBatteryMemberCard);
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();

        if (!Objects.equals(memberCardOrderAddAndUpdate.getMemberCardId().longValue(), userBatteryMemberCard.getMemberCardId())) {
            //套餐订单
            ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
            electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
            electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
            electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
            electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            electricityMemberCardOrder.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
            electricityMemberCardOrder.setUid(memberCardOrderAddAndUpdate.getUid());
            electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
            electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
            electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
            electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
            electricityMemberCardOrder.setUserName(userInfo.getName());
            electricityMemberCardOrder.setValidDays(0);
            electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
            electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
            electricityMemberCardOrder.setStoreId(userInfo.getStoreId());
            electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
            electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
            electricityMemberCardOrder.setPayCount(payCount + 1);
            electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
            electricityMemberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_ARTIFICIAL);

            //计算套餐剩余天数
            if (memberCardOrderAddAndUpdate.getMemberCardExpireTime() > System.currentTimeMillis()) {
                Double validDays = Math.ceil((memberCardOrderAddAndUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24.0);
                electricityMemberCardOrder.setValidDays(validDays.intValue());
            }

            baseMapper.insert(electricityMemberCardOrder);

            userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrder.getPayCount());
        }


        //用户
        Long remainingNumber = memberCardOrderAddAndUpdate.getMaxUseCount();
        Long memberCardExpireTime = memberCardOrderAddAndUpdate.getMemberCardExpireTime();

        if (memberCardExpireTime < now || Objects.equals(memberCardOrderAddAndUpdate.getMaxUseCount(), MemberCardOrderAddAndUpdate.ZERO_USER_COUNT) || Objects.nonNull(memberCardOrderAddAndUpdate.getValidDays()) && Objects.equals(memberCardOrderAddAndUpdate.getValidDays(), MemberCardOrderAddAndUpdate.ZERO_VALIdDAY_MEMBER_CARD) && (userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24 != MemberCardOrderAddAndUpdate.ZERO_VALIdDAY_MEMBER_CARD) {
            remainingNumber = MemberCardOrderAddAndUpdate.ZERO_USER_COUNT;
            if (memberCardExpireTime <= now) {
                memberCardExpireTime = memberCardOrderAddAndUpdate.getMemberCardExpireTime();
            } else {
                memberCardExpireTime = System.currentTimeMillis();
            }
        }

        if (Objects.equals(remainingNumber, UserBatteryMemberCard.UN_LIMIT_COUNT_REMAINING_NUMBER)) {
            remainingNumber = ElectricityMemberCard.UN_LIMITED_COUNT;
        }

        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
        userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        }
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);


        if (Objects.nonNull(serviceFeeUserInfo)) {
            ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoUpdate.setUid(serviceFeeUserInfo.getUid());
            serviceFeeUserInfoUpdate.setTenantId(serviceFeeUserInfo.getTenantId());
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        }

        Double oldCardDay = 0.0;
        if (userBatteryMemberCard.getMemberCardExpireTime() - now > 0) {
            oldCardDay = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - now) / 1000L / 60 / 60 / 24.0);
        }

        Double carDayTemp = 0.0;
        if (memberCardOrderAddAndUpdate.getMemberCardExpireTime() > now) {
            carDayTemp = Math.ceil((memberCardOrderAddAndUpdate.getMemberCardExpireTime() - now) / 1000L / 60 / 60 / 24.0);
        }


        Long oldMaxUseCount = userBatteryMemberCard.getRemainingNumber().longValue();
        if (Objects.equals(oldElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            oldMaxUseCount = UserBatteryMemberCard.UN_LIMIT_COUNT_REMAINING_NUMBER;
        }

        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                .operateUid(user.getUid())
                .uid(memberCardOrderAddAndUpdate.getUid())
                .name(user.getUsername())
                .oldValidDays(oldCardDay.intValue())
                .newValidDays(carDayTemp.intValue())
                .oldMaxUseCount(oldMaxUseCount)
                .newMaxUseCount(memberCardOrderAddAndUpdate.getMaxUseCount())
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
    
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }
        return R.ok();
    }

    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R renewalUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin editUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(memberCardOrderAddAndUpdate.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("admin editUserMemberCard ERROR! not found user! uid={}", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("admin editUserMemberCard ERROR! not pay deposit,uid={} ", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(memberCardOrderAddAndUpdate.getMemberCardId());
        if (Objects.isNull(electricityMemberCard) || !Objects.equals(electricityMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin editUserMemberCard ERROR ,NOT FOUND MEMBER_CARD BY ID={},uid={}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("admin editUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID={},uid={}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());

        Long now = System.currentTimeMillis();
        long cardDays = 0;
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
            BigDecimal serviceFee = checkUserMemberCardExpireBatteryService(userInfo, franchisee, cardDays);
            userChangeServiceFee = serviceFee;
        }

        Long disableMemberCardTime = userBatteryMemberCard.getDisableMemberCardTime();

        //判断用户是否产生停卡电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {

            cardDays = (now - disableMemberCardTime) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - disableMemberCardTime) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }

            BigDecimal serviceFee = checkUserDisableCardBatteryService(userInfo, memberCardOrderAddAndUpdate.getUid(), cardDays, null, serviceFeeUserInfo);
            userChangeServiceFee = serviceFee;
        }

        if (BigDecimal.valueOf(0).compareTo(userChangeServiceFee) != 0) {
            return R.fail("ELECTRICITY.100000", "用户存在电池服务费", userChangeServiceFee);
        }

        if (ObjectUtil.equal(UserBatteryMemberCard.MEMBER_CARD_DISABLE, userBatteryMemberCard.getMemberCardStatus())) {
            log.error("admin editUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID:{},uid:{}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("100028", "月卡暂停状态，不能修改套餐过期时间!");
        }
    
        Integer payCount = this.queryMaxPayCount(userBatteryMemberCard);

        //套餐订单
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        electricityMemberCardOrder.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
        electricityMemberCardOrder.setUid(memberCardOrderAddAndUpdate.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setStoreId(userInfo.getStoreId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setPayCount(payCount + 1);
        electricityMemberCardOrder.setSource(ElectricityMemberCardOrder.SOURCE_ARTIFICIAL);
        baseMapper.insert(electricityMemberCardOrder);

        //用户
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        Long memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime();
        if (memberCardExpireTime < now) {
            //当前时间加购买的套餐过期时间
            memberCardExpireTime = now + electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
        } else {
            memberCardExpireTime = userBatteryMemberCard.getMemberCardExpireTime() + electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
        }


        Long remainingNumber = electricityMemberCard.getMaxUseCount();
        if (!ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, userBatteryMemberCard.getRemainingNumber()) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && userBatteryMemberCard.getMemberCardExpireTime() > now) {
            remainingNumber = electricityMemberCard.getMaxUseCount() + userBatteryMemberCard.getRemainingNumber();
        }

        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
        userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber);
        userBatteryMemberCardUpdate.setCardPayCount(electricityMemberCardOrder.getPayCount());
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        }
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        if (Objects.nonNull(serviceFeeUserInfo)) {
            ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
            serviceFeeUserInfoUpdate.setUid(serviceFeeUserInfo.getUid());
            serviceFeeUserInfoUpdate.setTenantId(serviceFeeUserInfo.getTenantId());
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        }

        divisionAccountRecordService.handleBatteryMembercardDivisionAccount(electricityMemberCardOrder);

        Double oldCardDay = 0.0;
        if (userBatteryMemberCard.getMemberCardExpireTime() - now > 0) {
            oldCardDay = Math.ceil((userBatteryMemberCard.getMemberCardExpireTime() - now) / 1000L / 60 / 60 / 24.0);
        }

        Double carDayTemp = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - now) / 1000L / 60 / 60 / 24.0);

        Long oldMaxUseCount = userBatteryMemberCard.getRemainingNumber().longValue();
        Long newMaxUseCount = electricityMemberCard.getMaxUseCount();

        if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            oldMaxUseCount = UserBatteryMemberCard.UN_LIMIT_COUNT_REMAINING_NUMBER;
            newMaxUseCount = UserBatteryMemberCard.UN_LIMIT_COUNT_REMAINING_NUMBER;
        } else {
            newMaxUseCount = oldMaxUseCount + newMaxUseCount;
        }

        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                .operateUid(user.getUid())
                .uid(memberCardOrderAddAndUpdate.getUid())
                .name(user.getUsername())
                .oldValidDays(oldCardDay.intValue())
                .newValidDays(carDayTemp.intValue())
                .oldMaxUseCount(oldMaxUseCount)
                .newMaxUseCount(newMaxUseCount)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
    
        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }
        return R.ok();
    }

    @Override
    public Long calcRentCarMemberCardExpireTime(String rentType, Integer rentTime, UserCarMemberCard userCarMemberCard) {
        Long memberCardValidDays = null;
        if (ElectricityCarModel.RENT_TYPE_MONTH.equals(rentType)) {
            memberCardValidDays = rentTime * 30 * 24 * 60 * 60 * 1000L;
        } else if (ElectricityCarModel.RENT_TYPE_WEEK.equals(rentType)) {
            memberCardValidDays = rentTime * 7 * 24 * 60 * 60 * 1000L;
        } else {
            memberCardValidDays = 0L;
        }

        if (Objects.isNull(userCarMemberCard) || userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            return System.currentTimeMillis() + memberCardValidDays;
        } else {
            return userCarMemberCard.getMemberCardExpireTime() + memberCardValidDays;
        }
    }

    @Override
    public ElectricityMemberCardOrder queryLastPayMemberCardTimeByUid(Long uid, Long franchiseeId, Integer tenantId) {
        return baseMapper.queryLastPayMemberCardTimeByUid(uid, franchiseeId, tenantId);
    }

    @Override
    public ElectricityMemberCardOrder queryLastPayMemberCardTimeByUidAndSuccess(Long uid, Long franchiseeId, Integer tenantId) {
        return baseMapper.queryLastPayMemberCardTimeByUidAndSuccess(uid, franchiseeId, tenantId);
    }

    @Override
    public ElectricityMemberCardOrder selectLatestByUid(Long uid) {
        return baseMapper.selectLatestByUid(uid);
    }

    @Slave
    @Override
    public BigDecimal queryBatteryMemberCardTurnOver(Integer tenantId, Long
            todayStartTime, List<Long> franchiseeId) {
        return Optional.ofNullable(baseMapper.queryBatteryMemberCardTurnOver(tenantId, todayStartTime, franchiseeId)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    public BigDecimal queryCarMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeId) {
        return Optional.ofNullable(baseMapper.queryCarMemberCardTurnOver(tenantId, todayStartTime, franchiseeId)).orElse(BigDecimal.valueOf(0));
    }

    @Slave
    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryBatteryMemberCardTurnOverByCreateTime(Integer
                                                                                                     tenantId, List<Long> franchiseeId, Long beginTime, Long endTime) {
        return baseMapper.queryBatteryMemberCardTurnOverByCreateTime(tenantId, franchiseeId, beginTime, endTime);
    }

    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryCarMemberCardTurnOverByCreateTime(Integer
                                                                                                 tenantId, List<Long> franchiseeId, Long beginTime, Long endTime) {
        return baseMapper.queryCarMemberCardTurnOverByCreateTime(tenantId, franchiseeId, beginTime, endTime);
    }

    @Slave
    @Override
    public BigDecimal querySumMemberCardTurnOver(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long
            endTime) {
        return baseMapper.querySumMemberCardTurnOverByCreateTime(tenantId, franchiseeId, beginTime, endTime);
    }

    @Override
    public void batteryMemberCardExpireReminder() {
        if (!redisService.setNx(CacheConstant.CACHE_ELE_BATTERY_MEMBER_CARD_EXPIRED_LOCK, "ok", 120000L, false)) {
            log.warn("batteryMemberCardExpireReminder in execution...");
            return;
        }

        int offset = 0;
        int size = 300;
        Date date = new Date();
        long firstTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis() + 3 * 3600000 * 24;
        SimpleDateFormat simp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String firstTimeStr = redisService.get(CacheConstant.CACHE_ELE_BATTERY_MEMBER_CARD_EXPIRED_LAST_TIME);
        if (StrUtil.isNotBlank(firstTimeStr)) {
            firstTime = Long.parseLong(firstTimeStr);
        }

        redisService.set(CacheConstant.CACHE_ELE_BATTERY_MEMBER_CARD_EXPIRED_LAST_TIME, String.valueOf(lastTime));

        while (true) {
            List<BatteryMemberCardExpiringSoonQuery> franchiseeUserInfos =
                    userBatteryMemberCardService.batteryMemberCardExpire(offset, size, firstTime, lastTime);
            if (!DataUtil.collectionIsUsable(franchiseeUserInfos)) {
                return;
            }

            franchiseeUserInfos.parallelStream().forEach(item -> {
                ElectricityPayParams ele = electricityPayParamsService.queryFromCache(item.getTenantId());
                if (Objects.isNull(ele)) {
                    log.error(
                            "BATTERY MEMBER CARD EXPIRING SOON ERROR! ElectricityPayParams is null error! tenantId={}",
                            item.getTenantId());
                    return;
                }

                TemplateConfigEntity templateConfigEntity =
                        templateConfigService.queryByTenantIdFromCache(item.getTenantId());
                if (Objects.isNull(templateConfigEntity) || Objects
                        .isNull(templateConfigEntity.getBatteryOuttimeTemplate())) {
                    log.error(
                            "BATTERY MEMBER CARD EXPIRING SOON ERROR! TemplateConfigEntity is null error! tenantId={}",
                            item.getTenantId());
                    return;
                }

                date.setTime(item.getMemberCardExpireTime());

                item.setMerchantMinProAppId(ele.getMerchantMinProAppId());
                item.setMerchantMinProAppSecert(ele.getMerchantMinProAppSecert());
                item.setMemberCardExpiringTemplate(templateConfigEntity.getBatteryMemberCardExpiringTemplate());
                item.setMemberCardExpireTimeStr(simp.format(date));
                sendBatteryMemberCardExpiringTemplate(item);
            });
            offset += size;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void systemEnableMemberCardTask() {
        int offset = 0;
        int size = 300;

        while (true) {

            //获取停卡用户
            List<ServiceFeeUserInfo> userDisableMembercardList = serviceFeeUserInfoService.selectDisableMembercardList(offset, size);
            if(CollectionUtils.isEmpty(userDisableMembercardList)){
                return;
            }

            userDisableMembercardList.parallelStream().forEach(item->{
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(item.getUid());
                if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getDisableMemberCardTime())) {
                    log.error("ELE ENABLE BATTERY MEMBERCARD ERROR! not found userBatteryMemberCard,uid={}", item.getUid());
                    return;
                }

                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
                if(Objects.isNull(batteryMemberCard)){
                    log.error("ELE ENABLE BATTERY MEMBERCARD ERROR! not found batteryMemberCard,uid={},mid={}", item.getUid(),userBatteryMemberCard.getMemberCardId());
                    return;
                }

                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                if(Objects.isNull(userInfo)){
                    log.error("ELE ENABLE BATTERY MEMBERCARD ERROR! not found userInfo,uid={}", item.getUid());
                    return;
                }

                //获取停卡记录
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

                //套餐过期时间=停卡时间+暂停天数+套餐余量
                Long memberCardExpireTime = userBatteryMemberCard.getDisableMemberCardTime() + eleDisableMemberCardRecord.getChooseDays() * 24 * 60 * 60 * 1000L + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                Long orderExpireTime = userBatteryMemberCard.getDisableMemberCardTime() + eleDisableMemberCardRecord.getChooseDays() * 24 * 60 * 60 * 1000L + (userBatteryMemberCard.getOrderExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());

                //更新用户套餐到期时间，启用用户套餐
                UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
                userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
                userBatteryMemberCardUpdate.setOrderExpireTime(orderExpireTime);
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);

                //更新电池服务费产生时间
                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(userBatteryMemberCard.getUid());
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
                serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

                BigDecimal serviceFee=BigDecimal.ZERO;
                if(Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)){
                    serviceFee=batteryMemberCard.getServiceCharge().multiply(BigDecimal.valueOf(eleDisableMemberCardRecord.getChooseDays()));
                }

                //生成启用记录
                EnableMemberCardRecord enableMemberCardRecordInsert = EnableMemberCardRecord.builder()
                        .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                        .memberCardName(eleDisableMemberCardRecord.getMemberCardName())
                        .enableTime(System.currentTimeMillis())
                        .enableType(EnableMemberCardRecord.SYSTEM_ENABLE)
                        .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT)
                        .disableDays(eleDisableMemberCardRecord.getChooseDays())
                        .disableTime(eleDisableMemberCardRecord.getCreateTime())
                        .storeId(userInfo.getStoreId())
                        .franchiseeId(userInfo.getFranchiseeId())
                        .phone(userInfo.getPhone())
                        .serviceFee(serviceFee)
                        .createTime(System.currentTimeMillis())
                        .tenantId(userInfo.getTenantId())
                        .uid(userInfo.getUid())
                        .userName(userInfo.getName())
                        .updateTime(System.currentTimeMillis()).build();
                enableMemberCardRecordService.insert(enableMemberCardRecordInsert);

                //获取滞纳金订单
                EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder = eleBatteryServiceFeeOrderService.selectByOrderNo(item.getPauseOrderNo());
                if(Objects.nonNull(eleBatteryServiceFeeOrder)){
                    EleBatteryServiceFeeOrder eleBatteryServiceFeeOrderUpdate = new EleBatteryServiceFeeOrder();
                    eleBatteryServiceFeeOrderUpdate.setId(eleBatteryServiceFeeOrder.getId());
                    eleBatteryServiceFeeOrderUpdate.setPayAmount(eleBatteryServiceFeeOrder.getBatteryServiceFee().multiply(BigDecimal.valueOf(eleDisableMemberCardRecord.getChooseDays())));
                    eleBatteryServiceFeeOrderUpdate.setBatteryServiceFeeEndTime(userBatteryMemberCard.getDisableMemberCardTime() + eleDisableMemberCardRecord.getChooseDays() * 24 * 60 * 60 * 1000L);
                    eleBatteryServiceFeeOrderUpdate.setUpdateTime(System.currentTimeMillis());
                    eleBatteryServiceFeeOrderService.update(eleBatteryServiceFeeOrderUpdate);
                }
            });

            offset += size;
        }


        /*int offset = 0;
        int size = 300;
        long nowTime = System.currentTimeMillis();

        while (true) {
            List<EleDisableMemberCardRecord> eleDisableMemberCardRecordList = eleDisableMemberCardRecordService.queryDisableCardExpireRecord(offset, size, nowTime);

            log.debug("-----eleDisableMemberCardRecordList>>>>>{}", eleDisableMemberCardRecordList);

            if (!DataUtil.collectionIsUsable(eleDisableMemberCardRecordList)) {
                return;
            }

            eleDisableMemberCardRecordList.parallelStream().forEach(item -> {

                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(item.getUid());
                if(Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getDisableMemberCardTime()) ||Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())){
                    return;
                }

                ElectricityMemberCard electricityMemberCard = null;
                if (!Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
                    electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
                }

                if (Objects.isNull(electricityMemberCard)) {
                    return;
                }
                UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
                if (Objects.isNull(userInfo)) {
                    log.error("ELE ERROR! not found useeInfo,uid={}", item.getUid());
                    return;
                }

                EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                        .disableMemberCardNo(item.getDisableMemberCardNo())
                        .memberCardName(electricityMemberCard.getName())
                        .enableTime(System.currentTimeMillis())
                        .enableType(EnableMemberCardRecord.SYSTEM_ENABLE)
                        .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_NOT_PAY)
                        .disableDays(item.getChooseDays())
                        .disableTime(item.getUpdateTime())
                        .franchiseeId(userInfo.getFranchiseeId())
                        .phone(item.getPhone())
                        .createTime(System.currentTimeMillis())
                        .tenantId(userInfo.getTenantId())
                        .uid(item.getUid())
                        .userName(item.getUserName())
                        .updateTime(System.currentTimeMillis()).build();
                if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && Objects.nonNull(item.getChargeRate())) {
                    enableMemberCardRecord.setServiceFee(item.getChargeRate().multiply(BigDecimal.valueOf(item.getChooseDays())));
                }
                enableMemberCardRecordService.insert(enableMemberCardRecord);

                Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
                UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
                userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
                userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
                userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
                userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
                userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

                ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
                serviceFeeUserInfoUpdate.setUid(item.getUid());
                serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
                serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

                EleDisableMemberCardRecord eleDisableMemberCardRecordUpdate = new EleDisableMemberCardRecord();
                eleDisableMemberCardRecordUpdate.setId(item.getId());
                eleDisableMemberCardRecordUpdate.setRealDays(item.getChooseDays());
                eleDisableMemberCardRecordService.updateBYId(eleDisableMemberCardRecordUpdate);
            });
            offset += size;
        }*/
    }


    private void sendCarMemberCardExpiringTemplate(CarMemberCardExpiringSoonQuery carMemberCardExpiringSoonQuery) {
        AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
        appTemplateQuery.setFormId(RandomUtil.randomString(20));
        appTemplateQuery.setTouser(carMemberCardExpiringSoonQuery.getThirdId());
        appTemplateQuery.setAppId(carMemberCardExpiringSoonQuery.getMerchantMinProAppId());
        appTemplateQuery.setSecret(carMemberCardExpiringSoonQuery.getMerchantMinProAppSecert());
        appTemplateQuery.setTemplateId(carMemberCardExpiringSoonQuery.getMemberCardExpiringTemplate());
        Map<String, Object> data = new HashMap<>(4);
        appTemplateQuery.setData(data);

        data.put("thing2", "租车套餐");
        data.put("date4", carMemberCardExpiringSoonQuery.getRentCarMemberCardExpireTimeStr());
        data.put("thing3", "租车套餐即将过期，请及时续费。");

        log.info("CAR MEMBER CARD EXPIRING REMINDER: param={}", carMemberCardExpiringSoonQuery);

        weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
    }

    private void sendBatteryMemberCardExpiringTemplate(
            BatteryMemberCardExpiringSoonQuery batteryMemberCardExpiringSoonQuery) {
        AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
        appTemplateQuery.setAppId(batteryMemberCardExpiringSoonQuery.getMerchantMinProAppId());
        appTemplateQuery.setSecret(batteryMemberCardExpiringSoonQuery.getMerchantMinProAppSecert());
        appTemplateQuery.setTouser(batteryMemberCardExpiringSoonQuery.getThirdId());
        appTemplateQuery.setFormId(RandomUtil.randomString(20));
        appTemplateQuery.setTemplateId(batteryMemberCardExpiringSoonQuery.getMemberCardExpiringTemplate());
        Map<String, Object> data = new HashMap<>(4);
        appTemplateQuery.setData(data);

        data.put("thing2", batteryMemberCardExpiringSoonQuery.getCardName());
        data.put("date4", batteryMemberCardExpiringSoonQuery.getMemberCardExpireTimeStr());
        data.put("thing3", "电池套餐即将过期，请及时续费。");

        log.info("BATTERY MEMBER CARD EXPIRING REMINDER: param={}", batteryMemberCardExpiringSoonQuery);

        weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
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
    public BigDecimal checkUserDisableCardBatteryService(UserInfo userInfo, Long uid, Long cardDays, EleDisableMemberCardRecord eleDisableMemberCardRecord, ServiceFeeUserInfo serviceFeeUserInfo) {

        if (Objects.isNull(serviceFeeUserInfo)) {
            serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(uid);
        }
//        if (Objects.isNull(serviceFeeUserInfo) || Objects.equals(serviceFeeUserInfo.getExistBatteryServiceFee(), ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE)) {
//            return BigDecimal.valueOf(0);
//        }
        if (Objects.isNull(eleDisableMemberCardRecord)) {
            eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(uid, userInfo.getTenantId());
        }

        //判断服务费
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

        //判断服务费
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && cardDays >= 1) {
            //计算服务费
            BigDecimal userMemberCardExpireBatteryServiceFee = batteryServiceFee.multiply(BigDecimal.valueOf(cardDays));
            return userMemberCardExpireBatteryServiceFee;
        } else {
            return BigDecimal.valueOf(0);
        }
    }

    //停卡审核通知
    private void sendDisableMemberCardMessage(UserInfo userInfo) {
        List<MqNotifyCommon<AuthenticationAuditMessageNotify>> messageNotifyList = this.buildDisableMemberCardMessageNotify(userInfo);
        if (CollectionUtils.isEmpty(messageNotifyList)) {
            return;
        }

        messageNotifyList.forEach(i -> {
            rocketMqService.sendAsyncMsg(MqProducerConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(i), "", "", 0);
            log.info("ELE INFO! user authentication audit notify,msg={},uid={}", JsonUtil.toJson(i), userInfo.getUid());
        });
    }


    private List<MqNotifyCommon<AuthenticationAuditMessageNotify>> buildDisableMemberCardMessageNotify(UserInfo userInfo) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(userInfo.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.error("ELE ERROR! not found maintenanceUserNotifyConfig,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
            return Collections.EMPTY_LIST;
        }

        if ((notifyConfig.getPermissions() & MaintenanceUserNotifyConfig.TYPE_DISABLE_MEMBER_CARD)
                != MaintenanceUserNotifyConfig.TYPE_DISABLE_MEMBER_CARD) {
            log.info("ELE ERROR! not maintenance permission,permissions={},uid={}", notifyConfig.getPermissions(), userInfo.getUid());
            return Collections.EMPTY_LIST;
        }


        List<String> phones = JsonUtil.fromJsonArray(notifyConfig.getPhones(), String.class);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(phones)) {
            log.error("ELE ERROR! phones is empty,tenantId={},uid={}", userInfo.getTenantId(), userInfo.getUid());
            return Collections.EMPTY_LIST;
        }

        return phones.parallelStream().map(item -> {
            AuthenticationAuditMessageNotify messageNotify = new AuthenticationAuditMessageNotify();
            messageNotify.setBusinessCode(StringUtils.isBlank(userInfo.getIdNumber()) ? "/" : userInfo.getIdNumber().substring(userInfo.getIdNumber().length() - 6));
            messageNotify.setUserName(userInfo.getName());
            messageNotify.setAuthTime(DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));

            MqNotifyCommon<AuthenticationAuditMessageNotify> authMessageNotifyCommon = new MqNotifyCommon<>();
            authMessageNotifyCommon.setTime(System.currentTimeMillis());
            authMessageNotifyCommon.setType(MqNotifyCommon.TYPE_DISABLE_MEMBER_CARD);
            authMessageNotifyCommon.setPhone(item);
            authMessageNotifyCommon.setData(messageNotify);
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

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //校验用户
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
    public Triple<Boolean, String, Object> handleRentBatteryMemberCard(String productKey, String deviceName, Set<Integer> userCouponIds, Integer memberCardId, Long franchiseeId, UserInfo userInfo) {
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

        //购买套餐扫码的柜机
        Long refId = null;
        //购买套餐来源
        Integer source = ElectricityMemberCardOrder.SOURCE_NOT_SCAN;
        if (StringUtils.isNotBlank(productKey) && StringUtils.isNotBlank(deviceName)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
            if (Objects.isNull(electricityCabinet)) {
                log.error("BATTERY MEMBER ORDER ERROR! not found electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
                return Triple.of(false, "ELECTRICITY.0005", "未找到换电柜");
            }

            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,eid={},uid={}", electricityCabinet.getId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("BATTERY MEMBER ORDER ERROR!not found store,storeId={},uid={}", electricityCabinet.getStoreId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("BATTERY MEMBER ORDER ERROR!not found Franchisee,storeId={},uid={}", store.getId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }

            //换电柜加盟商和套餐加盟商一致  则保存套餐来源
            if (Objects.equals(store.getFranchiseeId(), electricityMemberCard.getFranchiseeId())) {
                source = ElectricityMemberCardOrder.SOURCE_SCAN;
                refId = electricityCabinet.getId().longValue();
            }
        }

        //查找计算优惠券
        //计算优惠后支付金额
        Triple<Boolean, String, Object> calculatePayAmountResult = calculatePayAmount(electricityMemberCard.getHolidayPrice(), userCouponIds);
        if(Boolean.FALSE.equals(calculatePayAmountResult.getLeft())){
            return calculatePayAmountResult;
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();


        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService
                .selectByUidFromCache(userInfo.getUid());
        Integer payCount = this.queryMaxPayCount(userBatteryMemberCard);

        //支付金额不能为负数
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

        return Triple.of(true, null, electricityMemberCardOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R cancelPayMemberCard() {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("cancel MEMBER CARD ERROR! not found user ");
            return R.ok();
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_BATTERY_CANCELL_PAYMENT_LOCK_KEY + user.getUid(), "1", 5 * 1000L, false)) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("cancel MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.ok();
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = baseMapper.queryCreateTimeMaxMemberCardOrder(userInfo.getUid(), userInfo.getTenantId());
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getStatus(), ElectricityMemberCardOrder.STATUS_INIT)) {
            return R.ok();
        }

        //取消支付  清除套餐来源
        ElectricityMemberCardOrder memberCardOrderUpdate=new ElectricityMemberCardOrder();
        memberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        memberCardOrderUpdate.setSource(NumberConstant.ZERO);
        memberCardOrderUpdate.setRefId(NumberConstant.ZERO_L);
        memberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_CANCELL);//更新订单状态 为取消支付
        memberCardOrderUpdate.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        memberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.baseMapper.updateById(memberCardOrderUpdate);

        //获取套餐订单优惠券
        List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());
        if(CollectionUtils.isEmpty(userCouponIds)){
            return R.ok();
        }

        Set<Integer> couponIds=userCouponIds.parallelStream().map(item->userCouponService.queryByIdFromDB(item.intValue())).filter(Objects::nonNull)
                .filter(e->Objects.equals(e.getStatus(), UserCoupon.STATUS_IS_BEING_VERIFICATION)).map(i->i.getId().intValue()).collect(Collectors.toSet());

        userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_UNUSED, null));

        redisService.delete(WechatPayConstant.PAY_ORDER_ID_CALL_BACK + electricityMemberCardOrder.getOrderId());

        return R.ok();
    }

    /**
     * 结束订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> endOrder(String orderNo, Long uid) {

        if (!redisService.setNx(CacheConstant.ELE_CACHE_BATTERY_CANCELL_PAYMENT_LOCK_KEY + uid, "1", 5 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = this.baseMapper.selectOne(
                new LambdaQueryWrapper<ElectricityMemberCardOrder>()
                        .eq(ElectricityMemberCardOrder::getOrderId, orderNo)
                        .eq(ElectricityMemberCardOrder::getUid, uid).in(ElectricityMemberCardOrder::getStatus, ElectricityMemberCardOrder.STATUS_INIT, ElectricityMemberCardOrder.STATUS_FAIL));

        if (Objects.isNull(electricityMemberCardOrder)) {
            log.error("BATTERY MEMBERCARD ERROR!not found electricityMemberCardOrder,uid={},orderId={}", uid, orderNo);
            return Triple.of(false, "ELECTRICITY.0015", "订单不存在！");
        }

        //取消支付  清除套餐来源
        ElectricityMemberCardOrder memberCardOrderUpdate = new ElectricityMemberCardOrder();
        memberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        memberCardOrderUpdate.setSource(NumberConstant.ZERO);
        memberCardOrderUpdate.setRefId(NumberConstant.ZERO_L);
        memberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_CANCELL);//更新订单状态 为取消支付
        memberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.baseMapper.updateById(memberCardOrderUpdate);

        //获取套餐订单优惠券
        List<Long> userCouponIds = memberCardOrderCouponService.selectCouponIdsByOrderId(electricityMemberCardOrder.getOrderId());
        if(CollectionUtils.isEmpty(userCouponIds)){
            return Triple.of(true, "", null);
        }

        Set<Integer> couponIds=userCouponIds.parallelStream().map(item->userCouponService.queryByIdFromDB(item.intValue())).filter(Objects::nonNull)
                .filter(e->Objects.equals(e.getStatus(), UserCoupon.STATUS_IS_BEING_VERIFICATION)).map(i->i.getId().intValue()).collect(Collectors.toSet());

        userCouponService.batchUpdateUserCoupon(electricityMemberCardOrderService.buildUserCouponList(couponIds, UserCoupon.STATUS_UNUSED, null));

        return Triple.of(true, "", null);
    }

    @Override
    public Pair<Boolean, Object> checkUserHaveBatteryServiceFee(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard) {
        //用户所产生的电池服务费
        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);

        //获取新用户所绑定的加盟商的电池服务费
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
//            log.error("BATTERY SERVICE FEE ERROR!not found franchisee,uid={}", userInfo.getUid());
            return Pair.of(false, null);
        }

        //没有开启电池服务费功能
        if (Objects.equals(Franchisee.CLOSE_SERVICE_FEE, franchisee.getIsOpenServiceFee())) {
            return Pair.of(false, null);
        }

        //统一型号，且电池服务费单价为0
        Integer modelType = franchisee.getModelType();
        if ((Objects.equals(modelType, Franchisee.OLD_MODEL_TYPE) && Objects.equals(franchisee.getBatteryServiceFee(), BigDecimal.valueOf(0)))) {
            return Pair.of(false, null);
        }


        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(serviceFeeUserInfo) || Objects.equals(serviceFeeUserInfo, ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE)) {
            return Pair.of(false, null);
        }

        //抄的  @See ServiceFeeUserInfoServiceImpl#queryUserBatteryServiceFee()

        long cardDays = 0;
        //用户产生的套餐过期电池服务费
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (System.currentTimeMillis() - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            userChangeServiceFee = this.checkUserMemberCardExpireBatteryService(userInfo, franchisee, cardDays);
        }


        Integer memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE;
        //用户产生的停卡电池服务费
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {
                cardDays = (System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                //不足一天按一天计算
                double time = Math.ceil((System.currentTimeMillis() - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1;
                }
                userChangeServiceFee = this.checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, null, serviceFeeUserInfo);
                memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_DISABLE;
            }
        }

        if (BigDecimal.valueOf(0).compareTo(userChangeServiceFee) < 0) {
            return Pair.of(true, userChangeServiceFee);
        }

        return Pair.of(false, null);
    }

    @Override
    public Triple<Boolean, String, Object> addUserDepositAndMemberCard(UserBatteryDepositAndMembercardQuery query) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(query.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getMembercardId());
        if (Objects.isNull(batteryMemberCard)) {
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }

        if(Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(),NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),batteryMemberCard.getFranchiseeId())){
            return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0042", "用户已缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
            return Triple.of(false, "ELECTRICITY.00121", "用户已绑定电池套餐");
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = saveUserInfoAndOrder(userInfo, batteryMemberCard, userBatteryMemberCard);

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

        //赠送优惠券
        sendUserCoupon(batteryMemberCard, electricityMemberCardOrder);

        return Triple.of(true, null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public ElectricityMemberCardOrder saveUserInfoAndOrder(UserInfo userInfo,BatteryMemberCard batteryMemberCard,UserBatteryMemberCard userBatteryMemberCard){
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid()))
                .uid(userInfo.getUid())
                .phone(userInfo.getPhone())
                .name(userInfo.getName())
                .payAmount(batteryMemberCard.getDeposit())
                .status(EleDepositOrder.STATUS_SUCCESS)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId())
                .franchiseeId(batteryMemberCard.getFranchiseeId())
                .payType(EleDepositOrder.OFFLINE_PAYMENT)
                .storeId(userInfo.getStoreId())
                .mid(batteryMemberCard.getId())
                .modelType(0).build();
        depositOrderService.insert(eleDepositOrder);

        ElectricityMemberCardOrder electricityMemberCardOrder = ElectricityMemberCardOrder.builder()
                .orderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()))
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .status(ElectricityMemberCardOrder.STATUS_SUCCESS)
                .memberCardId(batteryMemberCard.getId())
                .uid(userInfo.getUid())
                .maxUseCount(batteryMemberCard.getUseCount())
                .cardName(batteryMemberCard.getName())
                .payAmount(batteryMemberCard.getRentPrice())
                .userName(userInfo.getName())
                .validDays(batteryMemberCard.getValidDays())
                .tenantId(batteryMemberCard.getTenantId())
                .franchiseeId(batteryMemberCard.getFranchiseeId())
                .payCount(queryMaxPayCount(userBatteryMemberCard) + 1)
                .payType(ElectricityMemberCardOrder.OFFLINE_PAYMENT)
                .refId(null)
                .sendCouponId(Objects.nonNull(batteryMemberCard.getCouponId()) ? batteryMemberCard.getCouponId().longValue() : null)
                .useStatus(ElectricityMemberCardOrder.USE_STATUS_USING)
                .source(ElectricityMemberCardOrder.SOURCE_NOT_SCAN)
                .storeId(userInfo.getStoreId()).build();
        this.baseMapper.insert(electricityMemberCardOrder);

        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(userInfo.getUid());
        userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
        userInfoUpdate.setFranchiseeId(batteryMemberCard.getFranchiseeId());
        userInfoUpdate.setPayCount(userInfo.getPayCount()+1);
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
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);

        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(electricityMemberCardOrder.getUid());
        userBatteryMemberCardUpdate.setOrderId(electricityMemberCardOrder.getOrderId());
        userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,electricityMemberCardOrder));
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,electricityMemberCardOrder));
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
        serviceFeeUserInfoInsert.setServiceFeeGenerateTime(System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, electricityMemberCardOrder));
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

        EleUserOperateRecord eleUserDepositOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.DEPOSIT_MODEL)
                .operateContent(EleUserOperateRecord.DEPOSIT_MODEL)
                .operateUid(SecurityUtils.getUid())
                .uid(eleDepositOrder.getUid())
                .name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername())
                .oldBatteryDeposit(null)
                .newBatteryDeposit(eleDepositOrder.getPayAmount())
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserDepositOperateRecord);

        int oldValidDays = 0;
        int newValidDays = 0;
        long oldMaxUseCount = 0L;
        long newMaxUseCount = 0L;
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && !Objects.equals(userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                oldValidDays = Math.toIntExact((userBatteryMemberCard.getMemberCardExpireTime() / 24 / 60 / 60 / 1000));
                oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
            } else {
                newValidDays = Math.toIntExact(userBatteryMemberCardUpdate.getMemberCardExpireTime() / 24 / 60 / 60 / 1000);
                newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
            }
        }

        EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                .operateUid(SecurityUtils.getUid())
                .uid(electricityMemberCardOrder.getUid())
                .name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername())
                .rentUnit(batteryMemberCard.getRentUnit())
                .oldValidDays(oldValidDays)
                .newValidDays(newValidDays)
                .oldMaxUseCount(oldMaxUseCount)
                .newMaxUseCount(newMaxUseCount)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);

        return electricityMemberCardOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> editUserBatteryMemberCard(UserBatteryMembercardQuery query) {

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

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBER ORDER WARN! user exist battery service fee,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false,"ELECTRICITY.100000", "存在电池服务费");
        }

        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        if (Objects.nonNull(query.getMemberCardExpireTime()) || Objects.nonNull(query.getValidDays())) {
            if (Objects.isNull(query.getUseCount())) {
                //不限次套餐
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
                    userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                } else {
                    Long tempTime = Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() - userBatteryMemberCard.getOrderExpireTime() : (System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays())) - userBatteryMemberCard.getOrderExpireTime();

                    userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + tempTime);
                }
            } else {
                //限次套餐
                if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || userBatteryMemberCard.getRemainingNumber() <= 0) {
                    userBatteryMemberCardUpdate.setOrderRemainingNumber(query.getUseCount());
                    userBatteryMemberCardUpdate.setRemainingNumber(query.getUseCount());
                    userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                } else {
                    Long tempTime = Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() - userBatteryMemberCard.getOrderExpireTime() : (System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays())) - userBatteryMemberCard.getOrderExpireTime();

                    Long tempUseCount = query.getUseCount() - userBatteryMemberCard.getOrderRemainingNumber();

                    userBatteryMemberCardUpdate.setOrderRemainingNumber(query.getUseCount());
                    userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + tempUseCount);
                    userBatteryMemberCardUpdate.setOrderExpireTime(Objects.isNull(query.getValidDays()) ? query.getMemberCardExpireTime() : System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, query.getValidDays()));
                    userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + tempTime);
                }
            }
        }

        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

        if (Objects.nonNull(query.getMemberCardExpireTime()) || Objects.nonNull(query.getValidDays())) {
            int oldValidDays = 0;
            int newValidDays = 0;
            long oldMaxUseCount = 0L;
            long newMaxUseCount = 0L;

            if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && !Objects.equals(userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
                oldValidDays = Math.toIntExact((userBatteryMemberCard.getMemberCardExpireTime() / 24 / 60 / 60 / 1000));
                oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
            } else {
                newValidDays = Math.toIntExact(userBatteryMemberCardUpdate.getMemberCardExpireTime() / 24 / 60 / 60 / 1000);
                newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
            }

            EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder()
                    .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                    .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                    .operateUid(SecurityUtils.getUid())
                    .uid(userInfo.getUid())
                    .name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername())
                    .rentUnit(batteryMemberCard.getRentUnit())
                    .oldValidDays(oldValidDays)
                    .newValidDays(newValidDays)
                    .oldMaxUseCount(oldMaxUseCount)
                    .newMaxUseCount(newMaxUseCount)
                    .tenantId(TenantContextHolder.getTenantId())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
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

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> renewalUserBatteryMemberCard(UserBatteryMembercardQuery query) {
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

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if(Objects.isNull(userBatteryDeposit)){
            log.warn("ELE DEPOSIT WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }

        //是否有正在进行中的退押
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", userInfo.getUid());
            return Triple.of(false,"ELECTRICITY.0047", "电池押金退款中");
        }

        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if(CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)){
            log.warn("ELE DEPOSIT WARN! battery membercard refund review,uid={}", userInfo.getUid());
            return Triple.of(false,"100018", "套餐租金退款审核中");
        }

        BatteryMemberCard userBindbatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
//        if(Objects.isNull(userBindbatteryMemberCard)){
//            return Triple.of(false, "ELECTRICITY.00121", "用户电池套餐不存在");
//        }

        Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBER ORDER WARN! user exist battery service fee,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false,"ELECTRICITY.100000", "存在电池服务费");
        }

        ElectricityMemberCardOrder memberCardOrder=saveRenewalUserBatteryMemberCardOrder(user,userInfo,batteryMemberCard,userBatteryMemberCard,userBindbatteryMemberCard);

        //更新用户电池型号
//        userBatteryTypeService.updateUserBatteryType(memberCardOrder, userInfo);

        ChannelActivityHistory channelActivityHistory = channelActivityHistoryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(channelActivityHistory) && Objects
                .equals(channelActivityHistory.getStatus(), ChannelActivityHistory.STATUS_INIT)) {
            ChannelActivityHistory updateChannelActivityHistory = new ChannelActivityHistory();
            updateChannelActivityHistory.setId(channelActivityHistory.getId());
            updateChannelActivityHistory.setStatus(ChannelActivityHistory.STATUS_SUCCESS);
            updateChannelActivityHistory.setUpdateTime(System.currentTimeMillis());
            channelActivityHistoryService.update(updateChannelActivityHistory);
        }

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

        sendUserCoupon(batteryMemberCard, memberCardOrder);

        return Triple.of(true, null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public ElectricityMemberCardOrder saveRenewalUserBatteryMemberCardOrder(User user, UserInfo userInfo, BatteryMemberCard batteryMemberCard, UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard userBindbatteryMemberCard) {
        ElectricityMemberCardOrder memberCardOrder = new ElectricityMemberCardOrder();
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

        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || Objects.isNull(userBindbatteryMemberCard) || Objects.equals(userBindbatteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0) {

            memberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);

            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
            userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
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

            //如果用户原来绑定的有套餐 套餐过期了，需要把原来绑定的套餐订单状态更新为已过期
            if (StringUtils.isNotBlank(userBatteryMemberCard.getOrderId())) {
                ElectricityMemberCardOrder electricityMemberCardOrderUpdateUseStatus = new ElectricityMemberCardOrder();
                electricityMemberCardOrderUpdateUseStatus.setOrderId(userBatteryMemberCard.getOrderId());
                electricityMemberCardOrderUpdateUseStatus.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                electricityMemberCardOrderUpdateUseStatus.setUpdateTime(System.currentTimeMillis());
                electricityMemberCardOrderService.updateStatusByOrderNo(electricityMemberCardOrderUpdateUseStatus);
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
            userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime() + batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard, memberCardOrder));
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber() + memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setCardPayCount(queryMaxPayCount(userBatteryMemberCard) + 1);
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        }

        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setTenantId(userInfo.getTenantId());
        serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(userBatteryMemberCardUpdate.getMemberCardExpireTime());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(userInfo.getUid());
        userInfoUpdate.setPayCount(userInfo.getPayCount()+1);
        userInfoUpdate.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(userInfoUpdate);

        this.insert(memberCardOrder);

        int oldValidDays = 0;
        int newValidDays = 0;
        long oldMaxUseCount = 0L;
        long newMaxUseCount = 0L;
        if (Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY) && Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && !Objects.equals(userBatteryMemberCard.getMemberCardExpireTime(), NumberConstant.ZERO_L)) {
            oldValidDays = Math.toIntExact((userBatteryMemberCard.getMemberCardExpireTime() / 24 / 60 / 60 / 1000));
            oldMaxUseCount = userBatteryMemberCard.getRemainingNumber();
        } else {
            newValidDays = Math.toIntExact(userBatteryMemberCardUpdate.getMemberCardExpireTime() / 24 / 60 / 60 / 1000);
            newMaxUseCount = userBatteryMemberCardUpdate.getRemainingNumber();
        }

        EleUserOperateRecord eleUserMembercardOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                .operateUid(SecurityUtils.getUid())
                .uid(userInfo.getUid())
                .name(Objects.isNull(SecurityUtils.getUserInfo()) ? "" : SecurityUtils.getUserInfo().getUsername())
                .rentUnit(batteryMemberCard.getRentUnit())
                .oldValidDays(oldValidDays)
                .newValidDays(newValidDays)
                .oldMaxUseCount(oldMaxUseCount)
                .newMaxUseCount(newMaxUseCount)
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserMembercardOperateRecord);

        return memberCardOrder;
    }

    @Override
    public void sendUserCoupon(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder) {
        if (Objects.isNull(batteryMemberCard.getCouponId())) {
            return;
        }

        //发送优惠券
        UserCouponDTO userCouponDTO = new UserCouponDTO();
        userCouponDTO.setCouponId(batteryMemberCard.getCouponId().longValue());
        userCouponDTO.setUid(memberCardOrder.getUid());
        userCouponDTO.setSourceOrderNo(memberCardOrder.getOrderId());
        userCouponDTO.setTraceId(IdUtil.simpleUUID());
        userCouponService.asyncSendCoupon(userCouponDTO);
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
            batteryMemberCardVO.setEditUserMembercard(System.currentTimeMillis() > (userMemberCardOrder.getCreateTime() + batteryMemberCard.getRefundLimit() * 24 * 60 * 60 * 1000L));
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

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || StringUtils.isBlank(userBatteryDeposit.getOrderId())) {
            log.warn("ELE WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }

        userBatteryMemberCardInfoVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            log.warn("ELE WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }

        userBatteryMemberCardInfoVO.setIsExistMemberCard(UserBatteryMemberCardInfoVO.YES);
        userBatteryMemberCardInfoVO.setMemberCardStatus(userBatteryMemberCard.getMemberCardStatus());
        userBatteryMemberCardInfoVO.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime());
        userBatteryMemberCardInfoVO.setRemainingNumber(userBatteryMemberCard.getRemainingNumber());
        userBatteryMemberCardInfoVO.setMemberCardId(userBatteryMemberCard.getMemberCardId());

        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("ELE WARN! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(true, null, userBatteryMemberCardInfoVO);
        }

        //套餐订单金额
        ElectricityMemberCardOrder electricityMemberCardOrder = this.selectByOrderNo(userBatteryMemberCard.getOrderId());
        userBatteryMemberCardInfoVO.setBatteryMembercardPayAmount(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getPayAmount());
        userBatteryMemberCardInfoVO.setMemberCardPayTime(Objects.isNull(electricityMemberCardOrder) ? null : electricityMemberCardOrder.getCreateTime());

        userBatteryMemberCardInfoVO.setValidDays(batteryMemberCard.getValidDays());
        userBatteryMemberCardInfoVO.setMemberCardName(batteryMemberCard.getName());
        userBatteryMemberCardInfoVO.setRentUnit(batteryMemberCard.getRentUnit());
        userBatteryMemberCardInfoVO.setLimitCount(batteryMemberCard.getLimitCount());

        //用户电池型号
        userBatteryMemberCardInfoVO.setUserBatterySimpleType(userBatteryTypeService.selectUserSimpleBatteryType(userInfo.getUid()));

        return Triple.of(true, null, userBatteryMemberCardInfoVO);
    }

    @Override
    public Integer queryMaxPayCount(UserBatteryMemberCard userBatteryMemberCard) {
        return Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) ? 0
                : userBatteryMemberCard.getCardPayCount();
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
    public Long handlerMembercardBindActivity(ElectricityMemberCard electricityMemberCard, UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo, Long remainingNumber) {
        if (Objects.isNull(electricityMemberCard) || Objects.isNull(electricityMemberCard.getActivityId()) || !Objects.equals(electricityMemberCard.getIsBindActivity(), ElectricityMemberCard.BIND_ACTIVITY)) {
            return remainingNumber;
        }

        OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCard.getActivityId());
        if (Objects.isNull(oldUserActivity)) {
            log.error("MEMBERCARD ACTIVITY ERROR!oldUserActivity is null,uid={},activityId={}", userInfo.getUid(), electricityMemberCard.getActivityId());
            return remainingNumber;
        }

        //判断是否能够参与套餐活动
        if (!isCanJoinMembercardActivity(userBatteryMemberCard, oldUserActivity)) {
            return remainingNumber;
        }

        //送次数
        if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUNT) && Objects.nonNull(oldUserActivity.getCount())) {
            remainingNumber = remainingNumber + oldUserActivity.getCount();
        }

        //送优惠券
        if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {
            userCouponService.batchRelease(oldUserActivity.getCouponId(), ArraysUtil.array(userInfo.getUid()));
        }

        return remainingNumber;
    }

    private boolean isCanJoinMembercardActivity(UserBatteryMemberCard userBatteryMemberCard, OldUserActivity oldUserActivity) {

        if (Objects.equals(oldUserActivity.getUserScope(), OldUserActivity.USER_SCOPE_ALL)) {//套餐活动 用户范围为全部用户
            return Boolean.TRUE;
        } else if (Objects.equals(oldUserActivity.getUserScope(), OldUserActivity.USER_SCOPE_NEW) &&//套餐活动 用户范围为新用户
                (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getCardPayCount()) || userBatteryMemberCard.getCardPayCount() == 0)) {
            return Boolean.TRUE;
        } else if (Objects.equals(oldUserActivity.getUserScope(), OldUserActivity.USER_SCOPE_OLD) &&//套餐活动 用户范围为老用户
                Objects.nonNull(userBatteryMemberCard) && Objects.nonNull(userBatteryMemberCard.getCardPayCount()) && userBatteryMemberCard.getCardPayCount() > 0) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Triple<Boolean, String, Object> calculatePayAmount(BigDecimal price, Set<Integer> userCouponIds) {
        BigDecimal payAmount = price;

        if (CollectionUtils.isEmpty(userCouponIds)) {
            return Triple.of(true, null, payAmount);
        }

        for (Integer userCouponId : userCouponIds) {
            UserCoupon userCoupon = userCouponService.queryByIdFromDB(userCouponId);
            if (Objects.isNull(userCoupon)) {
                log.error("ELE ERROR! not found userCoupon,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }

            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.error("ELE ERROR! not found coupon,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }

            //优惠券是否使用
            if (!Objects.equals(UserCoupon.STATUS_UNUSED, userCoupon.getStatus())) {
                log.error("ELE ERROR! userCoupon is used,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0090", "优惠券不可用");
            }

            //优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.error("ELE ERROR! userCoupon is deadline,userCouponId={}", userCouponId);
                return Triple.of(false, "ELECTRICITY.0091", "您的优惠券已过期");
            }

            //使用折扣劵  折扣券作废，不处理——>产品提的需求
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.DISCOUNT)) {
                log.info("ELE INFO! not found coupon,userCouponId={}", userCouponId);
            }

            //使用满减劵
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
        if(CollectionUtils.isEmpty(orderIds)){
            return NumberConstant.ZERO;
        }
        return this.baseMapper.batchUpdateStatusByOrderNo(orderIds,useStatus);
    }

    @Override
    public Integer checkOrderByMembercardId(Long membercardId) {
        return baseMapper.checkOrderByMembercardId(membercardId);
    }

    public void handlerBatteryMembercardZeroPayment(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder, UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo) {

        //用户未绑定套餐
        if(Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId() , NumberConstant.ZERO_L)){
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
            userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis()+batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis()+batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setOrderRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);

        }else{
            //用户已绑定套餐
            UserBatteryMemberCardPackage userBatteryMemberCardPackage=new UserBatteryMemberCardPackage();
            userBatteryMemberCardPackage.setUid(userInfo.getUid());
            userBatteryMemberCardPackage.setMemberCardId(memberCardOrder.getMemberCardId());
            userBatteryMemberCardPackage.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardPackage.setRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardPackage.setMemberCardExpireTime(batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardPackage.setTenantId(userInfo.getTenantId());
            userBatteryMemberCardPackage.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardPackage.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardPackageService.insert(userBatteryMemberCardPackage);

            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime()+batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber()+memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);


            //获取用户电池型号
            List<String> userBatteryTypes = acquireUserBatteryType(userBatteryTypeService.selectByUid(userInfo.getUid()),memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId()));
            if(CollectionUtils.isNotEmpty(userBatteryTypes)){
                //更新用户电池型号
                userBatteryTypeService.deleteByUid(userInfo.getUid());
                userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(userBatteryTypes,userInfo));
            }
        }




        //TODO 发送MQ 更新优惠券状态 处理活动 分帐 相关

    }

    private List<String> acquireUserBatteryType(List<String> userBatteryTypeList, List<String> membercardBatteryTypeList) {
        if (CollectionUtils.isEmpty(membercardBatteryTypeList)) {
            return userBatteryTypeList;
        }

        if(CollectionUtils.isEmpty(userBatteryTypeList)){
            return Collections.emptyList();
        }

        Set<String> result = new HashSet<>();
        result.addAll(userBatteryTypeList);
        result.addAll(userBatteryTypeList);

        return new ArrayList<>(result);
    }

    @Override
    public void handlerBatteryMembercardPaymentNotify(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder, UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo) {
        //用户未绑定套餐
        if(Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId()) || Objects.equals(userBatteryMemberCard.getMemberCardId() , NumberConstant.ZERO_L)){
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardId(batteryMemberCard.getId());
            userBatteryMemberCardUpdate.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis()+batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardUpdate.setOrderExpireTime(System.currentTimeMillis()+batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setOrderRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setRemainingNumber(memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardUpdate.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.insert(userBatteryMemberCardUpdate);

        }else{
            //用户已绑定套餐
            UserBatteryMemberCardPackage userBatteryMemberCardPackage=new UserBatteryMemberCardPackage();
            userBatteryMemberCardPackage.setUid(userInfo.getUid());
            userBatteryMemberCardPackage.setMemberCardId(memberCardOrder.getMemberCardId());
            userBatteryMemberCardPackage.setOrderId(memberCardOrder.getOrderId());
            userBatteryMemberCardPackage.setRemainingNumber(batteryMemberCard.getUseCount());
            userBatteryMemberCardPackage.setMemberCardExpireTime(batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardPackage.setTenantId(userInfo.getTenantId());
            userBatteryMemberCardPackage.setCreateTime(System.currentTimeMillis());
            userBatteryMemberCardPackage.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardPackageService.insert(userBatteryMemberCardPackage);

            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            userBatteryMemberCardUpdate.setMemberCardExpireTime(userBatteryMemberCard.getMemberCardExpireTime()+batteryMemberCardService.transformBatteryMembercardEffectiveTime(batteryMemberCard,memberCardOrder));
            userBatteryMemberCardUpdate.setRemainingNumber(userBatteryMemberCard.getRemainingNumber()+memberCardOrder.getMaxUseCount());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);


            //获取用户电池型号
            List<String> userBatteryTypes = acquireUserBatteryType(userBatteryTypeService.selectByUid(userInfo.getUid()),memberCardBatteryTypeService.selectBatteryTypeByMid(batteryMemberCard.getId()));
            if(CollectionUtils.isNotEmpty(userBatteryTypes)){
                //更新用户电池型号
                userBatteryTypeService.deleteByUid(userInfo.getUid());
                userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(userBatteryTypes,userInfo));
            }
        }


        //TODO 发送MQ 更新优惠券状态 处理活动 分帐 相关

    }

    private Long calculateMembercardTime(BatteryMemberCard batteryMemberCard, ElectricityMemberCardOrder memberCardOrder){
        return Objects.equals(batteryMemberCard.getRentUnit(), BatteryMemberCard.RENT_UNIT_DAY) ? memberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L) : memberCardOrder.getValidDays() * (60 * 1000L);
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

    private Triple<Boolean, String, Object> handlerNonFirstBuyBatteryMemberCard(ElectricityMemberCardOrderQuery query,UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, UserInfo userInfo,Franchisee franchisee) {
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("BATTERY MEMBER ORDER WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(false, "100247", "用户信息不存在");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("BATTERY MEMBER ORDER WARN! userBatteryMemberCard is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "100241", "当前套餐暂停中，请先启用套餐");
        }

        if (!(Objects.equals(BatteryMemberCard.RENT_TYPE_OLD, batteryMemberCard.getRentType()) || Objects.equals(BatteryMemberCard.RENT_TYPE_UNLIMIT, batteryMemberCard.getRentType()))) {
            log.warn("BATTERY MEMBER ORDER WARN! new batteryMemberCard not available,uid={},mid={}", userInfo.getUid(), batteryMemberCard.getId());
            return Triple.of(false, "100275", "换电套餐不可用");
        }

        BatteryMemberCard userBindBatteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(userBindBatteryMemberCard)) {
            log.warn("BATTERY MEMBER ORDER WARN! userBindBatteryMemberCard is null,uid={}", userBatteryMemberCard.getUid());
            return Triple.of(false, "ELECTRICITY.0087", "套餐不存在");
        }

//        if (!Objects.equals(userBindBatteryMemberCard.getLimitCount(), batteryMemberCard.getLimitCount())) {
//            log.warn("BATTERY MEMBER ORDER WARN! batteryMemberCard limitCount inconformity,uid={},mid={}", userBatteryMemberCard.getUid(), batteryMemberCard.getId());
//            return Triple.of(false, "100276", "换电套餐类型不一致");
//        }

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

    private Triple<Boolean, String, Object> handlerFirstBuyBatteryMemberCard(ElectricityMemberCardOrderQuery query,UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard, UserInfo userInfo,Franchisee franchisee) {
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.warn("BATTERY MEMBER ORDER WARN! not allow buy,uid={}", userInfo.getUid());
            return Triple.of(false, "100274", "赠送套餐不允许续费");
        }

        if (!(Objects.equals(BatteryMemberCard.RENT_TYPE_NEW, batteryMemberCard.getRentType()) || Objects.equals(BatteryMemberCard.RENT_TYPE_UNLIMIT, batteryMemberCard.getRentType()))) {
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

    private Triple<Boolean, String, Object> verifyUserBatteryInsurance(UserInfo userInfo, Franchisee franchisee,BatteryMemberCard batteryMemberCard) {

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

            String batteryType=batteryTypeList.get(0);

            batteryV = batteryType.substring(batteryType.indexOf("_") + 1).substring(0, batteryType.substring(batteryType.indexOf("_") + 1).indexOf("_"));
        }

        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.selectByFranchiseeIdAndType(userInfo.getFranchiseeId(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY, batteryV);
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getIsConstraint(), FranchiseeInsurance.CONSTRAINT_FORCE)) {
            return Triple.of(true, null, null);
        }

        //用户是否没有保险信息或已过期
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.isNull(insuranceUserInfo) || !Objects.equals(insuranceUserInfo.getIsUse(), InsuranceUserInfo.NOT_USE) || insuranceUserInfo.getInsuranceExpireTime() < System.currentTimeMillis()) {
            log.warn("BATTERY MEMBER ORDER WARN! not pay battery insurance,uid={}", userInfo.getUid());
            return Triple.of(false, "100309", "未购买保险或保险已过期");
        }

        return Triple.of(true, null, null);
    }
}
