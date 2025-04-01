package com.xiliulou.electricity.service;

import com.xiliulou.electricity.dto.CreateFreeServiceFeeOrderDTO;
import com.xiliulou.electricity.dto.IsSupportFreeServiceFeeDTO;
import com.xiliulou.electricity.entity.FreeServiceFeeOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.FreeServiceFeePageQuery;
import com.xiliulou.electricity.vo.FreeServiceFeeOrderPageVO;
import com.xiliulou.electricity.vo.UserFreeServiceFeeStatusVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @Description: FreeServiceFeeOrderService
 * @Author: RenHang
 * @Date: 2025/03/27
 */
public interface FreeServiceFeeOrderService {


    /**
     * update
     *
     * @param freeServiceFeeOrder freeServiceFeeOrder
     * @return:
     */

    void update(FreeServiceFeeOrder freeServiceFeeOrder);

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
     * @param userInfo       userInfo
     * @param depositOrderId depositOrderId
     * @return: @return {@link IsSupportFreeServiceFeeDTO }
     */

    IsSupportFreeServiceFeeDTO isSupportFreeServiceFee(UserInfo userInfo, String depositOrderId);


    /**
     * 车是否支持免押服务费
     *
     * @param userInfo       userInfo
     * @param depositOrderId depositOrderId
     * @return: @return {@link IsSupportFreeServiceFeeDTO }
     */

    IsSupportFreeServiceFeeDTO isSupportFreeServiceFeeCar(UserInfo userInfo, String depositOrderId);


    /**
     * 生成免押服务费订单
     *
     * @param dto dto
     * @return: @return {@link FreeServiceFeeOrder }
     */

    FreeServiceFeeOrder createFreeServiceFeeOrder(CreateFreeServiceFeeOrderDTO dto);

    /**
     * pageList
     *
     * @param query query
     * @return: @return {@link FreeServiceFeeOrderPageVO }
     */

    List<FreeServiceFeeOrderPageVO> pageList(FreeServiceFeePageQuery query);

    /**
     * count
     *
     * @param query query
     * @return: @return {@link Long }
     */

    Long count(FreeServiceFeePageQuery query);


    /**
     * 根据订单id查询订单
     *
     * @param orderId orderId
     * @return: @return {@link FreeServiceFeeOrder }
     */

    FreeServiceFeeOrder queryByOrderId(String orderId);


    /**
     * 回调处理订单状态
     *
     * @param orderId orderId
     * @param tradeOrderStatus tradeOrderStatus
     * @param userInfo userInfo
     * @return: @return {@link Pair }<{@link Boolean }, {@link Object }>
     */

    Pair<Boolean, Object> notifyOrderHandler(String orderId, Integer tradeOrderStatus, UserInfo userInfo);


    /**
     * 根据免押订单id查询订单
     *
     * @param freeDepositOrderId freeDepositOrderId
     * @return: @return {@link FreeServiceFeeOrder }
     */

    FreeServiceFeeOrder queryByFreeDepositOrderId(String freeDepositOrderId);

    /**
     * 获取用户免押服务费状态
     *
     * @param uid uid
     * @return: @return {@link UserFreeServiceFeeStatusVO }
     */

    UserFreeServiceFeeStatusVO getFreeServiceFeeStatus(Long uid);
}
