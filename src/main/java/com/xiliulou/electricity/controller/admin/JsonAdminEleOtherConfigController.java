package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.EleOtherConfig;
import com.xiliulou.electricity.service.EleOtherConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: eclair
 * @Date: 2021/4/19 09:43
 * @Description:
 */
@RestController
public class JsonAdminEleOtherConfigController extends BaseController {
    @Autowired
    EleOtherConfigService eleOtherConfigService;

    @GetMapping("/admin/ele/other/config/{eid}")
    public R queryEleOtherConfigByCid(@PathVariable("eid") Integer eid) {

        return R.ok(this.eleOtherConfigService.queryByEidFromCache(eid));
    }

    @PutMapping("/admin/ele/other/config")
    @Log(title = "修改柜机其他配置")
    public R updateEleOtherConfig(@RequestBody EleOtherConfig eleOtherConfig){
        return eleOtherConfigService.updateEleOtherConfig(eleOtherConfig);
    }


}
