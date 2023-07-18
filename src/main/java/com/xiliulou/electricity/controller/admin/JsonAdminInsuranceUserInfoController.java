package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.InsuranceUserInfoVo;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        InsuranceUserInfoVo insuranceUserInfoVo = insuranceUserInfoService.queryByUidAndTenantId(uid, tenantId);
        if (Objects.isNull(insuranceUserInfoVo)) {
            return R.ok();
        }

        return R.ok(insuranceUserInfoVo);
    }


    /**
     * 更新用户保险出险状态
     *
     */
    @PutMapping(value = "/admin/insuranceUserInfo/insuranceStatus")
    @Log(title = "修改用户电池保险状态")
    public R updateServiceStatus(@RequestParam("uid") Long uid, @RequestParam("insuranceStatus") Integer insuranceStatus) {
        return insuranceUserInfoService.updateUserBatteryInsuranceStatus(uid, insuranceStatus);
    }

    @PostMapping(value = "/admin/insuranceUserInfo/addUserInsurance")
    @Log(title = "新增用户电池保险信息")
    public R addUserInsuranceInfo(@RequestBody InsuranceUserInfo order){
        return insuranceUserInfoService.insertUserBatteryInsurance(order);
    }

    @PutMapping(value = "/admin/insuranceUserInfo/editUserInsurance")
    @Log(title = "修改用户电池保险信息")
    public R editUserInsuranceInfo(@RequestBody InsuranceUserInfo order){
        return insuranceUserInfoService.editUserInsuranceInfo(order);
    }

    @PutMapping(value = "/admin/insuranceUserInfo/renewalUserInsurance")
    @Log(title = "续费用户电池保险")
    public R renewalUserInsuranceInfo(@RequestBody InsuranceUserInfo order){
        return insuranceUserInfoService.renewalUserBatteryInsurance(order);
    }
}
