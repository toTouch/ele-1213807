package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Province;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.DataScreenMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    StoreService storeService;
    @Autowired
    ProvinceService provinceService;
    @Autowired
    UserCouponService userCouponService;

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
            MemberCardOrderQuery memberCardOrderQuery = MemberCardOrderQuery.builder().tenantId(tenantId).build();
            Integer count = electricityMemberCardOrderService.queryCountForScreenStatistic(memberCardOrderQuery);
            orderStatisticsVo.setMemberCardOrderCount(count);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query memberCard Order Count error!", e);
            return null;
        });

        //租电数量统计
        CompletableFuture<Void> rentBatteryCount = CompletableFuture.runAsync(() -> {
            RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().tenantId(tenantId).build();
            Integer count = rentBatteryOrderService.queryCountForScreenStatistic(rentBatteryOrderQuery);
            orderStatisticsVo.setRentBatteryCount(count);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query rentBattery Order Count error!", e);
            return null;
        });

        //近七天换电订单统计
        CompletableFuture<Void> weekElectricityOrderStatistic = CompletableFuture.runAsync(() -> {
            List<WeekOrderStatisticVo> weekOrderStatisticVos = dataScreenMapper.queryWeekElectricityOrderStatistic(tenantId, beginTime);
            orderStatisticsVo.setWeekOrderStatisticVos(weekOrderStatisticVos);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query weekElectricity Order Count error!", e);
            return null;
        });

        //近七天购卡数量统计
        CompletableFuture<Void> weekMemberCardStatistic = CompletableFuture.runAsync(() -> {
            List<WeekOrderStatisticVo> weekOrderStatisticVoList = dataScreenMapper.queryWeekMemberCardStatistic(tenantId, beginTime);
            orderStatisticsVo.setWeekMemberCardStatisticVos(weekOrderStatisticVoList);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query weekMemberCard Order Count error!", e);
            return null;
        });

        //近七天租电池数量统计
        CompletableFuture<Void> weekRentBatteryStatistic = CompletableFuture.runAsync(() -> {
            List<WeekOrderStatisticVo> weekOrderStatisticVoList = dataScreenMapper.queryWeekRentBatteryStatistic(tenantId, beginTime);
            orderStatisticsVo.setWeekRentBatteryStatisticVos(weekOrderStatisticVoList);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query WeekRentBattery Order Count error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(electricityOrderCount, memberCardCount, rentBatteryCount
                , weekElectricityOrderStatistic, weekMemberCardStatistic, weekRentBatteryStatistic);

        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("ORDER STATISTICS ERROR!", e);
        }
        return R.ok(orderStatisticsVo);

    }

    @Override
    public R queryDataBrowsing(Integer tenantId) {

        DataBrowsingVo dataBrowsingVo = new DataBrowsingVo();

        //统计套餐总营业额
        CompletableFuture<BigDecimal> memberCardTurnOver = CompletableFuture.supplyAsync(() -> {
            return electricityMemberCardOrderService.queryTurnOver(tenantId);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query MemberCardTurnOver error!", e);
            return null;
        });

        //统计押金总营业额
        CompletableFuture<BigDecimal> depositTurnOver = CompletableFuture.supplyAsync(() -> {
            return eleDepositOrderService.queryTurnOver(tenantId);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query depositTurnOver error!", e);
            return null;
        });

        //统计退押金总额
        CompletableFuture<BigDecimal> refundTurnOver = CompletableFuture.supplyAsync(() -> {
            return eleRefundOrderService.queryTurnOver(tenantId);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query refundTurnOver count error!", e);
            return null;
        });

        //换电订单统计
        CompletableFuture<Integer> electricityOrderCount = CompletableFuture.supplyAsync(() -> {
            ElectricityCabinetOrderQuery electricityCabinetOrderQuery = ElectricityCabinetOrderQuery.builder().tenantId(tenantId).build();
            return electricityCabinetOrderService.queryCountForScreenStatistic(electricityCabinetOrderQuery);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query electricityOrderTurnOver error!", e);
            return null;
        });

        //租电订单统计
        CompletableFuture<Integer> rentBatteryCount = CompletableFuture.supplyAsync(() -> {
            RentBatteryOrderQuery rentBatteryOrderQuery = RentBatteryOrderQuery.builder().tenantId(tenantId).build();
            return rentBatteryOrderService.queryCountForScreenStatistic(rentBatteryOrderQuery);
        }, threadPool).exceptionally(e -> {
            log.error("DATA SUMMARY BROWSING ERROR! query rentBatteryTurnOver error!", e);
            return null;
        });

        //用户数量统计
        CompletableFuture<Void> userCount = CompletableFuture.runAsync(() -> {
            UserInfoQuery userInfoQuery = UserInfoQuery.builder().tenantId(tenantId).serviceStatus(1).build();
            Integer sumUserCount = userInfoService.querySumCount(userInfoQuery);
            dataBrowsingVo.setSumUserCount(sumUserCount);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query UserTurnOver error!", e);
            return null;
        });

        //租户总数统计
        CompletableFuture<Void> tenantCount = CompletableFuture.runAsync(() -> {
            Integer sumTenantCount = tenantService.querySumCount(null);
            dataBrowsingVo.setTenantCount(sumTenantCount);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query TenantTurnOver error!", e);
            return null;
        });

        //换电柜总数统计
        CompletableFuture<Void> electricityCabinetCount = CompletableFuture.runAsync(() -> {
            ElectricityCabinetQuery electricityCabinetQuery = ElectricityCabinetQuery.builder().tenantId(tenantId).build();
            Integer sumElectricityCabinetCount = electricityCabinetService.querySumCount(electricityCabinetQuery);
            dataBrowsingVo.setElectricityCabinetCount(sumElectricityCabinetCount);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricityCabinetTurnOver error!", e);
            return null;
        });

        //电池总数统计
        CompletableFuture<Void> batteryCount = CompletableFuture.runAsync(() -> {
            ElectricityBatteryQuery electricityBatteryQuery = ElectricityBatteryQuery.builder().tenantId(tenantId).build();
            Integer sumBatteryCount = electricityBatteryService.querySumCount(electricityBatteryQuery);
            dataBrowsingVo.setBatteryCount(sumBatteryCount);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query BatteryTurnOver error!", e);
            return null;
        });

        //换电成功率
        CompletableFuture<Void> electricityOrderSuccessRate = CompletableFuture.runAsync(() -> {
            BigDecimal orderSuccessRate = electricityCabinetOrderService.homeOneSuccess(null, null, null, tenantId);
            dataBrowsingVo.setElectricityOrderSuccessRate(orderSuccessRate);
        }, threadPool).exceptionally(e -> {
            log.error("ORDER STATISTICS ERROR! query electricityOrderSuccessRate error!", e);
            return null;
        });


        //计算总营业额
        CompletableFuture<Void> payAmountSumFuture = memberCardTurnOver
                .thenAcceptBoth(depositTurnOver, (memberCardSumAmount, depositSumAmount) -> {
                    BigDecimal turnover = memberCardSumAmount.add(depositSumAmount);
                    dataBrowsingVo.setSumTurnover(turnover);
                }).exceptionally(e -> {
                    log.error("DATA SUMMARY BROWSING ERROR! statistics pay amount sum error!", e);
                    return null;
                });

        //计算总订单数
        CompletableFuture<Void> orderSumFuture = electricityOrderCount
                .thenAcceptBoth(rentBatteryCount, (electricityOrderSum, rentBatterySum) -> {
                    Integer orderSum = electricityOrderSum + rentBatterySum;
                    dataBrowsingVo.setSumOrderCount(orderSum);
                }).exceptionally(e -> {
                    log.error("DATA SUMMARY BROWSING ERROR! statistics order sum error!", e);
                    return null;
                });

        //等待所有线程停止 thenAcceptBoth方法会等待a,b线程结束后获取结果
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(userCount, tenantCount, electricityCabinetCount, batteryCount, electricityOrderSuccessRate
                , payAmountSumFuture, orderSumFuture);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("DATA SUMMARY BROWSING ERROR!", e);
        }

        return R.ok(dataBrowsingVo);
    }

    @Override
    public R queryMapProvince(Integer tenantId) {
        //查询不同省份的门店数量
        List<MapVo> mapVoList = storeService.queryCountGroupByProvinceId(tenantId);

        if (CollectionUtils.isNotEmpty(mapVoList)) {
            mapVoList.parallelStream().forEach(item -> {
                if (Objects.nonNull(item.getPid())) {
                    //查询省份下所有门店id
                    List<Long> storeIds = storeService.queryStoreIdsByProvinceIdOrCityId(tenantId, item.getPid(), null);
                    //获取电柜数量
                    if (CollectionUtils.isNotEmpty(storeIds)) {
                        Integer electricityCabinetCount = electricityCabinetService.queryCountByStoreIds(tenantId, storeIds);
                        item.setElectricityCabinetCount(electricityCabinetCount);
                        return;
                    }
                }
                item.setElectricityCabinetCount(0);
            });
        }
        return R.ok(mapVoList);
    }

    @Override
    public R queryMapCity(Integer tenantId, Integer pid) {
        Province province = provinceService.queryByIdFromDB(pid);
        if (Objects.isNull(province)) {
            log.error("QUERY MAP CITY ERROR! province not find error! pid={}", pid);
            return R.fail("ELECTRICITY.00122", "未查询到该省份");
        }

        //获取该省下所有的城市Id，门店数
        List<MapVo> mapVoList = storeService.queryCountGroupByCityId(tenantId, pid);

        if (CollectionUtils.isNotEmpty(mapVoList)) {
            mapVoList.parallelStream().forEach(item -> {
                if (Objects.nonNull(item.getCid())) {
                    List<Long> storeIds = storeService.queryStoreIdsByProvinceIdOrCityId(tenantId, item.getPid(), item.getCid());
                    //根据门店id获取电柜数量
                    Integer electricityCabinetCount = electricityCabinetService.queryCountByStoreIds(tenantId, storeIds);
                    item.setElectricityCabinetCount(electricityCabinetCount);
                    return;
                }
                item.setElectricityCabinetCount(0);
            });
        }
        return R.ok(mapVoList);
    }

    @Override
    public R queryCoupon(Integer tenantId) {
        CouponStatisticVo couponStatisticVo = new CouponStatisticVo();

        //六天前凌晨的时间戳
        Long beginTime = daysToStamp(-6);

        //优惠券发放数量
        CompletableFuture<Void> couponIssue = CompletableFuture.runAsync(() -> {
            UserCouponQuery userCouponQuery = UserCouponQuery.builder().tenantId(tenantId).build();
            Integer couponIssueCount = (Integer) userCouponService.queryCount(userCouponQuery).getData();
            couponStatisticVo.setCouponIssueCount(couponIssueCount);
        }, threadPool).exceptionally(e -> {
            log.error("COUPON STATISTICS ERROR! query issue coupon Count error!", e);
            return null;
        });

        //优惠券使用数量
        CompletableFuture<Void> couponUse = CompletableFuture.runAsync(() -> {
            UserCouponQuery userCouponQuery = UserCouponQuery.builder().tenantId(tenantId).statusList(List.of(2)).build();
            Integer couponUseCount = (Integer) userCouponService.queryCount(userCouponQuery).getData();
            couponStatisticVo.setCouponUseCount(couponUseCount);
        }, threadPool).exceptionally(e -> {
            log.error("COUPON STATISTICS ERROR! query use coupon Count error!", e);
            return null;
        });

        //周优惠券发放数量
        CompletableFuture<Void> weekCouponIssueStatistic = CompletableFuture.runAsync(() -> {
            List<WeekCouponStatisticVo> weekCouponStatisticVos = dataScreenMapper.queryWeekCouponIssue(tenantId, beginTime, null);
            couponStatisticVo.setWeekCouponIssue(weekCouponStatisticVos);
        }, threadPool).exceptionally(e -> {
            log.error("COUPON STATISTICS ERROR! query issue coupon Count error!", e);
            return null;
        });

        //周优惠券使用数量
        CompletableFuture<Void> weekCouponUseStatistic = CompletableFuture.runAsync(() -> {
            List<WeekCouponStatisticVo> weekCouponStatisticVos = dataScreenMapper.queryWeekCouponIssue(tenantId, beginTime, List.of(2));
            couponStatisticVo.setWeekCouponUse(weekCouponStatisticVos);
        }, threadPool).exceptionally(e -> {
            log.error("COUPON STATISTICS ERROR! query issue coupon Count error!", e);
            return null;
        });

        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(couponIssue, couponUse, weekCouponIssueStatistic
                , weekCouponUseStatistic);

        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("ORDER STATISTICS ERROR!", e);
        }
        return R.ok(couponStatisticVo);
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
