package com.xiliulou.electricity.controller;

import com.xiliulou.electricity.service.ElectricityCabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


}