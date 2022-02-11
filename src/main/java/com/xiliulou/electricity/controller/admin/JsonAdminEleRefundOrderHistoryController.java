package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.EleRefundHistoryQuery;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.service.EleRefundOrderHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缴纳押金订单表(TEleDepositOrder)表控制层
 *
 * @author makejava
 * @since 2021-03-02 10:16:44
 */
@RestController
@Slf4j
public class JsonAdminEleRefundOrderHistoryController {
	/**
	 * 服务对象
	 */
	@Autowired
	EleRefundOrderHistoryService eleRefundOrderHistoryService;

	//退款列表
	@GetMapping("/admin/eleRefundOrderHistory/queryList")
	public R queryList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "refundOrderNo", required = false) String refundOrderNo,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		EleRefundHistoryQuery eleRefundHistoryQuery = EleRefundHistoryQuery.builder()
				.offset(offset)
				.size(size)
				.refundOrderNo(refundOrderNo)
				.beginTime(beginTime)
				.endTime(endTime)
				.tenantId(tenantId).build();

		return eleRefundOrderHistoryService.queryList(eleRefundHistoryQuery);
	}

	//退款列表总数
	@GetMapping("/admin/eleRefundOrderHistory/queryCount")
	public R queryCount(@RequestParam(value = "refundOrderNo", required = false) String refundOrderNo,
			@RequestParam(value = "beginTime", required = false) Long beginTime,
			@RequestParam(value = "endTime", required = false) Long endTime) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		EleRefundHistoryQuery eleRefundHistoryQuery = EleRefundHistoryQuery.builder()
				.refundOrderNo(refundOrderNo)
				.beginTime(beginTime)
				.endTime(endTime)
				.tenantId(tenantId).build();

		return eleRefundOrderHistoryService.queryCount(eleRefundHistoryQuery);
	}



}
