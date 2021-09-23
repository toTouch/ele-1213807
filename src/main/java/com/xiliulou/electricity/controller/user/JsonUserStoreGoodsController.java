package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreGoods;
import com.xiliulou.electricity.query.StoreShopsQuery;
import com.xiliulou.electricity.service.StoreGoodsService;
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
public class JsonUserStoreGoodsController {
    /**
     * 服务对象
     */
    @Autowired
    StoreGoodsService storeGoodsService;


    //列表查询
    @GetMapping(value = "/user/storeShops/list")
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


        return storeGoodsService.queryList(storeShopsQuery);
    }

    //列表查询
    @GetMapping(value = "/user/storeShops/queryCount")
    public R queryCount( @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "storeId", required = false) Long storeId) {

        //
        StoreShopsQuery storeShopsQuery = StoreShopsQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .storeId(storeId).build();

        return storeGoodsService.queryCount(storeShopsQuery);
    }



}
