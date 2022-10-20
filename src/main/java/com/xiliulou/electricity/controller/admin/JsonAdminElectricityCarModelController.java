package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.ElectricityCabinetModelQuery;
import com.xiliulou.electricity.query.ElectricityCarModelQuery;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 换电柜型号表(TElectricityCarModel)表控制层
 *
 * @author makejava
 * @since 2022-06-06 16:31:04
 */
@RestController
@Slf4j
public class JsonAdminElectricityCarModelController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    StoreService storeService;
    @Autowired
    FranchiseeService franchiseeService;

    //新增换电柜车辆型号
    @PostMapping(value = "/admin/electricityCarModel")
    public R save(@RequestBody @Validated ElectricityCarModel electricityCarModel) {
        return electricityCarModelService.save(electricityCarModel);
    }

    //修改换电柜车辆型号
    @PutMapping(value = "/admin/electricityCarModel")
    @Log(title = "修改换电柜车辆型号")
    public R update(@RequestBody ElectricityCarModel electricityCarModel) {
        return electricityCarModelService.edit(electricityCarModel);
    }

    //删除换电柜型号
    @DeleteMapping(value = "/admin/electricityCarModel/{id}")
    @Log(title = "删除换电柜型号")
    public R delete(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCarModelService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCarModel/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "storeId", required = false) Long storeId,
                       @RequestParam(value = "uid", required = false) Long uid) {
        if (size < 0 || size > 50 && size < 1000) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            Store store = storeService.queryByUid(user.getUid());
            if (Objects.nonNull(store)) {
                franchiseeId = store.getFranchiseeId();
            }
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                && !Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .franchiseeId(franchiseeId)
                .storeId(storeId)
                .uid(uid)
                .tenantId(tenantId).build();

        return electricityCarModelService.queryList(electricityCarModelQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCarModel/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name) {

        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Long franchiseeId = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            Store store = storeService.queryByUid(user.getUid());
            if (Objects.nonNull(store)) {
                franchiseeId = store.getFranchiseeId();
            }
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                && !Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        ElectricityCarModelQuery electricityCarModelQuery = ElectricityCarModelQuery.builder()
                .franchiseeId(franchiseeId)
                .name(name)
                .tenantId(tenantId).build();

        return electricityCarModelService.queryCount(electricityCarModelQuery);
    }


}
