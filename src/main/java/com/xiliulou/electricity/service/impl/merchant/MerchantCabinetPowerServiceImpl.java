package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceBindConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceCabinetBindConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetailPro;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetConditionRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceConditionRequest;
import com.xiliulou.electricity.service.ElePowerService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthDetailProService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthRecordProService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleSumPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceSelectVO;
import com.xiliulou.electricity.vo.merchant.MerchantPowerDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPowerRspVO;
import com.xiliulou.electricity.vo.merchant.MerchantPowerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 商户/场地/下 柜机电量/电费
 * @date 2024/2/20 19:14:12
 */
@Slf4j
@Service
public class MerchantCabinetPowerServiceImpl implements MerchantCabinetPowerService {
    
    XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("HandleMerchantPowerForProPool", 5, "merchantPowerForProPoolThread",
            new LinkedBlockingQueue<>(10000));
    
    @Resource
    private MerchantPlaceBindService merchantPlaceBindService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private ElePowerService elePowerService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantCabinetPowerMonthRecordProService merchantCabinetPowerMonthRecordProService;
    
    @Resource
    private MerchantCabinetPowerMonthDetailProService merchantCabinetPowerMonthDetailProService;
    
    @Resource
    private MerchantService merchantService;
    
    @Slave
    @Override
    public MerchantPowerRspVO todayPower(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro todayPower, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return null;
        }
        
        // 今日0点
        Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
        // 当前时间
        long todayEndTime = System.currentTimeMillis();
        
        // 执行查询
        MerchantPowerVO powerVo = getLivePowerData(cabinetIds, todayStartTime, todayEndTime, "today");
        
        MerchantPowerRspVO rspVO = new MerchantPowerRspVO();
        rspVO.setPower(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getPower());
        rspVO.setCharge(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getCharge());
        
        return rspVO;
    }
    
    @Slave
    @Override
    public MerchantPowerRspVO yesterdayPower(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro yesterdayPower, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return null;
        }
        
        // 昨日0点
        Long yesterdayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ONE);
        // 昨日23:59:59
        long yesterdayEndTime = DateUtils.getTimeAgoEndTime(NumberConstant.ONE);
        
        // 执行查询
        MerchantPowerVO powerVo = getLivePowerData(cabinetIds, yesterdayStartTime, yesterdayEndTime, "yesterday");
        
        MerchantPowerRspVO rspVO = new MerchantPowerRspVO();
        rspVO.setPower(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getPower());
        rspVO.setCharge(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getCharge());
        
        return rspVO;
    }
    
    @Slave
    @Override
    public MerchantPowerRspVO thisMonthPower(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro thisMonthPower, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return null;
        }
        
        // 本月第一天0点
        Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
        
        // 执行查询
        MerchantPowerVO powerVo = getLivePowerData(cabinetIds, thisMonthStartTime, System.currentTimeMillis(), "thisMonth");
        
        MerchantPowerRspVO rspVO = new MerchantPowerRspVO();
        rspVO.setPower(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getPower());
        rspVO.setCharge(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getCharge());
        
        return rspVO;
    }
    
    @Slave
    @Override
    public MerchantPowerRspVO lastMonthPower(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro lastMonthPower, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return null;
        }
        
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月最后一天23:59:59
        long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
        
        // 执行查询
        MerchantPowerVO powerVo = getLivePowerData(cabinetIds, lastMonthStartTime, lastMonthEndTime, "lastMonth");
        
        MerchantPowerRspVO rspVO = new MerchantPowerRspVO();
        rspVO.setPower(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getPower());
        rspVO.setCharge(Objects.isNull(powerVo) ? NumberConstant.ZERO_D : powerVo.getCharge());
        
        return rspVO;
    }
    
    @Slave
    @Override
    public MerchantPowerRspVO totalPower(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro totalPower, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return null;
        }
        
        //两个月前的数据（来源于历史表，定时任务）
        MerchantPowerDetailVO preTwoMonthPower = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, null);
        
        //上月第一天0点
        Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
        // 上月到当前的实时数据
        MerchantPowerVO recentlyTwoMonthPower = getLivePowerData(cabinetIds, lastMonthStartTime, System.currentTimeMillis(), "totalPower-recentlyTwoMonth");
        
        MerchantPowerRspVO vo = new MerchantPowerRspVO();
        vo.setPower(NumberConstant.ZERO_D);
        vo.setCharge(NumberConstant.ZERO_D);
        
        if (Objects.nonNull(preTwoMonthPower)) {
            vo.setPower(vo.getPower() + preTwoMonthPower.getPower());
            vo.setCharge(vo.getCharge() + preTwoMonthPower.getCharge());
        }
        if (Objects.nonNull(recentlyTwoMonthPower)) {
            vo.setPower(vo.getPower() + recentlyTwoMonthPower.getPower());
            vo.setCharge(vo.getCharge() + recentlyTwoMonthPower.getCharge());
        }
        
        return vo;
    }
    
    @Slave
    @Override
    public List<MerchantPowerDetailVO> lineData(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro lineData, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        List<MerchantPowerDetailVO> rspList = new ArrayList<>();
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return Collections.emptyList();
        }
        
        // 查询的月份
        List<String> monthList = request.getMonthList();
        if (CollectionUtils.isNotEmpty(monthList)) {
            for (String monthDate : monthList) {
                if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
                    continue;
                }
                
                MerchantPowerDetailVO merchantPowerDetailVO = merchantCabinetPowerMonthRecordProService.sumMonthPower(cabinetIds, List.of(monthDate));
                rspList.add(merchantPowerDetailVO);
            }
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    @Slave
    @Override
    public List<MerchantCabinetPowerVO> cabinetPowerList(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro cabinetPowerList, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return Collections.emptyList();
        }
        
        List<MerchantCabinetPowerVO> rspList = new ArrayList<>();
        // 遍历
        cabinetIds.forEach(cabinetId -> {
            // 今日0点
            Long todayStartTime = DateUtils.getTimeAgoStartTime(NumberConstant.ZERO);
            // 当前时间
            long todayEndTime = System.currentTimeMillis();
            // 今日电量/电费
            MerchantPowerVO todayPower = getLivePowerData(List.of(cabinetId), todayStartTime, todayEndTime, "cabinetPowerList-today");
            
            // 本月第一天0点
            Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
            // 本月电量/电费
            MerchantPowerVO thisMonthPower = getLivePowerData(List.of(cabinetId), thisMonthStartTime, System.currentTimeMillis(), "cabinetPowerList-thisMonth");
            
            //上月第一天0点
            Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
            // 上月最后一天23:59:59
            long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
            MerchantPowerVO lastMonthPower = getLivePowerData(List.of(cabinetId), lastMonthStartTime, lastMonthEndTime, "cabinetPowerList-lastMonth");
            
            // 获取历史数据查询的月份，比如：当前月份2024-02，要查的月份为2023-12、2023-11、2023-10、2023-09、2023-08、2023-07、2023-06、2023-05、2023-04、2023-03
            List<String> monthList = new ArrayList<>(10);
            for (int i = NumberConstant.TWO; i <= NumberConstant.TWELVE; i++) {
                monthList.add(DateUtils.getMonthDate((long) i));
            }
            MerchantPowerDetailVO merchantPowerDetailVO = merchantCabinetPowerMonthRecordProService.sumMonthPower(List.of(cabinetId), monthList);
            Long historyLatestReportTime = merchantCabinetPowerMonthDetailProService.queryLatestReportTime(cabinetId, monthList);
            
            // 最新上报时间
            Long latestTime = NumberConstant.ZERO_L;
            // 本年电量、本年电费
            Double thisYearPower = NumberConstant.ZERO_D;
            Double thisYearCharge = NumberConstant.ZERO_D;
            if (Objects.nonNull(merchantPowerDetailVO)) {
                thisYearPower += merchantPowerDetailVO.getPower();
                thisYearCharge += merchantPowerDetailVO.getCharge();
                latestTime = historyLatestReportTime;
            }
            if (Objects.nonNull(lastMonthPower)) {
                thisYearPower += lastMonthPower.getPower();
                thisYearCharge += lastMonthPower.getCharge();
                latestTime = lastMonthPower.getLatestTime();
            }
            if (Objects.nonNull(thisMonthPower)) {
                thisYearPower += thisMonthPower.getPower();
                thisYearCharge += thisMonthPower.getCharge();
                latestTime = thisMonthPower.getLatestTime();
            }
            
            if (Objects.nonNull(todayPower)) {
                latestTime = todayPower.getLatestTime();
            }
            
            //封装结果
            MerchantCabinetPowerVO merchantCabinetPowerVO = new MerchantCabinetPowerVO();
            merchantCabinetPowerVO.setTodayPower(Objects.isNull(todayPower) ? NumberConstant.ZERO_D : todayPower.getPower());
            merchantCabinetPowerVO.setTodayCharge(Objects.isNull(todayPower) ? NumberConstant.ZERO_D : todayPower.getCharge());
            merchantCabinetPowerVO.setThisMonthPower(Objects.isNull(thisMonthPower) ? NumberConstant.ZERO_D : thisMonthPower.getPower());
            merchantCabinetPowerVO.setThisMonthCharge(Objects.isNull(thisMonthPower) ? NumberConstant.ZERO_D : thisMonthPower.getCharge());
            merchantCabinetPowerVO.setThisYearPower(thisYearPower);
            merchantCabinetPowerVO.setThisYearCharge(thisYearCharge);
            merchantCabinetPowerVO.setLatestTime(latestTime);
            
            rspList.add(merchantCabinetPowerVO);
        });
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        // rspList根据latestTime倒叙排序
        rspList.sort(Comparator.comparing(MerchantCabinetPowerVO::getLatestTime).reversed());
        
        return rspList;
    }
    
    @Slave
    @Override
    public List<MerchantCabinetPowerDetailVO> cabinetPowerDetail(MerchantCabinetPowerRequest request) {
        Merchant merchant = merchantService.queryByUid(request.getUid());
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro cabinetPowerDetail, merchant not exist, uid={}", request.getUid());
            return null;
        }
        
        request.setMerchantId(merchant.getId());
        
        //获取要查询的柜机
        List<Long> cabinetIds = getStaticsCabinetIds(request);
        if (CollectionUtils.isEmpty(cabinetIds)) {
            return Collections.emptyList();
        }
        
        String monthDate = null;
        List<String> monthList = request.getMonthList();
        if (CollectionUtils.isNotEmpty(monthList)) {
            monthDate = monthList.get(NumberConstant.ZERO);
            if (!monthDate.matches(DateUtils.GREP_YEAR_MONTH)) {
                return Collections.emptyList();
            }
        }
        
        Long cabinetId = request.getCabinetId();
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(Math.toIntExact(cabinetId));
        if (Objects.isNull(electricityCabinet)) {
            log.warn("Merchant power for pro cabinetPowerDetail, cabinet not exist, cabinetId={}", cabinetId);
            return null;
        }
        
        String thisMonthDate = DateUtils.getMonthDate(NumberConstant.ZERO_L);
        String lastMonthDate = DateUtils.getMonthDate(NumberConstant.ONE_L);
        
        List<MerchantCabinetPowerDetailVO> rspList;
        
        // 近2个月数据实时查
        // 如果是本月
        if (Objects.equals(thisMonthDate, monthDate)) {
            // 本月第一天0点
            Long thisMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ZERO);
            
            MerchantPowerVO thisMonthPower = getLivePowerData(List.of(cabinetId), thisMonthStartTime, System.currentTimeMillis(), "cabinetPowerDetail-thisMonth");
            
            rspList = this.assembleLiveDetailPower(electricityCabinet, thisMonthPower, monthDate);
        } else if (Objects.equals(lastMonthDate, monthDate)) {
            // 如果是上月
            
            //上月第一天0点
            Long lastMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(NumberConstant.ONE);
            // 上月最后一天23:59:59
            long lastMonthEndTime = DateUtils.getBeforeMonthLastDayTimestamp(NumberConstant.ONE);
            
            MerchantPowerVO lastMonthPower = getLivePowerData(List.of(cabinetId), lastMonthStartTime, lastMonthEndTime, "cabinetPowerDetail-lastMonth");
            
            rspList = this.assembleLiveDetailPower(electricityCabinet, lastMonthPower, monthDate);
        } else {
            // 如果是2个月前,通过历史数据查询
            List<MerchantCabinetPowerMonthDetailPro> historyDetailList = merchantCabinetPowerMonthDetailProService.listByMonth(cabinetId, monthList);
            rspList = assembleHistoryDetailPower(electricityCabinet, historyDetailList, monthDate);
        }
        
        if (CollectionUtils.isEmpty(rspList)) {
            return Collections.emptyList();
        }
        
        return rspList;
    }
    
    private List<MerchantCabinetPowerDetailVO> assembleHistoryDetailPower(ElectricityCabinet cabinet, List<MerchantCabinetPowerMonthDetailPro> historyDetailList,
            String monthDate) {
        if (CollectionUtils.isEmpty(historyDetailList)) {
            return Collections.emptyList();
        }
        
        return historyDetailList.stream()
                .map(detail -> MerchantCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetId(detail.getEid()).sn(cabinet.getSn()).cabinetName(cabinet.getName())
                        .power(detail.getSumPower().doubleValue()).charge(detail.getSumCharge().doubleValue()).startTime(detail.getBeginTime()).endTime(detail.getEndTime())
                        .placeId(detail.getPlaceId())
                        .placeName(Optional.ofNullable(merchantPlaceService.queryFromCacheById(detail.getPlaceId())).map(MerchantPlace::getName).orElse(""))
                        .bindStatus(detail.getCabinetMerchantBindStatus()).build()).collect(Collectors.toList());
    }
    
    private List<MerchantCabinetPowerDetailVO> assembleLiveDetailPower(ElectricityCabinet cabinet, MerchantPowerVO monthPower, String monthDate) {
        if (Objects.isNull(monthPower)) {
            return Collections.emptyList();
        }
        
        List<MerchantPowerDetailVO> detailVOList = monthPower.getDetailVOList();
        if (CollectionUtils.isEmpty(detailVOList)) {
            return Collections.emptyList();
        }
        
        return detailVOList.stream()
                .map(detail -> MerchantCabinetPowerDetailVO.builder().monthDate(monthDate).cabinetId(detail.getEid()).sn(cabinet.getSn()).cabinetName(cabinet.getName())
                        .power(detail.getPower()).charge(detail.getCharge()).startTime(detail.getStartTime()).endTime(detail.getEndTime()).placeId(detail.getPlaceId())
                        .placeName(Optional.ofNullable(merchantPlaceService.queryFromCacheById(detail.getPlaceId())).map(MerchantPlace::getName).orElse(""))
                        .bindStatus(detail.getCabinetMerchantBindStatus()).build()).collect(Collectors.toList());
    }
    
    /**
     * 获取实时电量/电费处理逻辑
     */
    private MerchantPowerVO getLivePowerData(List<Long> cabinetIds, Long startTime, Long endTime, String date) {
        // 1.场地柜机绑定记录（绑定状态）：bindTime<=endTime
        MerchantPlaceCabinetConditionRequest cabinetBindRequest = new MerchantPlaceCabinetConditionRequest();
        cabinetBindRequest.setCabinetIds(new HashSet<>(cabinetIds));
        cabinetBindRequest.setEndTime(endTime);
        cabinetBindRequest.setStatus(MerchantPlaceCabinetBindConstant.STATUS_BIND);
        // 绑定状态记录
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.listBindRecord(cabinetBindRequest);
        
        // 2.解绑的场地柜机绑定记录（解绑状态）：unbindTime>=startTime或bindTime<=endTime
        MerchantPlaceCabinetConditionRequest cabinetUnbindRequest = new MerchantPlaceCabinetConditionRequest();
        cabinetUnbindRequest.setStartTime(startTime);
        cabinetUnbindRequest.setEndTime(endTime);
        cabinetUnbindRequest.setStatus(MerchantPlaceCabinetBindConstant.STATUS_UNBIND);
        cabinetUnbindRequest.setCabinetIds(new HashSet<>(cabinetIds));
        // 解绑状态记录
        List<MerchantPlaceCabinetBind> cabinetUnbindList = merchantPlaceCabinetBindService.listUnbindRecord(cabinetUnbindRequest);
        
        if (CollectionUtils.isEmpty(cabinetBindList) && CollectionUtils.isEmpty(cabinetUnbindList)) {
            return null;
        }
        
        // 3.商户场地绑定记录（绑定状态）：bindTime<=endTime
        MerchantPlaceConditionRequest placeBindRequest = new MerchantPlaceConditionRequest();
        placeBindRequest.setMerchantId(placeBindRequest.getMerchantId());
        placeBindRequest.setStatus(MerchantPlaceBindConstant.BIND);
        List<MerchantPlaceBind> placeBindList = merchantPlaceBindService.listBindRecord(placeBindRequest);
        
        // 4.解绑的商户场地绑定记录（解绑状态）：unbindTime>=startTime或bindTime<=endTime
        MerchantPlaceConditionRequest placeUnbindRequest = new MerchantPlaceConditionRequest();
        placeUnbindRequest.setMerchantId(placeUnbindRequest.getMerchantId());
        placeUnbindRequest.setStartTime(startTime);
        placeUnbindRequest.setEndTime(endTime);
        placeUnbindRequest.setStatus(MerchantPlaceBindConstant.UN_BIND);
        List<MerchantPlaceBind> placeUnbindList = merchantPlaceBindService.listBindRecord(placeBindRequest);
        
        if (CollectionUtils.isEmpty(placeBindList) && CollectionUtils.isEmpty(placeUnbindList)) {
            return null;
        }
        List<MerchantPlaceBind> placeMergeBindList = new ArrayList<>();
        placeMergeBindList.addAll(placeBindList);
        placeMergeBindList.addAll(placeUnbindList);
        
        // 5.根据柜机进行统计
        List<MerchantPowerDetailVO> powerVOList = new ArrayList<>();
        List<CabinetPowerProRunnable> collect = cabinetIds.parallelStream()
                .map(eid -> new CabinetPowerProRunnable(eid, electricityCabinetService, merchantPlaceBindService, elePowerService, startTime, endTime, cabinetBindList,
                        cabinetUnbindList, placeMergeBindList)).collect(Collectors.toList());
        try {
            List<Future<List<MerchantPowerDetailVO>>> futureList = executorService.invokeAll(collect);
            if (CollectionUtils.isNotEmpty(futureList)) {
                for (Future<List<MerchantPowerDetailVO>> future : futureList) {
                    List<MerchantPowerDetailVO> result = future.get();
                    if (CollectionUtils.isNotEmpty(result)) {
                        powerVOList.addAll(result);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("merchant " + date + " power for pro Exception occur! ", e);
        }
        
        MerchantPowerVO vo = null;
        if (CollectionUtils.isNotEmpty(powerVOList)) {
            // 电量
            double sumPower = powerVOList.parallelStream().mapToDouble(MerchantPowerDetailVO::getPower).sum();
            double sumCharge = powerVOList.parallelStream().mapToDouble(MerchantPowerDetailVO::getCharge).sum();
            
            vo = new MerchantPowerVO();
            vo.setPower(sumPower);
            vo.setCharge(sumCharge);
            vo.setLatestTime(powerVOList.stream().mapToLong(MerchantPowerDetailVO::getLatestTime).max().orElse(NumberConstant.ZERO));
            vo.setDetailVOList(powerVOList);
        }
        
        return vo;
    }
    
    @Slave
    @Override
    public Integer isShowPowerPage(Long uid) {
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro isShowPowerPage, merchant not exist, uid={}", uid);
            return null;
        }
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchant.getId(), null);
        if (CollectionUtils.isNotEmpty(bindList)) {
            return NumberConstant.ONE;
        }
        return NumberConstant.ZERO;
    }
    
    @Slave
    @Override
    public MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long uid) {
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro listPlaceAndCabinetByMerchantId, merchant not exist, uid={}", uid);
            return null;
        }
        
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchant.getId(), null);
        if (CollectionUtils.isEmpty(bindList)) {
            return null;
        }
        
        // 获取场地列表
        Set<Long> placeIdSet = bindList.parallelStream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        List<MerchantPlaceSelectVO> placeList = placeIdSet.parallelStream().map(placeId -> {
            MerchantPlaceSelectVO merchantPlaceUserVO = new MerchantPlaceSelectVO();
            merchantPlaceUserVO.setPlaceId(placeId);
            merchantPlaceUserVO.setPlaceName(Optional.ofNullable(merchantPlaceService.queryFromCacheById(placeId)).orElse(new MerchantPlace()).getName());
            
            return merchantPlaceUserVO;
        }).collect(Collectors.toList());
        
        // 获取柜机列表
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByPlaceIds(placeIdSet);
        if (CollectionUtils.isNotEmpty(merchantPlaceCabinetBindList)) {
            Set<Long> cabinetIdSet = merchantPlaceCabinetBindList.parallelStream().map(MerchantPlaceCabinetBind::getCabinetId).collect(Collectors.toSet());
            cabinetIdSet.forEach(cabinetId -> {
                MerchantPlaceCabinetVO merchantPlaceCabinetVO = new MerchantPlaceCabinetVO();
                merchantPlaceCabinetVO.setCabinetId(cabinetId);
                merchantPlaceCabinetVO.setCabinetName(
                        Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue())).orElse(new ElectricityCabinet()).getName());
                
                cabinetList.add(merchantPlaceCabinetVO);
            });
        }
        
        // 封装VO
        MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = new MerchantPlaceAndCabinetUserVO();
        merchantPlaceAndCabinetUserVO.setMerchantId(merchant.getId());
        merchantPlaceAndCabinetUserVO.setPlaceList(CollectionUtils.isEmpty(placeList) ? Collections.emptyList() : placeList);
        merchantPlaceAndCabinetUserVO.setCabinetList(CollectionUtils.isEmpty(cabinetList) ? Collections.emptyList() : cabinetList);
        
        return merchantPlaceAndCabinetUserVO;
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetVO> listCabinetByPlaceId(Long uid, Long placeId) {
        Merchant merchant = merchantService.queryByUid(uid);
        if (Objects.isNull(merchant)) {
            log.warn("Merchant power for pro listCabinetByPlaceId, merchant not exist, uid={}", uid);
            return null;
        }
        
        List<MerchantPlaceBind> bindList = merchantPlaceBindService.listByMerchantId(merchant.getId(), null);
        if (CollectionUtils.isEmpty(bindList)) {
            return null;
        }
        
        // 判断所选场地是否存在
        Set<Long> placeIdSet = bindList.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(placeIdSet) && !placeIdSet.contains(placeId)) {
            return null;
        }
        
        // 获取柜机列表
        List<MerchantPlaceCabinetVO> cabinetList = new ArrayList<>();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBindList = merchantPlaceCabinetBindService.listByPlaceIds(Set.of(placeId));
        if (CollectionUtils.isNotEmpty(merchantPlaceCabinetBindList)) {
            Set<Long> cabinetIdSet = merchantPlaceCabinetBindList.stream().map(MerchantPlaceCabinetBind::getCabinetId).collect(Collectors.toSet());
            cabinetIdSet.forEach(cabinetId -> {
                MerchantPlaceCabinetVO merchantPlaceCabinetVO = new MerchantPlaceCabinetVO();
                merchantPlaceCabinetVO.setCabinetId(cabinetId);
                merchantPlaceCabinetVO.setCabinetName(
                        Optional.ofNullable(electricityCabinetService.queryByIdFromCache(cabinetId.intValue())).orElse(new ElectricityCabinet()).getName());
                
                cabinetList.add(merchantPlaceCabinetVO);
            });
        }
        
        if (CollectionUtils.isEmpty(cabinetList)) {
            return Collections.emptyList();
        }
        
        return cabinetList;
    }
    
    @Slave
    @Override
    public List<Long> getStaticsCabinetIds(MerchantCabinetPowerRequest request) {
        Long merchantId = request.getMerchantId();
        if (Objects.isNull(merchantId)) {
            return Collections.emptyList();
        }
        
        Long placeId = request.getPlaceId();
        Long cabinetId = request.getCabinetId();
        
        // 设置key
        String key = CacheConstant.MERCHANT_PLACE_CABINET_SEARCH_LOCK + merchantId;
        if (Objects.nonNull(placeId)) {
            key = key + placeId;
            if (Objects.nonNull(cabinetId)) {
                key = key + cabinetId;
            }
        }
        
        // 先从缓存获取，如果未获取到再从数据库获取
        List<Long> cabinetIdList = null;
        String cabinetIdStr = redisService.get(key);
        if (StringUtils.isNotBlank(cabinetIdStr)) {
            return JsonUtil.fromJsonArray(cabinetIdStr, Long.class);
        }
        
        // 1.场地和柜机为null，查全量
        if (Objects.isNull(placeId) && Objects.isNull(cabinetId)) {
            MerchantPlaceAndCabinetUserVO merchantPlaceAndCabinetUserVO = this.listPlaceAndCabinetByMerchantId(merchantId);
            if (Objects.isNull(merchantPlaceAndCabinetUserVO) || CollectionUtils.isEmpty(merchantPlaceAndCabinetUserVO.getCabinetList())) {
                return Collections.emptyList();
            }
            
            List<MerchantPlaceCabinetVO> cabinetList = merchantPlaceAndCabinetUserVO.getCabinetList();
            cabinetIdList = cabinetList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 2.场地不为null，柜机为null
        if (Objects.nonNull(placeId) && Objects.isNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(merchantId, placeId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
        }
        
        // 3. 场地不为null,柜机不为null
        if (Objects.nonNull(placeId) && Objects.nonNull(cabinetId)) {
            List<MerchantPlaceCabinetVO> placeCabinetVOList = this.listCabinetByPlaceId(merchantId, merchantId);
            cabinetIdList = placeCabinetVOList.stream().map(MerchantPlaceCabinetVO::getCabinetId).distinct().collect(Collectors.toList());
            
            if (CollectionUtils.isNotEmpty(cabinetIdList) && cabinetIdList.contains(cabinetId)) {
                cabinetIdList = List.of(cabinetId);
            }
        }
        
        // 存入缓存
        redisService.saveWithString(key, cabinetIdList, 3L, TimeUnit.SECONDS);
        
        return cabinetIdList;
    }
    
}

@Slf4j
class CabinetPowerProRunnable implements Callable<List<MerchantPowerDetailVO>> {
    
    Long eid;
    
    ElectricityCabinetService electricityCabinetService;
    
    MerchantPlaceBindService merchantPlaceBindService;
    
    ElePowerService elePowerService;
    
    Long startTime;
    
    Long endTime;
    
    List<MerchantPlaceCabinetBind> bindList;
    
    List<MerchantPlaceCabinetBind> unbindList;
    
    List<MerchantPlaceBind> placeBindList;
    
    public CabinetPowerProRunnable(Long eid, ElectricityCabinetService electricityCabinetService, MerchantPlaceBindService merchantPlaceBindService,
            ElePowerService elePowerService, Long startTime, Long endTime, List<MerchantPlaceCabinetBind> bindList, List<MerchantPlaceCabinetBind> unbindList,
            List<MerchantPlaceBind> placeBindList) {
        this.eid = eid;
        this.electricityCabinetService = electricityCabinetService;
        this.merchantPlaceBindService = merchantPlaceBindService;
        this.elePowerService = elePowerService;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bindList = bindList;
        this.unbindList = unbindList;
        this.placeBindList = placeBindList;
    }
    
    @Override
    public List<MerchantPowerDetailVO> call() throws Exception {
        List<MerchantPowerDetailVO> powerVOList = new ArrayList<>();
        
        // 1.只有绑定状态记录
        if (CollectionUtils.isNotEmpty(bindList) && CollectionUtils.isEmpty(unbindList)) {
            MerchantPlaceCabinetBind bindRecord = bindList.get(NumberConstant.ZERO);
            Long placeId = bindRecord.getPlaceId();
            Long bindTime = bindRecord.getBindTime();
            
            if (bindTime > startTime) {
                startTime = bindTime;
            }
            
            Integer placeMerchantBindStatus = MerchantPlaceBindConstant.UN_BIND;
            // 需要统计场地和柜机的时间段为：startTime-endTime，判断该时间段的数据都是在哪些商户下产生的
            for (MerchantPlaceBind placeBind : placeBindList) {
                Long placeBindTime = placeBind.getBindTime();
                Long placeUnbindTime = placeBind.getUnBindTime();
                
                Long merchantId = placeBind.getMerchantId();
                
                // 绑定状态没有解绑时间，设置解绑时间endTime
                if (Objects.equals(placeBind.getType(), MerchantPlaceBindConstant.BIND)) {
                    placeUnbindTime = endTime;
                    placeMerchantBindStatus = MerchantPlaceBindConstant.BIND;
                }
                
                // 查询并处理数据
                MerchantPowerDetailVO eleSumPowerVO = handleTimeInterval(startTime, endTime, placeBindTime, placeUnbindTime, placeId, merchantId, placeMerchantBindStatus);
                if (Objects.nonNull(eleSumPowerVO)) {
                    powerVOList.add(eleSumPowerVO);
                }
            }
        }
        
        // 2. 只有解绑状态记录
        if (CollectionUtils.isEmpty(bindList) && CollectionUtils.isNotEmpty(unbindList)) {
            // 根据bindTime进行从小到大排序
            unbindList.sort(Comparator.comparing(MerchantPlaceCabinetBind::getBindTime));
            
            //时间段无交集，但可能会有子集，去除子集
            List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
            for (MerchantPlaceCabinetBind current : unbindList) {
                // 当前时间段的开始时间大于结果集最后一个时间段的结束时间
                if (resultList.isEmpty() || current.getBindTime().compareTo(resultList.get(resultList.size() - 1).getUnBindTime()) > 0) {
                    resultList.add(current);
                    
                    Integer placeMerchantBindStatus = MerchantPlaceBindConstant.UN_BIND;
                    
                    // 需要统计场地和柜机的时间段为：current.getBindTime()-current.getUnBindTime()，判断该时间段的数据都是在哪些商户下产生的
                    for (MerchantPlaceBind placeBind : placeBindList) {
                        Long placeBindTime = placeBind.getBindTime();
                        Long placeUnbindTime = placeBind.getUnBindTime();
                        
                        // 绑定状态没有解绑时间，设置解绑时间endTime
                        if (Objects.equals(placeBind.getType(), MerchantPlaceBindConstant.BIND)) {
                            placeUnbindTime = endTime;
                            placeMerchantBindStatus = MerchantPlaceBindConstant.BIND;
                        }
                        
                        // 查询并处理数据
                        MerchantPowerDetailVO eleSumPowerVO = handleTimeInterval(current.getBindTime(), current.getUnBindTime(), placeBindTime, placeUnbindTime,
                                current.getPlaceId(), placeBind.getMerchantId(), placeMerchantBindStatus);
                        if (Objects.nonNull(eleSumPowerVO)) {
                            powerVOList.add(eleSumPowerVO);
                        }
                    }
                }
            }
        }
        
        // 3. 既有绑定状态记录，又有解绑状态记录
        if (CollectionUtils.isNotEmpty(bindList) && CollectionUtils.isNotEmpty(unbindList)) {
            MerchantPlaceCabinetBind bindRecord = bindList.get(NumberConstant.ZERO);
            // 由于绑定状态的记录没有解绑时间，此处设置个虚拟的解绑时间方便进行时间段排序及去除子集处理
            bindRecord.setUnBindTime(endTime);
            
            // 如果绑定状态记录的createTime在startTime前，设置bindTime为startTime
            if (bindRecord.getCreateTime() <= startTime) {
                bindRecord.setBindTime(startTime);
            }
            
            // 合并-排序-去除子集
            List<MerchantPlaceCabinetBind> allBindList = new ArrayList<>();
            allBindList.add(bindRecord);
            allBindList.addAll(unbindList);
            
            //时间段无交集，但可能会有子集，去除子集
            List<MerchantPlaceCabinetBind> resultList = new ArrayList<>();
            for (MerchantPlaceCabinetBind current : allBindList) {
                // 当前时间段的开始时间大于结果集最后一个时间段的结束时间
                if (resultList.isEmpty() || current.getBindTime().compareTo(resultList.get(resultList.size() - 1).getUnBindTime()) > 0) {
                    resultList.add(current);
                    
                    Integer placeMerchantBindStatus = MerchantPlaceBindConstant.UN_BIND;
                    
                    // 需要统计场地和柜机的时间段为：current.getBindTime()-current.getUnBindTime()，判断该时间段的数据都是在哪些商户下产生的
                    for (MerchantPlaceBind placeBind : placeBindList) {
                        Long placeBindTime = placeBind.getBindTime();
                        Long placeUnbindTime = placeBind.getUnBindTime();
                        
                        // 绑定状态没有解绑时间，设置解绑时间endTime
                        if (Objects.equals(placeBind.getType(), MerchantPlaceBindConstant.BIND)) {
                            placeUnbindTime = endTime;
                            placeMerchantBindStatus = MerchantPlaceBindConstant.BIND;
                        }
                        
                        // 查询并处理数据
                        MerchantPowerDetailVO eleSumPowerVO = handleTimeInterval(current.getBindTime(), current.getUnBindTime(), placeBindTime, placeUnbindTime,
                                current.getPlaceId(), placeBind.getMerchantId(), placeMerchantBindStatus);
                        if (Objects.nonNull(eleSumPowerVO)) {
                            powerVOList.add(eleSumPowerVO);
                        }
                    }
                }
            }
        }
        
        return powerVOList;
    }
    
    /**
     * 处理场地-柜机和商户-场地 的时间交集
     */
    private MerchantPowerDetailVO handleTimeInterval(Long startTime, Long endTime, Long placeBindTime, Long placeUnbindTime, Long placeId, Long merchantId,
            Integer placeMerchantBindStatus) {
        MerchantPowerDetailVO eleSumPowerVO = null;
        
        Integer cabinetMerchantBindStatus = MerchantPlaceBindConstant.UN_BIND;
        if (Objects.equals(placeMerchantBindStatus, MerchantPlaceBindConstant.BIND)) {
            cabinetMerchantBindStatus = MerchantPlaceBindConstant.BIND;
        }
        
        if (placeBindTime <= startTime && placeUnbindTime >= endTime) {
            eleSumPowerVO = handlePowerDetail(startTime, endTime, eid, placeId, merchantId, cabinetMerchantBindStatus);
        }
        
        if (placeBindTime <= startTime && placeUnbindTime < endTime) {
            eleSumPowerVO = handlePowerDetail(startTime, placeUnbindTime, eid, placeId, merchantId, cabinetMerchantBindStatus);
        }
        
        if (placeBindTime > startTime && placeUnbindTime >= endTime) {
            eleSumPowerVO = handlePowerDetail(placeBindTime, endTime, eid, placeId, merchantId, cabinetMerchantBindStatus);
        }
        
        if (placeBindTime > startTime && placeUnbindTime < endTime) {
            eleSumPowerVO = handlePowerDetail(placeBindTime, placeUnbindTime, eid, placeId, merchantId, cabinetMerchantBindStatus);
        }
        
        return eleSumPowerVO;
    }
    
    /**
     * 查询并处理数据
     */
    private MerchantPowerDetailVO handlePowerDetail(Long startTime, Long endTime, Long eid, Long placeId, Long merchantId, Integer cabinetMerchantBindStatus) {
        //查询柜机
        ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(eid.intValue());
        
        // 查询电量
        EleSumPowerVO eleSumPowerVO = elePowerService.listByCondition(startTime, endTime, List.of(eid), cabinet.getTenantId());
        
        // 查询最新一条记录的reportTime，用于排序
        Long latestTime = elePowerService.queryLatestReportTime(startTime, endTime, List.of(eid), cabinet.getTenantId());
        
        return MerchantPowerDetailVO.builder().eid(eid).power(eleSumPowerVO.getSumPower()).charge(eleSumPowerVO.getSumCharge()).latestTime(latestTime).startTime(startTime)
                .endTime(endTime).placeId(placeId).merchantId(merchantId).cabinetMerchantBindStatus(cabinetMerchantBindStatus).build();
        
    }
}
