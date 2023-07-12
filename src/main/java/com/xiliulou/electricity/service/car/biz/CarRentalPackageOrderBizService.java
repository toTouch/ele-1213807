package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;
import com.xiliulou.electricity.vo.rental.RentalPackageVO;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;

/**
 * 租车套餐购买业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderBizService {

    /**
     * 根据用户ID查询正在使用的套餐信息<br />
     * 复合查询，车辆信息、门店信息、GPS信息、电池信息、保险信息
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @return com.xiliulou.core.web.R<com.xiliulou.electricity.vo.rental.RentalPackageVO>
     * @author xiaohui.song
     **/
    R<RentalPackageVO> queryUseRentalPackageOrderByUid(Integer tenantId, Long uid);

    /**
     * 取消租车套餐订单
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
