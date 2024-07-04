package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantJoinRecordPageRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
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
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * @param
     * @description 列表数量统计
     * @date 2023/12/15 18:17:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchantJoinRecord/pageCount")
    public R pageCount(@RequestParam(value = "merchantId", required = false) Long merchantId, @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(NumberConstant.ZERO);
            }
        }
        
        MerchantJoinRecordPageRequest merchantJoinRecordPageRequest = MerchantJoinRecordPageRequest.builder().merchantId(merchantId).status(status)
                .tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).build();
        return R.ok(merchantJoinRecordService.countTotal(merchantJoinRecordPageRequest));
    }
    
    /**
     * @param
     * @description 列表分页
     * @date 2023/11/21 13:15:54
     * @author maxiaodong
     */
    @GetMapping("/admin/merchantJoinRecord/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "merchantId", required = false) Long merchantId,
            @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        MerchantJoinRecordPageRequest merchantJoinRecordPageRequest = MerchantJoinRecordPageRequest.builder().offset(offset).size(size).merchantId(merchantId).status(status)
                .tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantJoinRecordService.listByPage(merchantJoinRecordPageRequest));
    }
}
