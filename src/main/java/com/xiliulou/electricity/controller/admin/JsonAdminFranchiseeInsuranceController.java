package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.FranchiseeInsuranceAddAndUpdate;
import com.xiliulou.electricity.service.EleAuthEntryService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 换电柜保险(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@RestController
@Slf4j
public class JsonAdminFranchiseeInsuranceController {

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    RedisService redisService;

    @Autowired
    FranchiseeService franchiseeService;


    /**
     * 新增保险配置
     *
     */
    @PostMapping(value = "/admin/franchiseeInsurance")
    public R updateEleAuthEntries(@RequestBody @Validated FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {
        return franchiseeInsuranceService.add(franchiseeInsuranceAddAndUpdate);
    }


    /**
     * 修改
     *
     * @return
     */
    @PutMapping("admin/franchiseeInsurance")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {
        if (Objects.isNull(franchiseeInsuranceAddAndUpdate)) {
            return R.failMsg("请求参数不能为空!");
        }
        return franchiseeInsuranceService.update(franchiseeInsuranceAddAndUpdate);
    }

    /**
     * 删除
     *
     * @return
     */
    @DeleteMapping("admin/franchiseeInsurance/{id}")
    public R delete(@PathVariable(value = "id") Integer id) {
        return franchiseeInsuranceService.delete(id);
    }


    /**
     * 分页
     *
     * @return
     */
    @GetMapping("admin/franchiseeInsurance/list")
    public R getElectricityMemberCardPage(@RequestParam(value = "offset") Long offset,
                                          @RequestParam(value = "size") Long size,
                                          @RequestParam(value = "insuranceType", required = false) Integer insuranceType,
                                          @RequestParam(value = "status", required = false) Integer status) {

        Integer tenantId = TenantContextHolder.getTenantId();


        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Long franchiseeId = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        return franchiseeInsuranceService.queryList(offset, size, status, insuranceType, tenantId, franchiseeId);
    }

    /**
     * 分页数量
     *
     * @return
     */
    @GetMapping("admin/franchiseeInsurance/queryCount")
    public R getElectricityMemberCardPage(@RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "cardModel", required = false) Integer cardModel) {

        Integer tenantId = TenantContextHolder.getTenantId();

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        Long franchiseeId = null;
        if (Objects.equals(user.getType(), User.TYPE_USER_FRANCHISEE)) {
            //加盟商
            Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
            if (Objects.nonNull(franchisee)) {
                franchiseeId = franchisee.getId();
            }
        }

        return franchiseeInsuranceService.queryCount(status, type, tenantId, franchiseeId);
    }

}
