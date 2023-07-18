package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ChargeConfigListQuery;
import com.xiliulou.electricity.service.EleChargeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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

        return returnPairResult(eleChargeConfigService.queryList(chargeConfigListQuery));
    }


}
