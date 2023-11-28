package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.enums.asset.StockStatusEnum;
import com.xiliulou.electricity.request.asset.AssetWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetBatchOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetOutWarehouseRequest;
import com.xiliulou.electricity.request.asset.ElectricityCabinetSnSearchRequest;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
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
import java.util.Objects;

@RestController
@Slf4j
public class JsonAdminElectricityCabinetV2Controller extends BasicController {
    
    @Resource
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    @Resource
    private AssetWarehouseService assetWarehouseService;
    
    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetAddRequest electricityCabinetAddRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(electricityCabinetV2Service.save(electricityCabinetAddRequest));
    }
    
    //出库
    @PostMapping(value = "/admin/electricityCabinet/outWarehouse")
    public R outWarehouse(@RequestBody @Validated(value = UpdateGroup.class) ElectricityCabinetOutWarehouseRequest outWarehouseRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(electricityCabinetV2Service.outWarehouse(outWarehouseRequest));
    }
    
    //出库
    @PostMapping(value = "/admin/electricityCabinet/batchOutWarehouse")
    public R batchOutWarehouse(@RequestBody @Validated(value = UpdateGroup.class) ElectricityCabinetBatchOutWarehouseRequest batchOutWarehouseRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return returnTripleResult(electricityCabinetV2Service.batchOutWarehouse(batchOutWarehouseRequest));
    }
    
    /**
     * @description 查询租户下的库房名称
     * @date 2023/11/21 20:49:07
     * @author HeYafeng
     */
    @GetMapping("/admin/electricityCabinet/warehouse/nameSearch")
    public R listWarehouseNames(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "name", required = false) String name) {
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
        
        AssetWarehouseRequest assetInventoryRequest = AssetWarehouseRequest.builder().size(size).offset(offset).name(name).build();
        
        return R.ok(assetWarehouseService.listWarehouseNames(assetInventoryRequest));
    }
    
    @GetMapping("/admin/electricityCabinet/snSearch")
    public R snSearchByFranchiseeId(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "franchiseeId") Long franchiseeId) {
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
        
        ElectricityCabinetSnSearchRequest electricityCabinetSnSearchRequest = ElectricityCabinetSnSearchRequest.builder().franchiseeId(franchiseeId)
                .stockStatus(StockStatusEnum.UN_STOCK.getCode()).size(size).offset(offset).build();
        
        return R.ok(electricityCabinetV2Service.listByFranchiseeIdAndStockStatus(electricityCabinetSnSearchRequest));
        
    }
}
