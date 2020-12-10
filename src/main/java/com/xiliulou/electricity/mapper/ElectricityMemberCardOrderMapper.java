package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface ElectricityMemberCardOrderMapper extends BaseMapper<ElectricityMemberCardOrder> {

    @Select("SELECT *  FROM t_electricity_member_card_order  WHERE order_id = #{orderId}")
    ElectricityMemberCardOrder selectByOrderNo(@Param("orderId") String orderNo);
    @Select("SELECT *  FROM t_electricity_member_card_order  WHERE uid =#{uid} and status =1 order by create_time desc limit #{offset},#{size}")
    List<ElectricityMemberCardOrder> getMemberCardOrderPage(@Param("uid") Long uid, @Param("offset") Long offset, @Param("size") Long size);

    @Select("SELECT sum(pay_amount)  FROM t_electricity_member_card_order  WHERE status = 1 and create_time &gt;= #{first} and create_time &lt;= #{now}")
    BigDecimal homeOne(@Param("first") Long first, @Param("now") Long now);


    @Select(" select from_unixtime(create_time / 1000, '%Y-%m-%d') date, sum(pay_amount) as money\n" +
            "from t_electricity_member_card_order\n" +
            "where create_time &gt;= #{startTimeMilliDay} and create_time &lt;=#{endTimeMilliDay}\n" +
            "group by from_unixtime(create_time / 1000, '%Y-%m-%d')\n" +
            "order by from_unixtime(create_time / 1000, '%Y-%m-%d') desc")
    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay);
}
