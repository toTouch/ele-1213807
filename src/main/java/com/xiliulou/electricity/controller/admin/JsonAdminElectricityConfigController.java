package com.xiliulou.electricity.controller.admin;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityConfigExtra;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;
import com.xiliulou.electricity.service.ElectricityConfigExtraService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.vo.ElectricityConfigVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
public class JsonAdminElectricityConfigController {
    /**
     * 服务对象
     */
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Resource
    private ElectricityConfigExtraService electricityConfigExtraService;

    //编辑平台名称
    @PutMapping(value = "/admin/electricityConfig")
    @Log(title = "编辑平台信息")
    public R edit(@RequestBody @Validated(value = CreateGroup.class)ElectricityConfigAddAndUpdateQuery electricityConfigAddAndUpdateQuery) {
        return electricityConfigService.edit(electricityConfigAddAndUpdateQuery);
    }

    //查询平台名称
    @GetMapping(value = "/admin/electricityConfig")
    public R queryOne() {
        Integer tenantId = TenantContextHolder.getTenantId();
    
        ElectricityConfig electricityConfig = electricityConfigService.queryByTenantId(tenantId);
        ElectricityConfigExtra electricityConfigExtra = electricityConfigExtraService.queryByTenantIdFromCache(tenantId);
    
        ElectricityConfigVO electricityConfigVO = new ElectricityConfigVO();
        if (Objects.nonNull(electricityConfig)) {
            BeanUtils.copyProperties(electricityConfig, electricityConfigVO);
        }
        if (Objects.nonNull(electricityConfigExtra)) {
            electricityConfigVO.setAccountDelSwitch(electricityConfigExtra.getAccountDelSwitch());
            electricityConfigVO.setDelUserMarkSwitch(electricityConfigExtra.getDelUserMarkSwitch());
        }
    
        return R.ok(electricityConfigVO);
    }

}
