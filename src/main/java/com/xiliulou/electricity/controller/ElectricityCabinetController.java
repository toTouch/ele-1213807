package com.xiliulou.electricity.controller;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@RequestMapping("tElectricityCabinet")
public class ElectricityCabinetController {
    /**
     * 服务对象
     */
    @Autowired
    private ElectricityCabinetService electricityCabinetService;

    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet")
    public R save(@RequestBody ElectricityCabinet electricityCabinet) {
        return electricityCabinetService.save(electricityCabinet);
    }

    //修改换电柜
    @PutMapping(value = "/admin/electricityCabinet")
    public R update(@RequestBody ElectricityCabinet electricityCabinet) {
        return electricityCabinetService.edit(electricityCabinet);
    }


    //删除换电柜
    @DeleteMapping(value = "/admin/electricityCabinet/{id}")
    public R delete(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return R.fail("id不能为空");
        }
        return electricityCabinetService.delete(id);
    }

   //列表查询
    @GetMapping(value = "/admin/electricityCabinet/list")
    public R queryList(@RequestParam(value = "size", required = false) Integer size,
                              @RequestParam(value = "offset", required = false) Integer offset) {
        if (Objects.isNull(size)) {
            size = 10;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .offset(offset)
                .size(size).build();

        return electricityCabinetService.queryList(electricityCabinetQuery);
    }



}