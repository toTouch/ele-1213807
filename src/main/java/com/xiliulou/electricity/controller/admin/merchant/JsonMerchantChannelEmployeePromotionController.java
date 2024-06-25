package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.ChannelEmployeePromotionRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeePromotionMonthRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/21 20:15
 * @desc
 */
@Slf4j
@RestController
public class JsonMerchantChannelEmployeePromotionController extends BaseController {
    
    @Resource
    private ChannelEmployeePromotionMonthRecordService channelEmployeePromotionMonthRecordService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @GetMapping("/admin/channelEmployeePromotion/page")
    public R page(@RequestParam("size") Integer size, @RequestParam("offset") Integer offset, @RequestParam(value = "monthDate", required = false) String monthDate) {
        if (size < 0 || size > 50) {
            size = 10;
        }
    
        if (offset < 0) {
            offset = 0;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        // 渠道员查询权限 admin,租户，加盟商
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            log.warn("channel employee promotion page warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee promotion page warn! franchisee is empty, uid = {}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
    
            franchiseeId = franchiseeIds.get(0);
        }
        
        ChannelEmployeePromotionRequest channelEmployeeRequest = ChannelEmployeePromotionRequest.builder().tenantId(TenantContextHolder.getTenantId()).size(size).offset(offset)
                .monthDate(monthDate).franchiseeId(franchiseeId).build();
        
        return R.ok(channelEmployeePromotionMonthRecordService.listByPage(channelEmployeeRequest));
        
    }
    
    
    @GetMapping("/admin/channelEmployeePromotion/pageCount")
    public R channelEmployeeCount(@RequestParam(value = "monthDate", required = false) String monthDate) {
        TokenUser user = SecurityUtils.getUserInfo();
    
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            log.warn("channel employee promotion page count warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee promotion page warn! franchisee is empty, uid = {}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        
            franchiseeId = franchiseeIds.get(0);
        }
        
        ChannelEmployeePromotionRequest channelEmployeeRequest = ChannelEmployeePromotionRequest.builder().tenantId(TenantContextHolder.getTenantId()).monthDate(monthDate).franchiseeId(franchiseeId).build();
        
        return R.ok(channelEmployeePromotionMonthRecordService.countTotal(channelEmployeeRequest));
        
    }
    
    /**
     * 导出
     * @param monthDate
     * @param response
     */
    @GetMapping("/admin/channelEmployeePromotion/exportExcel")
    public void exportExcel(@RequestParam("monthDate") String monthDate, HttpServletResponse response) {
        TokenUser user = SecurityUtils.getUserInfo();
    
        if (Objects.isNull(user)) {
            throw new CustomBusinessException("未查询到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            throw new CustomBusinessException("用户权限不足");
        }
    
        Long franchiseeId = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("channel employee promotion page warn! franchisee is empty, uid = {}", user.getUid());
                throw new CustomBusinessException("加盟商不存在");
            }
        
            franchiseeId = franchiseeIds.get(0);
        }
        channelEmployeePromotionMonthRecordService.export(monthDate, response, franchiseeId);
    }
}
