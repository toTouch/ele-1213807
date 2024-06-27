package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseRecordRequest;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseSaveRequest;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseRecordService;
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
 * @description 资产退库
 * @date 2023/11/23 18:34:30
 */

@RestController
@Slf4j
public class AssetExitWarehouseRecordController {
    
    @Autowired
    private AssetExitWarehouseRecordService assetExitWarehouseRecordService;
    
    /**
     * @description 新增退库
     * @date 2023/11/21 13:15:41
     * @author HeYafeng
     */
    @PostMapping("/admin/asset/exit/warehouse/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) AssetExitWarehouseSaveRequest assetExitWarehouseSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
    
        return assetExitWarehouseRecordService.save(assetExitWarehouseSaveRequest, user.getUid());
    }
    
    /**
     * @description 资产退库数量统计
     * @param type 退库类型(1-电柜, 2-电池, 3-车辆)
     * @date 2023/11/21 18:17:54
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/exit/warehouse/pageCount")
    public R pageCount(@RequestParam(value = "orderNo", required = false) String orderNo, @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
            @RequestParam(value = "type", required = false) Integer type) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
        //    return R.fail("ELECTRICITY.0066", "用户权限不足");
        //}
    
        AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest = AssetExitWarehouseRecordRequest
                .builder()
                .orderNo(orderNo)
                .franchiseeId(franchiseeId)
                .type(type)
                .build();
        
        return R.ok(assetExitWarehouseRecordService.countTotal(assetExitWarehouseRecordRequest));
    }
    
    /**
     * @description 资产退库分页
     * @param type 退库类型(1-电柜, 2-电池, 3-车辆)
     * @date 2023/11/21 13:15:54
     * @author HeYafeng
     */
    @GetMapping("/admin/asset/exit/warehouse/page")
    public R page(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId, @RequestParam(value = "type", required = false) Integer type) {
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
        
        //if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
        //    return R.fail("ELECTRICITY.0066", "用户权限不足");
        //}
    
        AssetExitWarehouseRecordRequest assetExitWarehouseRecordRequest = AssetExitWarehouseRecordRequest
                .builder()
                .size(size)
                .offset(offset)
                .orderNo(orderNo)
                .franchiseeId(franchiseeId)
                .type(type)
                .build();
        
        return R.ok(assetExitWarehouseRecordService.listByPage(assetExitWarehouseRecordRequest));
    }
    
}
