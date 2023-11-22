package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.queue.asset.ElectricityCabinetAddRequest;
import com.xiliulou.electricity.queue.asset.ElectricityCabinetOutWarehouseRequest;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
public class JsonAdminElectricityCabinetV2Controller extends BasicController {
    
    @Resource
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet/save")
    public R save(@RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetAddRequest electricityCabinetAddRequest) {
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
    @PostMapping(value = "/admin/electricityCabinet/outWarehouse")
    public R batchOutWarehouse(@RequestBody @Validated(value = UpdateGroup.class) List<ElectricityCabinetOutWarehouseRequest> list) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }
        return returnTripleResult(electricityCabinetV2Service.batchOutWarehouse(list));
    }
}
