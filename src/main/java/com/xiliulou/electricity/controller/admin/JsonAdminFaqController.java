package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FaqQuery;
import com.xiliulou.electricity.service.FaqService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : eclair
 * @date : 2021/9/26 4:45 下午
 */
@RestController
public class JsonAdminFaqController extends BaseController {
    @Autowired
    FaqService faqService;

    @GetMapping("/admin/faq/list")
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


    @GetMapping("/admin/faq/queryCount")
    public R queryCount() {
        return returnTripleResult(faqService.queryCount());
    }

    @PostMapping("/admin/faq")
    public R addFaq(@RequestBody @Validated(value = CreateGroup.class) FaqQuery faqQuery) {
        return returnTripleResult(faqService.addFaq(faqQuery));
    }

    @PutMapping("/admin/faq")
    public R updateFaq(@RequestBody @Validated(value = UpdateGroup.class) FaqQuery faqQuery) {
        return returnTripleResult(faqService.updateFaq(faqQuery));
    }

    @DeleteMapping("/admin/faq/{id}")
    public R deleteFaq(@PathVariable("id") Integer id) {
        return returnTripleResult(faqService.delete(id));
    }

}
