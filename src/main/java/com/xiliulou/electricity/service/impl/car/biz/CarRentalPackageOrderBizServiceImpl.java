package com.xiliulou.electricity.service.impl.car.biz;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.domain.car.CarInfoDO;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.dto.UserCouponDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.*;
import com.xiliulou.electricity.entity.clickhouse.CarAttr;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageOrderFreezeQryModel;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.*;
import com.xiliulou.electricity.service.car.biz.*;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.ElectricityUserBatteryVo;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVo;
import com.xiliulou.electricity.vo.car.CarVo;
import com.xiliulou.electricity.vo.insurance.UserInsuranceVO;
import com.xiliulou.electricity.vo.rental.RefundRentOrderHintVo;
import com.xiliulou.electricity.vo.rental.RentalPackageVO;
import com.xiliulou.electricity.web.query.battery.BatteryInfoQuery;
import com.xiliulou.electricity.web.query.jt808.Jt808GetInfoRequest;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租车套餐购买业务聚合 BizServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageOrderBizServiceImpl implements CarRentalPackageOrderBizService {

    @Resource
    private UserBatteryDepositService userBatteryDepositService;

    @Resource
    private UserBatteryTypeService userBatteryTypeService;

    @Resource
    private BatteryModelService batteryModelService;

    @Resource
    private Jt808RetrofitService jt808RetrofitService;

    @Resource
    private CarRentalOrderService carRentalOrderService;

    @Resource
    private CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private CarRentalPackageOrderFreezeService carRentalPackageOrderFreezeService;

    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;

    @Resource
    private ActivityService activityService;

    @Resource
    private DivisionAccountRecordService divisionAccountRecordService;

    @Resource
    private ElectricityCarModelService carModelService;

    @Resource
    private CarRenalPackageDepositBizService carRenalPackageDepositBizService;

    @Resource(name = "wxRefundPayCarRentServiceImpl")
    private WxRefundPayService wxRefundPayService;

    @Resource
    private WechatConfig wechatConfig;

    @Resource
    private WechatV3JsapiService wechatV3JsapiService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private InsuranceOrderService insuranceOrderService;

    @Resource
    private FranchiseeInsuranceService franchiseeInsuranceService;

    @Resource
    private ElectricityBatteryService batteryService;

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    @Resource
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;

    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    @Resource
    private ElectricityCarService carService;

    @Resource
    private UserCarService userCarService;

    @Resource
    private UserBizService userBizService;

    @Resource
    private UserInfoService userInfoService;

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
    private CarRentalPackageBizService carRentalPackageBizService;

    @Resource
    private RedisService redisService;

    @Resource
    private CarRentalPackageCarBatteryRelService carRentalPackageCarBatteryRelService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    /**
     * 根据用户UID查询总金额<br />
     * 订单支付成功总金额 - 退租订单成功总金额
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return 总金额
     */
    @Override
    public BigDecimal queryAmountTotalByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        BigDecimal paySuccessAmountTotal = carRentalPackageOrderService.selectPaySuccessAmountTotal(tenantId, uid);
        BigDecimal refundSuccessAmountTotal = carRentalPackageOrderRentRefundService.selectRefundSuccessAmountTotal(tenantId, uid);

        return paySuccessAmountTotal.subtract(refundSuccessAmountTotal).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 退租提示
     *
     * @param tenantId       租户ID
     * @param uid            用户ID
     * @param packageOrderNo 套餐购买订单编码
     * @return 提示模型
     */
    @Override
    public RefundRentOrderHintVo refundRentOrderHint(Integer tenantId, Long uid, String packageOrderNo) {
        if (!ObjectUtils.allNotNull(tenantId, uid) || StringUtils.isBlank(packageOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        if (Objects.isNull(userInfo)) {
            log.error("CarRentalPackageOrderBizService.refundRentOrderHint failed. not found user. uid is {}", uid);
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CarRentalPackageOrderBizService.refundRentOrderHint failed. user is disable. uid is {}", uid);
            throw new BizException( "ELECTRICITY.0024", "用户已被禁用");
        }

        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRentalPackageOrderBizService.refundRentOrderHint failed. t_car_rental_package_member_term not found or status is error. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        long now = System.currentTimeMillis();
        if (memberTermEntity.getDueTimeTotal() < now) {
            log.error("CarRentalPackageOrderBizService.refundRentOrderHint failed. t_car_rental_package_member_term due time total is {}. now is {}", memberTermEntity.getDueTimeTotal(), now);
            throw new BizException("300032", "套餐已过期，无法申请退租");
        }

        // 查询套餐购买订单
        CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(packageOrderNo);
        if (ObjectUtils.isEmpty(packageOrderEntity) || ObjectUtils.notEqual(tenantId, packageOrderEntity.getTenantId()) || ObjectUtils.notEqual(uid, packageOrderEntity.getUid())) {
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        if (ObjectUtils.notEqual(YesNoEnum.YES.getCode(), packageOrderEntity.getRentRebate())) {
            throw new BizException("300012", "订单不允许退租");
        } else {
            if (now >= packageOrderEntity.getRentRebateEndTime()) {
                throw new BizException("300013", "订单超过可退期限");
            }
        }

        if (ObjectUtils.notEqual(PayStateEnum.SUCCESS.getCode(), packageOrderEntity.getPayState())) {
            throw new BizException("300014", "订单支付异常");
        }

        if (UseStateEnum.notRefundCodes().contains(packageOrderEntity.getUseState())) {
            throw new BizException("300015", "订单状态异常");
        }

        // 购买的时候，赠送的优惠券是否被使用，若为使用中、已使用，则不允许退租
        UserCoupon userCoupon = userCouponService.selectBySourceOrderId(packageOrderEntity.getOrderNo());
        if (ObjectUtils.isNotEmpty(userCoupon)) {
            Integer status = userCoupon.getStatus();
            if (UserCoupon.STATUS_IS_BEING_VERIFICATION.equals(status) || UserCoupon.STATUS_USED.equals(status)) {
                throw new BizException("300016", "您已使用优惠券，该套餐不可退");
            }
        }

        if (UseStateEnum.IN_USE.getCode().equals(packageOrderEntity.getUseState())) {
            if (carRentalPackageOrderService.isExitUnUseAndRefund(tenantId, uid, now)) {
                throw new BizException("300017", "存在未使用的订单");
            }
            // 查询设备信息，存在设备，不允许退租
            ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
            if (ObjectUtils.isNotEmpty(electricityCar) && ObjectUtils.isNotEmpty(electricityCar.getSn()) ) {
                throw new BizException("300018", "存在未归还的车辆");
            }
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageOrderEntity.getRentalPackageType())) {
                ElectricityBattery battery = batteryService.queryByUid(uid);
                if (ObjectUtils.isNotEmpty(battery)) {
                    throw new BizException("300019", "存在未归还的电池");
                }
            }
        }

        // 计算实际应退金额及余量
        Triple<BigDecimal, Long, Long> refundAmountPair = calculateRefundAmount(packageOrderEntity, tenantId, uid);

        // 拼装返回数据
        RefundRentOrderHintVo refundRentOrderHintVo = new RefundRentOrderHintVo();
        refundRentOrderHintVo.setRentPayment(packageOrderEntity.getRentPayment());
        refundRentOrderHintVo.setRefundAmount(refundAmountPair.getLeft());
        refundRentOrderHintVo.setTenancyResidue(refundAmountPair.getMiddle());
        refundRentOrderHintVo.setTenancyResidueUnit(packageOrderEntity.getTenancyUnit());
        refundRentOrderHintVo.setConfine(packageOrderEntity.getConfine());
        refundRentOrderHintVo.setConfineResidue(refundAmountPair.getRight());

        return refundRentOrderHintVo;

    }

    /**
     * 后端给用户绑定套餐
     *
     * @param buyOptModel 购买套餐数据模型
     * @return true(成功)、false(失败)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindingPackage(CarRentalPackageOrderBuyOptModel buyOptModel) {
        if (!ObjectUtils.allNotNull(buyOptModel, buyOptModel.getTenantId(), buyOptModel.getUid(), buyOptModel.getFranchiseeId(), buyOptModel.getStoreId(), buyOptModel.getRentalPackageId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = buyOptModel.getTenantId();
        Long uid = buyOptModel.getUid();
        Long buyRentalPackageId = buyOptModel.getRentalPackageId();

        // 获取加锁 KEY
        String bindingUidLockKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_BUY_UID_KEY, uid);

        try {
            // 加锁
            if (!redisService.setNx(bindingUidLockKey, uid.toString(), 10 * 1000L, false)) {
                throw new BizException("ELECTRICITY.0034", "操作频繁");
            }

            // 1 获取用户信息
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.error("bindingPackage failed. Not found user. uid is {} ", uid);
                throw new BizException("ELECTRICITY.0001", "未找到用户");
            }

            // 1.1 用户可用状态
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("bindingPackage failed. User is unUsable. uid is {} ", uid);
                throw new BizException("ELECTRICITY.0024", "用户已被禁用");
            }

            // 1.2 用户实名认证状态
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("CheckBuyPackageCommon failed. User not auth. uid is {}", uid);
                throw new BizException("ELECTRICITY.0041", "用户尚未实名认证");
            }

            // 2. 判定滞纳金
            if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
                log.error("bindingPackage failed. Late fee not paid. uid is {}", uid);
                throw new BizException("300001", "存在滞纳金，请先缴纳");
            }

            // 初始化押金金额
            BigDecimal deposit = null;
            Integer userTenantId = userInfo.getTenantId();
            Long userFranchiseeId = Long.valueOf(buyOptModel.getFranchiseeId());
            Long userStoreId = Long.valueOf(buyOptModel.getStoreId());

            if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId()) && userInfo.getFranchiseeId() != 0L && !userFranchiseeId.equals(userInfo.getFranchiseeId())) {
                log.error("bindingPackage failed. userInfo's franchiseeId is {}. params franchiseeId is {}", userInfo.getFranchiseeId(), buyOptModel.getFranchiseeId());
                throw new BizException("300036", "所属机构不匹配");
            }
            if (ObjectUtils.isNotEmpty(userInfo.getStoreId()) && userInfo.getStoreId() != 0L && !userStoreId.equals(userInfo.getStoreId())) {
                log.error("bindingPackage failed. userInfo's storeId is {}. params storeId is {}", userInfo.getStoreId(), buyOptModel.getStoreId());
                throw new BizException("300036", "所属机构不匹配");
            }

            // 5. 获取租车套餐会员期限信息
            Integer rentalPackageConfine = null;
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 若非空，则押金必定缴纳
            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                // 非待生效
                if (!MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    // 5.1 用户套餐会员限制状态异常
                    if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
                        log.error("bindingPackage failed. member_term status wrong. uid is {}, status is {}", uid, memberTermEntity.getStatus());
                        throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
                    }
                    // 从会员期限中获取押金金额
                    log.info("bindingPackage deposit from memberTerm");
                    deposit = memberTermEntity.getDeposit();
                }
            }
            // 6. 获取套餐信息
            // 6.1 套餐不存在
            CarRentalPackagePo buyPackageEntity = carRentalPackageService.selectById(buyRentalPackageId);
            if (ObjectUtils.isEmpty(buyPackageEntity)) {
                log.error("bindingPackage failed. Package does not exist, buyRentalPackageId is {}", buyRentalPackageId);
                throw new BizException("300003", "套餐不存在");
            }

            // 6.2 套餐上下架状态
            if (UpDownEnum.DOWN.getCode().equals(buyPackageEntity.getStatus())) {
                log.error("bindingPackage failed. Package status is down. buyRentalPackageId is {}", buyRentalPackageId);
                throw new BizException("300004", "套餐已下架");
            }

            // 6.3 判定用户是否是老用户，然后和套餐的适用类型做比对
            Boolean oldUserFlag = userBizService.isOldUser(tenantId, uid);
            if (oldUserFlag && !ApplicableTypeEnum.oldUserApplicable().contains(buyPackageEntity.getApplicableType())) {
                log.error("bindingPackage failed. Package type mismatch. Buy package type is {}, user is old", buyPackageEntity.getApplicableType());
                throw new BizException("300005", "套餐不匹配");
            }

            // 7. 判定套餐互斥
            // 7.1 车或者电与车电一体互斥
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyPackageEntity.getType()) &&
                    (!UserInfo.BATTERY_DEPOSIT_STATUS_NO.equals(userInfo.getBatteryDepositStatus()) || !UserInfo.CAR_DEPOSIT_STATUS_NO.equals(userInfo.getCarDepositStatus()))) {
                log.error("bindingPackage failed. Package type mismatch. Buy package type is {}, user package type is battery", buyPackageEntity.getType());
                throw new BizException("300005", "套餐不匹配");
            }

            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                // 此处代表用户名下有租车套餐（单车或车电一体）
                if (!MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    // 7.2 用户名下的套餐类型和即将购买的套餐类型不一致
                    if (!memberTermEntity.getRentalPackageType().equals(buyPackageEntity.getType())) {
                        log.error("bindingPackage failed. Package type mismatch. Buy package type is {}, user package type is {}", buyPackageEntity.getType(), memberTermEntity.getRentalPackageType());
                        throw new BizException("300005", "套餐不匹配");
                    }
                    userTenantId = memberTermEntity.getTenantId();
                    userFranchiseeId = Long.valueOf(memberTermEntity.getFranchiseeId());
                    userStoreId = Long.valueOf(memberTermEntity.getStoreId());
                    rentalPackageConfine = memberTermEntity.getRentalPackageConfine();
                }
            }

            // 7.3 用户归属和套餐归属不一致(租户、加盟商、门店)，拦截
            if (ObjectUtils.notEqual(userStoreId, UserInfo.VIRTUALLY_STORE_ID)) {
                if (ObjectUtils.notEqual(userTenantId, buyPackageEntity.getTenantId()) || ObjectUtils.notEqual(userFranchiseeId, Long.valueOf(buyPackageEntity.getFranchiseeId()))
                        || ObjectUtils.notEqual(userStoreId, Long.valueOf(buyPackageEntity.getStoreId()))) {
                    log.error("bindingPackage failed. Package belong mismatch. ");
                    throw new BizException("300005", "套餐不匹配");
                }
            } else {
                if (ObjectUtils.notEqual(userTenantId, buyPackageEntity.getTenantId()) || ObjectUtils.notEqual(userFranchiseeId, Long.valueOf(buyPackageEntity.getFranchiseeId()))) {
                    log.error("bindingPackage failed. Package belong mismatch. ");
                    throw new BizException("300005", "套餐不匹配");
                }
            }

            // 7.4 比对套餐限制
            if (ObjectUtils.isNotEmpty(rentalPackageConfine) && !rentalPackageConfine.equals(buyPackageEntity.getConfine())) {
                log.error("bindingPackage failed. Package confine mismatch. ");
                throw new BizException("300005", "套餐不匹配");
            }

            if (ObjectUtils.isEmpty(deposit)) {
                log.info("BuyRentalPackageOrder deposit from rentalPackage.");
                deposit = buyPackageEntity.getDeposit();
            }

            // 7.4 类型一致、归属一致，比对：型号（车或者车、电） + 押金
            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                if (!MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    if (buyPackageEntity.getDeposit().compareTo(deposit) != 0) {
                        log.error("bindingPackage failed. Package deposit mismatch. ");
                        throw new BizException("300005", "套餐不匹配");
                    }
                    // 比对型号
                    Long  oriRentalPackageId = memberTermEntity.getRentalPackageId();
                    if (ObjectUtils.isEmpty(oriRentalPackageId)) {
                        // 查找押金缴纳的套餐ID
                        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(memberTermEntity.getDepositPayOrderNo());
                        oriRentalPackageId = depositPayEntity.getRentalPackageId();
                    }

                    // 比对车辆型号
                    CarRentalPackagePo oriCarRentalPackageEntity = carRentalPackageService.selectById(oriRentalPackageId);
                    if (!oriCarRentalPackageEntity.getCarModelId().equals(buyPackageEntity.getCarModelId())) {
                        log.error("bindingPackage failed. Package carModelId mismatch. ");
                        throw new BizException("300005", "套餐不匹配");
                    }
                    // 车电一体，比对电池型号
                    if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(oriCarRentalPackageEntity.getType())) {
                        // 恶心的逻辑判断，加盟商，存在多型号电池和单型号电池，若单型号电池，则电池型号为空
                        Franchisee franchisee = franchiseeService.queryByIdFromCache(userFranchiseeId);
                        if (ObjectUtils.isEmpty(franchisee)) {
                            log.error("bindingPackage failed. not found franchisee. franchiseeId is {}", userFranchiseeId);
                            throw new BizException("300000", "数据有误");
                        }
                        if (Franchisee.NEW_MODEL_TYPE.equals(franchisee.getModelType())) {
                            List<String> oriBatteryList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(oriCarRentalPackageEntity.getId()).stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).collect(Collectors.toList());
                            // TODO 临时处理
                            List<String> oriBatterySimpleList = oriBatteryList.stream().map(n -> {
                                StringJoiner simpleModel = new StringJoiner("_");
                                String[] strings = n.split("_");
                                simpleModel.add(strings[0]).add(strings[1]).add(strings[strings.length - 1]);
                                return simpleModel.toString();
                            }).collect(Collectors.toList());
                            // TODO 临时处理
                            List<String> buyBatteryList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(buyPackageEntity.getId()).stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).collect(Collectors.toList());
                            List<String> buyBatterySimpleList = buyBatteryList.stream().map(n -> {
                                StringJoiner simpleModel = new StringJoiner("_");
                                String[] strings = n.split("_");
                                simpleModel.add(strings[0]).add(strings[1]).add(strings[strings.length - 1]);
                                return simpleModel.toString();
                            }).collect(Collectors.toList());

                            if (!buyBatterySimpleList.containsAll(oriBatterySimpleList)) {
                                log.error("bindingPackage failed. Package battery mismatch. ");
                                throw new BizException("300005", "套餐不匹配");
                            }
                        }
                    }
                }
            }

            // 检测结束，进入购买阶段
            Integer payType = buyOptModel.getPayType();
            // 1）押金处理
            // 待新增的押金信息，肯定没有走免押
            CarRentalPackageDepositPayPo depositPayInsertEntity = null;
            // 押金缴纳订单编码
            String depositPayOrderNo = null;
            CarRentalPackageDepositPayVo depositPayVo = carRenalPackageDepositBizService.selectUnRefundCarDeposit(tenantId, uid);
            // 没有押金订单，此时肯定也没有申请免押，因为免押是另外的线路，在下订单之前就已经生成记录了
            if (ObjectUtils.isEmpty(depositPayVo) || PayStateEnum.UNPAID.getCode().equals(depositPayVo.getPayState())) {
                // 生成押金缴纳订单，准备 insert
                depositPayInsertEntity = buildCarRentalPackageDepositPay(tenantId, uid, buyPackageEntity.getDeposit(), YesNoEnum.NO.getCode(), buyPackageEntity.getFranchiseeId(), buyPackageEntity.getStoreId(), buyPackageEntity.getType(), payType, buyPackageEntity.getId());
                depositPayOrderNo = depositPayInsertEntity.getOrderNo();
            } else {
                depositPayOrderNo = depositPayVo.getOrderNo();
                log.info("BuyRentalPackageOrder deposit paid. depositPayOrderNo is {}", depositPayOrderNo);
                deposit = BigDecimal.ZERO;
            }

            // 3）支付金额处理
            // 实际支付租金金额
            BigDecimal rentPaymentAmount = buyPackageEntity.getRent();
            log.info("BuyRentalPackageOrder rentPaymentAmount is {}", rentPaymentAmount);
            // 实际支付总金额（租金 + 押金 + 保险）
            BigDecimal paymentAmount = rentPaymentAmount.add(deposit);
            log.info("BuyRentalPackageOrder paymentAmount is {}", paymentAmount);

            // 4）生成租车套餐订单，准备 insert
            CarRentalPackageOrderPo carRentalPackageOrder = buildCarRentalPackageOrder(buyPackageEntity, rentPaymentAmount, tenantId, uid, depositPayOrderNo, payType);
            carRentalPackageOrderService.insert(carRentalPackageOrder);

            // 判定 depositPayInsertEntity 是否需要新增
            if (ObjectUtils.isNotEmpty(depositPayInsertEntity)) {
                depositPayInsertEntity.setRentalPackageOrderNo(carRentalPackageOrder.getOrderNo());
                carRentalPackageDepositPayService.insert(depositPayInsertEntity);
            }

            // 5）租车套餐会员期限处理
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                // 生成租车套餐会员期限表信息，准备 Insert
                CarRentalPackageMemberTermPo memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, buyPackageEntity, carRentalPackageOrder);
                carRentalPackageMemberTermService.insert(memberTermInsertEntity);
            } else {
                if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    // 先删除
                    carRentalPackageMemberTermService.delByUidAndTenantId(memberTermEntity.getTenantId(), memberTermEntity.getUid(), memberTermEntity.getUid());
                    // 生成租车套餐会员期限表信息，准备 Insert
                    CarRentalPackageMemberTermPo memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, buyPackageEntity, carRentalPackageOrder);
                    carRentalPackageMemberTermService.insert(memberTermInsertEntity);
                }
            }

            // 7）无须唤起支付，走支付回调的逻辑，抽取方法，直接调用
            handBuyRentalPackageOrderSuccess(carRentalPackageOrder.getOrderNo(), tenantId, uid, null);

        } catch (BizException e) {
            log.error("bindingPackage failed. ", e);
            throw new BizException(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("bindingPackage failed. ", e);
            throw new BizException("000001", "系统异常");
        } finally {
            redisService.delete(bindingUidLockKey);
        }

        return true;
    }

    /**
     * 审批冻结申请单
     *
     * @param freezeRentOrderNo 冻结申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @return
     */
    @Override
    public Boolean approveFreezeRentOrder(String freezeRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid) {
        if (!ObjectUtils.allNotNull(freezeRentOrderNo, approveFlag, apploveUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        CarRentalPackageOrderFreezePo freezeEntity = carRentalPackageOrderFreezeService.selectByOrderNo(freezeRentOrderNo);
        if (ObjectUtils.isEmpty(freezeEntity) || !RentalPackageOrderFreezeStatusEnum.PENDING_APPROVAL.getCode().equals(freezeEntity.getStatus())) {
            log.error("approveFreezeRentOrder faild. not find t_car_rental_package_order_freeze or status error. freezeRentOrderNo is {}", freezeRentOrderNo);
            throw new BizException("300000", "数据有误");
        }

        CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(freezeEntity.getRentalPackageOrderNo());
        if (ObjectUtils.isEmpty(packageOrderEntity)) {
            log.error("approveFreezeRentOrder faild. not find t_car_rental_package_order. orderNo is {}", freezeEntity.getRentalPackageOrderNo());
            throw new BizException("300000", "数据有误");
        }

        // 审核通过之后，生成滞纳金
        CarRentalPackageOrderSlippagePo slippageInsertEntity = null;
        CarRentalPackageMemberTermPo memberTermEntity = null;
        boolean expireFlag = false;

        long nowTime = System.currentTimeMillis();
        if (approveFlag) {
            // 判定会员状态以及套餐是否过期
            memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(freezeEntity.getTenantId(), freezeEntity.getUid());
            if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.APPLY_FREEZE.getCode().equals(memberTermEntity.getStatus())) {
                log.error("approveFreezeRentOrder faild. not find car_rental_package_member_term or status error. freezeRentOrderNo is {}, uid is {}", freezeRentOrderNo, freezeEntity.getUid());
                throw new BizException("300000", "数据有误");
            }

            if (!memberTermEntity.getRentalPackageOrderNo().equals(freezeEntity.getRentalPackageOrderNo())) {
                expireFlag = true;
            } else {
                if (nowTime >= memberTermEntity.getDueTime()
                        || (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTermEntity.getRentalPackageConfine()) && memberTermEntity.getResidue() <= 0L)) {
                    expireFlag = true;
                }
            }

            slippageInsertEntity = buildCarRentalPackageOrderSlippage(freezeEntity.getUid(), packageOrderEntity, nowTime);
        }

        // TX 事务落库
        saveApproveFreezeRentOrderTx(expireFlag, freezeRentOrderNo, approveFlag, apploveDesc, apploveUid, freezeEntity, slippageInsertEntity, memberTermEntity, nowTime);

        if (expireFlag) {
            log.error("approveFreezeRentOrder faild. The package has expired and cannot be approved. freezeRentOrderNo is {}", freezeRentOrderNo);
            throw new BizException("300030", "套餐已过期，无法审核");
        }
        return true;
    }


    /**
     * 审核冻结申请单事务处理
     * @param expireFlag 过期标识
     * @param freezeRentOrderNo 冻结申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @param freezeEntity      冻结申请单DB数据
     * @param slippageInsertEntity  滞纳金订单
     * @param memberTermEntity  会员期限DB数据
     * @return void
     * @author xiaohui.song
     **/
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveFreezeRentOrderTx(boolean expireFlag, String freezeRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid,
                                             CarRentalPackageOrderFreezePo freezeEntity, CarRentalPackageOrderSlippagePo slippageInsertEntity,
                                             CarRentalPackageMemberTermPo memberTermEntity, Long nowTime) {
        CarRentalPackageOrderFreezePo freezeUpdateEntity = new CarRentalPackageOrderFreezePo();
        freezeUpdateEntity.setOrderNo(freezeRentOrderNo);
        freezeUpdateEntity.setAuditTime(nowTime);
        freezeUpdateEntity.setRemark(apploveDesc);
        freezeUpdateEntity.setUpdateUid(apploveUid);

        // 过期
        if (expireFlag) {
            // 1. 更新冻结申请单状态
            freezeUpdateEntity.setStatus(RentalPackageOrderFreezeStatusEnum.LOSE_EFFICACY.getCode());
            carRentalPackageOrderFreezeService.updateByOrderNo(freezeUpdateEntity);

            // 2. 更新会员期限信息
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(freezeEntity.getTenantId(), freezeEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), apploveUid);
        } else {
            if (approveFlag) {
                // 1. 更新冻结申请单状态
                freezeUpdateEntity.setStatus(RentalPackageOrderFreezeStatusEnum.AUDIT_PASS.getCode());
                carRentalPackageOrderFreezeService.updateByOrderNo(freezeUpdateEntity);

                // 2. 更新会员期限信息
                CarRentalPackageMemberTermPo memberTermUpdateEntity = new CarRentalPackageMemberTermPo();
                memberTermUpdateEntity.setStatus(MemberTermStatusEnum.FREEZE.getCode());
                memberTermUpdateEntity.setId(memberTermEntity.getId());
                memberTermUpdateEntity.setUpdateUid(apploveUid);

                // 计算总的订单到期时间及当前订单到期时间
                // 计算规则：审核通过的时间 + 申请期限
                // Long extendTime = freezeUpdateEntity.getAuditTime() + (freezeEntity.getApplyTerm() * TimeConstant.DAY_MILLISECOND);
                // 计算规则：原有的时间 + 申请期限
                Long extendTime = (freezeEntity.getApplyTerm() * TimeConstant.DAY_MILLISECOND);
                memberTermUpdateEntity.setDueTime(memberTermEntity.getDueTime() + extendTime);
                memberTermUpdateEntity.setDueTimeTotal(memberTermEntity.getDueTimeTotal() + extendTime);

                carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);

                // 3. 保存滞纳金订单
                if (ObjectUtils.isNotEmpty(slippageInsertEntity)) {
                    slippageInsertEntity.setLateFeeStartTime(nowTime);
                    carRentalPackageOrderSlippageService.insert(slippageInsertEntity);
                }
            } else {
                // 1. 更新冻结申请单状态
                freezeUpdateEntity.setStatus(RentalPackageOrderFreezeStatusEnum.AUDIT_REJECT.getCode());
                carRentalPackageOrderFreezeService.updateByOrderNo(freezeUpdateEntity);

                // 2. 更新会员期限信息
                carRentalPackageMemberTermService.updateStatusByUidAndTenantId(freezeEntity.getTenantId(), freezeEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), apploveUid);
            }
        }

    }

    /**
     * 审批退租申请单
     *
     * @param refundRentOrderNo 退租申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @return
     */
    @Override
    public Boolean approveRefundRentOrder(String refundRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid) {
        if (!ObjectUtils.allNotNull(refundRentOrderNo, approveFlag, apploveUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 退租申请单
        CarRentalPackageOrderRentRefundPo rentRefundEntity = carRentalPackageOrderRentRefundService.selectByOrderNo(refundRentOrderNo);
        if (ObjectUtils.isEmpty(rentRefundEntity) || !RefundStateEnum.PENDING_APPROVAL.getCode().equals(rentRefundEntity.getRefundState())) {
            log.error("approveRefundRentOrder faild. not find car_rental_package_order_rent_refund or status error. refundRentOrderNo is {}", refundRentOrderNo);
            throw new BizException("300000", "数据有误");
        }

        // 租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(rentRefundEntity.getTenantId(), rentRefundEntity.getUid());
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("approveRefundRentOrder faild. not find t_car_rental_package_member_term. uid is {}", rentRefundEntity.getUid());
            throw new BizException("300000", "数据有误");
        }

        // 购买套餐编码
        String orderNo = rentRefundEntity.getRentalPackageOrderNo();
        CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
        if (ObjectUtils.isEmpty(packageOrderEntity) || UseStateEnum.EXPIRED.getCode().equals(packageOrderEntity.getUseState()) || UseStateEnum.RETURNED.getCode().equals(packageOrderEntity.getUseState())) {
            log.error("approveRefundRentOrder faild. not find t_car_rental_package_order or status error. orderNo is {}", orderNo);
            throw new BizException("300000", "数据有误");
        }

        // 购买订单的交易方式
        Integer payType = packageOrderEntity.getPayType();
        if (!(PayTypeEnum.OFF_LINE.getCode().equals(payType) || PayTypeEnum.ON_LINE.getCode().equals(payType))) {
            log.error("approveRefundRentOrder faild. t_car_rental_package_order payType error. payType is {}", payType);
            throw new BizException("300000", "数据有误");
        }

        // TX 事务落库
        saveApproveRefundRentOrderTx(refundRentOrderNo, approveFlag, apploveDesc, apploveUid, rentRefundEntity, packageOrderEntity);

        return true;
    }


    /**
     * 调用微信支付
     * @param refundOrder
     * @return
     * @throws WechatPayException
     */
    private WechatJsapiRefundResultDTO wxRefund(RefundOrder refundOrder) throws WechatPayException {
        //第三方订单号
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(refundOrder.getOrderId());
        if (ObjectUtils.isEmpty(electricityTradeOrder)) {
            log.error("CarRentalPackageOrderBizService.wxRefund failed, not found t_electricity_trade_order. orderId is {}", refundOrder.getOrderId());
            throw new BizException("300000", "数据有误");
        }

        //调用退款
        WechatV3RefundQuery wechatV3RefundQuery = new WechatV3RefundQuery();
        wechatV3RefundQuery.setTenantId(electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setTotal(electricityTradeOrder.getTotalFee().intValue());
        wechatV3RefundQuery.setRefund(refundOrder.getRefundAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundQuery.setReason("租金退款");
        wechatV3RefundQuery.setOrderId(electricityTradeOrder.getTradeOrderNo());
        wechatV3RefundQuery.setNotifyUrl(wechatConfig.getCarRentRefundCallBackUrl() + electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setCurrency("CNY");
        wechatV3RefundQuery.setRefundId(refundOrder.getRefundOrderNo());

        return wechatV3JsapiService.refund(wechatV3RefundQuery);
    }

    /**
     * 审核退租申请单事务处理
     * @param refundRentOrderNo 退租申请单编码
     * @param approveFlag       审批标识，true(同意)；false(驳回)
     * @param apploveDesc       审批意见
     * @param apploveUid        审批人
     * @param rentRefundEntity  退租申请单DB数据
     * @param packageOrderEntity  套餐购买订单信息
     * @return void
     * @author xiaohui.song
     **/
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveRefundRentOrderTx(String refundRentOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, CarRentalPackageOrderRentRefundPo rentRefundEntity,
                                             CarRentalPackageOrderPo packageOrderEntity) {

        CarRentalPackageOrderRentRefundPo rentRefundUpdateEntity = new CarRentalPackageOrderRentRefundPo();
        rentRefundUpdateEntity.setOrderNo(refundRentOrderNo);
        rentRefundUpdateEntity.setAuditTime(System.currentTimeMillis());
        rentRefundUpdateEntity.setRemark(apploveDesc);
        rentRefundUpdateEntity.setUpdateUid(apploveUid);

        // 审核通过
        if (approveFlag) {
            // 购买订单时的支付方式
            Integer payType = packageOrderEntity.getPayType();
            // 购买订单时的支付订单号
            String orderNo = packageOrderEntity.getOrderNo();

            // 非 0 元退租
            if (BigDecimal.ZERO.compareTo(rentRefundEntity.getRefundAmount()) < 0) {
                // 默认状态，审核通过
                rentRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_PASS.getCode());
                if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
                    // 线下，直接设置为退款成功
                    rentRefundUpdateEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
                    WechatJsapiRefundOrderCallBackResource callBackResource = new WechatJsapiRefundOrderCallBackResource();
                    callBackResource.setRefundStatus("SUCCESS");
                    callBackResource.setOutRefundNo(refundRentOrderNo);
                    wxRefundPayService.process(callBackResource);
                } else {
                    try {
                        // 根据购买订单编码获取当初的支付流水
                        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(orderNo);
                        if (ObjectUtils.isEmpty(electricityTradeOrder)) {
                            log.error("saveApproveRefundRentOrderTx faild. not find t_electricity_trade_order. orderNo is {}", orderNo);
                            throw new BizException("300000", "数据有误");
                        }
                        Integer status = electricityTradeOrder.getStatus();
                        if (ElectricityTradeOrder.STATUS_INIT.equals(status) || ElectricityTradeOrder.STATUS_FAIL.equals(status)) {
                            log.error("saveApproveRefundRentOrderTx faild. t_electricity_trade_order status is wrong. orderNo is {}", orderNo);
                            throw new BizException("300000", "数据有误");
                        }

                        // 调用微信支付，进行退款
                        RefundOrder refundOrder = RefundOrder.builder()
                                .orderId(electricityTradeOrder.getOrderNo())
                                .payAmount(electricityTradeOrder.getTotalFee())
                                .refundOrderNo(refundRentOrderNo)
                                .refundAmount(rentRefundEntity.getRefundAmount()).build();
                        log.info("saveApproveRefundRentOrderTx, Call WeChat refund. params is {}", JsonUtil.toJson(refundOrder));
                        WechatJsapiRefundResultDTO wxRefundDto = wxRefund(refundOrder);
                        log.info("saveApproveRefundRentOrderTx, Call WeChat refund. result is {}", JsonUtil.toJson(wxRefundDto));

                        // 赋值退款单状态及审核时间
                        rentRefundUpdateEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());

                    } catch (WechatPayException e) {
                        log.error("saveApproveRefundRentOrderTx failed.", e);
                        throw new BizException(e.getMessage());
                    }
                }
            } else {
                // 0 元退租
                rentRefundUpdateEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());

                WechatJsapiRefundOrderCallBackResource callBackResource = new WechatJsapiRefundOrderCallBackResource();
                callBackResource.setRefundStatus("SUCCESS");
                callBackResource.setOutRefundNo(refundRentOrderNo);
                wxRefundPayService.process(callBackResource);
            }

            carRentalPackageOrderRentRefundService.updateByOrderNo(rentRefundUpdateEntity);
        } else {
            // 1. 更新退租申请单状态
            rentRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_REJECT.getCode());
            carRentalPackageOrderRentRefundService.updateByOrderNo(rentRefundUpdateEntity);

            // 2. 更新会员期限
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(rentRefundEntity.getTenantId(), rentRefundEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), apploveUid);
        }
    }

    /**
     * 启用用户冻结订单<br />
     * 自动启用
     *
     * @param offset 偏移量
     * @param size 取值数量
     */
    @Override
    public void enableFreezeRentOrderAuto(Integer offset, Integer size) {
        // 初始化定义
        offset = ObjectUtils.isEmpty(offset) ? 0: offset;
        size = ObjectUtils.isEmpty(size) ? 500: size;

        boolean lookFlag = true;

        CarRentalPackageOrderFreezeQryModel qryModel = new CarRentalPackageOrderFreezeQryModel();
        qryModel.setStatus(RentalPackageOrderFreezeStatusEnum.AUDIT_PASS.getCode());
        qryModel.setSize(size);

        while (lookFlag) {
            qryModel.setOffset(offset);

            List<CarRentalPackageOrderFreezePo> pageEntityList = carRentalPackageOrderFreezeService.page(qryModel);
            if (CollectionUtils.isEmpty(pageEntityList)) {
                log.info("enableFreezeRentOrderAuto, The data is empty and does not need to be processed");
                lookFlag = false;
                break;
            }

            // 当前时间
            long nowTime = System.currentTimeMillis();
            // 比对时间，进行数据处理
            for (CarRentalPackageOrderFreezePo freezeEntity : pageEntityList) {
                try {
                    Integer applyTerm = freezeEntity.getApplyTerm();
                    Long createTime = freezeEntity.getCreateTime();
                    // 到期时间
                    long expireTime = createTime + (TimeConstant.DAY_MILLISECOND * applyTerm);

                    if (nowTime < expireTime) {
                        continue;
                    }

                    // 二次保险
                    CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByUidAndPackageOrderNo(freezeEntity.getTenantId(), freezeEntity.getUid(), freezeEntity.getRentalPackageOrderNo());
                    if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.FREEZE.getCode().equals(memberTermEntity.getStatus())) {
                        continue;
                    }

                    // 赋值会员更新
                    CarRentalPackageMemberTermPo memberTermUpdateEntity = new CarRentalPackageMemberTermPo();
                    memberTermUpdateEntity.setStatus(MemberTermStatusEnum.NORMAL.getCode());
                    memberTermUpdateEntity.setId(memberTermEntity.getId());
                    memberTermUpdateEntity.setUpdateTime(nowTime);

                    // 是否存在因冻结产生的滞纳金，若有，则更新停止时间
                    CarRentalPackageOrderSlippagePo slippagePo = carRentalPackageOrderSlippageService.selectByPackageOrderNoAndType(freezeEntity.getRentalPackageOrderNo(), SlippageTypeEnum.FREEZE.getCode());
                    CarRentalPackageOrderSlippagePo orderSlippageUpdate = null;
                    if (ObjectUtils.isNotEmpty(slippagePo)) {
                        orderSlippageUpdate = new CarRentalPackageOrderSlippagePo();
                        orderSlippageUpdate.setId(slippagePo.getId());
                        orderSlippageUpdate.setUpdateTime(nowTime);
                        // 启用时间
                        //Pair<Long, Integer> realTermPair = carRentalPackageOrderFreezeService.calculateRealTerm(applyTerm, freezeEntity.getApplyTime(), true);
                        orderSlippageUpdate.setLateFeeEndTime(expireTime);
                        // 计算滞纳金金额
                        long diffDay = DateUtils.diffDay(slippagePo.getLateFeeStartTime(), orderSlippageUpdate.getLateFeeEndTime());
                        orderSlippageUpdate.setLateFeePay(slippagePo.getLateFee().multiply(new BigDecimal(diffDay)).setScale(2, RoundingMode.HALF_UP));
                    }

                    // 事务处理
                    enableFreezeRentOrderTx(freezeEntity.getUid(), freezeEntity.getRentalPackageOrderNo(), true, null, memberTermUpdateEntity, orderSlippageUpdate);
                } catch (Exception e) {
                    log.info("enableFreezeRentOrderAuto, skip. error: ", e);
                    continue;
                }

            }
            offset += size;
        }

    }

    /**
     * 启用用户冻结订单申请<br />
     * 手动启用
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param optUid 操作人ID
     * @return true(成功)、false(失败)
     */
    @Override
    public Boolean enableFreezeRentOrder(Integer tenantId, Long uid, String packageOrderNo, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        if (Objects.isNull(userInfo)) {
            log.error("CarRentalPackageOrderBizService.enableFreezeRentOrder failed. not found user. uid is {}", uid);
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CarRentalPackageOrderBizService.enableFreezeRentOrder failed. user is disable. uid is {}", uid);
            throw new BizException( "ELECTRICITY.0024", "用户已被禁用");
        }

        // 查询套餐会员期限
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("CarRentalPackageOrderBizService.enableFreezeRentOrder, memberTermEntity not found. uid is {}, tenantId is {}", uid, tenantId);
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        if (!MemberTermStatusEnum.FREEZE.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300002", "租车会员状态异常");
        }

        if (!memberTermEntity.getRentalPackageOrderNo().equals(packageOrderNo)) {
            throw new BizException("300020", "订单编码不匹配");
        }

        // 是否存在滞纳金（仅限租车套餐产生的滞纳金）
        if (carRentalPackageOrderSlippageService.isExitUnpaid(tenantId, uid)) {
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }

        CarRentalPackageOrderFreezePo freezeEntity = carRentalPackageOrderFreezeService.selectFreezeByUidAndPackageOrderNo(uid, packageOrderNo);
        if (ObjectUtils.isEmpty(freezeEntity)) {
            log.error("CarRentalPackageOrderBizService.enableFreezeRentOrder error. not found order. uid is {}, packageOrderNo is {}",
                    uid, packageOrderNo);
            throw new BizException("300020", "订单编码不匹配");
        }
        long nowTime = System.currentTimeMillis();
        // 赋值会员更新
        CarRentalPackageMemberTermPo memberTermUpdateEntity = new CarRentalPackageMemberTermPo();
        memberTermUpdateEntity.setStatus(MemberTermStatusEnum.NORMAL.getCode());
        memberTermUpdateEntity.setId(memberTermEntity.getId());
        memberTermUpdateEntity.setUpdateUid(uid);
        memberTermUpdateEntity.setUpdateTime(nowTime);
        // 提前启用、计算差额
        long diffTime = (freezeEntity.getApplyTerm() * TimeConstant.DAY_MILLISECOND) - (nowTime - freezeEntity.getApplyTime());
        memberTermUpdateEntity.setDueTime(memberTermEntity.getDueTime() - diffTime);
        memberTermUpdateEntity.setDueTimeTotal(memberTermEntity.getDueTimeTotal()- diffTime);

        // 事务处理
        enableFreezeRentOrderTx(uid, packageOrderNo, false, optUid, memberTermUpdateEntity, null);

        return true;
    }

    /**
     * 启用冻结订单，TX事务处理<br />
     * 非对外
     * @param uid 用户ID
     * @param packageOrderNo 购买订单编码
     * @param autoEnable 自动启用标识，true(自动)，false(手动提前启用)
     * @param optUid 操作人ID(可为空)
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableFreezeRentOrderTx(Long uid, String packageOrderNo, Boolean autoEnable, Long optUid, CarRentalPackageMemberTermPo memberTermEntity, CarRentalPackageOrderSlippagePo orderSlippageUpdate) {
        // 1. 更改订单冻结表数据
        carRentalPackageOrderFreezeService.enableFreezeRentOrderByUidAndPackageOrderNo(packageOrderNo, uid, autoEnable, optUid);

        // 2. 更改会员期限表数据
        carRentalPackageMemberTermService.updateById(memberTermEntity);

        // 3. 处理滞纳金
        if (ObjectUtils.isNotEmpty(orderSlippageUpdate)) {
            carRentalPackageOrderSlippageService.updateById(orderSlippageUpdate);
        }
    }

    /**
     * 撤销用户冻结订单申请
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param packageOrderNo  购买订单编码
     * @return
     */
    @Override
    public Boolean revokeFreezeRentOrder(Integer tenantId, Long uid, String packageOrderNo) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询套餐会员期限
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("CarRentalPackageOrderBizServiceImpl.cancelFreezeRentOrder, memberTermEntity not found. uid is {}, tenantId is {}", uid, tenantId);
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        if (!MemberTermStatusEnum.APPLY_FREEZE.getCode().equals(memberTermEntity.getStatus())) {
            throw new BizException("300002", "租车会员状态异常");
        }

        if (!memberTermEntity.getRentalPackageOrderNo().equals(packageOrderNo)) {
            throw new BizException("300020", "订单编码不匹配");
        }

        // 二次保险保底查询
        CarRentalPackageOrderFreezePo freezeEntity = carRentalPackageOrderFreezeService.selectPendingApprovalByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(freezeEntity) || !freezeEntity.getRentalPackageOrderNo().equals(packageOrderNo)) {
            throw new BizException("300020", "订单编码不匹配");
        }

        // 事务处理
        revokeFreezeRentOrderTx(tenantId, uid, freezeEntity.getOrderNo());

        return true;
    }

    /**
     * 撤销冻结申请事务处理
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param freeOrderNo 冻结申请单编码
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeFreezeRentOrderTx(Integer tenantId, Long uid, String freeOrderNo) {
        // 1. 撤销冻结申请
        carRentalPackageOrderFreezeService.revokeByOrderNo(freeOrderNo, uid);
        // 2. 更改会员期限表数据
        carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.NORMAL.getCode(), uid);
    }

    /**
     * 根据用户ID及订单编码进行冻结订单申请
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param packageOrderNo  套餐购买订单编号
     * @param applyTerm 申请期限(天)
     * @param applyReason 申请理由
     * @param systemDefinitionEnum 操作系统
     * @param optUid 操作人ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean freezeRentOrder(Integer tenantId, Long uid, String packageOrderNo, Integer applyTerm, String applyReason, SystemDefinitionEnum systemDefinitionEnum, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo, applyTerm, systemDefinitionEnum)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        if (SystemDefinitionEnum.WX_APPLET.getCode().equals(systemDefinitionEnum.getCode()) && StringUtils.isBlank(applyReason)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        if (Objects.isNull(userInfo)) {
            log.error("CarRentalPackageOrderBizService.freezeRentOrder failed. not found user. uid is {}", uid);
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CarRentalPackageOrderBizService.freezeRentOrder failed. user is disable. uid is {}", uid);
            throw new BizException( "ELECTRICITY.0024", "用户已被禁用");
        }

        // 查询套餐会员期限
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRentalPackageOrderBizService.freezeRentOrder failed. t_car_rental_package_member_term not found or status is error. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        long now = System.currentTimeMillis();
        if (memberTermEntity.getDueTime() < now) {
            log.error("CarRentalPackageOrderBizService.freezeRentOrder failed. t_car_rental_package_member_term due time is {}. now is {}", memberTermEntity.getDueTime(), now);
            throw new BizException("300033", "套餐已过期，无法申请冻结");
        }

        // 查询套餐购买订单信息
        CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(packageOrderNo);

        // 判定套餐是否允许冻结
        if (ObjectUtils.isEmpty(packageOrderEntity)) {
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        if(!PayStateEnum.SUCCESS.getCode().equals(packageOrderEntity.getPayState())) {
            throw new BizException("300014", "订单支付异常");
        }

        if(!UseStateEnum.IN_USE.getCode().equals(packageOrderEntity.getUseState())) {
            throw new BizException("300015", "订单状态异常");
        }

        Long useBeginTime = packageOrderEntity.getUseBeginTime();
        // 查询是否存在冻结订单
        CarRentalPackageOrderFreezePo freezePo = carRentalPackageOrderFreezeService.selectLastFreeByUid(uid);
        if (ObjectUtils.isNotEmpty(freezePo) && ObjectUtils.isNotEmpty(freezePo.getEnableTime()) && freezePo.getRentalPackageOrderNo().equals(packageOrderEntity.getOrderNo())) {
            useBeginTime = freezePo.getEnableTime();
        }

        // 计算余量
        Long residue = calculateResidue(packageOrderEntity.getConfine(), memberTermEntity.getResidue(), useBeginTime, packageOrderEntity.getTenancy(), packageOrderEntity.getTenancyUnit());
        // 生成冻结申请
        CarRentalPackageOrderFreezePo freezeEntity = buildCarRentalPackageOrderFreeze(uid, packageOrderEntity, applyTerm, residue, applyReason, optUid);

        // TX 事务
        saveFreezeInfoTx(freezeEntity, tenantId, uid, optUid, systemDefinitionEnum);

        return true;
    }

    /**
     * 冻结套餐，最终TX事务保存落库<br />
     * 非对外
     * @param freezeEntity 冻结订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param optUid 操作用户ID
     * @param systemDefinitionEnum 操作系统
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveFreezeInfoTx(CarRentalPackageOrderFreezePo freezeEntity, Integer tenantId, Long uid, Long optUid, SystemDefinitionEnum systemDefinitionEnum) {
        // 保存冻结记录
        carRentalPackageOrderFreezeService.insert(freezeEntity);
        // 更新会员状态
        carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.APPLY_FREEZE.getCode(), uid);

        if (SystemDefinitionEnum.BACKGROUND.getCode().equals(systemDefinitionEnum.getCode())) {
            approveFreezeRentOrder(freezeEntity.getOrderNo(), true, null, optUid);
        }

    }

    /**
     * 生成冻结订单
     * @param uid 用户ID
     * @param packageOrderEntity 套餐购买订单
     * @param applyTerm 申请期限(天)
     * @param residue 余量
     * @param optUid 操作用户ID
     * @return
     */
    private CarRentalPackageOrderFreezePo buildCarRentalPackageOrderFreeze(Long uid, CarRentalPackageOrderPo packageOrderEntity, Integer applyTerm, Long residue, String applyReason, Long optUid) {
        CarRentalPackageOrderFreezePo freezeEntity = new CarRentalPackageOrderFreezePo();
        freezeEntity.setUid(uid);
        freezeEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_SUSPEND, uid));
        freezeEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        freezeEntity.setRentalPackageId(packageOrderEntity.getRentalPackageId());
        freezeEntity.setRentalPackageType(packageOrderEntity.getRentalPackageType());
        freezeEntity.setResidue(residue);
        freezeEntity.setLateFee(packageOrderEntity.getLateFee());
        freezeEntity.setApplyTerm(applyTerm);
        freezeEntity.setApplyReason(applyReason);
        freezeEntity.setApplyTime(System.currentTimeMillis());
        freezeEntity.setTenantId(packageOrderEntity.getTenantId());
        freezeEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        freezeEntity.setStoreId(packageOrderEntity.getStoreId());
        freezeEntity.setCreateUid(optUid);

        // 设置余量单位
        if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntity.getConfine())) {
            freezeEntity.setResidueUnit(RentalUnitEnum.NUMBER.getCode());
        }
        if (RenalPackageConfineEnum.NO.getCode().equals(packageOrderEntity.getConfine())) {
            freezeEntity.setResidueUnit(packageOrderEntity.getTenancyUnit());
        }

        // 设置状态
        freezeEntity.setStatus(RentalPackageOrderFreezeStatusEnum.PENDING_APPROVAL.getCode());
        return freezeEntity;
    }

    /**
     * 生成逾期订单
     * @return
     */
    private CarRentalPackageOrderSlippagePo buildCarRentalPackageOrderSlippage(Long uid, CarRentalPackageOrderPo packageOrderEntity, Long nowTime) {
        // 初始化标识
        boolean createFlag = false;
        if (ObjectUtils.isEmpty(packageOrderEntity.getLateFee()) || BigDecimal.ZERO.compareTo(packageOrderEntity.getLateFee()) >= 0) {
            // 不收取滞纳金
            return null;
        }

        // 查询是否未归还设备
        // 1. 车辆
        ElectricityCar electricityCar = carService.selectByUid(packageOrderEntity.getTenantId(), uid);
        if (ObjectUtils.isNotEmpty(electricityCar)) {
            createFlag = true;
        }

        // 2. 根据套餐类型，是否查询电池
        ElectricityBattery battery = null;
        String batteryModelType = null;
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageOrderEntity.getRentalPackageType())) {
            battery = batteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                batteryModelType = battery.getModel();
                createFlag = true;
            }
        }

        // 不会生成滞纳金记录
        if (!createFlag) {
            return null;
        }

        // 生成实体记录
        CarRentalPackageOrderSlippagePo slippageEntity = new CarRentalPackageOrderSlippagePo();
        slippageEntity.setUid(uid);
        slippageEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        slippageEntity.setRentalPackageId(packageOrderEntity.getRentalPackageId());
        slippageEntity.setRentalPackageType(packageOrderEntity.getRentalPackageType());
        slippageEntity.setType(SlippageTypeEnum.FREEZE.getCode());
        slippageEntity.setLateFee(packageOrderEntity.getLateFee());
        slippageEntity.setLateFeeStartTime(nowTime);
        slippageEntity.setPayState(PayStateEnum.UNPAID.getCode());
        slippageEntity.setTenantId(packageOrderEntity.getTenantId());
        slippageEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        slippageEntity.setStoreId(packageOrderEntity.getStoreId());
        slippageEntity.setCreateUid(uid);

        // 记录设备信息
        if (ObjectUtils.isNotEmpty(electricityCar)) {
            slippageEntity.setCarSn(electricityCar.getSn());
            slippageEntity.setCarModelId(electricityCar.getModelId());
        }
        if (ObjectUtils.isNotEmpty(battery)) {
            slippageEntity.setBatterySn(battery.getSn());
            slippageEntity.setBatteryModelType(batteryModelType);
        }

        return slippageEntity;
    }


    /**
     * 根据用户ID及订单编码，退租购买的订单
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param packageOrderNo  套餐购买订单编号
     * @param optUid  操作人ID
     * @return
     */
    @Override
    public Boolean refundRentOrder(Integer tenantId, Long uid, String packageOrderNo, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, uid, packageOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 判定用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        if (Objects.isNull(userInfo)) {
            log.error("CarRentalPackageOrderBizService.refundRentOrder failed. not found user. uid is {}", uid);
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CarRentalPackageOrderBizService.refundRentOrder failed. user is disable. uid is {}", uid);
            throw new BizException( "ELECTRICITY.0024", "用户已被禁用");
        }

        // 查询会员期限信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRentalPackageOrderBizService.refundRentOrder failed. t_car_rental_package_member_term not found or status is error. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        long now = System.currentTimeMillis();
        if (memberTermEntity.getDueTimeTotal() < now) {
            log.error("CarRentalPackageOrderBizService.refundRentOrder failed. t_car_rental_package_member_term due time total is {}. now is {}", memberTermEntity.getDueTimeTotal(), now);
            throw new BizException("300032", "套餐已过期，无法申请退租");
        }

        // 查询套餐购买订单
        CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(packageOrderNo);
        if (ObjectUtils.isEmpty(packageOrderEntity) || ObjectUtils.notEqual(tenantId, packageOrderEntity.getTenantId()) || ObjectUtils.notEqual(uid, packageOrderEntity.getUid())) {
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        if (ObjectUtils.notEqual(YesNoEnum.YES.getCode(), packageOrderEntity.getRentRebate())) {
            throw new BizException("300012", "订单不允许退租");
        } else {
            if (now >= packageOrderEntity.getRentRebateEndTime()) {
                throw new BizException("300013", "订单超过可退期限");
            }
        }

        if (ObjectUtils.notEqual(PayStateEnum.SUCCESS.getCode(), packageOrderEntity.getPayState())) {
            throw new BizException("300014", "订单支付异常");
        }

        if (UseStateEnum.notRefundCodes().contains(packageOrderEntity.getUseState())) {
            throw new BizException("300015", "订单状态异常");
        }

        // 购买的时候，赠送的优惠券是否被使用，若为使用中、已使用，则不允许退租
        UserCoupon userCoupon = userCouponService.selectBySourceOrderId(packageOrderEntity.getOrderNo());
        if (ObjectUtils.isNotEmpty(userCoupon)) {
            Integer status = userCoupon.getStatus();
            if (UserCoupon.STATUS_IS_BEING_VERIFICATION.equals(status) || UserCoupon.STATUS_USED.equals(status)) {
                throw new BizException("300016", "您已使用优惠券，该套餐不可退");
            }
        }

        CarRentalPackageMemberTermPo memberTermUpdateEntity = null;
        if (UseStateEnum.IN_USE.getCode().equals(packageOrderEntity.getUseState())) {
            if (carRentalPackageOrderService.isExitUnUseAndRefund(tenantId, uid, now)) {
                throw new BizException("300017", "存在未使用的订单");
            }
            // 查询设备信息，存在设备，不允许退租
            ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
            if (ObjectUtils.isNotEmpty(electricityCar) && ObjectUtils.isNotEmpty(electricityCar.getSn()) ) {
                throw new BizException("300040", "需先退还资产再退租金");
            }
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageOrderEntity.getRentalPackageType())) {
                ElectricityBattery battery = batteryService.queryByUid(uid);
                if (ObjectUtils.isNotEmpty(battery)) {
                    throw new BizException("300040", "需先退还资产再退租金");
                }
            }
            memberTermUpdateEntity = buildRentRefundRentalPackageMemberTerm(tenantId, uid, optUid);
        }

        // 计算实际应退金额及余量
        Triple<BigDecimal, Long, Long> refundAmountPair = calculateRefundAmount(packageOrderEntity, tenantId, uid);

        // 生成租金退款审核订单
        CarRentalPackageOrderRentRefundPo rentRefundOrderEntity = buildRentRefundOrder(packageOrderEntity, refundAmountPair.getLeft(), uid, refundAmountPair.getMiddle(), refundAmountPair.getRight(), optUid);

        // TX 事务管理
        saveRentRefundOrderInfoTx(rentRefundOrderEntity, memberTermUpdateEntity);

        return true;
    }

    /**
     * 退租申请单的事务处理
     * @param rentRefundOrderEntity 退租申请单
     * @param memberTermUpdateEntity 会员期限数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRentRefundOrderInfoTx(CarRentalPackageOrderRentRefundPo rentRefundOrderEntity, CarRentalPackageMemberTermPo memberTermUpdateEntity) {
        carRentalPackageOrderRentRefundService.insert(rentRefundOrderEntity);
        if (ObjectUtils.isNotEmpty(memberTermUpdateEntity)) {
            carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);
        }
    }

    /**
     * 退租申请，构建会员期限更新数据
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @param optUid 操作用户UID
     * @return
     */
    private CarRentalPackageMemberTermPo buildRentRefundRentalPackageMemberTerm(Integer tenantId, Long uid, Long optUid) {
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("buildRentRefundRentalPackageMemberTerm faild. not find car_rental_package_member_term or status error. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        CarRentalPackageMemberTermPo memberTermUpdateEntity = new CarRentalPackageMemberTermPo();
        memberTermUpdateEntity.setId(memberTermEntity.getId());
        memberTermUpdateEntity.setStatus(MemberTermStatusEnum.APPLY_RENT_REFUND.getCode());
        memberTermUpdateEntity.setUpdateUid(ObjectUtils.isEmpty(optUid) ? uid : optUid);

        return memberTermUpdateEntity;
    }

    /**
     * 计算实际应退金额
     * @param packageOrderEntity 购买的套餐订单
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return Pair L：金额, M：租期余量，R：限制余量
     */
    private Triple<BigDecimal, Long, Long> calculateRefundAmount(CarRentalPackageOrderPo packageOrderEntity, Integer tenantId, Long uid) {
        // 定义实际应返金额
        BigDecimal refundAmount = null;
        // 定义使用的租期
        Long tenancyUse = null;
        // 定义租期余量
        Long tenancyResidue = null;
        // 定义限制使用量
        Long confineUse= null;
        // 定义限制余量
        Long confineResidue = null;
        // 实际支付金额
        BigDecimal rentPayment = packageOrderEntity.getRentPayment();

        // 判定订单状态
        // 使用中，计算金额
        if (UseStateEnum.IN_USE.getCode().equals(packageOrderEntity.getUseState())) {
            // 查询套餐会员期限
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 退款规则
            // 0. 计算剩余租期
            long nowTime = System.currentTimeMillis();
            long useBeginTime = packageOrderEntity.getUseBeginTime();

            // 查询是否存在冻结订单
            CarRentalPackageOrderFreezePo freezePo = carRentalPackageOrderFreezeService.selectLastFreeByUid(uid);

            if (RentalUnitEnum.DAY.getCode().equals(packageOrderEntity.getTenancyUnit())) {
                if (ObjectUtils.isNotEmpty(freezePo) && ObjectUtils.isNotEmpty(freezePo.getEnableTime()) && freezePo.getRentalPackageOrderNo().equals(packageOrderEntity.getOrderNo())) {
                    useBeginTime = freezePo.getEnableTime();
                }
                // 已使用天数
                tenancyUse = DateUtils.diffDay(useBeginTime, nowTime);

                // 剩余天数
                tenancyResidue = packageOrderEntity.getTenancy() - tenancyUse;
            }

            if (RentalUnitEnum.MINUTE.getCode().equals(packageOrderEntity.getTenancyUnit())) {
                if (ObjectUtils.isNotEmpty(freezePo) && ObjectUtils.isNotEmpty(freezePo.getEnableTime()) && freezePo.getRentalPackageOrderNo().equals(packageOrderEntity.getOrderNo())) {
                    useBeginTime = freezePo.getEnableTime();
                }
                // 已使用分钟数
                tenancyUse = DateUtils.diffMinute(useBeginTime, nowTime);
                // 剩余分钟数
                tenancyResidue = packageOrderEntity.getTenancy() - tenancyUse;
            }

            // 1. 若限制次数，则根据次数计算退款金额
            if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntity.getConfine())) {
                // 查询当前套餐的余量
                confineResidue = memberTermEntity.getResidue();
                // 余量为 0，则退款金额为 0
                if (confineResidue == 0) {
                    refundAmount = BigDecimal.ZERO;
                } else{
                    // 已使用数量
                    confineUse = packageOrderEntity.getConfineNum() - confineResidue;
                    refundAmount = diffAmount(confineUse, packageOrderEntity.getRentUnitPrice(), rentPayment);
                }
            }

            // 2. 若不限制，则根据租期使用量计算退款金额
            if (RenalPackageConfineEnum.NO.getCode().equals(packageOrderEntity.getConfine())) {
                refundAmount = diffAmount(tenancyUse, packageOrderEntity.getRentUnitPrice(), rentPayment);
            }
        }

        // 未使用，实际支付的租金金额
        if (UseStateEnum.UN_USED.getCode().equals(packageOrderEntity.getUseState())) {
            refundAmount = rentPayment;
            tenancyResidue = Long.valueOf(packageOrderEntity.getTenancy());
            if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntity.getConfine())) {
                confineResidue = packageOrderEntity.getConfineNum();
            }
        }

        return Triple.of(refundAmount, tenancyResidue, confineResidue);
    }

    /**
     * 计算套餐订单剩余量
     * @param confine 套餐订单是否限制
     * @param memberResidue 会员余量
     * @param useBeginTime 套餐订单开始时间时间
     * @param tenancy 租期
     * @param tenancyUnit 租期单位
     * @return
     */
    private Long calculateResidue(Integer confine, Long memberResidue, long useBeginTime, Integer tenancy, Integer tenancyUnit) {
        log.info("calculateResidue, confine is {}, memberResidue is {}, useBeginTime is {}, tenancy is {}, tenancyUnit is {}");
        // 1. 若限制次数，取余量
        if (RenalPackageConfineEnum.NUMBER.getCode().equals(confine)) {
            return memberResidue;
        }

        // 2. 若不限制，则根据时间单位（天、分钟）计算退款金额
        if (RenalPackageConfineEnum.NO.getCode().equals(confine)) {
            long nowTime = System.currentTimeMillis();
            log.info("calculateResidue, nowTime is {}", nowTime);
            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                // 已使用天数
                long diffDay = DateUtils.diffDay(useBeginTime, nowTime);
                return tenancy - diffDay;
            }

            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                // 已使用分钟数
                long diffMinute = DateUtils.diffMinute(useBeginTime, nowTime);
                return tenancy - diffMinute;
            }
        }

        return null;
    }

    /**
     * 计算差额
     * @param diffTime 差额时间
     * @param rentUnitPrice 单价
     * @param rentPayment 实际支付金额
     * @return
     */
    private BigDecimal diffAmount(long diffTime, BigDecimal rentUnitPrice, BigDecimal rentPayment) {
        BigDecimal diffAmount = BigDecimal.ZERO;
        // 应付款 计算规则：套餐单价 * 差额时间
        BigDecimal shouldPayAmount = NumberUtil.mul(diffTime, rentUnitPrice).setScale(2, RoundingMode.HALF_UP);
        // 计算实际应返金额
        if (rentPayment.compareTo(shouldPayAmount) > 0) {
            diffAmount = NumberUtil.sub(rentPayment, shouldPayAmount);
        }
        return diffAmount;
    }

    /**
     * 生成租金退款订单信息
     * @param packageOrderEntity 套餐购买订单
     * @param refundAmount 应退金额
     * @param uid 用户ID
     * @param tenancyResidue 租期余量
     * @param confineResidue 限制余量
     * @param optUid 操作人ID
     * @return com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo
     * @author xiaohui.song
     **/
    private CarRentalPackageOrderRentRefundPo buildRentRefundOrder(CarRentalPackageOrderPo packageOrderEntity, BigDecimal refundAmount, Long uid, Long tenancyResidue, Long confineResidue, Long optUid) {
        CarRentalPackageOrderRentRefundPo rentRefundOrderEntity = new CarRentalPackageOrderRentRefundPo();
        rentRefundOrderEntity.setUid(uid);
        rentRefundOrderEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        rentRefundOrderEntity.setRentalPackageId(packageOrderEntity.getRentalPackageId());
        rentRefundOrderEntity.setRentalPackageType(packageOrderEntity.getRentalPackageType());
        rentRefundOrderEntity.setRefundAmount(refundAmount);
        rentRefundOrderEntity.setRefundState(RefundStateEnum.PENDING_APPROVAL.getCode());
        rentRefundOrderEntity.setRentUnitPrice(packageOrderEntity.getRentUnitPrice());
        rentRefundOrderEntity.setRentPayment(packageOrderEntity.getRentPayment());
        rentRefundOrderEntity.setTenantId(packageOrderEntity.getTenantId());
        rentRefundOrderEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        rentRefundOrderEntity.setStoreId(packageOrderEntity.getStoreId());
        rentRefundOrderEntity.setCreateUid(ObjectUtils.isEmpty(optUid) ? uid : optUid);
        rentRefundOrderEntity.setDelFlag(DelFlagEnum.OK.getCode());
        // 设置租期余量及租期余量单位
        rentRefundOrderEntity.setTenancyResidue(tenancyResidue);
        rentRefundOrderEntity.setTenancyResidueUnit(packageOrderEntity.getTenancyUnit());
        // 设置限制及限制余量
        rentRefundOrderEntity.setConfine(packageOrderEntity.getConfine());
        rentRefundOrderEntity.setConfineResidue(confineResidue);
        // 设置交易方式
        rentRefundOrderEntity.setPayType(packageOrderEntity.getPayType());

        return rentRefundOrderEntity;
    }

    /**
     * 根据用户ID查询正在使用的套餐信息<br />
     * 复合查询，车辆信息、门店信息、GPS信息、电池信息、保险信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return com.xiliulou.core.web.R<com.xiliulou.electricity.vo.rental.RentalPackageVO>
     * @author xiaohui.song
     **/
    @Override
    public R<RentalPackageVO> queryUseRentalPackageOrderByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 1. 查询会员期限信息
        CarRentalPackageMemberTermPo memberTerm = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTerm) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTerm.getStatus())) {
            log.info("CarRentalPackageOrderBizService.queryUseRentalPackageOrderByUid, not found t_car_rental_package_member_term or status is pending effective.");
            return R.ok();
        }

        // 2. 查询套餐信息
        CarRentalPackagePo carRentalPackage = null;
        ElectricityCarModel carModel = null;
        Long rentalPackageId = memberTerm.getRentalPackageId();
        if (ObjectUtils.isNotEmpty(rentalPackageId)) {
            carRentalPackage = carRentalPackageService.selectById(rentalPackageId);
            if (ObjectUtils.isEmpty(carRentalPackage)) {
                log.info("CarRentalPackageOrderBizService.queryUseRentalPackageOrderByUid, not foun t_car_rental_package. rentalPackageId is {}", rentalPackageId);
                throw new BizException("300000", "数据有误");
            }
            // 查询车辆型号信息
            Integer carModelId = carRentalPackage.getCarModelId();
            carModel = carModelService.queryByIdFromCache(carModelId);
        }

        // 3. 查询套餐购买订单信息
        String rentalPackageOrderNo = memberTerm.getRentalPackageOrderNo();
        CarRentalPackageOrderPo carRentalPackageOrder = carRentalPackageOrderService.selectByOrderNo(rentalPackageOrderNo);

        // 4. 查询用户车辆信息
        ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
        CarInfoDO carInfo = null;
        CarAttr carAttr = null;
        Integer carRentalState = null;
        Integer lockType = null;
        if (ObjectUtils.isNotEmpty(electricityCar)) {
            // 5. 查询车辆相关信息
            carInfo = carService.queryByCarId(tenantId, Long.valueOf(electricityCar.getId()));
            // 6. 查询车辆地址更新时间
            carAttr = carService.queryLastReportPointBySn(electricityCar.getSn());
            // 7. 查询还车订单信息
            CarRentalOrderPo carRentalOrderPo = carRentalOrderService.selectLastByUidAndSnAndType(tenantId, uid, RentalTypeEnum.RETURN.getCode(), electricityCar.getSn());
            if (ObjectUtils.isNotEmpty(carRentalOrderPo)) {
                carRentalState = carRentalOrderPo.getRentalState();
            }
            //车辆锁状态
            if (StringUtils.isNotBlank(electricityCar.getSn())) {
                R<Jt808DeviceInfoVo> result = jt808RetrofitService
                        .getInfo(new Jt808GetInfoRequest(IdUtil.randomUUID(), electricityCar.getSn()));
                if (result.isSuccess()) {
                    lockType = result.getData().getDoorStatus();
                }
            }
        }

        // 6. 查询用户保险信息
        Integer rentalPackageType = memberTerm.getRentalPackageType();
        InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.selectUserInsuranceInfo(uid, rentalPackageType);

        // 7. 电池消息
        ElectricityUserBatteryVo userBatteryVo = null;
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            Triple<Boolean, String, Object> batteryTriple = batteryService.queryInfoByUid(uid, BatteryInfoQuery.NEED);
            if (!batteryTriple.getLeft()) {
                log.error("CarRentalPackageOrderBizService.queryUseRentalPackageOrderByUid, batteryService.queryInfoByUid failed. uid is {}", uid);
                throw new BizException(batteryTriple.getMiddle(), (String) batteryTriple.getRight());
            }
            userBatteryVo = (ElectricityUserBatteryVo) batteryTriple.getRight();
        }

        // 8. 滞纳金信息
        BigDecimal lateFeeAmount = carRenalPackageSlippageBizService.queryCarPackageUnpaidAmountByUid(tenantId, uid);

        // 构建返回信息
        RentalPackageVO rentalPackageVO = buildRentalPackageVO(memberTerm, carRentalPackage, carRentalPackageOrder, insuranceUserInfoVo, carInfo, userBatteryVo, lateFeeAmount, carModel,
                carAttr, carRentalState, lockType);

        return R.ok(rentalPackageVO);
    }

    private RentalPackageVO buildRentalPackageVO(CarRentalPackageMemberTermPo memberTerm, CarRentalPackagePo carRentalPackage, CarRentalPackageOrderPo carRentalPackageOrder,
                                                 InsuranceUserInfoVo insuranceUserInfoVo, CarInfoDO carInfo, ElectricityUserBatteryVo userBatteryVo, BigDecimal lateFeeAmount,
                                                 ElectricityCarModel carModel, CarAttr carAttr, Integer carRentalState, Integer lockType) {
        RentalPackageVO rentalPackageVO = new RentalPackageVO();
        rentalPackageVO.setDeadlineTime(memberTerm.getDueTimeTotal());
        rentalPackageVO.setLateFeeAmount(lateFeeAmount);
        rentalPackageVO.setStatus(memberTerm.getStatus());
        if (ObjectUtils.isNotEmpty(memberTerm.getDueTime())) {
            // 判定是否过期
            if (memberTerm.getDueTime() <= System.currentTimeMillis() ||
                    (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTerm.getRentalPackageConfine()) && memberTerm.getResidue() <= 0L)) {
                rentalPackageVO.setStatus(MemberTermStatusEnum.EXPIRE.getCode());
            }
        }

        // 套餐订单信息
        CarRentalPackageOrderVo carRentalPackageOrderVO = new CarRentalPackageOrderVo();
        carRentalPackageOrderVO.setRentalPackageId(memberTerm.getRentalPackageId());
        if (ObjectUtils.isNotEmpty(carRentalPackageOrder)) {
            carRentalPackageOrderVO.setOrderNo(carRentalPackageOrder.getOrderNo());
            carRentalPackageOrderVO.setRentalPackageType(carRentalPackageOrder.getRentalPackageType());
            carRentalPackageOrderVO.setConfine(carRentalPackageOrder.getConfine());
            carRentalPackageOrderVO.setConfineNum(carRentalPackageOrder.getConfineNum());
            carRentalPackageOrderVO.setTenancy(carRentalPackageOrder.getTenancy());
            carRentalPackageOrderVO.setTenancyUnit(carRentalPackageOrder.getTenancyUnit());
            carRentalPackageOrderVO.setRent(carRentalPackageOrder.getRent());
        }

        carRentalPackageOrderVO.setCarRentalPackageName(ObjectUtils.isNotEmpty(carRentalPackage) ? carRentalPackage.getName() : null);
        carRentalPackageOrderVO.setDeposit(memberTerm.getDeposit());
        carRentalPackageOrderVO.setBatteryVoltage(ObjectUtils.isNotEmpty(carRentalPackage) ? carRentalPackage.getBatteryVoltage() : null);
        carRentalPackageOrderVO.setCarModelName(ObjectUtils.isNotEmpty(carModel) ? carModel.getName() : null);
        // 赋值套餐订单信息
        rentalPackageVO.setCarRentalPackageOrder(carRentalPackageOrderVO);

        // 用户保险信息
        if (ObjectUtils.isNotEmpty(insuranceUserInfoVo)) {
            UserInsuranceVO userInsuranceVo = new UserInsuranceVO();
            userInsuranceVo.setInsuranceName(insuranceUserInfoVo.getInsuranceName());
            userInsuranceVo.setPremium(insuranceUserInfoVo.getPremium());
            userInsuranceVo.setIsUse(insuranceUserInfoVo.getIsUse());
            userInsuranceVo.setInsuranceExpireTime(insuranceUserInfoVo.getInsuranceExpireTime());
            userInsuranceVo.setForehead(insuranceUserInfoVo.getForehead());
            userInsuranceVo.setType(insuranceUserInfoVo.getType());
            rentalPackageVO.setUserInsurance(userInsuranceVo);
        }

        // 车辆信息
        if (ObjectUtils.isNotEmpty(carInfo)) {
            CarVo carVO = new CarVo();
            carVO.setCarSn(carInfo.getCarSn());
            carVO.setStoreName(carInfo.getStoreName());
            carVO.setLatitude(carInfo.getLatitude());
            carVO.setLongitude(carInfo.getLongitude());
            if (ObjectUtils.isNotEmpty(carAttr)) {
                carVO.setPointUpdateTime(carAttr.getCreateTime().getTime());
            }
            carVO.setCarRentalState(carRentalState);
            carVO.setLockType(lockType);
            // 赋值车辆信息
            rentalPackageVO.setCar(carVO);
        }

        // 电池信息
        rentalPackageVO.setUserBattery(userBatteryVo);

        return rentalPackageVO;
    }

    /**
     * 租车套餐订单
     *
     * @param packageOrderNo  租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelRentalPackageOrder(String packageOrderNo, Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPo carRentalPackageOrderEntity = null;
        if (StringUtils.isNotBlank(packageOrderNo)) {
            carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(packageOrderNo);
        } else {
            carRentalPackageOrderEntity = carRentalPackageOrderService.selectLastUnPayByUid(tenantId, uid);
        }

        if (ObjectUtil.isEmpty(carRentalPackageOrderEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_order");
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        packageOrderNo = carRentalPackageOrderEntity.getOrderNo();


        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), carRentalPackageOrderEntity.getPayState())) {
            log.error("CancelRentalPackageOrder failed, car_rental_package_order processed, order_no is {}", packageOrderNo);
            throw new BizException("300009", "租车套餐购买订单已处理");
        }


        // 更改套餐购买订单的支付状态
        carRentalPackageOrderService.updatePayStateByOrderNo(packageOrderNo, PayStateEnum.CANCEL.getCode());

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = carRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("CancelRentalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            throw new BizException("300010", "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.CANCEL.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        // 待生效的数据，直接删除
        if (ObjectUtils.isNotEmpty(memberTermEntity) && MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            carRentalPackageMemberTermService.delByUidAndTenantId(tenantId, uid, uid);
        }

        // 4. 处理用户优惠券的使用状态
        userCouponService.updateStatusByOrderId(packageOrderNo, UserCoupon.STATUS_UNUSED);

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
        Long buyRentalPackageId = buyOptModel.getRentalPackageId();

        if (!ObjectUtils.allNotNull(tenantId, uid, buyRentalPackageId)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 获取加锁 KEY
        String buyLockKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_BUY_UID_KEY, uid);

        try {
            // 加锁
            if (!redisService.setNx(buyLockKey, uid.toString(), 10 * 1000L, false)) {
                return R.fail("ELECTRICITY.0034", "操作频繁");
            }

            // 1 获取用户信息
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.error("buyRentalPackageOrder failed. Not found user. uid is {} ", uid);
                throw new BizException("ELECTRICITY.0001", "未找到用户");
            }

            // 1.1 用户可用状态
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("buyRentalPackageOrder failed. User is unUsable. uid is {} ", uid);
                throw new BizException("ELECTRICITY.0024", "用户已被禁用");
            }

            // 1.2 用户实名认证状态
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("buyRentalPackageOrder failed. User not auth. uid is {}", uid);
                throw new BizException("ELECTRICITY.0041", "用户尚未实名认证");
            }

            // 2. 判定滞纳金
            if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
                log.error("buyRentalPackageOrder failed. Late fee not paid. uid is {}", uid);
                throw new BizException("300001", "存在滞纳金，请先缴纳");
            }

            // 3. 支付相关
            ElectricityPayParams payParamsEntity = electricityPayParamsService.queryFromCache(tenantId);
            if (Objects.isNull(payParamsEntity)) {
                log.error("buyRentalPackageOrder failed. Not found pay_params. uid is {}", uid);
                throw new BizException("100234", "未配置支付参数");
            }

            // 4. 三方授权相关
            UserOauthBind userOauthBindEntity = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
            if (Objects.isNull(userOauthBindEntity) || Objects.isNull(userOauthBindEntity.getThirdId())) {
                log.error("buyRentalPackageOrder failed. Not found useroauthbind or thirdid is null. uid is {}", uid);
                throw new BizException("100235", "未找到用户的第三方授权信息");
            }

            // 初始化押金金额
            BigDecimal deposit = null;
            Integer userTenantId = userInfo.getTenantId();
            Long userFranchiseeId = Long.valueOf(buyOptModel.getFranchiseeId());
            Long userStoreId = Long.valueOf(buyOptModel.getStoreId());
            if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId()) && userInfo.getFranchiseeId() != 0L && !userFranchiseeId.equals(userInfo.getFranchiseeId())) {
                log.error("buyRentalPackageOrder failed. userInfo's franchiseeId is {}. params franchiseeId is {}", userInfo.getFranchiseeId(), buyOptModel.getFranchiseeId());
                throw new BizException("300036", "所属机构不匹配");
            }
            if (ObjectUtils.isNotEmpty(userInfo.getStoreId()) && userInfo.getStoreId() != 0L && !userStoreId.equals(userInfo.getStoreId())) {
                log.error("buyRentalPackageOrder failed. userInfo's storeId is {}. params storeId is {}", userInfo.getStoreId(), buyOptModel.getStoreId());
                throw new BizException("300036", "所属机构不匹配");
            }

            Integer rentalPackageConfine = null;
            // 5. 获取租车套餐会员期限信息
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            // 若非空，则押金必定缴纳
            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                // 非待生效
                if (!MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    // 5.1 用户套餐会员限制状态异常
                    if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
                        log.error("buyRentalPackageOrder failed. member_term status wrong. uid is {}, status is {}", uid, memberTermEntity.getStatus());
                        return R.fail("300057", "您有正在审核中/已冻结流程，不支持该操作");
                    }
                    // 从会员期限中获取押金金额
                    log.info("buyRentalPackageOrder deposit from memberTerm");
                    deposit = memberTermEntity.getDeposit();
                }
            }

            // 6. 获取套餐信息
            // 6.1 套餐不存在
            CarRentalPackagePo buyPackageEntity = carRentalPackageService.selectById(buyRentalPackageId);
            if (ObjectUtils.isEmpty(buyPackageEntity) || DelFlagEnum.DEL.getCode().equals(buyPackageEntity.getDelFlag())) {
                log.error("buyRentalPackageOrder failed. Package does not exist, buyRentalPackageId is {}", buyRentalPackageId);
                return R.fail("300003", "套餐不存在");
            }

            // 6.2 套餐上下架状态
            if (UpDownEnum.DOWN.getCode().equals(buyPackageEntity.getStatus())) {
                log.error("buyRentalPackageOrder failed. Package status is down. buyRentalPackageId is {}", buyRentalPackageId);
                return R.fail("300004", "套餐已下架");
            }

            // 6.3 判定用户是否是老用户，然后和套餐的适用类型做比对
            Boolean oldUserFlag = userBizService.isOldUser(tenantId, uid);
            if (oldUserFlag && !ApplicableTypeEnum.oldUserApplicable().contains(buyPackageEntity.getApplicableType())) {
                log.error("buyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user is old", buyPackageEntity.getApplicableType());
                return R.fail("300005", "套餐不匹配");
            }

            // 7. 判定套餐互斥
            // 7.1 车或者电与车电一体互斥
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyPackageEntity.getType()) &&
                    (!UserInfo.BATTERY_DEPOSIT_STATUS_NO.equals(userInfo.getBatteryDepositStatus()) || !UserInfo.CAR_DEPOSIT_STATUS_NO.equals(userInfo.getCarDepositStatus()))) {
                log.error("buyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is battery or car", buyPackageEntity.getType());
                return R.fail("300005", "套餐不匹配");
            }

            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                // 此处代表用户名下有租车套餐（单车或车电一体）
                if (!MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    // 7.2 用户名下的套餐类型和即将购买的套餐类型不一致
                    if (!memberTermEntity.getRentalPackageType().equals(buyPackageEntity.getType())) {
                        log.error("buyRentalPackageOrder failed. Package type mismatch. Buy package type is {}, user package type is {}", buyPackageEntity.getType(), memberTermEntity.getRentalPackageType());
                        return R.fail("300005", "套餐不匹配");
                    }
                    userTenantId = memberTermEntity.getTenantId();
                    userFranchiseeId = Long.valueOf(memberTermEntity.getFranchiseeId());
                    userStoreId = Long.valueOf(memberTermEntity.getStoreId());
                    rentalPackageConfine = memberTermEntity.getRentalPackageConfine();
                }
            }

            // 7.3 用户归属和套餐归属不一致(租户、加盟商、门店)，拦截
            if (ObjectUtils.notEqual(userStoreId, UserInfo.VIRTUALLY_STORE_ID)) {
                if (ObjectUtils.notEqual(userTenantId, buyPackageEntity.getTenantId()) || ObjectUtils.notEqual(userFranchiseeId, Long.valueOf(buyPackageEntity.getFranchiseeId()))
                        || ObjectUtils.notEqual(userStoreId, Long.valueOf(buyPackageEntity.getStoreId()))) {
                    log.error("buyRentalPackageOrder failed. Package belong mismatch. ");
                    return R.fail("300005", "套餐不匹配");
                }
            } else {
                if (ObjectUtils.notEqual(userTenantId, buyPackageEntity.getTenantId()) || ObjectUtils.notEqual(userFranchiseeId, Long.valueOf(buyPackageEntity.getFranchiseeId()))) {
                    log.error("buyRentalPackageOrder failed. Package belong mismatch. ");
                    return R.fail("300005", "套餐不匹配");
                }
            }

            // 7.4 比对套餐限制
            if (ObjectUtils.isNotEmpty(rentalPackageConfine) && !rentalPackageConfine.equals(buyPackageEntity.getConfine())) {
                log.error("bindingPackage failed. Package confine mismatch. ");
                throw new BizException("300005", "套餐不匹配");
            }

            if (ObjectUtils.isEmpty(deposit)) {
                log.info("buyRentalPackageOrder deposit from rentalPackage.");
                deposit = buyPackageEntity.getDeposit();
            }

            // 7.4 类型一致、归属一致，比对：型号（车或者车、电） + 押金
            if (ObjectUtils.isNotEmpty(memberTermEntity)) {
                if (!MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    if (buyPackageEntity.getDeposit().compareTo(deposit) != 0) {
                        log.error("buyRentalPackageOrder failed. Package deposit mismatch. ");
                        return R.fail("300005", "套餐不匹配");
                    }
                    // 比对型号
                    Long  oriRentalPackageId = memberTermEntity.getRentalPackageId();
                    if (ObjectUtils.isEmpty(oriRentalPackageId)) {
                        // 查找押金缴纳的套餐ID
                        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(memberTermEntity.getDepositPayOrderNo());
                        oriRentalPackageId = depositPayEntity.getRentalPackageId();
                    }

                    if (ObjectUtils.isNotEmpty(oriRentalPackageId)) {
                        // 比对车辆型号
                        CarRentalPackagePo oriCarRentalPackageEntity = carRentalPackageService.selectById(oriRentalPackageId);
                        if (!oriCarRentalPackageEntity.getCarModelId().equals(buyPackageEntity.getCarModelId())) {
                            log.error("buyRentalPackageOrder failed. Package carModelId mismatch. ");
                            return R.fail("300005", "套餐不匹配");
                        }
                        // 车电一体，比对电池型号
                        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(oriCarRentalPackageEntity.getType())) {
                            // 恶心的逻辑判断，加盟商，存在多型号电池和单型号电池，若单型号电池，则电池型号为空
                            Franchisee franchisee = franchiseeService.queryByIdFromCache(userFranchiseeId);
                            if (ObjectUtils.isEmpty(franchisee)) {
                                log.error("buyRentalPackageOrder failed. not found franchisee. franchiseeId is {}", userFranchiseeId);
                                return R.fail("300000", "数据有误");
                            }
                            if (Franchisee.NEW_MODEL_TYPE.equals(franchisee.getModelType())) {
                                List<String> oriBatteryList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(oriCarRentalPackageEntity.getId()).stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).collect(Collectors.toList());
                                // TODO 临时处理
                                List<String> oriBatterySimpleList = oriBatteryList.stream().map(n -> {
                                    StringJoiner simpleModel = new StringJoiner("_");
                                    String[] strings = n.split("_");
                                    simpleModel.add(strings[0]).add(strings[1]).add(strings[strings.length - 1]);
                                    return simpleModel.toString();
                                }).collect(Collectors.toList());
                                // TODO 临时处理
                                List<String> buyBatteryList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(buyPackageEntity.getId()).stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).collect(Collectors.toList());
                                List<String> buyBatterySimpleList = buyBatteryList.stream().map(n -> {
                                    StringJoiner simpleModel = new StringJoiner("_");
                                    String[] strings = n.split("_");
                                    simpleModel.add(strings[0]).add(strings[1]).add(strings[strings.length - 1]);
                                    return simpleModel.toString();
                                }).collect(Collectors.toList());

                                if (!buyBatterySimpleList.containsAll(oriBatterySimpleList)) {
                                    log.error("buyRentalPackageOrder failed. Package battery mismatch. ");
                                    throw new BizException("300005", "套餐不匹配");
                                }
                            }
                        }
                    }
                }
            }

            // 8. 保险校验
            FranchiseeInsurance buyInsurance = null;
            Long buyInsuranceId = buyOptModel.getInsuranceId();
            Boolean buyInsuranceFlag = insuranceUserInfoService.verifyUserIsNeedBuyInsurance(userInfo, buyPackageEntity.getType(), buyPackageEntity.getBatteryVoltage(), Long.valueOf(buyPackageEntity.getCarModelId()));
            if (buyInsuranceFlag) {
                if (ObjectUtils.isEmpty(buyInsuranceId)) {
                    log.error("buyRentalPackageOrder failed. Please purchase insurance. ");
                    return R.fail("300024", "请购买保险");
                }
            }
            // 8.1 查询保险信息
            if (ObjectUtils.isNotEmpty(buyInsuranceId)) {
                buyInsurance = franchiseeInsuranceService.queryByIdFromCache(buyInsuranceId.intValue());
                if (ObjectUtils.isEmpty(buyInsurance)) {
                    log.error("buyRentalPackageOrder failed. not found franchisee_insurance. insuranceId is {}", buyInsuranceId);
                    return R.fail("300025", "保险不存在");
                }

                if (FranchiseeInsurance.STATUS_UN_USABLE.equals(buyInsurance.getStatus())) {
                    log.error("buyRentalPackageOrder failed. Insurance disabled. insuranceId is {}", buyInsuranceId);
                    return R.fail("300026", "保险已被禁用");
                }

                if (!Long.valueOf(buyPackageEntity.getCarModelId()).equals(buyInsurance.getCarModelId())) {
                    log.error("buyRentalPackageOrder failed. Insurance and package do not match. Insurance's carModelId is {}, package's carModelId is {}", buyInsurance.getCarModelId(), buyPackageEntity.getCarModelId());
                    return R.fail("300027", "保险与套餐不匹配");
                }

                if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyPackageEntity.getType())) {
                    // 恶心的逻辑判断，加盟商，存在多型号电池和单型号电池，若单型号电池，则电池型号为空
                    Franchisee franchisee = franchiseeService.queryByIdFromCache(userFranchiseeId);
                    if (ObjectUtils.isEmpty(franchisee)) {
                        log.error("buyRentalPackageOrder failed. not found franchisee. franchiseeId is {}", userFranchiseeId);
                        return R.fail("300000", "数据有误");
                    }
                    if (Franchisee.NEW_MODEL_TYPE.equals(franchisee.getModelType()) && !buyPackageEntity.getBatteryVoltage().equals(buyInsurance.getSimpleBatteryType())) {
                        log.error("buyRentalPackageOrder failed. Package battery mismatch. ");
                        return R.fail("300027", "保险与套餐不匹配");
                    }
                }
            }

            // 检测结束，进入购买阶段
            Integer payType = buyOptModel.getPayType();
            // 1）押金处理
            // 待新增的押金信息，肯定没有走免押
            CarRentalPackageDepositPayPo depositPayInsertEntity = null;
            // 押金缴纳订单编码
            String depositPayOrderNo = null;
            CarRentalPackageDepositPayVo depositPayVo = carRenalPackageDepositBizService.selectUnRefundCarDeposit(tenantId, uid);
            // 没有押金订单，此时肯定也没有申请免押，因为免押是另外的线路，在下订单之前就已经生成记录了
            if (ObjectUtils.isEmpty(depositPayVo) || PayStateEnum.UNPAID.getCode().equals(depositPayVo.getPayState())) {
                // 生成押金缴纳订单，准备 insert
                depositPayInsertEntity = buildCarRentalPackageDepositPay(tenantId, uid, buyPackageEntity.getDeposit(), YesNoEnum.NO.getCode(), buyPackageEntity.getFranchiseeId(), buyPackageEntity.getStoreId(), buyPackageEntity.getType(), payType, buyPackageEntity.getId());
                depositPayOrderNo = depositPayInsertEntity.getOrderNo();
            } else {
                // 存在押金信息
                depositPayOrderNo = depositPayVo.getOrderNo();
                log.info("buyRentalPackageOrder deposit paid. depositPayOrderNo is {}", depositPayOrderNo);
                deposit = BigDecimal.ZERO;
            }

            // 2）保险处理
            InsuranceOrder insuranceOrderInsertEntity = buildInsuranceOrder(userInfo, buyInsurance, payType);
            // 保险费用
            BigDecimal insuranceAmount = ObjectUtils.isNotEmpty(buyInsurance) ? buyInsurance.getPremium() : BigDecimal.ZERO;

            // 3）支付金额处理
            // 优惠券只抵扣租金
            Integer packageType = RentalPackageTypeEnum.CAR.getCode().equals(buyPackageEntity.getType()) ? PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode() : PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode();
            Triple<BigDecimal, List<Long>, Boolean> couponTriple = carRentalPackageBizService.calculatePaymentAmount(buyPackageEntity.getRent(), buyOptModel.getUserCouponIds(), uid, buyRentalPackageId, packageType);
            // 实际支付租金金额
            BigDecimal rentPaymentAmount = couponTriple.getLeft();
            log.info("buyRentalPackageOrder rentPaymentAmount is {}", rentPaymentAmount);
            // 实际支付总金额（租金 + 押金 + 保险）
            BigDecimal paymentAmount = rentPaymentAmount.add(deposit).add(insuranceAmount);
            log.info("buyRentalPackageOrder paymentAmount is {}", paymentAmount);

            // 4）生成租车套餐订单，准备 insert
            CarRentalPackageOrderPo carRentalPackageOrder = buildCarRentalPackageOrder(buyPackageEntity, rentPaymentAmount, tenantId, uid, depositPayOrderNo, payType);
            carRentalPackageOrderService.insert(carRentalPackageOrder);

            // 判定 depositPayInsertEntity 是否需要新增
            if (ObjectUtils.isNotEmpty(depositPayInsertEntity)) {
                depositPayInsertEntity.setRentalPackageOrderNo(carRentalPackageOrder.getOrderNo());
                carRentalPackageDepositPayService.insert(depositPayInsertEntity);
            }

            // 赋值保险订单来源订单编码
            if (ObjectUtils.isNotEmpty(insuranceOrderInsertEntity)) {
                insuranceOrderInsertEntity.setSourceOrderNo(carRentalPackageOrder.getOrderNo());
                insuranceOrderService.insert(insuranceOrderInsertEntity);
            }

            // 5）租车套餐会员期限处理
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                // 生成租车套餐会员期限表信息，准备 Insert
                CarRentalPackageMemberTermPo memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, buyPackageEntity, carRentalPackageOrder);
                carRentalPackageMemberTermService.insert(memberTermInsertEntity);
            } else {
                if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                    // 先删除
                    carRentalPackageMemberTermService.delByUidAndTenantId(memberTermEntity.getTenantId(), memberTermEntity.getUid(), memberTermEntity.getUid());
                    // 生成租车套餐会员期限表信息，准备 Insert
                    CarRentalPackageMemberTermPo memberTermInsertEntity = buildCarRentalPackageMemberTerm(tenantId, uid, buyPackageEntity, carRentalPackageOrder);
                    carRentalPackageMemberTermService.insert(memberTermInsertEntity);
                }
            }

            // 6）更改用户优惠券状态使用中
            // 使用的优惠券
            List<Long> userCouponIds = CollectionUtils.isEmpty(couponTriple.getMiddle()) ? new ArrayList<>() : couponTriple.getMiddle();
            List<UserCoupon> userCouponList = buildUserCouponList(userCouponIds, UserCoupon.STATUS_IS_BEING_VERIFICATION, carRentalPackageOrder.getOrderNo());
            userCouponService.batchUpdateUserCoupon(userCouponList);

            // 7）支付零元的处理
            if (BigDecimal.ZERO.compareTo(paymentAmount) >= 0) {
                // 无须唤起支付，走支付回调的逻辑，抽取方法，直接调用
                handBuyRentalPackageOrderSuccess(carRentalPackageOrder.getOrderNo(), tenantId, uid, userCouponIds);
                return R.ok();
            }

            log.info("buyRentalPackageOrder paymentAmount is {}", paymentAmount);

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
        } catch (BizException e) {
            log.error("buyRentalPackageOrder failed. ", e);
            throw new BizException(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            log.error("buyRentalPackageOrder failed. ", e);
            throw new BizException("000001", "系统异常");
        } finally {
            redisService.delete(buyLockKey);
        }

    }

    /**
     * 支付成功之后的逻辑<br />
     * 此处逻辑不包含回调处理，是回调逻辑中的一处子逻辑<br />
     * 调用此方法需要慎重
     * @param buyOrderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param userCouponIds 用户优惠券ID集，可为空
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> handBuyRentalPackageOrderSuccess(String buyOrderNo, Integer tenantId, Long uid, List<Long> userCouponIds) {
        log.info("handBuyRentalPackageOrderSuccess, buyOrderNo is {}, uid is {}", buyOrderNo, uid);
        long currentTimeMillis = System.currentTimeMillis();

        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPo buyCarRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(buyOrderNo);
        if (ObjectUtil.isEmpty(buyCarRentalPackageOrderEntity)) {
            log.error("handBuyRentalPackageOrderSuccess failed, not found car_rental_package_order, order_no is {}", buyOrderNo);
            return Pair.of(false, "未找到租车套餐购买订单");
        }

        // 订单支付状态不匹配
        if (ObjectUtil.notEqual(PayStateEnum.UNPAID.getCode(), buyCarRentalPackageOrderEntity.getPayState())) {
            log.error("handBuyRentalPackageOrderSuccess failed, car_rental_package_order processed, order_no is {}", buyOrderNo);
            return Pair.of(false, "租车套餐购买订单已处理");
        }

        // 2. 处理租车套餐押金缴纳订单
        String depositPayOrderNo = buyCarRentalPackageOrderEntity.getDepositPayOrderNo();
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("handBuyRentalPackageOrderSuccess failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            return Pair.of(false, "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            log.info("handBuyRentalPackageOrderSuccess, change the payment status of the deposit and the payment and usage status of the order. ");
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.SUCCESS.getCode());
            // 更改套餐订单支付状态、使用状态、开始使用时间
            carRentalPackageOrderService.updateStateByOrderNo(buyOrderNo, PayStateEnum.SUCCESS.getCode(), UseStateEnum.IN_USE.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("handBuyRentalPackageOrderSuccess failed, not found car_rental_package_member_term, uid is {}", uid);
            return Pair.of(false, "未找到租车会员记录信息");
        }

        // 待生效的数据，直接更改状态
        if (MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.info("handBuyRentalPackageOrderSuccess, pending effective member data, changing normal status. ");
            carRentalPackageMemberTermService.updateStatusById(memberTermEntity.getId(), MemberTermStatusEnum.NORMAL.getCode(), null);
        } else {
            // 正常的数据，更改总计到期时间、总计套餐余量
            if (MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
                log.info("handBuyRentalPackageOrderSuccess, member status is normal. ");
                CarRentalPackageMemberTermPo memberTermUpdateEntity = new CarRentalPackageMemberTermPo();
                memberTermUpdateEntity.setId(memberTermEntity.getId());

                // 原本套餐，非退租且已过期
                if (StringUtils.isNotBlank(memberTermEntity.getRentalPackageOrderNo())) {
                    // 过期使用中
                    if (memberTermEntity.getDueTime() <= currentTimeMillis || (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTermEntity.getRentalPackageConfine()) && memberTermEntity.getResidue() <= 0L)) {
                        // 懒加载
                        log.info("handBuyRentalPackageOrderSuccess, member expired and in use. ");
                        // 根据用户ID查询第一条未使用的支付成功的订单信息
                        CarRentalPackageOrderPo packageUnUseOrderEntity = carRentalPackageOrderService.selectFirstUnUsedAndPaySuccessByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                        if (ObjectUtils.isNotEmpty(packageUnUseOrderEntity)) {
                            log.info("handBuyRentalPackageOrderSuccess, Lazy loading of orders.");

                            memberTermUpdateEntity.setRentalPackageId(packageUnUseOrderEntity.getRentalPackageId());
                            memberTermUpdateEntity.setRentalPackageType(packageUnUseOrderEntity.getRentalPackageType());
                            memberTermUpdateEntity.setRentalPackageOrderNo(packageUnUseOrderEntity.getOrderNo());
                            memberTermUpdateEntity.setRentalPackageConfine(packageUnUseOrderEntity.getConfine());
                            // 计算到期时间
                            Integer tenancy = packageUnUseOrderEntity.getTenancy();
                            Integer tenancyUnit = packageUnUseOrderEntity.getTenancyUnit();
                            long dueTime = currentTimeMillis;
                            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                                dueTime = currentTimeMillis + (tenancy * TimeConstant.DAY_MILLISECOND);
                            }
                            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                                dueTime = currentTimeMillis + (tenancy * TimeConstant.MINUTE_MILLISECOND);
                            }

                            // 计算新买的订单到期时间
                            Integer tenancyNew = buyCarRentalPackageOrderEntity.getTenancy();
                            Integer tenancyUnitNew = buyCarRentalPackageOrderEntity.getTenancyUnit();
                            long dueTimeTotalNew = memberTermEntity.getDueTimeTotal();
                            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnitNew)) {
                                dueTimeTotalNew = dueTimeTotalNew + (tenancyNew * TimeConstant.DAY_MILLISECOND);
                            }
                            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnitNew)) {
                                dueTimeTotalNew = dueTimeTotalNew + (tenancyNew * TimeConstant.MINUTE_MILLISECOND);
                            }

                            memberTermUpdateEntity.setDueTime(dueTime - (currentTimeMillis - memberTermEntity.getDueTime()));
                            memberTermUpdateEntity.setDueTimeTotal(dueTimeTotalNew);

                            // 套餐购买总次数
                            memberTermUpdateEntity.setPayCount(memberTermEntity.getPayCount() + 1);
                            if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageUnUseOrderEntity.getConfine())) {
                                if (memberTermEntity.getResidue() >= 0) {
                                    memberTermUpdateEntity.setResidue(packageUnUseOrderEntity.getConfineNum());
                                } else {
                                    memberTermUpdateEntity.setResidue(packageUnUseOrderEntity.getConfineNum() + memberTermEntity.getResidue());
                                }
                            }

                            carRentalPackageOrderService.updateUseStateByOrderNo(packageUnUseOrderEntity.getOrderNo(), UseStateEnum.IN_USE.getCode(), null, memberTermEntity.getDueTime());
                            carRentalPackageOrderService.updateUseStateByOrderNo(memberTermEntity.getRentalPackageOrderNo(), UseStateEnum.EXPIRED.getCode(), null, null);
                            carRentalPackageOrderService.updatePayStateByOrderNo(buyOrderNo, PayStateEnum.SUCCESS.getCode());

                        } else {
                            memberTermUpdateEntity.setRentalPackageId(buyCarRentalPackageOrderEntity.getRentalPackageId());
                            memberTermUpdateEntity.setRentalPackageType(buyCarRentalPackageOrderEntity.getRentalPackageType());
                            memberTermUpdateEntity.setRentalPackageOrderNo(buyOrderNo);
                            memberTermUpdateEntity.setRentalPackageConfine(buyCarRentalPackageOrderEntity.getConfine());
                            if (RenalPackageConfineEnum.NUMBER.getCode().equals(buyCarRentalPackageOrderEntity.getConfine())) {
                                if (memberTermEntity.getResidue() >= 0) {
                                    memberTermUpdateEntity.setResidue(buyCarRentalPackageOrderEntity.getConfineNum());
                                } else {
                                    memberTermUpdateEntity.setResidue(buyCarRentalPackageOrderEntity.getConfineNum() + memberTermEntity.getResidue());
                                }
                            }
                            // 计算到期时间
                            Integer tenancy = buyCarRentalPackageOrderEntity.getTenancy();
                            Integer tenancyUnit = buyCarRentalPackageOrderEntity.getTenancyUnit();
                            long dueTime = currentTimeMillis;
                            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                                dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
                            }
                            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                                dueTime = dueTime + (tenancy * TimeConstant.MINUTE_MILLISECOND);
                            }

                            memberTermUpdateEntity.setDueTime(dueTime);
                            memberTermUpdateEntity.setDueTimeTotal(dueTime);

                            // 套餐购买总次数
                            memberTermUpdateEntity.setPayCount(memberTermEntity.getPayCount() + 1);

                            // 更改套餐购买订单的使用状态
                            carRentalPackageOrderService.updateUseStateByOrderNo(memberTermEntity.getRentalPackageOrderNo(), UseStateEnum.EXPIRED.getCode(), null, null);
                            carRentalPackageOrderService.updateStateByOrderNo(buyOrderNo, PayStateEnum.SUCCESS.getCode(), UseStateEnum.IN_USE.getCode());
                        }

                    } else {
                        // 未过期使用中
                        // 计算总到期时间
                        Integer tenancy = buyCarRentalPackageOrderEntity.getTenancy();
                        Integer tenancyUnit = buyCarRentalPackageOrderEntity.getTenancyUnit();
                        long dueTimeTotal = ObjectUtils.isNotEmpty(memberTermEntity.getDueTimeTotal()) ? memberTermEntity.getDueTimeTotal() : currentTimeMillis;
                        if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                            dueTimeTotal = dueTimeTotal + (tenancy * TimeConstant.DAY_MILLISECOND);
                        }
                        if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                            dueTimeTotal = dueTimeTotal + (tenancy * TimeConstant.MINUTE_MILLISECOND);
                        }
                        memberTermUpdateEntity.setDueTimeTotal(dueTimeTotal);

                        // 套餐购买总次数
                        memberTermUpdateEntity.setPayCount(memberTermEntity.getPayCount() + 1);
                        // 更改套餐购买订单的支付状态
                        carRentalPackageOrderService.updatePayStateByOrderNo(buyOrderNo, PayStateEnum.SUCCESS.getCode());
                    }

                } else {
                    // 计算总到期时间
                    Integer tenancy = buyCarRentalPackageOrderEntity.getTenancy();
                    Integer tenancyUnit = buyCarRentalPackageOrderEntity.getTenancyUnit();
                    long dueTimeTotal = ObjectUtils.isNotEmpty(memberTermEntity.getDueTimeTotal()) ? memberTermEntity.getDueTimeTotal() : currentTimeMillis;
                    if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                        dueTimeTotal = dueTimeTotal + (tenancy * TimeConstant.DAY_MILLISECOND);
                    }
                    if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                        dueTimeTotal = dueTimeTotal + (tenancy * TimeConstant.MINUTE_MILLISECOND);
                    }
                    memberTermUpdateEntity.setDueTimeTotal(dueTimeTotal);

                    // 套餐购买总次数
                    memberTermUpdateEntity.setPayCount(memberTermEntity.getPayCount() + 1);

                    // 退租未退押
                    memberTermUpdateEntity.setRentalPackageId(buyCarRentalPackageOrderEntity.getRentalPackageId());
                    memberTermUpdateEntity.setRentalPackageOrderNo(buyOrderNo);
                    memberTermUpdateEntity.setRentalPackageConfine(buyCarRentalPackageOrderEntity.getConfine());
                    memberTermUpdateEntity.setDueTime(memberTermUpdateEntity.getDueTimeTotal());
                    memberTermUpdateEntity.setResidue(buyCarRentalPackageOrderEntity.getConfineNum());
                    // 更改套餐购买订单的使用状态
                    carRentalPackageOrderService.updateStateByOrderNo(buyOrderNo, PayStateEnum.SUCCESS.getCode(), UseStateEnum.IN_USE.getCode());
                }

                carRentalPackageMemberTermService.updateById(memberTermUpdateEntity);
            }
        }

        // 4. 处理用户押金支付信息、套餐购买次数信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        userInfo.setUpdateTime(currentTimeMillis);

        if (ObjectUtils.isEmpty(userInfo)) {
            log.error("handBuyRentalPackageOrderSuccess failed, not found user_info, uid is {}", uid);
            return Pair.of(false, "未找到用户信息");
        }

        if (YesNoEnum.NO.getCode().equals(userInfo.getCarBatteryDepositStatus()) || UserInfo.CAR_DEPOSIT_STATUS_NO.equals(userInfo.getCarDepositStatus())) {
            userInfo.setFranchiseeId(Long.valueOf(buyCarRentalPackageOrderEntity.getFranchiseeId()));
            userInfo.setStoreId(Long.valueOf(buyCarRentalPackageOrderEntity.getStoreId()));
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyCarRentalPackageOrderEntity.getRentalPackageType())) {
                userInfo.setCarBatteryDepositStatus(YesNoEnum.YES.getCode());
            } else {
                userInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            }
        }
        userInfo.setPayCount(userInfo.getPayCount() + 1);
        userInfoService.updateByUid(userInfo);

        // 6. 处理用户优惠券的使用状态
        if (!CollectionUtils.isEmpty(userCouponIds)) {
            // 此参数为了兼容同一个大事务，数据尚未落库。二次查询，目的是为了拿在事务缓存中的最新数据
            List<UserCoupon> userCoupons = userCouponService.listByIds(userCouponIds);
            if (!CollectionUtils.isEmpty(userCoupons)) {
                userCouponService.batchUpdateUserCoupon(buildUserCouponList(userCouponIds, UserCoupon.STATUS_USED, buyOrderNo));
            }
        } else {
            userCouponService.updateStatusByOrderId(buyOrderNo, UserCoupon.STATUS_USED);
        }

        // 7. 处理保险购买订单
        InsuranceOrder insuranceOrder = insuranceOrderService.selectBySourceOrderNoAndType(buyOrderNo, buyCarRentalPackageOrderEntity.getRentalPackageType());
        if (ObjectUtils.isNotEmpty(insuranceOrder)) {
            // 7.1 更改保险订单的支付状态
            insuranceOrder.setStatus(InsuranceOrder.STATUS_SUCCESS);
            insuranceOrder.setUpdateTime(currentTimeMillis);
            insuranceOrderService.updateOrderStatusById(insuranceOrder);
            // 7.2 给用户绑定保险
            insuranceUserInfoService.saveUserInsurance(insuranceOrder);
        }

        // 11. 车辆解锁
        ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(electricityCar)) {
            ElectricityConfig electricityConfig = electricityConfigService
                    .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects
                    .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {
                Boolean result = carRentalOrderBizService.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
                log.info("handBuyRentalPackageOrderSuccess, carRentalOrderBizService.retryCarLockCtrl result is {}", result);
                CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
                carLockCtrlHistory.setUid(userInfo.getUid());
                carLockCtrlHistory.setName(userInfo.getName());
                carLockCtrlHistory.setPhone(userInfo.getPhone());
                carLockCtrlHistory
                        .setStatus(result ? CarLockCtrlHistory.STATUS_UN_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_UN_LOCK_FAIL);
                carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
                carLockCtrlHistory.setCarModel(electricityCar.getModel());
                carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
                carLockCtrlHistory.setCarSn(electricityCar.getSn());
                carLockCtrlHistory.setCreateTime(currentTimeMillis);
                carLockCtrlHistory.setUpdateTime(currentTimeMillis);
                carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
                carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_MEMBER_CARD_UN_LOCK);

                carLockCtrlHistoryService.insert(carLockCtrlHistory);
            }
        }



        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                // 8. 处理分账
                DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
                divisionAccountOrderDTO.setOrderNo(buyOrderNo);
                divisionAccountOrderDTO.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyCarRentalPackageOrderEntity.getRentalPackageType()) ? PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode() : PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
                divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_PURCHASE.getCode());
                divisionAccountOrderDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
                divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);

                // 9. 处理活动
                ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
                activityProcessDTO.setOrderNo(buyOrderNo);
                activityProcessDTO.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyCarRentalPackageOrderEntity.getRentalPackageType()) ? PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode() : PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
                activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode());
                activityProcessDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
                activityService.asyncProcessActivity(activityProcessDTO);

                // 10. 发放优惠券
                if (ObjectUtils.isNotEmpty(buyCarRentalPackageOrderEntity.getCouponId())) {
                    UserCouponDTO userCouponDTO = new UserCouponDTO();
                    userCouponDTO.setCouponId(buyCarRentalPackageOrderEntity.getCouponId());
                    userCouponDTO.setUid(uid);
                    userCouponDTO.setSourceOrderNo(buyOrderNo);
                    userCouponDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
                    userCouponService.asyncSendCoupon(userCouponDTO);
                }

                // 车电一体，同步电池会员信息
                if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(buyCarRentalPackageOrderEntity.getRentalPackageType())) {
                    // 同步押金
                    if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
                        log.info("handBuyRentalPackageOrderSuccess, userBatteryDepositService.synchronizedUserBatteryDepositInfo. depositPayOrderNo is {}", depositPayEntity.getOrderNo());
                        userBatteryDepositService.synchronizedUserBatteryDepositInfo(uid, null, depositPayEntity.getOrderNo(), depositPayEntity.getDeposit());
                    }
                    // 同步电池会员表数据
                    // 此处二次查询，目的是为了拿在事务缓存中的最新数据
                    CarRentalPackageMemberTermPo memberTermEntityProcessed = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
                    List<CarRentalPackageCarBatteryRelPo> carBatteryRelPos = carRentalPackageCarBatteryRelService.selectByRentalPackageId(memberTermEntityProcessed.getRentalPackageId());
                    if (!CollectionUtils.isEmpty(carBatteryRelPos)) {
                        List<String> batteryTypes = carBatteryRelPos.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).collect(Collectors.toList());
                        log.info("handBuyRentalPackageOrderSuccess, userBatteryTypeService.synchronizedUserBatteryType, batteryTypes is {}", JsonUtil.toJson(batteryTypes));
                        userBatteryTypeService.synchronizedUserBatteryType(uid, tenantId, batteryTypes);
                    }
                }

            }
        });

        return Pair.of(true, userInfo.getPhone());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> handBuyRentalPackageOrderFailed(String orderNo, Integer tenantId, Long uid) {
        log.info("handBuyRentalPackageOrderFailed, orderNo is {}, uid is {}", orderNo, uid);

        // 1. 处理租车套餐购买订单
        CarRentalPackageOrderPo carRentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
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
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity)) {
            log.error("NotifyCarRenalPackageOrder failed, not found car_rental_package_deposit_pay, order_no is {}", depositPayOrderNo);
            return Pair.of(false, "未找到租车套餐押金缴纳订单");
        }

        // 判定押金缴纳订单是否需要更改支付状态
        if (ObjectUtil.equal(PayStateEnum.UNPAID.getCode(), depositPayEntity.getPayState())) {
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.FAILED.getCode());
        }

        // 3. 处理租车套餐会员期限
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
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
        userCouponService.updateStatusByOrderId(orderNo, UserCoupon.STATUS_UNUSED);

        return Pair.of(true, null);
    }

    /**
     * 构建租车套餐会员期限信息(首次新增数据，用此方法，此方法内有保存总到期时间)
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param packageEntity 租车套餐信息
     * @param carRentalPackageOrderEntity 租车套餐订单信息
     * @return 将要新增的租车会员期限信息
     */
    private CarRentalPackageMemberTermPo buildCarRentalPackageMemberTerm(Integer tenantId, Long uid, CarRentalPackagePo packageEntity, CarRentalPackageOrderPo carRentalPackageOrderEntity) {
        CarRentalPackageMemberTermPo carRentalPackageMemberTermEntity = new CarRentalPackageMemberTermPo();
        carRentalPackageMemberTermEntity.setUid(uid);
        carRentalPackageMemberTermEntity.setRentalPackageOrderNo(carRentalPackageOrderEntity.getOrderNo());
        carRentalPackageMemberTermEntity.setRentalPackageId(packageEntity.getId());
        carRentalPackageMemberTermEntity.setRentalPackageType(packageEntity.getType());
        carRentalPackageMemberTermEntity.setRentalPackageConfine(packageEntity.getConfine());
        carRentalPackageMemberTermEntity.setResidue(packageEntity.getConfineNum());
        carRentalPackageMemberTermEntity.setStatus(MemberTermStatusEnum.PENDING_EFFECTIVE.getCode());
        carRentalPackageMemberTermEntity.setDeposit(carRentalPackageOrderEntity.getDeposit());
        carRentalPackageMemberTermEntity.setDepositPayOrderNo(carRentalPackageOrderEntity.getDepositPayOrderNo());
        carRentalPackageMemberTermEntity.setTenantId(tenantId);
        carRentalPackageMemberTermEntity.setFranchiseeId(packageEntity.getFranchiseeId());
        carRentalPackageMemberTermEntity.setStoreId(packageEntity.getStoreId());
        carRentalPackageMemberTermEntity.setCreateUid(uid);
        carRentalPackageMemberTermEntity.setUpdateUid(uid);
        carRentalPackageMemberTermEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageMemberTermEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageMemberTermEntity.setDelFlag(DelFlagEnum.OK.getCode());
        carRentalPackageMemberTermEntity.setPayCount(1);
        // 计算到期时间
        Integer tenancy = packageEntity.getTenancy();
        Integer tenancyUnit = packageEntity.getTenancyUnit();
        long dueTime = System.currentTimeMillis();
        if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
            dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
        }
        if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
            dueTime = dueTime + (tenancy * TimeConstant.MINUTE_MILLISECOND);
        }
        carRentalPackageMemberTermEntity.setDueTime(dueTime);
        carRentalPackageMemberTermEntity.setDueTimeTotal(dueTime);

        return carRentalPackageMemberTermEntity;
    }

    /**
     * 构建保险订单信息
     * @param userInfo 用户信息
     * @param buyInsurance 保险信息
     * @param payType 交易方式
     * @return 保险订单信息
     */
    private InsuranceOrder buildInsuranceOrder(UserInfo userInfo, FranchiseeInsurance buyInsurance, Integer payType) {
        if (ObjectUtils.isEmpty(buyInsurance)) {
            return null;
        }
        InsuranceOrder insuranceOrder = new InsuranceOrder();
        insuranceOrder.setPayAmount(buyInsurance.getPremium());
        insuranceOrder.setValidDays(buyInsurance.getValidDays());
        insuranceOrder.setUid(userInfo.getUid());
        insuranceOrder.setPhone(userInfo.getPhone());
        insuranceOrder.setUserName(userInfo.getName());
        insuranceOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_INSURANCE, userInfo.getUid()));
        insuranceOrder.setStatus(InsuranceOrder.STATUS_INIT);
        insuranceOrder.setInsuranceId(buyInsurance.getId());
        insuranceOrder.setInsuranceName(buyInsurance.getName());
        insuranceOrder.setInsuranceType(buyInsurance.getInsuranceType());
        insuranceOrder.setFranchiseeId(buyInsurance.getFranchiseeId());
        insuranceOrder.setStoreId(buyInsurance.getStoreId());
        insuranceOrder.setCid(buyInsurance.getCid());
        insuranceOrder.setForehead(buyInsurance.getForehead());
        insuranceOrder.setIsUse(InsuranceOrder.NOT_USE);
        insuranceOrder.setTenantId(buyInsurance.getTenantId());
        long nowTime = System.currentTimeMillis();
        insuranceOrder.setCreateTime(nowTime);
        insuranceOrder.setUpdateTime(nowTime);

        // 支付方式的转换赋值
        if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
            payType = InsuranceOrder.ONLINE_PAY_TYPE;
        }
        if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
            payType = InsuranceOrder.OFFLINE_PAY_TYPE;

        }
        insuranceOrder.setPayType(payType);

        return insuranceOrder;
    }

    /**
     * 构建押金订单信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param deposit 押金
     * @param freeDeposit 免押
     * @param franchiseeId 加盟商ID
     * @param storeId 门店ID
     * @param rentalPackageType 套餐类型
     * @param payType 交易方式
     * @param rentalPackageId 套餐ID
     * @return 待新增的押金缴纳订单
     */
    private CarRentalPackageDepositPayPo buildCarRentalPackageDepositPay(Integer tenantId, Long uid, BigDecimal deposit, Integer freeDeposit,
                                                                         Integer franchiseeId, Integer storeId, Integer rentalPackageType, Integer payType, Long rentalPackageId) {
        CarRentalPackageDepositPayPo carRentalPackageDepositPayEntity = new CarRentalPackageDepositPayPo();
        carRentalPackageDepositPayEntity.setUid(uid);
        carRentalPackageDepositPayEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, uid));
        carRentalPackageDepositPayEntity.setRentalPackageId(rentalPackageId);
        carRentalPackageDepositPayEntity.setRentalPackageType(rentalPackageType);
        carRentalPackageDepositPayEntity.setType(DepositTypeEnum.NORMAL.getCode());
        carRentalPackageDepositPayEntity.setChangeAmount(BigDecimal.ZERO);
        carRentalPackageDepositPayEntity.setDeposit(deposit);
        carRentalPackageDepositPayEntity.setFreeDeposit(freeDeposit);
        carRentalPackageDepositPayEntity.setPayType(payType);
        carRentalPackageDepositPayEntity.setPayState(PayStateEnum.UNPAID.getCode());
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
     * @return
     */
    private List<UserCoupon> buildUserCouponList(List<Long> userCouponIds, Integer status, String orderNo) {
        return userCouponIds.stream().map(userCouponId -> {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setId(userCouponId);
            userCoupon.setOrderId(orderNo);
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
    private CarRentalPackageOrderPo buildCarRentalPackageOrder(CarRentalPackagePo packagePO, BigDecimal rentPayment, Integer tenantId, Long uid, String depositPayOrderNo, Integer payType) {

        CarRentalPackageOrderPo carRentalPackageOrderEntity = new CarRentalPackageOrderPo();
        carRentalPackageOrderEntity.setUid(uid);
        carRentalPackageOrderEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_MEMBERCARD, uid));
        carRentalPackageOrderEntity.setRentalPackageId(packagePO.getId());
        carRentalPackageOrderEntity.setRentalPackageType(packagePO.getType());
        carRentalPackageOrderEntity.setConfine(packagePO.getConfine());
        carRentalPackageOrderEntity.setConfineNum(packagePO.getConfineNum());
        carRentalPackageOrderEntity.setTenancy(packagePO.getTenancy());
        carRentalPackageOrderEntity.setTenancyUnit(packagePO.getTenancyUnit());
        carRentalPackageOrderEntity.setRentUnitPrice(packagePO.getRentUnitPrice());
        carRentalPackageOrderEntity.setRent(packagePO.getRent());
        carRentalPackageOrderEntity.setRentPayment(rentPayment);
        carRentalPackageOrderEntity.setApplicableType(packagePO.getApplicableType());
        carRentalPackageOrderEntity.setRentRebate(packagePO.getRentRebate());
        carRentalPackageOrderEntity.setRentRebateTerm(packagePO.getRentRebateTerm());
        if (ObjectUtils.isNotEmpty(packagePO.getRentRebateTerm())) {
            carRentalPackageOrderEntity.setRentRebateEndTime(TimeConstant.DAY_MILLISECOND * packagePO.getRentRebateTerm() + System.currentTimeMillis());
        }
        carRentalPackageOrderEntity.setDeposit(packagePO.getDeposit());
        carRentalPackageOrderEntity.setDepositPayOrderNo(depositPayOrderNo);
        carRentalPackageOrderEntity.setLateFee(packagePO.getLateFee());
        carRentalPackageOrderEntity.setPayType(payType);
        if (YesNoEnum.YES.getCode().equals(packagePO.getGiveCoupon())) {
            carRentalPackageOrderEntity.setCouponId(packagePO.getCouponId());
        }
        carRentalPackageOrderEntity.setPayState(PayStateEnum.UNPAID.getCode());
        carRentalPackageOrderEntity.setUseState(UseStateEnum.UN_USED.getCode());
        carRentalPackageOrderEntity.setTenantId(tenantId);
        carRentalPackageOrderEntity.setFranchiseeId(packagePO.getFranchiseeId());
        carRentalPackageOrderEntity.setStoreId(packagePO.getStoreId());
        carRentalPackageOrderEntity.setCreateUid(uid);
        carRentalPackageOrderEntity.setUpdateUid(uid);
        carRentalPackageOrderEntity.setCreateTime(System.currentTimeMillis());
        carRentalPackageOrderEntity.setUpdateTime(System.currentTimeMillis());
        carRentalPackageOrderEntity.setDelFlag(DelFlagEnum.OK.getCode());

        return carRentalPackageOrderEntity;
    }
}
