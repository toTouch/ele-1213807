package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.BindElectricityBatteryQuery;
import com.xiliulou.electricity.query.BindFranchiseeQuery;
import com.xiliulou.electricity.query.FranchiseeAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
@Slf4j
public class JsonAdminFranchiseeController {
    /**
     * 服务对象
     */
    @Autowired
    FranchiseeService franchiseeService;

    //新增加盟商
    @PostMapping(value = "/admin/franchisee")
    public R save(@RequestBody @Validated(value = CreateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        return franchiseeService.save(franchiseeAddAndUpdate);
    }

    //修改加盟商
    @PutMapping(value = "/admin/franchisee")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        return franchiseeService.edit(franchiseeAddAndUpdate);
    }

    //删除加盟商
    @DeleteMapping(value = "/admin/franchisee/{id}")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return franchiseeService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/franchisee/list")
    public R queryList(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        FranchiseeQuery franchiseeQuery = FranchiseeQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(tenantId).build();

        return franchiseeService.queryList(franchiseeQuery);

    }

    //列表查询
    @GetMapping(value = "/admin/franchisee/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {


        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        FranchiseeQuery franchiseeQuery = FranchiseeQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(tenantId).build();

        return franchiseeService.queryCount(franchiseeQuery);

    }

    //分配电池
    @PostMapping(value = "/admin/store/bindElectricityBattery")
    public R bindElectricityBattery(@RequestBody @Validated(value = CreateGroup.class) BindElectricityBatteryQuery bindElectricityBatteryQuery){
        return franchiseeService.bindElectricityBattery(bindElectricityBatteryQuery);
    }

    //查询电池
    @GetMapping(value = "/admin/store/getElectricityBatteryList/{id}")
    public R getElectricityBatteryList(@PathVariable("id") Integer id){
        return franchiseeService.getElectricityBatteryList(id);
    }






}
