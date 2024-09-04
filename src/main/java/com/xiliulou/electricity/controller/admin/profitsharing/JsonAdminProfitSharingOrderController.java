package com.xiliulou.electricity.controller.admin.profitsharing;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingOrderDetailPageRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingOrderDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 分账订单表(profitSharingOrder)表控制层
 *
 * @author maxiaodong
 * @since 2024-08-22 16:58:51
 */
@Slf4j
@RestController
@RequestMapping("/admin/profit/sharing/order")
public class JsonAdminProfitSharingOrderController {
    @Resource
    private ProfitSharingOrderDetailService profitSharingOrderDetailService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * @param
     * @description 商户列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/pageCount")
    public R pageCount(@RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime
            , @RequestParam(value = "outAccountType", required = false) Integer outAccountType, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "profitSharingReceiveName", required = false) String profitSharingReceiveName, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "businessType", required = false) Integer businessType, @RequestParam(value = "thirdTradeOrderNo", required = false) String thirdTradeOrderNo) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        List<Long> franchiseeIdList = new ArrayList<>();
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant page count warn! franchisee is empty uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
            
            franchiseeIdList.addAll(franchiseeIds);
        }
        
        if (Objects.nonNull(franchiseeId)) {
            franchiseeIdList.add(franchiseeId);
        }
    
        ProfitSharingOrderDetailPageRequest profitSharingOrderDetailPageRequest = ProfitSharingOrderDetailPageRequest.builder().startTime(startTime).endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).franchiseeIdList(franchiseeIdList).outAccountType(outAccountType).profitSharingReceiveName(profitSharingReceiveName)
                .status(status).businessType(businessType).thirdTradeOrderNo(thirdTradeOrderNo).build();
        
        return R.ok(profitSharingOrderDetailService.countTotal(profitSharingOrderDetailPageRequest));
    }
    
    /**
     * @param
     * @description 商户列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime
            , @RequestParam(value = "outAccountType", required = false) Integer outAccountType, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "profitSharingReceiveName", required = false) String profitSharingReceiveName, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "businessType", required = false) Integer businessType, @RequestParam(value = "thirdTradeOrderNo", required = false) String thirdTradeOrderNo) {
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
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIdList = new ArrayList<>();
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long> franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant page count warn! franchisee is empty uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        
            franchiseeIdList.addAll(franchiseeIds);
        }
    
        if (Objects.nonNull(franchiseeId)) {
            franchiseeIdList.add(franchiseeId);
        }
    
        ProfitSharingOrderDetailPageRequest profitSharingOrderDetailPageRequest = ProfitSharingOrderDetailPageRequest.builder().startTime(startTime).endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).franchiseeIdList(franchiseeIdList).outAccountType(outAccountType).profitSharingReceiveName(profitSharingReceiveName)
                .status(status).businessType(businessType).thirdTradeOrderNo(thirdTradeOrderNo).offset(offset).size(size).build();
        
        return R.ok(profitSharingOrderDetailService.listByPage(profitSharingOrderDetailPageRequest));
    }
}

