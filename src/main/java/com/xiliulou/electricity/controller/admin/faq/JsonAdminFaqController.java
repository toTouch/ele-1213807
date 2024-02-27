package com.xiliulou.electricity.controller.admin.faq;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.reqparam.faq.AdminFaqChangeTypeReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqUpDownReq;
import com.xiliulou.electricity.service.faq.FaqService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 常见问题控制器
 *
 * @author kuz
 * @date 2024/2/23 16:11
 */
@RestController
@AllArgsConstructor
@Validated
public class JsonAdminFaqController {
    
    private final FaqService faqService;
    
    /**
     * 获取常见问题详情
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/admin/faq/detail")
    public R detail(@RequestParam Long id) {
        return R.ok(faqService.detail(id));
    }
    
    /**
     * 查看常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/page")
    public R query(@RequestBody AdminFaqQuery faqQuery) {
        return R.ok(faqService.page(faqQuery));
    }
    
    /**
     * 添加常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/add")
    public R add(@RequestBody @Validated(value = CreateGroup.class) AdminFaqReq faqReq) {
        faqService.add(faqReq);
        return R.ok();
    }
    
    
    /**
     * 更改常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/edit")
    public R edit(@RequestBody @Validated(value = UpdateGroup.class) AdminFaqReq faqReq) {
        faqService.edit(faqReq);
        return R.ok();
    }
    
    /**
     * 批量删除常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/detele")
    public R deleteBatch(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<Long> ids) {
        faqService.removeByIds(ids);
        return R.ok();
    }
    
    
    /**
     * 批量上下架常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/upDown")
    public R upDownBatch(@RequestBody @Valid AdminFaqUpDownReq faqUpDownReq) {
        faqService.upDownBatch(faqUpDownReq);
        return R.ok();
    }
    
    /**
     * 批量切换分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/faq/change/type")
    public R changeTypeBatch(@RequestBody @Valid AdminFaqChangeTypeReq faqChangeTypeReq) {
        faqService.changeTypeBatch(faqChangeTypeReq);
        return R.ok();
    }
    
    
}
