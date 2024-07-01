package com.xiliulou.electricity.controller.user.faq;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Objects;


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
    
    private final ElectricityConfigMapper electricityConfigMapper;
    
    private final FaqCategoryV2Service faqCategoryV2Service;
    
    /**
     * 查询常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/user/faq/page/v2")
    public R page(@RequestBody AdminFaqQuery faqQuery) {
        return R.ok(faqV2Service.listFaqQueryToUser(faqQuery));
    }
    
    /**
     * 获取常见问题详情
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/user/faq/detail/v2")
    public R detail(@RequestParam Long id) {
        return faqV2Service.queryDetail(id);
    }
    
    /**
     * 查看常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/user/faq/category/page/v2")
    public R query(@RequestParam(value = "title", required = false) String title, @RequestParam(value = "typeId", required = false) Integer typeId) {
        return R.ok(faqCategoryV2Service.listFaqCategory(title, typeId));
    }
}
