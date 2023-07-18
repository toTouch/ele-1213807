package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.electricity.model.car.opt.CarRentalPackageDepositRefundOptModel;

import java.math.BigDecimal;

/**
 * 租车套餐押金业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRenalPackageDepositBizService {

    /**
     * 运营商端创建退押
     * @param optModel 租户ID
     * @return
     */
    boolean refundDepositCreate(CarRentalPackageDepositRefundOptModel optModel);

    /**
     * 审批退还押金申请单
     * @param refundDepositOrderNo 退押申请单
     * @param approveFlag 审批状态
     * @param apploveDesc 审批意见
     * @param apploveUid 审批人
     * @param refundAmount 退款金额
     * @return
     */
    boolean approveRefundDepositOrder(String refundDepositOrderNo, boolean approveFlag, String apploveDesc, Long apploveUid, BigDecimal refundAmount);

    /**
     * C端退押申请
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param depositPayOrderNo 押金缴纳支付订单编码
     * @param systemDefinition 操作系统来源
     * @return
     */
    boolean refundDeposit(Integer tenantId, Long uid, String depositPayOrderNo);

}
