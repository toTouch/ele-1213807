package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.queue.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.queue.asset.AssetWarehouseSaveOrUpdateRequest;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author HeYafeng
 * @description 库房
 * @date 2023/11/21 15:29:23
 */
@RestController
@Slf4j
public class JsonAdminWarehouseController {
    
    @Autowired
    private AssetWarehouseService assetWarehouseService;
    
    @PostMapping("/admin/asset/warehouse/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        assetWarehouseSaveOrUpdateRequest.setUid(user.getUid());
        
        return assetWarehouseService.save(assetWarehouseSaveOrUpdateRequest);
    }
    
    @PostMapping(value = "/admin/asset/warehouse/{id}")
    public R delete(@PathVariable("id") Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return assetWarehouseService.deleteById(id);
    }
    
    @PostMapping("/admin/asset/warehouse/update")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) AssetWarehouseSaveOrUpdateRequest assetWarehouseSaveOrUpdateRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return R.ok(assetWarehouseService.updateById(assetWarehouseSaveOrUpdateRequest));
    }
    
    /**
     * @description 查询租户下的库房名称
     * @date 2023/11/21 20:49:07
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/warehouse/names")
    public R listWarehouseNameByFranchiseeId() {
        return R.ok(assetWarehouseService.listWarehouseNameByFranchiseeId());
    }
    
    /**
     * @description 库房数量统计
     * @date 2023/11/21 18:17:54
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/warehouse/pageCount")
    public R pageCount(@RequestParam(value = "name", required = false) String name) {
        
        AssetWarehouseRequest assetInventoryRequest = AssetWarehouseRequest.builder().name(name).build();
        return R.ok(assetWarehouseService.queryCount(assetInventoryRequest));
    }
    
    /**
     * @description 库房列表
     * @date 2023/11/21 18:17:54
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/warehouse/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 50) {
            size = 10L;
        }
        
        if (offset < 0) {
            offset = 0L;
        }
        
        AssetWarehouseRequest assetInventoryRequest = AssetWarehouseRequest.builder().size(size).offset(offset).name(name).build();
        return R.ok(assetWarehouseService.listByFranchiseeId(assetInventoryRequest));
    }
    
}
