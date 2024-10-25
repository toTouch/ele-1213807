package com.xiliulou.electricity.handler.placeorder.chain;

import com.xiliulou.electricity.context.placeorder.PlaceOrderContext;
import com.xiliulou.electricity.handler.placeorder.PlaceOrderHandler;

import java.util.HashMap;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/24 13:45
 */
public class PlaceOrderChainManager {
    
    private final HashMap<Integer, PlaceOrderHandler<PlaceOrderContext>> FIRST_NODE_OF_DIFFERENT_SERVICES = new HashMap<>();
    
    private PlaceOrderHandler<PlaceOrderContext> firstNode;
    
    public PlaceOrderChainManager() {
    
    }
}
