package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.electricity.entity.EleDepositOrder;

import java.math.BigDecimal;
import java.util.List;

import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 缴纳押金订单表(TEleDepositOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleDepositOrderMapper extends BaseMapper<EleDepositOrder> {

    /**
     * 查询指定行数据2
     */
    List<EleDepositOrderVO> queryList(@Param("query") EleDepositOrderQuery eleDepositOrderQuery);

    List<EleDepositOrderVO> queryListForRentCar(@Param("query") EleDepositOrderQuery eleDepositOrderQuery);

    Integer queryCount(@Param("query") EleDepositOrderQuery eleDepositOrderQuery);

    List<EleDepositOrderVO> queryListForUser(@Param("query") EleDepositOrderQuery eleDepositOrderQuery);

    BigDecimal queryTurnOver(@Param("tenantId") Integer tenantId);

    EleDepositOrder queryLastPayDepositTimeByUid(@Param("uid") Long uid, @Param("franchiseeId") Long franchiseeId, @Param("tenantId") Integer tenantId);

    BigDecimal queryDepositTurnOverByDepositType(@Param("tenantId") Integer tenantId, @Param("todayStartTime") Long todayStartTime, @Param("depositType") Integer depositType, @Param("franchiseeId") Long franchiseeId);

    List<HomePageTurnOverGroupByWeekDayVo> queryDepositTurnOverAnalysisByDepositType(@Param("tenantId") Integer tenantId, @Param("depositType") Integer depositType, @Param("franchiseeId") Long franchiseeId, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);


}
