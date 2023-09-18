package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
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

    @GetMapping("/admin/franchisee/search")
    public R search(@RequestParam("size") Long size, @RequestParam("offset") Long offset,
                         @RequestParam(value = "name", required = false) String name) {
        if (size < 0 || size > 20) {
            size = 20L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        FranchiseeQuery franchiseeQuery = FranchiseeQuery.builder()
                .offset(offset)
                .size(size)
                .name(name)
                .tenantId(TenantContextHolder.getTenantId()).build();

        return R.ok(franchiseeService.search(franchiseeQuery));
    }

    //新增加盟商
    @PostMapping(value = "/admin/franchisee")
    public R save(@RequestBody @Validated(value = CreateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {

        return franchiseeService.save(franchiseeAddAndUpdate);
    }

    //修改加盟商
    @PutMapping(value = "/admin/franchisee")
	@Log(title = "修改加盟商")
	public R update(@RequestBody @Validated(value = UpdateGroup.class) FranchiseeAddAndUpdate franchiseeAddAndUpdate) {

        return franchiseeService.edit(franchiseeAddAndUpdate);
    }

	//删除加盟商
	@DeleteMapping(value = "/admin/franchisee/{id}")
	@Log(title = "删除加盟商")
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
            if(CollectionUtils.isEmpty(ids)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
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

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        List<Long> ids = null;
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            ids = userDataScopeService.selectDataIdByUid(user.getUid());
            if(CollectionUtils.isEmpty(ids)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
            return R.ok(Collections.EMPTY_LIST);
        }

        FranchiseeQuery franchiseeQuery = new FranchiseeQuery();
        franchiseeQuery.setIds(ids);
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
            if(CollectionUtils.isEmpty(ids)){
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if(Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)){
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


    //查询电池
    @GetMapping(value = "/admin/franchisee/getElectricityBatteryList/{id}")
    public R getElectricityBatteryList(@PathVariable("id") Long id) {
        return franchiseeService.getElectricityBatteryList(id);
    }

    /**
     * 删除电池型号校验
     */
    @Deprecated
    @GetMapping(value = "/admin/franchisee/checkBatteryType")
    public R checkBatteryType(@RequestParam("id") Long id, @RequestParam("batteryType") Integer batteryType) {
        return returnTripleResult(franchiseeService.checkBatteryType(id, batteryType));
    }

    //分账设置
    @PostMapping(value = "/admin/franchisee/setSplit")
	@Log(title = "加盟商分帐设置")
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
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
                .offset(offset)
                .size(size)
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
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
        if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
            franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return R.ok(Collections.EMPTY_LIST);
            }
        }

        if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .tenantId(TenantContextHolder.getTenantId())
                .franchiseeId(franchiseeId)
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
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "startTime", required = false) Long startTime,
			@RequestParam(value = "endTime", required = false) Long endTime){
		if (size < 0 || size > 50) {
			size = 50L;
		}

        if (offset < 0) {
            offset = 0L;
        }

		Integer tenantId = TenantContextHolder.getTenantId();

		FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
				.offset(offset)
				.size(size)
				.orderId(orderId)
				.startTime(startTime)
				.endTime(endTime)
				.tenantId(tenantId)
				.franchiseeId(franchiseeId).build();

        return franchiseeSplitAccountHistoryService.queryList(franchiseeAccountQuery);
    }


	/**
	 * 加盟商用户金额列表数量
	 */
	@GetMapping("/admin/franchisee/getAccountHistoryCount")
	public R getAccountHistoryCount(@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "startTime", required = false) Long startTime,
			@RequestParam(value = "endTime", required = false) Long endTime){


		Integer tenantId = TenantContextHolder.getTenantId();

		FranchiseeAccountQuery franchiseeAccountQuery = FranchiseeAccountQuery.builder()
				.startTime(startTime)
				.endTime(endTime)
				.orderId(orderId)
				.tenantId(tenantId)
				.franchiseeId(franchiseeId).build();

        return franchiseeSplitAccountHistoryService.queryCount(franchiseeAccountQuery);
    }


    //修改余额
    @PostMapping("/admin/franchisee/modifyAccount")
	@Log(title = "修改加盟商余额")
	public R modifyShopAccountAmount(@RequestParam("franchiseeId") Long franchiseeId,
                                     @RequestParam("balance") BigDecimal modifyBalance) {
        if (franchiseeId <= 0 || modifyBalance.compareTo(BigDecimal.valueOf(0.0)) >= 0) {
            return R.fail("LOCKER.10005", "不合法的参数");
        }

        return franchiseeAmountService.modifyBalance(franchiseeId, modifyBalance);
    }

}
