package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantJoinRecordPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantScanCodeRecordPageRequest;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/19 21:11
 * @desc 用户邀请记录
 */

@Slf4j
@RestController
public class JsonMerchantJoinRecordController {
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    /**
     * @param
     * @description 列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchantJoinRecord/pageCount")
    public R pageCount(@RequestParam(value = "merchantId", required = false) Long merchantId, @RequestParam(value = "status", required = false) Integer status) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        MerchantJoinRecordPageRequest merchantJoinRecordPageRequest = MerchantJoinRecordPageRequest.builder().merchantId(merchantId).status(status).tenantId(TenantContextHolder.getTenantId()).build();
        return R.ok(merchantJoinRecordService.countTotal(merchantJoinRecordPageRequest));
    }
    
    /**
     * @param
     * @description 列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchantJoinRecord/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset,
            @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "status", required = false) Integer status) {
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
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantJoinRecordPageRequest merchantJoinRecordPageRequest = MerchantJoinRecordPageRequest.builder().offset(offset).size(size)
                .merchantId(merchantId).status(status).tenantId(TenantContextHolder.getTenantId()).build();
        
        return R.ok(merchantJoinRecordService.listByPage(merchantJoinRecordPageRequest));
    }
    
    
    @GetMapping("/admin/merchantScanCodeRecord/count")
    public R scanCodeRecordCount(@RequestParam(value = "merchantId", required = false) Long merchantId, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "scanTimeStart", required = false) Long scanTimeStart,
            @RequestParam(value = "scanTimeEnd", required = false) Long scanTimeEnd, @RequestParam(value = "buyTimeStart", required = false) Long buyTimeStart,
            @RequestParam(value = "buyTimeEnd", required = false) Long buyTimeEnd) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantScanCodeRecordPageRequest merchantScanCodeRecordPageRequest = MerchantScanCodeRecordPageRequest.builder().merchantId(merchantId)
                .tenantId(TenantContextHolder.getTenantId()).scanTimeStart(scanTimeStart).scanTimeEnd(scanTimeEnd).buyTimeStart(buyTimeStart).buyTimeEnd(buyTimeEnd).phone(phone)
                .franchiseeId(franchiseeId).build();
        
        return R.ok(merchantJoinRecordService.countScanCodeRecord(merchantScanCodeRecordPageRequest));
    }
    
    @GetMapping("/admin/merchantScanCodeRecord/page")
    public R scanCodeRecordPage(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "scanTimeStart", required = false) Long scanTimeStart, @RequestParam(value = "scanTimeEnd", required = false) Long scanTimeEnd,
            @RequestParam(value = "buyTimeStart", required = false) Long buyTimeStart, @RequestParam(value = "buyTimeEnd", required = false) Long buyTimeEnd) {
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        MerchantScanCodeRecordPageRequest merchantScanCodeRecordPageRequest = MerchantScanCodeRecordPageRequest.builder().offset(offset).size(size).merchantId(merchantId)
                .tenantId(TenantContextHolder.getTenantId()).scanTimeStart(scanTimeStart).scanTimeEnd(scanTimeEnd).buyTimeStart(buyTimeStart).buyTimeEnd(buyTimeEnd).phone(phone)
                .franchiseeId(franchiseeId).build();
        
        return R.ok(merchantJoinRecordService.listScanCodeRecordPage(merchantScanCodeRecordPageRequest));
    }
}
