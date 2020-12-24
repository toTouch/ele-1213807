package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public R getElectricityBatteryPage(@RequestParam(value = "offset", required = true) Long offset,
                                       @RequestParam(value = "size", required = true) Long size
    ) {
        return electricityMemberCardService.getElectricityMemberCardPage(offset, size, null, ElectricityMemberCard.STATUS_USEABLE,null);
    }
}
