package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.query.ElectricityAppConfigQuery;
import com.xiliulou.electricity.service.ElectricityAppConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序配置表(TStore)表控制层
 *
 * @author zhangyongbo
 * @since 2023-10-12
 */
@RestController
public class JsonUserElectricityAppConfigController {
    
    @Autowired
    private ElectricityAppConfigService electricityAppConfigService;
    
    @PutMapping(value = "/user/electricityAppConfig")
    @Log(title = "编辑小程序用户配置信息")
    public R edit(@RequestBody @Validated ElectricityAppConfigQuery electricityAppConfigQuery) {
        return electricityAppConfigService.edit(electricityAppConfigQuery);
    }
    
    //查询小程序用户配置
    @GetMapping(value = "/user/electricityAppConfig")
    public R queryUserAppConfigInfo() {
        return electricityAppConfigService.queryUserAppConfigInfo();
    }
}
