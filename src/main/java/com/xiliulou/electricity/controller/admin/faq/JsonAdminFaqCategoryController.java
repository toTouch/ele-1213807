package com.xiliulou.electricity.controller.admin.faq;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.reqparam.faq.AdminFaqCategoryReq;
import com.xiliulou.electricity.service.faq.FaqCategoryService;
import com.xiliulou.electricity.service.faq.FaqService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 常见问题分类控制器
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@RestController
@AllArgsConstructor
public class JsonAdminFaqCategoryController {
    
    private final FaqCategoryService faqCategoryService;
    
    private final FaqService faqService;
    
    /**
     * 查看常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/admin/faq/category/page")
    public R query() {
        return R.ok(faqCategoryService.page());
    }
    
    /**
     * 添加常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/category/add")
    public R add(@RequestBody @Validated(value = CreateGroup.class) AdminFaqCategoryReq faqCategoryReq) {
        faqCategoryService.add(faqCategoryReq);
        return R.ok();
    }
    
    
    /**
     * 更新常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/category/edit")
    public R edit(@RequestBody @Validated(value = UpdateGroup.class) AdminFaqCategoryReq faqCategoryReq) {
        faqCategoryService.edit(faqCategoryReq);
        return R.ok();
    }
    
    /**
     * 删除常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @DeleteMapping("/admin/faq/category/detele/{id}")
    public R delete(@PathVariable Long id) {
        faqService.removeByCategoryId(id);
        return R.ok();
    }
    
    
}
