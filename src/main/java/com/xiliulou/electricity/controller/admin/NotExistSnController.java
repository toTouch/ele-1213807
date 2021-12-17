package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.FaqQuery;
import com.xiliulou.electricity.query.NotExistSnQuery;
import com.xiliulou.electricity.service.FaqService;
import com.xiliulou.electricity.service.NotExistSnService;
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
public class NotExistSnController extends BaseController {
    @Autowired
    NotExistSnService notExistSnService;

    @GetMapping("/admin/faq/list")
    public R getList(@RequestParam("size") Integer size,
                     @RequestParam("offset") Integer offset,
                     @RequestParam("eId") Integer eId) {
        if (size <= 0 || size > 50) {
            size = 10;
        }
        if (offset < 0) {
            offset = 0;
        }

        NotExistSnQuery notExistSnQuery = NotExistSnQuery.builder()
                .offset(offset)
                .size(size)
                .eId(eId).build();

        return notExistSnService.queryList(notExistSnQuery);
    }


    @GetMapping("/admin/faq/queryCount")
    public R queryCount(@RequestParam("eId") Integer eId) {

        NotExistSnQuery notExistSnQuery = NotExistSnQuery.builder()
                .eId(eId).build();
        return notExistSnService.queryCount(notExistSnQuery);
    }


}
