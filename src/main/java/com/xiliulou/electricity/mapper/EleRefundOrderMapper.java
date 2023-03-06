package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleRefundOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.vo.EleRefundOrderVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 退款订单表(TEleRefundOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
public interface EleRefundOrderMapper extends BaseMapper<EleRefundOrder> {

    List<EleRefundOrderVO> queryList(@Param("query") EleRefundQuery eleRefundQuery);

    Integer queryCount(@Param("query") EleRefundQuery eleRefundQuery);

    Long queryUserInfoId(@Param("refundOrderNo") String refundOrderNo);

    BigDecimal queryTurnOver(@Param("tenantId") Integer tenantId);

    BigDecimal queryTurnOverByTime(@Param("tenantId") Integer tenantId, @Param("todayStartTime") Long todayStartTime,@Param("refundOrderType") Integer refundOrderType);

    List<EleRefundOrderVO> selectCarRefundPageList(EleRefundQuery eleRefundQuery);

    Integer selectCarRefundPageCount(EleRefundQuery eleRefundQuery);

    BigDecimal queryTurnOverByTime(@Param("tenantId") Integer tenantId, @Param("todayStartTime") Long todayStartTime, @Param("refundOrderType") Integer refundOrderType, @Param("franchiseeIds") List<Long> franchiseeIds);

    BigDecimal queryCarRefundTurnOverByTime(@Param("tenantId") Integer tenantId, @Param("todayStartTime") Long todayStartTime, @Param("refundOrderType") Integer refundOrderType, @Param("franchiseeIds") List<Long> franchiseeIds);

    Long queryRefundTime(@Param("orderId") String orderId);

    List<EleRefundOrder> selectBatteryFreeDepositRefundingOrder(@Param("offset") Integer offset, @Param("size") Integer size);

    List<EleRefundOrder> selectCarFreeDepositRefundingOrder(@Param("offset") Integer offset, @Param("size") Integer size);
}
