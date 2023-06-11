package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.FreeDepositAlipayHistoryQuery;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (FreeDepositAlipayHistory)表控制层
 *
 * @author zgw
 * @since 2023-04-13 09:13:02
 */
@RestController
public class JsonAdminFreeDepositAlipayHistoryController {
    
    /**
     * 服务对象
     */
    @Resource
    private FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @GetMapping("/admin/freeDepositAlipayHistory/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "name", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "idCard", required = false) String idCard,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        if (Objects.isNull(size) || size > 50 || size < 0) {
            size = 50L;
        }
        
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        FreeDepositAlipayHistoryQuery query = new FreeDepositAlipayHistoryQuery();
        query.setSize(size);
        query.setOffset(offset);
        query.setOrderId(orderId);
        query.setUserName(userName);
        query.setPhone(phone);
        query.setIdCard(idCard);
        query.setType(type);
        query.setBeginTime(beginTime);
        query.setEndTime(endTime);
        query.setTenantId(TenantContextHolder.getTenantId());
        
        return freeDepositAlipayHistoryService.queryList(query);
    }
    
    @GetMapping("/admin/freeDepositAlipayHistory/queryCount")
    public R queryCount(@RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "name", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "idCard", required = false) String idCard,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime) {
        
        FreeDepositAlipayHistoryQuery query = new FreeDepositAlipayHistoryQuery();
        query.setOrderId(orderId);
        query.setUserName(userName);
        query.setPhone(phone);
        query.setIdCard(idCard);
        query.setType(type);
        query.setBeginTime(beginTime);
        query.setEndTime(endTime);
        query.setTenantId(TenantContextHolder.getTenantId());
        
        return freeDepositAlipayHistoryService.queryCount(query);
    }
}
