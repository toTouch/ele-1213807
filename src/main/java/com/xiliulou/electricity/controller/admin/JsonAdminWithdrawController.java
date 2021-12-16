package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CommonConstants;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.WithdrawRecord;
import com.xiliulou.electricity.query.CheckQuery;
import com.xiliulou.electricity.query.HandleWithdrawQuery;
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

import java.math.BigDecimal;
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
public class JsonAdminWithdrawController extends BaseController {
	@Autowired
	WithdrawRecordService withdrawRecordService;

	@Autowired
	RedisService redisService;

	@Autowired
	UserService userService;





	@PostMapping(value = "/admin/handleWithdraw")
	public R withdraw(@Validated @RequestBody HandleWithdrawQuery handleWithdrawQuery) {

		return withdrawRecordService.handleWithdraw(handleWithdrawQuery);
	}

	@GetMapping(value = "/admin/withdraw/list")
	public R queryList(@RequestParam(value = "size", required = false) Long size,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "uid", required = false) Long uid,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "status", required = false) Integer status) {
		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		List<Integer> statusList=new ArrayList<>();
		if(Objects.equals(status,-1)){
			statusList.add(WithdrawRecord.CHECK_PASS);
			statusList.add(WithdrawRecord.WITHDRAWING);
		}else {
			if(Objects.nonNull(status)) {
				statusList.add(status);
			}
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

	@GetMapping(value = "/admin/withdraw/queryCount")
	public R queryCount(@RequestParam(value = "uid", required = false) Long uid,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime,
			@RequestParam(value = "status", required = false) Integer status) {


		List<Integer> statusList=new ArrayList<>();
		if(Objects.equals(status,-1)){
			statusList.add(WithdrawRecord.CHECK_PASS);
			statusList.add(WithdrawRecord.WITHDRAWING);
		}else {
			if(Objects.nonNull(status)) {
				statusList.add(status);
			}
		}

		WithdrawRecordQuery withdrawRecordQuery = WithdrawRecordQuery.builder()
				.uid(uid)
				.beginTime(beginTime)
				.endTime(endTime)
				.status(statusList).build();

		return withdrawRecordService.queryCount(withdrawRecordQuery);
	}

}
