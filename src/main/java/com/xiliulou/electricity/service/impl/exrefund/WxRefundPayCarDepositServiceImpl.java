package com.xiliulou.electricity.service.impl.exrefund;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.user.biz.UserBizService;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 微信退款-租车押金退款 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service("wxRefundPayCarDepositServiceImpl")
public class WxRefundPayCarDepositServiceImpl implements WxRefundPayService {

    @Resource
    private UserBatteryDepositService userBatteryDepositService;

    @Resource
    private UserBatteryTypeService userBatteryTypeService;

    @Resource
    private InsuranceOrderService insuranceOrderService;

    @Resource
    private UserBizService userBizService;

    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;

    @Resource
    private CarRentalPackageDepositPayService carRentalPackageDepositPayService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRentalPackageDepositRefundService carRentalPackageDepositRefundService;

    @Resource
    private RedisService redisService;

    /**
     * 执行方法
     * @param callBackResource
     */
    @Override
    public void process(WechatJsapiRefundOrderCallBackResource callBackResource) {
        log.info("WxRefundPayCarDepositServiceImpl.process params is {}", JsonUtil.toJson(callBackResource));
        String outRefundNo = callBackResource.getOutRefundNo();
        String redisLockKey = WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + outRefundNo;

        try {
            if (!redisService.setNx(redisLockKey, outRefundNo, 10 * 1000L, false)) {
                return;
            }

            // 押金退款单信息
            CarRentalPackageDepositRefundPo depositRefundEntity = carRentalPackageDepositRefundService.selectByOrderNo(outRefundNo);
            if (ObjectUtils.isEmpty(depositRefundEntity) || !RefundStateEnum.REFUNDING.getCode().equals(depositRefundEntity.getRefundState())) {
                log.error("WxRefundPayCarDepositService.process failed. t_car_rental_package_order_rent_refund not found or status wrong. orderNo is {}", outRefundNo);
                return;
            }

            if (RefundStateEnum.SUCCESS.getCode().equals(depositRefundEntity.getRefundState())) {
                log.error("WxRefundPayCarRentServiceImpl.process failed. t_car_rental_package_order_rent_refund processing completed. orderNo is {}", outRefundNo);
                return;
            }

            // 查询会员期限信息
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(depositRefundEntity.getTenantId(), depositRefundEntity.getUid());
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_member_term. uid is {}", depositRefundEntity.getUid());
                throw new BizException("300000", "数据有误");
            }

            // 押金缴纳订单编码
            String depositPayOrderNo = depositRefundEntity.getDepositPayOrderNo();
            CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
            if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
                log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_deposit_refund or status error. orderNo is {}", depositPayOrderNo);
                throw new BizException("300000", "数据有误");
            }

            // 微信退款状态
            Integer refundState = StringUtils.isNotBlank(callBackResource.getRefundStatus()) && Objects.equals(callBackResource.getRefundStatus(), "SUCCESS") ? RefundStateEnum.SUCCESS.getCode() : RefundStateEnum.FAILED.getCode();

            // 构建押金退款订单表更新实体信息
            CarRentalPackageDepositRefundPo depositRefundUpdateEntity = buildDepositRefundEntity(depositRefundEntity, refundState);

            // 事务处理
            saveDepositRefundInfoTx(refundState, depositRefundUpdateEntity, memberTermEntity, depositPayEntity);

        } catch (Exception e) {
            log.error("WxRefundPayCarDepositService.process failed. ", e);
        } finally {
            redisService.delete(redisLockKey);
        }
    }

    /**
     * 获取操作类型
     *
     * @return
     */
    @Override
    public String getOptType() {
        return WxRefundPayOptTypeEnum.CAR_DEPOSIT_REFUND_CALL_BACK.getCode();
    }

    /**
     * 构建押金退款订单表更新实体信息
     * @param depositRefundEntity 押金退款原始DB信息
     * @param refundState 退款状态
     * @return
     */
    private CarRentalPackageDepositRefundPo buildDepositRefundEntity(CarRentalPackageDepositRefundPo depositRefundEntity, Integer refundState) {
        CarRentalPackageDepositRefundPo depositRefundUpdateEntity = new CarRentalPackageDepositRefundPo();
        depositRefundUpdateEntity.setOrderNo(depositRefundEntity.getOrderNo());
        depositRefundUpdateEntity.setRefundState(refundState);
        depositRefundUpdateEntity.setUpdateTime(System.currentTimeMillis());
        return depositRefundUpdateEntity;
    };

    /**
     * 退押之后的事务处理
     * @param refundState 退款状态
     * @param depositRefundUpdateEntity 押金退款信息
     * @param memberTermUpdateEntity 会员期限信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDepositRefundInfoTx(Integer refundState, CarRentalPackageDepositRefundPo depositRefundUpdateEntity, CarRentalPackageMemberTermPo memberTermUpdateEntity, CarRentalPackageDepositPayPo depositPayEntity) {
        // 更新退款订单的状态
        carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);

        if (RefundStateEnum.SUCCESS.getCode().equals(refundState)) {
            Integer payType = depositPayEntity.getPayType();
            // 线上
            if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                // 作废所有的套餐购买订单（未使用、使用中）、
                carRentalPackageOrderService.refundDepositByUid(memberTermUpdateEntity.getTenantId(), memberTermUpdateEntity.getUid(), null);
                // 查询用户保险
                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(memberTermUpdateEntity.getUid(), memberTermUpdateEntity.getRentalPackageType());
                // 按照人+类型，作废保险
                insuranceUserInfoService.deleteByUidAndType(depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                // 删除会员期限表信息
                carRentalPackageMemberTermService.delByUidAndTenantId(memberTermUpdateEntity.getTenantId(), memberTermUpdateEntity.getUid(), null);
                // 作废保险订单
                if (ObjectUtils.isNotEmpty(insuranceUserInfo)) {
                    insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
                }
                // 清理user信息/解绑车辆/解绑电池
                userBizService.depositRefundUnbind(depositPayEntity.getTenantId(), depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                // 车电一体押金，同步删除电池那边的数据
                if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(depositPayEntity.getRentalPackageType())) {
                    log.info("saveDepositRefundInfoTx, delete from battery member info. depositPayOrderNo is {}", depositPayEntity.getOrderNo());
                    userBatteryTypeService.deleteByUid(depositPayEntity.getUid());
                    userBatteryDepositService.deleteByUid(depositPayEntity.getUid());
                }
            }

        } else {
            // 失败，更新会员期限表信息
            carRentalPackageMemberTermService.updateStatusById(memberTermUpdateEntity.getId(), MemberTermStatusEnum.NORMAL.getCode(), null);
        }

    }


}
