package com.xiliulou.electricity.service;

/**
 * @ClassName: ExchangeExceptionHandlerService
 * @description:
 * @author: renhang
 * @create: 2024-09-29 15:12
 */
public interface ExchangeExceptionHandlerService {
    
    /**
     * 保存换电异常柜机格挡号
     *
     * @param orderStatus 订单状态
     * @param eid 柜机id
     * @param oldCell 旧仓门
     * @param newCell 新仓门
     */
    void saveExchangeExceptionCell(String orderStatus, Integer eid, Integer oldCell, Integer newCell);
    
    /**
     * 保存租借异常柜机格挡号
     *
     * @param orderStatus 订单状态
     * @param eid 柜机id
     * @param cellNo 仓门
     */
    void saveRentReturnExceptionCell(String orderStatus, Integer eid, Integer cellNo);
}
