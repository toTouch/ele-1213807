package com.xiliulou.electricity.controller.admin.supper;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.supper.DelBatteryReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Ant
 * @Date 2024/4/22
 * @Description:
 **/
@Slf4j
@RestController
@RequestMapping("/admin/inner/supper")
public class JsonAdminSupperController {
    
    /**
     * 根据电池SN删除电池
     * @param delBatteryReq
     */
    @PostMapping
    public R delBatterys(@RequestBody DelBatteryReq delBatteryReq) {
    
    }
}
