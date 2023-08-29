package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.FranchiseeInsuranceAddAndUpdate;
import com.xiliulou.electricity.query.FranchiseeInsuranceQuery;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Objects;

/**
 * 换电柜保险(FranchiseeInsurance)表服务接口
 *
 * @author makejava
 * @since 2022-11-02 13:37:11
 */
@RestController
@Slf4j
public class JsonAdminFranchiseeInsuranceController extends BaseController {

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    RedisService redisService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    UserInfoService userInfoService;

    /**
     * 新增保险配置
     */
    @PostMapping(value = "/admin/franchiseeInsurance")
    public R updateEleAuthEntries(@RequestBody @Validated FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

        return franchiseeInsuranceService.add(franchiseeInsuranceAddAndUpdate);
    }


    /**
     * 修改
     *
     * @return
     */
    @PutMapping("admin/franchiseeInsurance")
    @Log(title = "修改保险")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) FranchiseeInsuranceAddAndUpdate franchiseeInsuranceAddAndUpdate) {
        if (Objects.isNull(franchiseeInsuranceAddAndUpdate)) {
            return R.failMsg("请求参数不能为空!");
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

        return franchiseeInsuranceService.update(franchiseeInsuranceAddAndUpdate);
    }


    /**
     * 禁用启用保险
     *
     * @return
     */
    @PutMapping("admin/franchiseeInsurance/enableOrDisable")
    @Log(title = "禁启用保险保险")
    public R enableOrDisable(@RequestParam("id") Long id, @RequestParam("usableStatus") Integer usableStatus) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

        return franchiseeInsuranceService.enableOrDisable(id, usableStatus);
    }

    /**
     * 删除
     *
     * @return
     */
    @DeleteMapping("admin/franchiseeInsurance/{id}")
    @Log(title = "删除保险")
    public R delete(@PathVariable(value = "id") Integer id) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE) || Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE))) {
            return R.ok();
        }

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
                                          @RequestParam(value = "name", required = false) String name,
                                          @RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "insuranceType", required = false) Integer insuranceType,
                                          @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                          @RequestParam(value = "status", required = false) Integer status) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(Collections.emptyList());
        }

        FranchiseeInsuranceQuery query = FranchiseeInsuranceQuery.builder()
                .offset(offset)
                .size(size)
                .type(type)
                .franchiseeId(franchiseeId)
                .insuranceType(insuranceType)
                .name(name)
                .status(status)
                .tenantId(TenantContextHolder.getTenantId()).build();


//        return franchiseeInsuranceService.queryList(offset, size, status, insuranceType, tenantId, franchiseeId);
        return R.ok(franchiseeInsuranceService.selectByPage(query));
    }

    /**
     * 分页数量
     *
     * @return
     */
    @GetMapping("admin/franchiseeInsurance/queryCount")
    public R getElectricityMemberCardPage(@RequestParam(value = "insuranceType", required = false) Integer insuranceType,
                                          @RequestParam(value = "name", required = false) String name,
                                          @RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

        Integer tenantId = TenantContextHolder.getTenantId();

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!(SecurityUtils.isAdmin() || Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE))) {
            return R.ok(NumberConstant.ZERO);
        }

        FranchiseeInsuranceQuery query = FranchiseeInsuranceQuery.builder()
                .franchiseeId(franchiseeId)
                .type(type)
                .insuranceType(insuranceType)
                .name(name)
                .status(status)
                .tenantId(TenantContextHolder.getTenantId()).build();
//        return franchiseeInsuranceService.queryCount(status, insuranceType, tenantId, franchiseeId);
        return R.ok(franchiseeInsuranceService.selectPageCount(query));
    }

    /**
     * 多型号下查询可新增的保险的型号
     * @param franchiseeId
     * @return
     */
    @GetMapping("admin/franchiseeInsurance/queryCanAddInsuranceBatteryType")
    public R queryCanAddInsuranceBatteryType(@RequestParam(value = "franchiseeId") Long franchiseeId) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        return franchiseeInsuranceService.queryCanAddInsuranceBatteryType(franchiseeId);
    }

    /**
     * 展示加盟商保险列表
     * @param franchiseeId,batteryType
     * @return
     */
    @GetMapping("admin/franchiseeInsurance/Insurancelist")
    public R getFranchiseeInsurancePage(  @RequestParam(value = "franchiseeId") Long franchiseeId,@RequestParam(value = "model") String batteryType) {

        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("ELE ERROR! not found franchisee,franchiseeId={}", franchiseeId);
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }

        return franchiseeInsuranceService.selectInsuranceListByCondition(FranchiseeInsurance.STATUS_USABLE, FranchiseeInsurance.INSURANCE_TYPE_BATTERY, TenantContextHolder.getTenantId(), franchisee.getId(),batteryType);
    }


    /**
     * 获取用户可购买保险
     */
    @GetMapping("/admin/franchiseeInsurance/selectInsuranceByUid")
    public R selectInsuranceByUid(@RequestParam(value = "uid") Long uid, @RequestParam(value = "type") Integer type) {
        return returnTripleResult(franchiseeInsuranceService.selectInsuranceByUid(uid, type));
    }


}
