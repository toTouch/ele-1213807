package com.xiliulou.electricity.controller.outer;

import com.xiliulou.electricity.callback.impl.fy.FyInstallmentHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.OUTER_PARAM_BIZ_CONTENT;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/2 15:12
 */
@RestController
@AllArgsConstructor
@Slf4j
public class JsonOuterInstallmentController {
    
    private FyInstallmentHandler fyInstallmentHandler;
    
    /**
     * 签约及解约回调
     */
    @PostMapping("/outer/installment/sign/notify/{uid}")
    public String signNotify(@PathVariable Long uid, @RequestBody Map<String, Object> params) {
        if (!params.containsKey(OUTER_PARAM_BIZ_CONTENT) || StringUtils.isEmpty((String) params.get(OUTER_PARAM_BIZ_CONTENT))) {
            log.error("INSTALLMENT SIGN NOTIFY ERROR! no bizContent, uid={}", uid);
        }
        return fyInstallmentHandler.signNotify((String) params.get(OUTER_PARAM_BIZ_CONTENT), uid);
    }
    
    /**
     * 代扣回调
     */
    @PostMapping("/outer/installment/agreementPay/notify/{uid}")
    public String agreementPayNotify(@PathVariable Long uid, @RequestBody Map<String, Object> params) {
        if (!params.containsKey(OUTER_PARAM_BIZ_CONTENT) || StringUtils.isEmpty((String) params.get(OUTER_PARAM_BIZ_CONTENT))) {
            log.error("INSTALLMENT AGREEMENT PAY NOTIFY ERROR! no bizContent, uid={}", uid);
        }
        return fyInstallmentHandler.agreementPayNotify((String) params.get(OUTER_PARAM_BIZ_CONTENT), uid);
    }
    
}
