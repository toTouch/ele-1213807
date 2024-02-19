package com.xiliulou.electricity.controller.admin.merchant;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.merchant.MerchantAreaQuery;
import com.xiliulou.electricity.request.merchant.MerchantAreaSaveOrUpdateRequest;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 区域管理
 * @date 2024/2/6 13:37:49
 */
@Slf4j
@RestController
public class JsonAdminMerchantAreaController extends BaseController {
    
    @Resource
    private MerchantAreaService merchantAreaService;
    
    /**
     * 新增
     */
    @PostMapping("/admin/merchant/area/save")
    public R save(@RequestBody MerchantAreaSaveOrUpdateRequest saveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return merchantAreaService.deleteById(id);
    }
    
    /**
     * 编辑
     */
    @PostMapping("/admin/merchant/area/edit")
    public R updateById(@RequestBody MerchantAreaSaveOrUpdateRequest updateRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return merchantAreaService.updateById(updateRequest);
    }
    
    /**
     * 分页查询
     */
    @GetMapping("/admin/merchant/area/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name) {
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        MerchantAreaQuery query = MerchantAreaQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).name(name).build();
        
        return R.ok(merchantAreaService.listByPage(query));
    }
    
    /**
     * 区域下拉框查询
     */
    @GetMapping("/admin/merchant/area/queryList")
    public R queryList(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name) {
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        MerchantAreaQuery query = MerchantAreaQuery.builder().size(size).offset(offset).tenantId(TenantContextHolder.getTenantId()).name(name).build();
        
        return R.ok(merchantAreaService.queryList(query));
    }
    
    @GetMapping("/admin/merchant/area/pageCount")
    public R pageCount(@RequestParam(value = "name", required = false) String name) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
    
        MerchantAreaQuery query = MerchantAreaQuery.builder().tenantId(TenantContextHolder.getTenantId()).name(name).build();
    
        return R.ok(merchantAreaService.countTotal(query));
    }
    
    @GetMapping("/admin/merchant/area/selectByTenantId")
    public R listByTenantId(@RequestParam Integer tenantId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok();
        }
        
        return R.ok(merchantAreaService.listByTenantId(tenantId));
    }
    
}
