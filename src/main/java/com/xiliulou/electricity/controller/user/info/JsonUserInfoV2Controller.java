package com.xiliulou.electricity.controller.user.info;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.userinfo.*;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author xiaohui.song
 **/
@Slf4j
@RestController
@RequestMapping("/user/info/v2")
public class JsonUserInfoV2Controller extends BasicController {

    @Resource
    private UserBizService userBizService;

    @Autowired
    private UserBatteryMemberCardService batteryMemberCardService;

    @Autowired
    private BatteryMemberCardService memberCardService;

    @Autowired

    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取名下的总滞纳金（单电、单车、车电一体）
     * @return 总金额
     */
    @GetMapping("/querySlippageTotal")
    public R<BigDecimal> querySlippageTotal() {

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return R.ok(userBizService.querySlippageTotal(tenantId, user.getUid()));
    }

    /**
     * 查询用户会员名下的所有套餐的过期时间<br />
     * 单车、单电、车电一体
     * @return 会员套餐信息
     */
    @GetMapping("/queryRentalPackage")
    public R<UserMemberPackageVo> queryRentalPackage() {

        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Long uid = user.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        UserMemberPackageVo userMemberPackageVo = new UserMemberPackageVo();
        if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId())) {
            userMemberPackageVo.setFranchiseeId(userInfo.getFranchiseeId().intValue());
        }

        // 单电
        if (UserInfo.BATTERY_DEPOSIT_STATUS_YES.equals(userInfo.getBatteryDepositStatus())) {
            UserBatteryMemberCard batteryMemberCard = batteryMemberCardService.selectByUidFromCache(uid);
                if (ObjectUtils.isNotEmpty(batteryMemberCard) && ObjectUtils.isNotEmpty(batteryMemberCard.getMemberCardId()) && batteryMemberCard.getMemberCardId() != 0L) {
                Long orderExpireTime = batteryMemberCard.getOrderExpireTime();
                UserMemberBatteryPackageVo batteryPackage = new UserMemberBatteryPackageVo();
                batteryPackage.setDueTime(orderExpireTime);
                batteryPackage.setDueTimeTotal(batteryMemberCard.getMemberCardExpireTime());
                batteryPackage.setMemberCardStatus(batteryMemberCard.getMemberCardStatus());

                BatteryMemberCard batteryMemberCard1 = memberCardService.queryByIdFromCache(batteryMemberCard.getMemberCardId());
                batteryPackage.setRentUnit(Objects.isNull(batteryMemberCard1) ? null : batteryMemberCard1.getRentUnit());
                userMemberPackageVo.setBatteryPackage(batteryPackage);
            }
        }

        // 电车、车电一体
        if (UserInfo.CAR_DEPOSIT_STATUS_YES.equals(userInfo.getCarDepositStatus()) || YesNoEnum.YES.getCode().equals(userInfo.getCarBatteryDepositStatus())) {
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            if (ObjectUtils.isNotEmpty(memberTermEntity) && StringUtils.isNotBlank(memberTermEntity.getRentalPackageOrderNo())) {
                Integer rentalPackageType = memberTermEntity.getRentalPackageType();
                Long dueTime = memberTermEntity.getDueTime();

                if (RentalPackageTypeEnum.CAR.getCode().equals(rentalPackageType)) {
                    UserMemberCarPackageVo carPackage = new UserMemberCarPackageVo();
                    carPackage.setDueTime(dueTime);
                    carPackage.setDueTimeTotal(memberTermEntity.getDueTimeTotal());
                    carPackage.setStatus(memberTermEntity.getStatus());
                    userMemberPackageVo.setCarPackage(carPackage);
                }

                if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
                    UserMemberCarBatteryPackageVo carBatteryPackage = new UserMemberCarBatteryPackageVo();
                    carBatteryPackage.setDueTime(dueTime);
                    carBatteryPackage.setDueTimeTotal(memberTermEntity.getDueTimeTotal());
                    carBatteryPackage.setStatus(memberTermEntity.getStatus());

                    CarRentalPackageOrderPo carRentalPackageOrderPo = carRentalPackageOrderService.selectByOrderNo(memberTermEntity.getRentalPackageOrderNo());
                    carBatteryPackage.setRentUnit(carRentalPackageOrderPo.getTenancyUnit());

                    userMemberPackageVo.setCarBatteryPackage(carBatteryPackage);
                }
            }
        }
        return R.ok(userMemberPackageVo);
    }

    /**
     * 获取实名认证信息
     * @return 用户基本信息
     */
    @GetMapping("/queryAuthentication")
    public R<UserInfoVO> queryAuthentication() {
        // 用户拦截
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("BasicController.checkPermission failed. not found user.");
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        // 查询用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            UserInfoVO info = new UserInfoVO();
            info.setName(userInfo.getName());
            info.setPhone(userInfo.getPhone());
            info.setIdNumber(userInfo.getIdNumber());
            return R.ok(info);
        }

        return R.ok();
    }
}
