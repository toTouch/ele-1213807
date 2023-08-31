package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserExtra;
import com.xiliulou.electricity.query.UpdateUserSourceQuery;
import com.xiliulou.electricity.query.UserSourceQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserExtraService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 用户来源
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-12-15:58
 */
@RestController
@Slf4j
public class JsonAdminUserSourceController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    UserExtraService userExtraService;
    @Autowired
    UserDataScopeService userDataScopeService;


    @GetMapping(value = "/admin/userSource/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "uid", required = false) Long uid,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "phone", required = false) String phone,
                       @RequestParam(value = "source", required = false) Integer source,
                       @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                       @RequestParam(value = "storeId", required = false) Long storeId,
                       @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                       @RequestParam(value = "startTime", required = false) Long startTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
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

        UserSourceQuery userSourceQuery = UserSourceQuery.builder()
                .offset(offset)
                .size(size)
                .uid(uid)
                .name(name)
                .phone(phone)
                .franchiseeId(franchiseeId)
                .source(source)
                .electricityCabinetId(electricityCabinetId)
                .storeId(storeId)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .franchiseeId(franchiseeId)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(userService.selectUserSourceByPage(userSourceQuery));
    }

    @GetMapping(value = "/admin/userSource/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "phone", required = false) String phone,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "source", required = false) Integer source,
                        @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                        @RequestParam(value = "storeId", required = false) Long storeId,
                        @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                        @RequestParam(value = "startTime", required = false) Long startTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

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

        UserSourceQuery userSourceQuery = UserSourceQuery.builder()
                .name(name)
                .phone(phone)
                .uid(uid)
                .franchiseeId(franchiseeId)
                .source(source)
                .electricityCabinetId(electricityCabinetId)
                .storeId(storeId)
                .franchiseeIds(franchiseeIds)
                .storeIds(storeIds)
                .franchiseeId(franchiseeId)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).build();


        return R.ok(userService.selectUserSourcePageCount(userSourceQuery));
    }

    @PutMapping(value = "/admin/userSource")
    @Log(title = "修改用户来源")
    public R updateUserSource(@RequestBody @Validated UpdateUserSourceQuery userSourceQuery){
        User updateUser = new User();
        updateUser.setUid(userSourceQuery.getUid());
        updateUser.setRefId(userSourceQuery.getRefId());
        updateUser.setSource(userSourceQuery.getSource());
        updateUser.setTenantId(TenantContextHolder.getTenantId());
        updateUser.setUpdateTime(System.currentTimeMillis());

        userService.updateUserSource(updateUser);
        return R.ok();
    }

}
