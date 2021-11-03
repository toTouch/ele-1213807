package com.xiliulou.electricity.controller.admin;

import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAlert;
import com.xiliulou.electricity.entity.BatteryAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Miss.Li
 * @Date: 2021/11/2 10:01
 * @Description:
 */
@RestController
public class JsonAdminBatteryAttrController {

	@Autowired
	ClickHouseService clickHouseService;

	//
	@GetMapping(value = "/admin/battery/attr/list")
	public R attrList(@RequestParam("size") Long offset,
			@RequestParam("size") Long size,
			@RequestParam("sn") String sn,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {

		String sql = "select * from t_battery_attr where devId=? and creatTime>=? and creatTime<=? limit ?,?";
		return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn, beginTime, endTime, offset, size));
	}

	//
	@GetMapping(value = "/admin/battery/alert/list")
	public R alertList(@RequestParam("size") Long offset,
			@RequestParam("size") Long size,
			@RequestParam("sn") String sn,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {

		String sql = "select * from t_battery_warn where devId=? and creatTime>=? and creatTime<=? limit ?,?";
		return R.ok(clickHouseService.query(BatteryAlert.class, sql, sn, beginTime, endTime, offset, size));
	}
}
