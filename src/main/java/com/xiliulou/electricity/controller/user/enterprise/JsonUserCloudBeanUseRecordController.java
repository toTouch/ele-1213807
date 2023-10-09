package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.CloudBeanUseRecordQuery;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-18-15:41
 */
@Slf4j
@RestController
public class JsonUserCloudBeanUseRecordController extends BaseController {
    
    @Autowired
    private CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Autowired
    private EnterpriseCloudBeanOrderService enterpriseCloudBeanOrderService;
    
    /**
     * 云豆流水
     */
    @GetMapping("/user/cloudBeanUse/page")
    public R cloudBeanUsePage(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        CloudBeanUseRecordQuery query = CloudBeanUseRecordQuery.builder().offset(offset).size(size).uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId())
                .type(type).startTime(startTime).endTime(endTime).build();
        
        return R.ok(cloudBeanUseRecordService.selectByUserPage(query));
    }
    
    /**
     * 云豆流水统计
     */
    @GetMapping("/user/cloudBeanUse/statistics")
    public R cloudBeanUseStatistics(@RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        
        CloudBeanUseRecordQuery query = CloudBeanUseRecordQuery.builder().uid(SecurityUtils.getUid()).tenantId(TenantContextHolder.getTenantId()).type(type).startTime(startTime)
                .endTime(endTime).build();
        
        return R.ok(cloudBeanUseRecordService.cloudBeanUseStatisticsByUid(query));
    }
    
    /**
     * 回收用户套餐余量及押金
     */
    @PostMapping("/user/cloudBean/recycle/{uid}")
    public R recycleDepositMembercard(@PathVariable("uid") Long uid) {
        return returnTripleResult(cloudBeanUseRecordService.recycleDepositMembercard(uid));
    }
    
    /**
     * 云豆账单下载
     */
    @GetMapping("/user/cloudBeanOrder/download")
    public R cloudBeanOrderDownload(@RequestParam(value = "beginTime") Long beginTime, @RequestParam(value = "endTime") Long endTime) {
        return returnTripleResult(cloudBeanUseRecordService.cloudBeanOrderDownload(beginTime, endTime));
    }
    
}
