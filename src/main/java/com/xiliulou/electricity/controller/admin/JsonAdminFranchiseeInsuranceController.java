package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 换电柜保险(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@RestController
public class JsonAdminFranchiseeInsuranceController {

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    RedisService redisService;


    /**
     * 新增保险配置
     *
     */
    @PostMapping(value = "/admin/franchiseeInsurance")
    public R updateEleAuthEntries(@RequestBody @Validated FranchiseeInsurance franchiseeInsurance) {
        return franchiseeInsuranceService.add(franchiseeInsurance);
    }





}
