package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
public class ElectricityCabinetMemberCardController {

	@Autowired
	ElectricityMemberCardService electricityMemberCardService;

	/**
	 * 月卡分页
	 *
	 * @param
	 * @return
	 */
	@GetMapping(value = "/user/memberCard/page")
	public R getElectricityBatteryPage(@RequestParam(value = "offset", required = false) Long offset,
			@RequestParam(value = "size", required = false) Long size
	) {
		if (Objects.isNull(size)) {
			size = 10L;
		}

		if (Objects.isNull(offset) || offset < 0) {
			offset = 0L;
		}
		return electricityMemberCardService.queryElectricityMemberCard(offset, size);
	}
}
