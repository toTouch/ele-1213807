package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class JsonUserElectricityCarModelController {

    @Autowired
    ElectricityCarModelService electricityCarModelService;

    /**
     * 车辆型号列表
     */
    @GetMapping("/user/carModel/list")
    public R userCar(@RequestParam("storeId") Long storeId,
                     @RequestParam("size") Long size,
                     @RequestParam("offset") Long offset) {
        ElectricityCarModelQuery query = new ElectricityCarModelQuery();
        query.setSize(size);
        query.setOffset(offset);
        query.setStoreId(storeId);
        query.setTenantId(TenantContextHolder.getTenantId());

        return R.ok(electricityCarModelService.selectList(query));
    }

    /**
     * 车辆型号详情
     */
    @GetMapping("/user/carModel/detail")
    public R carModelDetail(@RequestParam("id") Long id) {

        return R.ok(electricityCarModelService.selectDetailById(id));
    }

}
