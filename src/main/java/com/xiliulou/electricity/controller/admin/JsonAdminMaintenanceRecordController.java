package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.MaintenanceRecordHandleQuery;
import com.xiliulou.electricity.query.MaintenanceRecordListQuery;
import com.xiliulou.electricity.service.MaintenanceRecordService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2021/9/26 4:02 下午
 */
@RestController
@Slf4j
public class JsonAdminMaintenanceRecordController extends BaseController {

    @Autowired
    MaintenanceRecordService maintenanceRecordService;
    @Autowired
    UserTypeFactory userTypeFactory;
    @Autowired
    UserDataScopeService userDataScopeService;

    @GetMapping("/admin/maintenance/record/list")
    public R getList(@RequestParam(value = "beginTime", required = false) Long beginTime,
                     @RequestParam(value = "endTime", required = false) Long endTime,
                     @RequestParam(value = "size") Integer size,
                     @RequestParam(value = "offset") Integer offset,
                     @RequestParam(value = "status", required = false) String status,
                     @RequestParam(value = "type", required = false) String type,
                     @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId) {

        if (size <= 0 || size >= 50) {
            size = 10;
        }

        if (offset < 0) {
            offset = 0;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
        }

        if (CollectionUtils.isEmpty(eleIdList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        MaintenanceRecordListQuery query = MaintenanceRecordListQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime)
                .offset(offset)
                .size(size)
                .status(status)
                .type(type)
                .electricityCabinetId(electricityCabinetId)
                .tenantId(TenantContextHolder.getTenantId())
                .eleIdList(eleIdList)
                .build();
        return returnTripleResult(maintenanceRecordService.queryListForAdmin(query));
    }


    @GetMapping("/admin/maintenance/record/queryCount")
    public R queryCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime,
                        @RequestParam(value = "status", required = false) String status,
                        @RequestParam(value = "type", required = false) String type,
                        @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
        }

        if (CollectionUtils.isEmpty(eleIdList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        MaintenanceRecordListQuery query = MaintenanceRecordListQuery.builder()
                .beginTime(beginTime)
                .endTime(endTime)
                .status(status)
                .type(type)
                .electricityCabinetId(electricityCabinetId)
                .eleIdList(eleIdList)
                .tenantId(TenantContextHolder.getTenantId())
                .build();
        return maintenanceRecordService.queryCountForAdmin(query);
    }


    @PostMapping("/admin/maintenance/handle")
    public R handleMaintenance(@RequestBody @Validated MaintenanceRecordHandleQuery maintenanceRecordHandleQuery) {
        return returnTripleResult(maintenanceRecordService.handleMaintenanceRecord(maintenanceRecordHandleQuery));
    }


}
