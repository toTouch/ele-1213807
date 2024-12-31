package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.exchange.ElectricityCabinetBoxLockPageQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxLockService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 换电柜锁仓仓门(TElectricityCabinetBoxLock)表控制层
 *
 * @author renhang
 */
@RestController
public class JsonAdminElectricityCabinetBoxLockController {

    @Resource
    private ElectricityCabinetBoxLockService electricityCabinetBoxLockService;


    @PostMapping(value = "/admin/electricityCabinetBoxLock/list")
    public R queryList(@RequestBody ElectricityCabinetBoxLockPageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        return R.ok(electricityCabinetBoxLockService.queryList(query));
    }


    @PostMapping(value = "/admin/electricityCabinetBoxLock/count")
    public R queryCount(@RequestBody ElectricityCabinetBoxLockPageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        return R.ok(electricityCabinetBoxLockService.queryCount(query));
    }

    @PostMapping(value = "/admin/electricityCabinet/enableBoxCell")
    public R enableBoxCell(@RequestBody EleOuterCommandQuery eleOuterCommandQuery) {
        return electricityCabinetBoxLockService.enableBoxCell(eleOuterCommandQuery);
    }

}
