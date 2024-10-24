package com.xiliulou.electricity.service.placeorder.chain;

import com.xiliulou.electricity.context.placeorder.PlaceOrderDepositAndMemberCardContext;
import com.xiliulou.electricity.service.placeorder.handler.PlaceOrderHandler;
import org.springframework.stereotype.Service;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/24 9:49
 */
@Service
public class PlaceOrderDepositAndMemberCardChain {
    
    private PlaceOrderHandler<PlaceOrderDepositAndMemberCardContext> head;
}
