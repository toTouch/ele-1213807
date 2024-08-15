/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/14
 */

package com.xiliulou.electricity.tx;

import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.mapper.car.CarRentalPackageDepositRefundMapper;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/14 18:44
 */
@Slf4j
@Service
public class CarRentalPackageDepositRefundTxService {
    
    
    @Resource
    private CarRentalPackageDepositRefundMapper carRentalPackageDepositRefundMapper;
    
    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;
    
    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;
    
    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Resource
    private InsuranceOrderService insuranceOrderService;
    
    @Resource
    private UserBizService userBizService;
    
    @Resource
    private UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    private UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    /**
     * 更新，挂起层事物
     *
     * @author caobotao.cbt
     * @date 2024/8/14 18:47
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean update(CarRentalPackageDepositRefundPo refundPo) {
        refundPo.setUpdateTime(System.currentTimeMillis());
    
        int num = carRentalPackageDepositRefundMapper.updateByOrderNo(refundPo);
    
        return num >= 0;
    }
    
    
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void saveRefundDepositInfo(CarRentalPackageDepositRefundPo refundDepositInsertEntity, CarRentalPackageMemberTermPo memberTermEntity, Long optId, boolean delFlag) {
        carRentalPackageDepositRefundService.insert(refundDepositInsertEntity);
        if (!delFlag) {
            // 处理状态
            carRentalPackageMemberTermService.updateStatusById(memberTermEntity.getId(), MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode(), optId);
        } else {
            // 作废所有的套餐购买订单（未使用、使用中）
            carRentalPackageOrderService.refundDepositByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid(), optId);
            // 查询用户保险
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(memberTermEntity.getUid(), memberTermEntity.getRentalPackageType());
            // 按照人+类型，作废用户保险
            insuranceUserInfoService.deleteByUidAndType(memberTermEntity.getUid(), memberTermEntity.getRentalPackageType());
            // 作废保险订单
            if (ObjectUtils.isNotEmpty(insuranceUserInfo)) {
                insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
            }
            // 删除会员期限表信息
            carRentalPackageMemberTermService.delByUidAndTenantId(memberTermEntity.getTenantId(), memberTermEntity.getUid(), optId);
            // 清理user信息/解绑车辆/解绑电池
            userBizService.depositRefundUnbind(memberTermEntity.getTenantId(), memberTermEntity.getUid(), memberTermEntity.getRentalPackageType());
            // 车电一体押金，同步删除电池那边的数据
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(memberTermEntity.getRentalPackageType())) {
                log.info("saveRefundDepositInfoTx, delete from battery member info. depositPayOrderNo is {}", memberTermEntity.getDepositPayOrderNo());
                userBatteryTypeService.deleteByUid(memberTermEntity.getUid());
                userBatteryDepositService.deleteByUid(memberTermEntity.getUid());
            }
        
            // 删除用户分组
            userInfoGroupDetailService.handleAfterRefundDeposit(memberTermEntity.getUid());
        }
    }
    
}
