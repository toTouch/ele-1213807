package com.xiliulou.electricity.controller.outer;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class JsonOuterElectricityConfigController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityConfigService electricityConfigService;


    //查询平台名称
    @GetMapping(value = "/outer/electricityConfig")
    public R queryOne() {
        return R.ok(electricityConfigService.queryOne());
    }

}
