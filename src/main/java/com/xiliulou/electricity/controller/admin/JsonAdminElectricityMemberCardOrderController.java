package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.MemberCardOrderAddAndUpdate;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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
	@Autowired
	FranchiseeService franchiseeService;

	/**
	 * 分页
	 *
	 * @return
	 */
	@GetMapping("admin/electricityMemberCardOrder/page")
	public R getElectricityMemberCardPage(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
		    @RequestParam(value = "franchiseeName", required = false) String franchiseeName,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "memberCardType", required = false) Integer cardType,
			@RequestParam(value = "status", required = false) Integer status,
			@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
			@RequestParam(value = "queryEndTime", required = false) Long queryEndTime) {

		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		Long franchiseeId = null;
		if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
				&& !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
			//加盟商
			Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
			if (Objects.isNull(franchisee)) {
				return R.ok(new ArrayList<>());
			}
			franchiseeId=franchisee.getId();
		}

		MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
				.phone(phone)
				.orderId(orderId)
				.cardType(cardType)
				.queryStartTime(queryStartTime)
				.queryEndTime(queryEndTime)
				.offset(offset)
				.size(size)
				.tenantId(tenantId)
				.status(status)
				.franchiseeName(franchiseeName)
				.franchiseeId(franchiseeId).build();

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
			@RequestParam(value = "status", required = false) Integer status,
			@RequestParam(value = "queryStartTime", required = false) Long queryStartTime,
			@RequestParam(value = "queryEndTime", required = false) Long queryEndTime,
			@RequestParam(value = "franchiseeName", required = false) String franchiseeName) {

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		Long franchiseeId=null;
		if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
				&& !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
			//加盟商
			Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
			if (Objects.isNull(franchisee)) {
				return R.ok(0);
			}
			franchiseeId=franchisee.getId();
		}

		MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
				.phone(phone)
				.orderId(orderId)
				.cardType(cardType)
				.queryStartTime(queryStartTime)
				.queryEndTime(queryEndTime)
				.tenantId(tenantId)
				.status(status)
				.franchiseeName(franchiseeName)
				.franchiseeId(franchiseeId).build();

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

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			throw new CustomBusinessException("查不到订单");
		}

		Long franchiseeId=null;
		if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
				&& !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
			//加盟商
			Franchisee franchisee = franchiseeService.queryByUid(user.getUid());
			if (Objects.isNull(franchisee)) {
				throw new CustomBusinessException("查不到订单");
			}
			franchiseeId=franchisee.getId();
		}

		MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder()
				.phone(phone)
				.orderId(orderId)
				.cardType(cardType)
				.queryStartTime(queryStartTime)
				.queryEndTime(queryEndTime)
				.tenantId(tenantId)
				.franchiseeId(franchiseeId).build();
		electricityMemberCardOrderService.exportExcel(memberCardOrderQuery,response);
	}

	/**
	 * 新增用户套餐
	 * @return
	 */
	@PostMapping(value = "/admin/electricityMemberCard/addUserMemberCard")
	public R addUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate){
		return electricityMemberCardOrderService.addUserMemberCard(memberCardOrderAddAndUpdate);
	}

	/**
	 * 编辑用户套餐
	 * @return
	 */
	@PutMapping(value = "/admin/electricityMemberCard/editUserMemberCard")
	public R editUserMemberCard(MemberCardOrderAddAndUpdate memberCardOrderAddAndUpdate){
		return electricityMemberCardOrderService.editUserMemberCard(memberCardOrderAddAndUpdate);
	}

}
