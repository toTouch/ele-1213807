package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.User;
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
        
        // 删除缓存
        FailureAlarm failureAlarm = (FailureAlarm) r.getRight();
        failureAlarmService.refreshCache(failureAlarm);
        return R.ok();
    }
    
    /**
     * @param
     * @description 故障告警设置数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/failure/alarm/pageCount")
    public R pageCount(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "errorCode", required = false) Integer errorCode,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "protectMeasureList", required = false) List<Long> protectMeasureIdList, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "model", required = false) Integer model, @RequestParam(value = "tenantVisible", required = false) Integer tenantVisible) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        FailureAlarmPageRequest allocateRecordPageRequest = FailureAlarmPageRequest.builder().name(name).type(type).errorCode(errorCode).type(type).grade(grade)
                .protectMeasureList(protectMeasureIdList).status(status).model(model).tenantVisible(tenantVisible).build();
        return R.ok(failureAlarmService.countTotal(allocateRecordPageRequest));
    }
    
    /**
     * @param
     * @description 故障告警设置拨分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/failure/alarm/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "errorCode", required = false) Integer errorCode, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "grade", required = false) Integer grade, @RequestParam(value = "protectMeasureList", required = false) List<Long> protectMeasureList,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "model", required = false) Integer model,
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        FailureAlarmPageRequest allocateRecordPageRequest = FailureAlarmPageRequest.builder().name(name).type(type).errorCode(errorCode).type(type).grade(grade)
                .protectMeasureList(protectMeasureList).status(status).model(model).tenantVisible(tenantVisible).size(size).offset(offset).build();
        
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
        failureAlarmService.refreshCache(failureAlarm);
        
        return R.ok();
    }
    
    
    /**
     * 删除故障告警设置
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/admin/failure/alarm/delete/{id}")
    @Log(title = "删除故障告警设置")
    public R delete(@PathVariable("id") Long id) {
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
    public void exportExcel(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "errorCode", required = false) Integer errorCode,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "protectMeasureIdList", required = false) List<Long> protectMeasureIdList, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "model", required = false) Integer model, @RequestParam(value = "tenantVisible", required = false) Integer tenantVisible,
            HttpServletResponse response) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("未查询到用户");
        }
        
        if (!SecurityUtils.isAdmin()) {
            throw new CustomBusinessException("用户权限不足");
        }
        
        FailureAlarmPageRequest allocateRecordPageRequest = FailureAlarmPageRequest.builder().name(name).type(type).errorCode(errorCode).type(type).grade(grade)
                .protectMeasureList(protectMeasureIdList).status(status).model(model).tenantVisible(tenantVisible).build();
        
        failureAlarmService.exportExcel(allocateRecordPageRequest, response);
    }
    
}
