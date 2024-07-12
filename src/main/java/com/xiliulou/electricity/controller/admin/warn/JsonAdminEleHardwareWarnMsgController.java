package com.xiliulou.electricity.controller.admin.warn;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareWarnMsgPageRequest;
import com.xiliulou.electricity.service.warn.EleHardwareWarnMsgService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
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
 * @date 2024/5/23 11:22
 * @desc
 */
@RestController
@Slf4j
public class JsonAdminEleHardwareWarnMsgController {
    @Resource
    private EleHardwareWarnMsgService eleHardwareWarnMsgService;
    
    /**
     * 故障告警记录超级管理员查看分页接口
     *
     * @param size
     * @param offset
     * @param sn
     * @param signalId
     * @param alarmStartTime
     * @param alarmEndTime
     * @param alarmFlag
     * @return
     */
    @GetMapping("/admin/warn/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "alarmId", required = false) String alarmId,
            @RequestParam(value = "deviceType", required = false) Integer deviceType, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "signalId", required = false) String signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
            @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime, @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag) {
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
        
        Integer tenantVisible = FailureAlarm.visible;
        Integer tenantId = TenantContextHolder.getTenantId();
        
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId)
                .alarmId(alarmId).signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).tenantVisible(tenantVisible)
                .status(FailureAlarm.enable).deviceType(deviceType).grade(grade).size(size).offset(offset).build();
        
        return eleHardwareWarnMsgService.listByPage(request);
    }
    
    /**
     * @param
     * @description 故障告警记录数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/warn/pageCount")
    public R pageCount(@RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "alarmId", required = false) String alarmId,
            @RequestParam(value = "signalId", required = false) String signalId,
            @RequestParam(value = "deviceType", required = false) Integer deviceType, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime, @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime,
            @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantVisible = FailureAlarm.visible;
        Integer tenantId = TenantContextHolder.getTenantId();
        
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId)
                .alarmId(alarmId).signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).tenantVisible(tenantVisible)
                .status(FailureAlarm.enable).deviceType(deviceType).grade(grade).build();
        return eleHardwareWarnMsgService.countTotal(request);
    }
    
    /**
     * @param
     * @description 故障告警记录数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/super/warn/pageCount")
    public R superPageCount(@RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "tenantId", required = false) Integer tenantId, @RequestParam(value = "signalId", required = false) String signalId,@RequestParam(value = "alarmId", required = false) String alarmId,
            @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime, @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime,
            @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag
            , @RequestParam(value = "noLimitSignalId", required = false) Integer noLimitSignalId, @RequestParam(value = "cabinetId", required = false) Integer cabinetId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId).alarmId(alarmId).cabinetId(cabinetId)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).build();
        
        // 故障告警统计分析运营商总览主动跳转到故障告警记录页面第一次的时候不添加状态的限制
        if (Objects.isNull(noLimitSignalId)) {
            request.setStatus(FailureAlarm.enable);
        }
        
        return eleHardwareWarnMsgService.countTotal(request);
    }
    
    /**
     * 故障告警记录超级管理员查看分页接口
     *
     * @param size
     * @param offset
     * @param sn
     * @param signalId
     * @param alarmStartTime
     * @param alarmEndTime
     * @param alarmFlag
     * @return
     */
    @GetMapping("/admin/super/warn/page")
    public R superPage(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "tenantId", required = false) Integer tenantId,@RequestParam(value = "alarmId", required = false) String alarmId,
            @RequestParam(value = "signalId", required = false) String signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
            @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime, @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag
            , @RequestParam(value = "noLimitSignalId", required = false) Integer noLimitSignalId, @RequestParam(value = "cabinetId", required = false) Integer cabinetId) {
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
        
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId).alarmId(alarmId).cabinetId(cabinetId)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).size(size).offset(offset).build();
        
        // 故障告警统计分析运营商总览主动跳转到故障告警记录页面第一次的时候不添加状态的限制
        if (Objects.isNull(noLimitSignalId)) {
            request.setStatus(FailureAlarm.enable);
        }
        
        return eleHardwareWarnMsgService.listByPage(request);
    }
    
    
    /**
     * 故障告警记录超级管理员查看分页接口
     *
     * @param size
     * @param offset
     * @param sn
     * @param signalId
     * @param alarmStartTime
     * @param alarmEndTime
     * @param alarmFlag
     * @return
     */
    @GetMapping("/admin/super/warn/export/page")
    public R superExportPage(@RequestParam("size") long size, @RequestParam(value = "offset", required = true) long offset, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "tenantId", required = false) Integer tenantId, @RequestParam(value = "alarmId", required = false) String alarmId,
            @RequestParam(value = "signalId", required = false) String signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
            @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime, @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag,
            @RequestParam(value = "noLimitSignalId", required = false) Integer noLimitSignalId, @RequestParam(value = "cabinetId", required = false) Integer cabinetId) {
        if (size > 2000) {
            size = 2000;
        }
        
        if (size < 0) {
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
        
        
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId).alarmId(alarmId).cabinetId(cabinetId)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).size(size).offset(offset).build();
        
        // 故障告警统计分析运营商总览主动跳转到故障告警记录页面第一次的时候不添加状态的限制
        if (Objects.isNull(noLimitSignalId)) {
            request.setStatus(FailureAlarm.enable);
        }
        
        return eleHardwareWarnMsgService.superExportPage(request);
    }
    
    
    /**
     * 售后导出列表查询
     * @param size
     * @param offset
     * @param sn
     * @param signalId
     * @param alarmStartTime
     * @param alarmEndTime
     * @param alarmFlag
     * @return
     */
    @GetMapping("/admin/warn/export/page")
    public R exportPage(@RequestParam("size") long size, @RequestParam(value = "offset", required = true) long offset, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "alarmId", required = false) String alarmId,
            @RequestParam(value = "signalId", required = false) String signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
            @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime, @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag) {
        if (size > 2000) {
            size = 2000L;
        }
        
        if (size < 0) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        Integer tenantVisible = FailureAlarm.visible;
        
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId).alarmId(alarmId)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).tenantVisible(tenantVisible).status(FailureAlarm.enable).size(size).offset(offset).build();
        
        return eleHardwareWarnMsgService.superExportPage(request);
    }
    
    @GetMapping("/admin/super/warn/proportion")
    public R proportion(@RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime).build();
        Triple<Boolean, String, Object> triple = eleHardwareWarnMsgService.proportion(request);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        return R.ok(triple.getRight());
    }
    
    @GetMapping("/admin/super/warn/proportion/export")
    public void proportionExport(@RequestParam(value = "startTime", required = true) Long startTime, @RequestParam(value = "endTime", required = true) Long endTime, HttpServletResponse response) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("未查询到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            throw new CustomBusinessException("用户权限不足");
        }
    
        EleHardwareWarnMsgPageRequest request = EleHardwareWarnMsgPageRequest.builder().alarmStartTime(startTime).alarmEndTime(endTime).build();
        eleHardwareWarnMsgService.proportionExport(request, response);
    }
}
