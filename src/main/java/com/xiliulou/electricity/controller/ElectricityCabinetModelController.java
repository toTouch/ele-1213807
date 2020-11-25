package com.xiliulou.electricity.controller;

import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 换电柜型号表(TElectricityCabinetModel)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@RestController
@RequestMapping("tElectricityCabinetModel")
public class ElectricityCabinetModelController {
    /**
     * 服务对象
     */
    @Autowired
    private ElectricityCabinetModelService electricityCabinetModelService;


}