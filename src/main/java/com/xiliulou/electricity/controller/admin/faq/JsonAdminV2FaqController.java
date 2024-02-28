package com.xiliulou.electricity.controller.admin.faq;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.faq.AdminFaqQuery;
import com.xiliulou.electricity.reqparam.faq.AdminFaqChangeTypeReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqReq;
import com.xiliulou.electricity.reqparam.faq.AdminFaqUpDownReq;
import com.xiliulou.electricity.service.faq.FaqV2Service;
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
public class JsonAdminV2FaqController {
    
    private final FaqV2Service faqV2Service;
    
    /**
     * 获取常见问题详情
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @GetMapping("/admin/v2/faq/detail")
    public R detail(@RequestParam Long id) {
        return R.ok(faqV2Service.detail(id));
    }
    
    /**
     * 查看常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/page")
    public R query(@RequestBody AdminFaqQuery faqQuery) {
        return R.ok(faqV2Service.page(faqQuery));
    }
    
    /**
     * 添加常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/add")
    public R add(@RequestBody @Validated(value = CreateGroup.class) AdminFaqReq faqReq) {
        faqV2Service.add(faqReq);
        return R.ok();
    }
    
    
    /**
     * 更改常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/edit")
    public R edit(@RequestBody @Validated(value = UpdateGroup.class) AdminFaqReq faqReq) {
        faqV2Service.edit(faqReq);
        return R.ok();
    }
    
    /**
     * 批量删除常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/detele")
    public R deleteBatch(@RequestBody @Valid @NotEmpty(message = "集合不能为空") List<Long> ids) {
        faqV2Service.removeByIds(ids);
        return R.ok();
    }
    
    
    /**
     * 批量上下架常见问题
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/upDown")
    public R upDownBatch(@RequestBody @Valid AdminFaqUpDownReq faqUpDownReq) {
        faqV2Service.upDownBatch(faqUpDownReq);
        return R.ok();
    }
    
    /**
     * 批量切换分类
     *
     * @author kuz
     * @date 2024/2/23 16:11
     */
    @PostMapping("/admin/v2/faq/change/type")
    public R changeTypeBatch(@RequestBody @Valid AdminFaqChangeTypeReq faqChangeTypeReq) {
        faqV2Service.changeTypeBatch(faqChangeTypeReq);
        return R.ok();
    }
    
    
}
