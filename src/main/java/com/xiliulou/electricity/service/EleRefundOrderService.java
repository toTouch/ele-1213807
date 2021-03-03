package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.query.EleRefundQuery;
import org.apache.commons.lang3.tuple.Pair;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 退款订单表(TEleRefundOrder)表服务接口
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
public interface EleRefundOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleRefundOrder queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleRefundOrder queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    EleRefundOrder insert(EleRefundOrder eleRefundOrder);

    /**
     * 修改数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    Integer update(EleRefundOrder eleRefundOrder);


    //调起退款
    Pair<Boolean, Object> commonCreateRefundOrder(RefundOrder refundOrder,
                                                  ElectricityPayParams electricityPayParams,
                                                  HttpServletRequest request);


    Pair<Boolean, Object> notifyDepositRefundOrder(Map<String, String> refundMap);

    R handleRefund(String refundOrderNo,Integer status,HttpServletRequest request);

    R queryList(EleRefundQuery eleRefundQuery);

    Integer queryCountByOrderId(String orderId);
}