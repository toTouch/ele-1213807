package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.UserTypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;

/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
@Slf4j
public class JsonAdminElectricityCabinetOrderOperHistoryController {
	/**
	 * 服务对象
	 */
	@Autowired
	ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
	@Autowired
	UserTypeFactory userTypeFactory;

	//换电柜历史记录查询
	@GetMapping("/admin/electricityCabinetOrderOperHistory/list")
	public R queryList(@RequestParam(value = "size", required = false) Long size,
			@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam("orderId") String orderId,
			@RequestParam("type") Integer type) {

		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery = ElectricityCabinetOrderOperHistoryQuery.builder()
				.offset(offset)
				.size(size)
				.orderId(orderId)
				.type(type).build();
		return electricityCabinetOrderOperHistoryService.queryListByOrderId(electricityCabinetOrderOperHistoryQuery);
	}

	//换电柜历史记录查询
	@GetMapping("/admin/electricityCabinetOrderOperHistory/queryCount")
	public R queryCount(@RequestParam("orderId") String orderId,
			@RequestParam("type") Integer type) {


		ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery = ElectricityCabinetOrderOperHistoryQuery.builder()
				.orderId(orderId)
				.type(type).build();
		return electricityCabinetOrderOperHistoryService.queryCountByOrderId(electricityCabinetOrderOperHistoryQuery);
	}

}
