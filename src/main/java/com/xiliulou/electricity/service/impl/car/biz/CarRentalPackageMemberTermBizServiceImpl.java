package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.userinfo.UserMemberInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermBizServiceImpl implements CarRentalPackageMemberTermBizService {

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Resource
    private CarRentalPackageOrderSlippageService carRentalPackageOrderSlippageService;

    @Resource
    private ElectricityBatteryService batteryService;

    @Resource
    private UserCarService userCarService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    /**
     * 根据用户ID获取会员的全量信息（套餐订单信息、保险信息、车辆信息、电池信息）
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
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.info("queryUserMemberInfo, t_car_rental_package_member_term is null or status is wrong. uid is {}", uid);
            return null;
        }



        return null;
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
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.info("CarRentalPackageMemberTermBizService.queryCarModelByUid return null, not found car_rental_package_member_term or status is pending effective. uid is {}", uid);
            return null;
        }

        // 退租未退押
        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        if (ObjectUtils.isEmpty(rentalPackageId) || rentalPackageId.longValue() == 0) {
            log.info("CarRentalPackageMemberTermBizService.queryCarModelByUid return null, User has retired from lease. uid is {}", uid);
            return null;
        }

        // 查询套餐设置信息
        CarRentalPackagePO rentalPackageEntity = carRentalPackageService.selectById(rentalPackageId);
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
            List<CarRentalPackageMemberTermPO> memberTermEntityList = carRentalPackageMemberTermService.pageExpire(offset, size, nowTime);
            if (CollectionUtils.isEmpty(memberTermEntityList)) {
                log.info("expirePackageOrder, The data is empty and does not need to be processed");
                lookFlag = false;
                break;
            }

            for (CarRentalPackageMemberTermPO memberTermEntity : memberTermEntityList) {
                try {
                    // 根据UID查询名下的未使用的订单第一条订单
                    CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectFirstUnUsedByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                    CarRentalPackageOrderSlippagePO slippageEntityInsert = null;
                    if (ObjectUtils.isEmpty(packageOrderEntity)) {
                        log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. user no available orders. uid is {}", memberTermEntity.getUid());
                        // 判定构建逾期订单
                        if (nowTime <= (memberTermEntity.getDueTime().longValue() + TimeConstant.DAY_MILLISECOND)) {
                            slippageEntityInsert = buildCarRentalPackageOrderSlippage(memberTermEntity.getUid(), memberTermEntity);
                            if (ObjectUtils.isEmpty(slippageEntityInsert)) {
                                log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. user no device. skip. uid is {}", memberTermEntity.getUid());
                                continue;
                            }
                        }
                    } else {
                        // 二次保底确认
                        CarRentalPackageMemberTermPO oriMemberTermEntity = carRentalPackageMemberTermService.selectById(memberTermEntity.getRentalPackageId());
                        if (oriMemberTermEntity.getRentalPackageOrderNo().equals(packageOrderEntity.getOrderNo())) {
                            log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. t_car_rental_package_member_term processed. skip. uid is {}", memberTermEntity.getUid());
                            continue;
                        }
                    }

                    // 若生成滞纳金，则代表肯定设置了滞纳金，此时查看是否存在因冻结产生的滞纳金，若存在，则更新数据，并新增一条过期的逾期订单
                    CarRentalPackageOrderSlippagePO slippageFreezeEntity = null;
                    if (ObjectUtils.isNotEmpty(slippageEntityInsert)) {
                        slippageFreezeEntity = carRentalPackageOrderSlippageService.selectByPackageOrderNoAndType(slippageEntityInsert.getRentalPackageOrderNo(), SlippageTypeEnum.FREEZE.getCode());
                        if (ObjectUtils.isNotEmpty(slippageFreezeEntity)) {
                            // 取会员的当前到期时间，因为在冻结的时候，会更新当前套餐订单的到期时间
                            slippageFreezeEntity.setUpdateTime(System.currentTimeMillis());
                            slippageFreezeEntity.setLateFeeEndTime(memberTermEntity.getDueTime());
                            // 计算滞纳金金额
                            long diffDay = DateUtils.diffDay(memberTermEntity.getDueTime(), slippageFreezeEntity.getLateFeeStartTime());
                            slippageFreezeEntity.setLateFeePay(slippageFreezeEntity.getLateFee().multiply(new BigDecimal(diffDay)).setScale(2, RoundingMode.HALF_UP));
                        }
                    }
                    // 数据落库处理
                    saveExpirePackageOrderTx(slippageEntityInsert, packageOrderEntity, memberTermEntity, slippageFreezeEntity, memberTermEntity.getRentalPackageOrderNo());
                } catch (Exception e) {
                    log.info("CarRentalPackageMemberTermBizService.expirePackageOrder skip. error. ", e);
                    continue;
                }
            }
            offset += size;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveExpirePackageOrderTx(CarRentalPackageOrderSlippagePO slippageEntityInsert, CarRentalPackageOrderPO packageOrderEntityNew,
                                         CarRentalPackageMemberTermPO memberTermEntity, CarRentalPackageOrderSlippagePO slippageFreezeEntity, String oriRentalPackageOrderNo) {
        // 生成逾期订单
        if (ObjectUtils.isNotEmpty(slippageEntityInsert)) {
            carRentalPackageOrderSlippageService.insert(slippageEntityInsert);
            // 更新对应的因冻结的产生的逾期订单记录
            carRentalPackageOrderSlippageService.updateById(slippageFreezeEntity);
        }
        if (ObjectUtils.isNotEmpty(packageOrderEntityNew)) {
            // 覆盖会员期限信息
            CarRentalPackageMemberTermPO memberTermEntityUpdate = new CarRentalPackageMemberTermPO();
            memberTermEntityUpdate.setRentalPackageOrderNo(packageOrderEntityNew.getOrderNo());
            memberTermEntityUpdate.setRentalPackageId(packageOrderEntityNew.getRentalPackageId());
            memberTermEntityUpdate.setRentalPackageConfine(packageOrderEntityNew.getConfine());
            memberTermEntityUpdate.setId(memberTermEntity.getId());

            // 计算到期时间
            Integer tenancy = packageOrderEntityNew.getTenancy();
            Integer tenancyUnit = packageOrderEntityNew.getTenancyUnit();
            Long dueTime = null;
            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                dueTime = (tenancy * TimeConstant.DAY_MILLISECOND);
            }
            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                dueTime = Long.valueOf(tenancy * TimeConstant.MINUTE_MILLISECOND);
            }

            memberTermEntityUpdate.setDueTime(dueTime);

            // 计算余量
            if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderEntityNew.getConfine())) {
                memberTermEntityUpdate.setResidue(packageOrderEntityNew.getConfineNum() - memberTermEntity.getResidue());
            } else {
                memberTermEntityUpdate.setResidue(0L);
            }

            carRentalPackageMemberTermService.updateById(memberTermEntityUpdate);

            // 更改原订单状态及新订单状态
            carRentalPackageOrderService.updateUseStateByOrderNo(oriRentalPackageOrderNo, UseStateEnum.EXPIRED.getCode(), null);
            carRentalPackageOrderService.updateUseStateByOrderNo(packageOrderEntityNew.getOrderNo(), UseStateEnum.IN_USE.getCode(), null);
        }
    }


    private CarRentalPackageOrderSlippagePO buildCarRentalPackageOrderSlippage(Long uid, CarRentalPackageMemberTermPO memberTermEntity) {
        // 查询当时购买的订单信息
        CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(memberTermEntity.getRentalPackageOrderNo());
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
        UserCar userCar = userCarService.selectByUidFromCache(uid);
        if (ObjectUtils.isNotEmpty(userCar) && ObjectUtils.isNotEmpty(userCar.getSn()) ) {
            createFlag = true;
        }

        // 2. 根据套餐类型，是否查询电池
        ElectricityBattery battery = null;
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType())) {
            battery = batteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                createFlag = true;
            }
        }

        // 不会生成滞纳金记录
        if (!createFlag) {
            return null;
        }


        // 生成实体记录
        CarRentalPackageOrderSlippagePO slippageEntity = new CarRentalPackageOrderSlippagePO();
        slippageEntity.setUid(uid);
        slippageEntity.setRentalPackageOrderNo(packageOrderEntity.getOrderNo());
        slippageEntity.setRentalPackageId(packageOrderEntity.getRentalPackageId());
        slippageEntity.setRentalPackageType(packageOrderEntity.getRentalPackageType());
        slippageEntity.setType(SlippageTypeEnum.FREEZE.getCode());
        slippageEntity.setLateFee(packageOrderEntity.getLateFee());
        slippageEntity.setLateFeeStartTime(System.currentTimeMillis());
        slippageEntity.setPayState(PayStateEnum.UNPAID.getCode());
        slippageEntity.setTenantId(packageOrderEntity.getTenantId());
        slippageEntity.setFranchiseeId(packageOrderEntity.getFranchiseeId());
        slippageEntity.setStoreId(packageOrderEntity.getStoreId());
        slippageEntity.setCreateUid(uid);

        // 记录设备信息
        if (ObjectUtils.isNotEmpty(userCar)) {
            slippageEntity.setCarSn(userCar.getSn());
        }
        if (ObjectUtils.isNotEmpty(battery)) {
            slippageEntity.setBatterySn(battery.getSn());
        }

        return slippageEntity;
    }
}
