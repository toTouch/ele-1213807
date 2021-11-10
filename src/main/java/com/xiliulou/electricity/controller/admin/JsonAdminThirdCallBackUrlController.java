package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ThirdCallBackUrlService;
import com.xiliulou.electricity.web.query.ThirdCallBackUrlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : eclair
 * @date : 2021/8/4 3:58 下午
 */
@RestController
public class JsonAdminThirdCallBackUrlController extends BaseController {
    @Autowired
    ThirdCallBackUrlService thirdCallBackUrlService;

    @GetMapping("/admin/third/callback/url")
    public R queryThirdCallBackUrl() {
        return returnPairResult(thirdCallBackUrlService.queryThirdCallBackByTenantId());
    }


    @PostMapping("/admin/third/callback/url")
    public R saveThirdCallBackUrl(@RequestBody ThirdCallBackUrlRequest thirdCallBackUrlRequest) {
        return returnPairResult(thirdCallBackUrlService.save(thirdCallBackUrlRequest));
    }

    @PutMapping("/admin/third/callback/url")
    public R updateThirdCallBackUrl(@RequestBody ThirdCallBackUrlRequest thirdCallBackUrlRequest) {
        return returnPairResult(thirdCallBackUrlService.update(thirdCallBackUrlRequest));
    }
}
