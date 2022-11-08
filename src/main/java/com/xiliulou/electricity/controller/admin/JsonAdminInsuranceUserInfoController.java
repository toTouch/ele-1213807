package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.FranchiseeInsuranceAddAndUpdate;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 换电柜保险(InsuranceUserInfo)表服务接口
 *
 * @author makejava
 * @since 2022-11-03 13:37:11
 */
@RestController
@Slf4j
public class JsonAdminInsuranceUserInfoController {

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    @Autowired
    RedisService redisService;

    @Autowired
    FranchiseeService franchiseeService;


    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/insuranceUserInfo/detail")
    public R getElectricityMemberCardPage(@RequestParam(value = "uid") Long uid) {

        Integer tenantId = TenantContextHolder.getTenantId();


        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }


        return R.ok(insuranceUserInfoService.queryByUidAndTenantId(uid, tenantId));
    }


    /**
     * 更新用户保险出险状态
     * @param uid
     * @param insuranceStatus
     * @return
     */
    @PutMapping(value = "/admin/insuranceUserInfo/insuranceStatus")
    public R updateServiceStatus(@RequestParam("uid") Long uid,@RequestParam("insuranceStatus") Integer insuranceStatus){
        return insuranceUserInfoService.updateInsuranceStatus(uid,insuranceStatus);
    }

}
