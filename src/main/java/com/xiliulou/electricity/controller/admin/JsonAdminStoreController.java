package com.xiliulou.electricity.controller.admin;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeBind;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.StoreBind;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreBindElectricityCabinetQuery;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.FranchiseeBindService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreBindService;
import com.xiliulou.electricity.service.StoreService;
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
    StoreBindService storeBindService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    FranchiseeBindService franchiseeBindService;

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

    //加盟商列表查询
    @GetMapping(value = "/admin/store/listByFranchisee")
    public R listByFranchisee(@RequestParam(value = "size", required = false) Long size,
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


        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Franchisee> franchiseeList=franchiseeService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(franchiseeList)){
            return R.ok();
        }
        List<FranchiseeBind> franchiseeBinds=new ArrayList<>();
        for (Franchisee franchisee:franchiseeList) {
            List<FranchiseeBind> franchiseeBindList= franchiseeBindService.queryByFranchiseeId(franchisee.getId());
            franchiseeBinds.addAll(franchiseeBindList);
        }
        if(ObjectUtil.isEmpty(franchiseeBinds)){
            return R.ok();
        }
        List<Integer> storeIdList=new ArrayList<>();
        for (FranchiseeBind franchiseeBind:franchiseeBinds) {
            storeIdList.add(franchiseeBind.getStoreId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return R.ok();
        }
        storeQuery.setStoreIdList(storeIdList);

        return storeService.queryList(storeQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/store/listByStore")
    public R listByStore(@RequestParam(value = "size", required = false) Long size,
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

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<StoreBind> storeBindList=storeBindService.queryByUid(user.getUid());
        if(ObjectUtil.isEmpty(storeBindList)){
            return R.ok();
        }

        List<Integer> storeIdList=new ArrayList<>();
        for (StoreBind storeBind:storeBindList) {
            storeIdList.add(storeBind.getStoreId());
        }
        if(ObjectUtil.isEmpty(storeIdList)){
            return R.ok();
        }
        storeQuery.setStoreIdList(storeIdList);


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
    public R bindElectricityCabinet(@RequestBody @Validated(value = CreateGroup.class) StoreBindElectricityCabinetQuery storeBindElectricityCabinetQuery){
        return storeService.bindElectricityCabinet(storeBindElectricityCabinetQuery);
    }

    //门店绑定电柜查询
    @GetMapping(value = "/admin/store/getElectricityCabinetList/{id}")
    public R getElectricityCabinetList(@PathVariable("id") Integer id){
        return storeService.getElectricityCabinetList(id);
    }


}
