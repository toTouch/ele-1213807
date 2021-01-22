package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.BindElectricityCabinetQuery;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class StoreAdminController {
    /**
     * 服务对象
     */
    @Autowired
    StoreService storeService;

    //新增门店
    @PostMapping(value = "/admin/store")
    public R save(@RequestBody @Validated(value = CreateGroup.class) StoreAddAndUpdate storeAddAndUpdate) {
        return storeService.save(storeAddAndUpdate);
    }

    //修改门店
    @PutMapping(value = "/admin/store")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) StoreAddAndUpdate storeAddAndUpdate) {
        return storeService.edit(storeAddAndUpdate);
    }

    //删除门店
    @DeleteMapping(value = "/admin/store/{id}")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return storeService.delete(id);
    }


    //列表查询
    @GetMapping(value = "/admin/store/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "batteryService", required = false) Integer batteryService,
                       @RequestParam(value = "carService", required = false) Integer carService,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        StoreQuery storeQuery = StoreQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .sn(sn)
                .address(address)
                .batteryService(batteryService)
                .carService(carService)
                .usableStatus(usableStatus).build();

        return storeService.queryList(storeQuery);
    }

    //禁用门店
    @PostMapping(value = "/admin/store/disable/{id}")
    public R disable(@PathVariable("id") Integer id) {
        return storeService.disable(id);
    }


    //启用门店
    @PostMapping(value = "/admin/store/reboot/{id}")
    public R reboot(@PathVariable("id") Integer id) {
        return storeService.reboot(id);
    }

    //门店绑定电柜
    @PostMapping(value = "/admin/store/bindElectricityCabinet")
    public R bindElectricityCabinet(@RequestBody @Validated(value = CreateGroup.class) BindElectricityCabinetQuery bindElectricityCabinetQuery){
        return storeService.bindElectricityCabinet(bindElectricityCabinetQuery);
    }


}