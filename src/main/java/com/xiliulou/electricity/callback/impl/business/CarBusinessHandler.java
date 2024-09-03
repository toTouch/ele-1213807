package com.xiliulou.electricity.callback.impl.business;


import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
    
    @Override
    public boolean support(Integer type) {
        return !Objects.equals(type, FreeDepositOrder.DEPOSIT_TYPE_BATTERY);
    }
    
    @Override
    public boolean freeDeposit(FreeDepositOrder order) {
        CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(order.getOrderId());
        Integer tenantId = depositPayEntity.getTenantId();
        Integer franchiseeId = depositPayEntity.getFranchiseeId();
        Integer storeId = depositPayEntity.getStoreId();
        Long uid = depositPayEntity.getUid();
        Integer rentalPackageType = depositPayEntity.getRentalPackageType();
        String depositPayOrderNo = depositPayEntity.getOrderNo();
        
        // 2. 更新押金缴纳订单数据
        carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.SUCCESS.getCode());
        // 3. 更新租车会员信息状态
        carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.NORMAL.getCode(), uid);
        // 4. 更新用户表押金状态
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(uid);
        
        UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
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
            log.info("saveFreeDepositSuccessTx, userBatteryDepositService.synchronizedUserBatteryDepositInfo. depositPayOrderNo is {}", depositPayEntity.getOrderNo());
            userBatteryDepositService.synchronizedUserBatteryDepositInfo(uid, null, depositPayEntity.getOrderNo(), depositPayEntity.getDeposit());
        }
        return true;
    }
    
    @Override
    public boolean unfree(FreeDepositOrder order) {
        return false;
    }
    
    @Override
    public boolean authPay(FreeDepositOrder order) {
        return false;
    }
    
    @Override
    public void timeout(FreeDepositOrder order) {
        // 1. 更新押金缴纳订单数据
        carRentalPackageDepositPayService.updatePayStateByOrderNo(order.getOrderId(), PayStateEnum.FAILED.getCode());
        // 2. 删除会员期限表数据
        carRentalPackageMemberTermService.delByUidAndTenantId(order.getTenantId(),order.getUid(),order.getUid());
    }
}
