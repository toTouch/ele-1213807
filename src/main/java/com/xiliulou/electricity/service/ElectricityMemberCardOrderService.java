package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

public interface ElectricityMemberCardOrderService {


    R createOrder(Long uid, Integer memberId, HttpServletRequest request);

    BigDecimal homeOne(Long first, Long now);
}
