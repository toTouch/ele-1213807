package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 租车套餐购买业务聚合 BizServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderBizServiceImpl implements CarRentalPackageOrderBizService {

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisService redisService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    /**
     * 租车套餐订单，购买/续租
     *
     * @param buyOptModel
     * @return
     */
    @Override
    public R<Boolean> buyRentalPackageOrder(CarRentalPackageOrderBuyOptModel buyOptModel) {

        // 参数校验
        Integer tenantId = buyOptModel.getTenantId();
        Long uid = buyOptModel.getUid();
        Long rentalPackageId = buyOptModel.getRentalPackageId();
        if (ObjectUtils.anyNotNull(tenantId, uid, rentalPackageId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        String buyLockKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_BUY_UID_KEY, uid);

        try {
            // 购买加锁
            if (!redisService.setNx(buyLockKey, "1")) {
                return R.fail("ELECTRICITY.0034", "操作频繁");
            }

            // 1 获取用户信息
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.error("BuyRentalPackageOrder failed. Not found user. uid is {} ", uid);
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }

            // 1.1 用户可用状态
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("BuyRentalPackageOrder failed. User is unUsable. uid is {} ", uid);
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }

            // 1.2 用户实名认证状态
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("BuyRentalPackageOrder failed. User not auth. uid is {}", uid);
                return R.fail("ELECTRICITY.0041", "用户尚未实名认证");
            }

            // 2. 获取租车套餐会员期限信息
            R<CarRentalPackageMemberTermPO> memberTermResult = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            if (!memberTermResult.isSuccess()) {
                return R.fail(memberTermResult.getErrCode(), memberTermResult.getErrMsg());
            }

            // 2.1 用户套餐会员限制状态异常
            CarRentalPackageMemberTermPO memberTerm = memberTermResult.getData();
            // 不为空，用户已经产生过交易记录；为空，首次交易，程序继续
            if (ObjectUtils.isNotEmpty(memberTerm) && !MemberTermStatusEnum.NORMAL.getCode().equals(memberTerm.getStatus())) {
                log.error("BuyRentalPackageOrder failed. Abnormal user status, uid is {}, status is {}", uid, memberTerm.getStatus());
                return R.fail("300204", "用户状态异常");
            }

            // 3. 获取套餐信息
            R<CarRentalPackagePO> packageResult = carRentalPackageService.selectById(rentalPackageId);
            if (!packageResult.isSuccess()) {
                return R.fail(packageResult.getErrCode(), packageResult.getErrMsg());
            }

            // 3.1 套餐不存在
            CarRentalPackagePO packagePO = packageResult.getData();
            if (ObjectUtils.isEmpty(packagePO)) {
                log.error("BuyRentalPackageOrder failed. Package does not exist, rentalPackageId is {}", rentalPackageId);
                return R.fail("300101", "套餐不存在");
            }

            // 3.2 套餐上下架状态
            if (UpDownEnum.DOWN.getCode().equals(packagePO.getStatus())) {
                log.error("BuyRentalPackageOrder failed. Package status is down. rentalPackageId is {}", rentalPackageId);
                return R.fail("300203", "套餐已下架");
            }

            // 4. 判定套餐互斥
            // 4.1 用户名下已有的套餐类型与购买的套餐类型判定(车与车电一体互斥)
            if (ObjectUtils.isNotEmpty(memberTerm) && !memberTerm.getRentalPackageType().equals(packagePO.getType())) {
                log.error("BuyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is {}", packagePO.getType(), memberTerm.getRentalPackageType());
                return R.fail("300205", "套餐不匹配");
            }

            // 4.2 电与车电一体互斥
            if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packagePO.getType())) {
                // TODO 志龙提供接口，根据 tenantId、uid 查询是否存在换电押金
                // TODO 存在，不允许购买
            }

            // 4.3 用户名下已有交易记录（未退租或退租未退押）
            if (ObjectUtils.isNotEmpty(memberTerm)) {
                // 未退租，按照订单编号购买同一类型套餐
                String rentalPackageOrderNo = memberTerm.getRentalPackageOrderNo();
                if (StringUtils.isNotBlank(rentalPackageOrderNo)) {
                    // 4.3 同类型套餐之间互斥. 规则：同一类型 + 同一型号 + 同一租金金额 + 同一套餐限制
                    // 4.3.1 当走到这一步的时候，类型必定一致。电池型号：完全包含于。
                    // 要下单的套餐
                    Integer buyCarModelId = packagePO.getCarModelId();
                    BigDecimal deposit = packagePO.getDeposit();
                    List<String> buyBatteryModelIds = Arrays.asList(packagePO.getBatteryModelIds());
                    Integer buyConfine = packagePO.getConfine();
                    // 根据套餐购买订单编号，获取套餐购买订单表，读取其中的套餐快照信息
                    R<CarRentalPackageOrderPO> packageOrderResult = carRentalPackageOrderService.selectByOrderNo(rentalPackageOrderNo);
                    if (!packageOrderResult.isSuccess()) {
                        return R.fail(packageOrderResult.getErrCode(), packageOrderResult.getErrMsg());
                    }
                    CarRentalPackageOrderPO packageOrderPO = packageOrderResult.getData();

                    Integer oriCarModelId = packageOrderPO.getCarModelId();
                    String oriBatteryModelIds = packageOrderPO.getBatteryModelIds();
                    BigDecimal oriDeposit = packageOrderPO.getDeposit();
                    Integer oriConfine = packageOrderPO.getConfine();
                    // 已有的套餐

                } else {
                    // 退租未退押，随意购买同一类型套餐

                }



                // 获取当前名下正在使用的套餐，通过套餐购买记录
            }


        } catch (Exception e) {

        } finally {
            redisService.delete(buyLockKey);
        }






        // 3、判定用户名下是否存在租车套餐或者换电套餐



        // 2.1、若为车电一体套餐，则判断互斥

        // 3、套餐信息是否一致，即是否购买套餐一致之外的另外一个套餐

        // 2、套餐互斥，互斥规则：车电一体不能与租车/换电共存
        // 2.1 是否存在换电套餐的押金 TODO 志龙提供接口，根据 uid 查询是否存在换电押金
        Boolean batteryDepositFlag = false;
        if (batteryDepositFlag) {
            return R.fail("300200", "下单失败，已有换电套餐");
        }

        // 2.2 是否存在租车/车电一体的押金


        return null;
    }
}
