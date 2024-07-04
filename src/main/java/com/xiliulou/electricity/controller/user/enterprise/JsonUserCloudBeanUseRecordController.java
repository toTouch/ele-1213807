package com.xiliulou.electricity.controller.user.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.enterprise.CloudBeanUseRecordQuery;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

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
    
    @Resource
    private EnterpriseInfoService enterpriseInfoService;
    
    /**
     * 云豆流水
     */
    @GetMapping({"/user/cloudBeanUse/page", "/merchant/cloudBeanUse/page"})
    public R cloudBeanUsePage(@RequestParam("offset") long offset, @RequestParam("size") long size, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime) {
        
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        CloudBeanUseRecordQuery query = CloudBeanUseRecordQuery.builder().offset(offset).size(size).tenantId(TenantContextHolder.getTenantId()).type(type).startTime(startTime)
                .endTime(endTime).build();
        
        return R.ok(cloudBeanUseRecordService.selectByUserPage(query));
    }
    
    /**
     * 云豆流水统计
     */
    @GetMapping({"/user/cloudBeanUse/statistics", "/merchant/cloudBeanUse/statistics"})
    public R cloudBeanUseStatistics(@RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        
        CloudBeanUseRecordQuery query = CloudBeanUseRecordQuery.builder().tenantId(TenantContextHolder.getTenantId()).type(type).startTime(startTime).endTime(endTime).build();
        
        return R.ok(cloudBeanUseRecordService.cloudBeanUseStatisticsByUid(query));
    }
    
    /**
     * 可回收云豆
     *
     * @return
     */
    @GetMapping({"/user/cloudBeanUse/recyclable/{uid}", "/merchant/cloudBeanUse/recyclable/{uid}"})
    public R cloudBeanUseRecyclable(@PathVariable("uid") Long uid) {
        return R.ok(cloudBeanUseRecordService.acquireUserCanRecycleCloudBean(uid));
    }
    
    /**
     * 已回收云豆
     *
     * @return
     */
    @GetMapping({"/user/cloudBeanUse/recyclabed/{uid}", "/merchant/cloudBeanUse/recyclabed/{uid}"})
    public R cloudBeanUseRecyclabed(@PathVariable("uid") Long uid) {
        Long id = SecurityUtils.getUid();
        
        EnterpriseInfoVO enterpriseInfoVO = enterpriseInfoService.selectEnterpriseInfoByUid(id);
        if (Objects.isNull(enterpriseInfoVO) || Objects.isNull(enterpriseInfoVO.getId())) {
            log.error("channel User Exit Check  enterprise not exists, uid={}", id);
            return R.fail("120311", "该用户无法操作");
        }
        
        return R.ok(cloudBeanUseRecordService.acquireUserRecycledCloudBean(uid, enterpriseInfoVO.getId()));
    }
    
    /**
     * 云豆账单下载
     */
    @GetMapping({"/user/cloudBeanOrder/download", "/merchant/cloudBeanOrder/download"})
    public R cloudBeanOrderDownload(@RequestParam(value = "beginTime") Long beginTime, @RequestParam(value = "endTime") Long endTime) {
        return returnTripleResult(cloudBeanUseRecordService.cloudBeanOrderDownload(beginTime, endTime));
    }
    
}
