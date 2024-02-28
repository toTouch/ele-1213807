package com.xiliulou.electricity.controller.user.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.request.merchant.MerchantEmployeeRequest;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/18 21:14
 */

@RestController
@Slf4j
public class JsonUserMerchantEmployeeController {
    
    @Resource
    private MerchantEmployeeService merchantEmployeeService;
    
    @Resource
    private MerchantService merchantService;
    
    @GetMapping("/merchant/queryMerchantEmployees")
    public R merchantEmployeeList(@RequestParam("size") long size,
                  @RequestParam("offset") Long offset,
                  @RequestParam(value = "merchantId", required = false) Long merchantUid,
                  @RequestParam(value = "channelUserId", required = false) Long channelUserId) {
    
        if (size < 0 || size > 50) {
            size = 10L;
        }
    
        if (offset < 0) {
            offset = 0L;
        }
    
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
    
        Merchant merchant = merchantService.queryByUid(uid);
        if(Objects.isNull(merchant)){
            log.error("find merchant user error, not found merchant user, uid = {}", uid);
            return R.fail("120007", "未找到商户");
        }
        
        MerchantEmployeeRequest merchantEmployeeRequest = MerchantEmployeeRequest.builder()
                .size(size)
                .offset(offset)
                .merchantUid(merchant.getUid())
                .tenantId(tenantId)
                .build();
        
        return R.ok(merchantEmployeeService.listMerchantEmployee(merchantEmployeeRequest));
    }
    
    @GetMapping("/merchant/queryMerchantEmployeesCount")
    public R listCount(@RequestParam(value = "merchantId", required = false) Long merchantUid,
                   @RequestParam(value = "channelUserId", required = false) Long channelUserId) {
        
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Merchant merchant = merchantService.queryByUid(uid);
        if(Objects.isNull(merchant)){
            log.error("find merchant user error, not found merchant user, uid = {}", uid);
            return R.fail("120007", "未找到商户");
        }
        
        MerchantEmployeeRequest merchantEmployeeRequest = MerchantEmployeeRequest.builder()
                .merchantUid(merchant.getUid())
                .tenantId(tenantId)
                .build();
        
        return R.ok(merchantEmployeeService.countMerchantEmployee(merchantEmployeeRequest));
    }
    
    @GetMapping("/merchant/addMerchantEmployee")
    public R save(@RequestParam(value = "name", required = true) String name,
               @RequestParam(value = "phone", required = true) String phone,
               @RequestParam(value = "status", required = true) Integer status,
               @RequestParam(value = "placeId", required = false) Long placeId) {
    
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        
        Merchant merchant = merchantService.queryByUid(uid);
        if(Objects.isNull(merchant)){
            log.error("find merchant user error, not found merchant user, uid = {}", uid);
            return R.fail("120007", "未找到商户");
        }
    
        MerchantEmployeeRequest merchantEmployeeRequest = MerchantEmployeeRequest.builder()
                .name(name)
                .phone(phone)
                .status(status)
                .placeId(placeId)
                .merchantUid(merchant.getUid())
                .tenantId(tenantId)
                .build();
    
        return R.ok(merchantEmployeeService.saveMerchantEmployee(merchantEmployeeRequest));
        
    }
    
    @GetMapping("/merchant/editMerchantEmployee")
    public R update(@RequestParam(value = "id", required = true) Long id,
               @RequestParam(value = "name", required = true) String name,
               @RequestParam(value = "phone", required = true) String phone,
               @RequestParam(value = "status", required = true) Integer status,
               @RequestParam(value = "placeId", required = false) Long placeId) {
    
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
    
        Merchant merchant = merchantService.queryByUid(uid);
        if(Objects.isNull(merchant)){
            log.error("find merchant user error, not found merchant user, uid = {}", uid);
            return R.fail("120007", "未找到商户");
        }
    
        MerchantEmployeeRequest merchantEmployeeRequest = MerchantEmployeeRequest.builder()
                .id(id)
                .name(name)
                .phone(phone)
                .status(status)
                .placeId(placeId)
                .merchantUid(merchant.getUid())
                .tenantId(tenantId)
                .build();
    
        return R.ok(merchantEmployeeService.updateMerchantEmployee(merchantEmployeeRequest));
    }
    
    @DeleteMapping(value = "/merchant/removeMerchantEmployee")
    public R remove(@RequestParam("id") Long id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        Long uid = SecurityUtils.getUid();
    
        Merchant merchant = merchantService.queryByUid(uid);
        if(Objects.isNull(merchant)){
            log.error("find merchant user error, not found merchant user, uid = {}", uid);
            return R.fail("120007", "未找到商户");
        }
        
        return R.ok(merchantEmployeeService.removeMerchantEmployee(id));
    }
    
    

}
