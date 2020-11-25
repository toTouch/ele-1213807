package com.xiliulou.electricity.controller;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@RestController
@RequestMapping("tElectricityCabinetBox")
public class ElectricityCabinetBoxController {
    /**
     * 服务对象
     */
    @Autowired
    private ElectricityCabinetBoxService electricityCabinetBoxService;

    //列表查询
    @GetMapping(value = "/admin/electricityCabinetBox/list")
    public R queryList(@RequestParam(value = "size", required = false) Integer size,
                       @RequestParam(value = "offset", required = false) Integer offset) {
        if (Objects.isNull(size)) {
            size = 10;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
        }

        ElectricityCabinetBoxQuery electricityCabinetBoxQuery = ElectricityCabinetBoxQuery.builder()
                .offset(offset)
                .size(size).build();

        return electricityCabinetBoxService.queryList(electricityCabinetBoxQuery);
    }



}