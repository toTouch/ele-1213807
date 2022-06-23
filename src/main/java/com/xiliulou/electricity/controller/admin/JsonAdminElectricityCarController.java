package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.sms.SmsService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

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

    //删除换电柜车辆
    @DeleteMapping(value = "/admin/electricityCar/{id}")
    public R delete(@PathVariable("id") Integer id) {
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

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if(Objects.equals(user.getType(),User.TYPE_USER_STORE)){
            Store store=storeService.queryByUid(user.getUid());
            if (Objects.nonNull(store)){
                storeId=store.getId();
            }
        }

        Long franchiseeId = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                && !Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        System.out.println("用户类型========================"+user.getType());

        System.out.println("门店Id========================"+storeId);

        System.out.println("加盟商Id========================"+franchiseeId);

        ElectricityCarQuery electricityCarQuery = ElectricityCarQuery.builder()
                .size(size)
                .offset(offset)
                .sn(sn)
                .model(model)
                .Phone(phone)
                .status(status)
                .storeId(storeId)
                .franchiseeId(franchiseeId)
                .batterySn(batterySn)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(tenantId).build();
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

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if(Objects.equals(user.getType(),User.TYPE_USER_STORE)){
            Store store=storeService.queryByUid(user.getUid());
            if (Objects.nonNull(store)){
                storeId=store.getId();
            }
        }

        Long franchiseeId = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)
                && !Objects.equals(user.getType(), User.TYPE_USER_STORE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        ElectricityCarQuery electricityCarQuery = ElectricityCarQuery.builder()
                .sn(sn)
                .model(model)
                .Phone(phone)
                .status(status)
                .storeId(storeId)
                .franchiseeId(franchiseeId)
                .batterySn(batterySn)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(tenantId).build();

        return electricityCarService.queryCount(electricityCarQuery);
    }

    //车辆绑定用户
    @PostMapping("/admin/electricityCar/bindUser")
    public R bindUser(@RequestBody @Validated(value = CreateGroup.class) ElectricityCarBindUser electricityCarBindUser) {
        return electricityCarService.bindUser(electricityCarBindUser);
    }


    //用户解绑车辆
    @PostMapping("/admin/electricityCar/unBindUser")
    public R unBindUser(@RequestBody @Validated(value = CreateGroup.class) ElectricityCarBindUser electricityCarBindUser) {
        return electricityCarService.unBindUser(electricityCarBindUser);
    }

}
