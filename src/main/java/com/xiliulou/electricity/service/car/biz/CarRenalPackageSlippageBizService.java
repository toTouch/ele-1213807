package com.xiliulou.electricity.service.car.biz;

import java.math.BigDecimal;

/**
 * 逾期业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRenalPackageSlippageBizService {

    /**
     * 清除滞纳金
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param optUid 操作用户ID
     * @return true(成功)、false(失败)
     */
    boolean clearSlippage(Integer tenantId, Long uid, Long optUid);

    /**
     * 根据用户ID查询车辆租赁套餐订单未支付的滞纳金金额
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    BigDecimal queryCarPackageUnpaidAmountByUid(Integer tenantId, Long uid);

    /**
     * 是否存在未支付的滞纳金<br />
     * 租车(单车、车电一体)
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return true(存在)、false(不存在)
     */
    boolean isExitUnpaid(Integer tenantId, Long uid);

}
