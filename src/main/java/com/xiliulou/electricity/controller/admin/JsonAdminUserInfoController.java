package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;

import java.util.Objects;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户列表(TUserInfo)表控制层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@RestController
@Slf4j
public class JsonAdminUserInfoController extends BaseController {
    /**
     * 服务对象
     */
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserTypeFactory userTypeFactory;

    //列表查询
    @GetMapping(value = "/admin/userInfo/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "batteryId", required = false) Long batteryId,
                       @RequestParam(value = "authStatus", required = false) Integer authStatus,
                       @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "memberCardId", required = false) Long memberCardId,
                       @RequestParam(value = "cardName", required = false) String cardName,
                       @RequestParam(value = "sortType", required = false) Integer sortType,
                       @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
                       @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }


        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .batteryId(batteryId)
                .franchiseeId(franchiseeId)
                .authStatus(authStatus)
                .serviceStatus(serviceStatus)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .uid(uid)
                .sortType(sortType)
                .memberCardId(memberCardId)
                .cardName(cardName)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return userInfoService.queryList(userInfoQuery);
    }


    /**
     * 会员列表导出
     *
     * @param response
     */
    @GetMapping("/admin/userInfo/exportExcel")
    public void exportExcel(@RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "phone", required = false) String phone,
                            @RequestParam(value = "nowElectricityBatterySn", required = false) String nowElectricityBatterySn,
                            @RequestParam(value = "authStatus", required = false) Integer authStatus,
                            @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus,
                            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                            @RequestParam(value = "uid", required = false) Long uid,
                            @RequestParam(value = "memberCardId", required = false) Long memberCardId,
                            @RequestParam(value = "cardName", required = false) String cardName,
                            @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
                            @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd, HttpServletResponse response) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("查不到订单");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_NORMAL_ADMIN)) {
            log.info("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            throw new CustomBusinessException("用户权限不足");
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .name(name)
                .phone(phone)
                .nowElectricityBatterySn(nowElectricityBatterySn)
                .franchiseeId(franchiseeId)
                .authStatus(authStatus)
                .serviceStatus(serviceStatus)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .uid(uid)
                .memberCardId(memberCardId)
                .cardName(cardName)
                .tenantId(TenantContextHolder.getTenantId()).build();

        userInfoService.exportExcel(userInfoQuery, response);
    }

    //列表查询
    @GetMapping(value = "/admin/userInfo/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
                        @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd,
                        @RequestParam(value = "batteryId", required = false) Long batteryId,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "cardName", required = false) String cardName,
                        @RequestParam(value = "memberCardId", required = false) Long memberCardId,
                        @RequestParam(value = "authStatus", required = false) Integer authStatus,
                        @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus) {

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .name(name)
                .phone(phone)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .cardName(cardName)
                .uid(uid)
                .batteryId(batteryId)
                .memberCardId(memberCardId)
                .authStatus(authStatus)
                .serviceStatus(serviceStatus)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return userInfoService.queryCount(userInfoQuery);
    }

    //禁/启用
    @PutMapping(value = "/admin/userInfo/updateStatus")
    @Log(title = "禁/启用用户")
    public R updateStatus(@RequestParam("uid") Long uid, @RequestParam("usableStatus") Integer usableStatus) {
        return userInfoService.updateStatus(uid, usableStatus);
    }

    //后台审核实名认证
    @PostMapping(value = "/admin/userInfo/verifyAuth")
    @Log(title = "实名认证审核")
    public R verifyAuth(@RequestParam("id") Long id, @RequestParam("authStatus") Integer authStatus) {
        return userInfoService.verifyAuth(id, authStatus);
    }

    //编辑实名认证
    @PutMapping(value = "/admin/userInfo")
    @Log(title = "编辑实名认证")
    public R updateAuth(@RequestBody UserInfo userInfo) {
        return userInfoService.updateAuth(userInfo);
    }

    //订单周期删缓存
    @PostMapping(value = "/admin/userInfo/deleteOrderCache")
    public R deleteOrderCache(@RequestParam("uid") Long uid) {
        redisService.delete(CacheConstant.ORDER_TIME_UID + uid);
        return R.ok();
    }

    /**
     * 更新用户服务状态
     * @param uid
     * @param serviceStatus
     * @return
     */
//    @PutMapping(value = "/admin/userInfo/serviceStatus")
//    @Log(title = "修改用户租赁状态")
//    public R updateServiceStatus(@RequestParam("uid") Long uid,@RequestParam("serviceStatus") Integer serviceStatus){
//        return returnTripleResult(franchiseeUserInfoService.updateServiceStatus(uid,serviceStatus));
//    }

    /**
     * 修改用户电池租赁状态
     *
     * @param uid
     * @param batteryRentStatus
     * @return
     */
    @PutMapping(value = "/admin/userInfo/batteryRentStatus")
    @Log(title = "修改用户电池租赁状态")
    public R updateRentBatteryStatus(@RequestParam("uid") Long uid, @RequestParam("batteryRentStatus") Integer batteryRentStatus) {
        return returnTripleResult(userInfoService.updateRentBatteryStatus(uid, batteryRentStatus));
    }

    @PutMapping(value = "/admin/userInfo/carRentStatus")
    @Log(title = "修改用户车辆租赁状态")
    public R updateRentCarStatus(@RequestParam("uid") Long uid, @RequestParam("carRentStatus") Integer carRentStatus) {
        return returnTripleResult(userInfoService.updateRentCarStatus(uid, carRentStatus));
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

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .phone(phone)
                .beginTime(beginTime)
                .endTime(endTime)
                .authStatus(authStatus)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return userInfoService.queryUserAuthInfo(userInfoQuery);
    }

    @GetMapping(value = "/admin/authenticationUserInfo/queryCount")
    public R queryAuthenticationCount(@RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "phone", required = false) String phone,
                                      @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
                                      @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd,
                                      @RequestParam(value = "nowElectricityBatterySn", required = false) String nowElectricityBatterySn,
                                      @RequestParam(value = "uid", required = false) Long uid,
                                      @RequestParam(value = "cardName", required = false) String cardName,
                                      @RequestParam(value = "memberCardId", required = false) Long memberCardId,
                                      @RequestParam(value = "authStatus", required = false) Integer authStatus,
                                      @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus) {

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .name(name)
                .phone(phone)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .cardName(cardName)
                .uid(uid)
                .nowElectricityBatterySn(nowElectricityBatterySn)
                .memberCardId(memberCardId)
                .authStatus(authStatus)
                .serviceStatus(serviceStatus)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return userInfoService.queryAuthenticationCount(userInfoQuery);
    }

    //绑定电池
    @PutMapping(value = "/admin/userInfo/bindBattery")
    @Log(title = "后台绑定电池")
    public R webBindBattery(@RequestBody @Validated(value = UpdateGroup.class) UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {
        return userInfoService.webBindBattery(userInfoBatteryAddAndUpdate);
    }

    //解绑电池
    @PutMapping(value = "/admin/userInfo/unBindBattery/{uid}")
    @Log(title = "后台解绑电池")
    public R webUnBindBattery(@PathVariable("uid") Long uid) {
        return userInfoService.webUnBindBattery(uid);
    }

    /**
     * 查询用户所属加盟商
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/admin/queryUserBelongFranchisee/{id}")
    public R queryUserBelongFranchisee(@PathVariable("id") Long id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        return userInfoService.queryUserBelongFranchisee(id, tenantId);
    }

    /**
     * 用户的总消费金额
     *
     * @return
     */
    @GetMapping(value = "/admin/queryUserAllConsumption/{id}")
    public R queryUserAllConsumption(@PathVariable("id") Long id) {
        return userInfoService.queryUserAllConsumption(id);
    }

    /**
     * 会员列表删除
     */
    @DeleteMapping(value = "/admin/userInfo/{uid}")
    @Log(title = "会员列表删除")
    public R deleteUserInfo(@PathVariable("uid") Long uid) {
        return userInfoService.deleteUserInfo(uid);
    }

}
