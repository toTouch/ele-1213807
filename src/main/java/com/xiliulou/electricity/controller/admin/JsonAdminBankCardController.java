package com.xiliulou.electricity.controller.admin;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.annotation.Log;
import com.xiliulou.electricity.query.BankCardQuery;
import com.xiliulou.electricity.service.BankCardService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.security.authentication.authorization.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
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
	@Autowired
	AuthorizationService authorizationService;

	/**
	 * 后台查询银行卡列表
	 */
	@GetMapping("/admin/bankcard/queryList")
	public R queryList(@RequestParam(value = "size", required = false) Long size,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "uid", required = false) Long uid,
			@RequestParam(value = "encBindUserName", required = false) String encBindUserName) {
		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		BankCardQuery bankCardQuery = BankCardQuery.builder()
				.offset(offset)
				.size(size)
				.uid(uid)
				.tenantId(tenantId)
				.encBindUserName(encBindUserName).build();

		return bankCardService.queryList(bankCardQuery);
	}


	/**
	 * 后台查询银行卡列表
	 */
	@GetMapping("/admin/bankcard/queryCount")
	public R queryCount(@RequestParam(value = "uid", required = false) Long uid,
			@RequestParam(value = "encBindUserName", required = false) String encBindUserName) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		BankCardQuery bankCardQuery = BankCardQuery.builder()
				.uid(uid)
				.tenantId(tenantId)
				.encBindUserName(encBindUserName).build();

		return bankCardService.queryCount(bankCardQuery);
	}


	/**
	 * 后台解绑卡
	 */
	@DeleteMapping("/admin/bankcard/unBindByWeb")
	@Log(title = "后台解绑银行卡")
	public R unBindByWeb(@RequestParam("id") Integer id) {
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();
		return bankCardService.unBindByWeb(id,tenantId);
	}

	/**
	 *
	 */
	@GetMapping("/admin/bankcard/abTest")
	public R abTest(@RequestParam("uid") Long uid,@RequestParam("userType") int userType){
		long beginTime = System.currentTimeMillis();
		Collection<? extends GrantedAuthority> authorities = authorizationService.acquireAllAuthorities(uid, userType);
		long endTime=System.currentTimeMillis();
		return R.ok(endTime-beginTime);
	}
}
