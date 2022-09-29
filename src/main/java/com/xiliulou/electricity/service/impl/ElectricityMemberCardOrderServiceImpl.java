package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
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

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:54
 **/
@Service
@Slf4j
public class ElectricityMemberCardOrderServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements ElectricityMemberCardOrderService {

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
    FranchiseeUserInfoService franchiseeUserInfoService;
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
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);

        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID:{}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(user.getUid());

        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("ELECTRICITY  ERROR! user not auth! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("rentBattery  ERROR! not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }


        Long now = System.currentTimeMillis();

        if (Objects.nonNull(franchiseeUserInfo.getBatteryServiceFeeGenerateTime())) {
            long cardDays = (now - franchiseeUserInfo.getBatteryServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;

            if (Objects.nonNull(franchiseeUserInfo.getNowElectricityBatterySn()) && cardDays >= 1) {
                //查询用户是否存在电池服务费
                Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeUserInfo.getFranchiseeId());
                Integer modelType = franchisee.getModelType();
                if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
                    Integer model = BatteryConstant.acquireBattery(franchiseeUserInfo.getBatteryType());
                    List<ModelBatteryDeposit> modelBatteryDepositList = JSONObject.parseArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
                    for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                        if (Objects.equals(model, modelBatteryDeposit.getModel())) {
                            //计算服务费
                            BigDecimal batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee().multiply(new BigDecimal(cardDays));
                            if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                                return R.fail("ELECTRICITY.100000", "用户存在电池服务费", batteryServiceFee);
                            }
                        }
                    }
                } else {
                    BigDecimal franchiseeBatteryServiceFee = franchisee.getBatteryServiceFee();
                    //计算服务费
                    BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(new BigDecimal(cardDays));
                    if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                        return R.fail("ELECTRICITY.100000", "用户存在电池服务费", batteryServiceFee);
                    }
                }
            }
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(electricityMemberCardOrderQuery.getMemberId());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID:{}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        //判断是否已绑定限次数套餐并且换电次数为负
        ElectricityMemberCard bindElectricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());

        if (Objects.nonNull(bindElectricityMemberCard) && !Objects.equals(bindElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) && franchiseeUserInfo.getRemainingNumber() < 0) {
            if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                log.error("payDeposit  ERROR! not buy same memberCard uid:{}", user.getUid());
                return R.fail("ELECTRICITY.00119", "套餐剩余次数为负,应购买相同类型套餐抵扣");
            }
        }

        Long franchiseeId = franchiseeUserInfo.getFranchiseeId();

        if (Objects.nonNull(electricityMemberCardOrderQuery.getProductKey())
                && Objects.nonNull(electricityMemberCardOrderQuery.getDeviceName())) {
            //换电柜
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(electricityMemberCardOrderQuery.getProductKey(), electricityMemberCardOrderQuery.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.error("rentBattery  ERROR! not found electricityCabinet ！productKey{},deviceName{}", electricityMemberCardOrderQuery.getProductKey(), electricityMemberCardOrderQuery.getDeviceName());
                return R.fail("ELECTRICITY.0005", "未找到换电柜");
            }

            //3、查出套餐
            //查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("queryByDevice  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("queryByDevice  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }

            //查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("queryByDevice  ERROR! not found Franchisee ！storeId{}", store.getId());
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
                log.error("ELECTRICITY  ERROR! not found userCoupon! userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0085", "未找到优惠券");
            }

            //优惠券是否使用
            if (Objects.equals(UserCoupon.STATUS_USED, userCoupon.getStatus())) {
                log.error("ELECTRICITY  ERROR!  userCoupon is used! userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0090", "您的优惠券已被使用");
            }

            //优惠券是否过期
            if (userCoupon.getDeadline() < System.currentTimeMillis()) {
                log.error("ELECTRICITY  ERROR!  userCoupon is deadline!userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
                return R.fail("ELECTRICITY.0091", "您的优惠券已过期");
            }

            Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
            if (Objects.isNull(coupon)) {
                log.error("ELECTRICITY  ERROR! not found coupon! userCouponId:{} ", electricityMemberCardOrderQuery.getUserCouponId());
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

        //同一个套餐可以续费
        if (Objects.equals(franchiseeUserInfo.getCardId(), electricityMemberCardOrderQuery.getMemberId())) {
            if (now < franchiseeUserInfo.getMemberCardExpireTime() && Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) && franchiseeUserInfo.getRemainingNumber() > 0) {
                now = franchiseeUserInfo.getMemberCardExpireTime();
            }
            //TODO 使用次数暂时叠加
            if (!Objects.equals(bindElectricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                remainingNumber = remainingNumber + franchiseeUserInfo.getRemainingNumber();
            }

        } else {
            if (Objects.nonNull(franchiseeUserInfo.getMemberCardExpireTime())
                    && Objects.nonNull(franchiseeUserInfo.getRemainingNumber()) &&
                    franchiseeUserInfo.getMemberCardExpireTime() > now &&
                    (ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, franchiseeUserInfo.getRemainingNumber()) || franchiseeUserInfo.getRemainingNumber() > 0)) {
                log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS NOT EXPIRED USERINFO:{}", userInfo);
                return R.fail("ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
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
        electricityMemberCardOrder.setUserName(userInfo.getUserName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(franchiseeId);
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
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


            //用户
            FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
            franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
            Long memberCardExpireTime = now +
                    electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
            franchiseeUserInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
            franchiseeUserInfoUpdate.setBatteryServiceFeeGenerateTime(memberCardExpireTime);
            franchiseeUserInfoUpdate.setRemainingNumber(remainingNumber);
            franchiseeUserInfoUpdate.setMemberCardDisableStatus(FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE);
            franchiseeUserInfoUpdate.setCardName(electricityMemberCardOrder.getCardName());
            franchiseeUserInfoUpdate.setCardId(electricityMemberCardOrder.getMemberCardId());
            franchiseeUserInfoUpdate.setCardType(electricityMemberCardOrder.getMemberCardType());
            franchiseeUserInfoUpdate.setBatteryServiceFeeStatus(FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE);
            franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoService.update(franchiseeUserInfoUpdate);

            //月卡订单
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            baseMapper.updateById(electricityMemberCardOrderUpdate);

            if (Objects.nonNull(electricityMemberCardOrderQuery.getUserCouponId())) {
                //修改劵可用状态
                userCoupon.setStatus(UserCoupon.STATUS_USED);
                userCoupon.setUpdateTime(System.currentTimeMillis());
                userCoupon.setOrderId(electricityMemberCardOrder.getOrderId());
                userCouponService.update(userCoupon);
            }

            //被邀请新买月卡用户
            //是否是新用户
            if (Objects.isNull(franchiseeUserInfo.getCardId())) {
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

        //调起支付
        try {
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(electricityMemberCardOrder.getOrderId())
                    .uid(user.getUid())
                    .payAmount(electricityMemberCardOrder.getPayAmount())
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_DEPOSIT)
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
        memberCardOrderQuery.setOffset(0L);
        memberCardOrderQuery.setSize(2000L);
        List<ElectricityMemberCardOrderVO> electricityMemberCardOrderVOList = baseMapper.queryList(memberCardOrderQuery);
        if (ObjectUtil.isEmpty(electricityMemberCardOrderVOList)) {
            throw new CustomBusinessException("查不到订单");
        }

        List<ElectricityMemberCardOrderExcelVO> electricityMemberCardOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (ElectricityMemberCardOrderVO electricityMemberCardOrderVO : electricityMemberCardOrderVOList) {
            index++;
            ElectricityMemberCardOrderExcelVO excelVo = new ElectricityMemberCardOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(electricityMemberCardOrderVO.getOrderId());
            excelVo.setPhone(electricityMemberCardOrderVO.getPhone());
            excelVo.setPayAmount(electricityMemberCardOrderVO.getPayAmount());

            if (Objects.nonNull(electricityMemberCardOrderVO.getUpdateTime())) {
                excelVo.setBeginningTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime())));
                if (Objects.nonNull(electricityMemberCardOrderVO.getValidDays())) {
                    excelVo.setEndTime(simpleDateFormat.format(new Date(electricityMemberCardOrderVO.getUpdateTime() + electricityMemberCardOrderVO.getValidDays() * 24 * 60 * 60 * 1000)));
                }
            }

            if (Objects.isNull(electricityMemberCardOrderVO.getMemberCardType())) {
                excelVo.setMemberCardType("");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_MONTH_CARD)) {
                excelVo.setMemberCardType("月卡");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_SEASON_CARD)) {
                excelVo.setMemberCardType("季卡");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getMemberCardType(), ElectricityCabinetOrder.PAYMENT_METHOD_YEAR_CARD)) {
                excelVo.setMemberCardType("年卡");
            }

            if (Objects.isNull(electricityMemberCardOrderVO.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_INIT)) {
                excelVo.setStatus("未支付");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_SUCCESS)) {
                excelVo.setStatus("支付成功");
            }
            if (Objects.equals(electricityMemberCardOrderVO.getStatus(), ElectricityMemberCardOrder.STATUS_FAIL)) {
                excelVo.setStatus("支付失败");
            }

            electricityMemberCardOrderExcelVOS.add(excelVo);
        }

        String fileName = "购卡订单报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ElectricityMemberCardOrderExcelVO.class).sheet("sheet").doWrite(electricityMemberCardOrderExcelVOS);
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
            log.error("DISABLE MEMBER CARD ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未缴纳押金
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("DISABLE MEMBER CARD ERROR!not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(franchiseeUserInfo.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("DISABLE MEMBER CARD ERROR! disable review userId:{}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
            log.error("DISABLE MEMBER CARD ERROR! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }


        //启用月卡时判断用户是否有电池，收取服务费
        if (Objects.equals(usableStatus, FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE)) {
            if (Objects.nonNull(franchiseeUserInfo.getNowElectricityBatterySn()) && Objects.equals(franchiseeUserInfo.getBatteryServiceFeeStatus(), FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE)) {

                if (Objects.nonNull(franchiseeUserInfo.getDisableMemberCardTime())) {

                    //判断用户是否产生电池服务费
                    Long now = System.currentTimeMillis();
                    long cardDays = (now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

                    //不足一天按一天计算
                    double time = Math.ceil((now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                    if (time < 24) {
                        cardDays = 1;
                    }

                    if (cardDays >= 1) {
                        //查询用户是否存在电池服务费
                        Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeUserInfo.getFranchiseeId());
                        Integer modelType = franchisee.getModelType();
                        if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
                            Integer model = BatteryConstant.acquireBattery(franchiseeUserInfo.getBatteryType());
                            List<ModelBatteryDeposit> modelBatteryDepositList = JSONObject.parseArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
                            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                                if (Objects.equals(model, modelBatteryDeposit.getModel())) {
                                    //计算服务费
                                    BigDecimal batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee().multiply(new BigDecimal(cardDays));
                                    if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                                        return R.fail("ELECTRICITY.100000", "用户启用月卡存在电池服务费", batteryServiceFee);
                                    }
                                }
                            }
                        } else {
                            BigDecimal franchiseeBatteryServiceFee = franchisee.getBatteryServiceFee();
                            //计算服务费
                            BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(new BigDecimal(cardDays));
                            if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                                return R.fail("ELECTRICITY.100000", "用户启用月卡存在电池服务费", batteryServiceFee);
                            }
                        }
                    }
                }
            }
        }


        if (Objects.equals(usableStatus, EleDisableMemberCardRecord.MEMBER_CARD_DISABLE)) {
            usableStatus = EleDisableMemberCardRecord.MEMBER_CARD_DISABLE_REVIEW;
        }

        EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                .disableMemberCardNo(generateOrderId(user.getUid()))
                .memberCardName(franchiseeUserInfo.getCardName())
                .phone(userInfo.getPhone())
                .userName(userInfo.getName())
                .status(usableStatus)
                .tenantId(userInfo.getTenantId())
                .uid(user.getUid())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
        if (Objects.equals(usableStatus, FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE)) {
            Long memberCardExpireTime = System.currentTimeMillis() + (franchiseeUserInfo.getMemberCardExpireTime() - franchiseeUserInfo.getDisableMemberCardTime());
            updateFranchiseeUserInfo.setMemberCardExpireTime(memberCardExpireTime);
            updateFranchiseeUserInfo.setBatteryServiceFeeGenerateTime(memberCardExpireTime);
            updateFranchiseeUserInfo.setBatteryServiceFeeStatus(FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE);
        }
        updateFranchiseeUserInfo.setId(franchiseeUserInfo.getId());
        updateFranchiseeUserInfo.setMemberCardDisableStatus(usableStatus);
        franchiseeUserInfoService.update(updateFranchiseeUserInfo);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R adminOpenOrDisableMemberCard(Integer usableStatus, Long uid) {

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

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未缴纳押金
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("DISABLE MEMBER CARD ERROR!not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(franchiseeUserInfo.getOrderId());
        if (refundCount > 0) {
            return R.fail("100018", "押金退款审核中");
        }

        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("DISABLE MEMBER CARD ERROR! disable review userId:{}", user.getUid());
            return R.fail("ELECTRICITY.100001", "用户停卡申请审核中");
        }

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
            log.error("DISABLE MEMBER CARD ERROR! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持停卡服务");
        }

        Long now = System.currentTimeMillis();
        if (now > franchiseeUserInfo.getMemberCardExpireTime()) {
            log.error("DISABLE MEMBER CARD ERROR! uid:{} ", user.getUid());
            return R.fail("100013", "用户套餐已经过期");
        }

        //启用月卡时判断用户是否有电池，收取服务费
        if (Objects.equals(usableStatus, FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE)) {
            if (Objects.nonNull(franchiseeUserInfo.getNowElectricityBatterySn()) && Objects.equals(franchiseeUserInfo.getBatteryServiceFeeStatus(), FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE)) {

                if (Objects.nonNull(franchiseeUserInfo.getDisableMemberCardTime())) {

                    //判断用户是否产生电池服务费
                    long cardDays = (now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;

                    //不足一天按一天计算
                    double time = Math.ceil((now - franchiseeUserInfo.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                    if (time < 24) {
                        cardDays = 1;
                    }

                    if (cardDays >= 1) {
                        //查询用户是否存在电池服务费
                        Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeUserInfo.getFranchiseeId());
                        Integer modelType = franchisee.getModelType();
                        if (Objects.equals(modelType, Franchisee.NEW_MODEL_TYPE)) {
                            Integer model = BatteryConstant.acquireBattery(franchiseeUserInfo.getBatteryType());
                            List<ModelBatteryDeposit> modelBatteryDepositList = JSONObject.parseArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
                            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                                if (Objects.equals(model, modelBatteryDeposit.getModel())) {
                                    //计算服务费
                                    BigDecimal batteryServiceFee = modelBatteryDeposit.getBatteryServiceFee().multiply(new BigDecimal(cardDays));
                                    if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                                        return R.fail("ELECTRICITY.100000", "用户启用月卡存在电池服务费", batteryServiceFee);
                                    }
                                }
                            }
                        } else {
                            BigDecimal franchiseeBatteryServiceFee = franchisee.getBatteryServiceFee();
                            //计算服务费
                            BigDecimal batteryServiceFee = franchiseeBatteryServiceFee.multiply(new BigDecimal(cardDays));
                            if (BigDecimal.valueOf(0).compareTo(batteryServiceFee) != 0) {
                                return R.fail("ELECTRICITY.100000", "用户启用月卡存在电池服务费", batteryServiceFee);
                            }
                        }
                    }
                }
            }
        }

        EleDisableMemberCardRecord eleDisableMemberCardRecord = EleDisableMemberCardRecord.builder()
                .disableMemberCardNo(generateOrderId(uid))
                .memberCardName(franchiseeUserInfo.getCardName())
                .phone(userInfo.getPhone())
                .userName(userInfo.getName())
                .status(usableStatus)
                .tenantId(userInfo.getTenantId())
                .uid(uid)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleDisableMemberCardRecordService.save(eleDisableMemberCardRecord);

        FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
        if (Objects.equals(usableStatus, FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE)) {
            updateFranchiseeUserInfo.setMemberCardExpireTime(System.currentTimeMillis() + (franchiseeUserInfo.getMemberCardExpireTime() - franchiseeUserInfo.getDisableMemberCardTime()));
            updateFranchiseeUserInfo.setBatteryServiceFeeStatus(FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE);
        } else {
            updateFranchiseeUserInfo.setDisableMemberCardTime(System.currentTimeMillis());
        }
        updateFranchiseeUserInfo.setId(franchiseeUserInfo.getId());
        updateFranchiseeUserInfo.setMemberCardDisableStatus(usableStatus);
        franchiseeUserInfoService.update(updateFranchiseeUserInfo);

        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_DISABLE)
                .operateUid(user.getUid())
                .uid(uid)
                .name(user.getUsername())
                .memberCardDisableStatus(usableStatus)
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

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未缴纳押金
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("DISABLE MEMBER CARD ERROR!not found deposit! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = franchiseeUserInfoService.queryUserBatteryServiceFee(uid);

        FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
        franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
        if (Objects.equals(franchiseeUserInfo.getMemberCardDisableStatus(), FranchiseeUserInfo.MEMBER_CARD_DISABLE)) {
            franchiseeUserInfoUpdate.setMemberCardDisableStatus(FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE);
            franchiseeUserInfoUpdate.setMemberCardExpireTime(System.currentTimeMillis() + (franchiseeUserInfo.getMemberCardExpireTime() - franchiseeUserInfo.getDisableMemberCardTime()));
            franchiseeUserInfoUpdate.setBatteryServiceFeeStatus(FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE);
        }
        franchiseeUserInfoUpdate.setBatteryServiceFeeGenerateTime(System.currentTimeMillis());
        franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(franchiseeUserInfoUpdate);

        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.CLEAN_BATTERY_SERVICE_FEE)
                .operateUid(user.getUid())
                .uid(uid)
                .name(user.getUsername())
                .batteryServiceFee(eleBatteryServiceFeeVO.getUserBatteryServiceFee())
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

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        //未找到用户
        if (Objects.isNull(oldFranchiseeUserInfo)) {
            log.error("admin saveUserMemberCard  ERROR! not found user! userId:{}", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断是否缴纳押金
        if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(oldFranchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(oldFranchiseeUserInfo.getOrderId())) {
            log.error("admin saveUserMemberCard not pay deposit! uid:{} ", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(memberCardOrderAddAndUpdate.getMemberCardId());
        if (Objects.isNull(electricityMemberCard)) {
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
        electricityMemberCardOrder.setUserName(userInfo.getUserName());
        electricityMemberCardOrder.setValidDays(memberCardOrderAddAndUpdate.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(oldFranchiseeUserInfo.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
        baseMapper.insert(electricityMemberCardOrder);

        //用户
        FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
        franchiseeUserInfoUpdate.setId(oldFranchiseeUserInfo.getId());
        Long memberCardExpireTime = System.currentTimeMillis() +
                memberCardOrderAddAndUpdate.getValidDays() * (24 * 60 * 60 * 1000L);
        franchiseeUserInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
        franchiseeUserInfoUpdate.setBatteryServiceFeeGenerateTime(memberCardExpireTime);
        franchiseeUserInfoUpdate.setRemainingNumber(memberCardOrderAddAndUpdate.getMaxUseCount());
        franchiseeUserInfoUpdate.setMemberCardDisableStatus(FranchiseeUserInfo.MEMBER_CARD_NOT_DISABLE);
        franchiseeUserInfoUpdate.setCardName(electricityMemberCard.getName());
        franchiseeUserInfoUpdate.setCardId(memberCardOrderAddAndUpdate.getMemberCardId());
        franchiseeUserInfoUpdate.setCardType(electricityMemberCard.getType());
        franchiseeUserInfoUpdate.setBatteryServiceFeeStatus(FranchiseeUserInfo.STATUS_NOT_IS_SERVICE_FEE);
        franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.update(franchiseeUserInfoUpdate);


        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                .operateUid(user.getUid())
                .uid(memberCardOrderAddAndUpdate.getUid())
                .name(user.getUsername())
                .oldValidDays(MemberCardOrderAddAndUpdate.ZERO_VALIdDAY_MEMBER_CARD)
                .newValidDays(memberCardOrderAddAndUpdate.getValidDays())
                .oldMaxUseCount(oldFranchiseeUserInfo.getRemainingNumber())
                .newMaxUseCount(memberCardOrderAddAndUpdate.getMaxUseCount())
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

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
        //未找到用户
        if (Objects.isNull(oldFranchiseeUserInfo)) {
            log.error("admin editUserMemberCard ERROR! not found user! userId:{}", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断是否缴纳押金
        if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(oldFranchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(oldFranchiseeUserInfo.getOrderId())) {
            log.error("admin editUserMemberCard ERROR! not pay deposit! uid:{} ", memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(memberCardOrderAddAndUpdate.getMemberCardId());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("admin editUserMemberCard ERROR ,NOT FOUND MEMBER_CARD BY ID:{},uid:{}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("admin editUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID:{},uid:{}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        if (ObjectUtil.equal(FranchiseeUserInfo.MEMBER_CARD_DISABLE, oldFranchiseeUserInfo.getMemberCardDisableStatus())) {
            log.error("admin editUserMemberCard ERROR ,MEMBER_CARD IS UN_USABLE ID:{},uid:{}", memberCardOrderAddAndUpdate.getMemberCardId(), memberCardOrderAddAndUpdate.getUid());
            return R.fail("100028", "月卡暂停状态，不能修改套餐过期时间!");
        }

        if (!Objects.equals(memberCardOrderAddAndUpdate.getMemberCardId(), oldFranchiseeUserInfo.getCardId())) {
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
            electricityMemberCardOrder.setUserName(userInfo.getUserName());
            electricityMemberCardOrder.setValidDays(memberCardOrderAddAndUpdate.getValidDays());
            electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
            electricityMemberCardOrder.setFranchiseeId(oldFranchiseeUserInfo.getFranchiseeId());
            electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
            electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
            electricityMemberCardOrder.setPayType(ElectricityMemberCardOrder.OFFLINE_PAYMENT);
            baseMapper.insert(electricityMemberCardOrder);
        }

        //用户
        FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
        Long remainingNumber = memberCardOrderAddAndUpdate.getMaxUseCount();
        Long memberCardExpireTime = System.currentTimeMillis() +
                memberCardOrderAddAndUpdate.getValidDays() * (24 * 60 * 60 * 1000L);
        if (Objects.nonNull(oldFranchiseeUserInfo.getMemberCardExpireTime()) && ((oldFranchiseeUserInfo.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24) == memberCardOrderAddAndUpdate.getValidDays()) {
            memberCardExpireTime = oldFranchiseeUserInfo.getMemberCardExpireTime();
        }
        if (Objects.equals(memberCardOrderAddAndUpdate.getMaxUseCount(), MemberCardOrderAddAndUpdate.ZERO_USER_COUNT) || Objects.nonNull(memberCardOrderAddAndUpdate.getValidDays()) && Objects.equals(memberCardOrderAddAndUpdate.getValidDays(), MemberCardOrderAddAndUpdate.ZERO_VALIdDAY_MEMBER_CARD) && (oldFranchiseeUserInfo.getMemberCardExpireTime() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24 != MemberCardOrderAddAndUpdate.ZERO_VALIdDAY_MEMBER_CARD) {
            memberCardExpireTime = System.currentTimeMillis();
        }

        franchiseeUserInfoUpdate.setCardId(memberCardOrderAddAndUpdate.getMemberCardId());
        franchiseeUserInfoUpdate.setCardName(electricityMemberCard.getName());
        franchiseeUserInfoUpdate.setCardType(electricityMemberCard.getType());
        franchiseeUserInfoUpdate.setId(oldFranchiseeUserInfo.getId());
        franchiseeUserInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
        franchiseeUserInfoUpdate.setBatteryServiceFeeGenerateTime(memberCardExpireTime);
        franchiseeUserInfoUpdate.setRemainingNumber(remainingNumber);
        franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
        franchiseeUserInfoService.updateMemberCardExpire(franchiseeUserInfoUpdate);

        Long now = System.currentTimeMillis();
        Long oldCardDay = 0L;
        if (oldFranchiseeUserInfo.getMemberCardExpireTime() - now > 0) {
            oldCardDay = (oldFranchiseeUserInfo.getMemberCardExpireTime() - now) / 1000 / 60 / 60 / 24;
        }
        Long oldMaxUseCount = null;
        if (Objects.nonNull(memberCardOrderAddAndUpdate.getMaxUseCount())) {
            oldMaxUseCount = oldFranchiseeUserInfo.getRemainingNumber();
        }

        //生成后台操作记录
        EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                .operateModel(EleUserOperateRecord.MEMBER_CARD_MODEL)
                .operateContent(EleUserOperateRecord.MEMBER_CARD_EXPIRE_CONTENT)
                .operateUid(user.getUid())
                .uid(memberCardOrderAddAndUpdate.getUid())
                .name(user.getUsername())
                .oldValidDays(oldCardDay.intValue())
                .newValidDays(memberCardOrderAddAndUpdate.getValidDays())
                .oldMaxUseCount(oldMaxUseCount)
                .newMaxUseCount(memberCardOrderAddAndUpdate.getMaxUseCount())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleUserOperateRecordService.insert(eleUserOperateRecord);
        return R.ok();
    }

    @Override
    public R payRentCarMemberCard(ElectricityMemberCardOrderQuery electricityMemberCardOrderQuery, HttpServletRequest request) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //支付相关
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.failMsg("未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(user.getUid(), tenantId);

        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID:{}", user.getUid());
            return R.failMsg("未找到用户的第三方授权信息!");
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(user.getUid());

        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("ELECTRICITY  ERROR! user not auth! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getRentCarStatus(), FranchiseeUserInfo.RENT_CAR_STATUS_INIT)
                || Objects.isNull(franchiseeUserInfo.getRentCarDeposit()) || Objects.isNull(franchiseeUserInfo.getRentCarOrderId())) {
            log.error("rentBattery  ERROR! not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(electricityMemberCardOrderQuery.getMemberId());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID:{}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0087", "未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", electricityMemberCardOrderQuery.getMemberId());
            return R.fail("ELECTRICITY.0088", "月卡已禁用!");
        }

        if (Objects.nonNull(franchiseeUserInfo.getRentCarCardId()) && !Objects.equals(franchiseeUserInfo.getBindCarModelId(), electricityMemberCard.getCarModelId())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS NOT EXPIRED USERINFO:{}", userInfo);
            return R.fail("ELECTRICITY.0089", "您的套餐未过期，只能购买您绑定的套餐类型!");
        }

        BigDecimal payAmount = electricityMemberCard.getHolidayPrice();

        //支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
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
        electricityMemberCardOrder.setUserName(userInfo.getUserName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(electricityMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(electricityMemberCard.getFranchiseeId());
        electricityMemberCardOrder.setIsBindActivity(electricityMemberCard.getIsBindActivity());
        electricityMemberCardOrder.setActivityId(electricityMemberCard.getActivityId());
        electricityMemberCardOrder.setMemberCardModel(ElectricityMemberCardOrder.RENT_CAR_MEMBER_CARD);
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
    public ElectricityMemberCardOrder queryLastPayMemberCardTimeByUid(Long uid, Long franchiseeId, Integer tenantId) {
        return baseMapper.queryLastPayMemberCardTimeByUid(uid, franchiseeId, tenantId);
    }

    @Override
    public BigDecimal queryBatteryMemberCardTurnOver(Integer tenantId, Long todayStartTime, Long franchiseeId) {
        return Optional.ofNullable(baseMapper.queryBatteryMemberCardTurnOver(tenantId, todayStartTime, franchiseeId)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    public BigDecimal queryCarMemberCardTurnOver(Integer tenantId, Long todayStartTime, Long franchiseeId) {
        return Optional.ofNullable(baseMapper.queryCarMemberCardTurnOver(tenantId, todayStartTime, franchiseeId)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryBatteryMemberCardTurnOverByCreateTime(Integer tenantId, Long franchiseeId, Long beginTime, Long endTime) {
        return baseMapper.queryBatteryMemberCardTurnOverByCreateTime(tenantId, franchiseeId, beginTime, endTime);
    }

    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryCarMemberCardTurnOverByCreateTime(Integer tenantId, Long franchiseeId, Long beginTime, Long endTime) {
        return baseMapper.queryCarMemberCardTurnOverByCreateTime(tenantId, franchiseeId, beginTime, endTime);
    }

    @Override
    public BigDecimal querySumMemberCardTurnOver(Integer tenantId, Long franchiseeId, Long beginTime, Long endTime) {
        return baseMapper.querySumMemberCardTurnOverByCreateTime(tenantId, franchiseeId, beginTime, endTime);
    }

    private String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
                RandomUtil.randomNumbers(6);
    }


}
