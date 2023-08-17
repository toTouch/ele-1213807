package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.vo.car.PageDataResult;

/**
 * 车辆运营数据
 */
public interface CarDataService {

    /**
     * 分页查询所有的车辆运营数据
     * @param carDataConditionReq
     * @return
     */
    PageDataResult queryAllCarDataPage(CarDataConditionReq carDataConditionReq);



}
