package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.EleRefundOrderHistory;
import com.xiliulou.electricity.query.EleRefundHistoryQuery;

/**
 * 退款订单表(TEleRefundOrder)表服务接口
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
public interface EleRefundOrderHistoryService {


    /**
     * 新增数据
     *
     * @param eleRefundOrderHistory 实例对象
     * @return 实例对象
     */
    EleRefundOrderHistory insert(EleRefundOrderHistory eleRefundOrderHistory);



    R queryList(EleRefundHistoryQuery eleRefundHistoryQuery);


	R queryCount(EleRefundHistoryQuery eleRefundHistoryQuery);
}
