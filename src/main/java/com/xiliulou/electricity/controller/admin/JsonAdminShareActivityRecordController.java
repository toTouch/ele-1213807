package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ShareActivityRecordQuery;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;

/**
 * 发起邀请活动记录(ShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:45:04
 */
@RestController
public class JsonAdminShareActivityRecordController {

	@Autowired
	private ShareActivityRecordService shareActivityRecordService;

	//列表查询
	@GetMapping(value = "/admin/shareActivityRecord/list")
	public R queryList(@RequestParam(value = "size", required = false) Long size,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "name", required = false) String name) {
		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		ShareActivityRecordQuery shareActivityRecordQuery = ShareActivityRecordQuery.builder()
				.offset(offset)
				.size(size)
				.phone(phone)
				.name(name).build();

		return shareActivityRecordService.queryList(shareActivityRecordQuery);

	}


	//列表查询
	@GetMapping(value = "/admin/shareActivityRecord/queryCount")
	public R queryCount(@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "name", required = false) String name) {

		ShareActivityRecordQuery shareActivityRecordQuery = ShareActivityRecordQuery.builder()
				.phone(phone)
				.name(name).build();

		return shareActivityRecordService.queryCount(shareActivityRecordQuery);

	}


}

