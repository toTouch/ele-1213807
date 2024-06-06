package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetWarehouseRecordRequest;
import com.xiliulou.electricity.service.asset.AssetWarehouseRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author HeYafeng
 * @description 库房资产记录
 * @date 2023/12/20 09:54:15
 */
@RestController
@Slf4j
public class JsonAdminWarehouseRecordController {
    
    @Resource
    private AssetWarehouseRecordService assetWarehouseRecordService;
    
    /**
     * @description 库房资产记录数量统计
     * @date 2023/12/20 10:21:56
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/warehouse/record/pageCount")
    public R pageCount(@RequestParam(value = "warehouseId") Long warehouseId, @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "sn", required = false) String sn) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        AssetWarehouseRecordRequest assetWarehouseRecordRequest = AssetWarehouseRecordRequest.builder().warehouseId(warehouseId).type(type).sn(sn).build();
        
        return R.ok(assetWarehouseRecordService.countTotal(assetWarehouseRecordRequest));
    }
    
    /**
     * @description 分页
     * @date 2023/12/20 10:21:56
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/warehouse/record/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "warehouseId") Long warehouseId,
            @RequestParam(value = "type", required = false) Integer type, @RequestParam(value = "sn", required = false) String sn) {
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
        
        AssetWarehouseRecordRequest assetWarehouseRecordRequest = AssetWarehouseRecordRequest.builder().size(size).offset(offset).warehouseId(warehouseId).type(type).sn(sn)
                .build();
        
        return R.ok(assetWarehouseRecordService.listByWarehouseId(assetWarehouseRecordRequest));
    }
    
}
