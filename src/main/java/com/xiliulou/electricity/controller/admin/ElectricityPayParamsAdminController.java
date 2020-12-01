package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:35
 **/
@Slf4j
@RestController
public class ElectricityPayParamsAdminController {
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    /**
     * 新增/修改支付参数
     *
     * @param
     * @return
     */
    @PostMapping(value = "/admin/battery")
    public R save(@RequestBody @Validated ElectricityPayParams electricityPayParams) {

        return electricityPayParamsService.saveOrUpdateElectricityPayParams(electricityPayParams, System.currentTimeMillis());
    }


}
