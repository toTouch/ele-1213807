package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
public class JsonAdminStoreController {
    /**
     * 服务对象
     */
    @Autowired
    StoreService storeService;

    @Autowired
    FranchiseeService franchiseeService;

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
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        StoreQuery storeQuery = StoreQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .tenantId(tenantId).build();


        return storeService.queryList(storeQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/store/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "usableStatus", required = false) Integer usableStatus) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        StoreQuery storeQuery = StoreQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .tenantId(tenantId).build();

        return storeService.queryCount(storeQuery);
    }

    //加盟商列表查询
    @GetMapping(value = "/admin/store/listByFranchisee")
    public R listByFranchisee(@RequestParam(value = "size", required = false) Long size,
                       @RequestParam(value = "offset", required = false) Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "address", required = false) String address,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime,
                       @RequestParam(value = "usableStatus", required = false) Integer usableStatus) {
        if (Objects.isNull(size)) {
            size = 10L;
        }

        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();


        StoreQuery storeQuery = StoreQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .tenantId(tenantId).build();


        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //1、先找到加盟商
        Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchisee)){
            return R.ok();
        }

        List<Store> storeList= storeService.queryByFranchiseeId(franchisee.getId());

        if(ObjectUtil.isEmpty(storeList)){
            return R.ok();
        }
        //2、再找加盟商绑定的门店
        List<Integer> storeIdList=new ArrayList<>();
        for (Store store:storeList) {
            storeIdList.add(store.getId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return  R.ok();
        }

        storeQuery.setStoreIdList(storeIdList);

        return storeService.queryList(storeQuery);
    }

    //加盟商列表查询
    @GetMapping(value = "/admin/store/queryCountByFranchisee")
    public R queryCountByFranchisee(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "usableStatus", required = false) Integer usableStatus) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        StoreQuery storeQuery = StoreQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .address(address)
                .usableStatus(usableStatus)
                .tenantId(tenantId).build();


        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //1、先找到加盟商
        Franchisee franchisee=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchisee)){
            return R.ok(0);
        }

        List<Store> storeList= storeService.queryByFranchiseeId(franchisee.getId());

        if(ObjectUtil.isEmpty(storeList)){
            return  R.ok(0);
        }
        //2、再找加盟商绑定的门店
        List<Integer> storeIdList=new ArrayList<>();
        for (Store store:storeList) {
            storeIdList.add(store.getId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return  R.ok(0);
        }

        storeQuery.setStoreIdList(storeIdList);

        return storeService.queryCountByFranchisee(storeQuery);
    }


    //禁启用门店
    @PutMapping(value = "/admin/store/updateStatus")
    public R updateStatus(@RequestParam("id") Integer id,@RequestParam("usableStatus") Integer usableStatus) {
        return storeService.updateStatus(id,usableStatus);
    }



}
