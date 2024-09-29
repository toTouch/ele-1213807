package com.xiliulou.electricity.service;

import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.vo.FreeDepositOrderVO;
import com.xiliulou.electricity.vo.FreeDepositVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    List<FreeDepositOrderVO> selectByPage(FreeDepositOrderQuery query);

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
    
    Triple<Boolean, String, Object> acquireUserFreeBatteryDepositStatusV2();
    
    Integer selectByPageCount(FreeDepositOrderQuery query);
    
    Triple<Boolean, String, Object> freeDepositAuthToPay(String orderId, BigDecimal payTransAmt, String remark);

    Triple<Boolean, String, Object> selectFreeDepositAuthToPay(String orderId);

    Triple<Boolean, String, Object> selectFreeDepositOrderStatus(String orderId);

    Triple<Boolean, String, Object> selectFreeDepositOrderStatus(FreeDepositOrder freeDepositOrder);

    void handleFreeDepositRefundOrder();

    Triple<Boolean, String, Object> synchronizFreeDepositOrderStatus(String orderId);

    Triple<Boolean, String, Object> freeCarBatteryDepositPreCheck();
    
    Triple<Boolean, String, Object> selectFreeDepositOrderDetail();

    Triple<Boolean, String, Object> freeBatteryDepositOrderV3(FreeBatteryDepositQueryV3 query);
    
    Triple<Boolean, String, Object> freeBatteryDepositOrderV4(FreeBatteryDepositQueryV3 query);
    
    Triple<Boolean, String, Object> checkFreeDepositStatusFromPxz(FreeDepositUserDTO freeDepositUserDTO, PxzConfig pxzConfig);

    Triple<Boolean, String, Object> freeBatteryDepositHybridOrderV3(FreeBatteryDepositHybridOrderQuery query, HttpServletRequest request);
    
    void freeDepositOrderUpdateStatusTask();
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    Triple<Boolean, String, Object> freeDepositTrilateralPay(String orderId, BigDecimal payTransAmt, String remark);
    
    Map<String,Double> selectPayTransAmtByOrderIdsToMap(List<String> orderId);
    
    Triple<Boolean, String, Object> syncAuthPayStatus(String orderId,String authPayOrderId);
    
    FreeDepositOrder queryUserOrderByHash(Integer tenantId, Long uid, String md5);
}
