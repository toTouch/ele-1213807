package com.xiliulou.electricity.service.placeorder.handler;

import com.xiliulou.core.web.R;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/24 9:47
 */
public abstract class AbstractPlaceOrderHandler<T> implements PlaceOrderHandler<T> {
    
    private PlaceOrderHandler<T> nextHandler;
    
    @Override
    public void fireProcess(T context, R<Object> result) {
        if (nextHandler != null) {
            nextHandler.process(context, result);
        }
    }
}
