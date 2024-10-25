package com.xiliulou.electricity.handler.placeorder;

import com.xiliulou.core.web.R;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/24 9:47
 */
public interface PlaceOrderHandler<T> {
    
    /**
     * 各级handler处理业务
     *
     * @param context 业务参数
     * @param result  响应结果
     */
    void process(T context, R<Object> result);
    
    /**
     * 退出当前handler处理，并获取下一个handler执行处理方法
     *
     * @param context 业务参数
     * @param result  响应结果
     */
    void fireProcess(T context, R<Object> result);
}
