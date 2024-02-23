package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.ChannelEmployeePromotionRequest;
import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.service.merchant.ChannelEmployeePromotionMonthRecordService;
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
 * @date 2024/2/21 20:15
 * @desc
 */
@Slf4j
@RestController
public class JsonAdminChannelEmployeePromotionController extends BaseController {
    
    @Resource
    private ChannelEmployeePromotionMonthRecordService channelEmployeePromotionMonthRecordService;
    
    @GetMapping("/admin/channelEmployeePromotion/page")
    public R page(@RequestParam("size") Integer size, @RequestParam("offset") Integer offset, @RequestParam(value = "time", required = false) Long time) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (size < 0 || size > 50) {
            size = 10;
        }
        
        if (offset < 0) {
            offset = 0;
        }
    
        Integer tenantId = null;
        if (!SecurityUtils.isAdmin()) {
            tenantId = TenantContextHolder.getTenantId();
        }
        
        ChannelEmployeePromotionRequest channelEmployeeRequest = ChannelEmployeePromotionRequest.builder().tenantId(tenantId).size(size).offset(offset).time(time).build();
        
        
        
        return R.ok(channelEmployeePromotionMonthRecordService.listByPage(channelEmployeeRequest));
        
    }
    
    
    @GetMapping("/admin/merchant/pageCount")
    public R channelEmployeeCount(@RequestParam(value = "time", required = false) Long time) {
        TokenUser user = SecurityUtils.getUserInfo();
    
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        Integer tenantId = null;
        if (!SecurityUtils.isAdmin()) {
            tenantId = TenantContextHolder.getTenantId();
        }
        
        ChannelEmployeePromotionRequest channelEmployeeRequest = ChannelEmployeePromotionRequest.builder().tenantId(tenantId).time(time).build();
        
        return R.ok(channelEmployeePromotionMonthRecordService.countTotal(channelEmployeeRequest));
        
    }
}
