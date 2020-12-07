package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;

import javax.servlet.http.HttpServletRequest;

public interface ElectricityMemberCardOrderService {


    R createOrder(Long uid, Integer memberId, HttpServletRequest request);
}
