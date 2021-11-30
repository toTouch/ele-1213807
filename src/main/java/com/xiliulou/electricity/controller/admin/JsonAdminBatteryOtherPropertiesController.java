package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.service.BatteryOtherPropertiesService;
import com.xiliulou.electricity.validator.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Miss.Li
 * @Date: 2021/10/22 09:22
 * @Description:
 */
@RestController
public class JsonAdminBatteryOtherPropertiesController {

	@Autowired
	BatteryOtherPropertiesService batteryOtherPropertiesService;


	//查询电池详情
	@GetMapping(value = "/admin/batteryOtherProperties/queryBySn")
	public R queryBySn(@RequestParam("sn") String sn) {
		return batteryOtherPropertiesService.queryBySn(sn);
	}
}
