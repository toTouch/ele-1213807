package com.xiliulou.electricity.handler.placeorder;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.handler.placeorder.context.PlaceOrderContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/24 9:47
 */
@Data
@Slf4j
public abstract class AbstractPlaceOrderHandler implements PlaceOrderHandler<PlaceOrderContext> {
    
    /**
     * 用于设置下一个节点组成执行链路
     */
    protected AbstractPlaceOrderHandler nextHandler;
    
    /**
     * 各节点业务类型
     */
    protected Integer nodePlaceOrderType;
    

    
    
    /**
     * 通用控制节点进出逻辑
     */
    public void processEntryAndExit(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        // 节点进入时，执行相关校验判断是否需要执行当前节点业务逻辑
        if (Objects.isNull(placeOrderType) || (placeOrderType & this.getNodePlaceOrderType()) != this.getNodePlaceOrderType() || !result.isSuccess() || Objects.isNull(
                context.getPlaceOrderQuery().getPayType())) {
            exit();
            return;
        }
        
        // 执行当前节点逻辑，正常退出当前节点，开始执行下一个节点的逻辑
        dealWithBusiness(context, result, placeOrderType);
    }
    
    /**
     * 通用退出当前节点逻辑
     */
    @Override
    public void fireProcess(PlaceOrderContext context, R<Object> result, Integer placeOrderType) {
        if (nextHandler != null) {
            nextHandler.processEntryAndExit(context, result, placeOrderType);
        }
    }
    
    /**
     * 通用退出执行链逻辑
     */
    @Override
    public void exit() {
        if (nextHandler != null) {
            nextHandler.exit();
        }
    }
}
