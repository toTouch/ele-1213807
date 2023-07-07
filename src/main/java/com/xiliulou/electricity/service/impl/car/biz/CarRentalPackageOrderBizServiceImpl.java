package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.car.biz.RentalPackageBizService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租车套餐购买业务聚合 BizServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderBizServiceImpl implements CarRentalPackageOrderBizService {

    @Resource
    private ElectricityTradeOrderService electricityTradeOrderService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private RentalPackageBizService rentalPackageBizService;

    @Resource
    private RedisService redisService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    /**
     * 租车套餐订单，购买/续租
     * @param buyOptModel
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> buyRentalPackageOrder(CarRentalPackageOrderBuyOptModel buyOptModel) {
        // 参数校验
        Integer tenantId = buyOptModel.getTenantId();
        Long uid = buyOptModel.getUid();
        Long rentalPackageId = buyOptModel.getRentalPackageId();

        if (!ObjectUtils.allNotNull(tenantId, uid, rentalPackageId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 获取加锁 KEY
        String buyLockKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_BUY_UID_KEY, uid);

        try {
            // 加锁
            if (!redisService.setNx(buyLockKey, "1")) {
                return R.fail("ELECTRICITY.0034", "操作频繁");
            }

            // 下单前的统一拦截校验
            rentalPackageBizService.checkBuyPackageCommon(tenantId, uid);

            // 1. 获取租车套餐会员期限信息
            CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 若非空，则押金必定缴纳，若空，则无此数据
            if (!ObjectUtils.isEmpty(memberTermEntity)) {
                // 1.1 用户套餐会员限制状态异常
                if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
                    log.error("BuyRentalPackageOrder failed. Abnormal user status, uid is {}, status is {}", uid, memberTermEntity.getStatus());
                    return R.fail("300204", "用户状态异常");
                }
            }

            // 2. 获取套餐信息
            // 2.1 套餐不存在
            CarRentalPackagePO packageEntity = carRentalPackageService.selectById(rentalPackageId);
            if (ObjectUtils.isEmpty(packageEntity)) {
                log.error("BuyRentalPackageOrder failed. Package does not exist, rentalPackageId is {}", rentalPackageId);
                return R.fail("300101", "套餐不存在");
            }

            // 2.2 套餐上下架状态
            if (UpDownEnum.DOWN.getCode().equals(packageEntity.getStatus())) {
                log.error("BuyRentalPackageOrder failed. Package status is down. rentalPackageId is {}", rentalPackageId);
                return R.fail("300203", "套餐已下架");
            }

            // 3. 判定套餐互斥
            BigDecimal deposit = memberTermEntity.getDeposit();
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                // 此处代表用户名下没有任何租车套餐（单车或车电一体）
                // 3.2 电与车电一体互斥
                if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageEntity.getType())) {
                    // TODO 志龙提供接口，根据 tenantId、uid 查询是否存在换电押金
                    // TODO 存在，不允许购买
                    Boolean batteryExist = Boolean.TRUE;
                    if(!batteryExist) {
                        log.error("BuyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is battery", packageEntity.getType());
                        return R.fail("300205", "套餐不匹配");
                    }
                }
            } else {
                // 此处代表用户名下有租车套餐（单车或车电一体）
                // 3.3 用户名下的套餐类型和即将购买的套餐类型不一致
                if (!memberTermEntity.getRentalPackageType().equals(packageEntity.getType())) {
                    log.error("BuyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is {}", packageEntity.getType(), memberTermEntity.getRentalPackageType());
                    return R.fail("300205", "套餐不匹配");
                }

                // 3.4 若类型一致的情况下，则比对：型号（车、电） + 押金 + 套餐限制
                String rentalPackageOrderNo = memberTermEntity.getRentalPackageOrderNo();

                if (StringUtils.isNotBlank(rentalPackageOrderNo)) {
                    // 未退租
                    // 根据套餐购买订单编号，获取套餐购买订单表，读取其中的套餐快照信息
                    CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(rentalPackageOrderNo);

                    // 已经购买的套餐订单
                    Integer oriCarModelId = packageOrderEntity.getCarModelId();
                    BigDecimal oriDeposit = packageOrderEntity.getDeposit();
                    Integer oriConfine = packageOrderEntity.getConfine();
                    List<String> oriBatteryModelIds = Arrays.asList(packageEntity.getBatteryModelIds());

                    // 要下单的套餐订单
                    Integer buyCarModelId = packageEntity.getCarModelId();
                    BigDecimal buyDeposit = packageEntity.getDeposit();
                    Integer buyConfine = packageEntity.getConfine();
                    List<String> buyBatteryModelIds = Arrays.asList(packageEntity.getBatteryModelIds());

                    // 车辆型号、押金、套餐限制，任意一个不一致，则判定为不一致套餐
                    if (!buyCarModelId.equals(oriCarModelId) || buyDeposit.compareTo(oriDeposit) != 0 || !buyConfine.equals(oriConfine)) {
                        return R.fail("300205", "套餐不匹配");
                    }

                    // 电池型号，若新买的，没有完全包含于已经购买的，则不允许购买
                    if (buyBatteryModelIds.size() < oriBatteryModelIds.size() || !buyBatteryModelIds.stream().allMatch(n -> oriBatteryModelIds.contains(n)) ) {
                        return R.fail("300205", "套餐不匹配");
                    }

                } else {
                    // 退租未退押，押金一致，可以购的租车套餐（单车、车电一体）
                    if (deposit.compareTo(packageEntity.getDeposit()) != 0) {
                        return R.fail("300205", "套餐不匹配");
                    }
                }
            }

            // 4. 押金信息
            CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByTenantIdAndUid(tenantId, uid);
            // 没有押金订单，此时肯定也没有申请免押，因为免押是另外的线路，在下订单之前就已经生成记录了
            if (ObjectUtils.isEmpty(depositPayEntity)) {
                if (YesNoEnum.YES.getCode().equals(buyOptModel.getDepositType())) {
                    // 免押
                    return R.fail("ELECTRICITY.0042", "未缴纳押金");
                }
                // TODO 生成押金缴纳订单
            }



            if (YesNoEnum.YES.getCode().equals(buyOptModel.getDepositType())) {
                // 免押

            } else {

            }


            // 8. 判定是否强制购买保险以及保险是否过期
            // TODO 志龙提供接口，根据车辆型号、电池型号查询是否存在保险

            // 9. TODO 套餐剩余次数为负数？？？

            // 10. 判定柜机信息，是否存在这个柜机，这个柜机是否所属门店以及所属加盟商
            // TODO 柜机的判定

            // 11. 计算实际支付金额（叠加优惠券、押金、保险）
            // 总计支付金额（租金、押金、保险）
            BigDecimal totalAmount = new BigDecimal("123");
            Triple<BigDecimal, List<Long>, Boolean> couponTriple = rentalPackageBizService.calculatePaymentAmount(totalAmount, buyOptModel.getUserCouponIds(), uid);

            BigDecimal paymentAmount = couponTriple.getLeft();
            List<Long> userCouponIds = couponTriple.getMiddle();


            // 生成租车套餐订单，准备 insert
            CarRentalPackageOrderPO carRentalPackageOrder = buildCarRentalPackageOrder(packageEntity, paymentAmount, tenantId, uid);
            // 生成用户优惠券使用信息，准备 Update
            List<UserCoupon> userCouponList = buildUserCouponList(userCouponIds, UserCoupon.STATUS_USED, carRentalPackageOrder.getOrderNo(), OrderTypeEnum.CAR_BUY_ORDER.getCode());

            // 支付零元的处理
            if (BigDecimal.ZERO.compareTo(paymentAmount) >= 0) {
                // 无须唤起支付

            } else {
                // 唤起支付，走回调
                /*CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                        .orderId(electricityMemberCardOrder.getOrderId())
                        .uid(uid)
                        .payAmount(paymentAmount)
                        .orderType(ElectricityTradeOrder.ORDER_TYPE_MEMBER_CARD)
                        .attach(String.valueOf(electricityMemberCardOrderQuery.getUserCouponId()))
                        .description("月卡收费")
                        .tenantId(tenantId).build();

                WechatJsapiOrderResultDTO resultDTO =
                        electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
                return R.ok(resultDTO);*/
            }


            // 13. 支付成功之后， 分账、使用优惠券、赠送优惠券、押金、保险、套餐订单记录、会员期限、用户信息userInfo、


        } catch (Exception e) {

        } finally {
            redisService.delete(buyLockKey);
        }

        return null;
    }

    /**
     * 构建用户优惠券使用信息
     * @param userCouponIds
     * @param status
     * @param orderNo
     * @param orderIdType
     * @return
     */
    private List<UserCoupon> buildUserCouponList(List<Long> userCouponIds, Integer status, String orderNo, Integer orderIdType) {
        return userCouponIds.stream().map(userCouponId -> {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setId(userCouponId);
            userCoupon.setOrderId(orderNo);
            userCoupon.setOrderIdType(orderIdType);
            userCoupon.setStatus(status);
            userCoupon.setUpdateTime(System.currentTimeMillis());
            return userCoupon;
        }).collect(Collectors.toList());
    }

    /**
     * 构建租车套餐订单购买信息
     * @param packagePO
     * @param paymentAmount
     * @return
     */
    private CarRentalPackageOrderPO buildCarRentalPackageOrder(CarRentalPackagePO packagePO, BigDecimal paymentAmount, Integer tenantId, Long uid) {

        CarRentalPackageOrderPO carRentalPackage = new CarRentalPackageOrderPO();
        carRentalPackage.setUid(uid);
        carRentalPackage.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_MEMBERCARD, uid));
        carRentalPackage.setRentalPackageId(packagePO.getId());
        carRentalPackage.setRentalPackageType(packagePO.getType());
        carRentalPackage.setConfine(packagePO.getConfine());
        carRentalPackage.setConfineNum(packagePO.getConfineNum());
        carRentalPackage.setTenancy(packagePO.getTenancy());
        carRentalPackage.setTenancyUnit(packagePO.getTenancyUnit());
        carRentalPackage.setRentUnitPrice(packagePO.getRentUnitPrice());
        carRentalPackage.setRent(packagePO.getRent());
        carRentalPackage.setRentPayment(paymentAmount);
        carRentalPackage.setCarModelId(packagePO.getCarModelId());
        carRentalPackage.setBatteryModelIds(packagePO.getBatteryModelIds());
        carRentalPackage.setApplicableType(packagePO.getApplicableType());
        carRentalPackage.setRentRebate(packagePO.getRentRebate());
        carRentalPackage.setRentRebateTerm(packagePO.getRentRebateTerm());
        carRentalPackage.setRentRebateEndTime(TimeConstant.DAY_MILLISECOND * packagePO.getRentRebateTerm() + System.currentTimeMillis());
        carRentalPackage.setDeposit(packagePO.getDeposit());
        // TODO 押金订单缴纳编码
        carRentalPackage.setDepositPayOrderNo("");

        carRentalPackage.setLateFee(packagePO.getLateFee());
        // TODO 支付方式
        carRentalPackage.setPayType(1);
        // TODO 购买方式
        carRentalPackage.setBuyType(1);
        // TODO 柜机ID
        carRentalPackage.setCabinetId(1);

        carRentalPackage.setCouponId(packagePO.getCouponId());
        carRentalPackage.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackage.setUseState(UseStateEnum.UN_USED.getCode());
        carRentalPackage.setTenantId(tenantId);
        // TODO 加盟商ID
        carRentalPackage.setFranchiseeId(1);
        // TODO 门店ID
        carRentalPackage.setStoreId(1);
        carRentalPackage.setCreateUid(uid);
        carRentalPackage.setUpdateUid(uid);
        carRentalPackage.setCreateTime(System.currentTimeMillis());
        carRentalPackage.setUpdateTime(System.currentTimeMillis());
        carRentalPackage.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackage;
    }
}
