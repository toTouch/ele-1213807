package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;

/**
 * 租车套餐购买业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderBizService {

    /**
     * 租车套餐订单
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    Boolean cancelRentalPackageOrder(String orderNo, Integer tenantId, Long uid);

    /**
     * 租车套餐订单，购买/续租
     * @param buyOptModel
     * @return
     */
    R buyRentalPackageOrder (CarRentalPackageOrderBuyOptModel buyOptModel, HttpServletRequest request);

    /**
     * 支付成功之后的逻辑<br />
     * 此处逻辑不包含回调处理，是回调逻辑中的一处子逻辑<br />
     * 调用此方法需要慎重
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    Pair<Boolean, Object> handBuyRentalPackageOrderSuccess(String orderNo, Integer tenantId, Long uid);

    /**
     * 支付失败之后的逻辑
     * @param orderNo 租车套餐购买订单编号
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return
     */
    Pair<Boolean, Object> handBuyRentalPackageOrderFailed(String orderNo, Integer tenantId, Long uid);

}
