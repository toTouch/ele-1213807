package com.xiliulou.electricity.controller.admin.car;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.car.CarDataConditionReq;
import com.xiliulou.electricity.service.car.CarDataService;
import com.xiliulou.electricity.vo.car.PageDataResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 车辆运营数据
 */
@RestController
public class JsonAdminCarDataController {

    @Autowired
    private CarDataService carDataService;

    /**
     * 所有车辆运营数据
     * @param carDataConditionReq
     * @return
     */
    @PostMapping("/admin/carData/all/page")
    public R<PageDataResult> queryAllCarDataPage(@RequestBody CarDataConditionReq carDataConditionReq){
        return R.ok(carDataService.queryAllCarDataPage(carDataConditionReq));
    }








}
