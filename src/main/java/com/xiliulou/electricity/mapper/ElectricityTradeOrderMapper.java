package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ElectricityTradeOrderMapper extends BaseMapper<ElectricityTradeOrder> {

    @Select("SELECT *  FROM t_electricity_trade_order WHERE trade_order_no =#{outTradeNo} ")
    ElectricityTradeOrder selectTradeOrderByTradeOrderNo(@Param("outTradeNo") String outTradeNo);

    @Select("SELECT *  FROM t_electricity_trade_order WHERE order_no =#{orderId} ")
    ElectricityTradeOrder selectTradeOrderByOrderId(String orderId);
    
    List<ElectricityTradeOrder> selectListByChannelOrderNoList(@Param("transactionIdList") List<String> transactionIdList, @Param("status") Integer status,@Param("endTime") Long endTime);
    
    List<Integer> existByTenantIdList(@Param("tenantIdList") List<Integer> tenantIdList,@Param("startTime") long startTime, @Param("endTime") long endTime);
}
