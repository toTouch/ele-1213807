package com.xiliulou.electricity.service.impl.car.biz;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.car.CarRentalOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.electricity.service.car.biz.CarRentalOrderBizService;
import com.xiliulou.electricity.service.retrofit.Jt808RetrofitService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.vo.Jt808DeviceInfoVo;
import com.xiliulou.electricity.web.query.jt808.Jt808DeviceControlRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 车辆租赁订单业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalOrderBizServiceImpl implements CarRentalOrderBizService {

    @Resource
    private RedisService redisService;

    @Resource
    private CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private Jt808RetrofitService jt808RetrofitService;

    @Resource
    private EleBindCarRecordService eleBindCarRecordService;

    @Resource
    private UserService userService;

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
     * 还车申请审批
     *
     * @param carRentalOrderNo 车辆租赁订单编码，还车
     * @param approveFlag      审批标识：true(通过)、false(驳回)
     * @param apploveDesc      审批意见
     * @param apploveUid       审批人
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean approveRefundCarOrder(String carRentalOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid) {
        if (!ObjectUtils.allNotNull(carRentalOrderNo, approveFlag)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询车辆租赁订单
        CarRentalOrderPo carRentalOrderPo = carRentalOrderService.selectByOrderNo(carRentalOrderNo);
        if (ObjectUtils.isEmpty(carRentalOrderPo)) {
            log.info("approveRefundCarOrder failed. not found t_car_rental_order. carRentalOrderNo is {}", carRentalOrderNo);
            throw new BizException("300000", "数据有误");
        }

        if (!RentalTypeEnum.RETURN.getCode().equals(carRentalOrderPo.getType()) || !CarRentalStateEnum.AUDIT_ING.getCode().equals(carRentalOrderPo.getRentalState())) {
            log.info("approveRefundCarOrder failed. t_car_rental_order type or state is wrong. carRentalOrderNo is {}", carRentalOrderNo);
            throw new BizException("300000", "数据有误");
        }

        Integer tenantId = carRentalOrderPo.getTenantId();
        Long uid = carRentalOrderPo.getUid();

        // 判定用户车辆
        ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(electricityCar)) {
            log.info("approveRefundCarOrder failed. User not bound to vehicle. uid is {}", uid);
            throw new BizException("100015", "用户未绑定车辆");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        // 构建还车审批数据
        CarRentalOrderPo rentalOrderApprove = buildApproveCarRentalOrderPo(carRentalOrderPo, approveFlag, apploveDesc, apploveUid);

        UserInfo userInfoUpdate = null;
        ElectricityCarModel carModelUpdate = null;
        ElectricityCar electricityCarUpdate = null;
        EleBindCarRecord eleBindCarRecord = null;
        CarLockCtrlHistory carLockCtrlHistory = null;
        if (approveFlag) {
            // 处理人用户租赁状态
            userInfoUpdate = buildUserInfo(uid, RentalTypeEnum.RETURN.getCode());

            // 减少车辆型号的已租数量
            ElectricityCarModel carModel = carModelService.queryByIdFromCache(electricityCar.getModelId());
            carModelUpdate = buildElectricityCarModel(carModel, RentalTypeEnum.RETURN.getCode());

            // 撤销车辆绑定用户信息
            electricityCarUpdate = buildElectricityCar(electricityCar, uid, userInfo, RentalTypeEnum.RETURN.getCode());

            // 生成车辆记录
            User user = userService.queryByUidFromCache(apploveUid);
            eleBindCarRecord = buildEleBindCarRecord(electricityCar, userInfo, user, RentalTypeEnum.RETURN.getCode());

            // JT808
            carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo, RentalTypeEnum.RETURN.getCode());
        } else {
            // JT808
            carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo, RentalTypeEnum.RENTAL.getCode());
        }

        // 处理事务
        approveRefundCarOrderTx(approveFlag, rentalOrderApprove, userInfoUpdate, carModelUpdate, electricityCarUpdate, eleBindCarRecord, carLockCtrlHistory);

        return true;
    }

    /**
     * 还车审核事务处理
     * @param approveFlag
     * @param rentalOrderApprove
     * @param userInfoUpdate
     * @param carModelUpdate
     * @param electricityCarUpdate
     * @param eleBindCarRecord
     * @param carLockCtrlHistory
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveRefundCarOrderTx(boolean approveFlag, CarRentalOrderPo rentalOrderApprove, UserInfo userInfoUpdate, ElectricityCarModel carModelUpdate,
                                        ElectricityCar electricityCarUpdate, EleBindCarRecord eleBindCarRecord, CarLockCtrlHistory carLockCtrlHistory) {
        // 更改车辆租赁订单
        carRentalOrderService.updateById(rentalOrderApprove);

        // JT808
        if (ObjectUtils.isNotEmpty(carLockCtrlHistory)) {
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
        }

        // 审核通过
        if (approveFlag) {
            // 更改用户租赁状态
            userInfoService.updateByUid(userInfoUpdate);
            // 更改车辆型号的已租数量
            carModelService.updateById(carModelUpdate);
            // 更新车辆的归属人
            carService.updateCarBindStatusById(electricityCarUpdate);
            // 车辆操作记录
            eleBindCarRecordService.insert(eleBindCarRecord);
        }
    }

    /**
     * 构建租赁订单的审批数据
     * @param carRentalOrderPo 还车审批订单
     * @param approveFlag 审批标识：true(通过)、false(驳回)
     * @param apploveDesc 审批意见
     * @param apploveUid 审批人UID
     * @return 租赁订单审批数据集
     */
    private CarRentalOrderPo buildApproveCarRentalOrderPo(CarRentalOrderPo carRentalOrderPo, boolean approveFlag, String apploveDesc, Long apploveUid) {
        CarRentalOrderPo rentalOrderApprove = new CarRentalOrderPo();
        rentalOrderApprove.setId(carRentalOrderPo.getId());
        rentalOrderApprove.setUpdateTime(System.currentTimeMillis());
        rentalOrderApprove.setUpdateUid(apploveUid);
        rentalOrderApprove.setRemark(apploveDesc);
        rentalOrderApprove.setAuditTime(System.currentTimeMillis());
        if (approveFlag) {
            rentalOrderApprove.setRentalState(CarRentalStateEnum.SUCCESS.getCode());
        } else {
            rentalOrderApprove.setRentalState(CarRentalStateEnum.AUDIT_REJECT.getCode());
        }

        return rentalOrderApprove;
    }

    /**
     * 用户还车申请
     *
     * @param tenantId 租户ID
     * @param uid      用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean refundCarOrderApply(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        String cacheKey = CacheConstant.CACHE_USER_RETURN_CAR_LOCK + uid;
        try {
            if (!redisService.setNx(cacheKey, String.valueOf(System.currentTimeMillis()), 10000L, false)) {
                log.error("refundCarOrderApply failed. frequent operations. uid is {}", uid);
                throw new BizException("100002", "操作频繁");
            }

            // 判定会员
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            if (ObjectUtils.isEmpty(memberTermEntity) || !MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
                log.error("refundCarOrderApply failed. t_car_rental_package_member_term not found or status is error. uid is {}", uid);
                throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
            }

            // 判定滞纳金
            boolean exitUnpaid = carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid);
            if (exitUnpaid) {
                log.error("refundCarOrderApply failed. User has a late fee. uid is {}", uid);
                throw new BizException("300001", "存在滞纳金，请先缴纳");
            }

            // 判定用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

            if (ObjectUtils.isEmpty(userInfo)) {
                log.error("refundCarOrderApply failed. not found userInfo, uid is {}", uid);
                throw new BizException("ELECTRICITY.0019", "未找到用户");
            }

            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("refundCarOrderApply failed. user is disable, uid is {}", uid);
                throw new BizException("ELECTRICITY.0024", "用户已被禁用");
            }

            // 判定用户绑定车辆
            ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
            if (ObjectUtils.isEmpty(electricityCar)) {
                log.error("refundCarOrderApply failed. user is unbound vehicle, uid is {}", uid);
                throw new BizException("100015", "用户未绑定车辆");
            }

            // 判定是否存在审核中的还车订单
            CarRentalOrderPo carRentalOrderPo = carRentalOrderService.selectLastByUidAndSnAndTypeAndState(tenantId, uid, RentalTypeEnum.RETURN.getCode(), CarRentalStateEnum.AUDIT_ING.getCode(), electricityCar.getSn());
            if (ObjectUtils.isNotEmpty(carRentalOrderPo)) {
                log.error("refundCarOrderApply failed. Returning the vehicle under review, uid is {}", uid);
                throw new BizException("100265", "还车审核中，请耐心等待");
            }

            CarRentalOrderPo carRentalOrderPoInsert = buildCarRentalOrderPo(userInfo, RentalTypeEnum.RETURN.getCode(), memberTermEntity, electricityCar);

            // 事务处理
            refundCarOrderApplyTx(carRentalOrderPoInsert);
        } catch (BizException e) {
            log.error("refundCarOrderApply failed.", e);
            throw new BizException(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("refundCarOrderApply failed.", e);
            throw new BizException(e.getMessage());
        } finally {
            redisService.delete(cacheKey);
        }

        return true;
    }

    /**
     * 还车申请事务处理
     * @param carRentalOrderPoInsert 车辆租赁订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundCarOrderApplyTx(CarRentalOrderPo carRentalOrderPoInsert) {
        carRentalOrderService.insert(carRentalOrderPoInsert);
    }

    /**
     * 构建车辆租赁订单
     * @param userInfo 用户会员信息
     * @param type 租赁类型
     * @param memberTermEntity 租车会员信息
     * @param electricityCar 车辆信息
     * @return 租赁订单
     */
    private CarRentalOrderPo buildCarRentalOrderPo(UserInfo userInfo, Integer type, CarRentalPackageMemberTermPo memberTermEntity, ElectricityCar electricityCar) {
        CarRentalOrderPo carRentalOrderPoInsert = new CarRentalOrderPo();
        carRentalOrderPoInsert.setUid(userInfo.getUid());

        if (RentalTypeEnum.RENTAL.getCode().equals(type)) {
            carRentalOrderPoInsert.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, userInfo.getUid()));
        }

        if (RentalTypeEnum.RETURN.getCode().equals(type)) {
            carRentalOrderPoInsert.setOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_CAR, userInfo.getUid()));
        }
        carRentalOrderPoInsert.setRentalPackageOrderNo(memberTermEntity.getRentalPackageOrderNo());
        carRentalOrderPoInsert.setType(type);
        carRentalOrderPoInsert.setCarModelId(electricityCar.getModelId());
        carRentalOrderPoInsert.setCarSn(electricityCar.getSn());
        carRentalOrderPoInsert.setPayType(type);
        carRentalOrderPoInsert.setRentalState(CarRentalStateEnum.AUDIT_ING.getCode());
        carRentalOrderPoInsert.setTenantId(userInfo.getTenantId());
        carRentalOrderPoInsert.setFranchiseeId(userInfo.getFranchiseeId().intValue());
        carRentalOrderPoInsert.setStoreId(userInfo.getStoreId().intValue());
        carRentalOrderPoInsert.setCreateUid(userInfo.getUid());
        carRentalOrderPoInsert.setCreateTime(System.currentTimeMillis());

        return carRentalOrderPoInsert;

    }

    /**
     * 用户扫码绑定车辆
     *
     * @param tenantId 租户ID
     * @param franchiseeId 加盟商ID
     * @param uid      用户UID
     * @param carSn    车辆SN码
     * @param optUid   操作用户UID
     * @return true(成功)、false(失败)
     */
    @Override
    public boolean bindingCarByQR(Integer tenantId, Integer franchiseeId, Long uid, String carSn, Long optUid) {
        if (!ObjectUtils.allNotNull(tenantId, franchiseeId, uid, carSn, optUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询车辆
        ElectricityCar electricityCar = carService.selectBySn(carSn, tenantId);
        if (ObjectUtils.isEmpty(electricityCar)) {
            log.error("bindingCarByQR, not found t_electricity_car. carSn is {}, tenantId is {}", carSn, tenantId);
            throw new BizException("100007", "未找到车辆");
        }

        if (!electricityCar.getFranchiseeId().equals(Long.valueOf(franchiseeId))) {
            log.error("bindingCarByQR, t_electricity_car franchiseeId and param franchiseeId mismatching. param franchiseeId is {}, car franchiseeId is {}", franchiseeId, electricityCar.getFranchiseeId());
            throw new BizException("300059", "该车辆SN码与加盟商不匹配，请重新扫码");
        }

        // 是否被其它用户绑定
        if (ObjectUtils.isNotEmpty(electricityCar.getUid()) && !uid.equals(electricityCar.getUid())) {
            log.error("bindingCarByQR, t_electricity_car bind uid is {}", electricityCar.getUid());
            throw new BizException("300038", "该车已被其他用户绑定");
        }

        // 判定滞纳金
        boolean exitUnpaid = carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid);
        if (exitUnpaid) {
            log.error("bindingCarByQR failed. User has a late fee. uid is {}", uid);
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }

        // 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.error("bindingCarByQR, not found t_car_rental_package_member_term or status is wrong. uid is {}", uid);
            throw new BizException("300037", "您名下暂无可用套餐，不支持该操作");
        }

        if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("bindingCarByQR, You have a process under review and cannot be operated. uid is {}", uid);
            throw new BizException("300056", "该用户有正在审核中流程，不可操作");
        }

        Integer franchiseeIdExit = memberTermEntity.getFranchiseeId();
        if (ObjectUtils.isNotEmpty(franchiseeIdExit) && !franchiseeId.equals(franchiseeIdExit)) {
            log.error("bindingCarByQR, t_car_rental_package_member_term franchiseeId and param franchiseeId mismatching. param franchiseeId is {}, member franchiseeId is {}", franchiseeId, franchiseeIdExit);
            throw new BizException("300059", "该车辆SN码与加盟商不匹配，请重新扫码");
        }

        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        if (ObjectUtils.isEmpty(rentalPackageId) || memberTermEntity.getDueTime() <= System.currentTimeMillis()
                || (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTermEntity.getRentalPackageConfine()) && memberTermEntity.getResidue() <= 0L)) {
            log.error("bindingCarByQR, t_car_rental_package_member_term not have rentalPackageId. uid is {}", uid);
            throw new BizException("300037", "您名下暂无可用套餐，不支持该操作");
        }

        // 通过套餐ID找到套餐
        CarRentalPackagePo rentalPackageEntity = carRentalPackageService.selectById(rentalPackageId);
        if (ObjectUtils.isEmpty(rentalPackageEntity)) {
            log.error("bindingCarByQR, not found t_car_rental_package. rentalPackageId is {}", rentalPackageId);
            throw new BizException("300000", "数据有误");
        }

        // 查询用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("bindingCarByQR, user is disable. uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");

        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("bindingCarByQR, user not auth. uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0041", "未实名认证");
        }

        // 查询自己名下是否存在车辆
        ElectricityCar electricityCarUser = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(electricityCarUser)) {
            throw new BizException("ELECTRICITY.0031", "用户已绑定车辆，请解绑后再绑定");
        }

        // 比对车辆是否符合(门店、型号)
        if (!rentalPackageEntity.getStoreId().equals(electricityCar.getStoreId().intValue()) || !rentalPackageEntity.getCarModelId().equals(electricityCar.getModelId())) {
            log.error("bindingCarByQR, t_electricity_car carModel or organization is wrong");
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

        // 生成车辆记录
        User user = userService.queryByUidFromCache(optUid);
        EleBindCarRecord eleBindCarRecord = buildEleBindCarRecord(electricityCar, userInfo, user, RentalTypeEnum.RENTAL.getCode());

        // JT808
        CarLockCtrlHistory carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo, RentalTypeEnum.RENTAL.getCode());

        // 处理事务层
        bindAndUnBindCarTx(carRentalOrderEntityInsert, userInfoUpdate, carModelUpdate, electricityCarUpdate, eleBindCarRecord, carLockCtrlHistory);

        return true;
    }

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
            log.error("unBindingCar, t_user_info is unBind. uid is {}", uid);
            throw new BizException("100015", "用户未绑定车辆");
        }

        // 解绑车辆的限制
        boolean exitUnpaid = carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid);
        if (exitUnpaid) {
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }

        // 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.error("unBindingCar, not found t_car_rental_package_member_term or status is wrong. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("unBindingCar, t_car_rental_package_member_term status is wrong. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        if (ObjectUtils.isEmpty(rentalPackageId)) {
            log.error("unBindingCar, t_car_rental_package_member_term not have rentalPackageId. uid is {}", uid);
            throw new BizException("100015", "用户未绑定车辆");
        }

        // 通过套餐ID找到套餐
        CarRentalPackagePo rentalPackageEntity = carRentalPackageService.selectById(rentalPackageId);
        if (ObjectUtils.isEmpty(rentalPackageEntity)) {
            log.error("unBindingCar, not found t_car_rental_package. rentalPackageId is {}", rentalPackageId);
            throw new BizException("300000", "数据有误");
        }

        // 查询车辆
        ElectricityCar electricityCar = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isEmpty(electricityCar)) {
            log.error("unBindingCar, not found t_electricity_car. uid is {}, tenantId is {}", uid, tenantId);
            throw new BizException("100015", "用户未绑定车辆");
        }

        // 判定是否存在审核中的还车订单
        CarRentalOrderPo carRentalOrderPo = carRentalOrderService.selectLastByUidAndSnAndTypeAndState(tenantId, uid, RentalTypeEnum.RETURN.getCode(), CarRentalStateEnum.AUDIT_ING.getCode(), electricityCar.getSn());
        if (ObjectUtils.isNotEmpty(carRentalOrderPo)) {
            log.error("unBindingCar failed. The user has submitted a return request, please review it. uid is {}", uid);
            throw new BizException("300055", "用户已提交还车申请，请审核");
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

        // 生成车辆记录
        User user = userService.queryByUidFromCache(optUid);
        EleBindCarRecord eleBindCarRecord = buildEleBindCarRecord(electricityCar, userInfo, user, RentalTypeEnum.RETURN.getCode());

        // JT808
        CarLockCtrlHistory carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo, RentalTypeEnum.RETURN.getCode());

        // 处理事务层
        bindAndUnBindCarTx(carRentalOrderEntityInsert, userInfoUpdate, carModelUpdate, electricityCarUpdate, eleBindCarRecord, carLockCtrlHistory);

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

        // 判定滞纳金
        boolean exitUnpaid = carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid);
        if (exitUnpaid) {
            log.error("bindingCar failed. User has a late fee. uid is {}", uid);
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }

        // 查询租车会员信息
        CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.PENDING_EFFECTIVE.getCode().equals(memberTermEntity.getStatus())) {
            log.error("bindingCar, not found t_car_rental_package_member_term or status is wrong. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        if (!MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("bindingCar, t_car_rental_package_member_term status is wrong. uid is {}", uid);
            throw new BizException("300057", "您有正在审核中/已冻结流程，不支持该操作");
        }

        Long rentalPackageId = memberTermEntity.getRentalPackageId();
        if (ObjectUtils.isEmpty(rentalPackageId) || memberTermEntity.getDueTime() <= System.currentTimeMillis()
                || (RenalPackageConfineEnum.NUMBER.getCode().equals(memberTermEntity.getRentalPackageConfine()) && memberTermEntity.getResidue() <= 0L)) {
            log.error("bindingCar, t_car_rental_package_member_term not have rentalPackageId. uid is {}", uid);
            throw new BizException("300037", "您名下暂无可用套餐，不支持该操作");
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

        // 是否被其它用户绑定
        if (ObjectUtils.isNotEmpty(electricityCar.getUid()) && !uid.equals(electricityCar.getUid())) {
            log.error("bindingCar, t_electricity_car bind uid is {}", electricityCar.getUid());
            throw new BizException("300038", "该车已被其他用户绑定");
        }

        // 查询用户信息
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("bindingCar, user is disable. uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0024", "用户已被禁用");

        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("bindingCar, user not auth. uid is {}", userInfo.getUid());
            throw new BizException("ELECTRICITY.0041", "未实名认证");
        }

        // 查询自己名下是否存在车辆
        ElectricityCar electricityCarUser = carService.selectByUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(electricityCarUser)) {
            // 先去解绑
            boolean unBindFlag = unBindingCar(tenantId, uid, optUid);
            if (!unBindFlag) {
                log.info("bindingCar, unBindingCar failed. uid is {}", uid);
                throw new BizException("300039", "车辆绑定失败");
            }
        }

        // 比对车辆是否符合(门店、型号)
        if (!rentalPackageEntity.getStoreId().equals(electricityCar.getStoreId().intValue()) || !rentalPackageEntity.getCarModelId().equals(electricityCar.getModelId())) {
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

        // 生成车辆记录
        User user = userService.queryByUidFromCache(optUid);
        EleBindCarRecord eleBindCarRecord = buildEleBindCarRecord(electricityCar, userInfo, user, RentalTypeEnum.RENTAL.getCode());

        // JT808
        CarLockCtrlHistory carLockCtrlHistory = buildCarLockCtrlHistory(electricityCar, userInfo, RentalTypeEnum.RENTAL.getCode());

        // 处理事务层
        bindAndUnBindCarTx(carRentalOrderEntityInsert, userInfoUpdate, carModelUpdate, electricityCarUpdate, eleBindCarRecord, carLockCtrlHistory);

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
    public void bindAndUnBindCarTx(CarRentalOrderPo carRentalOrderEntity, UserInfo userInfo, ElectricityCarModel carModelUpdate, ElectricityCar electricityCarUpdate, EleBindCarRecord eleBindCarRecord, CarLockCtrlHistory carLockCtrlHistory) {
        // 生成租赁订单
        carRentalOrderService.insert(carRentalOrderEntity);
        // 更改用户租赁状态
        userInfoService.updateByUid(userInfo);
        // 更改车辆型号的已租数量
        carModelService.updateById(carModelUpdate);
        // 更新车辆的归属人
        carService.updateCarBindStatusById(electricityCarUpdate);
        // 车辆操作记录
        eleBindCarRecordService.insert(eleBindCarRecord);

        if (ObjectUtils.isNotEmpty(carLockCtrlHistory)) {
            carLockCtrlHistoryService.insert(carLockCtrlHistory);
        }
    }

    /**
     * 构建JT808
     * @param electricityCar
     * @param userInfo
     * @return
     */
    private CarLockCtrlHistory buildCarLockCtrlHistory(ElectricityCar electricityCar, UserInfo userInfo, Integer rentalType) {
        // 是否开通JT808
        ElectricityConfig electricityConfig = electricityConfigService
                .queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (Objects.nonNull(electricityConfig) && Objects
                .equals(electricityConfig.getIsOpenCarControl(), ElectricityConfig.ENABLE_CAR_CONTROL)) {

            // 调用 JT808 服务
            boolean result = false;
            if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
                result = retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_UN_LOCK, 3);
            }
            if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
                result = retryCarLockCtrl(electricityCar.getSn(), ElectricityCar.TYPE_LOCK, 3);
            }

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
            carLockCtrlHistory.setCreateTime(System.currentTimeMillis());
            carLockCtrlHistory.setUpdateTime(System.currentTimeMillis());
            carLockCtrlHistory.setTenantId(TenantContextHolder.getTenantId());
            if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
                carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_BIND_USER_UN_LOCK);
            }
            if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
                carLockCtrlHistory.setType(CarLockCtrlHistory.TYPE_UN_BIND_USER_LOCK);
            }

            return carLockCtrlHistory;
        }
        return null;
    }

    /**
     * 构建车辆操作记录
     * @param electricityCar 车辆信息
     * @param userInfo 用户
     * @param user 操作用户
     * @param rentalType 操作类型
     * @return 车辆操作记录
     */
    private EleBindCarRecord buildEleBindCarRecord(ElectricityCar electricityCar, UserInfo userInfo, User user, Integer rentalType) {
        EleBindCarRecord eleBindCarRecord = EleBindCarRecord.builder()
                .carId(electricityCar.getId())
                .sn(electricityCar.getSn())
                .operateUser(user.getName())
                .model(electricityCar.getModel())
                .phone(userInfo.getPhone())
                .userName(userInfo.getName())
                .tenantId(TenantContextHolder.getTenantId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
            eleBindCarRecord.setStatus(EleBindCarRecord.BIND_CAR);
        }
        if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
            eleBindCarRecord.setStatus(EleBindCarRecord.NOT_BIND_CAR);
        }
        return eleBindCarRecord;
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
        carUpdate.setId(car.getId());
        carUpdate.setUpdateTime(System.currentTimeMillis());
        if (RentalTypeEnum.RENTAL.getCode().equals(rentalType)) {
            carUpdate.setUid(uid);
            carUpdate.setUserInfoId(userInfo.getId());
            carUpdate.setUserName(userInfo.getName());
            carUpdate.setPhone(userInfo.getPhone());
            carUpdate.setStatus(ElectricityCar.STATUS_IS_RENT);
        }
        if (RentalTypeEnum.RETURN.getCode().equals(rentalType)) {
            carUpdate.setUid(null);
            carUpdate.setUserInfoId(null);
            carUpdate.setUserName(null);
            carUpdate.setPhone(null);
            carUpdate.setStatus(ElectricityCar.STATUS_NOT_RENT);
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

    /**
     * JT 808 控制锁
     * @param sn
     * @param lockType
     * @param retryCount
     * @return
     */
    @Override
    public Boolean retryCarLockCtrl(String sn, Integer lockType, Integer retryCount) {
        if (Objects.isNull(retryCount)) {
            retryCount = 1;
        }

        retryCount = retryCount > 5 ? 5 : retryCount;

        for (int i = 0; i < retryCount; i++) {
            R<Jt808DeviceInfoVo> result = jt808RetrofitService
                    .controlDevice(new Jt808DeviceControlRequest(IdUtil.randomUUID(), sn, lockType));
            if (result.isSuccess()) {
                return true;
            }
            log.error("Jt808 error! controlDevice error! carSn is {}, result is {}, retryCount is {}", sn, result, i);
        }

        log.error("Jt808 error! controlDevice error! carSn is {}", sn);

        return false;
    }


}
