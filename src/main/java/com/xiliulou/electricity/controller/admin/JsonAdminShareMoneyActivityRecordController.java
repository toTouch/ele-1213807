package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityRecordQuery;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareMoneyActivityRecordService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 发起邀请活动记录(ShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@RestController
public class JsonAdminShareMoneyActivityRecordController {

	@Autowired
	private ShareMoneyActivityRecordService shareMoneyActivityRecordService;

	@Autowired
	UserDataScopeService userDataScopeService;

	//列表查询
	@GetMapping(value = "/admin/shareMoneyActivityRecord/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "name", required = false) String name) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		List<Long> storeIds = null;
		if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
			storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
			if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
				return R.ok(Collections.EMPTY_LIST);
			}
		}

		List<Long> franchiseeIds = null;
		if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
			franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
			if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
				return R.ok(Collections.EMPTY_LIST);
			}
		}


		ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery = ShareMoneyActivityRecordQuery.builder()
				.offset(offset)
				.size(size)
				.phone(phone)
				.name(name)
				.storeIds(storeIds)
				.franchiseeIds(franchiseeIds)
				.tenantId(TenantContextHolder.getTenantId()).build();

		return shareMoneyActivityRecordService.queryList(shareMoneyActivityRecordQuery);

	}

	//列表查询
	@GetMapping(value = "/admin/shareMoneyActivityRecord/queryCount")
	public R queryCount(@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "name", required = false) String name) {

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		List<Long> storeIds = null;
		if (Objects.equals(user.getDataType(), User.DATA_TYPE_STORE)) {
			storeIds = userDataScopeService.selectDataIdByUid(user.getUid());
			if(org.apache.commons.collections.CollectionUtils.isEmpty(storeIds)){
				return R.ok(Collections.EMPTY_LIST);
			}
		}

		List<Long> franchiseeIds = null;
		if (Objects.equals(user.getDataType(), User.DATA_TYPE_FRANCHISEE)) {
			franchiseeIds = userDataScopeService.selectDataIdByUid(user.getUid());
			if(org.apache.commons.collections.CollectionUtils.isEmpty(franchiseeIds)){
				return R.ok(Collections.EMPTY_LIST);
			}
		}


		ShareMoneyActivityRecordQuery shareMoneyActivityRecordQuery = ShareMoneyActivityRecordQuery.builder()
				.phone(phone)
				.name(name)
				.storeIds(storeIds)
				.franchiseeIds(franchiseeIds)
				.tenantId(TenantContextHolder.getTenantId()).build();

		return shareMoneyActivityRecordService.queryCount(shareMoneyActivityRecordQuery);

	}

}

