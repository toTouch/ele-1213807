package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.totp.TotpUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.EleOffLineSecretConfig;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.OffLineElectricityCabinetService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserFrontDetectionVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service("offLineElectricityCabinetService")
@Slf4j
public class OffLineElectricityCabinetServiceImpl implements OffLineElectricityCabinetService {

    @Autowired
    EleOffLineSecretConfig eleOffLineSecretConfig;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    RedisService redisService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;

    @Autowired
    CarRentalPackageMemberTermBizService carRentalPackageMemberTermBizService;
    
    @Autowired
    private BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;

    /**
     * 生成离线换电验证码
     *
     * @return
     */
    @Override
    public R generateVerificationCode() {

        //用户验证
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //用户是否实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }
    
        //未租电池
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            //单电
            Triple<Boolean, String, Object> result = verifySingleExchangeBattery(userInfo);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return R.fail(result.getMiddle(), (String) result.getRight());
            }
        } else if (Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            //车电一体
            carRentalPackageMemberTermBizService.verifyMemberSwapBattery(userInfo.getTenantId(),userInfo.getUid());
        } else {
            return R.fail( "ELECTRICITY.0042", "未缴纳押金");
        }

        //生成验证码key
        String key = eleOffLineSecretConfig.getSecret() + user.getPhone();
        //时间戳初始值
        long t0 = 0;
        //步长
        long step = eleOffLineSecretConfig.getStep();

        return R.ok(TotpUtils.generateTotp(key, System.currentTimeMillis() / 1000, 6, step, t0));
    }
    
    private Triple<Boolean, String, Object> verifySingleExchangeBattery(UserInfo userInfo) {
        //判断用户套餐
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("OffLINE ELECTRICITY WARN! user haven't memberCard uid={}", userInfo.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("OffLINE ELECTRICITY WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐停卡审核中");
        }
        
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("OffLINE ELECTRICITY WARN! user's member card is stop! uid={}", userInfo.getUid());
            return Triple.of(false, "100211", "换电套餐已暂停");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("OffLINE ELECTRICITY ERROR! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }
    
        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
            log.warn("OffLINE ELECTRICITY WARN! battery membercard refund review,uid={}", userInfo.getUid());
            return Triple.of(false, "100018", "套餐租金退款审核中");
        }
        
        //判断用户电池服务费
        Triple<Boolean, Integer, BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService
                .acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
        if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
            log.warn("OffLINE ELECTRICITY WARN! user exist battery service fee,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.100000", "存在电池服务费");
        }
        
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)
                && userBatteryMemberCard.getRemainingNumber() <= 0)) {
            log.warn("OffLINE ELECTRICITY WARN! battery memberCard is Expire,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0023", "套餐已过期");
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public R frontDetection() {

        UserFrontDetectionVO userFrontDetectionVO = new UserFrontDetectionVO();

        //用户验证
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.NOT_FOUND_USER);
            return R.ok(userFrontDetectionVO);
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.NOT_FOUND_USER);
            return R.ok(userFrontDetectionVO);
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_IS_DISABLE);
            return R.ok(userFrontDetectionVO);
        }

        //用户是否实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_AUTHENTICATION);
            return R.ok(userFrontDetectionVO);
        }

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//        //未缴纳押金
//        if (Objects.isNull(franchiseeUserInfo)) {
//            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_DEPOSIT);
//            return R.ok(userFrontDetectionVO);
//        }

        //判断是否缴纳押金
        if ((!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES))) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_DEPOSIT);
            return R.ok(userFrontDetectionVO);
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());


        //用户是否开通月卡
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_MEMBER_CARD);
            return R.ok(userFrontDetectionVO);
        }


        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_NOT_EXIST);
            return R.ok(userFrontDetectionVO);
        }

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(electricityMemberCard.getType(), ElectricityMemberCard.TYPE_COUNT)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.IS_NEW_USER_ACTIVITY_CARD);
            return R.ok(userFrontDetectionVO);
        }

        Long now = System.currentTimeMillis();
        if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && userBatteryMemberCard.getMemberCardExpireTime() < now) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_OVER_DUE);
            return R.ok(userFrontDetectionVO);
        }

        if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            if (userBatteryMemberCard.getRemainingNumber() < 0) {
                //用户需购买相同套餐，补齐所欠换电次数
                userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_NEGATIVE_NUMBER);
                return R.ok(userFrontDetectionVO);
            }

            if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
                userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_OVER_DUE);
                return R.ok(userFrontDetectionVO);
            }

            if (userBatteryMemberCard.getRemainingNumber() == 0) {
                userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_USE_UP);
                return R.ok(userFrontDetectionVO);
            }
        }
        //未租电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_BIND_BATTERY);
            return R.ok(userFrontDetectionVO);
        }

        userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_CAN_OFFLINE_ELECTRICITY);
        userFrontDetectionVO.setSecret(eleOffLineSecretConfig.getSecret());
        userFrontDetectionVO.setStep(eleOffLineSecretConfig.getStep());

        return R.ok(userFrontDetectionVO);
    }

    @Override
    public UserFrontDetectionVO getUserFrontDetection(UserInfo userInfo, UserBatteryMemberCard userBatteryMemberCard) {
        UserFrontDetectionVO userFrontDetectionVO = new UserFrontDetectionVO();

        //用户验证
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.NOT_FOUND_USER);
            return userFrontDetectionVO;
        }

//        //校验用户
//        if (Objects.isNull(userInfo)) {
//            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.NOT_FOUND_USER);
//            return userFrontDetectionVO;
//        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_IS_DISABLE);
            return userFrontDetectionVO;
        }

        //用户是否实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_AUTHENTICATION);
            return userFrontDetectionVO;
        }

        //未缴纳押金
//        if (Objects.isNull(franchiseeUserInfo)) {
//            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_DEPOSIT);
//            return userFrontDetectionVO;
//        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_DEPOSIT);
            return userFrontDetectionVO;
        }

        //用户是否开通月卡
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_MEMBER_CARD);
            return userFrontDetectionVO;
        }

        BatteryMemberCard electricityMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());

        if (Objects.isNull(electricityMemberCard)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_NOT_EXIST);
            return userFrontDetectionVO;
        }

        if (!Objects.equals(electricityMemberCard.getLimitCount(), BatteryMemberCard.UN_LIMIT)) {
            if (userBatteryMemberCard.getRemainingNumber() < 0) {
                //用户需购买相同套餐，补齐所欠换电次数
                userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_NEGATIVE_NUMBER);
                return userFrontDetectionVO;
            }

            if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
                userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_OVER_DUE);
                return userFrontDetectionVO;
            }

            if (userBatteryMemberCard.getRemainingNumber() == 0) {
                userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_USE_UP);
                return userFrontDetectionVO;
            }
        }
        //未租电池
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_BIND_BATTERY);
            return userFrontDetectionVO;
        }

        userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_CAN_OFFLINE_ELECTRICITY);
        userFrontDetectionVO.setSecret(eleOffLineSecretConfig.getSecret());
        userFrontDetectionVO.setStep(eleOffLineSecretConfig.getStep());

        return userFrontDetectionVO;
    }
}
