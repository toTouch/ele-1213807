package com.xiliulou.electricity.controller.admin.userinfo;

/**
 * @author HeYafeng
 * @description 用户分组详情历史记录
 * @date 2024/4/15 15:47:07
 */

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailHistoryBO;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailHistoryQuery;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.UserInfoGroupDetailHistoryVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class JsonAdminUserInfoGroupDetailHistoryController extends BasicController {
    
    @Resource
    private UserInfoGroupDetailHistoryService userInfoGroupDetailHistoryService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/userInfo/userInfoGroupDetailHistory/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "uid") Long uid) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromDbIncludeDelUser(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELE WARN! not found user");
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
        
        UserInfoGroupDetailHistoryQuery query = UserInfoGroupDetailHistoryQuery.builder().size(size).offset(offset).uid(uid).tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(franchiseeIds).build();
        List<UserInfoGroupDetailHistoryBO> boList = userInfoGroupDetailHistoryService.listByPage(query);
        
        // 转化为VO
        List<UserInfoGroupDetailHistoryVO> result = null;
        if (CollectionUtils.isNotEmpty(boList)) {
            result = boList.stream().map(bo -> {
                UserInfoGroupDetailHistoryVO vo = new UserInfoGroupDetailHistoryVO();
                BeanUtils.copyProperties(bo, vo);
                
                return vo;
            }).collect(Collectors.toList());
        }
        
        if (CollectionUtils.isEmpty(result)) {
            result = Collections.emptyList();
        }
        
        return R.ok(result);
    }
    
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/userInfo/userInfoGroupDetailHistory/pageCount")
    public R pageCount(@RequestParam(value = "uid") Long uid) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromDbIncludeDelUser(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELE WARN! not found user");
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
        
        UserInfoGroupDetailHistoryQuery query = UserInfoGroupDetailHistoryQuery.builder().uid(uid).tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).build();
        
        return R.ok(userInfoGroupDetailHistoryService.countTotal(query));
    }
}
