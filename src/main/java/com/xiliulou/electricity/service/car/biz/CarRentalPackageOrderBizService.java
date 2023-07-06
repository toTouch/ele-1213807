package com.xiliulou.electricity.service.car.biz;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageOrderBuyOptModel;

/**
 * 租车套餐购买业务聚合 BizService
 *
 * @author xiaohui.song
 **/
public interface CarRentalPackageOrderBizService {

    /**
     * 租车套餐订单，购买/续租
     * @param buyOptModel
     * @return
     */
    R<Boolean> buyRentalPackageOrder (CarRentalPackageOrderBuyOptModel buyOptModel);

}
