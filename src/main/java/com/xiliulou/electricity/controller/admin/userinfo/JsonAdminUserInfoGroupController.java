package com.xiliulou.electricity.controller.admin.userinfo;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.UserInfoGroupQuery;
import com.xiliulou.electricity.request.user.UserInfoGroupBatchImportRequest;
import com.xiliulou.electricity.request.user.UserInfoGroupSaveAndUpdateRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 用户分组
 * @date 2024/4/8 19:39:33
 */
@Slf4j
@RestController
public class JsonAdminUserInfoGroupController extends BasicController {
    
    @Resource
    private UserInfoGroupService userInfoGroupService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    /**
     * @description 新增用户分组
     * @date 2024/4/8 19:43:44
     * @author HeYafeng
     */
    @PostMapping("/admin/userInfo/userInfoGroup/add")
    public R save(@RequestBody @Validated(value = CreateGroup.class) UserInfoGroupSaveAndUpdateRequest userInfoGroupSaveAndUpdateRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        // 租户操作，加盟商ID不能为空
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            Long franchiseeId = userInfoGroupSaveAndUpdateRequest.getFranchiseeId();
        
            if (Objects.isNull(franchiseeService.queryByIdFromCache(franchiseeId))) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
        }
    
        // 加盟商操作，查询加盟商
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
        
            if (Objects.isNull(franchisee)) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
        
            userInfoGroupSaveAndUpdateRequest.setFranchiseeId(franchisee.getId());
        }
    
        return userInfoGroupService.save(userInfoGroupSaveAndUpdateRequest, user.getUid());
    }
    
    /**
     * @description 删除用户分组
     * @date 2024/4/8 19:43:44
     * @author HeYafeng
     */
    @PostMapping("/admin/userInfo/userInfoGroup/delete/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupService.remove(id, user.getUid());
    }
    
    /**
     * @description 编辑用户分组
     * @date 2024/4/8 19:43:44
     * @author HeYafeng
     */
    @PostMapping("/admin/userInfo/userInfoGroup/update")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) UserInfoGroupSaveAndUpdateRequest userInfoGroupSaveAndUpdateRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupService.update(userInfoGroupSaveAndUpdateRequest, user.getUid());
    }
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/userInfo/userInfoGroup/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "groupId", required = false) Long groupId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
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
        
        UserInfoGroupQuery query = UserInfoGroupQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).groupId(groupId)
                .build();
        
        return R.ok(userInfoGroupService.listByPage(query));
    }
    
    /**
     * 分页总数
     */
    @GetMapping("/admin/userInfo/userInfoGroup/pageCount")
    public R pageCount(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "groupId", required = false) Long groupId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
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
        
        UserInfoGroupQuery query = UserInfoGroupQuery.builder().tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).groupId(groupId).build();
        
        return R.ok(userInfoGroupService.countTotal(query));
    }
    
    /**
     * 批量导入用户
     */
    @PostMapping("/admin/userInfo/userInfoGroup/batchImport")
    public R batchImport(@RequestBody @Validated UserInfoGroupBatchImportRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return userInfoGroupService.batchImport(request, user.getUid());
    }
    
    /**
     * 租户下所有分组
     */
    @GetMapping("/admin/userInfo/userInfoGroup/allGroup")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
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
        
        UserInfoGroupQuery query = UserInfoGroupQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).franchiseeIds(franchiseeIds).build();
        
        return R.ok(userInfoGroupService.listAllGroup(query));
    }
    
}
