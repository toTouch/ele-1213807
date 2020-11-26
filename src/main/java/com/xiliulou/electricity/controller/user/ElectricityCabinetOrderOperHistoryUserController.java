package com.xiliulou.electricity.controller.user;

import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
@RestController
@RequestMapping("tElectricityCabinetOrderOperHistory")
public class ElectricityCabinetOrderOperHistoryUserController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;


}