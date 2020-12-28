package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 18:19
 **/
@RestController
@Slf4j
public class ElectricityMemberCardOrderAdminController {
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

        MemberCardOrderQuery memberCardOrderQuery = new MemberCardOrderQuery();
        memberCardOrderQuery.setPhone(phone);
        memberCardOrderQuery.setOrderId(orderId);
        memberCardOrderQuery.setCardType(cardType);
        memberCardOrderQuery.setQueryStartTime(queryStartTime);

        memberCardOrderQuery.setQueryEndTime(queryEndTime);
        return electricityMemberCardOrderService.memberCardOrderPage(offset, size, memberCardOrderQuery);
    }

}
