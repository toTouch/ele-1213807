package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.mapper.DataScreenMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.ElectricityMemberCardOrderQuery;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.query.RentBatteryOrderQuery;
import com.xiliulou.electricity.service.DataScreenService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.vo.OrderStatisticsVo;
import com.xiliulou.electricity.vo.WeekOrderStatisticVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 数据大屏service
 *
 * @author hrp
 * @since 2022-03-22 10:56:56
 */

@Service("dataScreenService")
@Slf4j
public class DataScreenServiceImpl implements DataScreenService {


    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    DataScreenMapper dataScreenMapper;

    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("DATA-SCREEN-THREAD-POOL", 4, "dataScreenThread:");

    @Override
    public R queryOrderStatistics(Integer tenantId) {

        OrderStatisticsVo orderStatisticsVo = new OrderStatisticsVo();

        //六天前凌晨的时间戳
        Long beginTime = daysToStamp(-6);

        //换电订单数量统计
        CompletableFuture<Void> electricityOrderCount = CompletableFuture.runAsync(() -> {
            ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().tenantId(tenantId).build();
            Integer count = electricityCabinetOrderService.queryCountForScreenStatistic(electricityCabinetOrderQuery);
            orderStatisticsVo.setElectricityOrderCount(count);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //购卡数量统计
        CompletableFuture<Void> memberCardCount = CompletableFuture.runAsync(() -> {
            MemberCardOrderQuery memberCardOrderQuery=MemberCardOrderQuery.builder().tenantId(tenantId).build();
            Integer count=electricityMemberCardOrderService.queryCountForScreenStatistic(memberCardOrderQuery);
            orderStatisticsVo.setMemberCardOrderCount(count);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //租电数量统计
        CompletableFuture<Void> rentBatteryCount = CompletableFuture.runAsync(() -> {
            RentBatteryOrderQuery rentBatteryOrderQuery=RentBatteryOrderQuery.builder().tenantId(tenantId).build();
            Integer count= rentBatteryOrderService.queryCountForScreenStatistic(rentBatteryOrderQuery);
            orderStatisticsVo.setRentBatteryCount(count);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //近七天换电订单统计
        CompletableFuture<Void> weekElectricityOrderStatistic = CompletableFuture.runAsync(() -> {
            List<WeekOrderStatisticVo> weekOrderStatisticVos=dataScreenMapper.queryWeekElectricityOrderStatistic(tenantId,beginTime);
            orderStatisticsVo.setWeekOrderStatisticVos(weekOrderStatisticVos);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //近七天购卡数量统计
        CompletableFuture<Void> weekMemberCardStatistic = CompletableFuture.runAsync(() -> {
            List<WeekOrderStatisticVo> weekOrderStatisticVoList= dataScreenMapper.queryWeekMemberCardStatistic(tenantId,beginTime);
            orderStatisticsVo.setWeekMemberCardStatisticVos(weekOrderStatisticVoList);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //近七天租电池数量统计
        CompletableFuture<Void> weekRentBatteryStatistic = CompletableFuture.runAsync(() -> {
            List<WeekOrderStatisticVo> weekOrderStatisticVoList= dataScreenMapper.queryWeekRentBatteryStatistic(tenantId,beginTime);
            orderStatisticsVo.setWeekRentBatteryStatisticVos(weekOrderStatisticVoList);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(electricityOrderCount, memberCardCount, rentBatteryCount
                , weekElectricityOrderStatistic,weekMemberCardStatistic,weekRentBatteryStatistic);

        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("ORDER STATISTICS ERROR!", e);
        }
        return R.ok(orderStatisticsVo);

    }



    /***
     * 传入一个天数返回天数的时间戳
     */
    public static long daysToStamp(int days) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.add(Calendar.DAY_OF_MONTH, days);

        return instance.getTime().getTime();


    }
}
