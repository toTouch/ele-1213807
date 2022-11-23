package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.SysOperLogQuery;
import com.xiliulou.electricity.service.SysOperLogService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.SysOperLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志记录(SysOperLog)表控制层
 *
 * @author zzlong
 * @since 2022-10-11 19:47:27
 */
@RestController
public class JsonAdminSysOperLogController extends BaseController {
    
    @Autowired
    private SysOperLogService sysOperLogService;
    
    
    @RequestMapping("/admin/sysOperLog/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }

        Integer tenantId=null;
        if(!SecurityUtils.isAdmin()){
            tenantId=TenantContextHolder.getTenantId();
        }
        
        SysOperLogQuery sysOperLogQuery = SysOperLogQuery.builder().size(size).offset(offset).status(status)
                .beginTime(beginTime).endTime(endTime).tenantId(tenantId).build();
        
        List<SysOperLogVO> sysOperLogs = sysOperLogService.selectByPage(sysOperLogQuery);
        return R.ok(sysOperLogs);
    }
    
    @GetMapping("/admin/sysOperLog/pageCount")
    public R pageCount(@RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        
        SysOperLogQuery sysOperLogQuery = SysOperLogQuery.builder().status(status).beginTime(beginTime).endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).build();
    
        int count=sysOperLogService.pageCount(sysOperLogQuery);
        
        return R.ok(count);
    }
    
}
