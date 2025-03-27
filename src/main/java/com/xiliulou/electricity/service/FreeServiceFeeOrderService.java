package com.xiliulou.electricity.service;

import com.xiliulou.electricity.dto.IsSupportFreeServiceFeeDTO;
import com.xiliulou.electricity.entity.FreeServiceFeeOrder;
import com.xiliulou.electricity.entity.UserInfo;

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
     * @param uid                uid
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


    /**
     * 是否支持免押服务费
     *
     * @param userInfo userInfo
     * @param depositOrderId depositOrderId
     * @return: @return {@link IsSupportFreeServiceFeeDTO }
     */

    IsSupportFreeServiceFeeDTO isSupportFreeServiceFee(UserInfo userInfo, String depositOrderId);
}
