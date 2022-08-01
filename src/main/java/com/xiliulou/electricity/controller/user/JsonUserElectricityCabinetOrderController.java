package com.xiliulou.electricity.controller.user;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 订单表(TElectricityCabinetOrder)表控制层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@RestController
@Slf4j
public class JsonUserElectricityCabinetOrderController extends BaseController {
	/**
	 * 服务对象
	 */
	@Autowired
	ElectricityCabinetOrderService electricityCabinetOrderService;

	//换电柜下单
	@Deprecated
	@PostMapping("/user/electricityCabinetOrder/order")
	public R order(@RequestBody @Validated(value = CreateGroup.class) OrderQuery orderQuery) {
		return electricityCabinetOrderService.order(orderQuery);
	}

	/**
	 * 换电柜下单，用新的命令
	 * @param orderQuery
	 * @return
	 */
	@PostMapping("/user/electricityCabinetOrder/order/v2")
	public R orderV2(@RequestBody @Validated OrderQueryV2 orderQuery) {
		return returnTripleResult(electricityCabinetOrderService.orderV2(orderQuery));
	}

	//换电柜再次开门
	@PostMapping("/user/electricityCabinetOrder/openDoor")
	public R openDoor(@RequestBody OpenDoorQuery openDoorQuery) {
		return electricityCabinetOrderService.openDoor(openDoorQuery);
	}

	//换电柜订单查询
	@GetMapping("/user/electricityCabinetOrder/list")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
				.offset(offset)
				.size(size)
				.beginTime(beginTime)
				.endTime(endTime)
				.uid(user.getUid())
				.tenantId(tenantId).build();
		return electricityCabinetOrderService.queryList(electricityCabinetOrderQuery);
	}

	//换电柜订单量
	@GetMapping("/user/electricityCabinetOrder/count")
	public R queryCount(@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {

		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder()
				.beginTime(beginTime)
				.endTime(endTime)
				.uid(user.getUid())
				.tenantId(tenantId).build();
		return electricityCabinetOrderService.queryCount(electricityCabinetOrderQuery);
	}

	//查订单状态（新）
	@GetMapping("/user/electricityCabinetOrder/queryNewStatus")
	public R queryNewStatus(@RequestParam("orderId") String orderId) {
		return electricityCabinetOrderService.queryNewStatus(orderId);
	}


	//换电柜自助开仓
	@PostMapping("/user/electricityCabinetOrder/orderSelfOpenCell")
	public R orderSelfOpenCellQuery(@RequestBody @Validated(value = CreateGroup.class) OrderSelfOpenCellQuery orderSelfOpenCellQuery) {
		return electricityCabinetOrderService.selfOpenCell(orderSelfOpenCellQuery);
	}

	//查看开门结果
	@GetMapping("/user/electricityCabinet/open/check")
	public R checkOpenSession(@RequestParam("sessionId") String sessionId) {
		if (StrUtil.isEmpty(sessionId)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}
		return electricityCabinetOrderService.checkOpenSessionId(sessionId);
	}

}
