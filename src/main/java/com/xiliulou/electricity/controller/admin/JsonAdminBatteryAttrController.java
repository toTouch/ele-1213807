package com.xiliulou.electricity.controller.admin;

import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAlert;
import com.xiliulou.electricity.entity.BatteryAttr;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/11/2 10:01
 * @Description:
 */
@RestController
public class JsonAdminBatteryAttrController {

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	ClickHouseService clickHouseService;



	//
	@GetMapping(value = "/admin/battery/attr/list")
	public R attrList(@RequestParam("sn") String sn,
			@RequestParam("beginTime") Long beginTime,
			@RequestParam("endTime") Long endTime,
			@RequestParam(value = "gsmType", required = false) String gsmType) {

		LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
		LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
		String begin = formatter.format(beginLocalDateTime);
		String end = formatter.format(endLocalDateTime);


		if(StringUtil.isEmpty(gsmType)){
			String sql = "select * from t_battery_attr where devId=? and createTime>=? AND createTime<=?";
			return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn,begin,end));
		}


		//给加的搜索，没什么意义
		String sql = "select * from t_battery_attr where devId=? and createTime>=? AND createTime<=? AND gsmType=?";
		return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn,begin,end,gsmType));
	}


	//
	@GetMapping(value = "/admin/battery/alert/list")
	public R alertList(@RequestParam("offset") Long offset,
			@RequestParam("size") Long size,
			@RequestParam("sn") String sn) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		String sql = "select * from t_battery_warn where devId=?  limit ?,?";
		return R.ok(clickHouseService.query(BatteryAlert.class, sql, sn,  offset, size));
	}

}
