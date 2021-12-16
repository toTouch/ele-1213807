package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BankNoConstants;
import com.xiliulou.electricity.constant.CommonConstants;
import com.xiliulou.electricity.entity.BankCard;
import com.xiliulou.electricity.query.BankCardQuery;
import com.xiliulou.electricity.service.BankCardService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author Hardy
 * @email ${email}
 * @date 2021-05-24 13:58:23
 */
@RestController
@Slf4j
public class JsonAdminBankCardController {
	@Autowired
	private BankCardService bankCardService;
	@Autowired
	RedisService redisService;

	/**
	 * 用户获取自己的银行卡
	 */
	@GetMapping("/user/bankcard/getCard")
	public R queryByUid() {
		Long uid = SecurityUtils.getUid();

		BankCardQuery bankCardQuery = BankCardQuery.builder()
				.uid(uid).build();
		return bankCardService.queryList(bankCardQuery);
	}

	/**
	 * 绑卡
	 */
	@PostMapping("/user/bankcard/bind")
	public R bindBank(@Validated @RequestBody BankCard bankCard) {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			log.error("bindBank  ERROR! not found user ");
			return R.fail("LOCKER.10017", "没有查询到相关用户");
		}

		if (ObjectUtil.isEmpty(BankNoConstants.BankNoMap.get(bankCard.getEncBankCode()))) {
			return R.fail("PAY_TRANSFER.0006", "不支持此银行卡");
		}
		Boolean getLockSuccess = redisService.setNx(CommonConstants.BIND_BANK_OPER_USER_LOCK + SecurityUtils.getUid(), IdUtil.simpleUUID(), 10L, false);
		if (!getLockSuccess) {
			return R.fail("PAY_TRANSFER.0007", "请求频繁,请稍后再试");
		}


		bankCard.setUid(uid);
		R result=bankCardService.bindBank(bankCard);

		redisService.delete(CommonConstants.BIND_BANK_OPER_USER_LOCK + SecurityUtils.getUid());
		return  result;
	}

	/**
	 * 解绑
	 */
	@DeleteMapping("/user/bankcard/unBind")
	public R unBindBank(@RequestParam("id") Integer id) {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			log.error("bindBank  ERROR! not found user ");
			return R.fail("LOCKER.10017", "没有查询到相关用户");
		}

		Boolean getLockSuccess = redisService.setNx(CommonConstants.BIND_BANK_OPER_USER_LOCK + SecurityUtils.getUid(), IdUtil.simpleUUID(), 10L,false);
		if (!getLockSuccess) {
			return R.fail("PAY_TRANSFER.0007", "请求频繁,请稍后再试");
		}


		R result=bankCardService.unBindBank(id);

		redisService.delete(CommonConstants.BIND_BANK_OPER_USER_LOCK + SecurityUtils.getUid());
		return result;
	}


}
