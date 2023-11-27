package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetInventorySaveOrUpdateRequest;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author HeYafeng
 * @description 资产退库
 * @date 2023/11/23 18:34:30
 */

@RestController
@Slf4j
public class AssetExitWarehouseRecord {
    
    /**
     * @description 新增退库
     * @date 2023/11/21 13:15:41
     * @author HeYafeng
     */
    /*@PostMapping("/admin/asset/exitWarehouse/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) AssetExitWarehouseSaveOrUpdateRequest assetInventorySaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        assetInventorySaveRequest.setUid(user.getUid());
        
        return assetInventoryService.save(assetInventorySaveRequest);
    }*/
    
}
