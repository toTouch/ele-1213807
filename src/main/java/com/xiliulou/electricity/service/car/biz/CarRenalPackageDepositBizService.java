package com.xiliulou.electricity.service.car.biz;

/**
 * 租车套餐押金业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRenalPackageDepositBizService {

    /**
     * 退押申请
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param depositPayOrderNo 押金缴纳支付订单编码
     * @return
     */
    boolean refundDeposit(Integer tenantId, Long uid, String depositPayOrderNo);

}
