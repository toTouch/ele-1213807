package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailBatchInventoryRequest;
import com.xiliulou.electricity.request.asset.AssetInventoryDetailRequest;
import com.xiliulou.electricity.service.asset.AssetInventoryDetailService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
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
public class AssetInventoryDetailController {
    
    @Autowired
    private AssetInventoryDetailService assetInventoryDetailService;
    
    /**
     * @description 资产盘点详情分页
     * @param status 是否已盘点：0-未盘点,1-已盘点
     * @date 2023/11/21 13:21:30
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/inventory/detail/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "franchiseeId") Long franchiseeId,
            @RequestParam(value = "orderNo") String orderNo, @RequestParam(value = "status", required = false) Integer status) {
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
        
        AssetInventoryDetailRequest assetInventoryRequest = AssetInventoryDetailRequest.builder().size(size).offset(offset).franchiseeId(franchiseeId).orderNo(orderNo).status(status).build();
        
        return R.ok(assetInventoryDetailService.listByOrderNo(assetInventoryRequest));
    }
    
    /**
     * @description 资产盘点详情数量统计
     * @param status 是否已盘点：0-未盘点,1-已盘点
     * @date 2023/12/5 14:36:25
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/inventory/detail/pageCount")
    public R pageCount(@RequestParam(value = "franchiseeId") Long franchiseeId, @RequestParam(value = "orderNo") String orderNo,
            @RequestParam(value = "status", required = false) Integer status) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        AssetInventoryDetailRequest assetInventoryRequest = AssetInventoryDetailRequest.builder().franchiseeId(franchiseeId).orderNo(orderNo).status(status).build();
        
        return R.ok(assetInventoryDetailService.countTotal(assetInventoryRequest));
    }
    
    /***
     * @description 批量盘点
     * @date 2023/11/21 13:23:50
     * @author HeYafeng
     */
    @PostMapping("/admin/asset/inventory/detail/batchInventory")
    public R batchUpdate(@RequestBody @Validated(value = UpdateGroup.class) AssetInventoryDetailBatchInventoryRequest assetInventoryDetailBatchInventoryRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        return assetInventoryDetailService.batchInventory(assetInventoryDetailBatchInventoryRequest, user.getUid());
        
    }
    
}
