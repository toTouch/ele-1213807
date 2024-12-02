package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.PlaceOrderQuery;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/29 13:41
 */
public interface PlaceOrderService {
    
    /**
     * 押金、套餐、保险购买下单支付接口
     *
     * @param query   购买请求参数
     * @param request 请求对象
     * @return 调起支付结果
     */
    R<Object> placeOrder(PlaceOrderQuery query, HttpServletRequest request);
}
