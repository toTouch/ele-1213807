package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.PlaceOrderConstant;
import com.xiliulou.electricity.query.PlaceOrderQuery;
import com.xiliulou.electricity.service.PlaceOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/29 14:38
 */
@RestController
@RequiredArgsConstructor
public class JsonAdminPlaceOrderController {
    
    private final PlaceOrderService placeOrderService;
    
    /**
     * 押金、套餐、保险购买下单支付接口
     */
    @PostMapping("admin/place/order")
    public R<Object> placeOrder(@RequestBody PlaceOrderQuery query, HttpServletRequest request) {
        // 设置支付类型
        query.setPayType(PlaceOrderConstant.OFFLINE_PAYMENT);
        
        return placeOrderService.placeOrder(query, request);
    }
}
