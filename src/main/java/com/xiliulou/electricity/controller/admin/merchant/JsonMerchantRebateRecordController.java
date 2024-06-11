package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName: JsonMerchantRebateRecordController
 * @description: 返利记录
 * @author: renhang
 * @create: 2024-03-26 09:33
 */
@Slf4j
@RestController
@RequestMapping("/admin/rebate/record/")
public class JsonMerchantRebateRecordController {
    
    @Resource
    private RebateRecordService rebateRecordService;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    /**
     * 返利分页
     */
    @GetMapping("page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "channeler", required = false) Long channeler, @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "orderId", required = false) String orderId) {
        
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
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        RebateRecordRequest query = RebateRecordRequest.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).uid(uid)
                .type(type).channeler(channeler).merchantId(merchantId).orderId(orderId).beginTime(beginTime).endTime(endTime).franchiseeIds(franchiseeIds).build();
        
        return R.ok(rebateRecordService.listByPage(query));
    }
    
    /**
     * 分页总数
     */
    @GetMapping("queryCount")
    public R pageCount(@RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "channeler", required = false) Long channeler,
            @RequestParam(value = "merchantId", required = false) Long merchantId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "orderId", required = false) String orderId) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        RebateRecordRequest query = RebateRecordRequest.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).uid(uid).type(type).channeler(channeler)
                .merchantId(merchantId).orderId(orderId).beginTime(beginTime).endTime(endTime).franchiseeIds(franchiseeIds).build();
        
        return R.ok(rebateRecordService.countByPage(query));
    }
}
