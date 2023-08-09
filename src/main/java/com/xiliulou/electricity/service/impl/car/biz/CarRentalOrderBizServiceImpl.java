package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RentalTypeEnum;
import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
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
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    @Resource
    private ElectricityCarModelService carModelService;

    @Resource
    private UserInfoService userInfoService;

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
        if (!ObjectUtils.allNotNull(tenantId, uid, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (UserInfo.CAR_RENT_STATUS_NO.equals(userInfo.getCarRentStatus())) {
            log.error("bindingCar, t_user_info is unBind. uid is {}", uid);
            throw new BizException("100015", "用户未绑定车辆");
        }

        // 解绑车辆的限制
        boolean exitUnpaid = carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid);
        if (exitUnpaid) {
            throw new BizException("300006", "未缴纳押金");
        }

        // 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.error("bindingCar, not found t_car_rental_package_member_term or status is wrong. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        if (ObjectUtils.isEmpty(rentalPackageId)) {
            log.error("bindingCar, t_car_rental_package_member_term not have rentalPackageId. uid is {}", uid);
            throw new BizException("100015", "用户未绑定车辆");
        }

        // 通过套餐ID找到套餐
        CarRentalPackagePo rentalPackageEntity = carRentalPackageService.selectById(rentalPackageId);
        if (ObjectUtils.isEmpty(rentalPackageEntity)) {
            log.error("bindingCar, not found t_car_rental_package. rentalPackageId is {}", rentalPackageId);
            throw new BizException("300000", "数据有误");
        }

        // 查询车辆
        ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(electricityCar)) {
            log.error("bindingCar, not found t_electricity_car. uid is {}, tenantId is {}", uid, tenantId);
            throw new BizException("100015", "用户未绑定车辆");
        }

        // 生成租赁还车订单
        CarRentalOrderPo carRentalOrderEntityInsert = buildCarRentalOrder(rentalPackageEntity, memberTermEntity, electricityCar, optUid, RentalTypeEnum.RETURN.getCode());

        // 生成用户租赁状态
        UserInfo userInfoUpdate = buildUserInfo(uid, RentalTypeEnum.RETURN.getCode());

        // 减少车辆型号的已租数量
        ElectricityCarModel carModel = carModelService.queryByIdFromCache(electricityCar.getModelId());
        ElectricityCarModel carModelUpdate = buildElectricityCarModel(carModel, RentalTypeEnum.RETURN.getCode());

        // 撤销车辆绑定用户信息
        ElectricityCar electricityCarUpdate = buildElectricityCar(electricityCar, uid, userInfo, RentalTypeEnum.RETURN.getCode());

        // 处理事务层
        bindAndUnBindCarTx(carRentalOrderEntityInsert, userInfoUpdate, carModelUpdate, electricityCarUpdate);

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

        // 查询用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (UserInfo.CAR_RENT_STATUS_YES.equals(userInfo.getCarRentStatus())) {
            log.error("bindingCar, t_user_info is bind car. uid is {}", uid);
            throw new BizException("100253", "用户已绑定车辆，请先解绑");
        }

        // 查询租车会员信息
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

        // 通过套餐ID找到套餐
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
        CarRentalOrderPo carRentalOrderEntityInsert = buildCarRentalOrder(rentalPackageEntity, memberTermEntity, electricityCar, optUid, RentalTypeEnum.RENTAL.getCode());

        // 生成用户租赁状态
        UserInfo userInfoUpdate = buildUserInfo(uid, RentalTypeEnum.RENTAL.getCode());

        // 构建车辆型号的已租数量
        ElectricityCarModel carModel = carModelService.queryByIdFromCache(electricityCar.getModelId());
        ElectricityCarModel carModelUpdate = buildElectricityCarModel(carModel, RentalTypeEnum.RENTAL.getCode());

        // 生成车辆绑定用户信息
        ElectricityCar electricityCarUpdate = buildElectricityCar(electricityCar, uid, userInfo, RentalTypeEnum.RENTAL.getCode());

        // 处理事务层
        bindAndUnBindCarTx(carRentalOrderEntityInsert, userInfoUpdate, carModelUpdate, electricityCarUpdate);

        return true;
    }


    /**
     * 用户绑定车辆事务处理
     * @param carRentalOrderEntity 车辆租赁订单
     * @param userInfo
     * @param carModelUpdate
     * @param electricityCarUpdate
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindAndUnBindCarTx(CarRentalOrderPo carRentalOrderEntity, UserInfo userInfo, ElectricityCarModel carModelUpdate, ElectricityCar electricityCarUpdate) {
        // 生成租赁订单
        carRentalOrderService.insert(carRentalOrderEntity);
        // 更改用户租赁状态
        userInfoService.updateByUid(userInfo);
        // 更改车辆型号的已租数量
        carModelService.updateById(carModelUpdate);
        // 更新车辆的归属人
        carService.updateCarBindStatusById(electricityCarUpdate);
    }

    /**
     * 构建车辆更新数据
     * @param car 原始车辆信息
     * @param uid 用户UID
     * @param rentalType 订单类型
     * @return 车辆更新信息
     */
    private ElectricityCar buildElectricityCar(ElectricityCar car, Long uid, UserInfo userInfo, Integer rentalType) {
        ElectricityCar carUpdate = new ElectricityCar();
        car.setId(car.getId());
        car.setUpdateTime(System.currentTimeMillis());
        if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
            car.setUid(uid);
            car.setUserInfoId(userInfo.getId());
            car.setUserName(userInfo.getName());
            car.setPhone(userInfo.getPhone());
        }
        if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
            car.setUid(null);
            car.setUserInfoId(null);
            car.setUserName(null);
            car.setPhone(null);
        }
        return carUpdate;
    }

    /**
     * 构建车辆型号更新数据
     * @param carModel 原始车辆型号信息
     * @param rentalType 订单类型
     * @return 车辆型号更新信息
     */
    private ElectricityCarModel buildElectricityCarModel(ElectricityCarModel carModel, Integer rentalType) {
        ElectricityCarModel carModelUpdate = new ElectricityCarModel();
        carModelUpdate.setId(carModel.getId());
        carModelUpdate.setUpdateTime(System.currentTimeMillis());
        if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
            carModelUpdate.setRentedQuantity(carModel.getRentedQuantity() + 1);
        }
        if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
            carModelUpdate.setRentedQuantity(carModel.getRentedQuantity() - 1);
        }
        return carModelUpdate;
    }

    /**
     * 构建用户更新数据
     * @param uid 用户UID
     * @param rentalType 订单类型
     * @return 用户信息
     */
    private UserInfo buildUserInfo(Long uid, Integer rentalType) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(uid);
        userInfo.setUpdateTime(System.currentTimeMillis());
        if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
            userInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_YES);
        }
        if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
            userInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
        }

        return userInfo;
    }

    /**
     * 构建租赁订单信息
     * @param rentalPackageEntity  租赁套餐信息
     * @param memberTermEntity 租车会员信息
     * @param electricityCar 车辆信息
     * @param optId 操作用户UID
     * @param rentalType 订单类型
     * @return 租赁订单信息
     */
    private CarRentalOrderPo buildCarRentalOrder(CarRentalPackagePo rentalPackageEntity, CarRentalPackageMemberTermPo memberTermEntity, ElectricityCar electricityCar, Long optId, Integer rentalType) {
        CarRentalOrderPo carRentalOrdeEntity = new CarRentalOrderPo();
        carRentalOrdeEntity.setUid(memberTermEntity.getUid());
        carRentalOrdeEntity.setType(rentalType);
        if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
            carRentalOrdeEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, memberTermEntity.getUid()));
        }
        if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
            carRentalOrdeEntity.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_CAR, memberTermEntity.getUid()));
        }
        carRentalOrdeEntity.setRentalPackageOrderNo(memberTermEntity.getRentalPackageOrderNo());
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
