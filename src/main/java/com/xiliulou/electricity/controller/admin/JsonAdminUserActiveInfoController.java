package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserActiveInfoQuery;
import com.xiliulou.electricity.service.UserActiveInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (UserActiveInfo)表控制层
 *
 * @author Hardy
 * @since 2023-03-01 10:15:12
 */
@RestController
public class JsonAdminUserActiveInfoController {
    
    /**
     * 服务对象
     */
    @Resource
    private UserActiveInfoService userActiveInfoService;
    
    @GetMapping("/admin/userActiveInfo/list")
    public R queryList(@RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "day", required = false) Integer day,
            @RequestParam(value = "batterySn", required = false) String batterySn,
            @RequestParam(value = "payCount", required = false) Integer payCount, @RequestParam("offset") Long offset,
            @RequestParam("size") Long size) {
        if (Objects.isNull(offset) || offset < 0) {
            offset = 0L;
        }
        
        if (Objects.isNull(size) || size < 0 || size > 50) {
            size = 50L;
        }
        
        UserActiveInfoQuery query = UserActiveInfoQuery.builder().userName(userName).phone(phone).day(day)
                .batterySn(batterySn).payCount(payCount).offset(offset).size(size)
                .tenantId(TenantContextHolder.getTenantId()).build();
        
        return userActiveInfoService.queryList(query);
    }
    
    @GetMapping("/admin/userActiveInfo/count")
    public R queryCount(@RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "day", required = false) Integer day,
            @RequestParam(value = "batterySn", required = false) String batterySn,
            @RequestParam(value = "payCount", required = false) Integer payCount) {
        
        UserActiveInfoQuery query = UserActiveInfoQuery.builder().userName(userName).phone(phone).day(day)
                .batterySn(batterySn).payCount(payCount).tenantId(TenantContextHolder.getTenantId()).build();
        return userActiveInfoService.queryCount(query);
    }
}
