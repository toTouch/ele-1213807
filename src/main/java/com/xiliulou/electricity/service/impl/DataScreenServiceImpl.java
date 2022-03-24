package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.DataScreenMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.DataBrowsingVo;
import com.xiliulou.electricity.vo.OrderStatisticsVo;
import com.xiliulou.electricity.vo.WeekOrderStatisticVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    TenantService tenantService;

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

    @Override
    public R queryDataBrowsing(Integer tenantId) {

        DataBrowsingVo dataBrowsingVo=new DataBrowsingVo();

        //统计套餐总营业额
        CompletableFuture<BigDecimal> memberCardTurnOver = CompletableFuture.supplyAsync(()->{
            return electricityMemberCardOrderService.queryTurnOver(tenantId);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query catering order count error!", e);
            return null;
        });

        //统计押金总营业额
        CompletableFuture<BigDecimal> depositTurnOver = CompletableFuture.supplyAsync(()->{
            return eleDepositOrderService.queryTurnOver(tenantId);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query catering order count error!", e);
            return null;
        });

        //统计退押金总额
        CompletableFuture<BigDecimal> refundTurnOver = CompletableFuture.supplyAsync(()->{
            return eleRefundOrderService.queryTurnOver(tenantId);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query catering order count error!", e);
            return null;
        });

        //换电订单统计
        CompletableFuture<Integer> electricityOrderCount = CompletableFuture.supplyAsync(()->{
            ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().tenantId(tenantId).build();
            return electricityCabinetOrderService.queryCountForScreenStatistic(electricityCabinetOrderQuery);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query catering order count error!", e);
            return null;
        });

        //租电订单统计
        CompletableFuture<Integer> rentBatteryCount = CompletableFuture.supplyAsync(()->{
            RentBatteryOrderQuery rentBatteryOrderQuery=RentBatteryOrderQuery.builder().tenantId(tenantId).build();
            return rentBatteryOrderService.queryCountForScreenStatistic(rentBatteryOrderQuery);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query catering order count error!", e);
            return null;
        });

        //用户数量统计
        CompletableFuture<Void> userCount = CompletableFuture.runAsync(() -> {
            UserInfoQuery userInfoQuery=UserInfoQuery.builder().tenantId(tenantId).serviceStatus(1).build();
            Integer sumUserCount=userInfoService.querySumCount(userInfoQuery);
            dataBrowsingVo.setSumUserCount(sumUserCount);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });

        //租户总数统计
        CompletableFuture<Void> tenantCount = CompletableFuture.runAsync(() -> {
            Integer sumTenantCount=tenantService.querySumCount(null);
            dataBrowsingVo.setTenantCount(sumTenantCount);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricity Order Count error!", e);
            return null;
        });



        //计算总营业额
        CompletableFuture<Void> payAmountSumFuture = memberCardTurnOver
                .thenAcceptBoth(depositTurnOver, (memberCardSumAmount, depositSumAmount) ->{
                    BigDecimal  turnover = memberCardSumAmount.add(depositSumAmount);
                    dataBrowsingVo.setSumTurnover(turnover);
                }).exceptionally(e -> {
                    log.error("DATA SUMMARY BROWSING ERROR! statistics pay amount sum error!" ,e);
                    return null;
                });

        //计算总订单数
        CompletableFuture<Void> orderSumFuture = electricityOrderCount
                .thenAcceptBoth(rentBatteryCount, (electricityOrderSum, rentBatterySum) ->{
                    Integer orderSum=electricityOrderSum+rentBatterySum;
                    dataBrowsingVo.setSumOrderCount(orderSum);
                }).exceptionally(e -> {
                    log.error("DATA SUMMARY BROWSING ERROR! statistics pay amount sum error!" ,e);
                    return null;
                });

        return null;
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
