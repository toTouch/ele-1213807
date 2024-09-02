package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/2 15:12
 */
@RestController
@Slf4j
public class JsonOuterInstallmentController {
    
    
    @PostMapping("/outer/installment/sign/notify/{uid}")
    public String signNotify(@PathVariable Long uid, @RequestBody Map<String, Object> params) {
        if (!params.containsKey("bizContent") || StringUtils.isEmpty((String)params.get("bizContent"))) {
            log.error("INSTALLMENT SIGN NOTIFY ERROR! no bizContent, uid={}", uid);
        }
        
        // TODO
        return "";
    }
    
}
