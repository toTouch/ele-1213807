package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FreeServiceFeeOrder;

/**
 * @Description: FreeServiceFeeOrderService
 * @Author: RenHang
 * @Date: 2025/03/27
 */
public interface FreeServiceFeeOrderService {

    /**
     * existsPaySuccessOrder
     *
     * @param freeDepositOrderId freeDepositOrderId
     * @param uid uid
     * @return: @return {@link Integer }
     */

    Integer existsPaySuccessOrder(String freeDepositOrderId, Long uid);

    /**
     * insertOrder
     *
     * @param freeServiceFeeOrder freeServiceFeeOrder
     * @return:
     */

    void insertOrder(FreeServiceFeeOrder freeServiceFeeOrder);

}
