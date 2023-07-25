package com.xiliulou.electricity.service.impl.car.biz;

import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageDepositBizService;
import com.xiliulou.electricity.service.car.biz.CarRenalPackageSlippageBizService;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
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
    private WechatConfig wechatConfig;

    @Resource
    private WechatV3JsapiService wechatV3JsapiService;

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
     * 运营商端创建退押
     *
     * @param optModel 租户ID
     * @return
     */
    @Override
    public boolean refundDepositCreate(CarRentalPackageDepositRefundOptModel optModel) {
        if (!ObjectUtils.allNotNull(optModel, optModel.getTenantId(), optModel.getUid(), optModel.getRealAmount(), optModel.getDepositPayOrderNo())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = optModel.getTenantId();
        Long uid = optModel.getUid();
        String depositPayOrderNo = optModel.getDepositPayOrderNo();
        BigDecimal realAmount = optModel.getRealAmount();

        // 查询会员期限信息
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isEmpty(memberTermEntity) || MemberTermStatusEnum.NORMAL.getCode().equals(memberTermEntity.getStatus())) {
            log.error("CarRenalPackageDepositBizService.checkRefundDeposit failed. car_rental_package_member_term not found or status is error. uid is {}", uid);
            throw new BizException("300000", "数据有误");
        }

        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.error("CarRenalPackageDepositBizService.refundDepositCreate failed. car_rental_package_deposit_pay not found or status is error. uid is {}, depositPayOrderNo is {}", uid, depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }

        // 退押检测
        checkRefundDeposit(tenantId, uid, memberTermEntity.getRentalPackageType(), depositPayOrderNo);

        Integer payType = depositPayEntity.getPayType();

        // 生成退押申请单
        CarRentalPackageDepositRefundPO refundDepositInsertEntity = budidCarRentalPackageOrderRentRefund(memberTermEntity, depositPayOrderNo,
                SystemDefinitionEnum.BACKGROUND, false, payType, realAmount);


        // 待审核
        if (RefundStateEnum.REFUNDING.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            // 实际退款0元，则直接成功，不调用退款接口
            if (BigDecimal.ZERO.compareTo(realAmount) == 0) {
                // 线上，退款中，先落库，在调用退款接口
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
            } else {
                // 线上，退款中，先落库，在调用退款接口
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, false);

                // 组装微信退款参数
                WechatV3RefundQuery wechatV3RefundQuery = new WechatV3RefundQuery();
                wechatV3RefundQuery.setTenantId(depositPayEntity.getTenantId());
                wechatV3RefundQuery.setReason("租车押金退款");
                wechatV3RefundQuery.setCurrency("CNY");
                wechatV3RefundQuery.setNotifyUrl(wechatConfig.getCarDepositRefundCallBackUrl() + depositPayEntity.getTenantId());
                wechatV3RefundQuery.setTotal(depositPayEntity.getDeposit().multiply(new BigDecimal(100)).intValue());
                wechatV3RefundQuery.setOrderId(depositPayEntity.getOrderNo());
                wechatV3RefundQuery.setRefund(realAmount.multiply(new BigDecimal(100)).intValue());
                wechatV3RefundQuery.setRefundId(refundDepositInsertEntity.getOrderNo());

                // 调用微信退款
                try {
                    wechatV3JsapiService.refund(wechatV3RefundQuery);
                } catch (WechatPayException e) {
                    log.error("CarRenalPackageDepositBizService.refundDepositCreate failed. ");
                }
            }
        } else if (RefundStateEnum.SUCCESS.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            // 成功，线下或者免押
            if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                // TODO 免押，先代扣，再解除授权，最后落库
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
            } else {
                // 线下：直接落库
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
            }
        }

        return true;
    }

    /**
     * 审批退还押金申请单
     *
     * @param refundDepositOrderNo 退押申请单
     * @param approveFlag          审批状态
     * @param apploveDesc          审批意见
     * @param apploveUid           审批人
     * @param refundAmount         退款金额
     * @return
     */
    @Override
    public boolean approveRefundDepositOrder(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, BigDecimal refundAmount) {
        if (ObjectUtils.allNotNull(refundDepositOrderNo, approveFlag, apploveUid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        CarRentalPackageDepositRefundPO depositRefundEntity = carRentalPackageDepositRefundService.selectByOrderNo(refundDepositOrderNo);
        if (ObjectUtils.isEmpty(depositRefundEntity) || !RefundStateEnum.PENDING_APPROVAL.getCode().equals(depositRefundEntity.getRefundState())) {
            log.error("approveRefundDepositOrder faild. not find car_rental_package_deposit_refund or status error. refundDepositOrderNo is {}", refundDepositOrderNo);
            throw new BizException("300000", "数据有误");
        }

        // TX 事务落库
        saveApproveRefundDepositOrderTx(refundDepositOrderNo, approveFlag, apploveDesc, apploveUid, depositRefundEntity, refundAmount);

        return true;
    }

    /**
     * 退押审批，TX事务处理
     * @param refundDepositOrderNo 退押申请订单号
     * @param approveFlag          审批状态
     * @param apploveDesc          审批意见
     * @param apploveUid           审批人
     * @param refundAmount         退款金额
     * @param depositRefundEntity         退押申请单信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveApproveRefundDepositOrderTx(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, CarRentalPackageDepositRefundPO depositRefundEntity, BigDecimal refundAmount) {

        CarRentalPackageDepositRefundPO depositRefundUpdateEntity = new CarRentalPackageDepositRefundPO();
        depositRefundUpdateEntity.setOrderNo(refundDepositOrderNo);
        depositRefundUpdateEntity.setAuditTime(System.currentTimeMillis());
        depositRefundUpdateEntity.setRemark(apploveDesc);
        depositRefundUpdateEntity.setUpdateUid(apploveUid);
        depositRefundUpdateEntity.setRealAmount(refundAmount);

        if (approveFlag) {
            // 1. 更新退押申请单
            depositRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_PASS.getCode());
            carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);

            // 2. 删除会员期限
            carRentalPackageMemberTermService.delByUidAndTenantId(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), apploveUid);
        } else {
            // 1. 更新退租申请单状态
            depositRefundUpdateEntity.setRefundState(RefundStateEnum.AUDIT_REJECT.getCode());
            carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);

            // 2. 更新会员期限
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(depositRefundEntity.getTenantId(), depositRefundEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), apploveUid);
        }
    }

    /**
     * C端退押申请
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
            throw new BizException("300000", "数据有误");
        }

        // 检测押金缴纳订单数据
        CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
        if (ObjectUtils.isEmpty(depositPayEntity) || PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
            log.error("CarRenalPackageDepositBizService.refundDeposit failed. car_rental_package_deposit_pay not found or status is error. uid is {}, depositPayOrderNo is {}", uid, depositPayOrderNo);
            throw new BizException("300000", "数据有误");
        }

        // 退押检测
        checkRefundDeposit(tenantId, uid, memberTermEntity.getRentalPackageType(), depositPayOrderNo);

        // 判定是否退押审核
        boolean depositAuditFlag = true;
        if (BigDecimal.ZERO.compareTo(memberTermEntity.getDeposit()) == 0) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
            Integer zeroDepositAuditEnabled = electricityConfig.getIsZeroDepositAuditEnabled();
            depositAuditFlag = ElectricityConfig.ENABLE_ZERO_DEPOSIT_AUDIT.equals(zeroDepositAuditEnabled) ? true : false;
        }

        Integer payType = depositPayEntity.getPayType();

        // 生成退押申请单
        CarRentalPackageDepositRefundPO refundDepositInsertEntity = budidCarRentalPackageOrderRentRefund(memberTermEntity, depositPayOrderNo, SystemDefinitionEnum.WX_APPLET,
                depositAuditFlag, payType, null);

        // 待审核
        if (RefundStateEnum.PENDING_APPROVAL.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, false);
        } else if (RefundStateEnum.SUCCESS.getCode().equals(refundDepositInsertEntity.getRefundState())) {
            // 免审，根据支付方式不一致，决定程序的先后执行顺序
            if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                // TODO 免押，先代扣，再解除授权，最后落库
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
            } else {
                // 线下：直接落库
                saveRefundDepositInfoTx(refundDepositInsertEntity, memberTermEntity, uid, true);
            }
        }

        return true;
    }

    /**
     * 退押申请事务处理
     * @param refundDepositInsertEntity 退押申请单数据
     * @param memberTermEntity 会员期限实体数据
     * @param optId 操作人ID
     * @param delFlag 删除标识
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRefundDepositInfoTx(CarRentalPackageDepositRefundPO refundDepositInsertEntity, CarRentalPackageMemberTermPO memberTermEntity, Long optId, boolean delFlag) {
        carRentalPackageDepositRefundService.insert(refundDepositInsertEntity);
        if (!delFlag) {
            // 处理状态
            carRentalPackageMemberTermService.updateStatusById(memberTermEntity.getId(), MemberTermStatusEnum.APPLY_REFUND_DEPOSIT.getCode(), optId);
        } else {
            // 直接删除
            carRentalPackageMemberTermService.delByUidAndTenantId(memberTermEntity.getTenantId(), memberTermEntity.getUid(), optId);
        }
    }

    /**
     * 构建退押申请单数据
     * @param memberTermEntity 会员期限信息
     * @param depositPayOrderNo 押金缴纳订单编码
     * @param systemDefinition 操作系统
     * @param depositAuditFlag 退押审批标识
     * @param depositAuditFlag 支付方式
     * @param depositAuditFlag 实际退款金额(可为空，后端操作不能为空)
     * @return
     */
    private CarRentalPackageDepositRefundPO budidCarRentalPackageOrderRentRefund(CarRentalPackageMemberTermPO memberTermEntity, String depositPayOrderNo, SystemDefinitionEnum systemDefinition,
                                                                                 boolean depositAuditFlag, Integer payType,  BigDecimal refundAmount) {
        CarRentalPackageDepositRefundPO refundDepositInsertEntity = new CarRentalPackageDepositRefundPO();
        refundDepositInsertEntity.setUid(memberTermEntity.getUid());
        refundDepositInsertEntity.setDepositPayOrderNo(depositPayOrderNo);
        refundDepositInsertEntity.setApplyAmount(memberTermEntity.getDeposit());
        refundDepositInsertEntity.setTenantId(memberTermEntity.getTenantId());
        refundDepositInsertEntity.setFranchiseeId(memberTermEntity.getFranchiseeId());
        refundDepositInsertEntity.setStoreId(memberTermEntity.getStoreId());
        refundDepositInsertEntity.setCreateUid(memberTermEntity.getUid());
        refundDepositInsertEntity.setPayType(payType);
        refundDepositInsertEntity.setRentalPackageType(memberTermEntity.getRentalPackageType());
        // 默认状态，待审核
        refundDepositInsertEntity.setRefundState(RefundStateEnum.PENDING_APPROVAL.getCode());

        // 设置退款状态
        if (SystemDefinitionEnum.BACKGROUND.getCode().equals(systemDefinition.getCode())) {
            refundDepositInsertEntity.setRealAmount(refundAmount);

            // 线上，退款中
            if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.REFUNDING.getCode());
            }
            // 线下，退款成功
            if (PayTypeEnum.OFF_LINE.getCode().equals(payType)) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
            }
            // 免押，退款成功
            if (PayTypeEnum.EXEMPT.getCode().equals(payType)) {
                refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
            }
        }

        if (SystemDefinitionEnum.WX_APPLET.getCode().equals(systemDefinition.getCode())) {
            // 不需要审核，必定是0元退押，所以直接退款成功即可
            if (!depositAuditFlag) {
                // 线上、线下、免押，退款成功
                refundDepositInsertEntity.setRefundState(RefundStateEnum.SUCCESS.getCode());
            }
        }

        return refundDepositInsertEntity;
    }

    /**
     * 退押检测
     * @param tenantId 租户ID
     * @param uid 用户ID
     */
    private void checkRefundDeposit(Integer tenantId, Long uid, Integer rentalPackageType, String depositPayOrderNo) {

        // 检测是否存在滞纳金
        if (carRenalPackageSlippageBizService.isExitUnpaid(tenantId, uid)) {
            log.info("CarRenalPackageDepositBizService.checkRefundDeposit, There is a Late fee, please pay first. uid is {}", uid);
            throw new BizException("300001", "存在滞纳金，请先缴纳");
        }

        // 查询设备(车辆)
        UserCar userCar = userCarService.selectByUidFromCache(uid);
        if (ObjectUtils.isEmpty(userCar) || StringUtils.isNotBlank(userCar.getSn())) {
            log.info("CarRenalPackageDepositBizService.checkRefundDeposit, There are vehicles that have not been returned. uid is {}", uid);
            throw new BizException("300018", "存在未归还的车辆");
        }

        // 车电一体，查询设备(电池)
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
            ElectricityBattery battery = electricityBatteryService.queryByUid(uid);
            if (ObjectUtils.isNotEmpty(battery)) {
                log.info("CarRenalPackageDepositBizService.checkRefundDeposit, There are unreturned batteries. uid is {}", uid);
                throw new BizException("300019", "存在未归还的电池");
            }
        }
    }
}
