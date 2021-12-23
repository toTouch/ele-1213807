package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.UserAmountHistoryQuery;
import com.xiliulou.electricity.service.UserAmountHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonUserAmountHistoryController {
	/**
	 * 服务对象
	 */
	@Autowired
	private UserAmountHistoryService userAmountHistoryService;



	/**
	 * 用户邀请记录
	 */
	@GetMapping(value = "/user/userAmountHistory/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "type", required = false) Integer type) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户区分
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		UserAmountHistoryQuery userAmountHistoryQuery = UserAmountHistoryQuery.builder()
				.offset(offset)
				.size(size)
				.tenantId(tenantId)
				.uid(user.getUid())
				.type(type).build();
		return userAmountHistoryService.queryList(userAmountHistoryQuery);
	}

}
