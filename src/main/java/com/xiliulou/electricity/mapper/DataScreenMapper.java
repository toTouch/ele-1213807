package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import com.xiliulou.electricity.vo.WeekCouponStatisticVo;
import com.xiliulou.electricity.vo.WeekOrderStatisticVo;
import com.xiliulou.electricity.vo.WeekTurnoverStatisticVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏数据库访问层
 *
 * @author Hrp
 * @since 2022-03-23 17:00:14
 */
public interface DataScreenMapper extends BaseMapper<ElectricityCabinet> {

    List<WeekOrderStatisticVo> queryWeekElectricityOrderStatistic(@Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime);

    List<WeekOrderStatisticVo> queryWeekMemberCardStatistic(@Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime);

    List<WeekOrderStatisticVo> queryWeekRentBatteryStatistic(@Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime);

    List<WeekCouponStatisticVo> queryWeekCouponIssue(@Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime,@Param("statusList") List<Integer> statusList);

    List<WeekTurnoverStatisticVo> queryWeekMemberCardTurnoverStatistic(@Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime);

    List<WeekTurnoverStatisticVo> queryWeekDepositTurnoverStatistic(@Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime);

    List<WeekOrderStatisticVo> queryWeekUserStatistic(@Param("tenantId") Integer tenantId, @Param("beginTime") Long beginTime);



}
