package com.xiliulou.electricity.controller.admin.userinfo;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupDetailPageBO;
import com.xiliulou.electricity.bo.userInfoGroup.UserInfoGroupNamesBO;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.userinfo.userInfoGroup.UserInfoGroupDetailQuery;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoBindGroupRequest;
import com.xiliulou.electricity.request.userinfo.userInfoGroup.UserInfoGroupDetailUpdateRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.UserInfoGroupDetailPageVO;
import com.xiliulou.electricity.vo.userinfo.userInfoGroup.UserInfoGroupNamesVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 用户分组详情
 * @date 2024/4/8 19:39:33
 */
@Slf4j
@RestController
public class JsonAdminUserInfoGroupDetailController extends BasicController {
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/userInfo/userInfoGroupDetail/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "uid", required = false) Long uid,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "groupId", required = false) Long groupId) {
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        List<Long> franchiseeIds = new ArrayList<>();
        if (Objects.nonNull(franchiseeId)) {
            franchiseeIds.add(franchiseeId);
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        UserInfoGroupDetailQuery query = UserInfoGroupDetailQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds)
                .groupId(groupId).uid(uid).build();
        List<UserInfoGroupDetailPageBO> boList = userInfoGroupDetailService.listByPage(query);
        
        // BO转化为VO
        List<UserInfoGroupDetailPageVO> result = null;
        if (CollectionUtils.isNotEmpty(boList)) {
            result = boList.stream().map(bo -> {
                UserInfoGroupDetailPageVO vo = new UserInfoGroupDetailPageVO();
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
     * 分页总数
     */
    @GetMapping("/admin/userInfo/userInfoGroupDetail/pageCount")
    public R pageCount(@RequestParam(value = "uid", required = false) Long uid, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "groupId", required = false) Long groupId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        List<Long> franchiseeIds = new ArrayList<>();
        if (Objects.nonNull(franchiseeId)) {
            franchiseeIds.add(franchiseeId);
        }
        
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.emptyList());
            }
        }
        
        UserInfoGroupDetailQuery query = UserInfoGroupDetailQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).groupId(groupId).uid(uid)
                .build();
        
        return R.ok(userInfoGroupDetailService.countTotal(query));
    }
    
    /**
     * 根据uid查询分组列表
     */
    @GetMapping("/admin/userInfo/userInfoGroupDetail/listGroupByUid")
    public R listGroupByUid(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "uid", required = false) Long uid) {
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
        
        UserInfoGroupDetailQuery query = UserInfoGroupDetailQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds)
                .uid(uid).build();
        List<UserInfoGroupNamesBO> boList = userInfoGroupDetailService.listGroupByUid(query);
        
        // BO转化为VO
        List<UserInfoGroupNamesVO> result = null;
        if (CollectionUtils.isNotEmpty(boList)) {
            result = boList.stream().map(bo -> {
                UserInfoGroupNamesVO vo = new UserInfoGroupNamesVO();
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
     * 编辑
     * TODO SJP
     */
    @Deprecated
    @PostMapping("/admin/userInfo/userInfoGroupDetail/update")
    public R update(@RequestBody @Validated UserInfoGroupDetailUpdateRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupDetailService.update(request, user.getUid());
    }
    
    /**
     * 编辑
     */
    @PostMapping("/admin/userInfo/userInfoGroupDetail/updateV2")
    public R updateV2(@RequestBody @Validated UserInfoGroupDetailUpdateRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupDetailService.updateV2(request, user);
    }
    
    /**
     * 加入分组
     * TODO SJP
     */
    @Deprecated
    @PostMapping("/admin/userInfo/userInfoGroupDetail/bindGroup")
    public R bindGroup(@RequestBody @Validated UserInfoBindGroupRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupDetailService.bindGroup(request, user.getUid());
    }
    
    /**
     * 加入分组
     */
    @PostMapping("/admin/userInfo/userInfoGroupDetail/bindGroupV2")
    public R bindGroupV2(@RequestBody @Validated UserInfoBindGroupRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupDetailService.bindGroupV2(request, user);
    }
    
    /**
     * 用户详情页预览用户分组详情
     * @param uid 要查看的用户uid
     * @return 查询结果
     */
    @GetMapping("/admin/userInfo/userInfoGroupDetail/selectAll")
    public R<Object> selectAll(@RequestParam(value = "uid") Long uid) {
        UserInfoGroupDetailQuery query = UserInfoGroupDetailQuery.builder().uid(uid).build();
        return userInfoGroupDetailService.selectAll(query);
    }
}
