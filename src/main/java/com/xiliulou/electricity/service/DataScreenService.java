package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.vo.WeekOrderStatisticVo;

import java.util.List;

/**
 * 数据大屏service
 *
 * @author hrp
 * @since 2022-03-22 10:56:56
 */
public interface DataScreenService {

    R queryOrderStatistics(Integer tenantId);

    R queryDataBrowsing(Integer tenantId);

    R queryMapProvince(Integer tenantId);

    R queryMapCity(Integer tenantId,Integer pid);

    R queryCoupon(Integer tenantId);

    R queryTurnoverAndUser(Integer tenantId);

    List<WeekOrderStatisticVo> queryWeekElectricityOrderStatistic(Integer tenantId, Long beginTime);

    List<WeekOrderStatisticVo> queryWeekMemberCardStatistic(Integer tenantId, Long beginTime);

    List<WeekOrderStatisticVo> queryWeekRentBatteryStatistic(Integer tenantId, Long beginTime);
}
