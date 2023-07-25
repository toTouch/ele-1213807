package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderSlippagePO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderSlippageService;
import com.xiliulou.electricity.service.car.biz.CarRentalPackageMemberTermBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermBizServiceImpl implements CarRentalPackageMemberTermBizService {

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

        // 当前时间 + 24小时
        long nowTime = System.currentTimeMillis() + TimeConstant.DAY_MILLISECOND;

        while (lookFlag) {
            // 1. 查询会员套餐表中，套餐购买订单已过期的数据
            List<CarRentalPackageMemberTermPO> memberTermEntityList = carRentalPackageMemberTermService.pageExpire(offset, size, nowTime);
            if (CollectionUtils.isEmpty(memberTermEntityList)) {
                lookFlag = false;
                break;
            }

            for (CarRentalPackageMemberTermPO memberTermEntity : memberTermEntityList) {
                // 根据UID查询名下的未使用的订单第一条订单
                CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectFirstUnUsedByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                CarRentalPackageOrderSlippagePO slippageEntityInsert = null;
                if (ObjectUtils.isEmpty(packageOrderEntity)) {
                    log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. user no available orders. uid is {}", memberTermEntity.getUid());
                    // 构建逾期订单
                    slippageEntityInsert = buildCarRentalPackageOrderSlippage(memberTermEntity.getUid(), memberTermEntity);
                    if (ObjectUtils.isEmpty(slippageEntityInsert)) {
                        log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. user no device. skip. uid is {}", memberTermEntity.getUid());
                        continue;
                    }
                } else {
                    // 二次保底确认
                    CarRentalPackageMemberTermPO oriMemberTermEntity = carRentalPackageMemberTermService.selectById(memberTermEntity.getRentalPackageId());
                    if (oriMemberTermEntity.getRentalPackageOrderNo().equals(packageOrderEntity.getOrderNo())) {
                        log.info("CarRentalPackageMemberTermBizService.expirePackageOrder. car_rental_package_member_term processed. skip. uid is {}", memberTermEntity.getUid());
                        continue;
                    }
                }
                // 数据落库处理
                saveExpirePackageOrderTx(slippageEntityInsert, packageOrderEntity, memberTermEntity);
            }


            offset += size;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveExpirePackageOrderTx(CarRentalPackageOrderSlippagePO slippageEntityInsert, CarRentalPackageOrderPO packageOrderEntityNew, CarRentalPackageMemberTermPO memberTermEntity) {
        // 生成逾期订单
        if (ObjectUtils.isNotEmpty(slippageEntityInsert)) {
            carRentalPackageOrderSlippageService.insert(slippageEntityInsert);
        }
        // TODO 查询过期订单是否存在因冻结产生的逾期订单，若有，则需要更新滞纳金结束时间
        if (ObjectUtils.isNotEmpty(packageOrderEntityNew)) {
            // 覆盖会员期限信息
            CarRentalPackageMemberTermPO memberTermEntityUpdate = new CarRentalPackageMemberTermPO();
            memberTermEntityUpdate.setRentalPackageOrderNo(packageOrderEntityNew.getDepositPayOrderNo());
            memberTermEntityUpdate.setRentalPackageId(packageOrderEntityNew.getRentalPackageId());
            memberTermEntityUpdate.setRentalPackageConfine(packageOrderEntityNew.getConfine());
            memberTermEntityUpdate.setId(memberTermEntity.getId());

            // 计算到期时间
            Integer tenancy = packageOrderEntityNew.getTenancy();
            Integer tenancyUnit = packageOrderEntityNew.getTenancyUnit();
            long dueTime = System.currentTimeMillis();
            if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
            }
            if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                dueTime = dueTime + (tenancy * 1000);
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
            carRentalPackageOrderService.updateUseStateByOrderNo(memberTermEntity.getRentalPackageOrderNo(), UseStateEnum.EXPIRED.getCode(), null);
            carRentalPackageOrderService.updateUseStateByOrderNo(packageOrderEntityNew.getOrderNo(), UseStateEnum.IN_USE.getCode(), null);
        }
    }


    private CarRentalPackageOrderSlippagePO buildCarRentalPackageOrderSlippage(Long uid, CarRentalPackageMemberTermPO memberTermEntity) {
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

        // 查询当时购买的订单信息
        CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(memberTermEntity.getRentalPackageOrderNo());
        if (ObjectUtils.isEmpty(packageOrderEntity)) {
            log.info("CarRentalPackageMemberTermBizService.buildCarRentalPackageOrderSlippage failed. not found car_rental_package_order. orderNo is {}", memberTermEntity.getRentalPackageOrderNo());
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
