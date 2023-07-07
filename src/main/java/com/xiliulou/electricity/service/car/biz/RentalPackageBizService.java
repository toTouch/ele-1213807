package com.xiliulou.electricity.service.car.biz;

import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.util.List;

/**
 * 租赁套餐相关的业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface RentalPackageBizService {

    /**
     * 计算需要支付的金额
     * @param amount 原金额
     * @param userCouponIds 用户的优惠券ID集合
     * @param uid 用户ID
     * @return Triple<BigDecimal, List<Long>, Boolean> 实际支付金额、已用的优惠券ID、Boolean（暂无实际意义）
     */
    Triple<BigDecimal, List<Long>, Boolean> calculatePaymentAmount(BigDecimal amount, List<Long> userCouponIds, Long uid);

    /**
     * 购买套餐订单统一检测
      * @param tenantId 租户ID
     * @param uid 用户ID
     */
    void checkBuyPackageCommon(Integer tenantId, Long uid);

}
