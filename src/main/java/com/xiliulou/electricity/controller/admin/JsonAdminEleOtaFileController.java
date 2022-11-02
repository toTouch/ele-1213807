package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.EleOtaFileService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *
 * @author zgw
 * @since 2022-10-12 17:31:11
 */
@RestController
public class JsonAdminEleOtaFileController {
    
    /**
     * 服务对象
     */
    @Resource
    private EleOtaFileService eleOtaFileService;
    
    
    @GetMapping("/admin/eleOtaFile/info")
    public R queryInfo(@RequestParam("eid") Integer eid) {
        return eleOtaFileService.queryInfo(eid);
    }
}
