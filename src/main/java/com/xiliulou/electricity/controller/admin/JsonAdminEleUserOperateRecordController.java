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

	@GetMapping(value = "/admin/eleUserOperateRecord/list")
	public R queryList(@RequestParam(value = "uid") Long uid,
					   @RequestParam("size") Long size,
					   @RequestParam("offset") Long offset) {

		System.out.println("用户uid======================="+uid);
		System.out.println("size==================="+size);
		System.out.println("offset===================="+offset);

		return eleUserOperateRecordService.queryList(uid,size,offset);
	}

	@GetMapping(value = "/admin/eleUserOperateRecord/queryCount")
	public R queryCount(@RequestParam(value = "uid") Long uid) {
		return eleUserOperateRecordService.queryCount(uid);
	}



}
