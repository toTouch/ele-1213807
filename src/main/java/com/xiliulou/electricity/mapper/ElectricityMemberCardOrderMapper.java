package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ElectricityMemberCardOrderMapper extends BaseMapper<ElectricityMemberCardOrder> {

    @Select("SELECT *  FROM t_electricity_member_card_order  WHERE order_id = #{orderId}")
    ElectricityMemberCardOrder selectByOrderNo(@Param("orderId") String orderNo);

    @Select("SELECT *  FROM t_electricity_member_card_order  WHERE uid =#{uid} and status =1 order by create_time desc limit #{offset},#{size}")
    List<ElectricityMemberCardOrder> getMemberCardOrderPage(@Param("uid") Long uid, @Param("offset") Long offset, @Param("size") Long size);
}
