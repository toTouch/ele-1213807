package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.car.CarRentalPackageCarBatteryRelPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.ApplicableTypeEnum;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.query.CouponQuery;
import com.xiliulou.electricity.query.car.CarRentalPackageQryReq;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.car.CarRentalPackageCarBatteryRelService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    public List<CarRentalPackagePO> queryCanPurchasePackage(CarRentalPackageQryReq qryReq, Long uid) {
        if (!ObjectUtils.allNotNull(qryReq, qryReq.getFranchiseeId(), qryReq.getStoreId(), qryReq.getCarModelId(), uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = qryReq.getTenantId();
        Integer franchiseeId = qryReq.getFranchiseeId();
        Integer storeId = qryReq.getStoreId();
        Integer carModelId = qryReq.getCarModelId();

        Boolean oldUserFlag = false;
        BigDecimal deposit = null;
        Integer rentalPackageType = null;
        List<String> batteryModelTypeList = null;


        // 1、查询是否存在会员期限信息(代表是否存在过套餐购买)
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        // 存在数据，则代表押金已经缴纳，即租户、加盟商、门店、套餐类型、押金全都定下来了
        if (ObjectUtils.isNotEmpty(memberTermEntity)) {
            // 所属机构不匹配
            if (!memberTermEntity.getFranchiseeId().equals(franchiseeId) || !memberTermEntity.getStoreId().equals(storeId)) {
                log.info("CarRentalPackageBizService.queryCanPurchasePackage, The user's organization does not match. return empty list.");
                return Collections.emptyList();
            }

            Long rentalPackageId = memberTermEntity.getRentalPackageId();

            // 必定是老用户
            if (ObjectUtils.isNotEmpty(rentalPackageId)) {
                // 查询套餐对应的车辆型号
                CarRentalPackagePO packageEntity = carRentalPackageService.selectById(rentalPackageId);

                // 车辆型号不匹配
                if (!packageEntity.getCarModelId().equals(carModelId)) {
                    log.info("CarRentalPackageBizService.queryCanPurchasePackage, The user's carModel does not match. return empty list.");
                    return Collections.emptyList();
                }
                oldUserFlag = true;
            } else {
                oldUserFlag = userBizService.isOldUser(tenantId, uid);
            }

            // 车电一体且存在订单
            if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType()) && ObjectUtils.isNotEmpty(rentalPackageId)) {
                // 查询电池型号信息
                List<CarRentalPackageCarBatteryRelPO> carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageId(rentalPackageId);
                batteryModelTypeList = carBatteryRelEntityList.stream().map(CarRentalPackageCarBatteryRelPO::getBatteryModelType).distinct().collect(Collectors.toList());
            }

            deposit = memberTermEntity.getDeposit();
            rentalPackageType = memberTermEntity.getRentalPackageType();

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
        List<CarRentalPackagePO> packageEntityList = carRentalPackageService.page(qryModel);
        if (CollectionUtils.isEmpty(packageEntityList)) {
            return Collections.emptyList();
        }

        // 车电一体，需要二次处理
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            // 查询型号关联关系
            List<Long> packageIdList = packageEntityList.stream().map(CarRentalPackagePO::getId).collect(Collectors.toList());
            List<CarRentalPackageCarBatteryRelPO> carBatteryRelEntityList = carRentalPackageCarBatteryRelService.selectByRentalPackageIds(packageIdList);
            Map<Long, List<CarRentalPackageCarBatteryRelPO>> carBatteryRelMap = carBatteryRelEntityList.stream().collect(Collectors.groupingBy(CarRentalPackageCarBatteryRelPO::getRentalPackageId));

            List<String> batteryModelTypeDbList = null;

            // 迭代器处理
            Iterator<CarRentalPackagePO> iterator = packageEntityList.iterator();
            while (iterator.hasNext()) {
                CarRentalPackagePO carRentalPackage = iterator.next();
                batteryModelTypeDbList = carBatteryRelMap.get(carRentalPackage.getId()).stream().map(CarRentalPackageCarBatteryRelPO::getBatteryModelType).distinct().collect(Collectors.toList());
                if (!batteryModelTypeDbList.containsAll(batteryModelTypeList)) {
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
                || !BasicEnum.isExist(optModel.getType(), CarRentalPackageTypeEnum.class)) {
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
        CarRentalPackagePO entity = new CarRentalPackagePO();
        BeanUtils.copyProperties(optModel, entity);
        Long packageId = carRentalPackageService.insert(entity);

        // 车电一体
        if (CarRentalPackageTypeEnum.CAR.getCode().equals(optModel.getType())) {
            return true;
        }

        // 车电一体
        List<String> batteryModelTypes = optModel.getBatteryModelTypes();
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(optModel.getType())) {
            if (CollectionUtils.isEmpty(batteryModelTypes)) {
                log.error("CarRentalPackageBizService.insertPackage failed. BatteryModelTypes is empty.");
                throw new BizException("ELECTRICITY.0007", "不合法的参数");
            }
        }

        // 1. 保存关联表
        List<CarRentalPackageCarBatteryRelPO> carBatteryRelEntityList = batteryModelTypes.stream().map(batteryModelType -> {
            CarRentalPackageCarBatteryRelPO carBatteryRelEntity = new CarRentalPackageCarBatteryRelPO();
            carBatteryRelEntity.setRentalPackageId(packageId);
            carBatteryRelEntity.setCarModelId(entity.getCarModelId());
            carBatteryRelEntity.setBatteryModelType(batteryModelType);
            carBatteryRelEntity.setBatteryV(optModel.getBatteryV());
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

    private BatteryMemberCard buildBatteryMemberCardEntity(CarRentalPackagePO entity) {
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
     * @return Triple<BigDecimal, List<Long>, Boolean> 实际支付金额、已用的用户优惠券ID、Boolean（暂无实际意义）
     */
    @Override
    public Triple<BigDecimal, List<Long>, Boolean> calculatePaymentAmount(BigDecimal amount, List<Long> userCouponIds, Long uid) {
        if (BigDecimal.ZERO.compareTo(amount) == 0 || CollectionUtils.isEmpty(userCouponIds)) {
            return Triple.of(BigDecimal.ZERO, null, true) ;
        }

        // 查询用户名下是否存在未使用、未过期的优惠券
        List<UserCoupon> userCoupons = userCouponService.selectEffectiveByUid(uid, userCouponIds, System.currentTimeMillis());
        if (CollectionUtils.isEmpty(userCoupons)) {
            return Triple.of(BigDecimal.ZERO, null, true) ;
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
            throw new BizException("使用优惠券有误");
        }

        // TODO 暴煜, 校验优惠券的使用，是否指定这个套餐

        // 真正使用的用户优惠券ID
        List<Long> userCouponIdList = userCoupons.stream().map(UserCoupon::getId).distinct().collect(Collectors.toList());

        // 计算总共减免金额
        BigDecimal discountAmount = couponList.stream().map(coupon -> coupon.getAmount()).reduce(BigDecimal::add).get();
        if (discountAmount.compareTo(amount) >= 0) {
            return Triple.of(BigDecimal.ZERO, userCouponIdList, true) ;
        }

        // 实际支付金额
        BigDecimal payAmount = amount.subtract(discountAmount);

        return Triple.of(payAmount, userCouponIdList, true) ;
    }
}
