package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FreeServiceFeeOrder;
import org.apache.ibatis.annotations.Param;

public interface FreeServiceFeeOrderMapper {

    Integer existsPaySuccessOrder(@Param("freeDepositOrderId") String freeDepositOrderId, @Param("uid") Long uid);

    void insert(FreeServiceFeeOrder freeServiceFeeOrder);
}
