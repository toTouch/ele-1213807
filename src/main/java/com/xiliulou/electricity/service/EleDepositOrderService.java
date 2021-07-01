package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务接口
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleDepositOrderService {


    EleDepositOrder queryByOrderId(String orderNo);

    R payDeposit(String productKey,String deviceName,HttpServletRequest request);

    R returnDeposit(HttpServletRequest request);

    R queryList(EleDepositOrderQuery eleDepositOrderQuery);

    void update(EleDepositOrder eleDepositOrderUpdate);

    R queryUserDeposit();

	void exportExcel(EleDepositOrderQuery eleDepositOrderQuery, HttpServletResponse response);

    R queryDeposit(String productKey,String deviceName);
}
