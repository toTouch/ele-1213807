package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.JsonShareActivityHistoryQuery;
import com.xiliulou.electricity.query.JsonShareMoneyActivityHistoryQuery;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareMoneyActivityHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonAdminJoinShareMoneyActivityHistoryController {
	/**
	 * 服务对象
	 */
	@Resource
	private JoinShareMoneyActivityHistoryService joinShareMoneyActivityHistoryService;



	/**
	 * 用户参与记录admin
	 */
	@GetMapping(value = "/admin/joinShareMoneyActivityHistory/list")
	public R joinActivity(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam( "uid") Long uid,
			@RequestParam( "activityId") Integer activityId) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		JsonShareMoneyActivityHistoryQuery jsonShareMoneyActivityHistoryQuery = JsonShareMoneyActivityHistoryQuery.builder()
				.offset(offset)
				.size(size)
				.uid(uid)
				.activityId(activityId).build();
		return joinShareMoneyActivityHistoryService.queryList(jsonShareMoneyActivityHistoryQuery);
	}

}































