package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.sms.SmsService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.entity.User;
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
    ElectricityCabinetService electricityCabinetService;
    @Qualifier("alibabaSmsService")
    @Autowired
    SmsService smsService;
    @Autowired
    StoreService storeService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    @Autowired
    RedisService redisService;
    @Autowired
    UserTypeFactory userTypeFactory;
    @Autowired
    ElectricityCarService electricityCarService;

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
                       @RequestParam(value = "storeId", required = false) Integer storeId,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "batterySn", required = false) String batterySn,
                       @RequestParam(value = "createTime", required = false) Long createTime) {
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
        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                return R.ok(new ArrayList<>());
            }
        }

        ElectricityCarQuery electricityCarQuery= ElectricityCarQuery.builder()
                .size(size)
                .offset(offset)
                .sn(sn)
                .model(model)
                .Phone(phone)
                .status(status)
                .storeId(storeId)
                .batterySn(batterySn)
                .createTime(createTime)
                .tenantId(tenantId).build();



        return electricityCarService.queryList(electricityCarQuery);
    }

    //列表数量查询
    @GetMapping(value = "/admin/electricityCar/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "address", required = false) String address,
                        @RequestParam(value = "usableStatus", required = false) Integer usableStatus,
                        @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus,
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
        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                return R.ok();
            }
        }

        ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder()
                .name(name)
                .address(address)
                .usableStatus(usableStatus)
                .onlineStatus(onlineStatus)
                .beginTime(beginTime)
                .endTime(endTime)
                .eleIdList(eleIdList)
                .tenantId(tenantId).build();

        return electricityCabinetService.queryCount(electricityCabinetQuery);
    }

}
