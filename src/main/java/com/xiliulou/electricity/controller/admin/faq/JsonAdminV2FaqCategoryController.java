package com.xiliulou.electricity.controller.admin.faq;

import com.xiliulou.common.sentinel.annotation.IdempotentCheck;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.service.faq.FaqCategoryV2Service;
import com.xiliulou.electricity.service.faq.FaqV2Service;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 常见问题分类控制器
 *
 *
 * @date 2024/2/23 16:11
 */
@RestController
@AllArgsConstructor
public class JsonAdminV2FaqCategoryController {
    
    private final FaqCategoryV2Service faqCategoryV2Service;
    
    private final FaqV2Service faqV2Service;
    
    /**
     * 查看常见问题分类
     *
     *
     * @date 2024/2/23 16:11
     */
    @GetMapping("/admin/faq/category/page/v2")
    public R query(@RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "typeId", required = false) Integer typeId) {
        return R.ok(faqCategoryV2Service.listFaqCategory(title, typeId));
    }
    
    /**
     * 查看常见问题分类
     *
     * @date 2024/2/23 16:11
     */
    @GetMapping("/admin/faq/category/page/count/v2")
    public R queryCount(@RequestParam(value = "title", required = false) String title) {
        return R.ok(faqCategoryV2Service.listFaqCategoryCount(title));
    }
    
    /**
     * 添加常见问题分类
     *
     *
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/category/add/v2")
    @IdempotentCheck(prefix = "faq_category")
    public R add(@RequestBody @Validated(value = CreateGroup.class) AdminFaqCategoryReq faqCategoryReq) {
        faqCategoryV2Service.saveFaqCategory(faqCategoryReq);
        return R.ok();
    }
    
    
    /**
     * 更新常见问题分类
     *
     *
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/category/edit/v2")
    @IdempotentCheck(prefix = "faq_category")
    public R edit(@RequestBody @Validated(value = UpdateGroup.class) AdminFaqCategoryReq faqCategoryReq) {
        faqCategoryV2Service.editFaqCategory(faqCategoryReq);
        return R.ok();
    }
    
    /**
     * 删除常见问题分类
     *
     *
     * @date 2024/2/23 16:11
     */
    @DeleteMapping("/admin/faq/category/v2/{id}")
    public R delete(@PathVariable Long id) {
        faqV2Service.removeByCategoryId(id);
        return R.ok();
    }
    
    
}
