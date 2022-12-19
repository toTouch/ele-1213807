package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.StoreTagQuery;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.StoreTagService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-14-14:46
 */
@Slf4j
@RestController
public class JsonAdminStoreTagController {

    @Autowired
    private StoreTagService storeTagService;

    @GetMapping(value = "/admin/storeTag/list")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                  @RequestParam(value = "title", required = false) String title,
                  @RequestParam(value = "storeId") Long storeId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        StoreTagQuery query = new StoreTagQuery();
        query.setSize(size);
        query.setOffset(offset);
        query.setTenantId(TenantContextHolder.getTenantId());
        query.setTitle(title);
        query.setStoreId(storeId);

        return R.ok(storeTagService.selectByPage(query));
    }


    @GetMapping(value = "/admin/storeTag/queryCount")
    public R count(@RequestParam(value = "title", required = false) String title,
                   @RequestParam(value = "storeId") Long storeId) {

        StoreTagQuery query = new StoreTagQuery();
        query.setTenantId(TenantContextHolder.getTenantId());
        query.setTitle(title);
        query.setStoreId(storeId);

        return R.ok(storeTagService.selectPageCount(query));
    }

    @PostMapping(value = "/admin/storeTag/insert")
    public R insert(@RequestBody  @Validated(value = CreateGroup.class) StoreTagQuery storeTagQuery){
        return R.ok(storeTagService.insert(storeTagQuery));
    }

    @PutMapping(value = "/admin/storeTag/update")
    public R update(@RequestBody  @Validated(value = UpdateGroup.class) StoreTagQuery storeTagQuery){
        return R.ok(storeTagService.update(storeTagQuery));
    }

    @DeleteMapping(value = "/admin/storeTag/{id}")
    public R update(@PathVariable("id") Long id){
        return R.ok(storeTagService.deleteById(id));
    }

}
