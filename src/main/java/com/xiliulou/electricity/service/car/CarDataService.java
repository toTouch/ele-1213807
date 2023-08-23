package com.xiliulou.electricity.service.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.vo.car.PageDataResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 车辆运营数据
 */
public  interface CarDataService {

    /**
     * 分页查询所有的车辆运营数据
     *
     * @param carDataConditionReq
     * @return
     */
    PageDataResult queryAllCarDataPage(CarDataConditionReq carDataConditionReq);


    /**
     * 获取全部车辆的分页数据
     */
    R queryAllCarDataPage(Long offset, Long size, Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone);

    /**
     * 获取全部车辆的数据总数
     */
    R queryAllCarDataCount(Long franchiseeId, Long storeId, Integer modelId, String sn, String userName, String phone);

    /**
     * 获取待租车辆的分页数据
     */
    R queryPendingRentalCarDataPage(Long offset,Long size,Long franchiseeId,Long storeId,Integer modelId,String sn,String userName,String phone);

    /**
     * 获取待租车辆的数据总数
     */
    R queryPendingRentalCarDataCount(Long franchiseeId,Long storeId,Integer modelId,String sn,String userName,String phone);

    /**
     * 获取已租车辆的分页数据
     */
    R queryLeasedCarDataPage(Long offset,Long size,Long franchiseeId,Long storeId,Integer modelId,String sn,String userName,String phone);

    /**
     * 获取已租车辆的数据总数
     */
    R queryLeasedCarDataCount(Long franchiseeId,Long storeId,Integer modelId,String sn,String userName,String phone);

    /**
     * 获取逾期车辆的分页数据
     */
    R queryOverdueCarDataPage(Long offset,Long size,Long franchiseeId,Long storeId,Integer modelId,String sn,String userName,String phone);

    /**
     * 获取逾期车辆的数据总数
     */
    R queryOverdueCarDataCount(Long franchiseeId, Long storeId,Integer modelId,String sn,String userName,String phone);
}
