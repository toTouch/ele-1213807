package com.xiliulou.electricity.controller.admin.fqa;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.reqparam.fqa.AdminFqaCategoryAddParam;
import com.xiliulou.electricity.service.fqa.FqaCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 常见问题分类控制器
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@RestController
@Validated
@AllArgsConstructor
public class JsonAdminFqaCategoryController {
    
    private final FqaCategoryService fqaCategoryService;
    
    /**
     * 添加常见问题分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/category/add")
    public R add(@RequestBody @Valid AdminFqaCategoryAddParam fqaCategoryAddParam) {
        fqaCategoryService.add(fqaCategoryAddParam);
        return R.ok();
    }
}
