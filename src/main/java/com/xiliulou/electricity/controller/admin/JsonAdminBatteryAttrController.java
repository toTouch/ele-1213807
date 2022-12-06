package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAlert;
import com.xiliulou.electricity.entity.BatteryAttr;
import com.xiliulou.electricity.entity.BatteryChangeInfo;
import com.xiliulou.electricity.entity.VoltageCurrentChange;
import jodd.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
			@RequestParam(value = "gsmType", required = false) String gsmType,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "size", required = false) Long size) {

		LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
		LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
		String begin = formatter.format(beginLocalDateTime);
		String end = formatter.format(endLocalDateTime);

		if (StringUtil.isEmpty(gsmType)) {
			if (Objects.nonNull(offset) || Objects.nonNull(size)) {
				String sql = "select * from t_battery_attr where devId=? and createTime>=? AND createTime<=? order by  createTime desc  limit ?,?";
				return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn, begin, end, offset, size));
			} else {
				String sql = "select * from t_battery_attr where devId=? and createTime>=? AND createTime<=? order by  createTime desc  ";
				return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn, begin, end));
			}
		}

		if (Objects.nonNull(offset) || Objects.nonNull(size)) {
			String sql = "select * from t_battery_attr where devId=? and createTime>=? AND createTime<=? AND gsmType=? order by  createTime desc  limit ?,?";
			return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn, begin, end, offset, size));
		}

		//给加的搜索，没什么意义
		String sql = "select * from t_battery_attr where devId=? and createTime>=? AND createTime<=? AND gsmType=? order by  createTime desc";
		return R.ok(clickHouseService.query(BatteryAttr.class, sql, sn, begin, end, gsmType));
	}

	//
	@GetMapping(value = "/admin/battery/alert/list")
	public R alertList(@RequestParam("offset") Long offset,
			@RequestParam("beginTime") Long beginTime,
			@RequestParam("endTime") Long endTime,
			@RequestParam("size") Long size,
			@RequestParam("sn") String sn) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
		LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
		String begin = formatter.format(beginLocalDateTime);
		String end = formatter.format(endLocalDateTime);

		String sql = "select * from t_battery_warn where devId=? and createTime>=? AND createTime<=? order by  createTime desc limit ?,? ";
		return R.ok(clickHouseService.query(BatteryAlert.class, sql, sn, begin, end, offset, size));
	}


	/**
	 * 柜机电池变化分页列表
	 * @return
	 */
	@GetMapping(value = "/admin/battery/change/list")
	public R batteryChangeInfoPage(@RequestParam("beginTime") Long beginTime,
								   @RequestParam("endTime") Long endTime,
								   @RequestParam(value = "offset") Long offset,
								   @RequestParam(value = "size") Long size,
								   @RequestParam(value = "electricityCabinetId") String electricityCabinetId,
								   @RequestParam(value = "orderId", required = false) String orderId,
								   @RequestParam(value = "cellNo") String cellNo) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
		LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
		String begin = formatter.format(beginLocalDateTime);
		String end = formatter.format(endLocalDateTime);

		if (verifyTime(begin, end, 5)) {
			return R.failMsg("查询时间区间不能超过5天!");
		}

		if (StringUtils.isNotBlank(orderId)) {
			String sql = "select * from t_battery_change where electricityCabinetId=? and orderId =? and cellNo=? and reportTime>=? AND reportTime<=? order by  createTime desc  limit ?,?";
			return R.ok(clickHouseService.query(BatteryChangeInfo.class, sql, electricityCabinetId, orderId, cellNo, begin, end, offset, size));
		}

		String sql = "select * from t_battery_change where electricityCabinetId=? and cellNo=? and reportTime>=? AND reportTime<=? order by  createTime desc  limit ?,?";
		return R.ok(clickHouseService.query(BatteryChangeInfo.class, sql, electricityCabinetId, cellNo, begin, end, offset, size));
	}

	/**
	 * 电池电压电流、充电器电压电流变化记录
	 */
	@GetMapping(value = "/admin/voltageCurrent/change/list")
	public R batteryVoltageCurrentChangelist(@RequestParam("beginTime") Long beginTime,
								   @RequestParam("endTime") Long endTime,
//								   @RequestParam(value = "offset") Long offset,
//								   @RequestParam(value = "size") Long size,
								   @RequestParam(value = "electricityCabinetId") Long electricityCabinetId,
								   @RequestParam(value = "cellNo") Integer cellNo) {
		DecimalFormat df = new DecimalFormat("0.00");
//		if (size < 0 || size > 50) {
//			size = 10L;
//		}
//
//		if (offset < 0) {
//			offset = 0L;
//		}
		

		LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
		LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
		String begin = formatter.format(beginLocalDateTime);
		String end = formatter.format(endLocalDateTime);

		if (verifyTime(begin, end, 1)) {
			return R.failMsg("查询时间区间不能超过1天!");
		}
		
		String sql = "select electricityCabinetId,cellNo,chargeV,chargeA,batteryChargeV,batteryChargeA,sessionId,reportTime,createTime from t_voltage_current_change where electricityCabinetId=? and cellNo=? and reportTime>=? AND reportTime<=? order by  reportTime desc";
		
		List<VoltageCurrentChange> list = clickHouseService.queryList(VoltageCurrentChange.class, sql, electricityCabinetId, cellNo, begin, end);
		if (CollectionUtils.isEmpty(list)) {
			return R.ok(Collections.EMPTY_LIST);
		}
		
		List<VoltageCurrentChange> voltageCurrentChangeList = list.parallelStream().map(item -> {
			Double batteryChargeA = item.getBatteryChargeA();
			Double chargeA = item.getChargeA();
			
			item.setBatteryChargeA(ObjectUtil.isNull(batteryChargeA) ? 0 : Double.parseDouble(df.format(batteryChargeA)));
			item.setChargeA(ObjectUtil.isNull(chargeA) ? 0 : Double.parseDouble(df.format(chargeA)));
			return item;
		}).collect(Collectors.toList());
		
		return R.ok(voltageCurrentChangeList);
	}



	/**
	 * 判断间隔时间
	 * @return
	 */
	private boolean verifyTime(String begin, String end, int dayNumber) {
		if (StringUtils.isNotBlank(begin) && StringUtils.isNotBlank(end)) {
			long day = DateUtil.betweenDay(DateUtil.parse(begin), DateUtil.parse(end), true);
			return day > dayNumber;
		}
		return Boolean.FALSE;
	}

}
