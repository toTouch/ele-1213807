package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventorySaveOrUpdateRequest;
import com.xiliulou.electricity.service.asset.AssetInventoryService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author HeYafeng
 * @description 资产盘点
 * @date 2023/11/20 14:26:31
 */
@RestController
@Slf4j
public class AssetInventoryController {
    
    @Autowired
    private AssetInventoryService assetInventoryService;
    
    /**
     * @description 新增资产盘点
     * @date 2023/11/21 13:15:41
     * @author HeYafeng
     */
    @PostMapping("/admin/asset/inventory/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) AssetInventorySaveOrUpdateRequest assetInventorySaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return assetInventoryService.save(assetInventorySaveRequest, user.getUid());
    }
    
    /**
     * @description 资产盘点数量统计
     * @date 2023/11/21 18:17:54
     * @param status 盘点状态(0-进行中,1-完成)
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/inventory/pageCount")
    public R pageCount(@RequestParam(value = "orderNo", required = false) String orderNo, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "status", required = false) Integer status) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        AssetInventoryRequest assetInventoryRequest = AssetInventoryRequest.builder().orderNo(orderNo).franchiseeId(franchiseeId).status(status).build();
        
        return R.ok(assetInventoryService.countTotal(assetInventoryRequest));
    }
    
    /**
     * @description 资产盘点分页
     * @date 2023/11/21 13:15:54
     * @param status 盘点状态(0-进行中,1-完成)
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/inventory/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "status", required = false) Integer status) {
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
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        AssetInventoryRequest assetInventoryRequest = AssetInventoryRequest.builder().size(size).offset(offset).orderNo(orderNo).franchiseeId(franchiseeId).status(status).build();
        
        return R.ok(assetInventoryService.listByPage(assetInventoryRequest));
    }
    
}
