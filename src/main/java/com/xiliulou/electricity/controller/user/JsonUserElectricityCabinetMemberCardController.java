package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 09:41
 **/
@RestController
@Slf4j
public class JsonUserElectricityCabinetMemberCardController {

	@Autowired
	ElectricityMemberCardService electricityMemberCardService;

	/**
	 * 月卡分页
	 *
	 * @param
	 * @return
	 */
	@GetMapping(value = "/user/memberCard/list")
	public R queryUserList(@RequestParam("size") Long size,
			@RequestParam("offset") Long offset,
			@RequestParam(value = "franchiseeId", required = false) Long franchiseeId,
			@RequestParam(value = "productKey", required = false) String productKey,
			@RequestParam(value = "deviceName", required = false) String deviceName) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}
		return electricityMemberCardService.queryUserList(offset, size,productKey,deviceName,franchiseeId);
	}


	/**
	 * 月卡详情
	 *
	 * @param
	 * @return
	 */
	@GetMapping(value = "/user/memberCard/{id}")
	public R queryUserList(@PathVariable("id") Integer id) {
		return R.ok(electricityMemberCardService.queryByStatus(id));
	}


	/**
	 * 租车月卡分页
	 *
	 * @param
	 * @return
	 */
	@GetMapping(value = "/user/rentCarMemberCard/list")
	public R queryRentCarMemberCardList(@RequestParam("size") Long size,
						   @RequestParam("offset") Long offset) {
		if (size < 0 || size > 50) {
			size = 10L;
		}

		if (offset < 0) {
			offset = 0L;
		}
		return electricityMemberCardService.queryRentCarMemberCardList(offset, size);
	}
}
