package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MqConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.manager.CalcRentCarPriceFactory;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
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

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.ognl.ObjectElementsAccessor;
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

    /**
     * 创建月卡订单
     *
     * @param
     * @param electricityMemberCardOrderQuery
     * @return
     */
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

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());

        //判断是否缴纳押金
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.error("CREATE MEMBER_ORDER ERROR! not pay deposit! uid={} ", user.getUid());
            return R.fail("100241", "当前套餐暂停中，请先启用套餐");
        }

        Long now = System.currentTimeMillis();

        //判断用户电池服务费
        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            long cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            BigDecimal userMemberCardExpireServiceFee = checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
            if (BigDecimal.valueOf(0).compareTo(userMemberCardExpireServiceFee) != 0) {
                return R.fail("ELECTRICITY.100000", "用户存在电池服务费", userMemberCardExpireServiceFee);
            }
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(electricityMemberCardOrderQuery.getMemberId());
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

        if (Objects.nonNull(electricityMemberCardOrderQuery.getProductKey())
                && Objects.nonNull(electricityMemberCardOrderQuery.getDeviceName())) {
            //换电柜
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(electricityMemberCardOrderQuery.getProductKey(), electricityMemberCardOrderQuery.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.error("rentBattery  ERROR! not found electricityCabinet ！productKey={},deviceName={}", electricityMemberCardOrderQuery.getProductKey(), electricityMemberCardOrderQuery.getDeviceName());
                return R.fail("ELECTRICITY.0005", "未找到换电柜");
            }

            //3、查出套餐
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("queryByDevice  ERROR! not found store ！electricityCabinetId={}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("queryByDevice  ERROR! not found store ！storeId={}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("queryByDevice  ERROR! not found Franchisee ！storeId={}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }
            franchiseeId = store.getFranchiseeId();
        }

        if (Objects.isNull(franchiseeId)) {
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        //查找计算优惠券
        //满减折扣劵
        UserCoupon userCoupon = null;
        BigDecimal payAmount = electricityMemberCard.getHolidayPrice();
        if (Objects.nonNull(electricityMemberCardOrderQuery.getUserCouponId())) {
            userCoupon = userCouponService.queryByIdFromDB(electricityMemberCardOrderQuery.getUserCouponId());
            if (Objects.isNull(userCoupon)) {
                log.error("ELECTRICITY  ERROR! not found userCoupon! userCouponId={} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0085", "未找到优惠券");
            }

            //优惠券是否使用
            if (Objects.equals(UserCoupon.STATUS_USED, userCoupon.getStatus())) {
                log.error("ELECTRICITY  ERROR!  userCoupon is used! userCouponId={} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0090", "您的优惠券已被使用");
            }

            //优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.error("ELECTRICITY  ERROR!  userCoupon is deadline!userCouponId={} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0091", "您的优惠券已过期");
            }

            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.error("ELECTRICITY  ERROR! not found coupon! userCouponId={} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0085", "未找到优惠券");
            }

            //使用满减劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.FULL_REDUCTION)) {

                //计算满减
                payAmount = payAmount.subtract(coupon.getAmount());
            }

            //使用折扣劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.DISCOUNT)) {

                //计算折扣
                payAmount = payAmount.multiply(coupon.getDiscount().divide(BigDecimal.valueOf(100)));
            }

        }

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
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        if (Objects.nonNull(electricityMemberCardOrderQuery.getUserCouponId())) {
            electricityMemberCardOrder.setCouponId(electricityMemberCardOrderQuery.getUserCouponId().longValue());
        }
        baseMapper.insert(electricityMemberCardOrder);


        //支付零元
        if (electricityMemberCardOrder.getPayAmount().compareTo(BigDecimal.valueOf(0.01)) < 0) {

            //月卡是否绑定活动
            if (Objects.equals(electricityMemberCard.getIsBindActivity(), ElectricityMemberCard.BIND_ACTIVITY) && Objects.nonNull(electricityMemberCard.getActivityId())) {
                OldUserActivity oldUserActivity = oldUserActivityService.queryByIdFromCache(electricityMemberCard.getActivityId());

                if (Objects.nonNull(oldUserActivity)) {

                    //次数
                    if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUNT) && Objects.nonNull(oldUserActivity.getCount())) {
                        remainingNumber = remainingNumber + oldUserActivity.getCount();
                    }

                    //优惠券
                    if (Objects.equals(oldUserActivity.getDiscountType(), OldUserActivity.TYPE_COUPON) && Objects.nonNull(oldUserActivity.getCouponId())) {
                        //发放优惠券
                        Long[] uids = new Long[1];
                        uids[0] = electricityMemberCardOrder.getUid();
                        userCouponService.batchRelease(oldUserActivity.getCouponId(), uids);
                    }
                }
            }


            //用户套餐
            UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
            userBatteryMemberCardUpdate.setUid(userInfo.getUid());
            Long memberCardExpireTime = now + electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
            userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
            userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber.intValue());
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardId(electricityMemberCardOrder.getMemberCardId().longValue());
            userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
            userBatteryMemberCardUpdate.setTenantId(userInfo.getTenantId());
            userBatteryMemberCardUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
            userBatteryMemberCardService.insertOrUpdate(userBatteryMemberCardUpdate);

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
                serviceFeeUserInfoInsertOrUpdate.setExistBatteryServiceFee(ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE);
                serviceFeeUserInfoService.insert(serviceFeeUserInfoInsertOrUpdate);
            } else {
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoInsertOrUpdate);
            }


            //月卡订单
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            baseMapper.updateById(electricityMemberCardOrderUpdate);

            //修改优惠券状态为已使用
            if (Objects.nonNull(electricityMemberCardOrderQuery.getUserCouponId())) {
                //修改劵可用状态
                userCoupon.setStatus(UserCoupon.STATUS_USED);
                userCoupon.setUpdateTime(System.currentTimeMillis());
                userCoupon.setOrderId(electricityMemberCardOrder.getOrderId());
                userCouponService.update(userCoupon);
            }

            //被邀请新买月卡用户
            //是否是新用户
            if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardId())) {
                //是否有人邀请
                JoinShareActivityRecord joinShareActivityRecord = joinShareActivityRecordService.queryByJoinUid(user.getUid());
                if (Objects.nonNull(joinShareActivityRecord)) {
                    //修改邀请状态
                    joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_SUCCESS);
                    joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareActivityRecordService.update(joinShareActivityRecord);

                    //修改历史记录状态
                    JoinShareActivityHistory oldJoinShareActivityHistory = joinShareActivityHistoryService.queryByRecordIdAndStatus(joinShareActivityRecord.getId());
                    if (Objects.nonNull(oldJoinShareActivityHistory)) {
                        oldJoinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_SUCCESS);
                        oldJoinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
                        joinShareActivityHistoryService.update(oldJoinShareActivityHistory);
                    }

                    //给邀请人增加邀请成功人数
                    shareActivityRecordService.addCountByUid(joinShareActivityRecord.getUid());
                }

                //是否有人返现邀请
                JoinShareMoneyActivityRecord joinShareMoneyActivityRecord = joinShareMoneyActivityRecordService.queryByJoinUid(user.getUid());
                if (Objects.nonNull(joinShareMoneyActivityRecord)) {
                    //修改邀请状态
                    joinShareMoneyActivityRecord.setStatus(JoinShareMoneyActivityRecord.STATUS_SUCCESS);
                    joinShareMoneyActivityRecord.setUpdateTime(System.currentTimeMillis());
                    joinShareMoneyActivityRecordService.update(joinShareMoneyActivityRecord);

                    //修改历史记录状态
                    JoinShareMoneyActivityHistory oldJoinShareMoneyActivityHistory = joinShareMoneyActivityHistoryService.queryByRecordIdAndStatus(joinShareMoneyActivityRecord.getId());
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

            return R.ok();
        }

        //修改优惠券状态为正在核销中
        if (Objects.nonNull(electricityMemberCardOrderQuery.getUserCouponId())) {
            //修改劵可用状态
            userCoupon.setStatus(UserCoupon.STATUS_IS_BEING_VERIFICATION);
            userCoupon.setUpdateTime(System.currentTimeMillis());
            userCoupon.setOrderId(electricityMemberCardOrder.getOrderId());
            userCouponService.update(userCoupon);
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
    public BigDecimal homeOne(Long first, Long now, List<Integer> cardIdList, Integer tenantId) {
        return baseMapper.homeOne(first, now, cardIdList, tenantId);
    }

    @Override
    public List<HashMap<String, String>> homeTwo(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> cardIdList, Integer tenantId) {
        return baseMapper.homeTwo(startTimeMilliDay, endTimeMilliDay, cardIdList, tenantId);
    }

    @Override
    public R queryUserList(Long offset, Long size, Long startTime, Long endTime) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return R.ok(baseMapper.queryUserList(user.getUid(), offset, size, startTime, endTime));
    }

    /**
     * 获取交易次数
     *
     * @param uid
     * @return
     */
    @Override
    public R getMemberCardOrderCount(Long uid, Long startTime, Long endTime) {
        return R.ok(baseMapper.getMemberCardOrderCount(uid, startTime, endTime));
    }

    @Override
    @DS("slave_1")
    public R queryList(MemberCardOrderQuery memberCardOrderQuery) {
        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = baseMapper.queryList(memberCardOrderQuery);
        if (ObjectUtil.isEmpty(electricityMemberCardOrderVOList)) {
            return R.ok(baseMapper.queryList(memberCardOrderQuery));
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

            ElectricityMemberCardOrderVOs.add(electricityMemberCardOrderVO);
        }

        return R.ok(ElectricityMemberCardOrderVOs);


    }

    @Override
    public void exportExcel(MemberCardOrderQuery memberCardOrderQuery, HttpServletResponse response) {
//        memberCardOrderQuery.setOffset(0L);
//        memberCardOrderQuery.setSize(2000L);
//        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = baseMapper.queryList(memberCardOrderQuery);
//        if (ObjectUtil.isEmpty(electricityMemberCardOrderVOList)) {
//            throw new CustomBusinessException("查不到订单");
//        }
//
//        List<ElectricityMemberCardOrderExcelVO> electricityMemberCardOrderExcelVOS = new ArrayList();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        int index = 0;
//        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
//            index++;
//            ElectricityMemberCardOrderExcelVO excelVo = new ElectricityMemberCardOrderExcelVO();
//            excelVo.setId(index);
//            excelVo.setOrderId(electricityMemberCardOrderVO.getOrderId());
//            excelVo.setPhone(electricityMemberCardOrderVO.getPhone());
//            excelVo.setPayAmount(electricityMemberCardOrderVO.getPayAmount());
//
//            if (Objects.nonNull(electricityMemberCardOrderVO.getUpdateTime())) {
//                excelVo.setBeginningTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime())));
//                if (Objects.nonNull(electricityMemberCardOrderVO.getValidDays())) {
//                    excelVo.setEndTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime() + electricityMemberCardOrderVO.getValidDays() * 24 * 60 * 60 * 1000)));
//                }
//            }
//
//            if (Objects.isNull(electricityMemberCardOrderVO.getMemberCardType())) {
//                excelVo.setMemberCardType("");
//            }
//            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_MONTH_CARD)) {
//                excelVo.setMemberCardType("月卡");
//            }
//            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_SEASON_CARD)) {
//                excelVo.setMemberCardType("季卡");
//            }
//            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_YEAR_CARD)) {
//                excelVo.setMemberCardType("年卡");
//            }
//
//            if (Objects.isNull(electricityMemberCardOrderVO.getStatus())) {
//                excelVo.setStatus("");
//            }
//            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_INIT)) {
//                excelVo.setStatus("未支付");
//            }
//            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS)) {
//                excelVo.setStatus("支付成功");
//            }
//            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_FAIL)) {
//                excelVo.setStatus("支付失败");
//            }
//
//            electricityMemberCardOrderExcelVOS.add(excelVo);
//        }

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
            excelVo.setFranchiseeName(electricityMemberCardOrders.get(i).getFranchiseeName());
            excelVo.setMemberCardName(electricityMemberCardOrders.get(i).getCardName());
            excelVo.setMaxUseCount(Objects.equals(electricityMemberCardOrders.get(i).getMaxUseCount(), -1L) ? "不限次" : String.valueOf(electricityMemberCardOrders.get(i).getMaxUseCount()));
            excelVo.setValidDays(electricityMemberCardOrders.get(i).getValidDays());
            excelVo.setStatus(Objects.equals(electricityMemberCardOrders.get(i).getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS) ? "已支付" : "未支付");
            excelVo.setPayAmount(electricityMemberCardOrders.get(i).getPayAmount());
            excelVo.setPayType(Objects.equals(electricityMemberCardOrders.get(i).getPayType(), ElectricityMemberCardOrder.ONLINE_PAYMENT) ? "线上支付" : "线下支付");
            excelVo.setBeginningTime(DateUtil.format(DateUtil.date(electricityMemberCardOrders.get(i).getCreateTime()), DatePattern.NORM_DATETIME_PATTERN));

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

    @Override
    public R queryCount(MemberCardOrderQuery memberCardOrderQuery) {
        return R.ok(baseMapper.queryCount(memberCardOrderQuery));
    }

    @Override
    public Integer queryCountForScreenStatistic(MemberCardOrderQuery memberCardOrderQuery) {
        return baseMapper.queryCount(memberCardOrderQuery);
    }

    @Override
    public BigDecimal queryTurnOver(Integer tenantId, Long uid) {
        return Optional.ofNullable(baseMapper.queryTurnOver(tenantId, uid)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R openOrDisableMemberCard(Integer usableStatus) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //限频
        Boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DISABLE_MEMBER_CARD_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁,请稍后再试!");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("DISABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
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

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.error("DISABLE MEMBER CARD ERROR! not apply disable membercard,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }


        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("DISABLE MEMBER CARD ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("DISABLE MEMBER CARD ERROR! disable review userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

//        //判断套餐是否为新用户送的次数卡
//        if (Objects.equals(electricityMemberCard.getType(), ElectricityMemberCard.TYPE_COUNT)) {
//            log.error("DISABLE MEMBER CARD ERROR! uid={} ", user.getUid());
//            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
//        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }


        //判断用户是否产生电池服务费
        Long now = System.currentTimeMillis();
        Long cardDays = 0L;
        if (Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {
            cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1L;
            }
        }

        //启用月卡时判断用户是否有电池，收取服务费
        if (Objects.equals(usableStatus, UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            BigDecimal batteryServiceFee = checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, null, null);
            if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                return R.fail("ELECTRICITY.100000", "用户启用月卡存在电池服务费", batteryServiceFee);
            }
        }

        Long memberCardExpireTime = null;

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(user.getUid());

        if (Objects.equals(usableStatus, EleDisableMemberCardRecord.MEMBER_CARD_DISABLE)) {
            usableStatus = EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW;
            EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                    .disableMemberCardNo(generateOrderId(user.getUid()))
                    .memberCardName(electricityMemberCard.getName())
                    .phone(userInfo.getPhone())
                    .userName(userInfo.getName())
                    .status(usableStatus)
                    .tenantId(userInfo.getTenantId())
                    .uid(user.getUid())
                    .disableCardTimeType(EleDisableMemberCardRecord.DISABLE_CARD_NOT_LIMIT_TIME)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            BigDecimal batteryServiceFee = checkDifferentModelBatteryServiceFee(franchisee, userInfo, null);
            eleDisableMemberCardRecord.setChargeRate(batteryServiceFee);
            eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

            Integer existServiceFee = ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE;
            if (Objects.equals(franchisee.getIsOpenServiceFee(), Franchisee.OPEN_SERVICE_FEE) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                existServiceFee = ServiceFeeUserInfo.EXIST_SERVICE_FEE;
            }

            ServiceFeeUserInfo insertOrUpdateServiceFeeUserInfo = ServiceFeeUserInfo.builder()
                    .existBatteryServiceFee(existServiceFee)
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .tenantId(eleDisableMemberCardRecord.getTenantId())
                    .uid(user.getUid())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            if (Objects.isNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoService.insert(insertOrUpdateServiceFeeUserInfo);
            } else {
                serviceFeeUserInfoService.updateByUid(insertOrUpdateServiceFeeUserInfo);
            }
        } else {
            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(), user.getTenantId());
            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(electricityMemberCard.getName())
                    .enableTime(System.currentTimeMillis())
                    .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .disableDays(cardDays.intValue())
                    .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                    .franchiseeId(userInfo.getFranchiseeId())
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
            serviceFeeUserInfoUpdate.setExistBatteryServiceFee(ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE);
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
            serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            serviceFeeUserInfoUpdate.setTenantId(serviceFeeUserInfo.getTenantId());
            serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);
        }


        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        if (Objects.equals(usableStatus, UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);
        } else {
            sendDisableMemberCardMessage(userInfo);
        }
        userBatteryMemberCardUpdate.setMemberCardStatus(usableStatus);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setUid(userInfo.getUid());
        userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUpdate);

        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R disableMemberCardForLimitTime(Integer disableCardDays, Long disableDeadline) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //限频
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
            log.error("DISABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
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


        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("DISABLE MEMBER CARD ERROR! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        //TODO 体验卡停卡提示套餐不存在
        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.error("DISABLE MEMBER CARD ERROR! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("DISABLE MEMBER CARD ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("DISABLE MEMBER CARD ERROR! disable review userId={}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("DISABLE MEMBER CARD ERROR! not found Franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                .disableMemberCardNo(generateOrderId(user.getUid()))
                .memberCardName(electricityMemberCard.getName())
                .phone(userInfo.getPhone())
                .userName(userInfo.getName())
                .status(EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW)
                .uid(userInfo.getUid())
                .tenantId(userInfo.getTenantId())
                .uid(user.getUid())
                .chooseDays(disableCardDays)
                .disableDeadline(disableDeadline)
                .disableCardTimeType(EleDisableMemberCardRecord.DISABLE_CARD_LIMIT_TIME)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        BigDecimal batteryServiceFee = checkDifferentModelBatteryServiceFee(franchisee, userInfo, null);
        eleDisableMemberCardRecord.setChargeRate(batteryServiceFee);
        eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

        Integer existServiceFee = ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE;
        if (Objects.equals(franchisee.getIsOpenServiceFee(), Franchisee.OPEN_SERVICE_FEE) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            existServiceFee = ServiceFeeUserInfo.EXIST_SERVICE_FEE;
        }

        ServiceFeeUserInfo insertOrUpdateServiceFeeUserInfo = ServiceFeeUserInfo.builder()
                .existBatteryServiceFee(existServiceFee)
                .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .franchiseeId(userInfo.getFranchiseeId())
                .tenantId(eleDisableMemberCardRecord.getTenantId())
                .uid(user.getUid())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            serviceFeeUserInfoService.insert(insertOrUpdateServiceFeeUserInfo);
        } else {
            serviceFeeUserInfoService.updateByUid(insertOrUpdateServiceFeeUserInfo);
        }


        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW);
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        return R.ok();
    }

    @Override
    public R enableMemberCardForLimitTime() {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //限频
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

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//
//        //未缴纳押金
//        if (Objects.isNull(franchiseeUserInfo)) {
//            log.error("ENABLE MEMBER CARD ERROR!not found user! userId={}", user.getUid());
//            return R.fail("ELECTRICITY.0042", "未缴纳押金");
//        }

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

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
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

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(electricityMemberCard.getType(), ElectricityMemberCard.TYPE_COUNT)) {
            log.error("ENABLE MEMBER CARD ERROR! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ENABLE  ERROR! not found Franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(serviceFeeUserInfo)) {
            log.error("ENABLE MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(user.getUid(), user.getTenantId());

        Integer serviceFeeStatus = EnableMemberCardRecord.STATUS_INIT;

        //判断用户是否产生电池服务费
        Long now = System.currentTimeMillis();
        Long cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

        //不足一天按一天计算
        double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
        if (time < 24) {
            cardDays = 1L;
        }

        BigDecimal batteryServiceFee = checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, eleDisableMemberCardRecord, serviceFeeUserInfo);
        if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
            return R.fail("ELECTRICITY.100000", "用户启用月卡存在电池服务费", batteryServiceFee);
        }

        EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                .memberCardName(electricityMemberCard.getName())
                .enableTime(System.currentTimeMillis())
                .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                .batteryServiceFeeStatus(serviceFeeStatus)
                .disableDays(cardDays.intValue())
                .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                .franchiseeId(userInfo.getFranchiseeId())
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
        userBatteryMemberCardUdpate.setMemberCardExpireTime(memberCardExpireTime);
        userBatteryMemberCardUdpate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardUdpate.setUpdateTime(System.currentTimeMillis());
        if (Objects.equals(serviceFeeUserInfo.getExistBatteryServiceFee(), ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE)) {
            userBatteryMemberCardUdpate.setDisableMemberCardTime(null);
        }
        userBatteryMemberCardService.updateByUidForDisableCard(userBatteryMemberCardUdpate);

        return R.ok();
    }

    @Override
    public R enableOrDisableMemberCardIsLimitTime() {
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

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//
//        //未缴纳押金
//        if (Objects.isNull(franchiseeUserInfo)) {
//            log.error("DISABLE MEMBER CARD ERROR!not found user! userId={}", user.getUid());
//            return R.fail("ELECTRICITY.0042", "未缴纳押金");
//        }
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
    public R adminOpenOrDisableMemberCard(Integer usableStatus, Long uid) {

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

        //
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//        if (Objects.isNull(franchiseeUserInfo) || !Objects.equals(franchiseeUserInfo.getTenantId(), TenantContextHolder.getTenantId())) {
//            log.error("DISABLE MEMBER CARD ERROR!not found user! userId:{}", user.getUid());
//            return R.fail("ELECTRICITY.0042", "未缴纳押金");
//        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_NO)) {
            log.error("admin saveUserMemberCard  ERROR! user is rent deposit,uid={} ", userInfo.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
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

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("admin saveUserMemberCard  ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("admin saveUserMemberCard  ERROR! disable review userId:{}", userInfo.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(electricityMemberCard.getType(), ElectricityMemberCard.TYPE_COUNT)) {
            log.error("admin saveUserMemberCard  ERROR! uid:{} ", userInfo.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }

        Long now = System.currentTimeMillis();
        if (Objects.equals(usableStatus, ElectricityMemberCard.STATUS_UN_USEABLE)) {
            if (now > userBatteryMemberCard.getMemberCardExpireTime()) {
                log.error("DISABLE MEMBER CARD ERROR! uid:{} ", userInfo.getUid());
                return R.fail("100013", "用户套餐已经过期");
            }
        }

        Franchisee franchisee = franchiseeService.queryByIdFromDB(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("DISABLE MEMBER CARD ERROR! not found franchisee ！franchiseeId={}", userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }

        //启用月卡时判断用户是否有电池，收取服务费
        if (Objects.equals(usableStatus, UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {

            //判断用户是否产生电池服务费
            Long cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1L;
            }

            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(), user.getTenantId());
            BigDecimal batteryServiceFee = checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, eleDisableMemberCardRecord, null);
            if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                return R.fail("ELECTRICITY.100000", "用户启用月卡存在电池服务费", batteryServiceFee);
            }

            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(electricityMemberCard.getName())
                    .enableTime(System.currentTimeMillis())
                    .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT)
                    .disableDays(cardDays.intValue())
                    .disableTime(eleDisableMemberCardRecord.getUpdateTime())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .phone(userInfo.getPhone())
                    .createTime(System.currentTimeMillis())
                    .tenantId(user.getTenantId())
                    .uid(uid)
                    .userName(userInfo.getName())
                    .updateTime(System.currentTimeMillis()).build();
            enableMemberCardRecordService.insert(enableMemberCardRecord);
        } else {

            BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);

            ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());

            long cardDays = 0;
//            if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
//                cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
//                //查询用户是否存在套餐过期电池服务费
//                BigDecimal serviceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, null, cardDays);
//                userChangeServiceFee = serviceFee;
//            }

            Long disableMemberCardTime = userBatteryMemberCard.getDisableMemberCardTime();

            //判断用户是否产生电池服务费
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {

                cardDays = (now - disableMemberCardTime) / 1000L / 60 / 60 / 24;

                //不足一天按一天计算
                double time = Math.ceil((now - disableMemberCardTime) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1;
                }
                BigDecimal serviceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, userInfo.getUid(), cardDays, null, serviceFeeUserInfo);
                userChangeServiceFee = serviceFee;
            }


            log.error("用户启用套餐存在电池服务费===================================" + userChangeServiceFee);

            if (BigDecimal.valueOf(0).compareTo(userChangeServiceFee) != 0) {
                log.error("DISABLE MEMBER CARD ERROR! user exist battery service fee ! uid={}", userInfo.getUid());
                return R.fail("ELECTRICITY.100000", "存在电池服务费", userChangeServiceFee);
            }


            EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                    .disableMemberCardNo(generateOrderId(uid))
                    .memberCardName(electricityMemberCard.getName())
                    .phone(userInfo.getPhone())
                    .userName(userInfo.getName())
                    .status(usableStatus)
                    .tenantId(userInfo.getTenantId())
                    .uid(uid)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            BigDecimal batteryServiceFee = checkDifferentModelBatteryServiceFee(franchisee, userInfo, null);
            eleDisableMemberCardRecord.setChargeRate(batteryServiceFee);
            eleDisableMemberCardRecord.setCardDays((userBatteryMemberCard.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000L / 60 / 60 / 24);
            eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

            Integer existServiceFee = ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE;
            if (Objects.equals(franchisee.getIsOpenServiceFee(), Franchisee.OPEN_SERVICE_FEE) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
                existServiceFee = ServiceFeeUserInfo.EXIST_SERVICE_FEE;
            }

            ServiceFeeUserInfo insertOrUpdateServiceFeeUserInfo = ServiceFeeUserInfo.builder()
                    .existBatteryServiceFee(existServiceFee)
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .franchiseeId(userInfo.getFranchiseeId())
                    .serviceFeeGenerateTime(userBatteryMemberCard.getMemberCardExpireTime())
                    .tenantId(eleDisableMemberCardRecord.getTenantId())
                    .uid(uid)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();

            if (Objects.isNull(serviceFeeUserInfo)) {
                serviceFeeUserInfoService.insert(insertOrUpdateServiceFeeUserInfo);
            } else {
                serviceFeeUserInfoService.updateByUid(insertOrUpdateServiceFeeUserInfo);
            }
        }


        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        if (Objects.equals(usableStatus, UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
        } else {
            userBatteryMemberCardUpdate.setDisableMemberCardTime(System.currentTimeMillis());
        }
        userBatteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setMemberCardStatus(usableStatus);
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

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

        return R.ok();
    }

    @Override
    public R cleanBatteryServiceFee(Long uid) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin saveUserMemberCard ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("admin saveUserMemberCard  ERROR! not found user! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//        if (Objects.isNull(franchiseeUserInfo) || !Objects.equals(franchiseeUserInfo.getTenantId(), TenantContextHolder.getTenantId())) {
//            log.error("DISABLE MEMBER CARD ERROR!not found deposit! userId:{}", user.getUid());
//            return R.fail("ELECTRICITY.0001", "未找到用户");
//        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("HOME ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = serviceFeeUserInfoService.queryUserBatteryServiceFee(uid);


        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            userBatteryMemberCardUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
            userBatteryMemberCardUpdate.setMemberCardExpireTime(System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime()));
            userBatteryMemberCardUpdate.setDisableMemberCardTime(null);

            Long now = System.currentTimeMillis();
            //判断用户是否产生电池服务费
            Long cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

            //不足一天按一天计算
            double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1L;
            }

            EleDisableMemberCardRecord eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(userInfo.getUid(), user.getTenantId());

            EnableMemberCardRecord enableMemberCardRecord = EnableMemberCardRecord.builder()
                    .disableMemberCardNo(eleDisableMemberCardRecord.getDisableMemberCardNo())
                    .memberCardName(electricityMemberCard.getName())
                    .enableTime(System.currentTimeMillis())
                    .enableType(EnableMemberCardRecord.ARTIFICIAL_ENABLE)
                    .batteryServiceFeeStatus(EnableMemberCardRecord.STATUS_INIT)
                    .disableDays(cardDays.intValue())
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


        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
        ServiceFeeUserInfo serviceFeeUserInfoUpdate = new ServiceFeeUserInfo();
        serviceFeeUserInfoUpdate.setUid(userInfo.getUid());
        serviceFeeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        serviceFeeUserInfoUpdate.setTenantId(serviceFeeUserInfo.getTenantId());
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            serviceFeeUserInfoUpdate.setExistBatteryServiceFee(ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE);
            Long memberCardExpireTime = System.currentTimeMillis() + (userBatteryMemberCard.getMemberCardExpireTime() - userBatteryMemberCard.getDisableMemberCardTime());
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(memberCardExpireTime);
        } else {
            serviceFeeUserInfoUpdate.setServiceFeeGenerateTime(System.currentTimeMillis());
        }
        serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);


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


        return R.ok();
    }

    @Override
    public R getDisableMemberCardList(ElectricityMemberCardRecordQuery electricityMemberCardRecordQuery) {
        return eleDisableMemberCardRecordService.list(electricityMemberCardRecordQuery);
    }

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

        //套餐订单
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        electricityMemberCardOrder.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId());
        electricityMemberCardOrder.setUid(memberCardOrderAddAndUpdate.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(0);
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
        //计算套餐剩余天数
        if (memberCardOrderAddAndUpdate.getMemberCardExpireTime() > System.currentTimeMillis()) {
            Double validDays = Math.ceil((memberCardOrderAddAndUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24.0);
            electricityMemberCardOrder.setValidDays(validDays.intValue());
        }

        baseMapper.insert(electricityMemberCardOrder);

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        UserBatteryMemberCard userBatteryMemberCardAddAndUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardAddAndUpdate.setUid(userInfo.getUid());
        userBatteryMemberCardAddAndUpdate.setTenantId(userInfo.getTenantId());
        userBatteryMemberCardAddAndUpdate.setMemberCardExpireTime(memberCardOrderAddAndUpdate.getMemberCardExpireTime());
        userBatteryMemberCardAddAndUpdate.setRemainingNumber(memberCardOrderAddAndUpdate.getMaxUseCount().intValue());
        userBatteryMemberCardAddAndUpdate.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
        userBatteryMemberCardAddAndUpdate.setMemberCardStatus(UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE);
        userBatteryMemberCardAddAndUpdate.setUpdateTime(System.currentTimeMillis());
        userBatteryMemberCardAddAndUpdate.setDelFlag(UserBatteryMemberCard.DEL_NORMAL);
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
            serviceFeeUserInfoInsertOrUpdate.setExistBatteryServiceFee(ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE);
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
        return R.ok();
    }

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

        if (!Objects.equals(memberCardOrderAddAndUpdate.getMemberCardId(), userBatteryMemberCard.getMemberCardId())) {
            //套餐订单
            ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
            electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
            electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
            electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
            electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            electricityMemberCardOrder.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId());
            electricityMemberCardOrder.setUid(memberCardOrderAddAndUpdate.getUid());
            electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
            electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
            electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
            electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
            electricityMemberCardOrder.setUserName(userInfo.getName());
            electricityMemberCardOrder.setValidDays(0);
            electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
            electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
            electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
            electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
            electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);

            //计算套餐剩余天数
            if (memberCardOrderAddAndUpdate.getMemberCardExpireTime() > System.currentTimeMillis()) {
                Double validDays = Math.ceil((memberCardOrderAddAndUpdate.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24.0);
                electricityMemberCardOrder.setValidDays(validDays.intValue());
            }

            baseMapper.insert(electricityMemberCardOrder);
        }


        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();

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
        userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber.intValue());
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
        return R.ok();
    }

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

        //套餐订单
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
        electricityMemberCardOrder.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId());
        electricityMemberCardOrder.setUid(memberCardOrderAddAndUpdate.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
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


        Long remainingNumber = userBatteryMemberCard.getRemainingNumber().longValue();
        if (!ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, userBatteryMemberCard.getRemainingNumber())) {
            remainingNumber = electricityMemberCard.getMaxUseCount() + userBatteryMemberCard.getRemainingNumber();
        }

        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setMemberCardId(memberCardOrderAddAndUpdate.getMemberCardId().longValue());
        userBatteryMemberCardUpdate.setMemberCardExpireTime(memberCardExpireTime);
        userBatteryMemberCardUpdate.setRemainingNumber(remainingNumber.intValue());
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

        Double carDayTemp = Math.ceil((userBatteryMemberCardUpdate.getMemberCardExpireTime() - now) / 1000L / 60 / 60 / 24.0);

        Long oldMaxUseCount = userBatteryMemberCard.getRemainingNumber().longValue();
        Long newMaxUseCount = electricityMemberCard.getMaxUseCount();

        if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            oldMaxUseCount = FranchiseeUserInfo.UN_LIMIT_COUNT_REMAINING_NUMBER;
            newMaxUseCount = FranchiseeUserInfo.UN_LIMIT_COUNT_REMAINING_NUMBER;
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
        return R.ok();
    }

    @Override
    @Deprecated
    public R payRentCarMemberCard(CarMemberCardOrderQuery carMemberCardOrderQuery, HttpServletRequest request) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(electricityPayParams)) {
            log.error("ELE CAR MEMBER CARD ERROR!not found pay params,uid={}", user.getUid());
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), TenantContextHolder.getTenantId());
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("ELE CAR MEMBER CARD ERROR!not found userOauthBind or thirdId is null,uid={}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE CAR MEMBER CARD ERROR! user is disable,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE CAR MEMBER CARD ERROR! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE CAR MEMBER CARD ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //获取车辆型号
        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(carMemberCardOrderQuery.getCarModelId());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found electricityCarModel id={},uid={}", carMemberCardOrderQuery.getCarModelId(), user.getUid());
            return R.fail("ELECTRICITY.0087", "未找到车辆型号!");
        }

        //获取租车套餐计费规则
        Map<String, Double> rentCarPriceRule = electricityCarModelService.parseRentCarPriceRule(electricityCarModel);
        if (ObjectUtil.isEmpty(rentCarPriceRule)) {
            log.error("ELE CAR MEMBER CARD ERROR! not found rentCarPriceRule id={},uid={}", carMemberCardOrderQuery.getCarModelId(), user.getUid());
            return R.fail("ELECTRICITY.0087", "租车套餐计费规则不存在!");
        }

        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(user.getUid());
        if (Objects.nonNull(userCarMemberCard) && Objects.nonNull(userCarMemberCard.getCardId())
                && userCarMemberCard.getMemberCardExpireTime() > System.currentTimeMillis()
                && !Objects.equals(userCarMemberCard.getCardId(), electricityCarModel.getId())) {
            log.error("ELE CAR MEMBER CARD ERROR! member_card is not expired uid={}", user.getUid());
            return R.fail("ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
        }

        EleCalcRentCarPriceService calcRentCarPriceInstance = calcRentCarPriceFactory.getInstance(carMemberCardOrderQuery.getRentType());
        if (Objects.isNull(calcRentCarPriceInstance)) {
            log.error("ELE CAR MEMBER CARD ERROR! calcRentCarPriceInstance is null,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0087", "租车套餐计费规则不存在!");
        }

        Pair<Boolean, Object> calcSavePrice = calcRentCarPriceInstance.getRentCarPrice(userInfo, carMemberCardOrderQuery.getRentTime(), rentCarPriceRule);
        if (!calcSavePrice.getLeft()) {
            return R.fail("ELECTRICITY.0087", "租车套餐计费规则不存在!");
        }

        BigDecimal rentCarPrice = (BigDecimal) calcSavePrice.getRight();


        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_PACKAGE, user.getUid());

        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(orderId);
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(carMemberCardOrderQuery.getCarModelId());
        electricityMemberCardOrder.setUid(user.getUid());
        electricityMemberCardOrder.setMaxUseCount(0L);
        electricityMemberCardOrder.setMemberCardType(0);
        electricityMemberCardOrder.setCardName(carMemberCardOrderQuery.getRentType());
        electricityMemberCardOrder.setPayAmount(rentCarPrice);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(carMemberCardOrderQuery.getRentTime());
        electricityMemberCardOrder.setTenantId(userInfo.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(carMemberCardOrderQuery.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(carMemberCardOrderQuery.getActivityId());
        electricityMemberCardOrder.setMemberCardModel(ElectricityMemberCardOrder.RENT_CAR_MEMBER_CARD);

        //支付金额不能为负数
        if (rentCarPrice.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            baseMapper.insert(electricityMemberCardOrder);

            UserCarMemberCard updateUserCarMemberCard = new UserCarMemberCard();
            updateUserCarMemberCard.setUid(userInfo.getUid());
            updateUserCarMemberCard.setCardId(electricityMemberCardOrder.getMemberCardId().longValue());
            updateUserCarMemberCard.setMemberCardExpireTime(calcRentCarMemberCardExpireTime(carMemberCardOrderQuery.getRentType(), carMemberCardOrderQuery.getRentTime(), userCarMemberCard));
            updateUserCarMemberCard.setUpdateTime(System.currentTimeMillis());

            userCarMemberCardService.updateByUid(updateUserCarMemberCard);

            return R.ok();
        }

        baseMapper.insert(electricityMemberCardOrder);
        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(electricityMemberCardOrder.getOrderId())
                    .uid(user.getUid())
                    .payAmount(electricityMemberCardOrder.getPayAmount())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_RENT_MEMBER_CARD)
                    .attach(ElectricityTradeOrder.ATTACH_RENT_CAR_MEMBER_CARD)
                    .description("租车月卡收费")
                    .tenantId(TenantContextHolder.getTenantId()).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return R.ok(resultDTO);
        } catch (WechatPayException e) {
            log.error("ELE CAR MEMBER CARD ERROR! wechat v3 order  error! uid={}", user.getUid(), e);
        }

        return R.fail("ELECTRICITY.0099", "下单失败");
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
    public BigDecimal queryBatteryMemberCardTurnOver(Integer tenantId, Long
            todayStartTime, List<Long> franchiseeId) {
        return Optional.ofNullable(baseMapper.queryBatteryMemberCardTurnOver(tenantId, todayStartTime, franchiseeId)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    public BigDecimal queryCarMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeId) {
        return Optional.ofNullable(baseMapper.queryCarMemberCardTurnOver(tenantId, todayStartTime, franchiseeId)).orElse(BigDecimal.valueOf(0));
    }

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
    @Deprecated
    public void carMemberCardExpireReminder() {
        if (!redisService.setNx(CacheConstant.CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LOCK, "ok", 120000L, false)) {
            log.warn("carMemberCardExpireReminder in execution...");
            return;
        }

        int offset = 0;
        int size = 300;
        Date date = new Date();
        long firstTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis() + 3 * 3600000 * 24;
        SimpleDateFormat simp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String firstTimeStr = redisService.get(CacheConstant.CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LAST_TIME);
        if (StrUtil.isNotBlank(firstTimeStr)) {
            firstTime = Long.parseLong(firstTimeStr);
        }

        redisService.set(CacheConstant.CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LAST_TIME, String.valueOf(lastTime));

        while (true) {
            List<CarMemberCardExpiringSoonQuery> franchiseeUserInfos =
                    userBatteryMemberCardService.carMemberCardExpire(offset, size, firstTime, lastTime);
            if (!DataUtil.collectionIsUsable(franchiseeUserInfos)) {
                return;
            }

            franchiseeUserInfos.parallelStream().forEach(item -> {
                ElectricityPayParams ele = electricityPayParamsService.queryFromCache(item.getTenantId());
                if (Objects.isNull(ele)) {
                    log.error("CAR MEMBER CARD EXPIRING SOON ERROR! ElectricityPayParams is null error! tenantId={}",
                            item.getTenantId());
                    return;
                }

                TemplateConfigEntity templateConfigEntity =
                        templateConfigService.queryByTenantIdFromCache(item.getTenantId());
                if (Objects.isNull(templateConfigEntity) || Objects
                        .isNull(templateConfigEntity.getBatteryOuttimeTemplate())) {
                    log.error("CAR MEMBER CARD EXPIRING SOON ERROR! templateConfigEntity is null error! tenantId={}",
                            item.getTenantId());
                    return;
                }

                date.setTime(item.getRentCarMemberCardExpireTime());

                item.setMerchantMinProAppId(ele.getMerchantMinProAppId());
                item.setMerchantMinProAppSecert(ele.getMerchantMinProAppSecert());
                item.setMemberCardExpiringTemplate(templateConfigEntity.getCarMemberCardExpiringTemplate());
                item.setRentCarMemberCardExpireTimeStr(simp.format(date));
                sendCarMemberCardExpiringTemplate(item);
            });
            offset += size;
        }
    }

    @Override
    public void systemEnableMemberCardTask() {

        int offset = 0;
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

                ElectricityMemberCard electricityMemberCard = null;
                if (Objects.nonNull(userBatteryMemberCard) && !Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
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
                serviceFeeUserInfoUpdate.setTenantId(userBatteryMemberCard.getTenantId());
                serviceFeeUserInfoService.updateByUid(serviceFeeUserInfoUpdate);

                EleDisableMemberCardRecord eleDisableMemberCardRecordUpdate = new EleDisableMemberCardRecord();
                eleDisableMemberCardRecordUpdate.setId(item.getId());
                eleDisableMemberCardRecordUpdate.setRealDays(item.getChooseDays());
                eleDisableMemberCardRecordService.updateBYId(eleDisableMemberCardRecordUpdate);
            });
            offset += size;
        }
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

        data.put("thing2", carMemberCardExpiringSoonQuery.getCardName());
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

    @Override
    public void expireReminderHandler() {

    }

    /**
     * 区分单型号或者多型号的服务费收费标准
     *
     * @param franchisee
     * @param userBattery
     * @param userInfo
     * @return
     */
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
            Integer model = BatteryConstant.acquireBattery(userBattery.getBatteryType());
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
    public BigDecimal checkUserDisableCardBatteryService(UserInfo userInfo, Long uid, Long cardDays, EleDisableMemberCardRecord eleDisableMemberCardRecord, ServiceFeeUserInfo serviceFeeUserInfo) {

        if (Objects.isNull(serviceFeeUserInfo)) {
            serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(uid);
        }
        if (Objects.isNull(serviceFeeUserInfo) || Objects.equals(serviceFeeUserInfo.getExistBatteryServiceFee(), ServiceFeeUserInfo.NOT_EXIST_SERVICE_FEE)) {
            return BigDecimal.valueOf(0);
        }
        if (Objects.isNull(eleDisableMemberCardRecord)) {
            eleDisableMemberCardRecord = eleDisableMemberCardRecordService.queryCreateTimeMaxEleDisableMemberCardRecord(uid, userInfo.getTenantId());
        }


        log.error("用户绑定套餐======================"+serviceFeeUserInfo);
        log.error("用户停卡记录======================"+eleDisableMemberCardRecord);

        //判断服务费
        if (Objects.nonNull(eleDisableMemberCardRecord) && Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES) && Objects.equals(serviceFeeUserInfo.getExistBatteryServiceFee(), ServiceFeeUserInfo.EXIST_SERVICE_FEE)) {
            BigDecimal franchiseeBatteryServiceFee = eleDisableMemberCardRecord.getChargeRate();
            //计算服务费
            BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(BigDecimal.valueOf(cardDays));
            return batteryServiceFee;
        } else {
            return BigDecimal.valueOf(0);
        }
    }

    /**
     * 计算套餐过期用户电池服务费
     *
     * @param userInfo
     * @param franchisee
     * @param cardDays
     * @return
     */
    public BigDecimal checkUserMemberCardExpireBatteryService(UserInfo userInfo, Franchisee franchisee, Long cardDays) {

        if (Objects.isNull(franchisee)) {
            franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
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
            rocketMqService.sendAsyncMsg(MqConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(i), "", "", 0);
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
    public Triple<Boolean, String, Object> handleRentBatteryMemberCard(RentCarHybridOrderQuery query, UserInfo userInfo) {
        if (Objects.isNull(query.getMemberCardId())) {
            return Triple.of(true, "", null);
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(query.getMemberCardId());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,not found member_card by id={},uid={}", query.getMemberCardId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,member_card is un_usable id={},uid={}", query.getMemberCardId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0088", "月卡已禁用!");
        }

        //查找计算优惠券
        //满减折扣劵
        UserCoupon userCoupon = null;
        BigDecimal payAmount = electricityMemberCard.getHolidayPrice();
        if (Objects.nonNull(query.getUserCouponId())) {
            userCoupon = userCouponService.queryByIdFromDB(query.getUserCouponId());
            if (Objects.isNull(userCoupon)) {
                log.error("ELECTRICITY  ERROR! not found userCoupon! userCouponId={},uid={} ", query.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }

            //优惠券是否使用
            if (Objects.equals(UserCoupon.STATUS_USED, userCoupon.getStatus())) {
                log.error("ELECTRICITY  ERROR!  userCoupon is used! userCouponId={},uid={} ", query.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0090", "您的优惠券已被使用");
            }

            //优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.error("ELECTRICITY  ERROR!  userCoupon is deadline!userCouponId={},uid={} ", query.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0091", "您的优惠券已过期");
            }

            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.error("ELECTRICITY  ERROR! not found coupon! userCouponId={},uid={} ", query.getUserCouponId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0085", "未找到优惠券");
            }

            //使用满减劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.FULL_REDUCTION)) {

                //计算满减
                payAmount = payAmount.subtract(coupon.getAmount());
            }

            //使用折扣劵
            if (Objects.equals(userCoupon.getDiscountType(), UserCoupon.DISCOUNT)) {

                //计算折扣
                payAmount = payAmount.multiply(coupon.getDiscount().divide(BigDecimal.valueOf(100)));
            }

        }

        //支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_PACKAGE, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(query.getMemberCardId());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setCardName(electricityMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        if (Objects.nonNull(query.getUserCouponId())) {
            electricityMemberCardOrder.setCouponId(query.getUserCouponId().longValue());
        }

        return Triple.of(true, null, electricityMemberCardOrder);
    }

    @Override
    public R cancelPayMemberCard() {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("cancel MEMBER CARD ERROR! not found user ");
            return R.ok();
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("cancel MEMBER CARD ERROR! not found user,uid={} ", user.getUid());
            return R.ok();
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = baseMapper.queryCreateTimeMaxMemberCardOrder(userInfo.getUid(), userInfo.getTenantId());
        if (Objects.isNull(electricityMemberCardOrder) || !Objects.equals(electricityMemberCardOrder.getStatus(), ElectricityMemberCardOrder.STATUS_INIT) || Objects.isNull(electricityMemberCardOrder.getCouponId())) {
            return R.ok();
        }

        UserCoupon userCoupon = userCouponService.queryByIdFromDB(electricityMemberCardOrder.getCouponId().intValue());
        if (Objects.isNull(userCoupon) || !Objects.equals(userCoupon.getStatus(), UserCoupon.STATUS_IS_BEING_VERIFICATION)) {
            return R.ok();
        }

        userCoupon.setStatus(UserCoupon.STATUS_UNUSED);
        userCoupon.setOrderId(null);
        userCoupon.setUpdateTime(System.currentTimeMillis());
        userCouponService.updateStatus(userCoupon);
        return R.ok();
    }

    @Override
    public Pair<Boolean, Object> checkUserHaveBatteryServiceFee(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard) {
        //用户所产生的电池服务费
        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);

        //获取新用户所绑定的加盟商的电池服务费
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("BATTERY SERVICE FEE ERROR!not found franchisee,uid={}", userInfo.getUid());
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

}


