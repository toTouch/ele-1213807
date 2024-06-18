package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.EnableMemberCardRecordQuery;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 启用套餐(TEnableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@RestController
@Slf4j
public class JsonAdminEnableMemberCardRecordController extends BaseController {
    
    
    @Autowired
    EnableMemberCardRecordService enableMemberCardRecordService;
    
    @Autowired
    UserTypeFactory userTypeFactory;
    
    @Autowired
    UserDataScopeService userDataScopeService;
    
    // 列表查询
    @GetMapping(value = "/admin/enableMemberCardRecord/list")
    public R queryList(@RequestParam("size") Long size, @RequestParam("offset") Long offset, @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "enableType", required = false) Integer enableType, @RequestParam(value = "beginTime", required = false) Long beginTime,
            @RequestParam(value = "endTime", required = false) Long endTime, @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo,
            @RequestParam(value = "beginDisableTime", required = false) Long beginDisableTime, @RequestParam(value = "endDisableTime", required = false) Long endDisableTime) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        EnableMemberCardRecordQuery enableMemberCardRecordQuery = EnableMemberCardRecordQuery.builder().enableType(enableType).beginTime(beginTime).endTime(endTime).offset(offset)
                .size(size).phone(phone).franchiseeIds(franchiseeIds).storeIds(storeIds).userName(userName).uid(uid).tenantId(tenantId).disableMemberCardNo(disableMemberCardNo)
                .beginDisableTime(beginDisableTime).endDisableTime(endDisableTime).build();
        
        return enableMemberCardRecordService.queryList(enableMemberCardRecordQuery);
    }
    
    
    // 列表数量查询
    @GetMapping(value = "/admin/enableMemberCardRecord/queryCount")
    public R queryCount(@RequestParam(value = "userName", required = false) String userName, @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "enableType", required = false) Integer enableType,
            @RequestParam(value = "beginTime", required = false) Long beginTime, @RequestParam(value = "endTime", required = false) Long endTime,
            @RequestParam(value = "id", required = false) Integer id, @RequestParam(value = "disableMemberCardNo", required = false) String disableMemberCardNo,
            @RequestParam(value = "beginDisableTime", required = false) Long beginDisableTime, @RequestParam(value = "endDisableTime", required = false) Long endDisableTime) {
        
        // 租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        List<Long> storeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.springframework.util.CollectionUtils.isEmpty(storeIds)) {
                return R.ok(NumberConstant.ZERO);
            }
        }
        
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }
        
        EnableMemberCardRecordQuery enableMemberCardRecordQuery = EnableMemberCardRecordQuery.builder().enableType(enableType).beginTime(beginTime).endTime(endTime).phone(phone)
                .franchiseeIds(franchiseeIds).storeIds(storeIds).userName(userName).uid(uid).tenantId(tenantId).disableMemberCardNo(disableMemberCardNo)
                .beginDisableTime(beginDisableTime).endDisableTime(endDisableTime).build();
        
        return enableMemberCardRecordService.queryCount(enableMemberCardRecordQuery);
    }
    
}
