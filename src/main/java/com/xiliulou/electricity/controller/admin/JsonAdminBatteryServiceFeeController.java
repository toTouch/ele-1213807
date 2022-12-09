package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.BatteryServiceFeeQuery;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 电池服务费控制层
 *
 * @author makejava
 * @since 2022-04-21 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminBatteryServiceFeeController {
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserDataScopeService userDataScopeService;


    /**
     * 查询用户的服务费支付记录
     *
     * @param offset
     * @param size
     * @param queryStartTime
     * @param queryEndTime
     * @return
     */
    @GetMapping("/admin/batteryServiceFee/orderList")
    public R queryBatteryServiceFeeOrder(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
                                         @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                                         @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
                                         @RequestParam("uid") Long uid,
                                         @RequestParam(value = "status", required = false) Integer status) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return eleBatteryServiceFeeOrderService.queryListForAdmin(offset, size, queryStartTime, queryEndTime, uid, status,tenantId);
    }

    /**
     * 查询电池服务费
     *
     * @return
     */
    @GetMapping("/admin/batteryServiceFee/query")
    public R queryBatteryServiceFee(@RequestParam("uid") Long uid) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("admin saveUserMemberCard  ERROR! not found user! uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(userInfo.getTenantId(),tenantId)){
            return R.ok();
        }

        return R.ok(franchiseeUserInfoService.queryUserBatteryServiceFee(uid));
    }

    /**
     * 电池服务费列表
     *
     * @param offset
     * @param size
     * @param queryStartTime
     * @param queryEndTime
     * @param uid
     * @param phone
     * @param status
     * @param source
     * @return
     */
    @GetMapping("/admin/batteryServiceFee/queryList")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
                       @RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                       @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "sn", required = false) String sn,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "source", required = false) Integer source) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }

        BatteryServiceFeeQuery batteryServiceFeeQuery = BatteryServiceFeeQuery.builder()
                .uid(uid)
                .queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime)
                .offset(offset)
                .size(size)
                .name(name)
                .sn(sn)
                .status(status)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(franchiseeIds)
                .source(source)
                .phone(phone).build();

        return eleBatteryServiceFeeOrderService.queryList(batteryServiceFeeQuery);
    }

    /**
     * 电池服务费列表
     *
     * @param queryStartTime
     * @param queryEndTime
     * @param uid
     * @param phone
     * @param status
     * @param source
     * @return
     */
    @GetMapping("/admin/batteryServiceFee/queryCount")
    public R queryCount(@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
                        @RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "sn", required = false) String sn,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "source", required = false) Integer source) {
       

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok();
            }
        }

        BatteryServiceFeeQuery batteryServiceFeeQuery = BatteryServiceFeeQuery.builder()
                .uid(uid)
                .queryStartTime(queryStartTime)
                .queryEndTime(queryEndTime)
                .status(status)
                .name(name)
                .sn(sn)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(franchiseeIds)
                .source(source)
                .phone(phone).build();

        return eleBatteryServiceFeeOrderService.queryCount(batteryServiceFeeQuery);


    }

}
