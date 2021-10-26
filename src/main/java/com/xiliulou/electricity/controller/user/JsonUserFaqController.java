package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 5:32 下午
 */
@RestController
public class JsonUserFaqController extends BaseController {
    @Autowired
    FaqService faqService;

    @GetMapping("/user/faq/list")
    public R getList(@RequestParam("size") Integer size,
                     @RequestParam("offset") Integer offset) {
        if (size <= 0 || size > 50) {
            size = 10;
        }
        if (offset < 0) {
            offset = 0;
        }
        return returnTripleResult(faqService.queryList(size, offset));
    }
}
