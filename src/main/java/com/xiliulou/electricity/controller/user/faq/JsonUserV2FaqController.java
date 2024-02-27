package com.xiliulou.electricity.controller.user.faq;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 常见问题user控制器
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@RestController
@AllArgsConstructor
@Validated
public class JsonUserV2FaqController {

    private final FaqV2Service faqV2Service;
    
    /**
     * 查询常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/user/v2/faq/page")
    public R page(@RequestBody AdminFaqQuery faqQuery) {
        return R.ok(faqV2Service.page(faqQuery));
    }
    
    
}
