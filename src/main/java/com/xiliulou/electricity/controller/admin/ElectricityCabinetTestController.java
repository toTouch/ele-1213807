package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ProvincialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
public class ElectricityCabinetTestController {
    /**
     * 服务对象
     */
    @Autowired
    ProvincialService provincialService;

    //测试省市
    @GetMapping(value = "/admin/test")
    public R test() {
        return provincialService.test();
    }

}