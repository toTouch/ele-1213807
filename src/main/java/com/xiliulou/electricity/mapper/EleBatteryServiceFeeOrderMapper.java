package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.EleBatteryServiceFeeOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeOrderVo;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 缴纳押金订单表(EleBatteryServiceFeeOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleBatteryServiceFeeOrderMapper extends BaseMapper<EleBatteryServiceFeeOrder>{


    List<EleBatteryServiceFeeOrder> queryUserList(@Param("uid") Long uid,
                                                   @Param("offset") Long offset, @Param("size") Long size, @Param("startTime") Long startTime, @Param("endTime") Long endTime);

    List<EleBatteryServiceFeeOrderVo> queryListForAdmin(@Param("uid") Long uid,
                                                        @Param("offset") Long offset, @Param("size") Long size, @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("status") Integer status);

    BigDecimal queryTurnOver(@Param("tenantId") Integer tenantId, @Param("uid") Long uid);

    BigDecimal queryTenantTurnOver(@Param("tenantId") Integer tenantId, @Param("todayStartTime") Long todayStartTime);
}
