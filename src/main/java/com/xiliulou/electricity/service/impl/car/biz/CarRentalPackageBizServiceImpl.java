package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageCarBatteryRelService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageBizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 租赁套餐相关的业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageBizServiceImpl implements CarRentalPackageBizService {

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private  CouponActivityPackageService couponActivityPackageService;

    @Resource
    private CarRenalPackageDepositBizService carRenalPackageDepositBizService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private BatteryMemberCardService batteryMemberCardService;

    @Resource
    private CarRentalPackageCarBatteryRelService carRentalPackageCarBatteryRelService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private UserBizService userBizService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Resource
    private CouponService couponService;

    @Resource
    private UserCouponService userCouponService;

    /**
     * 获取用户可以购买的套餐
     *
     * @param qryReq 查询模型
     * @param uid    用户ID
     * @return
     */
    @Override
    public List<CarRentalPackagePo> queryCanPurchasePackage(CarRentalPackageQryReq qryReq, Long uid) {
        if (!ObjectUtils.allNotNull(qryReq, qryReq.getFranchiseeId(), qryReq.getStoreId(), qryReq.getCarModelId(), uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = qryReq.getTenantId();
        Integer franchiseeId = qryReq.getFranchiseeId();
        Integer storeId = qryReq.getStoreId();
        Integer carModelId = qryReq.getCarModelId();
        Integer rentalPackageType = qryReq.getRentalPackageType();


        Boolean oldUserFlag = false;
        BigDecimal deposit = null;
        List<String> batteryModelTypeList = new ArrayList<>();
        Integer confine = null;

        // 0. 获取用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("queryCanPurchasePackage failed. Not found user. uid is {} ", uid);
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        // 0.1 用户可用状态
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("queryCanPurchasePackage failed. User is unUsable. uid is {} ", uid);
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");
        }

        // 0.2 用户实名认证状态
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("queryCanPurchasePackage failed. User not auth. uid is {}", uid);
            throw new BizException("ELECTRICITY.0041", "用户尚未实名认证");
        }

        if (ObjectUtils.isNotEmpty(userInfo.getFranchiseeId()) && userInfo.getFranchiseeId() != 0L && !franchiseeId.equals(userInfo.getFranchiseeId().intValue())) {
            log.error("queryCanPurchasePackage failed. userInfo's franchiseeId is {}. params franchiseeId is {}", userInfo.getFranchiseeId(), qryReq.getFranchiseeId());
            throw new BizException("300036", "所属机构不匹配");
        }
        if (ObjectUtils.isNotEmpty(userInfo.getStoreId()) && userInfo.getStoreId() != 0L && !storeId.equals(userInfo.getStoreId().intValue())) {
            log.error("queryCanPurchasePackage failed. userInfo's storeId is {}. params storeId is {}", userInfo.getStoreId(), qryReq.getStoreId());
            throw new BizException("300036", "所属机构不匹配");
        }


        // 1、查询是否存在会员期限信息(代表是否存在过套餐购买)
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        // 存在数据，则代表押金已经缴纳，即租户、加盟商、门店、套餐类型、押金全都定下来了
        if (ObjectUtils.isNotEmpty(memberTermEntity)) {
            // 待生效，代表未支付
            if (!MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
                // 所属机构不匹配
                if (!memberTermEntity.getFranchiseeId().equals(franchiseeId) || !memberTermEntity.getStoreId().equals(storeId)) {
                    log.info("CarRentalPackageBizService.queryCanPurchasePackage, The user's organization does not match. return empty list.");
                    return Collections.emptyList();
                }

                Long rentalPackageId = memberTermEntity.getRentalPackageId();

                // 必定是老用户
                if (ObjectUtils.isNotEmpty(rentalPackageId)) {
                    // 查询套餐对应的车辆型号
                    CarRentalPackagePo packageEntity = carRentalPackageService.selectById(rentalPackageId);

                    // 车辆型号不匹配
                    if (!packageEntity.getCarModelId().equals(carModelId)) {
                        log.info("CarRentalPackageBizService.queryCanPurchasePackage, The user's carModel does not match. return empty list.");
                        return Collections.emptyList();
                    }
                    oldUserFlag = true;
                } else {
                    oldUserFlag = userBizService.isOldUser(tenantId, uid);
                    rentalPackageId = carRenalPackageDepositBizService.queryRentalPackageIdByDepositPayOrderNo(memberTermEntity.getDepositPayOrderNo());
                }

                // 车电一体且存在订单
                if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType()) && ObjectUtils.isNotEmpty(rentalPackageId)) {
                    // 查询电池型号信息
                    List<CarRentalPackageCarBatteryRelPo> carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(rentalPackageId);
                    batteryModelTypeList = carBatteryRelEntityList.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).distinct().collect(Collectors.toList());
                }

                deposit = memberTermEntity.getDeposit();
                rentalPackageType = memberTermEntity.getRentalPackageType();
                confine = memberTermEntity.getRentalPackageConfine();
            }
        } else {
            oldUserFlag = userBizService.isOldUser(tenantId, uid);

            // 是否缴纳过租车的押金（单车、车电一体）
            if (UserInfo.CAR_DEPOSIT_STATUS_YES.equals(userInfo.getCarDepositStatus()) || YesNoEnum.YES.getCode().equals(userInfo.getCarBatteryDepositStatus())) {
                // 查询保险缴纳信息
                CarRentalPackageDepositPayPo depositPayPo = carRentalPackageDepositPayService.selectLastPaySucessByUid(tenantId, uid);
                confine = depositPayPo.getRentalPackageType();
            }
        }

        // 结合如上两点，从数据库中筛选合适的套餐
        CarRentalPackageQryModel qryModel = new CarRentalPackageQryModel();
        qryModel.setOffset(qryReq.getOffset());
        qryModel.setSize(qryReq.getSize());
        qryModel.setTenantId(tenantId);
        qryModel.setFranchiseeId(franchiseeId);
        qryModel.setStoreId(storeId);
        qryModel.setApplicableTypeList(oldUserFlag ? ApplicableTypeEnum.oldUserApplicable() : ApplicableTypeEnum.newUserApplicable());
        qryModel.setDeposit(deposit);
        qryModel.setType(rentalPackageType);
        qryModel.setCarModelId(carModelId);
        qryModel.setStatus(UpDownEnum.UP.getCode());
        qryModel.setConfine(confine);
        List<CarRentalPackagePo> packageEntityList = carRentalPackageService.page(qryModel);
        if (CollectionUtils.isEmpty(packageEntityList)) {
            return Collections.emptyList();
        }

        // 车电一体，需要二次处理
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            // 查询型号关联关系
            List<Long> packageIdList = packageEntityList.stream().map(CarRentalPackagePo::getId).collect(Collectors.toList());
            List<CarRentalPackageCarBatteryRelPo> carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageIds(packageIdList);
            if (CollectionUtils.isEmpty(carBatteryRelEntityList)) {
                return packageEntityList;
            }

            Map<Long, List<CarRentalPackageCarBatteryRelPo>> carBatteryRelMap = carBatteryRelEntityList.stream().collect(Collectors.groupingBy(CarRentalPackageCarBatteryRelPo::getRentalPackageId));

            // TODO 临时处理
            List<String> batteryModelTypeSimpleList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(batteryModelTypeList)) {
                batteryModelTypeSimpleList = batteryModelTypeList.stream().map(n -> {
                    StringJoiner simpleModel = new StringJoiner("_");
                    String[] strings = n.split("_");
                    simpleModel.add(strings[0]).add(strings[1]).add(strings[strings.length - 1]);
                    return simpleModel.toString();
                }).collect(Collectors.toList());
            }

            // 迭代器处理
            Iterator<CarRentalPackagePo> iterator = packageEntityList.iterator();
            while (iterator.hasNext()) {
                CarRentalPackagePo carRentalPackage = iterator.next();
                List<CarRentalPackageCarBatteryRelPo> carBatteryRels = carBatteryRelMap.get(carRentalPackage.getId());
                if (CollectionUtils.isEmpty(carBatteryRels)) {
                    continue;
                }
                List<String> batteryModelTypeDbList = carBatteryRels.stream().map(CarRentalPackageCarBatteryRelPo::getBatteryModelType).distinct().collect(Collectors.toList());
                // TODO 临时处理
                List<String> batteryModelTypeDbSimpleList = batteryModelTypeDbList.stream().map(n -> {
                    StringJoiner simpleModel = new StringJoiner("_");
                    String[] strings = n.split("_");
                    simpleModel.add(strings[0]).add(strings[1]).add(strings[strings.length - 1]);
                    return simpleModel.toString();
                }).collect(Collectors.toList());

                if (!batteryModelTypeDbSimpleList.containsAll(batteryModelTypeSimpleList)) {
                    iterator.remove();
                }
            }
        }

        return packageEntityList;
    }

    /**
     * 根据套餐ID删除套餐信息
     * @param packageId 套餐ID
     * @param optId 操作人ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delPackageById(Long packageId, Long optId) {
        if (ObjectUtils.isEmpty(packageId)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        carRentalPackageService.delById(packageId, optId);
        carRentalPackageCarBatteryRelService.delByRentalPackageId(packageId, optId);
        return false;
    }

    /**
     * 新增套餐
     *
     * @param optModel
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertPackage(CarRentalPackageOptModel optModel) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getCreateUid(), optModel.getTenantId(), optModel.getName())
                || !BasicEnum.isExist(optModel.getType(), RentalPackageTypeEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = optModel.getTenantId();
        String name = optModel.getName();

        // 检测唯一
        if (carRentalPackageService.uqByTenantIdAndName(tenantId, name)) {
            log.info("CarRentalPackageBizService.insertPackage, Package name already exists.");
            throw new BizException("300022", "套餐名称已存在");
        }

        // 新增租车套餐
        CarRentalPackagePo entity = new CarRentalPackagePo();
        BeanUtils.copyProperties(optModel, entity);
        Long packageId = carRentalPackageService.insert(entity);

        // 车电一体
        if (RentalPackageTypeEnum.CAR.getCode().equals(optModel.getType())) {
            return true;
        }

        // 车电一体
        List<String> batteryModelTypes = CollectionUtils.isEmpty(optModel.getBatteryModelTypes()) ? new ArrayList<>() : optModel.getBatteryModelTypes();
        if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(optModel.getType())) {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(Long.valueOf(optModel.getFranchiseeId()));
            if (Franchisee.NEW_MODEL_TYPE.equals(franchisee.getModelType()) && CollectionUtils.isEmpty(batteryModelTypes)) {
                log.error("CarRentalPackageBizService.insertPackage failed. BatteryModelTypes is empty.");
                throw new BizException("ELECTRICITY.0007", "不合法的参数");
            }
        }

        // 1. 保存关联表
        List<CarRentalPackageCarBatteryRelPo> carBatteryRelEntityList = batteryModelTypes.stream().map(batteryModelType -> {
            CarRentalPackageCarBatteryRelPo carBatteryRelEntity = new CarRentalPackageCarBatteryRelPo();
            carBatteryRelEntity.setRentalPackageId(packageId);
            carBatteryRelEntity.setCarModelId(entity.getCarModelId());
            carBatteryRelEntity.setBatteryModelType(batteryModelType);
            carBatteryRelEntity.setBatteryVoltage(optModel.getBatteryVoltage());
            carBatteryRelEntity.setTenantId(entity.getTenantId());
            carBatteryRelEntity.setFranchiseeId(entity.getFranchiseeId());
            carBatteryRelEntity.setStoreId(entity.getStoreId());
            carBatteryRelEntity.setCreateUid(entity.getCreateUid());
            carBatteryRelEntity.setUpdateUid(entity.getUpdateUid());
            carBatteryRelEntity.setCreateTime(entity.getCreateTime());
            carBatteryRelEntity.setUpdateTime(entity.getUpdateTime());

            return carBatteryRelEntity;

        }).collect(Collectors.toList());
        carRentalPackageCarBatteryRelService.batchInsert(carBatteryRelEntityList);

        // 2. 调用租电套餐设置接口
        BatteryMemberCard batteryMemberCard = buildBatteryMemberCardEntity(entity);
        batteryMemberCardService.insertBatteryMemberCardAndBatteryType(batteryMemberCard, batteryModelTypes);

        return true;
    }

    private BatteryMemberCard buildBatteryMemberCardEntity(CarRentalPackagePo entity) {
        BatteryMemberCard batteryMemberCardEntity = new BatteryMemberCard();
        batteryMemberCardEntity.setName(entity.getName());
        batteryMemberCardEntity.setDeposit(entity.getDeposit());
        batteryMemberCardEntity.setRentPrice(entity.getRent());
        batteryMemberCardEntity.setRentPriceUnit(entity.getRentUnitPrice());
        batteryMemberCardEntity.setValidDays(entity.getTenancy());
        batteryMemberCardEntity.setRentUnit(entity.getTenancyUnit());
        batteryMemberCardEntity.setFranchiseeId(Long.valueOf(entity.getFranchiseeId()));
        batteryMemberCardEntity.setRentType(entity.getApplicableType());
        batteryMemberCardEntity.setSendCoupon(entity.getGiveCoupon());
        batteryMemberCardEntity.setStatus(entity.getStatus());
        batteryMemberCardEntity.setLimitCount(entity.getConfine());
        batteryMemberCardEntity.setUseCount(entity.getConfineNum());
        if (ObjectUtils.isNotEmpty(entity.getCouponId())) {
            batteryMemberCardEntity.setCouponId(Integer.valueOf(entity.getCouponId().intValue()));
        }
        batteryMemberCardEntity.setIsRefund(entity.getRentRebate());
        batteryMemberCardEntity.setRefundLimit(entity.getRentRebateTerm());
        batteryMemberCardEntity.setFreeDeposite(entity.getFreeDeposit());
        batteryMemberCardEntity.setServiceCharge(entity.getLateFee());
        batteryMemberCardEntity.setRemark(entity.getRemark());
        batteryMemberCardEntity.setBusinessType(BatteryMemberCard.BUSINESS_TYPE_BATTERY_CAR);
        batteryMemberCardEntity.setDelFlag(DelFlagEnum.OK.getCode());
        batteryMemberCardEntity.setTenantId(entity.getTenantId());
        batteryMemberCardEntity.setCreateTime(entity.getCreateTime());
        batteryMemberCardEntity.setUpdateTime(entity.getUpdateTime());

        return batteryMemberCardEntity;
    }

    /**
     * 计算需要支付的金额<br />
     * 目前优惠券只有一种减免金额卷
     * @param amount    原金额
     * @param userCouponIds 用户的优惠券ID集合
     * @param uid       用户ID
     * @param packageId       套餐ID
     * @param packageType       套餐类型：1-租电、2-租车、3-车电一体 @see com.xiliulou.electricity.enums.PackageTypeEnum
     * @return Triple<BigDecimal, List<Long>, Boolean> 实际支付金额、已用的用户优惠券ID、Boolean（暂无实际意义）
     */
    @Override
    public Triple<BigDecimal, List<Long>, Boolean> calculatePaymentAmount(BigDecimal amount, List<Long> userCouponIds, Long uid, Long packageId, Integer packageType) {
        log.info("calculatePaymentAmount amount is {}", amount);
        if (BigDecimal.ZERO.compareTo(amount) == 0) {
            return Triple.of(BigDecimal.ZERO, userCouponIds, true) ;
        }

        if (amount.compareTo(BigDecimal.ZERO) > 0 && CollectionUtils.isEmpty(userCouponIds)) {
            return Triple.of(amount, null, true) ;
        }

        // 查询用户名下是否存在未使用、未过期的优惠券
        List<UserCoupon> userCoupons = userCouponService.selectEffectiveByUid(uid, userCouponIds, System.currentTimeMillis());
        if (CollectionUtils.isEmpty(userCoupons)) {
            return Triple.of(amount, null, true) ;
        }

        List<Integer> couponIdList = userCoupons.stream().map(UserCoupon::getCouponId).distinct().collect(Collectors.toList());
        List<Long> couponIds = couponIdList.stream().map(s -> Long.valueOf(s)).collect(Collectors.toList());

        // 查询优惠券信息
        CouponQuery couponQuery = CouponQuery.builder().ids(couponIds).build();
        R couponResult = couponService.queryList(couponQuery);
        if (!couponResult.isSuccess()) {
            throw new BizException(couponResult.getErrMsg());
        }

        List<Coupon> couponList = (List<Coupon>) couponResult.getData();

        // 按照优惠券是否可叠加分组
        Map<Integer, List<Coupon>> superpositionMap = couponList.stream().collect(Collectors.groupingBy(Coupon::getSuperposition));
        if (superpositionMap.size() == 2 || (superpositionMap.size() == 1 && superpositionMap.containsKey(Coupon.SUPERPOSITION_NO) && superpositionMap.get(Coupon.SUPERPOSITION_NO).size() > 1)) {
            throw new BizException("300034", "使用优惠券有误");
        }

        // 校验优惠券的使用，是否指定这个套餐
        // 1-租电 2-租车 3-车电一体
        Boolean valid = couponActivityPackageService.checkPackageIsValid(couponList, packageId, packageType);
        if (!valid) {
            throw new BizException("300034", "使用优惠券有误");
        }

        // 真正使用的用户优惠券ID
        List<Long> userCouponIdList = userCoupons.stream().map(UserCoupon::getId).distinct().collect(Collectors.toList());

        // 计算总共减免金额
        BigDecimal discountAmount = couponList.stream().map(coupon -> ObjectUtils.isEmpty(coupon.getAmount()) ? new BigDecimal(0) : coupon.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (discountAmount.compareTo(amount) >= 0) {
            return Triple.of(BigDecimal.ZERO, userCouponIdList, true) ;
        }
        log.info("calculatePaymentAmount discountAmount is {}", discountAmount);

        // 实际支付金额
        BigDecimal payAmount = amount.subtract(discountAmount);
        log.info("calculatePaymentAmount payAmount is {}", payAmount);

        return Triple.of(payAmount, userCouponIdList, true) ;
    }
}
