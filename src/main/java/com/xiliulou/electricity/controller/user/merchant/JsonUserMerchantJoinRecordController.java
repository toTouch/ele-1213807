package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 17:28:03
 */

@RestController
@Slf4j
public class JsonUserMerchantJoinRecordController extends BaseController {
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    /**
     * 扫码二维码参与，生成记录
     */
    @PostMapping("/user/merchant/joinRecord/joinScanCode")
    public R joinScanCode(@RequestParam String code) {
        return merchantJoinRecordService.joinScanCode(code);
    }
}
