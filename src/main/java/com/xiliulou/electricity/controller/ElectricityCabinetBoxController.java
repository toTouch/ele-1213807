package com.xiliulou.electricity.controller;

import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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



}