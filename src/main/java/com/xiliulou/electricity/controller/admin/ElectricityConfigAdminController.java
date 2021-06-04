package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class ElectricityConfigAdminController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityConfigService electricityConfigService;

    //编辑平台名称
    @PutMapping(value = "/admin/electricityConfig")
    public R edit(@RequestParam("name") String name,@RequestParam("orderTime") Integer orderTime,@RequestParam("isManualReview") Integer isManualReview) {
        return electricityConfigService.edit(name,orderTime,isManualReview);
    }

    //查询平台名称
    @GetMapping(value = "/admin/electricityConfig")
    public R queryOne() {
        return R.ok(electricityConfigService.queryOne());
    }

}
