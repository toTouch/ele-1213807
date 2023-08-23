package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.service.car.CarDataService;
import com.xiliulou.electricity.vo.car.PageDataResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 车辆运营数据
 */
@Slf4j
@RestController
public class JsonAdminCarDataController extends BasicController {

    @Autowired
    private CarDataService carDataService;

    /**
     * 获取全部车辆的分页数据
     */
    @GetMapping("/admin/carData/allCar/page")
    public R queryAllCarDataPage(@RequestParam("offset") Long offset,
                                 @RequestParam("size") Long size,
                                 @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                 @RequestParam(value = "storeId", required = false) Long storeId,
                                 @RequestParam(value = "modelId", required = false) Integer modelId,
                                 @RequestParam(value = "sn", required = false) String sn,
                                 @RequestParam(value = "userName", required = false) String userName,
                                 @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryAllCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone);
    }

    /**
     * 获取全部车辆的数据总数
     */
    @GetMapping("/admin/carData/allCar/page")
    public R queryAllCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryAllCarDataCount(franchiseeId,storeId, modelId, sn, userName, phone);
    }

    /**
     * 获取待租车辆的分页数据
     */
    @GetMapping("/admin/carData/pendingRentalCar/page")
    public R queryPendingRentalCarDataPage(@RequestParam("offset") Long offset,
                                           @RequestParam("size") Long size,
                                           @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                           @RequestParam(value = "storeId", required = false) Long storeId,
                                           @RequestParam(value = "modelId", required = false) Integer modelId,
                                           @RequestParam(value = "sn", required = false) String sn,
                                           @RequestParam(value = "userName", required = false) String userName,
                                           @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryPendingRentalCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone);
    }

    /**
     * 获取待租车辆的数据总数
     */
    @GetMapping("/admin/carData/pendingRentalCar/count")
    public R queryPendingRentalCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryPendingRentalCarDataCount( franchiseeId,storeId, modelId, sn, userName, phone);
    }

    /**
     * 获取已租车辆的分页数据
     */
    @GetMapping("/admin/carData/leasedRentalCar/page")
    public R queryLeasedCarDataPage(@RequestParam("offset") Long offset,
                                    @RequestParam("size") Long size,
                                    @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                    @RequestParam(value = "storeId", required = false) Long storeId,
                                    @RequestParam(value = "modelId", required = false) Integer modelId,
                                    @RequestParam(value = "sn", required = false) String sn,
                                    @RequestParam(value = "userName", required = false) String userName,
                                    @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryLeasedCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone);
    }

    /**
     * 获取已租车辆的数据总数
     */
    @GetMapping("/admin/carData/leasedRentalCar/count")
    public R queryLeasedCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryLeasedCarDataCount( franchiseeId,storeId, modelId, sn, userName, phone);
    }

    /**
     * 获取逾期车辆的分页数据
     */
    @GetMapping("/admin/carData/overdueCar/page")
    public R queryOverdueCarDataPage(@RequestParam("offset") Long offset,
                                     @RequestParam("size") Long size,
                                     @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                     @RequestParam(value = "storeId", required = false) Long storeId,
                                     @RequestParam(value = "modelId", required = false) Integer modelId,
                                     @RequestParam(value = "sn", required = false) String sn,
                                     @RequestParam(value = "userName", required = false) String userName,
                                     @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryOverdueCarDataPage(offset,size, franchiseeId,storeId, modelId, sn, userName, phone);
    }

    /**
     * 获取逾期车辆的数据总数
     */
    @GetMapping("/admin/carData/overdueCar/count")
    public R queryOverdueCarDataCount(
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone) {
        return carDataService.queryOverdueCarDataCount( franchiseeId,storeId, modelId, sn, userName, phone);
    }
}
