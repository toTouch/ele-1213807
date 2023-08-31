package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 用户列表(TUserInfo)表控制层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@RestController
@Slf4j
public class JsonAdminUserInfoController extends BaseController {
    //全部
    private static final Integer MEMBERCARD_EXPIRE_TYPE_ALL=0;
    //没过期
    private static final Integer MEMBERCARD_EXPIRE_TYPE_NOT_EXPIRE=1;
    //三天过期
    private static final Integer MEMBERCARD_EXPIRE_TYPE_THREE=2;
    //七天过期
    private static final Integer MEMBERCARD_EXPIRE_TYPE_SEVEN=3;
    //已过期
    private static final Integer MEMBERCARD_EXPIRE_TYPE_EXPIRE=4;


    /**
     * 服务对象
     */
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserTypeFactory userTypeFactory;
    @Autowired
    UserDataScopeService userDataScopeService;
    @Autowired
    ActivityService activityService;

    //列表查询
    @GetMapping(value = "/admin/userInfo/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "batteryId", required = false) Long batteryId,
                       @RequestParam(value = "authStatus", required = false) Integer authStatus,
                       @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus,
                       @RequestParam(value = "batteryRentStatus", required = false) Integer batteryRentStatus,
                       @RequestParam(value = "batteryDepositStatus", required = false) Integer batteryDepositStatus,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "memberCardId", required = false) Long memberCardId,
                       @RequestParam(value = "cardName", required = false) String cardName,
                       @RequestParam(value = "sortType", required = false) Integer sortType,
                       @RequestParam(value = "cardPayCount", required = false) Integer cardPayCount,
                       @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
                       @RequestParam(value = "memberCardExpireType", required = false) Integer memberCardExpireType,
                       @RequestParam(value = "carMemberCardExpireType", required = false) Integer carMemberCardExpireType,
                       @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd,
                       @RequestParam(value = "carMemberCardExpireTimeBegin", required = false) Long carMemberCardExpireTimeBegin,
                       @RequestParam(value = "carMemberCardExpireTimeEnd", required = false) Long carMemberCardExpireTimeEnd,
                       @RequestParam(value = "userCreateBeginTime", required = false) Long userCreateBeginTime,
                       @RequestParam(value = "userCreateEndTime", required = false) Long userCreateEndTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
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
                .carMemberCardExpireType(carMemberCardExpireType)
                .carMemberCardExpireTimeBegin(carMemberCardExpireTimeBegin)
                .carMemberCardExpireTimeEnd(carMemberCardExpireTimeEnd)
                .uid(uid)
                .sortType(sortType)
                .cardPayCount(cardPayCount)
                .memberCardId(memberCardId)
                .cardName(cardName)
                .memberCardExpireType(memberCardExpireType)
                .batteryRentStatus(batteryRentStatus)
                .batteryDepositStatus(batteryDepositStatus)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .userCreateBeginTime(userCreateBeginTime)
                .userCreateEndTime(userCreateEndTime)
                .tenantId(TenantContextHolder.getTenantId()).build();

        verifyMemberCardExpireTimeEnd(userInfoQuery);
        verifyCarMemberCardExpireTimeEnd(userInfoQuery);

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
                            @RequestParam(value = "batteryId", required = false) Long batteryId,
                            @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus,
                            @RequestParam(value = "batteryRentStatus", required = false) Integer batteryRentStatus,
                            @RequestParam(value = "batteryDepositStatus", required = false) Integer batteryDepositStatus,
                            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                            @RequestParam(value = "uid", required = false) Long uid,
                            @RequestParam(value = "memberCardId", required = false) Long memberCardId,
                            @RequestParam(value = "sortType", required = false) Integer sortType,
                            @RequestParam(value = "cardName", required = false) String cardName,
                            @RequestParam(value = "cardPayCount", required = false) Integer cardPayCount,
                            @RequestParam(value = "memberCardExpireType", required = false) Integer memberCardExpireType,
                            @RequestParam(value = "userCreateBeginTime", required = false) Long userCreateBeginTime,
                            @RequestParam(value = "userCreateEndTime", required = false) Long userCreateEndTime,
                            @RequestParam(value = "carMemberCardExpireType", required = false) Integer carMemberCardExpireType,
                            @RequestParam(value = "carMemberCardExpireTimeBegin", required = false) Long carMemberCardExpireTimeBegin,
                            @RequestParam(value = "carMemberCardExpireTimeEnd", required = false) Long carMemberCardExpireTimeEnd,
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
                .batteryRentStatus(batteryRentStatus)
                .batteryDepositStatus(batteryDepositStatus)
                .sortType(sortType)
                .batteryId(batteryId)
                .memberCardExpireType(memberCardExpireType)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .carMemberCardExpireType(carMemberCardExpireType)
                .carMemberCardExpireTimeBegin(carMemberCardExpireTimeBegin)
                .carMemberCardExpireTimeEnd(carMemberCardExpireTimeEnd)
                .uid(uid)
                .memberCardId(memberCardId)
                .cardName(cardName)
                .cardPayCount(cardPayCount).userCreateBeginTime(userCreateBeginTime)
                .userCreateEndTime(userCreateEndTime)
                .tenantId(TenantContextHolder.getTenantId()).build();

        verifyMemberCardExpireTimeEnd(userInfoQuery);
        verifyCarMemberCardExpireTimeEnd(userInfoQuery);

        userInfoService.exportExcel(userInfoQuery, response);
    }

    //列表查询
    @GetMapping(value = "/admin/userInfo/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "memberCardExpireType", required = false) Integer memberCardExpireType,
                        @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
                        @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd,
                        @RequestParam(value = "carMemberCardExpireTimeBegin", required = false) Long carMemberCardExpireTimeBegin,
                        @RequestParam(value = "carMemberCardExpireTimeEnd", required = false) Long carMemberCardExpireTimeEnd,
                        @RequestParam(value = "carMemberCardExpireType", required = false) Integer carMemberCardExpireType,
                        @RequestParam(value = "userCreateBeginTime", required = false) Long userCreateTimeBegin,
                        @RequestParam(value = "userCreateEndTime", required = false) Long userCreateTimeEnd,
                        @RequestParam(value = "batteryId", required = false) Long batteryId,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "batteryRentStatus", required = false) Integer batteryRentStatus,
                        @RequestParam(value = "batteryDepositStatus", required = false) Integer batteryDepositStatus,
                        @RequestParam(value = "cardName", required = false) String cardName,
                        @RequestParam(value = "memberCardId", required = false) Long memberCardId,
                        @RequestParam(value = "cardPayCount", required = false) Integer cardPayCount,
                        @RequestParam(value = "authType", required = false) Integer authType,
                        @RequestParam(value = "sortType", required = false) Integer sortType,
                        @RequestParam(value = "authStatus", required = false) Integer authStatus,
                        @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus) {
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .name(name)
                .phone(phone)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .carMemberCardExpireType(carMemberCardExpireType)
                .carMemberCardExpireTimeBegin(carMemberCardExpireTimeBegin)
                .carMemberCardExpireTimeEnd(carMemberCardExpireTimeEnd)
                .cardName(cardName)
                .uid(uid)
                .batteryId(batteryId)
                .memberCardId(memberCardId)
                .authStatus(authStatus)
                .authType(authType)
                .sortType(sortType)
                .serviceStatus(serviceStatus)
                .batteryRentStatus(batteryRentStatus)
                .batteryDepositStatus(batteryDepositStatus)
                .cardPayCount(cardPayCount)
                .memberCardExpireType(memberCardExpireType)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .userCreateBeginTime(userCreateTimeBegin)
                .userCreateEndTime(userCreateTimeEnd)
                .tenantId(TenantContextHolder.getTenantId()).build();

        verifyMemberCardExpireTimeEnd(userInfoQuery);
        verifyCarMemberCardExpireTimeEnd(userInfoQuery);

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
        R result= userInfoService.verifyAuth(id, authStatus);

        //人工审核成功后，触发活动处理流程
        UserInfo userInfo = userInfoService.queryByIdFromDB(id);
        ActivityProcessDTO activityProcessDTO = new ActivityProcessDTO();
        activityProcessDTO.setUid(userInfo.getUid());
        activityProcessDTO.setActivityType(ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode());
        activityProcessDTO.setTraceId(IdUtil.simpleUUID());
        log.info("handle activity after manual review success: {}", JsonUtil.toJson(activityProcessDTO));
        activityService.asyncProcessActivity(activityProcessDTO);

        return result;
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

    /**
     * 实名认证审核列表
     * @return
     */
    @GetMapping(value = "/admin/userInfo/list/v2")
    public R queryListV2(@RequestParam(value = "size") Long size, @RequestParam(value = "offset") Long offset,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "authType", required = false) Integer authType,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "authStatus", required = false) Integer authStatus) {
        if (size < 0 || size > 50) {
            size = 50L;
        }
    
        if (offset < 0) {
            offset = 0L;
        }
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder().offset(offset).size(size).name(name).phone(phone).uid(uid).authType(authType)
                .beginTime(beginTime).endTime(endTime).authStatus(authStatus).franchiseeIds(franchiseeIds).storeIds(storeIds)
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
            @RequestParam(value = "authType", required = false) Integer authType,
            @RequestParam(value = "serviceStatus", required = false) Integer serviceStatus) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }
        
        UserInfoQuery userInfoQuery = UserInfoQuery.builder().name(name).phone(phone)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin).memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .cardName(cardName).uid(uid).nowElectricityBatterySn(nowElectricityBatterySn).memberCardId(memberCardId)
                .authStatus(authStatus).serviceStatus(serviceStatus).franchiseeIds(franchiseeIds).authType(authType)
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
    
    /**
     * 会员列表详情信息（基本信息）
     */
    @GetMapping(value = "/admin/userInfo/details/basicInfo")
    public R queryDetailsBasicInfo(@RequestParam("uid") Long uid) {
        return userInfoService.queryDetailsBasicInfo(uid);
    }
    
    /**
     * 会员列表详情信息（电池信息）
     */
    @GetMapping(value = "/admin/userInfo/details/batteryInfo")
    public R queryDetailsBatteryInfo(@RequestParam("uid") Long uid) {
        return userInfoService.queryDetailsBatteryInfo(uid);
    }
    
    /**
     * 会员列表详情信息（车辆信息）
     */
    @GetMapping(value = "/admin/userInfo/details/carInfo")
    public R queryDetailsCarInfo(@RequestParam("uid") Long uid) {
        return userInfoService.queryDetailsCarInfo(uid);
    }


    private void verifyMemberCardExpireTimeEnd(UserInfoQuery userInfoQuery) {
        if (Objects.isNull(userInfoQuery.getMemberCardExpireType())) {
            return;
        }

        if (Objects.equals(userInfoQuery.getMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_ALL)) {
            return;
        }

        if (Objects.isNull(userInfoQuery.getMemberCardExpireTimeBegin()) && Objects.isNull(userInfoQuery.getMemberCardExpireTimeEnd())) {
            Long memberCardExpireTimeEnd = null;
            Long memberCardExpireTimeBegin = null;

            if (Objects.equals(userInfoQuery.getMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_NOT_EXPIRE)) {
                memberCardExpireTimeBegin = System.currentTimeMillis();
            } else if (Objects.equals(userInfoQuery.getMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_THREE)) {
                memberCardExpireTimeBegin = System.currentTimeMillis();
                memberCardExpireTimeEnd = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L;
            } else if (Objects.equals(userInfoQuery.getMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_SEVEN)) {
                memberCardExpireTimeBegin = System.currentTimeMillis();
                memberCardExpireTimeEnd = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L;
            } else if (Objects.equals(userInfoQuery.getMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_EXPIRE)) {
                memberCardExpireTimeEnd = System.currentTimeMillis();
            }
            userInfoQuery.setMemberCardExpireTimeBegin(memberCardExpireTimeBegin);
            userInfoQuery.setMemberCardExpireTimeEnd(memberCardExpireTimeEnd);
        }
    }

    private void verifyCarMemberCardExpireTimeEnd(UserInfoQuery userInfoQuery) {
        if (Objects.isNull(userInfoQuery.getCarMemberCardExpireType())) {
            return;
        }

        if (Objects.equals(userInfoQuery.getCarMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_ALL)) {
            return;
        }

        if (Objects.isNull(userInfoQuery.getCarMemberCardExpireTimeBegin()) && Objects.isNull(userInfoQuery.getCarMemberCardExpireTimeEnd())) {
            Long carMemberCardExpireTimeEnd = null;
            Long carMemberCardExpireTimeBegin = null;

            if (Objects.equals(userInfoQuery.getCarMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_NOT_EXPIRE)) {
                carMemberCardExpireTimeBegin = System.currentTimeMillis();
            } else if (Objects.equals(userInfoQuery.getCarMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_THREE)) {
                carMemberCardExpireTimeBegin = System.currentTimeMillis();
                carMemberCardExpireTimeEnd = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L;
            } else if (Objects.equals(userInfoQuery.getCarMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_SEVEN)) {
                carMemberCardExpireTimeBegin = System.currentTimeMillis();
                carMemberCardExpireTimeEnd = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L;
            } else if (Objects.equals(userInfoQuery.getCarMemberCardExpireType(), MEMBERCARD_EXPIRE_TYPE_EXPIRE)) {
                carMemberCardExpireTimeEnd = System.currentTimeMillis();
            }
            userInfoQuery.setCarMemberCardExpireTimeBegin(carMemberCardExpireTimeBegin);
            userInfoQuery.setCarMemberCardExpireTimeEnd(carMemberCardExpireTimeEnd);
        }
    }
    
    /**
     * 下拉列表搜索
     */
    @GetMapping(value = "/admin/userInfo/search")
    public R userInfoSearch(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "name", required = false) String name) {
        if (Objects.isNull(size) || size < 0 || size > 20) {
            size = 20L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        return userInfoService.userInfoSearch(size, offset, name);
    }

    @GetMapping("/admin/userInfo/exportCarRentalExcel")
    public void exportCarRentalExcel(@RequestParam(value = "uid", required = false) Long uid,
                                     @RequestParam(value = "sortType", required = false) Integer sortType,
                                     @RequestParam(value = "sortBy", required = false) String sortBy,
                                     @RequestParam(value = "carRentalExpireTimeBegin", required = false) Long carRentalExpireTimeBegin,
                                     @RequestParam(value = "carRentalExpireTimeEnd", required = false) Long carRentalExpireTimeEnd,
                                     @RequestParam(value = "carRentalExpireType", required = false) Integer carRentalExpireType , HttpServletResponse response) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            throw new CustomBusinessException("当前用户不存在");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER) && !Objects.equals(user.getType(), User.TYPE_USER_NORMAL_ADMIN)) {
            log.info("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            throw new CustomBusinessException("用户权限不足");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return;
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return;
            }
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .carMemberCardExpireType(carRentalExpireType)
                .carMemberCardExpireTimeBegin(carRentalExpireTimeBegin)
                .carMemberCardExpireTimeEnd(carRentalExpireTimeEnd)
                .uid(uid)
                .sortType(sortType)
                .sortBy(sortBy)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        verifyCarMemberCardExpireTimeEnd(userInfoQuery);

        userInfoService.exportCarRentalExcel(userInfoQuery, response);
    }

    @GetMapping(value = "/admin/userInfo/carRentalList")
    public R queryCarRentalList(@RequestParam("size") Long size,
                                @RequestParam("offset") Long offset,
                                @RequestParam(value = "uid", required = false) Long uid,
                                @RequestParam(value = "name", required = false) String name,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "sortType", required = false) Integer sortType,
                                @RequestParam(value = "sortBy", required = false) String sortBy,
                                @RequestParam(value = "carRentalExpireTimeBegin", required = false) Long carRentalExpireTimeBegin,
                                @RequestParam(value = "carRentalExpireTimeEnd", required = false) Long carRentalExpireTimeEnd,
                                @RequestParam(value = "carRentalExpireType", required = false) Integer carRentalExpireType ) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .offset(offset)
                .size(size)
                .carMemberCardExpireType(carRentalExpireType)
                .carMemberCardExpireTimeBegin(carRentalExpireTimeBegin)
                .carMemberCardExpireTimeEnd(carRentalExpireTimeEnd)
                .uid(uid)
                .name(name)
                .phone(phone)
                .sortType(sortType)
                .sortBy(sortBy)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        verifyCarMemberCardExpireTimeEnd(userInfoQuery);

        return userInfoService.queryCarRentalList(userInfoQuery);
    }

    @GetMapping(value = "/admin/userInfo/carRentalCount")
    public R queryCount(@RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "sortType", required = false) Integer sortType,
                        @RequestParam(value = "sortBy", required = false) String sortBy,
                        @RequestParam(value = "carRentalExpireTimeBegin", required = false) Long carRentalExpireTimeBegin,
                        @RequestParam(value = "carRentalExpireTimeEnd", required = false) Long carRentalExpireTimeEnd,
                        @RequestParam(value = "carRentalExpireType", required = false) Integer carRentalExpireType) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .carMemberCardExpireType(carRentalExpireType)
                .carMemberCardExpireTimeBegin(carRentalExpireTimeBegin)
                .carMemberCardExpireTimeEnd(carRentalExpireTimeEnd)
                .uid(uid)
                .name(name)
                .phone(phone)
                .sortType(sortType)
                .sortBy(sortBy)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();


        verifyCarMemberCardExpireTimeEnd(userInfoQuery);

        return userInfoService.queryCarRentalCount(userInfoQuery);
    }


    //列表查询
    @GetMapping(value = "/admin/userInfo/eleList")
    public R queryEleList(@RequestParam("size") Long size,
                          @RequestParam("offset") Long offset,
                          @RequestParam(value = "uid", required = false) Long uid,
                          @RequestParam(value = "batteryRentStatus", required = false) Integer batteryRentStatus,
                          @RequestParam(value = "memberCardExpireType", required = false) Integer memberCardExpireType,
                          @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
                          @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd,
                          @RequestParam(value = "sortType", required = false) Integer sortType,
                          @RequestParam(value = "sortBy", required = false) String sortBy) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()
                .offset(offset)
                .size(size)
                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .uid(uid)
                .memberCardExpireType(memberCardExpireType)
                .batteryRentStatus(batteryRentStatus)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .sortBy(sortBy)
                .sortType(sortType)
                .tenantId(TenantContextHolder.getTenantId()).build();

        verifyMemberCardExpireTimeEnd(userInfoQuery);

        return userInfoService.queryEleList(userInfoQuery);
    }
    //列表查询
    @GetMapping(value = "/admin/userInfo/eleListCount")
    public R queryEleListCount(
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "batteryRentStatus", required = false) Integer batteryRentStatus,
            @RequestParam(value = "memberCardExpireType", required = false) Integer memberCardExpireType,
            @RequestParam(value = "memberCardExpireTimeBegin", required = false) Long memberCardExpireTimeBegin,
            @RequestParam(value = "memberCardExpireTimeEnd", required = false) Long memberCardExpireTimeEnd) {


        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        UserInfoQuery userInfoQuery = UserInfoQuery.builder()

                .memberCardExpireTimeBegin(memberCardExpireTimeBegin)
                .memberCardExpireTimeEnd(memberCardExpireTimeEnd)
                .uid(uid)
                .memberCardExpireType(memberCardExpireType)
                .batteryRentStatus(batteryRentStatus)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .tenantId(TenantContextHolder.getTenantId()).build();

        verifyMemberCardExpireTimeEnd(userInfoQuery);

        return userInfoService.queryEleListCount(userInfoQuery);
    }
}
