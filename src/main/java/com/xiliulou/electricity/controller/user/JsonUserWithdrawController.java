package com.xiliulou.electricity.controller.user;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstants;
import com.xiliulou.electricity.query.CheckQuery;
import com.xiliulou.electricity.query.WithdrawQuery;
import com.xiliulou.electricity.query.WithdrawRecordQuery;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.WithdrawRecordService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
@RestController
@Slf4j
public class JsonUserWithdrawController extends BaseController {
	@Autowired
	WithdrawRecordService withdrawRecordService;

	@Autowired
	RedisService redisService;

	@Autowired
	UserService userService;


	//提现前校验
	@PostMapping(value = "/user/withdraw/check")
	public R check(@Validated @RequestBody CheckQuery query) {
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("bindBank  ERROR! not found user ");
			return R.fail("LOCKER.10017", "没有查询到相关用户");
		}

		query.setUid(user.getUid());
		return withdrawRecordService.check(query);
	}


	@PostMapping(value = "/user/withdraw")
	public R withdraw(@Validated @RequestBody WithdrawQuery query) {
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("bindBank  ERROR! not found user ");
			return R.fail("LOCKER.10017", "没有查询到相关用户");
		}


		//限频
		Boolean getLockSuccess = redisService.setNx(CommonConstants.CACHE_WITHDRAW_USER_UID + user.getUid(), "1", 5L, false);
		if (!getLockSuccess) {
			return R.fail("PAY_TRANSFER.0007", "请求频繁,请稍后再试");
		}

		query.setUid(user.getUid());
		query.setType(user.getType());
		return withdrawRecordService.withdraw(query);
	}

	/**
	 * 用户获取提现记录
	 */
	@GetMapping("/user/bankcard/getWithdraw")
	public R queryByUid(@RequestParam(value = "size", required = false) Long size,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "status", required = false) Integer status) {

		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		Long uid = SecurityUtils.getUid();

		List<Integer> statusList=new ArrayList<>();
		if(Objects.nonNull(status)) {
			statusList.add(status);
		}

		WithdrawRecordQuery withdrawRecordQuery = WithdrawRecordQuery.builder()
				.offset(offset)
				.size(size)
				.uid(uid)
				.beginTime(beginTime)
				.endTime(endTime)
				.status(statusList).build();

		return withdrawRecordService.queryList(withdrawRecordQuery);
	}

	/**
	 * 用户获取提现审核数量
	 */
	@GetMapping("/user/bankcard/getWithdrawCount")
	public R getWithdrawCount() {
		Long uid = SecurityUtils.getUid();
		return withdrawRecordService.getWithdrawCount(uid);
	}


}
