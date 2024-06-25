package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.handler.iot.impl.HardwareFaultMsgHandler;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureAlarmQueryModel;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmBatchSetRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmSaveRequest;
import com.xiliulou.electricity.service.FailureAlarmService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * 故障预警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */

@RestController
@Slf4j
public class FailureAlarmController {
    
    @Resource
    private FailureAlarmService failureAlarmService;
    
    @Resource
    private HardwareFaultMsgHandler hardwareFaultMsgHandler;
    
    /**
     * @param
     * @description 故障告警设置数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @PostMapping ("/admin/super/failure/alarm/test")
    public R test(@RequestBody FailureAlarmSaveRequest request) {
        // todo 告警同步测试需要删除
        log.info("testSignalName:{}", request.getSignalName());
        hardwareFaultMsgHandler.testSend(request.getSignalName(), request.getType());
        return R.ok();
    }
    
    
    /**
     * 保存
     *
     * @param failureAlarmSaveRequest
     * @return
     */
    @PostMapping("/admin/failure/alarm/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) FailureAlarmSaveRequest failureAlarmSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        Triple<Boolean, String, Object> r = failureAlarmService.save(failureAlarmSaveRequest, user.getUid());
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        return R.ok();
    }
    
    /**
     * @param
     * @description 故障告警设置数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/failure/alarm/pageCount")
    public R pageCount(@RequestParam(value = "signalName", required = false) String signalName, @RequestParam(value = "signalId", required = false) String signalId,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "protectMeasureList", required = false) List<Long> protectMeasureList, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "deviceType", required = false) Integer deviceType, @RequestParam(value = "tenantVisible", required = false) Integer tenantVisible) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        FailureAlarmPageRequest allocateRecordPageRequest = FailureAlarmPageRequest.builder().signalName(signalName).type(type).signalId(signalId).type(type).grade(grade)
                .protectMeasureList(protectMeasureList).status(status).deviceType(deviceType).tenantVisible(tenantVisible).build();
        return R.ok(failureAlarmService.countTotal(allocateRecordPageRequest));
    }
    
    /**
     * @param
     * @description 故障告警设置拨分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/failure/alarm/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "signalName", required = false) String signalName,
            @RequestParam(value = "signalId", required = false) String signalId, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "grade", required = false) Integer grade, @RequestParam(value = "protectMeasureList", required = false) List<Long> protectMeasureList,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "deviceType", required = false) Integer deviceType,
            @RequestParam(value = "tenantVisible", required = false) Integer tenantVisible) {
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
        
        FailureAlarmPageRequest allocateRecordPageRequest = FailureAlarmPageRequest.builder().signalName(signalName).type(type).signalId(signalId).type(type).grade(grade)
                .protectMeasureList(protectMeasureList).status(status).deviceType(deviceType).tenantVisible(tenantVisible).size(size).offset(offset).build();
        
        return R.ok(failureAlarmService.listByPage(allocateRecordPageRequest));
    }
    
    /**
     * 修改故障告警设置
     *
     * @param failureAlarmSaveRequest
     * @return
     */
    @PutMapping(value = "/admin/failure/alarm/update")
    @Log(title = "修改故障告警设置")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) FailureAlarmSaveRequest failureAlarmSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Triple<Boolean, String, Object> r = failureAlarmService.update(failureAlarmSaveRequest, user.getUid());
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
        
        // 更新缓存
        FailureAlarm failureAlarm = (FailureAlarm) r.getRight();
        failureAlarmService.deleteCache(failureAlarm);
        
        return R.ok();
    }
    
    
    /**
     * 删除故障告警设置
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/admin/failure/alarm/delete")
    @Log(title = "删除故障告警设置")
    public R delete(@RequestParam("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
    
        Triple<Boolean, String, Object> r = failureAlarmService.delete(id, user.getUid());
        if (!r.getLeft()) {
            return R.fail(r.getMiddle(), (String) r.getRight());
        }
    
        // 更新缓存
        FailureAlarm failureAlarm = (FailureAlarm) r.getRight();
        failureAlarmService.deleteCache(failureAlarm);
        
        return R.ok();
    }
    
    /**
     * 批量设置
     *
     * @param
     * @return
     */
    @PostMapping(value = "/admin/failure/alarm/batchSet")
    @Log(title = "故障告警批量设置")
    public R batchSet(@RequestBody FailureAlarmBatchSetRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return failureAlarmService.batchSet(request, user.getUid());
    }
    
    
    /**
     * 故障告警数据导出
     */
    @GetMapping(value = "/admin/failure/alarm/exportExcel")
    public void exportExcel(HttpServletResponse response) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("未查询到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            throw new CustomBusinessException("用户权限不足");
        }
        
        FailureAlarmPageRequest allocateRecordPageRequest = FailureAlarmPageRequest.builder().build();
        
        failureAlarmService.exportExcel(allocateRecordPageRequest, response);
    }
    
    /**
     *
     * @return
     */
    @GetMapping("/admin/failure/alarm/getDictList")
    public R getDictList(@RequestParam("name") String name, @RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam("type") Integer type) {
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
        
        Integer tenantVisible = null;
        if (!SecurityUtils.isAdmin()) {
            tenantVisible = FailureAlarm.visible;
        }
        FailureAlarmQueryModel queryModel = FailureAlarmQueryModel.builder().tenantVisible(tenantVisible).status(FailureAlarm.enable).signalName(name).type(type)
                .offset(offset).size(size).build();
        List<FailureAlarm> list = failureAlarmService.listByParams(queryModel);
        return R.ok(list);
    }
    
}
