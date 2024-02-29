package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.service.merchant.RebateRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 返利记录
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-20-15:59
 */
@Slf4j
@RestController
public class JsonMerchantRebateRecordController extends BaseController {
    
    @Autowired
    private RebateRecordService rebateRecordService;
    
    /**
     * 分页列表
     */
    @GetMapping("/admin/rebate/record/page")
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
        
        RebateRecordRequest query = RebateRecordRequest.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).uid(uid)
                .type(type).channeler(channeler).merchantId(merchantId).orderId(orderId).beginTime(beginTime).endTime(endTime).build();
        
        return R.ok(rebateRecordService.listByPage(query));
    }
    
    /**
     * 分页总数
     */
    @GetMapping("/admin/rebate/record/queryCount")
    public R pageCount(@RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "channeler", required = false) Long channeler,
            @RequestParam(value = "merchantId", required = false) Long merchantId, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "orderId", required = false) String orderId) {
        
        RebateRecordRequest query = RebateRecordRequest.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeId(franchiseeId).uid(uid).type(type).channeler(channeler)
                .merchantId(merchantId).orderId(orderId).beginTime(beginTime).endTime(endTime).build();
        
        return R.ok(rebateRecordService.countByPage(query));
    }
    
}
