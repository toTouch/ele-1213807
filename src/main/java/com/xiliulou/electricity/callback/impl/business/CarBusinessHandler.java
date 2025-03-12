package com.xiliulou.electricity.callback.impl.business;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.car.v2.CarRenalPackageDepositV2BizService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.xiliulou.electricity.constant.CacheConstant.CAR_FREE_DEPOSIT_USER_INFO_LOCK_KEY;
import static com.xiliulou.electricity.constant.CacheConstant.UN_FREE_DEPOSIT_USER_INFO_LOCK_KEY;

/**
 * <p>
 * Description: This class is CarBusinessHandler!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
@Slf4j
@Service
@AllArgsConstructor
public class CarBusinessHandler implements BusinessHandler {
    
    private final CarRentalPackageDepositPayService carRentalPackageDepositPayService;
    
    private final CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    private final UserInfoService userInfoService;
    
    private final UserBatteryDepositService userBatteryDepositService;
    
    private final CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;
    
    private final CarRentalPackageOrderService carRentalPackageOrderService;
    
    private final InsuranceUserInfoService insuranceUserInfoService;
    
    private final InsuranceOrderService insuranceOrderService;
    
    private final UserBizService userBizService;
    
    private final UserBatteryTypeService userBatteryTypeService;
    
    private final UserInfoGroupDetailService userInfoGroupDetailService;
    
    private final RedisService redisService;
    
    private final CarRenalPackageDepositV2BizService carRenalPackageDepositV2BizService;
    
    private final CarRentalPackageService carRentalPackageService;

    private final FreeDepositExpireRecordService freeDepositExpireRecordService;
    
    @Override
    public boolean support(Integer type) {
        return !Objects.equals(type, FreeDepositOrder.DEPOSIT_TYPE_BATTERY);
    }
    
    @Override
    public boolean freeDeposit(FreeDepositOrder order) {
        try {
            log.info("Enter the process of free deposit callback for car/car electronics, order number: {}",order.getOrderId());
            CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(order.getOrderId());
            Integer tenantId = depositPayEntity.getTenantId();
            Integer franchiseeId = depositPayEntity.getFranchiseeId();
            Integer storeId = depositPayEntity.getStoreId();
            Long uid = depositPayEntity.getUid();
            Integer rentalPackageType = depositPayEntity.getRentalPackageType();
            String depositPayOrderNo = depositPayEntity.getOrderNo();
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(depositPayEntity.getRentalPackageId());
            if (Objects.isNull(carRentalPackagePo)){
                log.warn("The car rental package information corresponding to the order number {} does not exist",depositPayOrderNo);
                return false;
            }
            CarRentalPackageMemberTermPo memberTermPo = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
            CarRentalPackageMemberTermPo memberTermPoInsertOrUpdate = carRenalPackageDepositV2BizService.buildCarRentalPackageMemberTerm(order.getTenantId(), order.getUid(),
                    carRentalPackagePo, order.getOrderId(), memberTermPo);
            memberTermPoInsertOrUpdate.setStatus(MemberTermStatusEnum.NORMAL.getCode());
            memberTermPoInsertOrUpdate.setUpdateUid(uid);
            
            // 2. 更新押金缴纳订单数据
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.SUCCESS.getCode());
            // 3. 更新租车会员信息状态
            if (Objects.isNull(memberTermPoInsertOrUpdate.getId())){
                carRentalPackageMemberTermService.insert(memberTermPoInsertOrUpdate);
            }else {
                carRentalPackageMemberTermService.updateById(memberTermPoInsertOrUpdate);
            }
//            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.NORMAL.getCode(), uid);
            // 4. 更新用户表押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            
            UserInfo userInfo = userInfoService.queryByUidFromDbIncludeDelUser(uid);
            Long boundFranchiseeId = userInfo.getFranchiseeId();
            if (Objects.isNull(boundFranchiseeId) || Objects.equals(boundFranchiseeId, NumberConstant.ZERO_L)) {
                userInfoUpdate.setFranchiseeId(Long.valueOf(franchiseeId));
            }
            
            Long boundStoreId = userInfo.getStoreId();
            if (Objects.isNull(boundStoreId) || Objects.equals(boundStoreId, NumberConstant.ZERO_L) || Objects.equals(boundFranchiseeId, Long.valueOf(franchiseeId))) {
                userInfoUpdate.setStoreId(Long.valueOf(storeId));
            }
            
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            if (RentalPackageTypeEnum.CAR.getCode().equals(rentalPackageType)) {
                userInfoUpdate.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            }
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
                userInfoUpdate.setCarBatteryDepositStatus(YesNoEnum.YES.getCode());
            }
            userInfoService.updateByUid(userInfoUpdate);
            // 车电一体，同步押金
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
                log.info("userBatteryDepositService.synchronizedUserBatteryDepositInfo. depositPayOrderNo is {}", depositPayEntity.getOrderNo());
                userBatteryDepositService.synchronizedUserBatteryDepositInfo(uid, null, depositPayEntity.getOrderNo(), depositPayEntity.getDeposit());
            }
            //删除5分钟的二维码
            String userKey = String.format(CacheConstant.FREE_DEPOSIT_USER_INFO_KEY, uid);
            String md5s = redisService.get(userKey);
            if (ObjectUtils.isNotEmpty(md5s)) {
                Arrays.stream(md5s.split(","))
                        .forEach(md5 -> {
                            // TODO: 2024/10/10 兼容历史数据 后续删除
                            String oldKey = String.format(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2_OLD, uid,md5);
                            if (redisService.hasKey(oldKey)) {
                                redisService.delete(oldKey);
                            }
                            
                            String key = String.format(CacheConstant.ELE_CACHE_CAR_RENTAL_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2, uid,md5);
                            if (redisService.hasKey(key)) {
                                redisService.delete(key);
                            }
                        });
                redisService.delete(userKey);
            }
            String freeSuccessKey = String.format(CAR_FREE_DEPOSIT_USER_INFO_LOCK_KEY,order.getTenantId() , uid);
            redisService.set(freeSuccessKey, order.getOrderId(), 1L, TimeUnit.DAYS);
            log.info("Car/car electronics order no deposit callback completed, order number: {}",order.getOrderId());
            return true;
        }catch (Exception e){
            log.error("freeDeposit callback failed.", e);
            return false;
        }
    }
    
    @Override
    public boolean unfree(FreeDepositOrder order) {
        if (!redisService.hasKey(String.format(UN_FREE_DEPOSIT_USER_INFO_LOCK_KEY, order.getOrderId()))){
            return false;
        }
        try {
            log.info("Enter the process of unfree deposit callback for car/car electronics, order number: {}",order.getOrderId());
            CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectLastByDepositPayOrderNo(order.getOrderId());
            // 更改押金状态
            CarRentalPackageDepositRefundPo depositRefundUpdateEntity = new CarRentalPackageDepositRefundPo();
            depositRefundUpdateEntity.setOrderNo(depositRefundEntity.getOrderNo());
            depositRefundUpdateEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
            depositRefundUpdateEntity.setUpdateTime(System.currentTimeMillis());
            carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);
            // 作废所有的套餐购买订单（未使用、使用中）、
            carRentalPackageOrderService.refundDepositByUid(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), null);
            // 查询用户保险
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(depositRefundEntity.getUid(), depositRefundEntity.getRentalPackageType());
            // 按照人+类型，作废保险
            insuranceUserInfoService.deleteByUidAndType(depositRefundEntity.getUid(), depositRefundEntity.getRentalPackageType());
            // 作废保险订单
            if (ObjectUtils.isNotEmpty(insuranceUserInfo)) {
                insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);

                InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(depositRefundEntity.getUid(), depositRefundEntity.getRentalPackageType(), InsuranceOrder.NOT_EFFECTIVE);
                if (Objects.nonNull(insuranceOrder)){
                    insuranceOrderService.updateUseStatusByOrderId(insuranceOrder.getOrderId(), InsuranceOrder.INVALID);
                }
            }
            // 删除会员期限表信息
            carRentalPackageMemberTermService.delByUidAndTenantId(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), null);
            // 清理user信息/解绑车辆/解绑电池
            userBizService.depositRefundUnbind(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), depositRefundEntity.getRentalPackageType());
            // 车电一体押金，同步删除电池那边的数据
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(depositRefundEntity.getRentalPackageType())) {
                log.info("saveFreeDepositRefundHandlerTx, delete from battery member info. depositPayOrderNo is {}", depositRefundEntity.getOrderNo());
                userBatteryTypeService.deleteByUid(depositRefundEntity.getUid());
                userBatteryDepositService.deleteByUid(depositRefundEntity.getUid());
            }

            freeDepositExpireRecordService.unfreeAfterDel(order.getOrderId());
            
            redisService.delete(String.format(UN_FREE_DEPOSIT_USER_INFO_LOCK_KEY, order.getOrderId()));
            log.info("Car/car electronics order no unfree deposit callback completed, order number: {}",order.getOrderId());
            return true;
        }catch (Exception e){
            log.error("unfree callback failed.", e);
            return false;
        }
    }
    
    @Override
    public boolean authPay(FreeDepositOrder order) {
        return true; //主要逻辑是处理freeDepositOrder表，已在上层处理，无需处理
    }
    
    @Override
    public void timeout(FreeDepositOrder order) {
        // 1. 更新押金缴纳订单数据
        carRentalPackageDepositPayService.updatePayStateByOrderNo(order.getOrderId(), PayStateEnum.FAILED.getCode());
        // 2. 删除会员期限表数据
        carRentalPackageMemberTermService.delByUidAndTenantId(order.getTenantId(),order.getUid(),order.getUid());
    }
}
