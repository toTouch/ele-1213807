package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.electricity.reqparam.opt.carpackage.ExpirePackageOrderReq;

/**
 * description:  套餐过期业务service
 *
 * @author caobotao.cbt
 * @date 2024/11/26 17:36
 */
public interface CarRentalMemberTermExpireBizService {
    
    
    /**
     * 执行
     *
     * @param req
     * @author caobotao.cbt
     * @date 2024/11/26 17:42
     */
    void expirePackageOrder(ExpirePackageOrderReq req);
}