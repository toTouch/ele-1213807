package com.xiliulou.electricity.service.impl.exrefund;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.dto.DivisionAccountOrderDTO;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderPo;
import com.xiliulou.electricity.entity.car.CarRentalPackageOrderRentRefundPo;
import com.xiliulou.electricity.enums.*;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.service.DivisionAccountRecordService;
import com.xiliulou.electricity.service.UserCouponService;
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
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.UUID;

/**
 * 微信退款-租车租金退款 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Slf4j
@Service("wxRefundPayCarRentServiceImpl")
public class WxRefundPayCarRentServiceImpl implements WxRefundPayService {

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private DivisionAccountRecordService divisionAccountRecordService;

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
        String outRefundNo = callBackResource.getOutRefundNo();
        String redisLockKey = WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + outRefundNo;

        if (!redisService.setNx(redisLockKey, outRefundNo, 10 * 1000L, false)) {
            return;
        }

        try {

            // 退租订单信息
            CarRentalPackageOrderRentRefundPo rentRefundEntity = carRentalPackageOrderRentRefundService.selectByOrderNo(outRefundNo);
            if (ObjectUtils.isEmpty(rentRefundEntity)) {
                log.error("WxRefundPayCarRentServiceImpl.process failed. not found t_car_rental_package_order_rent_refund. refundOrderNo is {}", outRefundNo);
                return;
            }

            if (RefundStateEnum.SUCCESS.getCode().equals(rentRefundEntity.getRefundState())) {
                log.error("WxRefundPayCarRentServiceImpl.process failed. t_car_rental_package_order_rent_refund processing completed. refundOrderNo is {}", outRefundNo);
                return;
            }

            // 租车会员信息
            CarRentalPackageMemberTermPo memberTermEntity = carRentalPackageMemberTermService.selectByTenantIdAndUid(rentRefundEntity.getTenantId(), rentRefundEntity.getUid());
            if (ObjectUtils.isEmpty(memberTermEntity)) {
                log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_member_term. uid is {}", rentRefundEntity.getUid());
                throw new BizException("300000", "数据有误");
            }

            // 购买套餐编码
            String orderNo = rentRefundEntity.getRentalPackageOrderNo();
            CarRentalPackageOrderPo packageOrderEntity = carRentalPackageOrderService.selectByOrderNo(orderNo);
            if (ObjectUtils.isEmpty(packageOrderEntity) || UseStateEnum.EXPIRED.getCode().equals(packageOrderEntity.getUseState()) || UseStateEnum.RETURNED.getCode().equals(packageOrderEntity.getUseState())) {
                log.error("WxRefundPayCarRentServiceImpl faild. not find t_car_rental_package_order or status error. orderNo is {}", orderNo);
                throw new BizException("300000", "数据有误");
            }

            // 微信退款状态
            Integer refundState = StringUtils.isNotBlank(callBackResource.getRefundStatus()) && Objects.equals(callBackResource.getRefundStatus(), "SUCCESS") ? RefundStateEnum.SUCCESS.getCode() : RefundStateEnum.FAILED.getCode();

            long nowTime = System.currentTimeMillis();
            // 更新退款单数据
            CarRentalPackageOrderRentRefundPo rentRefundUpdate = new CarRentalPackageOrderRentRefundPo();
            rentRefundUpdate.setOrderNo(outRefundNo);
            rentRefundUpdate.setRefundState(refundState);
            rentRefundUpdate.setUpdateTime(nowTime);

            if (RefundStateEnum.SUCCESS.getCode().equals(refundState)) {
                // 是否退掉最后一个订单
                boolean isLastOrder = false;
                // 第一条未使用的支付成功的订单信息
                CarRentalPackageOrderPo packageOrderUnUseEntity = null;
                if (orderNo.equals(memberTermEntity.getRentalPackageOrderNo())) {
                    // 二次判定，根据用户ID查询第一条未使用的支付成功的订单信息
                    packageOrderUnUseEntity = carRentalPackageOrderService.selectFirstUnUsedAndPaySuccessByUid(memberTermEntity.getTenantId(), memberTermEntity.getUid());
                    if (ObjectUtils.isNotEmpty(packageOrderUnUseEntity)) {
                        if (packageOrderUnUseEntity.getOrderNo().equals(orderNo)) {
                            isLastOrder = true;
                        }
                    } else {
                        isLastOrder = true;
                    }
                }

                // 处理租车会员期限信息
                if (isLastOrder) {
                    carRentalPackageMemberTermService.rentRefundByUidAndPackageOrderNo(rentRefundEntity.getTenantId(), rentRefundEntity.getUid(), memberTermEntity.getRentalPackageOrderNo(), null);
                } else {
                    if (ObjectUtils.isNotEmpty(packageOrderUnUseEntity)) {
                        // 2. 更新会员状态表
                        // 剩余的时间
                        Long tenancyResidue = rentRefundEntity.getTenancyResidue();
                        Integer tenancyResidueUnit = rentRefundEntity.getTenancyResidueUnit();
                        Long diffTenancyResidue = null;
                        if (RentalUnitEnum.DAY.getCode().equals(tenancyResidueUnit)) {
                            diffTenancyResidue = tenancyResidue * TimeConstant.DAY_MILLISECOND;
                        }
                        if (RentalUnitEnum.MINUTE.getCode().equals(tenancyResidueUnit)) {
                            diffTenancyResidue = tenancyResidue * TimeConstant.MINUTE_MILLISECOND;
                        }

                        // 计算总到期时间, 重新计算，不能获取
                        Long dueTimeTotal = carRentalPackageOrderService.dueTimeTotal(rentRefundEntity.getTenantId(), rentRefundEntity.getUid());
                        if (ObjectUtils.isEmpty(dueTimeTotal)) {
                            log.error("WxRefundPayCarRentServiceImpl.process failed. dueTimeTotal is wrong. refundOrderNo is {}", outRefundNo);
                            return;
                        }

                        // 计算当前时间
                        Integer tenancy = packageOrderUnUseEntity.getTenancy();
                        Integer tenancyUnit = packageOrderUnUseEntity.getTenancyUnit();
                        Long dueTime = nowTime;
                        if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                            dueTime = dueTime + (tenancy * TimeConstant.DAY_MILLISECOND);
                        }
                        if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                            dueTime = dueTime + (tenancy * TimeConstant.MINUTE_MILLISECOND);
                        }

                        // 更新数据
                        CarRentalPackageMemberTermPo entityModify = new CarRentalPackageMemberTermPo();
                        entityModify.setId(memberTermEntity.getId());
                        entityModify.setDueTime(dueTime);
                        entityModify.setDueTimeTotal(nowTime + dueTimeTotal);
                        entityModify.setUpdateTime(nowTime);
                        entityModify.setRentalPackageId(packageOrderUnUseEntity.getRentalPackageId());
                        entityModify.setRentalPackageOrderNo(packageOrderUnUseEntity.getOrderNo());
                        entityModify.setRentalPackageType(packageOrderUnUseEntity.getRentalPackageType());
                        entityModify.setRentalPackageConfine(packageOrderUnUseEntity.getConfine());
                        entityModify.setStatus(MemberTermStatusEnum.NORMAL.getCode());
                        if (RenalPackageConfineEnum.NUMBER.getCode().equals(packageOrderUnUseEntity.getConfine()) && memberTermEntity.getResidue() <= 0L) {
                            entityModify.setResidue(packageOrderUnUseEntity.getConfineNum() + memberTermEntity.getResidue());
                        } else {
                            entityModify.setResidue(packageOrderUnUseEntity.getConfineNum());
                        }
                        carRentalPackageMemberTermService.updateById(entityModify);
                        // 1. 置为使用中
                        carRentalPackageOrderService.updateUseStateByOrderNo(packageOrderUnUseEntity.getOrderNo(), UseStateEnum.IN_USE.getCode(), null, nowTime);
                        // 2. 旧的置为已失效
                        carRentalPackageOrderService.updateUseStateByOrderNo(orderNo, UseStateEnum.RETURNED.getCode(), null, null);
                    } else {
                        // 计算总到期时间
                        Integer tenancy = packageOrderEntity.getTenancy();
                        Integer tenancyUnit = packageOrderEntity.getTenancyUnit();
                        long dueTimeTotal = memberTermEntity.getDueTimeTotal();
                        if (RentalUnitEnum.DAY.getCode().equals(tenancyUnit)) {
                            dueTimeTotal = dueTimeTotal - (tenancy * TimeConstant.DAY_MILLISECOND);
                        }
                        if (RentalUnitEnum.MINUTE.getCode().equals(tenancyUnit)) {
                            dueTimeTotal = dueTimeTotal - (tenancy * TimeConstant.MINUTE_MILLISECOND);
                        }

                        // 更新数据
                        CarRentalPackageMemberTermPo entityModify = new CarRentalPackageMemberTermPo();
                        entityModify.setDueTimeTotal(dueTimeTotal);
                        entityModify.setId(memberTermEntity.getId());
                        entityModify.setUpdateTime(nowTime);
                        carRentalPackageMemberTermService.updateById(entityModify);
                    }
                }

                // 3. 更新购买订单状态
                carRentalPackageOrderService.updateUseStateById(packageOrderEntity.getId(), UseStateEnum.RETURNED.getCode(), null);

                // 4. 作废掉赠送给用户的优惠券
                userCouponService.cancelBySourceOrderIdAndUnUse(orderNo);

                // 5. 异步处理分账
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        DivisionAccountOrderDTO divisionAccountOrderDTO = new DivisionAccountOrderDTO();
                        divisionAccountOrderDTO.setOrderNo(outRefundNo);
                        divisionAccountOrderDTO.setType(RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentRefundEntity.getRentalPackageType()) ? PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode() : PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode());
                        divisionAccountOrderDTO.setDivisionAccountType(DivisionAccountEnum.DA_TYPE_REFUND.getCode());
                        divisionAccountOrderDTO.setTraceId(UUID.randomUUID().toString().replaceAll("-", ""));
                        divisionAccountRecordService.asyncHandleDivisionAccount(divisionAccountOrderDTO);
                    }
                });

            } else {
                // 2. 更新会员期限
                carRentalPackageMemberTermService.updateStatusByUidAndTenantId(rentRefundEntity.getTenantId(), rentRefundEntity.getUid(), MemberTermStatusEnum.NORMAL.getCode(), null);
            }

            // 更新退款单信息
            carRentalPackageOrderRentRefundService.updateByOrderNo(rentRefundUpdate);

        } catch (Exception e) {
            log.error("WxRefundPayCarRentServiceImpl.process failed. ", e);
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
        return WxRefundPayOptTypeEnum.CAR_RENT_REFUND_CALL_BACK.getCode();
    }
}
