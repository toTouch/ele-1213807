package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityCarAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityCarBindUser;
import com.xiliulou.electricity.query.ElectricityCarMoveQuery;
import com.xiliulou.electricity.query.ElectricityCarQuery;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 换电柜表(TElectricityCar)表控制层
 *
 * @author makejava
 * @since 2022-06-06 16:00:14
 */
@RestController
@Slf4j
public class JsonAdminElectricityCarController {
    /**
     * 服务对象
     */
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCarService electricityCarService;
    @Autowired
    StoreService storeService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserDataScopeService userDataScopeService;
    
    @Autowired
    UserTypeFactory userTypeFactory;

    //新增换电柜车辆
    @PostMapping(value = "/admin/electricityCar")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCarAddAndUpdate electricityCarAddAndUpdate) {
        return electricityCarService.save(electricityCarAddAndUpdate);
    }

    //修改换电柜车辆
    @PutMapping(value = "/admin/electricityCar")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) ElectricityCarAddAndUpdate electricityCarAddAndUpdate) {
        return electricityCarService.edit(electricityCarAddAndUpdate);
    }

    //删除车辆
    @DeleteMapping(value = "/admin/electricityCar/{id}")
    @Log(title = "删除车辆")
    public R deleteCar(@PathVariable("id") Integer id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return electricityCarService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/electricityCar/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "model", required = false) String model,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "storeId", required = false) Long storeId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "batterySn", required = false) String batterySn,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }


        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds=null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }

            List<Store> stores = storeService.selectByFranchiseeIds(franchiseeIds);
            if(CollectionUtils.isEmpty(stores)){
                return R.ok(Collections.EMPTY_LIST);
            }

            storeIds=stores.stream().map(Store::getId).collect(Collectors.toList());
        }

        ElectricityCarQuery electricityCarQuery = ElectricityCarQuery.builder()
                .size(size)
                .offset(offset)
                .sn(sn)
                .model(model)
                .Phone(phone)
                .status(status)
                .storeId(storeId)
                .storeIds(storeIds)
                .batterySn(batterySn)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).build();
        return electricityCarService.queryList(electricityCarQuery);
    }

    //列表数量查询
    @GetMapping(value = "/admin/electricityCar/queryCount")
    public R queryCount(@RequestParam(value = "sn", required = false) String sn,
                        @RequestParam(value = "model", required = false) String model,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "storeId", required = false) Long storeId,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "batterySn", required = false) String batterySn,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds=null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }

            List<Store> stores = storeService.selectByFranchiseeIds(franchiseeIds);
            if(CollectionUtils.isEmpty(stores)){
                return R.ok(Collections.EMPTY_LIST);
            }

            storeIds=stores.stream().map(Store::getId).collect(Collectors.toList());
        }

        ElectricityCarQuery electricityCarQuery = ElectricityCarQuery.builder()
                .sn(sn)
                .model(model)
                .Phone(phone)
                .status(status)
                .storeId(storeId)
                .storeIds(storeIds)
                .batterySn(batterySn)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return electricityCarService.queryCount(electricityCarQuery);
    }

    //车辆绑定用户
    @PostMapping("/admin/electricityCar/bindUser")
    @Log(title = "车辆绑定用户")
    public R bindUser(@RequestBody @Validated(value = CreateGroup.class) ElectricityCarBindUser electricityCarBindUser) {
        return electricityCarService.bindUser(electricityCarBindUser);
    }


    //用户解绑车辆
    @PostMapping("/admin/electricityCar/unBindUser")
    @Log(title = "用户解绑车辆")
    public R unBindUser(@RequestBody @Validated(value = CreateGroup.class) ElectricityCarBindUser electricityCarBindUser) {
        return electricityCarService.unBindUser(electricityCarBindUser);
    }
    
    /**
     * 车辆总览
     *
     * @param
     * @param sn
     * @return
     */
    @GetMapping("/admin/car/electricityCar/overview")
    public R queryBatteryOverview(@RequestParam(value = "sn", required = false) String sn) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Integer> carIdList = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE) || Objects
                .equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
        
            carIdList = userTypeService.getCarIdListByyDataType(user);
            if (ObjectUtil.isEmpty(carIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
    
        return electricityCarService.queryElectricityCarOverview(sn, carIdList);
    }
    
    /**
     * 车辆统计
     *
     * @return
     */
    @GetMapping("/admin/electricityCar/statistics")
    public R batteryStatistical() {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Integer> carIdList = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE) || Objects
                .equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getDataType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userDataType={}", user.getDataType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            
            carIdList = userTypeService.getCarIdListByyDataType(user);
            if (ObjectUtil.isEmpty(carIdList)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
    
        return electricityCarService.batteryStatistical(carIdList, TenantContextHolder.getTenantId());
    }
    
    /**
     * 迁移车辆查询
     */
    @GetMapping("/admin/electricityCar/queryMoveCar")
    public R queryElectricityCarMove(@RequestParam("storeId") Long storeId) {
        return electricityCarService.queryElectricityCarMove(storeId);
    }
    
    /**
     * 车辆迁移
     */
    @PutMapping("/admin/electricityCar/moveCar")
    @Log(title = "车辆迁移")
    public R electricityCarMove(
            @RequestBody @Validated(value = UpdateGroup.class) ElectricityCarMoveQuery electricityCarMoveQuery) {
        return electricityCarService.electricityCarMove(electricityCarMoveQuery);
    }
}
