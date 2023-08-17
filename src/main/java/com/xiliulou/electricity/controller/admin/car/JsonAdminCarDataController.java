package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.vo.car.CarDataVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 车辆运营数据
 */
@RestController
public class JsonAdminCarDataController {

    /**
     * 所有车辆运营数据
     * @param carDataConditionReq
     * @return
     */
    @GetMapping("/admin/carData/all/page")
    public R<List<CarDataVO>> queryAllCarDataPage(@RequestBody CarDataConditionReq carDataConditionReq){
        return R.ok();
    }

    /**
     * 车辆运营数据总个数
     * @param carDataConditionReq
     * @return
     */
    @GetMapping("/admin/carData/all/count")
    public R queryAllCardDataCount(@RequestBody CarDataConditionReq carDataConditionReq){
        return R.ok();
    }

    /**
     * 已经出租的车辆
     * @param carDataConditionReq
     * @return
     */
    @GetMapping("/admin/carData/rent/page")
    public R queryCardDataRentPage(@RequestBody CarDataConditionReq carDataConditionReq){
        return R.ok();
    }
    /**
     * 已经出租的车辆总数
     * @param carDataConditionReq
     * @return
     */
    @GetMapping("/admin/carData/rent/count")
    public R queryCardDataRentCount(@RequestBody CarDataConditionReq carDataConditionReq){
        return R.ok();
    }

    /**
     * 未出租车辆
     * @param carDataConditionReq
     * @return
     */
    @GetMapping("/admin/carData/notRent/page")
    public R queryCardDataNotRentPage(@RequestBody CarDataConditionReq carDataConditionReq){
        return R.ok();
    }
    /**
     * 未出租车辆总数
     * @param carDataConditionReq
     * @return
     */
    @GetMapping("/admin/carData/notRent/count")
    public R queryCardDataNotRentCount(@RequestBody CarDataConditionReq carDataConditionReq){
        return R.ok();
    }








}
