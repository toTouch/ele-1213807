package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 18:19
 **/
@RestController
@Slf4j
public class JsonAdminElectricityMemberCardOrderController {
	@Autowired
	ElectricityMemberCardOrderService electricityMemberCardOrderService;

	/**
	 * 分页
	 *
	 * @return
	 */
	@GetMapping("admin/electricityMemberCardOrder/page")
	public R getElectricityMemberCardPage(@RequestParam(value = "offset") Long offset,
			@RequestParam(value = "size") Long size,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "memberCardType", required = false) Integer cardType,
			@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
			@RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {

		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
				.phone(phone)
				.orderId(orderId)
				.cardType(cardType)
				.queryStartTime(queryStartTime)
				.queryEndTime(queryEndTime)
				.offset(offset)
				.size(size)
				.tenantId(tenantId).build();

		return electricityMemberCardOrderService.queryList(memberCardOrderQuery);
	}

	/**
	 * 分页
	 *
	 * @return
	 */
	@GetMapping("admin/electricityMemberCardOrder/queryCount")
	public R queryCount(@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "memberCardType", required = false) Integer cardType,
			@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
			@RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {


		MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
				.phone(phone)
				.orderId(orderId)
				.cardType(cardType)
				.queryStartTime(queryStartTime)
				.queryEndTime(queryEndTime).build();

		return electricityMemberCardOrderService.queryCount(memberCardOrderQuery);
	}

	//换电柜购卡订单导出报表
	@GetMapping("/admin/electricityMemberCardOrder/exportExcel")
	public void exportExcel(@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "memberCardType", required = false) Integer cardType,
			@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
			@RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
			HttpServletResponse response) {

		Double days = (Double.valueOf(queryEndTime - queryStartTime)) / 1000 / 3600 / 24;
		if (days > 33) {
			throw new CustomBusinessException("搜索日期不能大于33天");
		}
		MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
				.phone(phone)
				.orderId(orderId)
				.cardType(cardType)
				.queryStartTime(queryStartTime)
				.queryEndTime(queryEndTime).build();
		electricityMemberCardOrderService.exportExcel(memberCardOrderQuery,response);
	}

}
