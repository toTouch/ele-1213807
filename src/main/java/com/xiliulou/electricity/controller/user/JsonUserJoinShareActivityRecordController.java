package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
@Slf4j
public class JsonUserJoinShareActivityRecordController {
	/**
	 * 服务对象
	 */
	@Autowired
	private JoinShareActivityRecordService joinShareActivityRecordService;

	@Autowired
	GenerateShareUrlService generateShareUrlService;


	/**
	 * 点击分享链接进入活动
	 */
	@PostMapping(value = "/user/joinShareActivityRecord/joinActivity")
	public R joinActivity(@RequestParam(value = "activityId") Integer activityId, @RequestParam(value = "uid") Long uid) {
		return joinShareActivityRecordService.joinActivity(activityId, uid);
	}

}
