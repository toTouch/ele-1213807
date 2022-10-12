package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.OtaFileConfigService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.web.multipart.MultipartFile;

/**
 * (OtaFileConfig)表控制层
 *
 * @author Hardy
 * @since 2022-10-12 09:24:49
 */
@RestController
public class JsonAdminOtaFileConfigController {
    
    /**
     * 服务对象
     */
    @Resource
    private OtaFileConfigService otaFileConfigService;
    
    @PostMapping("admin/otaFileConfig/upload")
    public R otaFileConfigUpload(@RequestParam("file") MultipartFile file, @RequestParam("name") String name,
            @RequestParam("version") String version, @RequestParam(value = "type", defaultValue = "1") Integer type) {
        
        return otaFileConfigService.otaFileConfigUpload(file, name, version, type);
    }
}
