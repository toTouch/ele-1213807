package com.xiliulou.electricity.controller.outer;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2024/12/3 16:52
 * @desc
 */
@RestController
@Slf4j
public class JsonOuterFaqController {
    @Resource
    private  FaqV2Service faqV2Service;
    
    @Resource
    private  FaqCategoryV2Service faqCategoryV2Service;
    
    /**
     * 查询常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/outer/faq/page/v2")
    public R page(@RequestBody AdminFaqQuery faqQuery) {
        return R.ok(faqV2Service.listFaqQueryToUser(faqQuery));
    }
    
    /**
     * 获取常见问题详情
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/outer/faq/detail/v2")
    public R detail(@RequestParam Long id) {
        return faqV2Service.queryDetail(id);
    }
    
    /**
     * 查看常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/outer/faq/category/page/v2")
    public R query(@RequestParam(value = "title", required = false) String title, @RequestParam(value = "typeId", required = false) Integer typeId) {
        return R.ok(faqCategoryV2Service.listFaqCategory(title, typeId));
    }
}
