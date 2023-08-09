package com.xiliulou.electricity.service.car.biz;

/**
 * 车辆租赁订单业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRentalOrderBizService {

    /**
     * 解绑用户车辆
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @param optUid 操作用户UID
     * @return true(成功)、false(失败)
     */
    boolean unBindingCar(Integer tenantId, Long uid, Long optUid);

    /**
     * 给用户绑定车辆
     * @param tenantId 租户ID
     * @param uid 用户UID
     * @param carSn 车辆SN码
     * @param optUid 操作用户UID
     * @return true(成功)、false(失败)
     */
    boolean bindingCar(Integer tenantId, Long uid, String carSn, Long optUid);

    /**
     * JT 808 控制锁
     * @param sn
     * @param lockType
     * @param retryCount
     * @return
     */
    Boolean retryCarLockCtrl(String sn, Integer lockType, Integer retryCount);

}
