package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.merchant.MerchantAreaRequest;
import com.xiliulou.electricity.request.merchant.MerchantAreaSaveOrUpdateRequest;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 区域管理
 * @date 2024/2/6 13:37:49
 */
@Slf4j
@RestController
public class JsonMerchantAreaController extends BaseController {
    
    @Resource
    private MerchantAreaService merchantAreaService;
    
    @Resource
    private UserDataScopeService userDataScopeService;
    
    /**
     * 新增
     */
    @PostMapping("/admin/merchant/area/save")
    public R save(@RequestBody @Validated(CreateGroup.class) MerchantAreaSaveOrUpdateRequest saveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            log.error("merchant area save warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant save warn! franchisee is empty");
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
    
            saveRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        return merchantAreaService.save(saveRequest, user.getUid());
    }
    
    /**
     * 删除（物理）
     */
    @PostMapping("/admin/merchant/area/delete")
    public R delete(@RequestParam Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            log.error("merchant area delete warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant save warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        return merchantAreaService.deleteById(id, franchiseeIds);
    }
    
    /**
     * 编辑
     */
    @PostMapping("/admin/merchant/area/edit")
    public R updateById(@RequestBody @Validated(UpdateGroup.class)MerchantAreaSaveOrUpdateRequest updateRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            log.error("merchant area update warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant area update warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
    
            updateRequest.setBindFranchiseeIdList(franchiseeIds);
        }
        
        return merchantAreaService.updateById(updateRequest);
    }
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/merchant/area/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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
            log.warn("merchant area page warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant area page warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantAreaRequest request = MerchantAreaRequest.builder().size(size).offset(offset).name(name).franchiseeIdList(franchiseeIds).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantAreaService.listByPage(request));
    }
    
    @GetMapping("/admin/merchant/area/pageCount")
    public R pageCount(@RequestParam(value = "name", required = false) String name, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            log.warn("merchant area page count warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant area page count warn! franchisee is empty, uid = {}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantAreaRequest request = MerchantAreaRequest.builder().name(name).franchiseeIdList(franchiseeIds).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantAreaService.countTotal(request));
    }
    
    /**
     * 区域下拉框查询
     */
    @GetMapping("/admin/merchant/area/queryList")
    public R queryList(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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
            log.error("merchant area query all warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIdList = new ArrayList<>();
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            List<Long>  franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant area query all warn! franchisee is empty, uid = {}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
    
            franchiseeIdList.addAll(franchiseeIds);
        }
        
        if (Objects.nonNull(franchiseeId)){
            franchiseeIdList.add(franchiseeId);
        }
        
        
        // 如果是租户登录则需将加盟商为零的租户级别的区域返回
        if (SecurityUtils.isAdmin()) {
            franchiseeIdList.add(0L);
        }
        
        MerchantAreaRequest request = MerchantAreaRequest.builder().size(size).offset(offset).name(name).franchiseeIdList(franchiseeIdList).build();
        
        return R.ok(merchantAreaService.queryList(request));
    }
    
    @GetMapping("/admin/merchant/area/selectAll")
    public R listAll(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
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
            log.error("merchant area select all warn! user not auth");
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        List<Long> franchiseeIds = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)) {
                log.warn("merchant area select all warn! franchisee is empty, uid={}", user.getUid());
                return R.fail("ELECTRICITY.0038", "加盟商不存在");
            }
        }
        
        MerchantAreaRequest request = MerchantAreaRequest.builder().size(size).offset(offset).name(name).franchiseeIdList(franchiseeIds).franchiseeId(franchiseeId).build();
        
        return R.ok(merchantAreaService.listAll(request));
    }
    
}
