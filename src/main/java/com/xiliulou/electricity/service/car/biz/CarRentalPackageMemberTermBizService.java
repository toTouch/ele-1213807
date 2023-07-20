package com.xiliulou.electricity.service.car.biz;

/**
 * 租车套餐会员期限业务聚合 Service
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageMemberTermBizService {

    /**
     * 套餐购买订单过期处理<br />
     * 用于定时任务
     * @param offset
     * @param size
     */
    void expirePackageOrder(Integer offset, Integer size);
}
