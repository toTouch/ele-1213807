package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/5 10:49
 */

@Slf4j
@RestController
public class JsonAdminMerchantWithdrawController {
    
    @Resource
    UserDataScopeService userDataScopeService;
    
    @Resource
    MerchantWithdrawApplicationService merchantWithdrawApplicationService;
    
    
    @GetMapping(value = "/admin/merchant/withdraw/page")
    public R queryMerchantWithdrawList(@RequestParam(value = "size") Long size,
            @RequestParam(value = "offset") Long offset,
            @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
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
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest = MerchantWithdrawApplicationRequest.builder()
                .tenantId(user.getTenantId())
                .franchiseeIds(franchiseeIds)
                .size(size)
                .offset(offset)
                .merchantUid(uid)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();
        
        return R.ok(merchantWithdrawApplicationService.queryMerchantWithdrawApplicationList(merchantWithdrawApplicationRequest));
        
    }
    
    @GetMapping(value = "/admin/merchant/withdraw/pageCount")
    public R queryMerchantWithdrawCount(@RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "status", required = false) Integer status) {
    
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
    
        MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest = MerchantWithdrawApplicationRequest.builder()
                .tenantId(user.getTenantId())
                .franchiseeIds(franchiseeIds)
                .merchantUid(uid)
                .status(status)
                .beginTime(beginTime)
                .endTime(endTime)
                .build();
        
        return R.ok(merchantWithdrawApplicationService.countMerchantWithdrawApplication(merchantWithdrawApplicationRequest));
    }
    
    



}
