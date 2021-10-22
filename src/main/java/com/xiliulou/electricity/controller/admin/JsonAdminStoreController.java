package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.query.FranchiseeAccountQuery;
import com.xiliulou.electricity.query.StoreAccountQuery;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreAmountService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.StoreSplitAccountHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
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
public class JsonAdminStoreController {
	/**
	 * 服务对象
	 */
	@Autowired
	StoreService storeService;

	@Autowired
	FranchiseeService franchiseeService;

	@Autowired
	StoreAmountService storeAmountService;

	@Autowired
	StoreSplitAccountHistoryService storeSplitAccountHistoryService;

	//新增门店
	@PostMapping(value = "/admin/store")
	public R save(@RequestBody @Validated(value = CreateGroup.class) StoreAddAndUpdate storeAddAndUpdate) {
		return storeService.save(storeAddAndUpdate);
	}

	//修改门店
	@PutMapping(value = "/admin/store")
	public R update(@RequestBody @Validated(value = UpdateGroup.class) StoreAddAndUpdate storeAddAndUpdate) {
		return storeService.edit(storeAddAndUpdate);
	}

	//删除门店
	@DeleteMapping(value = "/admin/store/{id}")
	public R delete(@PathVariable("id") Long id) {
		if (Objects.isNull(id)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return storeService.delete(id);
	}

	//列表查询
	@GetMapping(value = "/admin/store/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "usableStatus", required = false) Integer usableStatus,
			@RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		StoreQuery storeQuery = StoreQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.beginTime(beginTime)
				.endTime(endTime)
				.address(address)
				.usableStatus(usableStatus)
				.tenantId(tenantId)
				.franchiseeId(franchiseeId).build();

		return storeService.queryList(storeQuery);
	}

	//列表查询
	@GetMapping(value = "/admin/store/queryCount")
	public R queryCount(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "usableStatus", required = false) Integer usableStatus,
			@RequestParam(value = "franchiseeId", required = false) Long franchiseeId) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		StoreQuery storeQuery = StoreQuery.builder()
				.name(name)
				.beginTime(beginTime)
				.endTime(endTime)
				.address(address)
				.usableStatus(usableStatus)
				.tenantId(tenantId)
				.franchiseeId(franchiseeId).build();

		return storeService.queryCount(storeQuery);
	}

	//加盟商列表查询
	@GetMapping(value = "/admin/store/listByFranchisee")
	public R listByFranchisee(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "usableStatus", required = false) Integer usableStatus) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		StoreQuery storeQuery = StoreQuery.builder()
				.offset(offset)
				.size(size)
				.name(name)
				.beginTime(beginTime)
				.endTime(endTime)
				.address(address)
				.usableStatus(usableStatus)
				.tenantId(tenantId).build();

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//1、先找到加盟商
		Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
		if (ObjectUtil.isEmpty(franchisee)) {
			return R.ok(new ArrayList<>());
		}

		List<Store> storeList = storeService.queryByFranchiseeId(franchisee.getId());

		if (ObjectUtil.isEmpty(storeList)) {
			return R.ok(new ArrayList<>());
		}
		//2、再找加盟商绑定的门店
		List<Long> storeIdList = new ArrayList<>();
		for (Store store : storeList) {
			storeIdList.add(store.getId());
		}
		if (ObjectUtil.isEmpty(storeIdList)) {
			return R.ok(new ArrayList<>());
		}

		storeQuery.setStoreIdList(storeIdList);

		return storeService.queryList(storeQuery);
	}

	//加盟商列表查询
	@GetMapping(value = "/admin/store/queryCountByFranchisee")
	public R queryCountByFranchisee(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "address", required = false) String address,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "usableStatus", required = false) Integer usableStatus) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		StoreQuery storeQuery = StoreQuery.builder()
				.name(name)
				.beginTime(beginTime)
				.endTime(endTime)
				.address(address)
				.usableStatus(usableStatus)
				.tenantId(tenantId).build();

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//1、先找到加盟商
		Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
		if (ObjectUtil.isEmpty(franchisee)) {
			return R.ok(0);
		}

		List<Store> storeList = storeService.queryByFranchiseeId(franchisee.getId());

		if (ObjectUtil.isEmpty(storeList)) {
			return R.ok(0);
		}
		//2、再找加盟商绑定的门店
		List<Long> storeIdList = new ArrayList<>();
		for (Store store : storeList) {
			storeIdList.add(store.getId());
		}
		if (ObjectUtil.isEmpty(storeIdList)) {
			return R.ok(0);
		}

		storeQuery.setStoreIdList(storeIdList);

		return storeService.queryCountByFranchisee(storeQuery);
	}

	//禁启用门店
	@PutMapping(value = "/admin/store/updateStatus")
	public R updateStatus(@RequestParam("id") Long id, @RequestParam("usableStatus") Integer usableStatus) {
		return storeService.updateStatus(id, usableStatus);
	}


	/**
	 * 门店用户金额列表
	 */
	@GetMapping("/admin/store/getAccountList")
	public R getAccountList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "storeId", required = false) Long storeId,
			@RequestParam(value = "startTime", required = false) Long startTime,
			@RequestParam(value = "endTime", required = false) Long endTime){
		if (size < 0 || size > 50) {
			size = 50L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		Integer tenantId = TenantContextHolder.getTenantId();

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//1、先找到加盟商
		Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
		if (ObjectUtil.isEmpty(franchisee)) {
			return R.ok(0);
		}

		List<Store> storeList = storeService.queryByFranchiseeId(franchisee.getId());

		if (ObjectUtil.isEmpty(storeList)) {
			return R.ok(0);
		}
		//2、再找加盟商绑定的门店
		List<Long> storeIdList = new ArrayList<>();
		for (Store store : storeList) {
			storeIdList.add(store.getId());
		}
		if (ObjectUtil.isEmpty(storeIdList)) {
			return R.ok(0);
		}


		StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
				.offset(offset)
				.size(size)
				.startTime(startTime)
				.endTime(endTime)
				.tenantId(tenantId)
				.storeId(storeId)
				.storeIdList(storeIdList).build();

		return storeAmountService.queryList(storeAccountQuery);
	}


	/**
	 * 门店用户金额列表数量
	 */
	@GetMapping("/admin/store/getAccountCount")
	public R getAccountCount(@RequestParam(value = "storeId", required = false) Long storeId,
			@RequestParam(value = "startTime", required = false) Long startTime,
			@RequestParam(value = "endTime", required = false) Long endTime){


		Integer tenantId = TenantContextHolder.getTenantId();

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//1、先找到加盟商
		Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
		if (ObjectUtil.isEmpty(franchisee)) {
			return R.ok(0);
		}

		List<Store> storeList = storeService.queryByFranchiseeId(franchisee.getId());

		if (ObjectUtil.isEmpty(storeList)) {
			return R.ok(0);
		}
		//2、再找加盟商绑定的门店
		List<Long> storeIdList = new ArrayList<>();
		for (Store store : storeList) {
			storeIdList.add(store.getId());
		}
		if (ObjectUtil.isEmpty(storeIdList)) {
			return R.ok(0);
		}


		StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
				.startTime(startTime)
				.endTime(endTime)
				.tenantId(tenantId)
				.storeId(storeId)
				.storeIdList(storeIdList).build();

		return storeAmountService.queryCount(storeAccountQuery);
	}


	@GetMapping("/admin/store/getAccountHistoryList")
	public R getAccountHistoryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "storeId", required = false) Long storeId,
			@RequestParam(value = "startTime", required = false) Long startTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {
		if (size < 0 || size > 50) {
			size = 50L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
				.offset(offset)
				.size(size)
				.startTime(startTime)
				.endTime(endTime)
				.storeId(storeId).build();

		return storeSplitAccountHistoryService.queryList(storeAccountQuery);
	}


	@GetMapping("/admin/store/getAccountHistoryCount")
	public R getAccountHistoryCount(
			@RequestParam(value = "storeId", required = false) Long storeId,
			@RequestParam(value = "startTime", required = false) Long startTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "oid", required = false) Long oid) {


		StoreAccountQuery storeAccountQuery = StoreAccountQuery.builder()
				.startTime(startTime)
				.endTime(endTime)
				.storeId(storeId).build();

		return storeSplitAccountHistoryService.queryCount(storeAccountQuery);
	}

	//修改余额
	@PostMapping("/admin/store/modifyAccount")
	public R modifyShopAccountAmount(@RequestParam("storeId") Long storeId,
			@RequestParam("balance") BigDecimal modifyBalance) {
		if (storeId <= 0 || modifyBalance.compareTo(BigDecimal.valueOf(0.0)) >= 0) {
			return R.fail("LOCKER.10005", "不合法的参数");
		}

		return storeAmountService.modifyBalance(storeId, modifyBalance);
	}


}
