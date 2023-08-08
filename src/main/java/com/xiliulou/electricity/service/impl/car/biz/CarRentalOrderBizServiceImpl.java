package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.car.CarRentalOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RentalTypeEnum;
import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.car.CarRentalOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 车辆租赁订单业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalOrderBizServiceImpl implements CarRentalOrderBizService {

    @Resource
    private CarRentalOrderService carRentalOrderService;

    @Resource
    private ElectricityCarService carService;

    @Resource
    private CarRentalPackageService carRentalPackageService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    /**
     * 解绑用户车辆
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @param optUid   操作用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean unBindingCar(Integer tenantId, Long uid, Long optUid) {
        // 生成租赁订单
        // 更改用户租赁状态
        // 增加车辆型号的已租数量
        return true;
    }

    /**
     * 给用户绑定车辆
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @param carSn    车辆SN码
     * @param optUid   操作用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean bindingCar(Integer tenantId, Long uid, String carSn, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, uid, carSn, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询租车会员信
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.error("bindingCar, not found t_car_rental_package_member_term or status is wrong. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        if (ObjectUtils.isEmpty(rentalPackageId)) {
            log.error("bindingCar, t_car_rental_package_member_term not have rentalPackageId. uid is {}", uid);
            throw new BizException("300037", "该用户下无套餐订单，请先绑定套餐");
        }

        // 通过套餐找到套餐
        CarRentalPackagePo rentalPackageEntity = carRentalPackageService.selectById(rentalPackageId);
        if (ObjectUtils.isEmpty(rentalPackageEntity)) {
            log.error("bindingCar, not found t_car_rental_package. rentalPackageId is {}", rentalPackageId);
            throw new BizException("300000", "数据有误");
        }

        // 查询车辆
        ElectricityCar electricityCar = carService.selectBySn(carSn, tenantId);
        if (ObjectUtils.isEmpty(electricityCar)) {
            log.error("bindingCar, not found t_electricity_car. carSn is {}, tenantId is {}", rentalPackageId, tenantId);
            throw new BizException("100007", "未找到车辆");
        }

        // 是否被用户绑定
        if ((ObjectUtils.isNotEmpty(electricityCar.getUid()) && electricityCar.getUid() != 0L) || electricityCar.getUid().equals(uid)) {
            log.error("bindingCar, t_electricity_car bind uid is {}", electricityCar.getUid());
            throw new BizException("100253", "用户已绑定车辆，请先解绑");
        }

        // 比对车辆是否符合(加盟商、门店、型号)
        if (!rentalPackageEntity.getFranchiseeId().equals(electricityCar.getFranchiseeId().intValue()) || !rentalPackageEntity.getStoreId().equals(electricityCar.getStoreId().intValue())
                || !rentalPackageEntity.getCarModelId().equals(electricityCar.getModelId())) {
            log.error("bindingCar, t_electricity_car carModel or organization is wrong", electricityCar.getUid());
            throw new BizException("100007", "未找到车辆");
        }

        // 生成租赁订单
        CarRentalOrderPo carRentalOrderEntity = buildCarRentalOrder(rentalPackageEntity, memberTermEntity, electricityCar, optUid);

        // 更改用户租赁状态
        // 增加车辆型号的已租数量

        bindingCarTx(carRentalOrderEntity);
        return true;
    }

    /**
     * 用户绑定车辆事务处理
     * @param carRentalOrderEntity 车辆租赁订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindingCarTx(CarRentalOrderPo carRentalOrderEntity) {
        // 生成租赁订单
        carRentalOrderService.insert(carRentalOrderEntity);
        // 更改用户租赁状态

        // 增加车辆型号的已租数量

    }

    /**
     * 构建租赁订单信息
     * @param rentalPackageEntity  租赁套餐信息
     * @param memberTermEntity 租车会员信息
     * @param electricityCar 车辆信息
     * @param optId 操作用户UID
     * @return 租赁订单信息
     */
    private CarRentalOrderPo buildCarRentalOrder(CarRentalPackagePo rentalPackageEntity, CarRentalPackageMemberTermPo memberTermEntity, ElectricityCar electricityCar, Long optId) {

        CarRentalOrderPo carRentalOrdeEntity = new CarRentalOrderPo();
        carRentalOrdeEntity.setUid(memberTermEntity.getUid());
        carRentalOrdeEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, memberTermEntity.getUid()));
        carRentalOrdeEntity.setRentalPackageOrderNo(memberTermEntity.getRentalPackageOrderNo());
        carRentalOrdeEntity.setType(RentalTypeEnum.RENTAL.getCode());
        carRentalOrdeEntity.setCarModelId(rentalPackageEntity.getCarModelId());
        carRentalOrdeEntity.setCarSn(electricityCar.getSn());
        carRentalOrdeEntity.setPayType(PayTypeEnum.OFF_LINE.getCode());
        carRentalOrdeEntity.setRentalState(CarRentalStateEnum.SUCCESS.getCode());
        carRentalOrdeEntity.setTenantId(rentalPackageEntity.getTenantId());
        carRentalOrdeEntity.setFranchiseeId(rentalPackageEntity.getFranchiseeId());
        carRentalOrdeEntity.setStoreId(rentalPackageEntity.getStoreId());
        carRentalOrdeEntity.setCreateUid(optId);

        return carRentalOrdeEntity;

    }


}
