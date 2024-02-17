package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 10:26
 */

@Slf4j
@RestController
public class JsonAdminChannelEmployeeController {
    
    @Resource
    private ChannelEmployeeService channelEmployeeService;
    
    @GetMapping("/admin/channelEmployee/channelEmployeeList")
    public R channelEmployeeList(@RequestParam("size") Integer size,
                                 @RequestParam("offset") Integer offset,
                                 @RequestParam(value = "uid", required = false) Long name,
                                 @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                 @RequestParam(value = "areaId", required = false) Long areaId) {
    
        if (size < 0 || size > 50) {
            size = 10;
        }
    
        if (offset < 0) {
            offset = 0;
        }
    
        ChannelEmployeeRequest channelEmployeeRequest = ChannelEmployeeRequest.builder()
                .size(size)
                .offset(offset)
                .uid(name)
                .franchiseeId(franchiseeId)
                .areaId(areaId)
                .build();
        //
        
        return R.ok(channelEmployeeService.listChannelEmployee(channelEmployeeRequest));
        
    }
    
    
    @GetMapping("/admin/channelEmployee/channelEmployeeCount")
    public R channelEmployeeCount(
                                @RequestParam(value = "uid", required = false) Long name,
                                @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                @RequestParam(value = "areaId", required = false) Long areaId) {
    
        ChannelEmployeeRequest channelEmployeeRequest = ChannelEmployeeRequest.builder()
                .uid(name)
                .franchiseeId(franchiseeId)
                .areaId(areaId)
                .build();
        
        return R.ok(channelEmployeeService.countChannelEmployee(channelEmployeeRequest));
        
    }
    
    @PostMapping("/admin/channelEmployee/addChannelEmployee")
    public R addChannelEmployee(@RequestBody @Validated(value = CreateGroup.class) ChannelEmployeeRequest channelEmployeeRequest) {
        
        
        return null;
    }
    
    
    @PostMapping("/admin/channelEmployee/updateChannelEmployee")
    public R updateChannelEmployee(@RequestBody @Validated(value = UpdateGroup.class) ChannelEmployeeRequest channelEmployeeRequest) {
        
        return null;
    }
    
    @GetMapping("/admin/channelEmployee/queryChannelEmployeeById")
    public R queryChannelEmployeeById(@RequestParam("id") Long id) {
        
        return null;
    }
    
    @GetMapping("/admin/channelEmployee/deleteChannelEmployeeById")
    public R deleteChannelEmployeeById(@RequestParam("id") Long id) {
        
        return null;
    }


}
