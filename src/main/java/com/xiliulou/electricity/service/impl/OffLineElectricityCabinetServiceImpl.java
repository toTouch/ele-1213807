package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.totp.TotpUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.EleOffLineSecretConfig;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.OffLineElectricityCabinetService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
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
    FranchiseeUserInfoService franchiseeUserInfoService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    RedisService redisService;


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
        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
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
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("OffLINE ELECTRICITY  ERROR! user not auth!  uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未缴纳押金
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("OffLINE ELECTRICITY payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");

        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("OffLINE ELECTRICITY  ERROR! not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //用户是否开通月卡
        if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
                || Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
            log.error("OffLINE ELECTRICITY  ERROR! not found memberCard ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }

        //判断套餐是否为新用户送的次数卡
        if (Objects.equals(franchiseeUserInfo.getCardType(), FranchiseeUserInfo.TYPE_COUNT)) {
            log.error("OffLINE ELECTRICITY  ERROR! memberCard Type  is newUserActivity ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.00116", "新用户体验卡，不支持离线换电");
        }

        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(franchiseeUserInfo.getCardId());
        if (Objects.isNull(electricityMemberCard)){
            log.error("OffLINE ELECTRICITY  ERROR! memberCard  is not exist ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.00121","套餐不存在");
        }

        Long now = System.currentTimeMillis();
        if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE) && franchiseeUserInfo.getMemberCardExpireTime() < now) {
            log.error("order  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }

        if (!Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
            if (franchiseeUserInfo.getRemainingNumber() < 0) {
                //用户需购买相同套餐，补齐所欠换电次数
                log.error("order  ERROR! memberCard remainingNumber insufficient uid={}", user.getUid());
                return R.fail("ELECTRICITY.00117", "套餐剩余次数为负",franchiseeUserInfo.getCardId());
            }

            if (franchiseeUserInfo.getMemberCardExpireTime() < now){
                log.error("order  ERROR! memberCard  is Expire ! uid:{} ", user.getUid());
                return R.fail("ELECTRICITY.0023", "月卡已过期");
            }

            if (franchiseeUserInfo.getRemainingNumber()==0) {
                log.error("order  ERROR! not found memberCard uid={}", user.getUid());
                return R.fail("ELECTRICITY.00118", "月卡可用次数已用完");
            }
        }


//        //未租电池
//        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
//            log.error("OffLINE ELECTRICITY  ERROR! user not rent battery! uid:{} ", user.getUid());
//            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
//        }

        //生成验证码key
        String key = eleOffLineSecretConfig.getSecret() + user.getPhone();
        //时间戳初始值
        long t0 = 0;
        //步长
        long step = eleOffLineSecretConfig.getStep();

        return R.ok(TotpUtils.generateTotp(key, System.currentTimeMillis() / 1000, 6, step, t0));
    }
}
