package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryAlert;
import com.xiliulou.electricity.entity.BatteryAttr;
import com.xiliulou.electricity.entity.BatteryChangeInfo;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author: Miss.HRP
 * @Date: 2022/07/12 10:01
 * @Description:
 */
@RestController
public class JsonAdminEleUserOperateRecordController {

	@Autowired
	EleUserOperateRecordService eleUserOperateRecordService;

	/**
	 * 用户操作记录  3.0已作废
	 * @param uid
	 * @param size
	 * @param offset
	 * @param beginTime
	 * @param endTime
	 * @param operateModel
	 * @return
	 */
	@GetMapping(value = "/admin/eleUserOperateRecord/list")
	@Deprecated
	public R queryList(@RequestParam(value = "uid") Long uid,
					   @RequestParam("size") Long size,
					   @RequestParam("offset") Long offset,
					   @RequestParam(value = "beginTime", required = false) Long beginTime,
					   @RequestParam(value = "endTime", required = false) Long endTime,
					   @RequestParam(value = "operateModel",required = false) Integer operateModel) {
		return eleUserOperateRecordService.queryList(uid,size,offset,beginTime,endTime,operateModel);
	}

	/**
	 * 用户操作记录  3.0已作废
	 * @param uid
	 * @param beginTime
	 * @param endTime
	 * @param operateModel
	 * @return
	 */
	@GetMapping(value = "/admin/eleUserOperateRecord/queryCount")
	@Deprecated
	public R queryCount(@RequestParam(value = "uid") Long uid,
						@RequestParam(value = "beginTime", required = false) Long beginTime,
						@RequestParam(value = "endTime", required = false) Long endTime,
						@RequestParam(value = "operateModel",required = false) Integer operateModel) {
		return eleUserOperateRecordService.queryCount(uid,beginTime,endTime,operateModel);
	}



}
