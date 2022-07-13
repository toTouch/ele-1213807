package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserMoveHistory;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 用户列表(TUserInfo)表控制层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@RestController
public class JsonAdminUserInfoController {
    /**
     * 服务对象
     */
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    RedisService redisService;

    //列表查询
    @GetMapping(value = "/admin/userInfo/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "batterySn",required = false) String batterySn,
                       @RequestParam(value = "authStatus", required = false) Integer authStatus,
                       @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "memberCardId",required = false) Long memberCardId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .batterySn(batterySn)
                .franchiseeId(franchiseeId)
                .authStatus(authStatus)
                .serviceStatus(serviceStatus)
                .uid(uid)
                .memberCardId(memberCardId)
                .tenantId(tenantId).build();

        return userInfoService.queryList(userInfoQuery);
    }

    //列表查询
    @GetMapping(value = "/admin/userInfo/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "authStatus", required = false) Integer authStatus,
                        @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .authStatus(authStatus)
                .serviceStatus(serviceStatus)
                .tenantId(tenantId).build();

        return userInfoService.queryCount(userInfoQuery);
    }

    //禁/启用
    @PutMapping(value = "/admin/userInfo/updateStatus")
    public R updateStatus(@RequestParam("id") Long id, @RequestParam("usableStatus") Integer usableStatus) {
        return userInfoService.updateStatus(id, usableStatus);
    }

    //后台审核实名认证
    @PostMapping(value = "/admin/userInfo/verifyAuth")
    public R verifyAuth(@RequestParam("id") Long id, @RequestParam("authStatus") Integer authStatus) {
        return userInfoService.verifyAuth(id, authStatus);
    }

    //编辑实名认证
    @PutMapping(value = "/admin/userInfo")
    public R updateAuth(@RequestBody UserInfo userInfo) {
        return userInfoService.updateAuth(userInfo);
    }

    //订单周期删缓存
    @PostMapping(value = "/admin/userInfo/deleteOrderCache")
    public R deleteOrderCache(@RequestParam("uid") Long uid) {
        redisService.delete(ElectricityCabinetConstant.ORDER_TIME_UID + uid);
        return R.ok();
    }

    //列表查询
    @GetMapping(value = "/admin/userInfo/list/v2")
    public R queryListV2(@RequestParam(value = "size") Long size,
                         @RequestParam(value = "offset") Long offset,
                         @RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "beginTime", required = false) Long beginTime,
                         @RequestParam(value = "endTime", required = false) Long endTime,
                         @RequestParam(value = "authStatus", required = false) Integer authStatus) {
        if (size < 0 || size > 50) {
            size = 50L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .authStatus(authStatus)
                .tenantId(tenantId).build();

        return userInfoService.queryUserAuthInfo(userInfoQuery);
    }

    //绑定电池
    @PutMapping(value = "/admin/userInfo/bindBattery")
    public R webBindBattery(@RequestBody @Validated(value = UpdateGroup.class) UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {
        return userInfoService.webBindBattery(userInfoBatteryAddAndUpdate);
    }

    //解绑电池
    @PutMapping(value = "/admin/userInfo/unBindBattery/{id}")
    public R webUnBindBattery(@PathVariable("id") Long id) {
        return userInfoService.webUnBindBattery(id);
    }

    //迁移用户数据
    @PostMapping(value = "/admin/userInfo/userMove")
    public R userMove(@RequestBody UserMoveHistory userMoveHistory) {
        return userInfoService.userMove(userMoveHistory);
    }

    /**
     * 查询用户所属加盟商
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/queryUserBelongFranchisee/{id}")
    public R queryUserBelongFranchisee(@PathVariable("id") Long id) {
        return userInfoService.queryUserBelongFranchisee(id);
    }

    /**
     * 用户的总消费金额
     * @return
     */
    @GetMapping(value = "/admin/queryUserAllConsumption/{id}")
    public R queryUserAllConsumption(@PathVariable("id") Long id){
        return userInfoService.queryUserAllConsumption(id);
    }

}
