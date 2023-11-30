package com.xiliulou.electricity.controller.admin.asset;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.CarAddRequest;
import com.xiliulou.electricity.request.asset.CarBatchSaveRequest;
import com.xiliulou.electricity.request.asset.CarOutWarehouseRequest;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@Slf4j
public class JsonAdminCarV2Controller {
    @Autowired
    ElectricityCarService electricityCarService;
    
    /**
     *
     * @param carAddRequest 车辆新增参数
     * @return 返回参数
     */
    @PostMapping(value = "/admin/electricityCar/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) CarAddRequest carAddRequest) {
        // 用户校验
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return electricityCarService.saveV2(carAddRequest);
    }
    
    /**
     *
     * @param carOutWarehouseRequest 车辆出库参数
     * @return 返回参数
     */
    @PostMapping(value = "/admin/electricityCar/outWarehouse")
    public R outWareHouse(@RequestBody @Validated(value = UpdateGroup.class) CarOutWarehouseRequest carOutWarehouseRequest) {
        // 用户校验
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return electricityCarService.batchUpdateFranchiseeIdAndStoreId(carOutWarehouseRequest);
    }
    
    /**
     *
     * @param carBatchSaveRequest 车辆参数
     * @return 返回参数
     */
    @PostMapping(value = "/admin/electricityCar/batchSave")
    public R batchSave(@RequestBody @Validated(value = CreateGroup.class) CarBatchSaveRequest carBatchSaveRequest) {
        // 用户校验
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return electricityCarService.bathSaveCar(carBatchSaveRequest);
    }
}
