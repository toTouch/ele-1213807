package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:02
 * @desc
 */

@RestController
@Slf4j
public class EleHardwareFailureWarnMsgController {
    @Resource
    private EleHardwareFailureWarnMsgService failureWarnMsgService;
    
    /**
     * 故障告警记录超级管理员查看分页接口
     * @param size
     * @param offset
     * @param sn
     * @param deviceType
     * @param grade
     * @param signalId
     * @param alarmStartTime
     * @param alarmEndTime
     * @param alarmFlag
     * @return
     */
    @GetMapping("/admin/failure/warn/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "deviceType", required = false) Integer deviceType, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "signalId", required = false) Integer signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
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
    
        Integer tenantVisible  = FailureAlarm.visible;
        Integer tenantId = TenantContextHolder.getTenantId();
        
        EleHardwareFailureWarnMsgPageRequest request = EleHardwareFailureWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId).deviceType(deviceType).grade(grade)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).tenantVisible(tenantVisible).status(FailureAlarm.enable).size(size).offset(offset).build();
        
        return failureWarnMsgService.listByPage(request);
    }
    
    /**
     * @param
     * @description 故障告警记录数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/failure/warn/pageCount")
    public R pageCount(
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "deviceType", required = false) Integer deviceType, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "signalId", required = false) Integer signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
            @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime, @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        Integer tenantVisible  = FailureAlarm.visible;
        Integer tenantId = TenantContextHolder.getTenantId();
    
        EleHardwareFailureWarnMsgPageRequest request = EleHardwareFailureWarnMsgPageRequest.builder().type(EleHardwareFailureWarnMsg.WARN).sn(sn).tenantId(tenantId).deviceType(deviceType).grade(grade)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).tenantVisible(tenantVisible).status(FailureAlarm.enable).build();
        return failureWarnMsgService.countTotal(request);
    }
    
    /**
     * @param
     * @description 故障告警记录数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/super/failure/warn/pageCount")
    public R superPageCount(@RequestParam(value = "type", required = true) Integer type,
            @RequestParam(value = "sn", required = false) String sn, @RequestParam(value = "tenantId", required = false) Integer tenantId,
            @RequestParam(value = "deviceType", required = false) Integer deviceType, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "signalId", required = false) Integer signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
            @RequestParam(value = "alarmEndTime", required = true) Long alarmEndTime, @RequestParam(value = "alarmFlag", required = false) Integer alarmFlag) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        EleHardwareFailureWarnMsgPageRequest request = EleHardwareFailureWarnMsgPageRequest.builder().type(type).sn(sn).tenantId(tenantId).deviceType(deviceType).grade(grade)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).status(FailureAlarm.enable).build();
        return failureWarnMsgService.countTotal(request);
    }
    
    /**
     * 故障告警记录超级管理员查看分页接口
     * @param size
     * @param offset
     * @param sn
     * @param deviceType
     * @param grade
     * @param signalId
     * @param alarmStartTime
     * @param alarmEndTime
     * @param alarmFlag
     * @return
     */
    @GetMapping("/admin/super/failure/warn/page")
    public R superPage(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "sn", required = false) String sn,
            @RequestParam(value = "tenantId", required = false) Integer tenantId, @RequestParam(value = "type", required = true) Integer type,
            @RequestParam(value = "deviceType", required = false) Integer deviceType, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "signalId", required = false) Integer signalId, @RequestParam(value = "alarmStartTime", required = true) Long alarmStartTime,
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
    
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        EleHardwareFailureWarnMsgPageRequest request = EleHardwareFailureWarnMsgPageRequest.builder().type(type).sn(sn).tenantId(tenantId).deviceType(deviceType).grade(grade)
                .signalId(signalId).alarmStartTime(alarmStartTime).alarmEndTime(alarmEndTime).alarmFlag(alarmFlag).status(FailureAlarm.enable).size(size).offset(offset).build();
        
        return failureWarnMsgService.listByPage(request);
    }
    
    @GetMapping("/admin/failure/warn/frequency")
    public R frequency(@RequestParam(value = "startTime", required = true) Long startTime,
            @RequestParam(value = "endTime", required = true) Long endTime) {
        EleHardwareFailureWarnMsgPageRequest request = EleHardwareFailureWarnMsgPageRequest.builder().alarmStartTime(startTime).alarmStartTime(endTime).build();
        Triple<Boolean, String, Object> triple = failureWarnMsgService.calculateFrequency(request);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        return R.ok(triple.getRight());
    }
}
