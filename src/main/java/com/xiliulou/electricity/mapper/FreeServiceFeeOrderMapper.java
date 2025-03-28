package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FreeServiceFeeOrder;
import com.xiliulou.electricity.query.FreeServiceFeePageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FreeServiceFeeOrderMapper {

    void update(@Param("freeServiceFeeOrder") FreeServiceFeeOrder freeServiceFeeOrder);

    Integer existsPaySuccessOrder(@Param("freeDepositOrderId") String freeDepositOrderId, @Param("uid") Long uid);

    void insert(FreeServiceFeeOrder freeServiceFeeOrder);

    List<FreeServiceFeeOrder> selectPageList(@Param("query") FreeServiceFeePageQuery query);

    Long selectCount(@Param("query") FreeServiceFeePageQuery query);

    FreeServiceFeeOrder selectByOrderId(@Param("orderId") String orderId);

    FreeServiceFeeOrder selectByFreeDepositOrderId(@Param("orderId") String freeDepositOrderId);
}
