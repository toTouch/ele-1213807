package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.CarLockCtrlHistoryQuery;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (CarLockCtrlHistory)表控制层
 *
 * @author Hardy
 * @since 2023-04-04 16:22:29
 */
@RestController
public class JsonAdminCarLockCtrlHistoryController {
    
    /**
     * 服务对象
     */
    @Resource
    private CarLockCtrlHistoryService carLockCtrlHistoryService;

    @Autowired
    UserDataScopeService userDataScopeService;
    
    @GetMapping("admin/carLockCtrlHistory/list")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "carSn", required = false) String carSn,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
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

        CarLockCtrlHistoryQuery query = CarLockCtrlHistoryQuery.builder().offset(offset)
                .size(size)
                .tenantId(TenantContextHolder.getTenantId())
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .name(name)
                .phone(phone)
                .uid(uid)
                .carSn(carSn)
                .beginTime(beginTime)
                .endTime(endTime).build();

        return carLockCtrlHistoryService.queryList(query);
    }
    
    @GetMapping("admin/carLockCtrlHistory/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "carSn", required = false) String carSn,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
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

        CarLockCtrlHistoryQuery query = CarLockCtrlHistoryQuery.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .storeIds(storeIds)
                .franchiseeIds(franchiseeIds)
                .name(name)
                .phone(phone)
                .uid(uid)
                .carSn(carSn)
                .beginTime(beginTime)
                .endTime(endTime).build();

        return carLockCtrlHistoryService.queryCount(query);
    }
}
