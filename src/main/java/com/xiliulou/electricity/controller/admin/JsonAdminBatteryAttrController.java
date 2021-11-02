package com.xiliulou.electricity.controller.admin;

import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: Miss.Li
 * @Date: 2021/11/2 10:01
 * @Description:
 */
public class JsonAdminBatteryAttrController {

	@Autowired
	ClickHouseService clickHouseService;

	//
	@GetMapping(value = "/admin/batteryAttr/list")
	public R list(@RequestParam("size") Long offset,
			@RequestParam("size") Long size,
			@RequestParam("sn") String sn) {

		String sql = "select * from t_battery_attr where devId=? limit ?,?";
		return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn, offset, size));
	}
}
