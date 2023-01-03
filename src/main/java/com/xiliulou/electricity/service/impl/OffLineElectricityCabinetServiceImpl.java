package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.totp.TotpUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.EleOffLineSecretConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserFrontDetectionVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            log.error("OffLINE ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("OffLINE ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("OffLINE ELECTRICITY  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //用户是否实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("OffLINE ELECTRICITY  ERROR! user not auth!  uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

//        //是否缴纳押金，是否绑定电池
//        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
//
//        //未缴纳押金
//        if (Objects.isNull(franchiseeUserInfo)) {
//            log.error("OffLINE ELECTRICITY payDeposit  ERROR! not found user! userId:{}", user.getUid());
//            return R.fail("ELECTRICITY.0042", "未缴纳押金");
//
//        }
        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("OffLINE ELECTRICITY  ERROR! not pay deposit,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime()) || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("HOME WARN! user haven't memberCard uid={}", user.getUid());
            return R.fail("100210", "用户未开通套餐");
        }

        if (!Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            log.error("OffLINE ELECTRICITY  ERROR! disable memberCard ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.100002", "月卡停卡");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (Objects.isNull(electricityMemberCard)) {
            log.error("HOME ERROR! memberCard  is not exit,uid={},memberCardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return R.fail("ELECTRICITY.00121", "套餐不存在");
        }

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(electricityMemberCard.getType(), ElectricityMemberCard.TYPE_COUNT)) {
            log.error("OffLINE ELECTRICITY  ERROR! memberCard Type  is newUserActivity ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持离线换电");
        }

        Long now = System.currentTimeMillis();
        if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && userBatteryMemberCard.getMemberCardExpireTime() < now) {
            log.error("order  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }

        if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            if (userBatteryMemberCard.getRemainingNumber() < 0) {
                //用户需购买相同套餐，补齐所欠换电次数
                log.error("order  ERROR! memberCard remainingNumber insufficient uid={}", user.getUid());
                return R.fail("ELECTRICITY.00117", "套餐剩余次数为负", userBatteryMemberCard.getMemberCardId());
            }

            if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
                log.error("order  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
                return R.fail("ELECTRICITY.0023", "月卡已过期");
            }

            if (userBatteryMemberCard.getRemainingNumber() == 0) {
                log.error("order  ERROR! not found memberCard uid={}", user.getUid());
                return R.fail("ELECTRICITY.00118", "月卡可用次数已用完");
            }
        }

        //未租电池
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("OffLINE ELECTRICITY  ERROR! user not rent battery! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        //生成验证码key
        String key = eleOffLineSecretConfig.getSecret() + user.getPhone();
        //时间戳初始值
        long t0 = 0;
        //步长
        long step = eleOffLineSecretConfig.getStep();

        return R.ok(TotpUtils.generateTotp(key, System.currentTimeMillis() / 1000, 6, step, t0));
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

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());

        if (Objects.isNull(electricityMemberCard)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_NOT_EXIST);
            return userFrontDetectionVO;
        }
        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(electricityMemberCard.getType(), ElectricityMemberCard.TYPE_COUNT)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.IS_NEW_USER_ACTIVITY_CARD);
            return userFrontDetectionVO;
        }

        if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.MEMBER_CARD_OVER_DUE);
            return userFrontDetectionVO;
        }

        if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
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
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_NOT_BIND_BATTERY);
            return userFrontDetectionVO;
        }

        userFrontDetectionVO.setServiceStatus(UserFrontDetectionVO.USER_CAN_OFFLINE_ELECTRICITY);
        userFrontDetectionVO.setSecret(eleOffLineSecretConfig.getSecret());
        userFrontDetectionVO.setStep(eleOffLineSecretConfig.getStep());

        return userFrontDetectionVO;
    }
}
