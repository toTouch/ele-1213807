package com.xiliulou.electricity.controller.admin;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
public class ElectricityCabinetOrderOperHistoryAdminController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;


}