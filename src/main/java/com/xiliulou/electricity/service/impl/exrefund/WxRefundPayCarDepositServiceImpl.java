package com.xiliulou.electricity.service.impl.exrefund;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
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
    private UserInfoService userInfoService;

    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;

    @Resource
    private FreeDepositOrderService freeDepositOrderService;

    @Resource
    private PxzDepositService pxzDepositService;

    @Resource
    private PxzConfigService pxzConfigService;

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
            CarRentalPackageDepositRefundPO depositRefundEntity = carRentalPackageDepositRefundService.selectByOrderNo(outRefundNo);
            if (ObjectUtils.isEmpty(depositRefundEntity) || !RefundStateEnum.REFUNDING.getCode().equals(depositRefundEntity.getRefundState())) {
                log.error("WxRefundPayCarDepositService.process failed. t_car_rental_package_order_rent_refund not found or status wrong. orderNo is {}", outRefundNo);
                return;
            }

            if (RefundStateEnum.SUCCESS.getCode().equals(depositRefundEntity.getRefundState())) {
                log.error("WxRefundPayCarRentServiceImpl.process failed. t_car_rental_package_order_rent_refund processing completed. orderNo is {}", outRefundNo);
                return;
            }

            // 查询会员期限信息
            CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(depositRefundEntity.getTenantId(), depositRefundEntity.getUid());
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_member_term. uid is {}", depositRefundEntity.getUid());
                throw new BizException("300000", "数据有误");
            }

            // 押金缴纳订单编码
            String depositPayOrderNo = depositRefundEntity.getDepositPayOrderNo();
            CarRentalPackageDepositPayPO depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(depositPayOrderNo);
            if (ObjectUtils.isEmpty(depositPayEntity) || !PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
                log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_deposit_refund or status error. orderNo is {}", depositPayOrderNo);
                throw new BizException("300000", "数据有误");
            }

            // 微信退款状态
            Integer refundState = StringUtils.isNotBlank(callBackResource.getRefundStatus()) && Objects.equals(callBackResource.getRefundStatus(), "SUCCESS") ? RefundStateEnum.SUCCESS.getCode() : RefundStateEnum.FAILED.getCode();

            // 构建押金退款订单表更新实体信息
            CarRentalPackageDepositRefundPO depositRefundUpdateEntity = buildDepositRefundEntity(depositRefundEntity, refundState);

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
    private CarRentalPackageDepositRefundPO buildDepositRefundEntity(CarRentalPackageDepositRefundPO depositRefundEntity, Integer refundState) {
        CarRentalPackageDepositRefundPO depositRefundUpdateEntity = new CarRentalPackageDepositRefundPO();
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
    public void saveDepositRefundInfoTx(Integer refundState, CarRentalPackageDepositRefundPO depositRefundUpdateEntity, CarRentalPackageMemberTermPO memberTermUpdateEntity, CarRentalPackageDepositPayPO depositPayEntity) {
        // 更新退款订单的状态
        carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);

        if (RefundStateEnum.SUCCESS.getCode().equals(refundState)) {
            Integer payType = depositPayEntity.getPayType();
            // 线上
            if (PayTypeEnum.ON_LINE.getCode().equals(payType)) {
                // 作废所有的套餐购买订单（未使用、使用中）、
                carRentalPackageOrderService.refundDepositByUid(memberTermUpdateEntity.getTenantId(), memberTermUpdateEntity.getUid(), null);
                // 按照人+类型，作废保险
                insuranceUserInfoService.deleteByUidAndType(depositPayEntity.getUid(), depositPayEntity.getRentalPackageType());
                // 删除会员期限表信息
                carRentalPackageMemberTermService.delByUidAndTenantId(memberTermUpdateEntity.getTenantId(), memberTermUpdateEntity.getUid(), null);
                // TODO 清理user信息/解绑车辆/解绑电池
                /*UserInfo userInfo = new UserInfo();
                userInfo.setCarBatteryDepositStatus();
                userInfo.setCarDepositStatus();
                userInfoService.updateByUid(userInfo);*/
            }

        } else {
            // 失败，更新会员期限表信息
            carRentalPackageMemberTermService.updateStatusById(memberTermUpdateEntity.getId(), MemberTermStatusEnum.NORMAL.getCode(), null);
        }

    }


}
