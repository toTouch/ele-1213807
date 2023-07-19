package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.query.BatteryServiceFeeQuery;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.apache.commons.lang3.tuple.Pair;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 退款订单表(TEleBatteryServiceFeeOrder)表服务接口
 *
 * @author makejava
 * @since 2022-04-20 10:21:24
 */
public interface EleBatteryServiceFeeOrderService {

    EleBatteryServiceFeeOrder queryEleBatteryServiceFeeOrderByOrderId(String orderNo);

    Integer insert(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder);

    void update(EleBatteryServiceFeeOrder eleBatteryServiceFeeOrder);

    /**
     * 用户查询电池服务费支付记录
     *
     * @param offset
     * @param size
     * @param startTime
     * @param endTime
     * @return
     */
    R queryList(Long offset, Long size, Long startTime, Long endTime);

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
    R queryListForAdmin(Long offset, Long size, Long startTime, Long endTime, Long uid, Integer status,Integer tenantId);

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

    void membercardExpireGenerateServiceFeeOrder();
}
