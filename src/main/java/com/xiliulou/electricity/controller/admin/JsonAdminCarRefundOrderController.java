package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.CarRefundOrderQuery;
import com.xiliulou.electricity.service.CarRefundOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (CarRefundOrder)表控制层
 *
 * @author Hardy
 * @since 2023-03-15 13:42:00
 */
@RestController
public class JsonAdminCarRefundOrderController {
    
    /**
     * 服务对象
     */
    @Resource
    private CarRefundOrderService carRefundOrderService;
    
    @GetMapping("admin/carRefundOrder/queryList")
    public R queryList(@RequestParam("offset") Long offset, @RequestParam("size") Long size,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "storeId", required = false) Long storeId) {
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 50L;
        }
        
        CarRefundOrderQuery query = CarRefundOrderQuery.builder().offset(offset).size(size).orderId(orderId)
                .userName(userName).storeId(storeId).phone(phone).tenantId(TenantContextHolder.getTenantId()).build();
        
        return carRefundOrderService.queryList(query);
    }
    
    @GetMapping("admin/carRefundOrder/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "storeId", required = false) Long storeId) {
        
        CarRefundOrderQuery query = CarRefundOrderQuery.builder().orderId(orderId).userName(userName).storeId(storeId)
                .phone(phone).tenantId(TenantContextHolder.getTenantId()).build();
        
        return carRefundOrderService.queryCount(query);
    }
    
    @PutMapping("admin/carRefundOrder/review")
    public R carRefundOrderReview(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        return carRefundOrderService.carRefundOrderReview(id, status);
    }
}
