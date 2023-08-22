package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.*;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.reqparam.opt.carpackage.MemberCurrPackageOptReq;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.*;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.car.CarRentalPackageDepositPayVo;
import com.xiliulou.electricity.vo.car.CarRentalPackageOrderVo;
import com.xiliulou.electricity.vo.car.CarVo;
import com.xiliulou.electricity.vo.userinfo.UserMemberInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermBizServiceImpl implements CarRentalPackageMemberTermBizService {

    @Resource
    private UserBatteryTypeService userBatteryTypeService;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private CarRentalOrderBizService carRentalOrderBizService;

    @Resource
    private CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private BatteryModelService batteryModelService;

    @Resource
    private StoreService storeService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private ElectricityCarService carService;

    @Resource
    private CarRentalPackageCarBatteryRelService carRentalPackageCarBatteryRelService;

    @Resource
    private ElectricityCarModelService carModelService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    @Resource
    private ElectricityBatteryService batteryService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    /**
     * 扣减余量次数
     * 只有状态正常且未过期，扣减成功返回为true
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean substractResidue(Integer tenantId, Long uid) {

        // 查询会员当前信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);

        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("isExpirePackageOrder, not found t_car_rental_package_member_term. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("isExpirePackageOrder. t_car_rental_package_member_term status is {}. uid is {}", memberTermEntity.getStatus(), uid);
            throw new BizException("300002", "租车会员状态异常");
        }

        if (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTermEntity.getRentalPackageConfine())) {
            CarRentalPackageMemberTermPo memberTermEntityUpdate = new CarRentalPackageMemberTermPo();
            memberTermEntityUpdate.setId(memberTermEntity.getId());
            memberTermEntityUpdate.setUpdateUid(uid);
            memberTermEntityUpdate.setUpdateTime(System.currentTimeMillis());
            memberTermEntityUpdate.setResidue(memberTermEntity.getResidue() - 1);
            return carRentalPackageMemberTermService.updateById(memberTermEntityUpdate);
        }

        return true;
    }

    /**
     * 判定租户的套餐是否过期<br />
     * 只有状态正常且过期，返回为true
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return true(过期)、false(未过期)
     */
    @Override
    public boolean isExpirePackageOrder(Integer tenantId, Long uid) {
        long now = System.currentTimeMillis();

        // 查询会员当前信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);

        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("isExpirePackageOrder, not found t_car_rental_package_member_term. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("isExpirePackageOrder. t_car_rental_package_member_term status is {}. uid is {}", memberTermEntity.getStatus(), uid);
            return false;
        }

        if (now >= memberTermEntity.getDueTimeTotal() || (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTermEntity.getRentalPackageConfine()) && memberTermEntity.getResidue() <= 0L)) {
            log.error("isExpirePackageOrder. t_car_rental_package_member_term time or residue is expire.");
            return true;
        }

        return false;
    }

    /**
     * 编辑会员当前套餐信息
     *
     * @param tenantId 租户ID
     * @param optReq  操作数据模型
     * @param optUid   操作用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean updateCurrPackage(Integer tenantId, MemberCurrPackageOptReq optReq, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, optReq, optReq.getUid(), optUid, optReq.getPackageOrderNo(), optReq.getType()) || !BasicEnum.isExist(optReq.getType(), MemberOptTypeEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Long uid = optReq.getUid();

        // 查询滞纳金信息
        boolean exitUnpaid = carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid);
        if (exitUnpaid) {
            throw new BizException("300006", "未缴纳押金");
        }

        long now = System.currentTimeMillis();

        // 查询会员当前信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("updateCurrPackage failed. t_car_rental_package_member_term not found or status is error. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        String rentalPackageOrderNo = memberTermEntity.getRentalPackageOrderNo();
        if (StringUtils.isBlank(rentalPackageOrderNo) || !optReq.getPackageOrderNo().equals(rentalPackageOrderNo)) {
            log.error("updateCurrPackage failed. t_car_rental_package_member_term rentalPackageOrderNo is expire. member's rentalPackageOrderNo is {}", rentalPackageOrderNo);
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        if (memberTermEntity.getDueTime() <= now || (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTermEntity.getRentalPackageConfine()) && memberTermEntity.getResidue() <= 0L)) {
            log.error("updateCurrPackage failed. t_car_rental_package_member_term time or frequency reaching threshold", rentalPackageOrderNo);
            throw new BizException("300042", "该套餐已过期，请返回上一步进行续费套餐");
        }

        // 查询订单信息
        CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(rentalPackageOrderNo);
        if (ObjectUtils.isEmpty(packageOrderEntity)) {
            log.error("updateCurrPackage failed. t_car_rental_package_order not found. rentalPackageOrderNo is {}", rentalPackageOrderNo);
            throw new BizException("300008", "未找到租车套餐购买订单");
        }

        if (YesNoEnum.YES.getCode().equals(packageOrderEntity.getRentRebate()) && packageOrderEntity.getRentRebateEndTime() >= System.currentTimeMillis()) {
            log.error("updateCurrPackage failed. No changes allowed within the refundable period. rentalPackageOrderNo is {}", rentalPackageOrderNo);
            throw new BizException("300058", "可退期限内，不允许变更");
        }

        Integer tenancyReq = optReq.getTenancy();
        Long dueTimeReq = optReq.getDueTime();
        Long residueReq = optReq.getResidue();

        Integer rentalPackageType = memberTermEntity.getRentalPackageType();
        Integer tenancyUnit = packageOrderEntity.getTenancyUnit();

        Integer rentalPackageConfine = memberTermEntity.getRentalPackageConfine();
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType) && RenalPackageConfineEnum.NUMBER.getCode().equals(rentalPackageConfine)) {
            if (ObjectUtils.isEmpty(residueReq)) {
                throw new BizException("ELECTRICITY.0007", "不合法的参数");
            }
        }

        Long dueTime = memberTermEntity.getDueTime();
        Long dueTimeTotal = memberTermEntity.getDueTimeTotal();
        Long residue = memberTermEntity.getResidue();

        Long dueTimeNew = dueTime;
        Long dueTimeTotalNew = dueTimeTotal;
        Long residueNew = residue;

        Integer type = optReq.getType();
        if (MemberOptTypeEnum.NUMBER.getCode().equals(type)) {
            if (ObjectUtils.isEmpty(tenancyReq)) {
                throw new BizException("ELECTRICITY.0007", "不合法的参数");
            }
            if (tenancyReq != 0) {
                // 天
                if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                    dueTimeNew = now + (tenancyReq * TimeConstant.DAY_MILLISECOND);
                }
                // 分钟
                if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                    dueTimeNew = now + (tenancyReq * TimeConstant.MINUTE_MILLISECOND);
                }
            } else {
                dueTimeNew = now;
            }
            // 总到期时间
            dueTimeTotalNew = dueTimeTotal - (dueTime - dueTimeNew);
        }

        if (MemberOptTypeEnum.TIME.getCode().equals(type)) {
            if (ObjectUtils.isEmpty(dueTimeReq)) {
                throw new BizException("ELECTRICITY.0007", "不合法的参数");
            }
            dueTimeNew = dueTimeReq;
            dueTimeTotalNew = dueTimeTotal - (dueTime -  dueTimeNew);
        }

        if (RenalPackageConfineEnum.NUMBER.getCode().equals(rentalPackageConfine) && ObjectUtils.isNotEmpty(residueReq)) {
            residueNew = residueReq;
        }

        // 待更新数据
        CarRentalPackageMemberTermPo newMemberTermEntity = new CarRentalPackageMemberTermPo();
        newMemberTermEntity.setId(memberTermEntity.getId());
        newMemberTermEntity.setUpdateUid(optUid);
        newMemberTermEntity.setUpdateTime(now);

        // 判定是否过期, 过期自动提订单
        if (dueTimeNew <= now || (RenalPackageConfineEnum.NUMBER.getCode().equals(rentalPackageConfine) && residueNew <= 0L)) {
            // 根据用户ID查询第一条未使用的支付成功的订单信息
            CarRentalPackageOrderPo packageOrderEntityUnUse = carRentalPackageOrderService.selectFirstUnUsedAndPaySuccessByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
            if (ObjectUtils.isNotEmpty(packageOrderEntityUnUse)) {
                // 二次保底确认
                CarRentalPackageMemberTermPo oriMemberTermEntity = carRentalPackageMemberTermService.selectById(memberTermEntity.getId());
                if (ObjectUtils.isEmpty(oriMemberTermEntity)) {
                    log.info("updateCurrPackage failed. t_car_rental_package_member_term Abnormal old data. skip. id is {}", memberTermEntity.getId());
                    throw new BizException("300002", "租车会员状态异常");
                }
                if (oriMemberTermEntity.getRentalPackageOrderNo().equals(packageOrderEntityUnUse.getOrderNo())) {
                    log.info("updateCurrPackage failed. t_car_rental_package_member_term processed. skip. id is {}", memberTermEntity.getId());
                    return true;
                }

                // 赋值新数据
                newMemberTermEntity.setRentalPackageOrderNo(packageOrderEntityUnUse.getOrderNo());
                newMemberTermEntity.setRentalPackageId(packageOrderEntityUnUse.getRentalPackageId());
                newMemberTermEntity.setRentalPackageType(packageOrderEntityUnUse.getRentalPackageType());
                newMemberTermEntity.setRentalPackageConfine(packageOrderEntityUnUse.getConfine());

                if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntityUnUse.getConfine())) {
                    if (memberTermEntity.getResidue() >= 0) {
                        newMemberTermEntity.setResidue(packageOrderEntityUnUse.getConfineNum());
                    } else {
                        newMemberTermEntity.setResidue(packageOrderEntityUnUse.getConfineNum() + memberTermEntity.getResidue());
                    }
                }
                // 计算当前到期时间
                Integer tenancyUnUse = packageOrderEntityUnUse.getTenancy();
                Integer tenancyUnitUnUse = packageOrderEntityUnUse.getTenancyUnit();
                Long dueTimeNow = now;
                if (RentalUnitEnum.DAY.getCode().equals(tenancyUnitUnUse)) {
                    dueTimeNow = now + (tenancyUnUse * TimeConstant.DAY_MILLISECOND);
                }
                if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnitUnUse)) {
                    dueTimeNow = now + (tenancyUnUse * TimeConstant.MINUTE_MILLISECOND);
                }
                newMemberTermEntity.setDueTime(dueTimeNow);
                newMemberTermEntity.setDueTimeTotal(dueTimeTotalNew);
            } else {
                newMemberTermEntity.setResidue(residueNew);
                newMemberTermEntity.setDueTime(now);
                newMemberTermEntity.setDueTimeTotal(now);
            }
        } else {
            newMemberTermEntity.setDueTime(dueTimeNew);
            newMemberTermEntity.setDueTimeTotal(dueTimeTotalNew);
            newMemberTermEntity.setResidue(residueNew);
        }

        saveUpdateCurrPackageTx(memberTermEntity, newMemberTermEntity, optUid);

        return true;
    }

    /**
     * 保存事务数据
     * @param memberTermEntity 旧的会员数据
     * @param newMemberTermEntity 新的会员数据
     * @param optUid 操作人用户UID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveUpdateCurrPackageTx(CarRentalPackageMemberTermPo memberTermEntity, CarRentalPackageMemberTermPo newMemberTermEntity, Long optUid) {
        carRentalPackageMemberTermService.updateById(newMemberTermEntity);
        if (StringUtils.isNotEmpty(newMemberTermEntity.getRentalPackageOrderNo())) {
            carRentalPackageOrderService.updateUseStateByOrderNo(memberTermEntity.getRentalPackageOrderNo(), UseStateEnum.EXPIRED.getCode(), optUid);
            carRentalPackageOrderService.updateUseStateByOrderNo(newMemberTermEntity.getRentalPackageOrderNo(), UseStateEnum.IN_USE.getCode(), optUid);
        }

        // 此处二次查询，目的是为了拿在事务缓存中的最新数据
        CarRentalPackageMemberTermPo memberTermEntityProcessed = carRentalPackageMemberTermService.selectByTenantIdAndUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntityProcessed.getRentalPackageType())) {
            List<CarRentalPackageCarBatteryRelPo> carBatteryRelPos = carRentalPackageCarBatteryRelService.selectByRentalPackageId(memberTermEntityProcessed.getRentalPackageId());
            if (!CollectionUtils.isEmpty(carBatteryRelPos)) {
                List<String> batteryTypes = carBatteryRelPos.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).collect(Collectors.toList());
                log.info("saveUpdateCurrPackageTx, userBatteryTypeService.synchronizedUserBatteryType, batteryTypes is {}", JsonUtil.toJson(batteryTypes));
                userBatteryTypeService.synchronizedUserBatteryType(memberTermEntity.getUid(), memberTermEntity.getTenantId(), batteryTypes);
            }
        }
    }

    /**
     * 根据用户ID获取会员的全量信息（套餐订单信息、车辆信息）
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 用户会员全量信息
     */
    @Override
    public UserMemberInfoVo queryUserMemberInfo(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查看会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.info("queryUserMemberInfo, t_car_rental_package_member_term is null or status is wrong. uid is {}", uid);
            return null;
        }

        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        String depositPayOrderNo = memberTermEntity.getDepositPayOrderNo();
        String rentalPackageOrderNo = memberTermEntity.getRentalPackageOrderNo();
        Integer rentalPackageType = memberTermEntity.getRentalPackageType();
        Integer franchiseeId = memberTermEntity.getFranchiseeId();
        Integer storeId = memberTermEntity.getStoreId();

        boolean rentalPackageEntityFlag = true;
        // 套餐信息
        CarRentalPackagePo rentalPackageEntity = carRentalPackageService.selectById(rentalPackageId);

        // 套餐订单信息
        CarRentalPackageOrderPo rentalPackageOrderEntity = carRentalPackageOrderService.selectByOrderNo(rentalPackageOrderNo);

        // 押金缴纳信息
        CarRentalPackageDepositPayPo depositPayEntity= carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(rentalPackageEntity)) {
            rentalPackageEntityFlag = false;
            rentalPackageEntity = carRentalPackageService.selectById(depositPayEntity.getRentalPackageId());
        }

        // 车辆型号信息
        ElectricityCarModel carModelEntity = carModelService.queryByIdFromCache(rentalPackageEntity.getCarModelId());

        // 用户车辆信息
        ElectricityCar carEntity = carService.selectByUid(tenantId, uid);

        // 加盟商信息
        Franchisee franchiseeEntity = franchiseeService.queryByIdFromCache(Long.valueOf(franchiseeId));

        // 门店信息
        Store storeEntity = storeService.queryByIdFromCache(Long.valueOf(storeId));

        // 套餐对应的电池型号信息、用户电池信息
        List<CarRentalPackageCarBatteryRelPo> carBatteryRelEntityList = null;
        List<BatteryModel> batteryModelEntityList = null;
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(rentalPackageEntity.getId());
            if (!CollectionUtils.isEmpty(carBatteryRelEntityList)) {
                List<String> batteryTypes = carBatteryRelEntityList.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).distinct().collect(Collectors.toList());
                batteryModelEntityList = batteryModelService.selectByBatteryTypes(tenantId, batteryTypes);
            }
        }

        // 获取滞纳金
        BigDecimal lateFeeAmount = carRenalPackageSlippageBizService.queryCarPackageUnpaidAmountByUid(tenantId, uid);

        UserMemberInfoVo memberInfoVo = buildUserMemberInfoVo(memberTermEntity, rentalPackageEntity, batteryModelEntityList, rentalPackageOrderEntity,
                depositPayEntity, carModelEntity, carEntity, franchiseeEntity, storeEntity, rentalPackageEntityFlag, lateFeeAmount);

        return memberInfoVo;
    }

    /**
     * 构建返回值信息
     * @param memberTermEntity 会员期限信息
     * @param rentalPackageEntity 套餐信息
     * @param batteryModelEntityList 电池型号集
     * @param rentalPackageOrderEntity 套餐购买订单
     * @param depositPayEntity 押金缴纳信息
     * @param carModelEntity 车辆型号信息
     * @param carEntity 车辆信息
     * @param franchiseeEntity 加盟商信息
     * @param storeEntity 门店信息
     * @return 会员信息
     */
    private UserMemberInfoVo buildUserMemberInfoVo(CarRentalPackageMemberTermPo memberTermEntity, CarRentalPackagePo rentalPackageEntity, List<BatteryModel> batteryModelEntityList,
                                                   CarRentalPackageOrderPo rentalPackageOrderEntity, CarRentalPackageDepositPayPo depositPayEntity, ElectricityCarModel carModelEntity,
                                                   ElectricityCar carEntity, Franchisee franchiseeEntity, Store storeEntity, boolean rentalPackageEntityFlag, BigDecimal lateFeeAmount) {

        UserMemberInfoVo userMemberInfoVo = new UserMemberInfoVo();
        userMemberInfoVo.setType(memberTermEntity.getRentalPackageType());
        userMemberInfoVo.setDueTime(memberTermEntity.getDueTime());
        userMemberInfoVo.setDueTimeTotal(memberTermEntity.getDueTimeTotal());
        userMemberInfoVo.setResidue(memberTermEntity.getResidue());
        userMemberInfoVo.setStatus(memberTermEntity.getStatus());
        userMemberInfoVo.setFranchiseeId(franchiseeEntity.getId().intValue());
        userMemberInfoVo.setFranchiseeName(franchiseeEntity.getName());
        userMemberInfoVo.setStoreId(storeEntity.getId().intValue());
        userMemberInfoVo.setStoreName(storeEntity.getName());
        userMemberInfoVo.setCarModelId(carModelEntity.getId());
        userMemberInfoVo.setCarModelName(carModelEntity.getName());
        userMemberInfoVo.setResidue(memberTermEntity.getResidue());
        userMemberInfoVo.setLateFeeAmount(lateFeeAmount);
        // 退租不退押，不显示套餐信息
        if (rentalPackageEntityFlag) {
            userMemberInfoVo.setRentalPackageId(rentalPackageEntity.getId());
            userMemberInfoVo.setRentalPackageName(rentalPackageEntity.getName());
        }
        // 更改状态
        if ((ObjectUtils.isNotEmpty(memberTermEntity.getDueTime()) && memberTermEntity.getDueTime() != 0L
                && memberTermEntity.getDueTime() <= System.currentTimeMillis()) || (ObjectUtils.isNotEmpty(memberTermEntity.getResidue()) && memberTermEntity.getResidue() <= 0L)) {
            userMemberInfoVo.setStatus(MemberTermStatusEnum.EXPIRE.getCode());
        }
        if (!CollectionUtils.isEmpty(batteryModelEntityList)) {
            List<String> batteryVShortList = batteryModelEntityList.stream().map(BatteryModel::getBatteryVShort).collect(Collectors.toList());
            userMemberInfoVo.setBatteryVShortList(batteryVShortList);
        }

        // 套餐购买信息
        if (ObjectUtils.isNotEmpty(rentalPackageOrderEntity)) {
            CarRentalPackageOrderVo carRentalPackageOrder = new CarRentalPackageOrderVo();
            BeanUtils.copyProperties(rentalPackageOrderEntity, carRentalPackageOrder);
            userMemberInfoVo.setCarRentalPackageOrder(carRentalPackageOrder);
            if (YesNoEnum.NO.getCode().equals(carRentalPackageOrder.getRentRebate()) || carRentalPackageOrder.getRentRebateEndTime() <= System.currentTimeMillis()) {
                userMemberInfoVo.setCarRentalPackageOrderRefundFlag(false);
            }

            // 购买的时候，赠送的优惠券是否被使用，若为使用中、已使用，则不允许退租
            UserCoupon userCoupon = userCouponService.selectBySourceOrderId(rentalPackageOrderEntity.getOrderNo());
            if (ObjectUtils.isNotEmpty(userCoupon)) {
                Integer status = userCoupon.getStatus();
                if (UserCoupon.STATUS_IS_BEING_VERIFICATION.equals(status) || UserCoupon.STATUS_USED.equals(status)) {
                    userMemberInfoVo.setCarRentalPackageOrderRefundFlag(false);
                }
            }
        } else {
            userMemberInfoVo.setCarRentalPackageOrderRefundFlag(false);
        }

        // 押金缴纳订单信息
        if (ObjectUtils.isNotEmpty(depositPayEntity)) {
            CarRentalPackageDepositPayVo carRentalPackageDepositPay = new CarRentalPackageDepositPayVo();
            BeanUtils.copyProperties(depositPayEntity, carRentalPackageDepositPay);
            userMemberInfoVo.setCarRentalPackageDepositPay(carRentalPackageDepositPay);
        }

        // 车辆信息
        if (ObjectUtils.isNotEmpty(carEntity)) {
            CarVo car = new CarVo();
            car.setCarSn(carEntity.getSn());
            car.setCarModelName(carEntity.getModel());
            userMemberInfoVo.setCar(car);
        }

        return userMemberInfoVo;

    }

    /**
     * 根据用户ID获取当前用户的绑定车辆型号ID<br />
     * 可能为null
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return 车辆型号ID
     */
    @Override
    public Integer queryCarModelByUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.info("CarRentalPackageMemberTermBizService.queryCarModelByUid return null, not found car_rental_package_member_term or status is pending effective. uid is {}", uid);
            return null;
        }

        // 退租未退押
        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        if (ObjectUtils.isEmpty(rentalPackageId) || rentalPackageId == 0L) {
            log.info("CarRentalPackageMemberTermBizService.queryCarModelByUid return null, User has retired from lease. uid is {}", uid);
            return null;
        }

        // 查询套餐设置信息
        CarRentalPackagePo rentalPackageEntity = carRentalPackageService.selectById(rentalPackageId);
        if (ObjectUtils.isEmpty(rentalPackageEntity)) {
            log.info("CarRentalPackageMemberTermBizService.queryCarModelByUid return null, not found car_rental_package. rentalPackageId is {}", rentalPackageId);
            return null;
        }

        return rentalPackageEntity.getCarModelId();
    }

    /**
     * 套餐购买订单过期处理<br />
     * 用于定时任务
     *
     * @param offset 偏移量
     * @param size 取值数量
     */
    @Override
    public void expirePackageOrder(Integer offset, Integer size) {
        // 初始化定义
        offset = ObjectUtils.isEmpty(offset) ? 0: offset;
        size = ObjectUtils.isEmpty(size) ? 500: size;

        boolean lookFlag = true;

        // 当前时间
        long nowTime = System.currentTimeMillis();

        while (lookFlag) {
            // 1. 查询会员套餐表中，套餐购买订单已过期的数据
            List<CarRentalPackageMemberTermPo> memberTermEntityList = carRentalPackageMemberTermService.pageExpire(offset, size, nowTime);
            if (CollectionUtils.isEmpty(memberTermEntityList)) {
                log.info("expirePackageOrder, The data is empty and does not need to be processed");
                lookFlag = false;
                break;
            }

            for (CarRentalPackageMemberTermPo memberTermEntity : memberTermEntityList) {
                CarLockCtrlHistory carLockCtrlHistory = null;
                try {
                    // 根据用户ID查询第一条未使用的支付成功的订单信息
                    CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectFirstUnUsedAndPaySuccessByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                    CarRentalPackageOrderSlippagePo slippageEntityInsert = null;
                    if (ObjectUtils.isEmpty(packageOrderEntity)) {
                        log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. user no available orders. uid is {}", memberTermEntity.getUid());
                        // JT808，套餐过期级锁
                        ElectricityCar electricityCar = carService.selectByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                        if (ObjectUtils.isNotEmpty(electricityCar)) {
                            UserInfo userInfo = userInfoService.queryByUidFromCache(memberTermEntity.getUid());
                            carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo, nowTime, CarLockCtrlHistory.TYPE_MEMBER_CARD_LOCK);
                        }

                        // 判定构建逾期订单
                        // TODO 为了测试，更改为10分钟，实际值 DAY_MILLISECOND
                        if (nowTime >= (memberTermEntity.getDueTime() + TimeConstant.TEN_MINUTE_MILLISECOND)) {
                            slippageEntityInsert = buildCarRentalPackageOrderSlippage(memberTermEntity.getUid(), memberTermEntity);
                            if (ObjectUtils.isEmpty(slippageEntityInsert)) {
                                log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. user no device. skip. uid is {}", memberTermEntity.getUid());
                                continue;
                            }
                        }
                    } else {
                        // 二次保底确认
                        CarRentalPackageMemberTermPo oriMemberTermEntity = carRentalPackageMemberTermService.selectById(memberTermEntity.getId());
                        if (ObjectUtils.isEmpty(oriMemberTermEntity)) {
                            log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. t_car_rental_package_member_term Abnormal old data. skip. id is {}", memberTermEntity.getId());
                            continue;
                        }
                        if (oriMemberTermEntity.getRentalPackageOrderNo().equals(packageOrderEntity.getOrderNo())) {
                            log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. t_car_rental_package_member_term processed. skip. id is {}", memberTermEntity.getId());
                            continue;
                        }
                    }

                    // 若生成滞纳金，则代表肯定设置了滞纳金，此时查看是否存在因冻结产生的滞纳金，若存在，则更新数据，并新增一条过期的逾期订单
                    CarRentalPackageOrderSlippagePo slippageFreezeEntity = null;
                    if (ObjectUtils.isNotEmpty(slippageEntityInsert)) {
                        CarRentalPackageOrderSlippagePo slippageExpireEntity = carRentalPackageOrderSlippageService.selectByPackageOrderNoAndType(slippageEntityInsert.getRentalPackageOrderNo(), SlippageTypeEnum.EXPIRE.getCode());
                        if (ObjectUtils.isNotEmpty(slippageExpireEntity)) {
                            log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. The user already has an expired order. skip. uid is {}, rentalPackageOrderNo is {}", memberTermEntity.getId(), slippageEntityInsert.getRentalPackageOrderNo());
                            continue;
                        }

                        slippageFreezeEntity = carRentalPackageOrderSlippageService.selectByPackageOrderNoAndType(slippageEntityInsert.getRentalPackageOrderNo(), SlippageTypeEnum.FREEZE.getCode());
                        if (ObjectUtils.isNotEmpty(slippageFreezeEntity)) {
                            // 取会员的当前到期时间，因为在冻结的时候，会更新当前套餐订单的到期时间
                            slippageFreezeEntity.setUpdateTime(System.currentTimeMillis());
                            slippageFreezeEntity.setLateFeeEndTime(memberTermEntity.getDueTime());
                            // 计算滞纳金金额
                            long diffDay = DateUtils.diffDay(slippageFreezeEntity.getLateFeeStartTime(), memberTermEntity.getDueTime());
                            slippageFreezeEntity.setLateFeePay(slippageFreezeEntity.getLateFee().multiply(new BigDecimal(diffDay)).setScale(2, RoundingMode.HALF_UP));
                        }
                        // JT808
                        ElectricityCar electricityCar = carService.selectByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                        if (ObjectUtils.isNotEmpty(electricityCar)) {
                            UserInfo userInfo = userInfoService.queryByUidFromCache(memberTermEntity.getUid());
                            carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo, nowTime, CarLockCtrlHistory.TYPE_SLIPPAGE_LOCK);
                        }
                    }
                    // 数据落库处理
                    saveExpirePackageOrderTx(slippageEntityInsert, packageOrderEntity, memberTermEntity, slippageFreezeEntity, memberTermEntity.getRentalPackageOrderNo(), carLockCtrlHistory);
                } catch (Exception e) {
                    log.error("CarRentalPackageMemberTermBizService.expirePackageOrder skip. error. ", e);
                    continue;
                }
            }
            offset += size;
        }
    }

    /**
     * 构建JT808
     * @param electricityCar
     * @param userInfo
     * @return
     */
    private CarLockCtrlHistory buildCarLockCtrlHistory(ElectricityCar electricityCar, UserInfo userInfo, Long nowTime, Integer type) {
        Integer tenantId = userInfo.getTenantId();
        if (ObjectUtils.isEmpty(electricityCar) && UserInfo.CAR_RENT_STATUS_YES.equals(userInfo.getCarRentStatus())) {
            electricityCar = carService.selectByUid(tenantId, userInfo.getUid());
        }

        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(tenantId);
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {

            boolean result = carRentalOrderBizService.retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_LOCK, 3);
            log.info("buildCarLockCtrlHistory, carRentalOrderBizService.retryCarLockCtrl result is {}", result);

            CarLockCtrlHistory carLockCtrlHistory = new CarLockCtrlHistory();
            carLockCtrlHistory.setUid(userInfo.getUid());
            carLockCtrlHistory.setName(userInfo.getName());
            carLockCtrlHistory.setPhone(userInfo.getPhone());
            carLockCtrlHistory
                    .setStatus(result ? CarLockCtrlHistory.STATUS_LOCK_SUCCESS : CarLockCtrlHistory.STATUS_LOCK_FAIL);
            carLockCtrlHistory.setCarModelId(electricityCar.getModelId().longValue());
            carLockCtrlHistory.setCarModel(electricityCar.getModel());
            carLockCtrlHistory.setCarId(electricityCar.getId().longValue());
            carLockCtrlHistory.setCarSn(electricityCar.getSn());
            carLockCtrlHistory.setCreateTime(nowTime);
            carLockCtrlHistory.setUpdateTime(nowTime);
            carLockCtrlHistory.setTenantId(tenantId);
            carLockCtrlHistory.setType(type);

            return carLockCtrlHistory;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveExpirePackageOrderTx(CarRentalPackageOrderSlippagePo slippageEntityInsert, CarRentalPackageOrderPo packageOrderEntityNew,
                                         CarRentalPackageMemberTermPo memberTermEntity, CarRentalPackageOrderSlippagePo slippageFreezeEntity,
                                         String oriRentalPackageOrderNo, CarLockCtrlHistory carLockCtrlHistory) {
        // 生成逾期订单
        if (ObjectUtils.isNotEmpty(slippageEntityInsert)) {
            carRentalPackageOrderSlippageService.insert(slippageEntityInsert);
        }
        if (ObjectUtils.isNotEmpty(slippageFreezeEntity)) {
            // 更新对应的因冻结的产生的逾期订单记录
            carRentalPackageOrderSlippageService.updateById(slippageFreezeEntity);
        }
        if (ObjectUtils.isNotEmpty(packageOrderEntityNew)) {
            // 覆盖会员期限信息
            CarRentalPackageMemberTermPo memberTermEntityUpdate = new CarRentalPackageMemberTermPo();
            memberTermEntityUpdate.setRentalPackageOrderNo(packageOrderEntityNew.getOrderNo());
            memberTermEntityUpdate.setRentalPackageId(packageOrderEntityNew.getRentalPackageId());
            memberTermEntityUpdate.setRentalPackageConfine(packageOrderEntityNew.getConfine());
            memberTermEntityUpdate.setId(memberTermEntity.getId());

            // 计算到期时间
            Integer tenancy = packageOrderEntityNew.getTenancy();
            Integer tenancyUnit = packageOrderEntityNew.getTenancyUnit();
            Long dueTime = ObjectUtils.isNotEmpty(memberTermEntity.getDueTime()) ? memberTermEntity.getDueTime() : System.currentTimeMillis();
            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
            }
            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * TimeConstant.MINUTE_MILLISECOND);
            }

            memberTermEntityUpdate.setDueTime(dueTime);

            // 计算余量
            if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntityNew.getConfine())) {
                if (memberTermEntity.getResidue() >= 0) {
                    memberTermEntityUpdate.setResidue(packageOrderEntityNew.getConfineNum());
                } else {
                    memberTermEntityUpdate.setResidue(packageOrderEntityNew.getConfineNum() + memberTermEntity.getResidue());
                }
            } else {
                memberTermEntityUpdate.setResidue(0L);
            }

            carRentalPackageMemberTermService.updateById(memberTermEntityUpdate);

            // 更改原订单状态及新订单状态
            carRentalPackageOrderService.updateUseStateByOrderNo(oriRentalPackageOrderNo, UseStateEnum.EXPIRED.getCode(), null);
            carRentalPackageOrderService.updateUseStateByOrderNo(packageOrderEntityNew.getOrderNo(), UseStateEnum.IN_USE.getCode(), null);

            // 车电一体，同步电池那边的数据
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(packageOrderEntityNew.getRentalPackageType())) {
                // 同步押金
                List<CarRentalPackageCarBatteryRelPo> carBatteryRelPos = carRentalPackageCarBatteryRelService.selectByRentalPackageId(packageOrderEntityNew.getRentalPackageId());
                if (!CollectionUtils.isEmpty(carBatteryRelPos)) {
                    List<String> batteryTypes = carBatteryRelPos.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).collect(Collectors.toList());
                    log.info("saveExpirePackageOrderTx, userBatteryTypeService.synchronizedUserBatteryType, batteryTypes is {}", JsonUtil.toJson(batteryTypes));
                    userBatteryTypeService.synchronizedUserBatteryType(packageOrderEntityNew.getUid(), packageOrderEntityNew.getTenantId(), batteryTypes);
                }
            }
        }

        if (ObjectUtils.isNotEmpty(carLockCtrlHistory)) {
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
        }
    }


    private CarRentalPackageOrderSlippagePo buildCarRentalPackageOrderSlippage(Long uid, CarRentalPackageMemberTermPo memberTermEntity) {
        // 查询当时购买的订单信息
        CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(memberTermEntity.getRentalPackageOrderNo());
        if (ObjectUtils.isEmpty(packageOrderEntity)) {
            log.info("CarRentalPackageMemberTermBizService.buildCarRentalPackageOrderSlippage failed. not found car_rental_package_order. orderNo is {}", memberTermEntity.getRentalPackageOrderNo());
        }

        // 免除滞纳金
        if (ObjectUtils.isEmpty(packageOrderEntity.getLateFee()) || BigDecimal.ZERO.compareTo(packageOrderEntity.getLateFee()) >= 0) {
            return null;
        }

        // 初始化标识
        boolean createFlag = false;

        // 查询是否未归还设备
        // 1. 车辆
        ElectricityCar electricityCar = carService.selectByUid(memberTermEntity.getTenantId(), uid);
        if (ObjectUtils.isNotEmpty(electricityCar) && ObjectUtils.isNotEmpty(electricityCar.getSn()) ) {
            createFlag = true;
        }

        // 2. 根据套餐类型，是否查询电池
        ElectricityBattery battery = null;
        Long batteryModelId = null;
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType())) {
            battery = batteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                BatteryModel batteryModel = batteryModelService.selectByBatteryType(packageOrderEntity.getTenantId(), battery.getModel());
                if (ObjectUtils.isNotEmpty(batteryModel)) {
                    batteryModelId = batteryModel.getId();
                }
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
        slippageEntity.setType(SlippageTypeEnum.EXPIRE.getCode());
        slippageEntity.setLateFee(packageOrderEntity.getLateFee());
        slippageEntity.setLateFeeStartTime(System.currentTimeMillis());
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
            slippageEntity.setBatteryModelId(batteryModelId);
        }

        return slippageEntity;
    }
}
