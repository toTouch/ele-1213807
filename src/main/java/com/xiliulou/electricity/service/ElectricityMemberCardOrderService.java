package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.query.MemberCardOrderQuery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface ElectricityMemberCardOrderService {


    R createOrder(Integer memberId,String productKey, String deviceName, HttpServletRequest request);

    R queryUserList(Long offset, Long size, Long startTime, Long endTime);

    BigDecimal homeOne(Long first, Long now,List<Integer> cardIdList,Integer tenantId);

    List<HashMap<String, String>> homeTwo(long startTimeMilliDay, Long endTimeMilliDay,List<Integer> cardIdList,Integer tenantId);

    R getMemberCardOrderCount(Long uid, Long startTime, Long endTime);


    R queryList(Long offset, Long size, MemberCardOrderQuery memberCardOrderQuery);

	void exportExcel(MemberCardOrderQuery memberCardOrderQuery, HttpServletResponse response);
}
