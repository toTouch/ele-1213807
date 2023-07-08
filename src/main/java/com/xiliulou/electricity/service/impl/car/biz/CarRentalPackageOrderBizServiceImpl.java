package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.InsuranceOrder;
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

            // 初始化押金金额
            BigDecimal deposit = BigDecimal.ZERO;

            // 1. 获取租车套餐会员期限信息
            CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 若非空，则押金必定缴纳，若空，则无此数据
            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                // 1.1 用户套餐会员限制状态异常
                if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
                    log.error("BuyRentalPackageOrder failed. Abnormal user status, uid is {}, status is {}", uid, memberTermEntity.getStatus());
                    return R.fail("300204", "用户状态异常");
                }
                // 从会员期限中赋值押金金额
                deposit = memberTermEntity.getDeposit();
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

            // 2.3 TODO 判定用户是否是老用户，然后和套餐的适用类型做比对

            // 3. 判定套餐互斥
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
            // 从套餐里面赋值押金
            deposit = packageEntity.getDeposit();
            // 待新增的押金信息，肯定没有走免押
            CarRentalPackageDepositPayPO depositPayInsertEntity = null;
            // 押金缴纳订单编码
            String depositPayOrderNo = null;
            CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByTenantIdAndUid(tenantId, uid);
            // 没有押金订单，此时肯定也没有申请免押，因为免押是另外的线路，在下订单之前就已经生成记录了
            if (ObjectUtils.isEmpty(depositPayEntity)) {
                if (YesNoEnum.YES.getCode().equals(buyOptModel.getDepositType())) {
                    // 免押
                    return R.fail("ELECTRICITY.0042", "未缴纳押金");
                }
                // 生成押金缴纳订单，准备 insert
                depositPayInsertEntity = buildCarRentalPackageDepositPay(tenantId, uid, packageEntity.getDeposit(), DepositExemptionEnum.NO.getCode());
                depositPayOrderNo = depositPayInsertEntity.getOrderNo();
            }

            // 存在押金信息，但是不匹配
            if ((YesNoEnum.YES.getCode().equals(buyOptModel.getDepositType()) && !PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType()))
                    || YesNoEnum.NO.getCode().equals(buyOptModel.getDepositType()) && PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType())) {
                // 免押
                return R.fail("", "请选择对应的押金缴纳方式");
            }
            depositPayOrderNo = depositPayEntity.getOrderNo();

            // TODO 需要重新计算保险金额以及是否强制购买保险的判断逻辑校验
            // TODO 判定 t_insurance_order、t_insurance_user_info是否需要操作
            // TODO insuranceAmount 需要重新赋值
             InsuranceOrder insuranceOrderInsertEntity = buildInsuranceOrder(uid);
            // 保险费用初始化
            BigDecimal insuranceAmount = BigDecimal.ZERO;
            // TODO 志龙提供接口，根据车辆型号、电池型号（电压伏数）查询是否存在保险

            // TODO 柜机的判定，此逻辑取决于是否有购买来源（柜机、非柜机）

            // 11. 计算金额（叠加优惠券、押金、保险）
            // 优惠券只抵扣租金
            Triple<BigDecimal, List<Long>, Boolean> couponTriple = rentalPackageBizService.calculatePaymentAmount(packageEntity.getRent(), buyOptModel.getUserCouponIds(), uid);

            // 实际支付租金金额
            BigDecimal rentPaymentAmount = couponTriple.getLeft();
            // 实际支付总金额（租金 + 押金 + 保险）
            BigDecimal paymentAmount = rentPaymentAmount.add(deposit).add(insuranceAmount);
            List<Long> userCouponIds = couponTriple.getMiddle();

            // TODO 判定 depositPayInsertEntity 是否需要新增

            // 生成租车套餐订单，准备 insert
            CarRentalPackageOrderPO carRentalPackageOrder = buildCarRentalPackageOrder(packageEntity, rentPaymentAmount, tenantId, uid, depositPayOrderNo);
            // 生成用户优惠券使用信息(已使用)，准备 Update
            List<UserCoupon> userCouponList = buildUserCouponList(userCouponIds, UserCoupon.STATUS_USED, carRentalPackageOrder.getOrderNo(), OrderTypeEnum.CAR_BUY_ORDER.getCode());
            // 判定 memberTermEntity
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                // 生成租车套餐会员期限表信息，准备 Insert
                CarRentalPackageMemberTermPO memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, packageEntity, carRentalPackageOrder);
            }


            // 支付零元的处理
            if (BigDecimal.ZERO.compareTo(paymentAmount) >= 0) {
                // TODO 无须唤起支付，走支付回调的逻辑，抽取方法，直接调用

            } else {
                // TODO 唤起支付，走回调
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
     * 构建租车套餐会员期限信息
     * @param tenantId
     * @param uid
     * @param packageEntity
     * @param carRentalPackageOrderEntity
     * @return
     */
    private CarRentalPackageMemberTermPO buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePO packageEntity, CarRentalPackageOrderPO carRentalPackageOrderEntity) {
        CarRentalPackageMemberTermPO carRentalPackageMemberTermPO = new CarRentalPackageMemberTermPO();
        carRentalPackageMemberTermPO.setUid(uid);
        carRentalPackageMemberTermPO.setRentalPackageOrderNo(carRentalPackageOrderEntity.getOrderNo());
        carRentalPackageMemberTermPO.setRentalPackageId(packageEntity.getId());
        carRentalPackageMemberTermPO.setRentalPackageType(packageEntity.getType());

        // 计算到期时间
        Integer tenancy = packageEntity.getTenancy();
        Integer tenancyUnit = packageEntity.getTenancyUnit();
        long dueTime = System.currentTimeMillis();
        if (TimeUnitEnum.DAY.getCode().equals(tenancyUnit)) {
            dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
        }
        if (TimeUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
            dueTime = dueTime + (tenancy * 1000);
        }

        carRentalPackageMemberTermPO.setDueTime(dueTime);
        carRentalPackageMemberTermPO.setDueTimeTotal(dueTime);
        carRentalPackageMemberTermPO.setResidue(packageEntity.getConfineNum());
        carRentalPackageMemberTermPO.setResidueTotal(carRentalPackageMemberTermPO.getResidue());
        carRentalPackageMemberTermPO.setStatus(MemberTermStatusEnum.PENDING_EFFECTIVE.getCode());
        carRentalPackageMemberTermPO.setDeposit(carRentalPackageOrderEntity.getDeposit());
        carRentalPackageMemberTermPO.setTenantId(tenantId);
        // TODO 加盟商ID
        carRentalPackageMemberTermPO.setFranchiseeId(0);
        // TODO 门店ID
        carRentalPackageMemberTermPO.setStoreId(0);
        carRentalPackageMemberTermPO.setCreateUid(uid);
        carRentalPackageMemberTermPO.setUpdateUid(uid);
        carRentalPackageMemberTermPO.setCreateTime(System.currentTimeMillis());
        carRentalPackageMemberTermPO.setUpdateTime(System.currentTimeMillis());
        carRentalPackageMemberTermPO.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackageMemberTermPO;
    }

    /**
     * 构建保险订单信息
     * @return
     */
    private InsuranceOrder buildInsuranceOrder(Long uid) {
        // TODO 赋值具体值
        InsuranceOrder insuranceOrder = new InsuranceOrder();
        return insuranceOrder;
    }

    /**
     * 构建押金订单信息
     * @param tenantId
     * @param uid
     * @return
     */
    private CarRentalPackageDepositPayPO buildCarRentalPackageDepositPay(Integer tenantId, Long uid, BigDecimal deposit, Integer depositExemption) {
        CarRentalPackageDepositPayPO carRentalPackageDepositPayEntity = new CarRentalPackageDepositPayPO();
        carRentalPackageDepositPayEntity.setUid(uid);
        carRentalPackageDepositPayEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, uid));
        carRentalPackageDepositPayEntity.setRentalPackageId(0L);
        carRentalPackageDepositPayEntity.setRentalPackageType(0);
        carRentalPackageDepositPayEntity.setType(DepositTypeEnum.NORMAL.getCode());
        carRentalPackageDepositPayEntity.setChangeAmount(BigDecimal.ZERO);
        carRentalPackageDepositPayEntity.setDeposit(deposit);
        carRentalPackageDepositPayEntity.setDepositExemption(depositExemption);
        carRentalPackageDepositPayEntity.setDepositRebateApprove(0);
        // TODO 这两个需要看是否抽取，若此方法服务于C端，则此值正常，若运营端，则有点问题
        carRentalPackageDepositPayEntity.setPayType(PayTypeEnum.ON_LINE.getCode());
        carRentalPackageDepositPayEntity.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackageDepositPayEntity.setRefundFlag(YesNoEnum.NO.getCode());
        carRentalPackageDepositPayEntity.setTenantId(tenantId);
        // TODO 加盟商取值
        carRentalPackageDepositPayEntity.setFranchiseeId(0);
        // TODO 门店取值
        carRentalPackageDepositPayEntity.setStoreId(0);
        carRentalPackageDepositPayEntity.setCreateUid(uid);
        carRentalPackageDepositPayEntity.setUpdateUid(uid);
        carRentalPackageDepositPayEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageDepositPayEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageDepositPayEntity.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackageDepositPayEntity;
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
    private CarRentalPackageOrderPO buildCarRentalPackageOrder(CarRentalPackagePO packagePO, BigDecimal paymentAmount, Integer tenantId, Long uid, String depositPayOrderNo) {

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
        carRentalPackage.setDepositPayOrderNo(depositPayOrderNo);
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
