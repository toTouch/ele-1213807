package com.xiliulou.electricity.controller.admin.asset;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.asset.CarAddRequest;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
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
public class JsonAdminCarController {
    @Autowired
    ElectricityCarService electricityCarService;
    
    @PostMapping(value = "/admin/electricityCar/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) CarAddRequest carAddRequest) {
        // 用户校验
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        return electricityCarService.saveV2(carAddRequest);
    }
}
