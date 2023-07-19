package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.query.ChargeConfigListQuery;
import com.xiliulou.electricity.query.ChargeConfigQuery;
import com.xiliulou.electricity.service.EleChargeConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @author : eclair
 * @date : 2023/7/18 10:22
 */
@RestController
public class JsonAdminEleChargeConfigController extends BaseController {
    @Autowired
    EleChargeConfigService eleChargeConfigService;

    @GetMapping("/admin/charge/config/list")
    public R getList(ChargeConfigListQuery chargeConfigListQuery) {
        if (Objects.isNull(chargeConfigListQuery.getSize()) || chargeConfigListQuery.getSize() >= 50 || chargeConfigListQuery.getSize() < 0) {
            chargeConfigListQuery.setSize(10);
        }

        if (Objects.isNull(chargeConfigListQuery.getOffset()) || chargeConfigListQuery.getOffset() < 0) {
            chargeConfigListQuery.setOffset(0);
        }

        chargeConfigListQuery.setTenantId(TenantContextHolder.getTenantId());
        return returnPairResult(eleChargeConfigService.queryList(chargeConfigListQuery));
    }

    @PostMapping("/admin/charge/config/save")
    @Log(title = "增加电费规则")
    public R saveConfig(@RequestBody @Validated ChargeConfigQuery chargeConfigQuery) {
        return returnPairResult(eleChargeConfigService.saveConfig(chargeConfigQuery));
    }

    @PostMapping("/admin/charge/config/modify")
    @Log(title = "修改电费规则")
    public R modifyConfig(@RequestBody @Validated(UpdateGroup.class) ChargeConfigQuery chargeConfigQuery) {
        return returnPairResult(eleChargeConfigService.modifyConfig(chargeConfigQuery));
    }

    @PostMapping("/admin/charge/config/del/{id}")
    @Log(title = "删除电费规则")
    public R delConfig(@PathVariable("id") Long id) {
        return returnPairResult(eleChargeConfigService.delConfig(id));
    }


}
