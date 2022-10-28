package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.FranchiseeAmountService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FranchiseeSplitAccountHistoryService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 门店表(TStore)表控制层
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@RestController
@Slf4j
public class JsonAdminFranchiseeController extends BaseController {
    /**
     * 服务对象
     */
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    FranchiseeAmountService franchiseeAmountService;
    @Autowired
    FranchiseeSplitAccountHistoryService franchiseeSplitAccountHistoryService;
    @Autowired
    UserDataScopeService userDataScopeService;

    //新增加盟商
    @PostMapping(value = "/admin/franchisee")
    public R save(@RequestBody @Validated(value = CreateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        return franchiseeService.save(franchiseeAddAndUpdate);
    }

    //修改加盟商
    @PutMapping(value = "/admin/franchisee")
    public R update(@RequestBody @Validated(value = UpdateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {
        return franchiseeService.edit(franchiseeAddAndUpdate);
    }

    //删除加盟商
    @DeleteMapping(value = "/admin/franchisee/{id}")
    public R delete(@PathVariable("id") Long id) {
        if (Objects.isNull(id)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        return franchiseeService.delete(id);
    }

    //列表查询
    @GetMapping(value = "/admin/franchisee/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "beginTime", required = false) Long beginTime,
                       @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50 && size < 1000) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> ids = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            ids = userDataScopeService.selectDataIdByUid(user.getUid());
        }else if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }

        FranchiseeQuery franchiseeQuery = FranchiseeQuery.builder()
                .offset(offset)
                .size(size)
                .ids(ids)
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return franchiseeService.queryList(franchiseeQuery);
    }

    /**
     * 获取租户下加盟商列表
     * @return
     */
    @GetMapping(value = "/admin/franchisee/selectListByTenantId")
    public R selectFranchiseeList(){
        FranchiseeQuery franchiseeQuery = new FranchiseeQuery();
        franchiseeQuery.setTenantId(TenantContextHolder.getTenantId());
        return returnTripleResult(franchiseeService.selectListByQuery(franchiseeQuery));
    }

    //列表查询
    @GetMapping(value = "/admin/franchisee/queryCount")
    public R queryCount(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "beginTime", required = false) Long beginTime,
                        @RequestParam(value = "endTime", required = false) Long endTime) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> ids = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            ids = userDataScopeService.selectDataIdByUid(user.getUid());
        }else if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }

        FranchiseeQuery franchiseeQuery = FranchiseeQuery.builder()
                .name(name)
                .beginTime(beginTime)
                .endTime(endTime)
                .ids(ids)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return franchiseeService.queryCount(franchiseeQuery);

    }

    //分配电池
    @PostMapping(value = "/admin/franchisee/bindElectricityBattery")
    public R bindElectricityBattery(@RequestBody @Validated(value = CreateGroup.class) BindElectricityBatteryQuery bindElectricityBatteryQuery) {
        return franchiseeService.bindElectricityBattery(bindElectricityBatteryQuery);
    }

    //查询电池
    @GetMapping(value = "/admin/franchisee/getElectricityBatteryList/{id}")
    public R getElectricityBatteryList(@PathVariable("id") Long id) {
        return franchiseeService.getElectricityBatteryList(id);
    }

    //分账设置
    @PostMapping(value = "/admin/franchisee/setSplit")
    public R setSplit(@RequestBody List<FranchiseeSetSplitQuery> franchiseeSetSplitQueryList) {
        return franchiseeService.setSplit(franchiseeSetSplitQueryList);
    }


    /**
     * 加盟商用户金额列表
     */
    @GetMapping("/admin/franchisee/getAccountList")
    public R getAccountList(@RequestParam("size") Long size,
                            @RequestParam("offset") Long offset,
                            @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                            @RequestParam(value = "startTime", required = false) Long startTime,
                            @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 50L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
    
        List<Long> franchiseeIds = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        } else if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
                .offset(offset)
                .size(size)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(franchiseeIds).build();

        return franchiseeAmountService.queryList(franchiseeAccountQuery);
    }


    /**
     * 加盟商用户金额列表数量
     */
    @GetMapping("/admin/franchisee/getAccountCount")
    public R getAccountCount(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                             @RequestParam(value = "startTime", required = false) Long startTime,
                             @RequestParam(value = "endTime", required = false) Long endTime) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> franchiseeIds = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(franchiseeIds).build();

        return franchiseeAmountService.queryCount(franchiseeAccountQuery);
    }


    /**
     * 加盟商分账金额列表
     */
    @GetMapping("/admin/franchisee/getAccountHistoryList")
    public R getAccountHistoryList(@RequestParam("size") Long size,
                                   @RequestParam("offset") Long offset,
                                   @RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                   @RequestParam(value = "startTime", required = false) Long startTime,
                                   @RequestParam(value = "endTime", required = false) Long endTime) {
        if (size < 0 || size > 50) {
            size = 50L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
                .offset(offset)
                .size(size)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(Arrays.asList(franchiseeId)).build();

        return franchiseeSplitAccountHistoryService.queryList(franchiseeAccountQuery);
    }


    /**
     * 加盟商用户金额列表数量
     */
    @GetMapping("/admin/franchisee/getAccountHistoryCount")
    public R getAccountHistoryCount(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
                                    @RequestParam(value = "startTime", required = false) Long startTime,
                                    @RequestParam(value = "endTime", required = false) Long endTime) {

        FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeIds(Arrays.asList(franchiseeId)).build();

        return franchiseeSplitAccountHistoryService.queryCount(franchiseeAccountQuery);
    }


    //修改余额
    @PostMapping("/admin/franchisee/modifyAccount")
    public R modifyShopAccountAmount(@RequestParam("franchiseeId") Long franchiseeId,
                                     @RequestParam("balance") BigDecimal modifyBalance) {
        if (franchiseeId <= 0 || modifyBalance.compareTo(BigDecimal.valueOf(0.0)) >= 0) {
            return R.fail("LOCKER.10005", "不合法的参数");
        }

        return franchiseeAmountService.modifyBalance(franchiseeId, modifyBalance);
    }

}
