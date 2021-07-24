package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
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
public class JsonUserJoinShareActivityHistoryController {
	/**
	 * 服务对象
	 */
	@Resource
	private JoinShareActivityHistoryService joinShareActivityHistoryService;



	/**
	 * 用户邀请记录
	 */
	@GetMapping(value = "/user/joinShareActivityHistory/list")
	public R joinActivity(@RequestParam( "activityId") Integer activityId) {
		return joinShareActivityHistoryService.userList(activityId);
	}

}
