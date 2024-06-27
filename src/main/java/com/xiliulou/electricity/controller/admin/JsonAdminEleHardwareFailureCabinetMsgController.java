package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.failureAlarm.FailureWarnCabinetMsgPageRequest;
import com.xiliulou.electricity.service.EleHardwareFailureCabinetMsgService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:20
 * @desc
 */

@RestController
@Slf4j
public class JsonAdminEleHardwareFailureCabinetMsgController {
    @Resource
    private EleHardwareFailureCabinetMsgService failureCabinetMsgService;
    
    
    /**
     * 故障统计分析：运营商总览
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/admin/super/failure/warn/tenantOverviewPage")
    public R tenantOverview(@RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime,
            @RequestParam(value = "type", required = true) Integer type, @RequestParam("size") long size, @RequestParam(value = "offset", required = true) long offset) {
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
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        FailureWarnCabinetMsgPageRequest request = FailureWarnCabinetMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime)
                .type(type).offset(offset).size(size).build();
        Triple<Boolean, String, Object> triple = failureCabinetMsgService.tenantOverviewPage(request);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        return R.ok(triple.getRight());
    }
    
    /**
     * 故障统计分析 运营商总览
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/admin/super/failure/warn/tenantOverviewPageCount")
    public R tenantOverviewPageCount(@RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime,
            @RequestParam(value = "type", required = true) Integer type) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        FailureWarnCabinetMsgPageRequest request = FailureWarnCabinetMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime).type(type).build();
        Triple<Boolean, String, Object> triple = failureCabinetMsgService.tenantOverviewPageCount(request);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        return R.ok(triple.getRight());
    }
    
    /**
     * 故障统计分析 设备商总览
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/admin/super/failure/warn/cabinetOverviewPage")
    public R cabinetOverviewPage(@RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime,
            @RequestParam(value = "type", required = true) Integer type, @RequestParam("size") long size, @RequestParam(value = "offset", required = true) long offset) {
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
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        FailureWarnCabinetMsgPageRequest request = FailureWarnCabinetMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime)
                .type(type).offset(offset).size(size).build();
        Triple<Boolean, String, Object> triple = failureCabinetMsgService.cabinetOverviewPage(request);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        return R.ok(triple.getRight());
    }
    
    /**
     * 故障统计分析 设备商总览
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/admin/super/failure/warn/cabinetOverviewPageCount")
    public R cabinetOverviewPageCount(@RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime,
            @RequestParam(value = "type", required = true) Integer type) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        FailureWarnCabinetMsgPageRequest request = FailureWarnCabinetMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime).type(type).build();
        Triple<Boolean, String, Object> triple = failureCabinetMsgService.cabinetOverviewPageCount(request);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        return R.ok(triple.getRight());
    }
    
    /**
     * 设备故障总览导出
     */
    @GetMapping(value = "/admin/failure/warn/cabinetOverviewExport")
    public void cabinetOverviewExport(HttpServletResponse response, @RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime,
            @RequestParam(value = "type", required = true) Integer type) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("未查询到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            throw new CustomBusinessException("用户权限不足");
        }
        
        FailureWarnCabinetMsgPageRequest request = FailureWarnCabinetMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime).type(type).build();
        failureCabinetMsgService.cabinetOverviewExport(request, response);
    }
    
    /**
     * 运营商总览导出
     */
    @GetMapping(value = "/admin/failure/warn/tenantOverviewExport")
    public void tenantOverviewExport(HttpServletResponse response, @RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime,
            @RequestParam(value = "type", required = true) Integer type) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("未查询到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            throw new CustomBusinessException("用户权限不足");
        }
        
        FailureWarnCabinetMsgPageRequest request = FailureWarnCabinetMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime).type(type).build();
        failureCabinetMsgService.tenantOverviewExport(request, response);
    }
    
}
