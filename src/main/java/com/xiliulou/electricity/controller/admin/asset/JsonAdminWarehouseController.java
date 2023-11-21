package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetWarehouseSaveRequest;
import com.xiliulou.electricity.service.asset.AssetWarehouseService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public R save(@RequestBody @Validated(value = CreateGroup.class) AssetWarehouseSaveRequest assetWarehouseSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        assetWarehouseSaveRequest.setUid(user.getUid());
        
        return assetWarehouseService.save(assetWarehouseSaveRequest);
    }
    
}
