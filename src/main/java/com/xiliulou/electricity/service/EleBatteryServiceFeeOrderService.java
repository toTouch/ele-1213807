package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.query.BatteryServiceFeeOrderQuery;
import com.xiliulou.electricity.query.BatteryServiceFeeQuery;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款订单表(TEleBatteryServiceFeeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-04-20 10:21:24
 */
public interface EleBatteryServiceFeeOrderService {
    
    Integer insert(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder);
    
    void update(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder);
    
    /**
     * 用户查询电池服务费支付记录
     *
     * @return
     */
    R queryList(BatteryServiceFeeOrderQuery query);
    
    /**
     * 后台查询电池服务费支付记录
     *
     * @param offset
     * @param size
     * @param startTime
     * @param endTime
     * @param uid
     * @param status
     * @return
     */
    R queryListForAdmin(Long offset, Long size, Long startTime, Long endTime, Long uid, Integer status, Integer tenantId);
    
    R queryList(BatteryServiceFeeQuery batteryServiceFeeQuery);
    
    R queryCount(BatteryServiceFeeQuery batteryServiceFeeQuery);
    
    /**
     * 用户的总消费额
     *
     * @param tenantId
     * @param uid
     * @return
     */
    BigDecimal queryUserTurnOver(Integer tenantId, Long uid);
    
    /**
     * 总消费额
     *
     * @param tenantId
     * @param todayStartTime
     * @return
     */
    BigDecimal queryTurnOver(Integer tenantId, Long todayStartTime, List<Long> franchiseeId);
    
    List<HomePageTurnOverGroupByWeekDayVo> queryTurnOverByCreateTime(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime);
    
    BigDecimal queryAllTurnOver(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long endTime);
    
    
    EleBatteryServiceFeeOrder selectByOrderNo(String orderNo);
    
    Integer updateByOrderNo(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder);
    
    void membercardExpireGenerateServiceFeeOrder(String s);
    
    R listSuperAdminPage(BatteryServiceFeeQuery batteryServiceFeeQuery);
    
    R countTotalForSuperAdmin(BatteryServiceFeeQuery batteryServiceFeeQuery);
    
    /**
     * 获取租户的滞纳金起算时间，未生成滞纳金订单时 orderId 传null即可
     *
     * @param eleBatteryServiceFeeOrder 滞纳金订单
     * @param tenantId                  租户id
     * @return 过期滞纳金起算时间
     */
    Integer getExpiredProtectionTime(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder, Integer tenantId);
}
