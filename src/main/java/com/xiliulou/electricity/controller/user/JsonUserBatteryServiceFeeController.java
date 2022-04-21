package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.UserAmountService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 电池服务费控制层
 *
 * @author makejava
 * @since 2022-04-21 09:44:36
 */
@RestController
@Slf4j
public class JsonUserBatteryServiceFeeController {


	@Autowired
	ElectricityBatteryService electricityBatteryService;
	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;


	@GetMapping("/user/batteryServiceFee/query")
	public R queryBatteryServiceFee(){
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户!");
		}
		return R.ok(franchiseeUserInfoService.queryUserBatteryServiceFee(uid));
	}

}
