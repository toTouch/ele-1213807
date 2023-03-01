package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.query.*;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * (FreeDepositOrder)表服务接口
 *
 * @author makejava
 * @since 2023-02-15 11:39:27
 */
public interface FreeDepositOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositOrder queryByIdFromDB(Long id);

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    List<FreeDepositOrder> selectByPage(FreeDepositOrderQuery query);

    /**
     * 新增数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    FreeDepositOrder insert(FreeDepositOrder freeDepositOrder);

    /**
     * 修改数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    Integer update(FreeDepositOrder freeDepositOrder);

    FreeDepositOrder selectByOrderId(String orderId);

    Triple<Boolean, String, Object> freeBatteryDepositOrder(FreeBatteryDepositQuery freeBatteryDepositQuery);

    Triple<Boolean, String, Object> freeBatteryDepositPreCheck();

    Triple<Boolean, String, Object> freeCarDepositPreCheck();

    Triple<Boolean, String, Object> acquireUserFreeBatteryDepositStatus();

    Triple<Boolean, String, Object> freeBatteryDepositHybridOrder(FreeBatteryDepositHybridOrderQuery query, HttpServletRequest request);

    Triple<Boolean, String, Object> freeCarDepositOrder(FreeCarDepositQuery freeCarDepositQuery);

    Triple<Boolean, String, Object> acquireFreeCarDepositStatus();

    Triple<Boolean, String, Object> freeCarDepositHybridOrder(FreeCarDepositHybridOrderQuery query, HttpServletRequest request);

    Triple<Boolean, String, Object> freeCarBatteryDepositHybridOrder(FreeCarBatteryDepositHybridOrderQuery query, HttpServletRequest request);

    Integer selectByPageCount(FreeDepositOrderQuery query);
    
    Triple<Boolean, String, Object> freeDepositAuthToPay(String orderId, BigDecimal payTransAmt);
    
    Triple<Boolean, String, Object> selectFreeDepositAuthToPay(String orderId);

    Triple<Boolean, String, Object> selectFreeDepositOrderStatus(String orderId);

    void handleFreeDepositRefundOrder();
}
