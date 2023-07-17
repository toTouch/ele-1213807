package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 租车套餐押金业务聚合 BizServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRenalPackageDepositBizServiceImpl implements CarRenalPackageDepositBizService {


    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;

    @Resource
    private ElectricityConfigService electricityConfigService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private ElectricityBatteryService electricityBatteryService;

    @Resource
    private UserCarService userCarService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRenalPackageSlippageBizService carRenalPackageSlippageBizService;

    /**
     * 退押申请
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param depositPayOrderNo 押金缴纳支付订单编码
     * @return
     */
    @Override
    public boolean refundDeposit(Integer tenantId, Long uid, String depositPayOrderNo) {
        if (!ObjectUtils.allNotNull(tenantId, uid, depositPayOrderNo)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 查询会员期限信息
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRenalPackageDepositBizService.checkRefundDeposit failed. car_rental_package_member_term not found or status is error. uid is {}", uid);
            // TODO 错误编码
            throw new BizException("", "数据有误");
        }

        // 退押检测
        checkRefundDeposit(tenantId, uid, memberTermEntity.getRentalPackageType(), depositPayOrderNo);

        // 判定是否0元退押审核
        boolean zeroDepositAuditFlag = false;
        if (BigDecimal.ZERO.compareTo(memberTermEntity.getDeposit()) == 0) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            Integer zeroDepositAuditEnabled = electricityConfig.getIsZeroDepositAuditEnabled();
            zeroDepositAuditFlag = ElectricityConfig.ENABLE_ZERO_DEPOSIT_AUDIT.equals(zeroDepositAuditEnabled) ? true : false;
        }

        // 生成退押申请单
        CarRentalPackageDepositRefundPO refundDepositInsertEntity = budidCarRentalPackageOrderRentRefund(memberTermEntity, depositPayOrderNo, SystemDefinitionEnum.WX_APPLET, zeroDepositAuditFlag);

        // TX 事务处理
        saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid);

        return true;
    }

    /**
     * 退押申请事务处理
     * @param refundDepositInsertEntity
     * @param memberTermEntity
     * @param optId
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRefundDepositInfoTx(CarRentalPackageDepositRefundPO refundDepositInsertEntity, CarRentalPackageMemberTermPO memberTermEntity, Long optId) {
        carRentalPackageDepositRefundService.insert(refundDepositInsertEntity);
        carRentalPackageMemberTermService.updateStatusById(memberTermEntity.getId(), MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode(), optId);

    }

    /**
     * 构建退押申请单数据
     * @param memberTermEntity
     * @param depositPayOrderNo
     * @param systemDefinition
     * @param zeroDepositAuditFlag
     * @return
     */
    private CarRentalPackageDepositRefundPO budidCarRentalPackageOrderRentRefund(CarRentalPackageMemberTermPO memberTermEntity, String depositPayOrderNo, SystemDefinitionEnum systemDefinition, boolean zeroDepositAuditFlag) {
        CarRentalPackageDepositRefundPO refundDepositInsertEntity = new CarRentalPackageDepositRefundPO();
        refundDepositInsertEntity.setUid(memberTermEntity.getUid());
        refundDepositInsertEntity.setDepositPayOrderNo(depositPayOrderNo);
        refundDepositInsertEntity.setApplyAmount(memberTermEntity.getDeposit());

        if (SystemDefinitionEnum.BACKGROUND.getCode().equals(systemDefinition.getCode())) {
            refundDepositInsertEntity.setPayType(PayTypeEnum.OFF_LINE.getCode());
            refundDepositInsertEntity.setRefundState(RefundStateEnum.AUDIT_PASS.getCode());
        } else {
            refundDepositInsertEntity.setPayType(PayTypeEnum.ON_LINE.getCode());
            if (zeroDepositAuditFlag) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.PENDING_APPROVAL.getCode());
            }
            refundDepositInsertEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
        }

        refundDepositInsertEntity.setTenantId(memberTermEntity.getTenantId());
        refundDepositInsertEntity.setFranchiseeId(memberTermEntity.getFranchiseeId());
        refundDepositInsertEntity.setStoreId(memberTermEntity.getStoreId());
        refundDepositInsertEntity.setCreateUid(memberTermEntity.getUid());

        return refundDepositInsertEntity;
    }

    /**
     * 退押检测
     * @param tenantId 租户ID
     * @param uid 用户ID
     */
    private void checkRefundDeposit(Integer tenantId, Long uid, Integer rentalPackageType, String depositPayOrderNo) {

        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState()) || YesNoEnum.YES.getCode().equals(depositPayEntity.getRefundFlag())) {
            log.error("CarRenalPackageDepositBizService.checkRefundDeposit failed. car_rental_package_deposit_pay not found or status or refundFlag is error. uid is {},  depositPayOrderNo is {}", uid, depositPayOrderNo);
            // TODO 错误编码
            throw new BizException("", "数据有误");
        }

        // 检测是否存在滞纳金
        if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
            // TODO 错误编码
            throw new BizException("", "名下存在未缴纳滞纳金，不允许退押");
        }

        // 查询设备(车辆)
        UserCar userCar = userCarService.selectByUidFromCache(uid);
        if (ObjectUtils.isEmpty(userCar) || StringUtils.isNotBlank(userCar.getSn())) {
            // TODO 错误编码
            throw new BizException("", "名下存在未归还车辆设备，不允许退押");
        }

        // 车电一体，查询设备(电池)
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            ElectricityBattery battery = electricityBatteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                // TODO 错误编码
                throw new BizException("", "名下存在未归还电池设备，不允许退押");
            }
        }
    }
}
