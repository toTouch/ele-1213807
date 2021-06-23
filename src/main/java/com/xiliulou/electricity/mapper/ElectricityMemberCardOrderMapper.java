package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.vo.ElectricityMemberCardOrderVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public interface ElectricityMemberCardOrderMapper extends BaseMapper<ElectricityMemberCardOrder> {

    @Select("SELECT *  FROM t_electricity_member_card_order  WHERE order_id = #{orderId}")
    ElectricityMemberCardOrder selectByOrderNo(@Param("orderId") String orderNo);

    List<ElectricityMemberCardOrder> queryUserList(@Param("uid") Long uid,
                                                            @Param("offset") Long offset, @Param("size") Long size, @Param("startTime") Long startTime, @Param("endTime") Long endTime);


    BigDecimal homeOne(@Param("first") Long first, @Param("now") Long now,@Param("cardIdList") List<Integer> cardIdList);



    List<HashMap<String, String>> homeThree(@Param("startTimeMilliDay") long startTimeMilliDay, @Param("endTimeMilliDay") Long endTimeMilliDay,@Param("cardIdList") List<Integer> cardIdList);

    @Select("SELECT * FROM  t_electricity_member_card_order  WHERE uid = #{uid} AND status =1  ORDER BY create_time desc LIMIT 0,1")
    ElectricityMemberCardOrder getRecentOrder(@Param("uid") Long uid);

    List<ElectricityMemberCardOrderVO> queryList(@Param("query") MemberCardOrderQuery memberCardOrderQuery);

    Long getMemberCardOrderCount(@Param("uid") Long uid, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
