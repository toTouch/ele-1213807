package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.query.EleDeviceCodeInsertQuery;
import com.xiliulou.electricity.query.EleDeviceCodeQuery;
import com.xiliulou.electricity.service.EleDeviceCodeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
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

/**
 * @author zzlong
 */
@Slf4j
@RestController
public class JsonAdminEleDeviceCodeController extends BaseController {
    
    @Autowired
    private EleDeviceCodeService eleDeviceCodeService;
    
    /**
     * 分页列表
     */
    @GetMapping("/admin/eleDeviceCode/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus, @RequestParam(value = "startTime",required = false) Long startTime,
                  @RequestParam(value = "endTime",required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        EleDeviceCodeQuery query = EleDeviceCodeQuery.builder().size(size).offset(offset).deviceName(deviceName).onlineStatus(onlineStatus).delFlag(CommonConstant.DEL_N).startTime(startTime)
                .endTime(endTime).build();
        
        return R.ok(eleDeviceCodeService.listByPage(query));
    }
    
    /**
     * 分页总数
     */
    @GetMapping("/admin/eleDeviceCode/queryCount")
    public R pageCount(@RequestParam(value = "deviceName", required = false) String deviceName, @RequestParam(value = "onlineStatus", required = false) Integer onlineStatus, @RequestParam(value = "startTime",required = false) Long startTime,
                       @RequestParam(value = "endTime",required = false) Long endTime) {
        
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        EleDeviceCodeQuery query = EleDeviceCodeQuery.builder().deviceName(deviceName).onlineStatus(onlineStatus).delFlag(CommonConstant.DEL_N).startTime(startTime).endTime(endTime).build();
        
        return R.ok(eleDeviceCodeService.countByPage(query));
    }
    
    /**
     * 新增
     */
    @PostMapping("/admin/eleDeviceCode")
    public R save(@RequestBody @Validated(CreateGroup.class) EleDeviceCodeQuery query) {
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(eleDeviceCodeService.save(query));
    }
    
    /**
     * 修改备注
     */
    @PutMapping("/admin/eleDeviceCode")
    public R update(@RequestBody @Validated(UpdateGroup.class) EleDeviceCodeQuery query) {
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(eleDeviceCodeService.modify(query));
    }
    
    /**
     * 删除
     */
    @DeleteMapping("/admin/eleDeviceCode/{id}")
    public R delete(@PathVariable("id") Long id) {
        if (!SecurityUtils.isAdmin()) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(eleDeviceCodeService.delete(id));
    }
}
