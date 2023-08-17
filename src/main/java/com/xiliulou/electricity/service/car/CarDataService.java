package com.xiliulou.electricity.service.car;

import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.vo.car.CarDataResult;
import com.xiliulou.electricity.vo.car.CarDataVO;

import java.util.List;

/**
 * 车辆运营数据
 */
public interface CarDataService {

    /**
     * 分页查询所有的车辆运营数据
     * @param carDataConditionReq
     * @return
     */
    CarDataResult queryAllCarDataPage(CarDataConditionReq carDataConditionReq);

    /**
     * 查询所有车辆运营数据的数量
     * @return
     */
    Integer queryAllCarDataCount(CarDataConditionReq carDataConditionReq);


}
