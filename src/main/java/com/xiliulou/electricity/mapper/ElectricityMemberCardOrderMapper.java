package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface ElectricityMemberCardOrderMapper extends BaseMapper<ElectricityMemberCardOrder> {

    @Select("SELECT *  FROM t_electricity_member_card_order  WHERE order_id = #{orderId}")
    ElectricityMemberCardOrder selectByOrderNo(@Param("orderId") String orderNo);

    @Select("SELECT sum(pay_amount)  FROM t_electricity_member_card_order  WHERE status = 1 and create_time &gt;= #{first} and create_time &lt;= #{now}")
    BigDecimal homeOne(@Param("first") Long first, @Param("now") Long now);
}
