package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.query.MemberCardOrderQuery;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface ElectricityMemberCardOrderService {


    R createOrder(Long uid, Integer memberId, HttpServletRequest request);
    R getMemberCardOrderPage(Long uid, Long offset, Long size);

    BigDecimal homeOne(Long first, Long now);

    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay);

    R getMemberCardOrderCount(Long uid);

    ElectricityMemberCardOrder getRecentOrder(Long uid);

    R memberCardOrderPage(Long offset, Long size, MemberCardOrderQuery memberCardOrderQuery);
}
