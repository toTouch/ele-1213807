package com.xiliulou.electricity.controller.admin.enterprise;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanOrderQuery;
import com.xiliulou.electricity.request.enterprise.EnterpriseCloudBeanUseRecordPageRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.enterprise.CloudBeanUseRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/7/2 22:00
 * @desc
 */
@Slf4j
@RestController
public class JsonAdminEnterpriseCloudBeanUseRecordController extends BaseController {
    @Resource
    private CloudBeanUseRecordService cloudBeanUseRecordService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * 分页列表
     */
    @GetMapping("/admin/enterpriseCloudBeanUseRecord/page")
    public R page(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "operateUid", required = false) Long operateUid,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "enterpriseId", required = false) Long enterpriseId) {
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
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        EnterpriseCloudBeanUseRecordPageRequest request = EnterpriseCloudBeanUseRecordPageRequest.builder()
                .size(size)
                .offset(offset)
                .enterpriseId(enterpriseId)
                .orderId(orderId)
                .uid(uid)
                .operateUid(operateUid)
                .type(type)
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .build();
        
        return R.ok(cloudBeanUseRecordService.listByPage(request));
    }
    
    /**
     * 分页总数
     */
    @GetMapping("/admin/enterpriseCloudBeanUseRecord/count")
    public R pageCount(@RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "operateUid", required = false) Long operateUid,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "enterpriseId", required = false) Long enterpriseId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        EnterpriseCloudBeanUseRecordPageRequest request = EnterpriseCloudBeanUseRecordPageRequest.builder()
                .enterpriseId(enterpriseId)
                .orderId(orderId)
                .operateUid(operateUid)
                .uid(uid)
                .type(type)
                .franchiseeId(franchiseeId)
                .franchiseeIds(franchiseeIds)
                .tenantId(TenantContextHolder.getTenantId())
                .build();
        
        return R.ok(cloudBeanUseRecordService.countTotal(request));
    }
}
