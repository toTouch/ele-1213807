package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.request.asset.AssetAllocateRecordRequest;
import com.xiliulou.electricity.service.asset.AssetAllocateRecordService;
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
 * @description 资产调拨
 * @date 2023/11/28 09:42:34
 */
@RestController
@Slf4j
public class AssetAllocateRecordController {
    
    @Autowired
    private AssetAllocateRecordService assetAllocateRecordService;
    
    /**
     * @description 新增资产调拨
     * @date 2023/11/21 13:15:41
     * @author HeYafeng
     */
    @PostMapping("/admin/asset/allocate/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) AssetAllocateRecordRequest assetAllocateRecordRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        
        return assetAllocateRecordService.save(assetAllocateRecordRequest, user.getUid());
    }

}
