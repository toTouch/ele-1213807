package com.xiliulou.electricity.service.impl.exrefund;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositRefundPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.RefundStateEnum;
import com.xiliulou.electricity.enums.WxRefundPayOptTypeEnum;
import com.xiliulou.electricity.enums.WxRefundStatusEnum;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.wxrefund.WxRefundPayService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 微信退款-租车押金退款 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service("wxRefundPayCarDepositService")
public class WxRefundPayCarDepositServiceImpl implements WxRefundPayService {

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
        String outTradeNo = callBackResource.getOutTradeNo();
        String outRefundNo = callBackResource.getOutRefundNo();
        String refundStatus = callBackResource.getRefundStatus();

        String redisLockKey = WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + outTradeNo;

        try {
            if (!redisService.setNx(redisLockKey, outTradeNo, 10 * 1000L, false)) {
                return;
            }

            // 查询押金退款单信息
            CarRentalPackageDepositRefundPO depositRefundEntity = carRentalPackageDepositRefundService.selectByOrderNo(outRefundNo);
            if (ObjectUtils.isEmpty(depositRefundEntity) || !RefundStateEnum.REFUNDING.getCode().equals(depositRefundEntity.getRefundState())) {
                log.error("WxRefundPayCarDepositService.process failed. car_rental_package_order_rent_refund not found or status wrong. orderNo is {}", outRefundNo);
                return;
            }

            // 查询会员期限信息
            CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(depositRefundEntity.getTenantId(), depositRefundEntity.getUid());

            RefundStateEnum refundStateEnum = RefundStateEnum.SUCCESS;
            if (!WxRefundStatusEnum.SUCCESS.getCode().equals(refundStatus)) {
                refundStateEnum = RefundStateEnum.FAILED;
            }

            // 构建押金退款订单表更新实体信息
            CarRentalPackageDepositRefundPO depositRefundUpdateEntity = buildDepositRefundEntity(depositRefundEntity, refundStateEnum);

            saveDepositRefundInfoTx(refundStateEnum, depositRefundUpdateEntity, memberTermEntity);

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
     * @param refundStateEnum 退款状态
     * @return
     */
    private CarRentalPackageDepositRefundPO buildDepositRefundEntity(CarRentalPackageDepositRefundPO depositRefundEntity, RefundStateEnum refundStateEnum) {
        CarRentalPackageDepositRefundPO depositRefundUpdateEntity = new CarRentalPackageDepositRefundPO();
        depositRefundUpdateEntity.setOrderNo(depositRefundEntity.getOrderNo());
        depositRefundUpdateEntity.setRefundState(refundStateEnum.getCode());
        depositRefundUpdateEntity.setUpdateTime(System.currentTimeMillis());
        return depositRefundUpdateEntity;
    };

    /**
     * 退押之后的事务处理
     * @param refundStateEnum
     * @param depositRefundUpdateEntity
     * @param memberTermUpdateEntity
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDepositRefundInfoTx(RefundStateEnum refundStateEnum, CarRentalPackageDepositRefundPO depositRefundUpdateEntity, CarRentalPackageMemberTermPO memberTermUpdateEntity) {
        // 更新退款订单的状态
        carRentalPackageDepositRefundService.updateByOrderNo(depositRefundUpdateEntity);

        if (RefundStateEnum.SUCCESS.getCode().equals(refundStateEnum.getCode())) {
            // 成功，作废所有的套餐购买订单、删除会员期限表信息
            carRentalPackageOrderService.refundDepositByUid(memberTermUpdateEntity.getTenantId(), memberTermUpdateEntity.getUid(), null);
            carRentalPackageMemberTermService.delByUidAndTenantId(memberTermUpdateEntity.getTenantId(), memberTermUpdateEntity.getUid(), null);
        } else {
            // 失败，更新会员期限表信息
            carRentalPackageMemberTermService.updateStatusById(memberTermUpdateEntity.getId(), MemberTermStatusEnum.NORMAL.getCode(), null);
        }

    }
}
