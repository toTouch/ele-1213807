package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.OtaFileEleSha256Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (OtaFileEleSha256)表控制层
 *
 * @author zgw
 * @since 2022-10-12 17:31:11
 */
@RestController
public class JsonAdminOtaFileEleSha256Controller {
    
    /**
     * 服务对象
     */
    @Resource
    private OtaFileEleSha256Service otaFileEleSha256Service;
    
    
    @GetMapping("/admin/otaFileEleSha256/info")
    public R queryInfo(@RequestParam("eid") Integer eid) {
        return otaFileEleSha256Service.queryInfo(eid);
    }
}
