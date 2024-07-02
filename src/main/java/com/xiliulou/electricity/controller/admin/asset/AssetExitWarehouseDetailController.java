package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetExitWarehouseDetailRequest;
import com.xiliulou.electricity.service.asset.AssetExitWarehouseDetailService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author HeYafeng
 * @description 退库详情
 * @date 2023/11/28 09:42:34
 */
@RestController
@Slf4j
public class AssetExitWarehouseDetailController {
    
    @Autowired
    private AssetExitWarehouseDetailService assetExitWarehouseDetailService;
    
    @GetMapping("/admin/asset/exit/warehouse/detail")
    public R exitWarehouseSnSearch(@RequestParam("size") long size, @RequestParam("offset") long offset, @RequestParam(value = "orderNo") String orderNo) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.warn("ELE WARN! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        AssetExitWarehouseDetailRequest assetExitWarehouseDetailRequest = AssetExitWarehouseDetailRequest.builder().orderNo(orderNo).size(size).offset(offset).build();
        
        return R.ok(assetExitWarehouseDetailService.listSnByOrderNo(assetExitWarehouseDetailRequest));
    }
    
}
