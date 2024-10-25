package com.xiliulou.electricity.handler.placeorder;

import com.xiliulou.core.web.R;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/24 9:47
 */
public interface PlaceOrderHandler<T> {
    
    /**
     * 管理各节点进入与退出
     *
     * @param context 业务参数
     * @param result  响应结果
     * @param placeOrderType 购买下单业务类型
     */
    void processEntryAndExit(T context, R<Object> result, Integer placeOrderType);
    
    /**
     * 处理各节点的业务逻辑
     *
     * @param context 业务参数
     * @param result  响应结果
     * @param placeOrderType 购买下单业务类型
     */
    void dealWithBusiness(T context, R<Object> result, Integer placeOrderType);
    
    /**
     * 退出当前handler处理，并获取下一个handler执行处理方法
     *
     * @param context 业务参数
     * @param result  响应结果
     * @param placeOrderType 购买下单业务类型
     */
    void fireProcess(T context, R<Object> result, Integer placeOrderType);
    
    /**
     * 退出，不执行本节点逻辑
     */
    void exit();
}
