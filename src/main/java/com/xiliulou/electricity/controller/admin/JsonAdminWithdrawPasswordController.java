package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.WithdrawPassword;
import com.xiliulou.electricity.service.WithdrawPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Miss.Li
 * @Date: 2021/10/8 13:44
 * @Description:
 */
@RestController
public class JsonAdminWithdrawPasswordController {
	@Autowired
	private WithdrawPasswordService withdrawPasswordService;


	/**
	 * 编辑提现密码
	 */
	@PutMapping(value = "/admin/withdrawPassword")
	public R update(@Validated @RequestBody WithdrawPassword withdrawPassword) {
		return withdrawPasswordService.update(withdrawPassword);
	}


}
