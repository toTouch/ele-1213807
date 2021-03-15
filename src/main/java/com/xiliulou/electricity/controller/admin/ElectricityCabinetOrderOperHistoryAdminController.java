package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;


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

    //换电柜订单查询
    @GetMapping("/admin/electricityCabinetOrderOperHistory/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "type", required = false) Integer type,
                       @RequestParam(value = "orderType", required = false) Integer orderType,
                       @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                       @RequestParam(value = "cellNo", required = false) Integer cellNo,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {

        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery = ElectricityCabinetOrderOperHistoryQuery.builder()
                .offset(offset)
                .size(size)
                .orderId(orderId)
                .status(status)
                .type(type)
                .orderType(orderType)
                .electricityCabinetId(electricityCabinetId)
                .cellNo(cellNo)
                .beginTime(beginTime)
                .endTime(endTime).build();
        return electricityCabinetOrderOperHistoryService.queryList(electricityCabinetOrderOperHistoryQuery);
    }


}