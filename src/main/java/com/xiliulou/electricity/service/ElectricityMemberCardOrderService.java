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

    R getMemberCardOrderPage(Long uid, Long offset, Long size, Long startTime, Long endTime);

    BigDecimal homeOne(Long first, Long now,List<Integer> cardIdList);

    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay,List<Integer> cardIdList);

    R getMemberCardOrderCount(Long uid, Long startTime, Long endTime);

    ElectricityMemberCardOrder getRecentOrder(Long uid);

    R memberCardOrderPage(Long offset, Long size, MemberCardOrderQuery memberCardOrderQuery);
}
