package com.xiliulou.electricity.service.impl.exrefund;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPO;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPO;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderRentRefundService;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
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
 * 微信退款-租车租金退款 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service("wxRefundPayCarRentServiceImpl")
public class WxRefundPayCarRentServiceImpl implements WxRefundPayService {

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;

    @Resource
    private CarRentalPackageOrderRentRefundService carRentalPackageOrderRentRefundService;

    @Resource
    private RedisService redisService;

    /**
     * 执行方法
     *
     * @param callBackResource
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(WechatJsapiRefundOrderCallBackResource callBackResource) {
        log.info("WxRefundPayCarRentServiceImpl.process params is {}", JsonUtil.toJson(callBackResource));
        String refundOrderNo = callBackResource.getOutTradeNo();
        String cacheKey = WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + refundOrderNo;

        if (!redisService.setNx(cacheKey, String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            return;
        }

        // 退租订单信息
        CarRentalPackageOrderRentRefundPO rentRefundEntity = carRentalPackageOrderRentRefundService.selectByOrderNo(refundOrderNo);
        if (ObjectUtils.isEmpty(rentRefundEntity)) {
            log.error("WxRefundPayCarRentServiceImpl.process failed. not found t_car_rental_package_order_rent_refund. refundOrderNo is {}", refundOrderNo);
            return;
        }

        if (RefundStateEnum.SUCCESS.getCode().equals(rentRefundEntity.getRefundState())) {
            log.error("WxRefundPayCarRentServiceImpl.process failed. t_car_rental_package_order_rent_refund processing completed. refundOrderNo is {}", refundOrderNo);
            return;
        }

        // 租车会员信息
        CarRentalPackageMemberTermPO memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(rentRefundEntity.getTenantId(), rentRefundEntity.getUid());
        if (ObjectUtils.isEmpty(memberTermEntity)) {
            log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_member_term. uid is {}", rentRefundEntity.getUid());
            throw new BizException("300000", "数据有误");
        }

        // 购买套餐编码
        String orderNo = rentRefundEntity.getRentalPackageOrderNo();
        CarRentalPackageOrderPO packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
        if (ObjectUtils.isEmpty(packageOrderEntity) || UseStateEnum.EXPIRED.getCode().equals(packageOrderEntity.getUseState()) || UseStateEnum.RETURNED.getCode().equals(packageOrderEntity.getUseState())) {
            log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_order or status error. orderNo is {}", orderNo);
            throw new BizException("300000", "数据有误");
        }

        // 微信退款状态
        Integer refundState = StringUtils.isNotBlank(callBackResource.getRefundStatus()) && Objects.equals(callBackResource.getRefundStatus(), "SUCCESS") ? RefundStateEnum.SUCCESS.getCode() : RefundStateEnum.FAILED.getCode();

        // 更新退款单数据
        CarRentalPackageOrderRentRefundPO rentRefundUpdate = new CarRentalPackageOrderRentRefundPO();
        rentRefundUpdate.setOrderNo(refundOrderNo);
        rentRefundUpdate.setRefundState(refundState);
        rentRefundUpdate.setUpdateTime(System.currentTimeMillis());

        if (RefundStateEnum.SUCCESS.getCode().equals(refundState)) {
            // 是否退掉最后一个订单
            boolean isLastOrder = false;
            if (orderNo.equals(memberTermEntity.getRentalPackageOrderNo())) {
                isLastOrder = true;
            }

            // 处理租车会员期限信息
            if (isLastOrder) {
                carRentalPackageMemberTermService.rentRefundByUidAndPackageOrderNo(rentRefundEntity.getTenantId(), rentRefundEntity.getUid(), memberTermEntity.getRentalPackageOrderNo(), null);
            } else {
                // 计算总到期时间
                Integer tenancy = packageOrderEntity.getTenancy();
                Integer tenancyUnit = packageOrderEntity.getTenancyUnit();
                long dueTimeTotal = memberTermEntity.getDueTimeTotal();
                if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                    dueTimeTotal = dueTimeTotal - (tenancy * TimeConstant.DAY_MILLISECOND);
                }
                if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                    dueTimeTotal = dueTimeTotal - (tenancy * 1000);
                }

                // 更新数据
                CarRentalPackageMemberTermPO entityModify = new CarRentalPackageMemberTermPO();
                entityModify.setDueTimeTotal(dueTimeTotal);
                entityModify.setId(memberTermEntity.getId());
                entityModify.setUpdateTime(System.currentTimeMillis());
                carRentalPackageMemberTermService.updateById(entityModify);

                // 3. 更新购买订单状态
                carRentalPackageOrderService.updateUseStateById(packageOrderEntity.getId(), UseStateEnum.RETURNED.getCode(), null);
            }

            // 4. TODO 异步处理分账、活动

        } else {
            // 2. 更新会员期限
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(rentRefundEntity.getTenantId(), rentRefundEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), null);
        }

        // 更新退款单信息
        carRentalPackageOrderRentRefundService.updateByOrderNo(rentRefundUpdate);

    }

    /**
     * 获取操作类型
     *
     * @return
     */
    @Override
    public String getOptType() {
        return WxRefundPayOptTypeEnum.CAR_RENT_REFUND_CALL_BACK.getCode();
    }
}
