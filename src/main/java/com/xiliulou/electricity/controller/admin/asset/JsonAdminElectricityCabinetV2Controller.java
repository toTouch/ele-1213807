package com.xiliulou.electricity.controller.admin.asset;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.request.asset.ElectricityCabinetRequest;
import com.xiliulou.electricity.service.asset.ElectricityCabinetV2Service;
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

@RestController
@Slf4j
public class JsonAdminElectricityCabinetV2Controller extends BasicController {
    
    @Autowired
    private ElectricityCabinetV2Service electricityCabinetV2Service;
    
    //新增换电柜
    @PostMapping(value = "/admin/electricityCabinet/save")
    public R save(
            @RequestBody @Validated(value = CreateGroup.class) ElectricityCabinetRequest electricityCabinetRequest) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        return electricityCabinetV2Service.save(electricityCabinetRequest);
    }
    
}
