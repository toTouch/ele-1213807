package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


/**
 * 换电柜型号表(TElectricityCabinetModel)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@RestController
public class JsonAdminElectricityCabinetModelController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCabinetModelService electricityCabinetModelService;

    //新增换电柜型号
    @PostMapping(value = "/admin/electricityCabinetModel")
    public R save(@RequestBody @Validated ElectricityCabinetModel electricityCabinetModel) {
        return electricityCabinetModelService.save(electricityCabinetModel);
    }

    //修改换电柜型号
    @PutMapping(value = "/admin/electricityCabinetModel")
    public R update(@RequestBody ElectricityCabinetModel electricityCabinetModel) {
        return electricityCabinetModelService.edit(electricityCabinetModel);
    }

    //删除换电柜型号
    @DeleteMapping(value = "/admin/electricityCabinetModel/{id}")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCabinetModelService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCabinetModel/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        Integer tenantId = TenantContextHolder.getTenantId();


        ElectricityCabinetModelQuery electricityCabinetModelQuery = ElectricityCabinetModelQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .tenantId(tenantId).build();

        return electricityCabinetModelService.queryList(electricityCabinetModelQuery);
    }


    //列表查询
    @GetMapping(value = "/admin/electricityCabinetModel/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name) {


        Integer tenantId = TenantContextHolder.getTenantId();


        ElectricityCabinetModelQuery electricityCabinetModelQuery = ElectricityCabinetModelQuery.builder()
                .name(name)
                .tenantId(tenantId).build();

        return electricityCabinetModelService.queryCount(electricityCabinetModelQuery);
    }


}
