package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreShops;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreShopsQuery;
import com.xiliulou.electricity.service.StoreShopsService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Objects;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
@Slf4j
public class JsonAdminStoreShopsController {
    /**
     * 服务对象
     */
    @Autowired
    StoreShopsService storeShopsService;


    //新增门店
    @PostMapping(value = "/admin/storeShops")
    public R save(@RequestBody @Validated(value = CreateGroup.class) StoreShops storeShops) {
        return storeShopsService.insert(storeShops);
    }

    //修改门店
    @PutMapping(value = "/admin/storeShops")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) StoreShops storeShops) {
        return storeShopsService.update(storeShops);
    }

    //删除门店
    @DeleteMapping(value = "/admin/storeShops/{id}")
    public R delete(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return storeShopsService.delete(id);
    }


    //列表查询
    @GetMapping(value = "/admin/storeShops/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "storeId", required = false) Long storeId) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }



        StoreShopsQuery storeShopsQuery = StoreShopsQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .storeId(storeId).build();


        return storeShopsService.queryList(storeShopsQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/storeShops/queryCount")
    public R queryCount( @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "storeId", required = false) Long storeId) {

        //租户
        StoreShopsQuery storeShopsQuery = StoreShopsQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .storeId(storeId).build();

        return storeShopsService.queryCount(storeShopsQuery);
    }



}
