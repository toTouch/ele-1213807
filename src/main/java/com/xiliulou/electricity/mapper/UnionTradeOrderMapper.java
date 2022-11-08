package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UnionTradeOrderMapper extends BaseMapper<UnionTradeOrder> {

    @Select("SELECT *  FROM t_union_trade_order WHERE trade_order_no =#{outTradeNo} ")
    UnionTradeOrder selectTradeOrderByTradeOrderNo(@Param("outTradeNo") String outTradeNo);

    @Select("SELECT *  FROM t_union_trade_order WHERE order_no =#{orderId} ")
    UnionTradeOrder selectTradeOrderByOrderId(String orderId);
}
