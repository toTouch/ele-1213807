package com.xiliulou.electricity.service.impl.car.biz;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageMemberTermOptModel;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageOrderBizService;
import com.xiliulou.electricity.service.car.biz.RentalPackageBizService;
import com.xiliulou.electricity.service.car.biz.SlippageBizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVO;
import com.xiliulou.electricity.vo.car.CarVO;
import com.xiliulou.electricity.vo.rental.RentalPackageVO;
import com.xiliulou.mq.service.RocketMqService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租车套餐购买业务聚合 BizServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderBizServiceImpl implements CarRentalPackageOrderBizService {

    @Resource
    private SlippageBizService slippageBizService;

    @Resource
    private ElectricityCarService electricityCarService;

    @Resource
    private UserCarService userCarService;

    @Resource
    private UserBizService userBizService;

    @Resource
    private RocketMqService rocketMqService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private UserOauthBindService userOauthBindService;

    @Resource
    private ElectricityPayParamsService electricityPayParamsService;

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
     * 根据用户ID查询正在使用的套餐信息<br />
     * 复合查询，车辆信息、门店信息、GPS信息、电池信息、保险信息
     *
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return com.xiliulou.core.web.R
     * @author xiaohui.song
     **/
    @Override
    public R<RentalPackageVO> queryUseRentalPackageOrderByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 1. 查询会员期限信息
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            return R.ok();
        }

        // 2. 查询套餐信息
        CarRentalPackagePO carRentalPackageEntity = carRentalPackageService.selectById(memberTermEntity.getRentalPackageId());

        // 3. 查询用户车辆信息
        UserCar userCar = userCarService.selectByUidFromCache(uid);

        // 4. 查询车辆相关信息
        CarInfoDO carInfoDO = electricityCarService.queryByCarId(tenantId, userCar.getCid());

        // 5. TODO 查询保险信息，志龙

        // 6. TODO 电池消息，志龙

        // 7. 滞纳金信息
        String lateFeeAmount = slippageBizService.queryCarPackageUnpaidAmountByUid(tenantId, uid);

        // 构建返回信息
        RentalPackageVO rentalPackageVO = buildRentalPackageVO(memberTermEntity, carRentalPackageEntity, carInfoDO, lateFeeAmount);

        return R.ok(rentalPackageVO);
    }

    private RentalPackageVO buildRentalPackageVO(CarRentalPackageMemberTermPO memberTermEntity, CarRentalPackagePO carRentalPackageEntity, CarInfoDO carInfoDO, String lateFeeAmount) {
        // 构建返回值
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            return null;
        }
        RentalPackageVO rentalPackageVO = new RentalPackageVO();
        rentalPackageVO.setStatus(memberTermEntity.getStatus());
        rentalPackageVO.setDeadlineTime(memberTermEntity.getDueTimeTotal());
        rentalPackageVO.setLateFeeAmount(lateFeeAmount);

        // 套餐订单信息
        CarRentalPackageOrderVO carRentalPackageOrderVO = new CarRentalPackageOrderVO();
        carRentalPackageOrderVO.setOrderNo(memberTermEntity.getRentalPackageOrderNo());
        carRentalPackageOrderVO.setRentalPackageType(carRentalPackageEntity.getType());
        carRentalPackageOrderVO.setConfine(carRentalPackageEntity.getConfine());
        carRentalPackageOrderVO.setConfineNum(carRentalPackageEntity.getConfineNum());
        carRentalPackageOrderVO.setTenancy(carRentalPackageEntity.getTenancy());
        carRentalPackageOrderVO.setTenancyUnit(carRentalPackageEntity.getTenancyUnit());
        carRentalPackageOrderVO.setRent(carRentalPackageEntity.getRent());
        carRentalPackageOrderVO.setCarModelId(carRentalPackageEntity.getCarModelId());
        carRentalPackageOrderVO.setCarRentalPackageName(carRentalPackageEntity.getName());
        // 赋值套餐订单信息
        rentalPackageVO.setCarRentalPackageOrder(carRentalPackageOrderVO);

        // 车辆信息
        if (ObjectUtils.isNotEmpty(carInfoDO)) {
            CarVO carVO = new CarVO();
            carVO.setCarSn(carInfoDO.getCarSn());
            carVO.setStoreName(carInfoDO.getStoreName());
            carVO.setLatitude(carInfoDO.getLatitude());
            carVO.setLongitude(carInfoDO.getLongitude());
            // 赋值车辆信息
            rentalPackageVO.setCar(carVO);
        }

        return rentalPackageVO;
    }

    /**
     * 租车套餐订单
     *
     * @param orderNo  租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Override
    public Boolean cancelRentalPackageOrder(String orderNo, Integer tenantId, Long uid) {
        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPO carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(carRentalPackageOrderEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_order, order_no is {}", orderNo);
            // TODO 错误码定义
            throw new BizException("", "未找到租车套餐购买订单");
        }

        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), carRentalPackageOrderEntity.getPayState())) {
            log.error("CancelRentalPackageOrder failed, car_rental_package_order processed, order_no is {}", orderNo);
            // TODO 错误码定义
            throw new BizException("", "租车套餐购买订单已处理");
        }

        // 更改套餐购买订单的支付状态
        carRentalPackageOrderService.updatePayStateByOrderNo(orderNo, PayStateEnum.CANCEL.getCode());

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = carRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            // TODO 错误码定义
            throw new BizException("", "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.CANCEL.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_member_term, uid is {}", uid);
            // TODO 错误码定义
            throw new BizException("", "未找到租车会员记录信息");
        }

        // 待生效的数据，直接删除
        if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            carRentalPackageMemberTermService.delByUidAndTenantId(tenantId, uid, uid);
        }

        // 4. 处理用户押金支付信息（保持原样，不做处理）

        // 5. 处理用户优惠券的使用状态
        userCouponService.updateStatusByOrderId(orderNo, OrderTypeEnum.CAR_BUY_ORDER.getCode(), UserCoupon.STATUS_UNUSED);

        // 7. TODO 处理保险购买订单

        return true;
    }

    /**
     * 租车套餐订单，购买/续租
     * @param buyOptModel
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R buyRentalPackageOrder(CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request) {
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
            if (!redisService.setNx(buyLockKey, "1", 10 * 1000L, false)) {
                return R.fail("ELECTRICITY.0034", "操作频繁");
            }

            // 下单前的统一拦截校验
            rentalPackageBizService.checkBuyPackageCommon(tenantId, uid);

            // 2. 支付相关
            ElectricityPayParams payParamsEntity = electricityPayParamsService.queryFromCache(tenantId);
            if (Objects.isNull(payParamsEntity)) {
                log.error("CheckBuyPackageCommon failed. Not found pay_params. uid is {}", uid);
                throw new BizException("未配置支付参数");
            }

            // 3. 三方授权相关
            UserOauthBind userOauthBindEntity = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
            if (Objects.isNull(userOauthBindEntity) || Objects.isNull(userOauthBindEntity.getThirdId())) {
                log.error("CheckBuyPackageCommon failed. Not found useroauthbind or thirdid is null. uid is {}", uid);
                throw new BizException("未找到用户的第三方授权信息");
            }

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

            // 2.3 判定用户是否是老用户，然后和套餐的适用类型做比对
            Boolean oldUserFlag = userBizService.isOldUser(tenantId, uid);
            if (oldUserFlag && !ApplicableTypeEnum.oldUserApplicable().contains(packageEntity.getApplicableType())) {
                return R.fail("300205", "套餐不匹配");
            }

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
            }

            // 此处代表用户名下有租车套餐（单车或车电一体）
            // 3.3 用户名下的套餐类型和即将购买的套餐类型不一致
            if (!memberTermEntity.getRentalPackageType().equals(packageEntity.getType())) {
                log.error("BuyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is {}", packageEntity.getType(), memberTermEntity.getRentalPackageType());
                return R.fail("300205", "套餐不匹配");
            }

            // 3.4 若类型一致的情况下，则比对：型号（车、电） + 押金 + 套餐限制
            String rentalPackageOrderNo = memberTermEntity.getRentalPackageOrderNo();

            // 未退租
            if (StringUtils.isNotBlank(rentalPackageOrderNo)) {
                // 根据套餐购买订单编号，获取套餐购买订单表，读取其中的套餐快照信息
                CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(rentalPackageOrderNo);

                // 已经购买的套餐订单
                Integer oriCarModelId = packageOrderEntity.getCarModelId();
                BigDecimal oriDeposit = packageOrderEntity.getDeposit();
                Integer oriConfine = packageOrderEntity.getConfine();
                List<String> oriBatteryModelIds = Arrays.asList(packageOrderEntity.getBatteryModelIds().split(","));

                // 要下单的套餐订单
                Integer buyCarModelId = packageEntity.getCarModelId();
                BigDecimal buyDeposit = packageEntity.getDeposit();
                Integer buyConfine = packageEntity.getConfine();
                List<String> buyBatteryModelIds = Arrays.asList(packageEntity.getBatteryModelIds().split(","));

                // 车辆型号、押金、套餐限制，任意一个不一致，则判定为不一致套餐
                if (!buyCarModelId.equals(oriCarModelId) || buyDeposit.compareTo(oriDeposit) != 0 || !buyConfine.equals(oriConfine)) {
                    return R.fail("300205", "套餐不匹配");
                }

                // 电池型号，若新买的，没有完全包含于已经购买的，则不允许购买
                if (buyBatteryModelIds.size() < oriBatteryModelIds.size() || !buyBatteryModelIds.containsAll(oriBatteryModelIds)) {
                    return R.fail("300205", "套餐不匹配");
                }
            }

            // 退租未退押，押金不一致
            if (deposit.compareTo(packageEntity.getDeposit()) != 0) {
                return R.fail("300205", "套餐不匹配");
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
                depositPayInsertEntity = buildCarRentalPackageDepositPay(tenantId, uid, packageEntity.getDeposit(), DepositExemptionEnum.NO.getCode(), packageEntity.getFranchiseeId(), packageEntity.getStoreId());
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
            // TODO 志龙提供接口，根据车辆型号、电池型号（电压伏数）查询是否存在保险
             InsuranceOrder insuranceOrderInsertEntity = buildInsuranceOrder(uid);
            // 保险费用初始化
            BigDecimal insuranceAmount = BigDecimal.ZERO;

            // TODO 柜机的判定，此逻辑取决于是否有购买来源（柜机、非柜机）

            // 11. 计算金额（叠加优惠券、押金、保险）
            // 优惠券只抵扣租金
            Triple<BigDecimal, List<Long>, Boolean> couponTriple = rentalPackageBizService.calculatePaymentAmount(packageEntity.getRent(), buyOptModel.getUserCouponIds(), uid);

            // 实际支付租金金额
            BigDecimal rentPaymentAmount = couponTriple.getLeft();
            // 实际支付总金额（租金 + 押金 + 保险）
            BigDecimal paymentAmount = rentPaymentAmount.add(deposit).add(insuranceAmount);
            List<Long> userCouponIds = couponTriple.getMiddle();

            // 判定 depositPayInsertEntity 是否需要新增
            if (!ObjectUtils.isEmpty(depositPayInsertEntity)) {
                carRentalPackageDepositPayService.insert(depositPayInsertEntity);
            }

            // 生成租车套餐订单，准备 insert
            CarRentalPackageOrderPO carRentalPackageOrder = buildCarRentalPackageOrder(packageEntity, rentPaymentAmount, tenantId, uid, depositPayOrderNo);
            carRentalPackageOrderService.insert(carRentalPackageOrder);
            // 生成用户优惠券使用信息(核销中[实际意义：被占用])，准备 Update
            // 判定 memberTermEntity
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                // 生成租车套餐会员期限表信息，准备 Insert
                CarRentalPackageMemberTermPO memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, packageEntity, carRentalPackageOrder);
                carRentalPackageMemberTermService.insert(memberTermInsertEntity);
            }

            // 支付零元的处理
            if (BigDecimal.ZERO.compareTo(paymentAmount) >= 0) {
                // 无须唤起支付，走支付回调的逻辑，抽取方法，直接调用
                handBuyRentalPackageOrderSuccess(carRentalPackageOrder.getOrderNo(), tenantId, uid);
                return R.ok();
            }

            // 更改用户优惠券状态使用中
            List<UserCoupon> userCouponList = buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, carRentalPackageOrder.getOrderNo(), OrderTypeEnum.CAR_BUY_ORDER.getCode());
            userCouponService.batchUpdateUserCoupon(userCouponList);

            // 唤起支付
            CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                    .orderId(carRentalPackageOrder.getOrderNo())
                    .uid(uid)
                    .payAmount(paymentAmount)
                    .orderType(CallBackEnums.CAR_RENAL_PACKAGE_ORDER.getCode())
                    .attach(CallBackEnums.CAR_RENAL_PACKAGE_ORDER.getDesc())
                    .description("租车套餐购买收费")
                    .tenantId(tenantId).build();

            WechatJsapiOrderResultDTO resultDTO =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, payParamsEntity, userOauthBindEntity.getThirdId(), request);

            return R.ok(resultDTO);
        } catch (Exception e) {
            log.error("BuyRentalPackageOrder failed. ", e);

        } finally {
            redisService.delete(buyLockKey);
        }

        return R.ok();
    }

    /**
     * 支付成功之后的逻辑<br />
     * 此处逻辑不包含回调处理，是回调逻辑中的一处子逻辑<br />
     * 调用此方法需要慎重
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> handBuyRentalPackageOrderSuccess(String orderNo, Integer tenantId, Long uid) {
        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPO carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(carRentalPackageOrderEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_order, order_no is {}", orderNo);
            return Pair.of(false, "未找到租车套餐购买订单");
        }

        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), carRentalPackageOrderEntity.getPayState())) {
            log.error("NotifyCarRenalPackageOrder failed, car_rental_package_order processed, order_no is {}", orderNo);
            return Pair.of(false, "租车套餐购买订单已处理");
        }

        // 更改套餐购买订单的支付状态
        carRentalPackageOrderService.updatePayStateByOrderNo(orderNo, PayStateEnum.SUCCESS.getCode());

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = carRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            return Pair.of(false, "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.SUCCESS.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_member_term, uid is {}", uid);
            return Pair.of(false, "未找到租车会员记录信息");
        }

        // 待生效的数据，直接更改状态
        if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            carRentalPackageMemberTermService.updateStatusById(memberTermEntity.getId(), MemberTermStatusEnum.NORMAL.getCode(), null);
        }

        // 正常的数据，更改总计到期时间、总计套餐余量
        if (MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            CarRentalPackageMemberTermOptModel optModel = new CarRentalPackageMemberTermOptModel();
            optModel.setId(memberTermEntity.getId());

            // 计算总到期时间
            Integer tenancy = carRentalPackageOrderEntity.getTenancy();
            Integer tenancyUnit = carRentalPackageOrderEntity.getTenancyUnit();
            long dueTime = System.currentTimeMillis();
            if (TimeUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
            }
            if (TimeUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * 1000);
            }
            optModel.setDueTimeTotal(memberTermEntity.getDueTimeTotal() + dueTime);

            // 计算总套餐余量
            if (ObjectUtils.isNotEmpty(memberTermEntity.getResidueTotal())) {
                optModel.setResidueTotal(memberTermEntity.getResidueTotal() + carRentalPackageOrderEntity.getConfineNum());
            }

            carRentalPackageMemberTermService.updateById(optModel);
        }

        // 4. 处理用户押金支付信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("NotifyCarRenalPackageOrder failed, not found user_info, uid is {}", uid);
            return Pair.of(false, "未找到用户信息");
        }

        if (YesNoEnum.NO.getCode().equals(userInfo.getCarBatteryDepositStatus())) {
            LambdaUpdateWrapper<UserInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserInfo::getUid, uid).eq(UserInfo::getTenantId, tenantId)
                    .set(UserInfo::getUpdateTime, System.currentTimeMillis());
            if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(carRentalPackageOrderEntity.getRentalPackageType())) {
                updateWrapper.set(UserInfo::getCarBatteryDepositStatus, YesNoEnum.YES.getCode());
            } else {
                updateWrapper.set(UserInfo::getCarDepositStatus, YesNoEnum.YES.getCode());
            }
            userInfoService.update(updateWrapper);
        }

        // 5. 处理用户优惠券的使用状态
        userCouponService.updateStatusByOrderId(orderNo, OrderTypeEnum.CAR_BUY_ORDER.getCode(), UserCoupon.STATUS_USED);

        // 6. TODO 车辆断启电

        rocketMqService.sendAsyncMsg("topic", "msg");
        // 7. TODO 处理保险购买订单
        // 8. TODO 处理分账
        // 9. TODO 处理活动
        return Pair.of(true, userInfo.getPhone());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> handBuyRentalPackageOrderFailed(String orderNo, Integer tenantId, Long uid) {
        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPO carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
        if (ObjectUtil.isEmpty(carRentalPackageOrderEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_order, order_no is {}", orderNo);
            return Pair.of(false, "未找到租车套餐购买订单");
        }

        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), carRentalPackageOrderEntity.getPayState())) {
            log.error("NotifyCarRenalPackageOrder failed, car_rental_package_order processed, order_no is {}", orderNo);
            return Pair.of(false, "租车套餐购买订单已处理");
        }

        // 更改套餐购买订单的支付状态
        carRentalPackageOrderService.updatePayStateByOrderNo(orderNo, PayStateEnum.FAILED.getCode());

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = carRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            return Pair.of(false, "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.FAILED.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_member_term, uid is {}", uid);
            return Pair.of(false, "未找到租车会员记录信息");
        }

        // 待生效的数据，直接删除
        if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            carRentalPackageMemberTermService.delByUidAndTenantId(tenantId, uid, uid);
        }

        // 4. 处理用户押金支付信息（保持原样，不做处理）

        // 5. 处理用户优惠券的使用状态
        userCouponService.updateStatusByOrderId(orderNo, OrderTypeEnum.CAR_BUY_ORDER.getCode(), UserCoupon.STATUS_UNUSED);

        // 7. TODO 处理保险购买订单

        return Pair.of(true, null);
    }

    /**
     * 构建租车套餐会员期限信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageEntity 租车套餐信息
     * @param carRentalPackageOrderEntity 租车套餐订单信息
     * @return
     */
    private CarRentalPackageMemberTermPO buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePO packageEntity, CarRentalPackageOrderPO carRentalPackageOrderEntity) {
        CarRentalPackageMemberTermPO carRentalPackageMemberTermPO = new CarRentalPackageMemberTermPO();
        carRentalPackageMemberTermPO.setUid(uid);
        carRentalPackageMemberTermPO.setRentalPackageOrderNo(carRentalPackageOrderEntity.getOrderNo());
        carRentalPackageMemberTermPO.setRentalPackageId(packageEntity.getId());
        carRentalPackageMemberTermPO.setRentalPackageType(packageEntity.getType());
        carRentalPackageMemberTermPO.setRentalPackageConfine(packageEntity.getConfine());
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
        carRentalPackageMemberTermPO.setFranchiseeId(packageEntity.getFranchiseeId());
        carRentalPackageMemberTermPO.setStoreId(packageEntity.getStoreId());
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
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param deposit 押金
     * @param depositExemption 免押
     * @param franchiseeId 加盟商ID
     * @param storeId 门店ID
     * @return
     */
    private CarRentalPackageDepositPayPO buildCarRentalPackageDepositPay(Integer tenantId, Long uid, BigDecimal deposit, Integer depositExemption, Integer franchiseeId, Integer storeId) {
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
        carRentalPackageDepositPayEntity.setPayType(PayTypeEnum.ON_LINE.getCode());
        carRentalPackageDepositPayEntity.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackageDepositPayEntity.setRefundFlag(YesNoEnum.NO.getCode());
        carRentalPackageDepositPayEntity.setTenantId(tenantId);
        carRentalPackageDepositPayEntity.setFranchiseeId(franchiseeId);
        carRentalPackageDepositPayEntity.setStoreId(storeId);
        carRentalPackageDepositPayEntity.setCreateUid(uid);
        carRentalPackageDepositPayEntity.setUpdateUid(uid);
        carRentalPackageDepositPayEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageDepositPayEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageDepositPayEntity.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackageDepositPayEntity;
    }

    /**
     * 构建用户优惠券使用信息
     * @param userCouponIds 用户优惠券ID
     * @param status 状态
     * @param orderNo 订单编号
     * @param orderIdType 订单类型
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
     * @param packagePO 套餐信息
     * @param rentPayment 租金(支付价格)
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param depositPayOrderNo 押金缴纳订单编号
     * @return
     */
    private CarRentalPackageOrderPO buildCarRentalPackageOrder(CarRentalPackagePO packagePO, BigDecimal rentPayment, Integer tenantId, Long uid, String depositPayOrderNo) {

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
        carRentalPackage.setRentPayment(rentPayment);
        carRentalPackage.setCarModelId(packagePO.getCarModelId());
        carRentalPackage.setBatteryModelIds(packagePO.getBatteryModelIds());
        carRentalPackage.setApplicableType(packagePO.getApplicableType());
        carRentalPackage.setRentRebate(packagePO.getRentRebate());
        carRentalPackage.setRentRebateTerm(packagePO.getRentRebateTerm());
        carRentalPackage.setRentRebateEndTime(TimeConstant.DAY_MILLISECOND * packagePO.getRentRebateTerm() + System.currentTimeMillis());
        carRentalPackage.setDeposit(packagePO.getDeposit());
        carRentalPackage.setDepositPayOrderNo(depositPayOrderNo);
        carRentalPackage.setLateFee(packagePO.getLateFee());
        carRentalPackage.setPayType(PayTypeEnum.ON_LINE.getCode());
        carRentalPackage.setCouponId(packagePO.getCouponId());
        carRentalPackage.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackage.setUseState(UseStateEnum.UN_USED.getCode());
        carRentalPackage.setTenantId(tenantId);
        carRentalPackage.setFranchiseeId(packagePO.getFranchiseeId());
        carRentalPackage.setStoreId(packagePO.getStoreId());
        carRentalPackage.setCreateUid(uid);
        carRentalPackage.setUpdateUid(uid);
        carRentalPackage.setCreateTime(System.currentTimeMillis());
        carRentalPackage.setUpdateTime(System.currentTimeMillis());
        carRentalPackage.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackage;
    }
}
