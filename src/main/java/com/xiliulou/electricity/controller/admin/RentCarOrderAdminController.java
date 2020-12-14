package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.query.RentCarOrderQuery;
import com.xiliulou.electricity.service.RentCarOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * 租车记录(TRentCarOrder)表控制层
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@RestController
public class RentCarOrderAdminController {
    /**
     * 服务对象
     */
    @Autowired
    private RentCarOrderService rentCarOrderService;

    //列表查询
    @GetMapping(value = "/admin/rentCarOrder/list")
    public R queryList(@RequestParam(value = "size", required = false) Integer size,
                       @RequestParam(value = "offset", required = false) Integer offset,
                       @RequestParam(value = "batteryStoreId", required = false) Integer carStoreId,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0;
        }

        RentCarOrderQuery rentCarOrderQuery = RentCarOrderQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .carStoreId(carStoreId)
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status).build();

        return rentCarOrderService.queryList(rentCarOrderQuery);
    }

}