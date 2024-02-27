package com.xiliulou.electricity.controller.admin.faq;

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
import org.springframework.web.bind.annotation.RestController;

/**
 * 常见问题分类控制器
 *
 * @author kuz
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
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/admin/v2/faq/category/page")
    public R query() {
        return R.ok(faqCategoryV2Service.page());
    }
    
    /**
     * 添加常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/category/add")
    public R add(@RequestBody @Validated(value = CreateGroup.class) AdminFaqCategoryReq faqCategoryReq) {
        faqCategoryV2Service.add(faqCategoryReq);
        return R.ok();
    }
    
    
    /**
     * 更新常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/category/edit")
    public R edit(@RequestBody @Validated(value = UpdateGroup.class) AdminFaqCategoryReq faqCategoryReq) {
        faqCategoryV2Service.edit(faqCategoryReq);
        return R.ok();
    }
    
    /**
     * 删除常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @DeleteMapping("/admin/v2/faq/category/detele/{id}")
    public R delete(@PathVariable Long id) {
        faqV2Service.removeByCategoryId(id);
        return R.ok();
    }
    
    
}
