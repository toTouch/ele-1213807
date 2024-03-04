package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceBindConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceCabinetBindConstant;
import com.xiliulou.electricity.constant.merchant.RebateRecordConstant;
import com.xiliulou.electricity.dto.merchant.MerchantPlaceCabinetBindDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindHistory;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonth;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthDetail;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeDailyRecordMapper;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetBindHistoryService;
import com.xiliulou.electricity.service.merchant.MerchantCabinetBindTimeService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthDetailService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceMapService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetFeeDetailShowVO;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetFeeDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetFeeDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeCurMonthVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeLineDataVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/2/25 10:28
 * @desc
 */
@Service("MerchantPlaceFeeService")
@Slf4j
public class MerchantPlaceFeeServiceImpl implements MerchantPlaceFeeService {
    
    @Resource
    private MerchantPlaceBindService merchantPlaceBindService;
    
    @Resource
    private MerchantPlaceMapService merchantPlaceMapService;
    
    @Resource
    private MerchantPlaceFeeMonthService merchantPlaceFeeMonthService;
    
    @Resource
    private MerchantCabinetBindHistoryService merchantCabinetBindHistoryService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private MerchantService merchantService;
    
    private String dateFormat = "yyyy-MM-dd";
    
    @Resource
    private MerchantPlaceFeeDailyRecordMapper merchantPlaceFeeDailyRecordMapper;
    
    @Resource
    private MerchantPlaceFeeMonthRecordService merchantPlaceFeeMonthRecordService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    @Resource
    private MerchantPlaceFeeMonthDetailService merchantPlaceFeeMonthDetailService;
    
    @Resource
    private MerchantCabinetBindTimeService merchantCabinetBindTimeService;
    
    
    /**
     * 判断商户绑定的柜机是否存在场地费
     *
     * @param merchantId
     * @return
     */
    @Slave
    @Override
    public Integer isShowPlacePage(Long merchantId) {
        // 判断商户表的标志是否存在场地费
        Merchant merchant = merchantService.queryByIdFromCache(merchantId);
        if (Objects.equals(merchant.getExistPlaceFee(), MerchantConstant.EXISTS_PLACE_FEE_YES)) {
            return MerchantConstant.EXISTS_PLACE_FEE_YES;
        }
        
        return MerchantConstant.EXISTS_PLACE_FEE_NO;
    }
    
    @Slave
    @Override
    public MerchantPlaceFeeCurMonthVO getFeeData(MerchantPlaceFeeRequest request) {
        request.setMerchantId(42L);
        MerchantPlaceFeeCurMonthVO merchantPlaceFeeCurMonthVO = new MerchantPlaceFeeCurMonthVO();
        // 计算上个月一号到当前场地费的总和
        // 获取上个月的场地费
        BigDecimal lastMothFee = getLastMothFee(request);
        merchantPlaceFeeCurMonthVO.setLastMonthFee(lastMothFee);
        
        // 获取本月的场地费用
        BigDecimal curMothFee = getCurMothFee(request);
        merchantPlaceFeeCurMonthVO.setCurrentMonthFee(curMothFee);
        
        // 计算累计场地费 上月之前的月份+上月+本月
        // 上月的第一天
        long time = DateUtils.getBeforeMonthFirstDayTimestamp(MerchantPlaceBindConstant.LAST_MONTH);
        BigDecimal sumFeeHistory = merchantPlaceFeeMonthService.sumFeeByTime(request.getMerchantId(), request.getPlaceId(), request.getCabinetId(), time);
        
        if (ObjectUtils.isEmpty(sumFeeHistory)) {
            sumFeeHistory = BigDecimal.ZERO;
        }
        
        BigDecimal sumFee = sumFeeHistory.add(curMothFee).add(lastMothFee);
        merchantPlaceFeeCurMonthVO.setMonthFee(sumFee);
        
        return merchantPlaceFeeCurMonthVO;
    }
    
    /**
     * 商户场地费折线图
     *
     * @param request
     * @return
     */
    @Override
    public MerchantPlaceFeeLineDataVO lineData(MerchantPlaceFeeRequest request) {
        request.setMerchantId(42L);
        
        MerchantPlaceFeeLineDataVO vo = new MerchantPlaceFeeLineDataVO();
        
        // 计算月份
        List<String> xDataList = getMonthList(request.getStartTime(), request.getEndTime());
        vo.setXDataList(xDataList);
        
        // 为空提前返回
        if (ObjectUtils.isEmpty(xDataList)) {
            vo.setYDataList(Collections.emptyList());
            return vo;
        }
        
        // 计算上个月的月份
        long lastMonthFistDay = DateUtils.getBeforeMonthFirstDayTimestamp(MerchantPlaceBindConstant.LAST_MONTH);
        String lastMoth = DateUtil.format(new Date(lastMonthFistDay), "yyyy-MM");
        
        // 计算上个月的数据
        BigDecimal lastMothFee = getLastMothFee(request);
        
        // 从历史月结账单中统计出对应月份的数据
        List<MerchantPlaceFeeMonth> placeFeeMonths = merchantPlaceFeeMonthService.queryListByMonth(request.getPlaceId(), request.getCabinetId(), xDataList);
        Map<String, BigDecimal> placeMap = new HashMap<>();
        
        if (ObjectUtils.isNotEmpty(placeFeeMonths)) {
            placeMap = placeFeeMonths.stream().collect(Collectors.toMap(MerchantPlaceFeeMonth::getCalculateMonth, MerchantPlaceFeeMonth::getPlaceFee, (key, key1) -> key1));
        }
        
        placeMap.put(lastMoth, lastMothFee);
        
        List<BigDecimal> yDataList = new ArrayList<>();
        Map<String, BigDecimal> finalPlaceMap = placeMap;
        
        xDataList.forEach(item -> {
            if (ObjectUtils.isNotEmpty(finalPlaceMap.get(item))) {
                yDataList.add(finalPlaceMap.get(item));
            } else {
                yDataList.add(BigDecimal.ZERO);
            }
        });
        
        vo.setYDataList(yDataList);
        
        return vo;
    }
    
    /**
     * 场地费详情
     *
     * @param request
     * @return
     */
    @Override
    public MerchantCabinetFeeDetailShowVO getCabinetPlaceDetail(MerchantPlaceFeeRequest request) {
        request.setMerchantId(42L);
        MerchantCabinetFeeDetailShowVO resVo = new MerchantCabinetFeeDetailShowVO();
        
        // 根据商户id查询所有的柜机的id
        List<Long> cabinetIdList = merchantPlaceFeeMonthService.selectCabinetIdByMerchantId(request.getMerchantId());
        List<MerchantPlaceFeeMonth> feeMonthsHistory = merchantPlaceFeeMonthService.queryListByMerchantId(request.getMerchantId(), request.getCabinetId(), request.getPlaceId());
        log.info("getCabinetPlaceDetail={}", feeMonthsHistory);
        
        Map<Long, BigDecimal> feeMonthsHistoryMap = new HashMap<>();
        
        if (ObjectUtils.isNotEmpty(feeMonthsHistory)) {
            feeMonthsHistoryMap = feeMonthsHistory.stream()
                    .collect(Collectors.groupingBy(MerchantPlaceFeeMonth::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.sumHistoryFee(e))));
        }
        
        // 查询上月
        List<MerchantPlaceFeeMonthDetail> lastMonthFeeRecords = getLastMonthFeeRecords(request);
        log.info("getCabinetPlaceDetail1={}", lastMonthFeeRecords);
    
        // 添加场地过滤条件
        if (Objects.nonNull(request.getPlaceId())) {
            lastMonthFeeRecords = lastMonthFeeRecords.stream().filter(item -> Objects.equals(item.getPlaceId(), request.getPlaceId())).collect(Collectors.toList());
        }
        
        // 添加柜机过滤条件
        if (Objects.nonNull(request.getCabinetId())) {
            lastMonthFeeRecords = lastMonthFeeRecords.stream().filter(item -> Objects.equals(item.getCabinetId(), request.getCabinetId())).collect(Collectors.toList());
        }
        
        Map<Long, BigDecimal> lastMonthCabinetFeeMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(lastMonthFeeRecords)) {
            List<Long> lastMonthCabinetIdList = lastMonthFeeRecords.stream().map(MerchantPlaceFeeMonthDetail::getCabinetId).collect(Collectors.toList());
            
            if (ObjectUtils.isNotEmpty(lastMonthCabinetIdList)) {
                cabinetIdList.addAll(lastMonthCabinetIdList);
            }
            
            lastMonthCabinetFeeMap = lastMonthFeeRecords.stream()
                    .collect(Collectors.groupingBy(MerchantPlaceFeeMonthDetail::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.sumFee(e))));
        }
        
        // 查询本月的
        List<MerchantPlaceFeeMonthDetail> curMothFeeRecords = getCurMonthFeeRecords(request);
        log.info("getCabinetPlaceDetail2={}", curMothFeeRecords);
    
        if (Objects.nonNull(request.getPlaceId())) {
            curMothFeeRecords = curMothFeeRecords.stream().filter(item -> Objects.equals(item.getPlaceId(), request.getPlaceId())).collect(Collectors.toList());
        }
        
        if (Objects.nonNull(request.getCabinetId())) {
            curMothFeeRecords = curMothFeeRecords.stream().filter(item -> Objects.equals(item.getCabinetId(), request.getCabinetId())).collect(Collectors.toList());
        }
        
        Map<Long, BigDecimal> curMonthCabinetFeeMap = new HashMap<>();
        
        // 本月数据处理
        if (ObjectUtils.isNotEmpty(curMothFeeRecords)) {
            List<Long> curMonthCabinetIdList = curMothFeeRecords.stream().map(MerchantPlaceFeeMonthDetail::getCabinetId).distinct().collect(Collectors.toList());
            
            if (ObjectUtils.isNotEmpty(curMonthCabinetIdList)) {
                cabinetIdList.addAll(curMonthCabinetIdList);
            }
            
            // 根据柜机分组 然后统计每个柜机的场地费的总共和
            curMonthCabinetFeeMap = curMothFeeRecords.stream()
                    .collect(Collectors.groupingBy(MerchantPlaceFeeMonthDetail::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.sumFee(e))));
        }
    
        log.info("getCabinetPlaceDetail3={}", cabinetIdList);
        // 历史账单，本月，上月都没有数据则返回空
        if (ObjectUtils.isEmpty(cabinetIdList)) {
            resVo.setCabinetCount(NumberConstant.ZERO);
            resVo.setCabinetFeeDetailList(Collections.emptyList());
            
            return resVo;
        }
        
        // 去重
        List<Long> cabinetIds = cabinetIdList.stream().distinct().collect(Collectors.toList());
        
        List<MerchantPlaceCabinetFeeDetailVO> resList = new ArrayList<>();
        Map<Long, BigDecimal> finalCurMonthCabinetFeeMap = curMonthCabinetFeeMap;
        Map<Long, BigDecimal> finalFeeMonthsHistoryMap = feeMonthsHistoryMap;
        Map<Long, BigDecimal> finalLastMonthCabinetFeeMap = lastMonthCabinetFeeMap;
    
        log.info("getCabinetPlaceDetail4,a={},b={},c={}", finalCurMonthCabinetFeeMap, finalFeeMonthsHistoryMap, finalLastMonthCabinetFeeMap);
        cabinetIds.forEach(cabinetId -> {
            MerchantPlaceCabinetFeeDetailVO vo = new MerchantPlaceCabinetFeeDetailVO();
            ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(cabinetId.intValue());
            
            // 柜机名称和柜机的创建时间
            if (Objects.nonNull(cabinet)) {
                vo.setCabinetName(cabinet.getName());
                vo.setTime(cabinet.getCreateTime());
            }
            
            vo.setCabinetId(cabinetId);
            
            // 设置本月的场地费
            BigDecimal currentMonthFee = BigDecimal.ZERO;
            
            if (ObjectUtils.isNotEmpty(finalCurMonthCabinetFeeMap.get(cabinetId))) {
                currentMonthFee = finalCurMonthCabinetFeeMap.get(cabinetId);
            }
            
            BigDecimal historyFee = BigDecimal.ZERO;
            // 设置累加的场地费
            if (ObjectUtils.isNotEmpty(finalFeeMonthsHistoryMap.get(cabinetId))) {
                historyFee = historyFee.add(finalFeeMonthsHistoryMap.get(cabinetId));
            }
            
            if (ObjectUtils.isNotEmpty(finalLastMonthCabinetFeeMap.get(cabinetId))) {
                historyFee = historyFee.add(finalLastMonthCabinetFeeMap.get(cabinetId));
            }
    
            vo.setCurrentMonthFee(currentMonthFee);
    
            historyFee = historyFee.add(currentMonthFee);
            vo.setMonthFeeSum(historyFee);
            
            resList.add(vo);
        });
        
        resList.stream().sorted(Comparator.comparing(MerchantPlaceCabinetFeeDetailVO::getTime).reversed());
        
        resVo.setCabinetFeeDetailList(resList);
        resVo.setCabinetCount(resList.size());
        
        return resVo;
    }
    
    @Override
    public List<MerchantCabinetFeeDetailVO> getPlaceDetailByCabinetId(MerchantPlaceFeeRequest request) {
        request.setMerchantId(42L);
        List<MerchantCabinetFeeDetailVO> resList = new ArrayList<>();
        // 获取当前月份
        String currentMonth = DateUtil.format(new Date(), "yyyy-MM");
        
        String today = DateUtil.format(new Date(), "yyyy-MM-dd hh:mm");
        if (Objects.equals(currentMonth, request.getMonth())) {
            List<MerchantPlaceFeeMonthDetail> curMothFeeRecords = getCurMonthFeeRecords(request);
            log.info("getPlaceDetailByCabinetId={}", curMothFeeRecords);
            if (ObjectUtils.isEmpty(curMothFeeRecords)) {
                return Collections.emptyList();
            }
            
            if (ObjectUtils.isNotEmpty(curMothFeeRecords)) {
                for (MerchantPlaceFeeMonthDetail placeFeeMonthDetail : curMothFeeRecords) {
                    MerchantCabinetFeeDetailVO vo = new MerchantCabinetFeeDetailVO();
                    vo.setPlaceFee(placeFeeMonthDetail.getPlaceFee());
                    MerchantPlace merchantPlace = merchantPlaceService.queryByIdFromCache(placeFeeMonthDetail.getPlaceId());
                    
                    if (Objects.nonNull(merchantPlace)) {
                        vo.setPlaceName(merchantPlace.getName());
                    }
                    
                    ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(placeFeeMonthDetail.getCabinetId().intValue());
                    if (Objects.nonNull(cabinet)) {
                        vo.setCabinetName(cabinet.getName());
                    }
                    
                    vo.setStartTime(placeFeeMonthDetail.getStartTime());
                    vo.setEndTime(placeFeeMonthDetail.getEndTime());
                    
                    vo.setStatus(placeFeeMonthDetail.getCabinetPlaceBindStatus());
                    
                    // 如果当前状态为绑定状态则将结束时间改为空
                    if (Objects.equals(placeFeeMonthDetail.getCabinetPlaceBindStatus(), MerchantPlaceBindConstant.BIND)) {
                        vo.setEndTime(null);
                    }
                    
                    resList.add(vo);
                }
            }
            return resList;
        }
        
        // 查询历史月份的账单数据
        List<String> monthList = new ArrayList<>();
        monthList.add(request.getMonth());
        List<MerchantCabinetBindHistory> placeFeeMonths = merchantCabinetBindHistoryService.queryListByMonth(request.getCabinetId(), null, monthList);
        log.info("getPlaceDetailByCabinetId1={}", placeFeeMonths);
    
        if (ObjectUtils.isEmpty(placeFeeMonths)) {
            return Collections.emptyList();
        }
        
        if (ObjectUtils.isNotEmpty(placeFeeMonths)) {
            for (MerchantCabinetBindHistory placeFeeMonthDetail : placeFeeMonths) {
                MerchantCabinetFeeDetailVO vo = new MerchantCabinetFeeDetailVO();
                vo.setPlaceFee(placeFeeMonthDetail.getPlaceFee());
                
                MerchantPlace merchantPlace = merchantPlaceService.queryByIdFromCache(placeFeeMonthDetail.getPlaceId());
                if (Objects.nonNull(merchantPlace)) {
                    vo.setPlaceName(merchantPlace.getName());
                }
                
                ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(placeFeeMonthDetail.getCabinetId().intValue());
                if (Objects.nonNull(cabinet)) {
                    vo.setCabinetName(cabinet.getName());
                }
                
                vo.setStartTime(placeFeeMonthDetail.getStartTime());
                vo.setEndTime(placeFeeMonthDetail.getEndTime());
                vo.setStatus(placeFeeMonthDetail.getStatus());
                resList.add(vo);
            }
        }
        
        return resList;
    }
    
    private BigDecimal sumHistoryFee(List<MerchantPlaceFeeMonth> list) {
        AtomicReference<BigDecimal> atomicReference = new AtomicReference<>();
        atomicReference.set(BigDecimal.ZERO);
        
        list.stream().forEach(item -> {
            atomicReference.set(atomicReference.get().add(item.getPlaceFee()));
        });
        
        return atomicReference.get();
    }
    
    private BigDecimal sumFee(List<MerchantPlaceFeeMonthDetail> list) {
        if (ObjectUtils.isEmpty(list)) {
            return BigDecimal.ZERO;
        }
        
        AtomicReference<BigDecimal> atomicReference = new AtomicReference<>();
        atomicReference.set(BigDecimal.ZERO);
        
        list.stream().forEach(item -> {
            atomicReference.set(atomicReference.get().add(item.getPlaceFee()));
        });
        
        return atomicReference.get();
    }
    
    private List<String> getMonthList(Long startTime, Long endTime) {
        if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        calendar.setTimeInMillis(startTime);
        while (true) {
            String month = DateUtil.format(calendar.getTime(), "yyyy-MM");
            list.add(month);
            calendar.add(Calendar.MONTH, 1);
            if (calendar.getTimeInMillis() > endTime) {
                break;
            }
        }
        String endMonth = DateUtil.format(new Date(endTime), "yyyy-MM");
        if (!list.contains(endMonth)) {
            list.add(endMonth);
        }
        return list;
    }
    
    private BigDecimal getCurMothFee(MerchantPlaceFeeRequest request) {
        List<MerchantPlaceFeeMonthDetail> list = getCurMonthFeeRecords(request);
        log.info("getCurMothFee1={}", list);
        
        if (ObjectUtils.isEmpty(list)) {
            return BigDecimal.ZERO;
        }
        
        // 根据场地id进行过滤
        if (Objects.nonNull(request.getPlaceId())) {
            list = list.stream().filter(item -> Objects.equals(item.getPlaceId(), request.getPlaceId())).collect(Collectors.toList());
        }
        
        // 根据柜机id进行过滤
        if (Objects.nonNull(request.getCabinetId())) {
            list = list.stream().filter(item -> Objects.equals(item.getCabinetId(), request.getCabinetId())).collect(Collectors.toList());
        }
        
        AtomicReference<BigDecimal> fee = new AtomicReference<>();
        fee.set(BigDecimal.ZERO);
        list.stream().forEach(item -> {
            if (Objects.nonNull(item.getPlaceFee())) {
                fee.set(fee.get().add(item.getPlaceFee()));
            }
        });
        
        return fee.get();
    }
    
    private List<MerchantPlaceFeeMonthDetail> getCurMonthFeeRecords(MerchantPlaceFeeRequest request) {
        List<MerchantPlaceBind> merchantPlaceBinds = merchantPlaceBindService.queryNoSettleByMerchantId(request.getMerchantId());
        log.info("getCurMonthFeeRecords1={}", merchantPlaceBinds);
        if (ObjectUtils.isEmpty(merchantPlaceBinds)) {
            return Collections.emptyList();
        }
        
        // 排除掉开始时间和结束时间在一天的数据
        List<Long> placeIdList = merchantPlaceBinds.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toList());
        
        // 本月的第一天
        long startTime = DateUtils.getBeforeMonthFirstDayTimestamp(0);
        
        // 当前时间
        long endTime = System.currentTimeMillis();
        
        // 计算当前月份
        String curMonth = DateUtil.format(new Date(), "yyyy-MM");
        // 修改标记id集合
        List<Long> updateIdList = new ArrayList<>();
        List<MerchantPlaceFeeMonthDetail> list = new ArrayList<>();
        
        // 计算当前月份的账单
        List<MerchantPlaceFeeMonthRecord> curPlaceFeeMonthRecords = getCurMonthRecordFirst(placeIdList);
        log.info("getCurMonthFeeRecords2={}", curPlaceFeeMonthRecords);
    
        if (ObjectUtils.isEmpty(curPlaceFeeMonthRecords)) {
            log.info("get current moth place fee records is empty,merchantId={},placeIdList={}", request.getMerchantId(), placeIdList);
            return null;
        }
        
        Map<Long, List<MerchantPlaceFeeMonthRecord>> placeFeeMonthRecordMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(curPlaceFeeMonthRecords)) {
            placeFeeMonthRecordMap = curPlaceFeeMonthRecords.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getPlaceId));
        }
        
        Map<Long, List<MerchantPlaceBind>> placeMap = merchantPlaceBinds.stream().collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : placeMap.entrySet()) {
            
            Long merchantId = request.getMerchantId();
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> value = entry.getValue();
            
            // 处理连续的时间段
            value = dealSameRecord(value);
            
            List<MerchantPlaceFeeMonthRecord> cabinetRecordList = placeFeeMonthRecordMap.get(placeId);
            log.info("getCurMonthFeeRecords3={}", cabinetRecordList);
            if (ObjectUtils.isEmpty(cabinetRecordList)) {
                log.info("current month Fee records cabinet map is empty, merchantId={},placeId={}, curMonth={}", merchantId, placeId, curMonth);
                continue;
            }
            log.info("getCurMonthFeeRecords33={}", value);
            // 根据柜机id进行分组统计
            Map<Long, List<MerchantPlaceFeeMonthRecord>> cabinetMap = cabinetRecordList.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getEid));
            log.info("getCurMonthFeeRecords4={}", cabinetMap);
            for (MerchantPlaceBind bind : value) {
                Long bindStartTime = null;
                Long bindEndTime = null;
                // 绑定 开始时间必须小于等于本月的最后一天
                if (Objects.equals(bind.getType(), MerchantPlaceBindConstant.BIND) && Objects.nonNull(bind.getBindTime()) && bind.getBindTime() <= endTime) {
                    if (bind.getBindTime() <= startTime) {
                        bindStartTime = startTime;
                    } else {
                        bindStartTime = bind.getBindTime();
                    }
                    bindEndTime = endTime;
                }
                
                // 解绑 开始时间小于本月月末  结束时间大于本月月初
                if (Objects.equals(bind.getType(), MerchantPlaceBindConstant.UN_BIND) && Objects.nonNull(bind.getBindTime()) && Objects.nonNull(bind.getUnBindTime())
                        && bind.getBindTime() <= endTime && bind.getUnBindTime() >= startTime) {
                    if (bind.getBindTime() <= startTime) {
                        bindStartTime = startTime;
                    } else {
                        bindStartTime = bind.getBindTime();
                    }
                    
                    if (bind.getUnBindTime() <= endTime) {
                        bindEndTime = bind.getUnBindTime();
                        updateIdList.add(bind.getId());
                    } else {
                        bindEndTime = endTime;
                    }
                }
                
                if (Objects.isNull(bindStartTime) || Objects.isNull(bindEndTime)) {
                    log.info("merchant place fee month detail handler time is invalid id={}", bind.getId());
                    continue;
                }
                log.info("getCurMonthFeeRecords5, bindStartTime={}, bindEndTime={}", bindStartTime, bindEndTime);
                // 判断场地的时间是否与柜机的时间存在重叠
                for (Map.Entry<Long, List<MerchantPlaceFeeMonthRecord>> cabinetEntry : cabinetMap.entrySet()) {
                    Long cabinetId = cabinetEntry.getKey();
                    List<MerchantPlaceFeeMonthRecord> cabinetDetail = cabinetEntry.getValue();
                    for (MerchantPlaceFeeMonthRecord cabinetRecord : cabinetDetail) {
                        if (bindStartTime > cabinetRecord.getRentEndTime() || bindEndTime < cabinetRecord.getRentStartTime()) {
                            log.info("getCurMonthFeeRecords56, bindStartTime={}, bindEndTime={}, placeId={}, cabinetId={}, cabinetRecord={}", bindStartTime, bindEndTime, placeId, cabinetId, cabinetRecord);
                            continue;
                        }
                        Long cabinetStartTime = null;
                        Long cabinetEndTime = null;
                        // 场地的绑定的开始时间
                        if (bindStartTime <= cabinetRecord.getRentStartTime()) {
                            cabinetStartTime = cabinetRecord.getRentStartTime();
                        } else {
                            cabinetStartTime = bindStartTime;
                        }
                        
                        // 场地绑定的结束时间
                        if (bindEndTime <= cabinetRecord.getRentEndTime()) {
                            cabinetEndTime = bindEndTime;
                        } else {
                            cabinetEndTime = cabinetRecord.getRentEndTime();
                        }
                        
                        log.info("getCurMonthFeeRecords56, bindStartTime={}, bindEndTime={}, placeId={}, cabinetId={}", cabinetStartTime, cabinetEndTime, placeId, cabinetId);
                        if (Objects.isNull(cabinetStartTime) || Objects.isNull(cabinetEndTime)) {
                            continue;
                        }
                        
                        MerchantPlaceFeeMonthDetail merchantPlaceFeeMonthDetail = MerchantPlaceFeeMonthDetail.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId)
                                .tenantId(cabinetRecord.getTenantId()).startTime(cabinetStartTime).endTime(cabinetEndTime).build();
    
                        merchantPlaceFeeMonthDetail.setCabinetPlaceBindStatus(MerchantPlaceBindConstant.UN_BIND);
                        
                        // 商户和场地处于绑定，场地和柜机处于绑定则意味着当前记录为绑定
                        if (Objects.equals(bind.getType(), MerchantPlaceBindConstant.BIND) && Objects.equals(cabinetRecord.getCabinetPlaceBindStatus(), MerchantPlaceBindConstant.BIND)) {
                            merchantPlaceFeeMonthDetail.setCabinetPlaceBindStatus(MerchantPlaceBindConstant.BIND);
                        }
                        
                        // 计算开始和结束时间的场地费的总和
                        BigDecimal feeSum = merchantPlaceFeeDailyRecordMapper.selectList(cabinetStartTime, cabinetEndTime, cabinetId);
                        
                        if (Objects.isNull(feeSum)) {
                            feeSum = BigDecimal.ZERO;
                        }
                        merchantPlaceFeeMonthDetail.setPlaceFee(feeSum);
                        list.add(merchantPlaceFeeMonthDetail);
                    }
                }
            }
        }
        log.info("getCurMonthFeeRecords55={}", list);
        return list;
    }
    
    private List<MerchantPlaceFeeMonthRecord> getCurMonthRecordFirst(List<Long> placeIdList) {
        List<MerchantPlaceFeeMonthRecord> list = new ArrayList<>();
        // 获取柜机绑定记录表
        
        // 获取本月第一天的时间戳
        long dayOfMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(0);
        
        // 获取当前时间戳
        long dayOfMonthEndTime = System.currentTimeMillis();
        
        String settleDate = new SimpleDateFormat("yyyy-MM").format(new Date(dayOfMonthStartTime));
        // 获取场地的柜机绑定记录
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.queryListByPlaceId(placeIdList,
                MerchantPlaceCabinetBindConstant.PLACE_MONTH_NOT_SETTLEMENT);
        log.info("getCurMonthRecordFirst1={}", cabinetBindList);
        if (ObjectUtils.isEmpty(cabinetBindList)) {
            return list;
        }
        
        // 根据柜机的场地进行分组
        Map<Long, List<MerchantPlaceCabinetBind>> placeCabinetBindMap = cabinetBindList.stream().collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getPlaceId));
        placeCabinetBindMap.forEach((placeId, bindList) -> {
            if (ObjectUtils.isNotEmpty(bindList)) {
                // 获取场地的费用记录
                List<MerchantPlaceCabinetBindDTO> placeCabinetBindList = buildBindStatusRecordFirst(bindList, dayOfMonthStartTime, dayOfMonthEndTime);
                log.info("getCurMonthRecordFirst2={}", placeCabinetBindList);
                if (ObjectUtils.isNotEmpty(placeCabinetBindList)) {
                    AtomicReference<Long> atomicReference = new AtomicReference();
                    atomicReference.set(0L);
                    placeCabinetBindList.forEach(cabinetBind -> {
                        MerchantPlaceFeeMonthRecord record = new MerchantPlaceFeeMonthRecord();
                        atomicReference.set(atomicReference.get() + 1L);
                        record.setId(System.currentTimeMillis() + atomicReference.get());
                        record.setMonthDate(settleDate);
                        record.setPlaceId(cabinetBind.getPlaceId());
                        record.setCabinetPlaceBindStatus(cabinetBind.getStatus());
                        record.setEid(cabinetBind.getCabinetId());
                        record.setRentStartTime(cabinetBind.getBindTime());
                        record.setRentEndTime(cabinetBind.getUnBindTime());
                        if (Objects.nonNull(cabinetBind.getUnBindTime())) {
                            Integer days = (int) ((cabinetBind.getUnBindTime() - cabinetBind.getBindTime()) / (24 * 60 * 60 * 1000));
                            record.setRentDays(days);
                        } else {
                            Integer days = (int) ((dayOfMonthEndTime - cabinetBind.getBindTime()) / (24 * 60 * 60 * 1000));
                            record.setRentDays(days);
                        }
                        record.setRentDays(cabinetBind.getMonthSettlement());
                        record.setCreateTime(System.currentTimeMillis());
                        record.setUpdateTime(System.currentTimeMillis());
                        list.add(record);
                    });
                }
            }
            
        });
        
        return list;
    }
    
    
    
    private BigDecimal getLastMothFee(MerchantPlaceFeeRequest request) {
        List<MerchantPlaceFeeMonthDetail> list = getLastMonthFeeRecords(request);
        log.info("get Last Moth Fee list={}", list);
        if (ObjectUtils.isEmpty(list)) {
            return BigDecimal.ZERO;
        }
        
        // 根据场地id进行过滤
        if (Objects.nonNull(request.getPlaceId())) {
            list = list.stream().filter(item -> Objects.equals(item.getPlaceId(), request.getPlaceId())).collect(Collectors.toList());
        }
        
        // 根据柜机id进行过滤
        if (Objects.nonNull(request.getCabinetId())) {
            list = list.stream().filter(item -> Objects.equals(item.getCabinetId(), request.getCabinetId())).collect(Collectors.toList());
        }
        
        AtomicReference<BigDecimal> fee = new AtomicReference<>();
        fee.set(BigDecimal.ZERO);
        list.stream().forEach(item -> {
            if (Objects.nonNull(item.getPlaceFee())) {
                fee.set(fee.get().add(item.getPlaceFee()));
            }
        });
        
        return fee.get();
    }
    
    private List<MerchantPlaceFeeMonthDetail> getLastMonthFeeRecords(MerchantPlaceFeeRequest request) {
        List<MerchantPlaceBind> merchantPlaceBinds = merchantPlaceBindService.queryNoSettleByMerchantId(request.getMerchantId());
        
        if (ObjectUtils.isEmpty(merchantPlaceBinds)) {
            return Collections.emptyList();
        }
        
        // 排除掉开始时间和结束时间在一天的数据
        List<Long> placeIdList = merchantPlaceBinds.stream().map(MerchantPlaceBind::getPlaceId).collect(Collectors.toList());
        
        // 三个月前的第一天
        long startTime = DateUtils.getBeforeMonthFirstDayTimestamp(MerchantPlaceBindConstant.LAST_MONTH);
        // 三个月的最后一天
        long endTime = DateUtils.getBeforeMonthLastDayTimestamp(MerchantPlaceBindConstant.LAST_MONTH);
        
        Date startDate = new Date(startTime);
        
        // 计算上的月份
        String lastMonth = DateUtil.format(startDate, RebateRecordConstant.MONTH_DATE_FORMAT);
        
        // 计算当前月份
        String curMonth = DateUtil.format(new Date(), RebateRecordConstant.MONTH_DATE_FORMAT);
        
        // 修改标记id集合
        List<String> monthList = new ArrayList<>();
        monthList.add(lastMonth);
        List<MerchantPlaceFeeMonthDetail> list = new ArrayList<>();
        
        // 查询场地下的月度账单信息
        List<MerchantPlaceFeeMonthRecord> placeFeeMonthRecords = merchantPlaceFeeMonthRecordService.queryList(placeIdList, monthList);
        
        // 计算当前月份的账单
        List<MerchantPlaceFeeMonthRecord> curPlaceFeeMonthRecords = getCurMonthRecordFirst(placeIdList);
        if (ObjectUtils.isNotEmpty(curPlaceFeeMonthRecords)) {
            placeFeeMonthRecords.addAll(curPlaceFeeMonthRecords);
        }
        
        Map<Long, List<MerchantPlaceFeeMonthRecord>> placeFeeMonthRecordMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(placeFeeMonthRecords)) {
            placeFeeMonthRecordMap = placeFeeMonthRecords.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getPlaceId));
        }
        
        Map<Long, List<MerchantPlaceBind>> placeMap = merchantPlaceBinds.stream().collect(Collectors.groupingBy(MerchantPlaceBind::getPlaceId));
        
        for (Map.Entry<Long, List<MerchantPlaceBind>> entry : placeMap.entrySet()) {
            
            Long merchantId = request.getMerchantId();
            Long placeId = entry.getKey();
            List<MerchantPlaceBind> value = entry.getValue();
            
            // 处理连续的时间段
            value = dealSameRecord(value);
            
            // 获取柜机再三个月前的有效的时间段
            Map<Long, List<MerchantPlaceFeeMonthRecord>> cabinetMap = getPlaceCabinetMonthRecord(placeFeeMonthRecordMap.get(placeId), curMonth, lastMonth);
            if (ObjectUtils.isEmpty(cabinetMap)) {
                log.info("merchant place fee month detail handler bind month cabinet record is empty, merchantId={},placeId={}, twoBeforeMonth={}, lastMonth={}", merchantId,
                        placeId, curMonth, lastMonth);
                continue;
            }
            
            for (MerchantPlaceBind bind : value) {
                Long bindStartTime = null;
                Long bindEndTime = null;
                // 绑定 开始时间必须小于等于本月的最后一天
                if (Objects.equals(bind.getType(), MerchantPlaceBindConstant.BIND) && Objects.nonNull(bind.getBindTime()) && bind.getBindTime() <= endTime) {
                    if (bind.getBindTime() <= startTime) {
                        bindStartTime = startTime;
                    } else {
                        bindStartTime = bind.getBindTime();
                    }
                    bindEndTime = endTime;
                }
                
                // 解绑 开始时间小于本月月末  结束时间大于本月月初
                if (Objects.equals(bind.getType(), MerchantPlaceBindConstant.UN_BIND) && Objects.nonNull(bind.getBindTime()) && Objects.nonNull(bind.getUnBindTime())
                        && bind.getBindTime() <= endTime && bind.getUnBindTime() >= startTime) {
                    if (bind.getBindTime() <= startTime) {
                        bindStartTime = startTime;
                    } else {
                        bindStartTime = bind.getBindTime();
                    }
                    
                    if (bind.getUnBindTime() <= endTime) {
                        bindEndTime = bind.getUnBindTime();
                    } else {
                        bindEndTime = endTime;
                    }
                }
                
                if (Objects.isNull(bindStartTime) || Objects.isNull(bindEndTime)) {
                    log.info("merchant place fee month detail handler time is invalid id={}", bind.getId());
                    continue;
                }
                
                // 判断场地的时间是否与柜机的时间存在重叠
                for (Map.Entry<Long, List<MerchantPlaceFeeMonthRecord>> cabinetEntry : cabinetMap.entrySet()) {
                    Long cabinetId = cabinetEntry.getKey();
                    List<MerchantPlaceFeeMonthRecord> cabinetDetail = cabinetEntry.getValue();
                    for (MerchantPlaceFeeMonthRecord cabinetRecord : cabinetDetail) {
                        if (bindStartTime > cabinetRecord.getRentEndTime() || bindEndTime < cabinetRecord.getRentStartTime()) {
                            continue;
                        }
                        Long cabinetStartTime = null;
                        Long cabinetEndTime = null;
                        // 场地的绑定的开始时间
                        if (bindStartTime <= cabinetRecord.getRentStartTime()) {
                            cabinetStartTime = cabinetRecord.getRentStartTime();
                        } else {
                            cabinetStartTime = bindStartTime;
                        }
                        
                        // 场地绑定的结束时间
                        if (bindEndTime <= cabinetRecord.getRentEndTime()) {
                            cabinetEndTime = bindEndTime;
                        } else {
                            cabinetEndTime = cabinetRecord.getRentEndTime();
                        }
                        if (Objects.isNull(cabinetStartTime) || Objects.isNull(cabinetEndTime)) {
                            continue;
                        }
                        
                        MerchantPlaceFeeMonthDetail merchantPlaceFeeMonthDetail = MerchantPlaceFeeMonthDetail.builder().merchantId(merchantId).placeId(placeId).cabinetId(cabinetId)
                                .tenantId(cabinetRecord.getTenantId()).startTime(cabinetStartTime).endTime(cabinetEndTime).build();
                        
                        // 计算开始和结束时间的场地费的总和
                        BigDecimal feeSum = merchantPlaceFeeDailyRecordMapper.selectList(cabinetStartTime, cabinetEndTime, cabinetId);
                        
                        if (ObjectUtils.isEmpty(feeSum)) {
                            feeSum = BigDecimal.ZERO;
                        }
                        
                        merchantPlaceFeeMonthDetail.setPlaceFee(feeSum);
                        list.add(merchantPlaceFeeMonthDetail);
                    }
                }
            }
        }
        
        return list;
    }
    
    public static List<MerchantPlaceBind> dealSameRecord(List<MerchantPlaceBind> value) {
        
        List<MerchantPlaceBind> bindList = new ArrayList<>();
        List<MerchantPlaceBind> unBindList = new ArrayList<>();
        
        for (MerchantPlaceBind item : value) {
            if (Objects.equals(item.getType(), MerchantPlaceBindConstant.BIND) && Objects.nonNull(item.getBindTime())) {
                bindList.add(item);
            }
            
            if (Objects.equals(item.getType(), MerchantPlaceBindConstant.UN_BIND) && Objects.nonNull(item.getBindTime()) && Objects.nonNull(item.getUnBindTime())) {
                String startDate = DateUtil.format(new Date(item.getBindTime()), RebateRecordConstant.MONTH_DAY_DATE_FORMAT);
                String endDate = DateUtil.format(new Date(item.getUnBindTime()), RebateRecordConstant.MONTH_DAY_DATE_FORMAT);
                if (!Objects.equals(startDate, endDate)) {
                    unBindList.add(item);
                }
            }
        }
        
        if (ObjectUtils.isEmpty(bindList) || ObjectUtils.isEmpty(unBindList)) {
            return Collections.emptyList();
        }
        
        // 解绑为空返回绑定
        if (ObjectUtils.isEmpty(unBindList)) {
            return bindList;
        }
        
        // 解绑开始和结束日期连续的进行合并
        for (int i = 1; i < unBindList.size(); i++) {
            MerchantPlaceBind unbind1 = unBindList.get(i - 1);
            MerchantPlaceBind unbind2 = unBindList.get(i);
            if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                unbind1.setUnBindTime(unbind2.getUnBindTime());
                unBindList.remove(unbind2);
            }
        }
        
        // 排除掉开始时间和结束时间在一天的数据
        unBindList = unBindList.stream().filter(item -> {
            // 将结束的时间戳回退一天
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.getUnBindTime());
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            item.setUnBindTime(calendar.getTimeInMillis());
            return true;
        }).collect(Collectors.toList());
        
        // 合并绑定记录
        if (ObjectUtils.isNotEmpty(bindList)) {
            unBindList.addAll(bindList);
        }
        
        // 排序
        unBindList = unBindList.stream().sorted(Comparator.comparing(MerchantPlaceBind::getBindTime)).collect(Collectors.toList());
        
        return unBindList;
    }
    
    /**
     * 获取本月指定场地的账单
     *
     * @param placeIdList
     * @return
     */
    private List<MerchantPlaceFeeMonthRecord> getCurMonthRecord(List<Long> placeIdList) {
        List<MerchantPlaceFeeMonthRecord> list = new ArrayList<>();
        // 获取柜机绑定记录表
        
        // 获取本月第一天的时间戳
        long dayOfMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(0);
        
        // 获取本月最后一天的时间戳
        long dayOfMonthEndTime = System.currentTimeMillis();
        
        String settleDate = new SimpleDateFormat(RebateRecordConstant.MONTH_DATE_FORMAT).format(new Date(dayOfMonthStartTime));
        
        // 获取场地的柜机绑定记录
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.queryListByPlaceId(placeIdList,
                MerchantPlaceCabinetBindConstant.PLACE_MONTH_NOT_SETTLEMENT);
        
        if (ObjectUtils.isEmpty(cabinetBindList)) {
            return list;
        }
        
        // 根据柜机的场地进行分组
        Map<Long, List<MerchantPlaceCabinetBind>> placeCabinetBindMap = cabinetBindList.stream().collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getPlaceId));
        placeCabinetBindMap.forEach((placeId, bindList) -> {
            if (ObjectUtils.isNotEmpty(bindList)) {
                // 获取场地的费用记录
                List<MerchantPlaceCabinetBindDTO> placeCabinetBindList = buildBindStatusRecord(bindList, dayOfMonthStartTime, dayOfMonthEndTime);
                if (ObjectUtils.isNotEmpty(placeCabinetBindList)) {
                    AtomicReference<Long> atomicReference = new AtomicReference();
                    atomicReference.set(0L);
                    placeCabinetBindList.forEach(cabinetBind -> {
                        MerchantPlaceFeeMonthRecord record = new MerchantPlaceFeeMonthRecord();
                        atomicReference.set(atomicReference.get() + 1L);
                        record.setId(System.currentTimeMillis() + atomicReference.get());
                        record.setMonthDate(settleDate);
                        record.setPlaceId(cabinetBind.getPlaceId());
                        record.setEid(cabinetBind.getCabinetId());
                        record.setRentStartTime(cabinetBind.getBindTime());
                        record.setRentEndTime(cabinetBind.getUnBindTime());
                        if (Objects.nonNull(cabinetBind.getUnBindTime())) {
                            Integer days = (int) ((cabinetBind.getUnBindTime() - cabinetBind.getBindTime()) / (24 * 60 * 60 * 1000));
                            record.setRentDays(days);
                        } else {
                            Integer days = (int) ((dayOfMonthEndTime - cabinetBind.getBindTime()) / (24 * 60 * 60 * 1000));
                            record.setRentDays(days);
                        }
                        record.setRentDays(cabinetBind.getMonthSettlement());
                        record.setCreateTime(System.currentTimeMillis());
                        record.setUpdateTime(System.currentTimeMillis());
                        list.add(record);
                    });
                }
            }
            
        });
        
        return list;
    }
    
    private List<MerchantPlaceCabinetBindDTO> buildBindStatusRecord(List<MerchantPlaceCabinetBind> cabinetBindList, long dayOfMonthStartTime, long dayOfMonthEndTime) {
        List<MerchantPlaceCabinetBindDTO> result = new ArrayList<>();
        List<MerchantPlaceCabinetBind> updatePlaceCabinetBindList = new ArrayList<>();
    
        List<MerchantPlaceCabinetBind> bindList = cabinetBindList.stream()
                .filter(cabinetBind -> Objects.equals(cabinetBind.getStatus(), MerchantPlaceCabinetBindConstant.STATUS_BIND)).collect(Collectors.toList());
    
        List<MerchantPlaceCabinetBind> unbindList = cabinetBindList.stream()
                .filter(cabinetBind -> Objects.equals(cabinetBind.getStatus(), MerchantPlaceCabinetBindConstant.STATUS_UNBIND))
                .sorted(Comparator.comparing(MerchantPlaceCabinetBind::getBindTime)).collect(Collectors.toList());
    
        log.info("test cabinetBindList={}", JsonUtil.toJson(cabinetBindList));
        log.info("test bindList={}", JsonUtil.toJson(bindList));
        log.info("test unbindList={}", JsonUtil.toJson(unbindList));
    
        //绑定记录如果有 则只有一条
        if (DataUtil.collectionIsUsable(bindList)) {
            MerchantPlaceCabinetBind bind = bindList.get(0);
            MerchantPlaceCabinetBindDTO bindDTO = new MerchantPlaceCabinetBindDTO();
            BeanUtils.copyProperties(bind, bindDTO);
            bindDTO.setCabinetEndBind(MerchantPlaceCabinetBindConstant.CABINET_END_UN_BIND);
            bindDTO.setUnBindTime(dayOfMonthEndTime);
            String placeMonthSettlementDetail = bindDTO.getPlaceMonthSettlementDetail();
            
            /*Long bindTime = bindDTO.getBindTime();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
            
            // 绑定时间都是从本月出开始计算
            if (bindDTO.getBindTime() < dayOfMonthStartTime) {
                bindDTO.setBindTime(dayOfMonthStartTime);
            }*/
        
            Long bindTime = bindDTO.getBindTime();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
            String settlementDate = fmt.format(new Date(bindTime));
        
            //判断绑定时间月份是否出过账  如果沒有账，则绑定时间设置为上月初
            if (bindDTO.getBindTime() < dayOfMonthStartTime && (Objects.nonNull(placeMonthSettlementDetail) && placeMonthSettlementDetail.contains(settlementDate))) {
                bindDTO.setBindTime(dayOfMonthStartTime);
            }
        
            if (ObjectUtils.isEmpty(unbindList)) {
                result.add(bindDTO);
                return result;
            }
        
            if (DataUtil.collectionIsUsable(unbindList)) {
                log.info("test 22 unbindList={}", JsonUtil.toJson(unbindList));
                // 过滤掉解绑记录的绑定时间大于绑定记录的绑定时间的记录z
                for (int i = 0; i < unbindList.size(); i++) {
                    MerchantPlaceCabinetBind unbind = unbindList.get(i);
                    log.info("unbind time={},bindDTO time={}", unbind.getBindTime(), bindDTO.getBindTime());
                    if (unbind.getBindTime() > bindDTO.getBindTime()) {
                        updatePlaceCabinetBindList.add(unbind);
                        unbindList.remove(unbind);
                        i--;
                    }
                }
            
                log.info("filter unbind={}", JsonUtil.toJson(unbindList));
            
                for (int i = 1; i < unbindList.size(); i++) {
                    MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                    MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
                    if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                        unbind1.setUnBindTime(unbind2.getUnBindTime());
                        unbindList.remove(unbind2);
                        i--;
                    }
                }
            
                log.info("filter 3333 unbind2={}", JsonUtil.toJson(unbindList));
                List<MerchantPlaceCabinetBindDTO> unbindDTOList = unbindList.parallelStream().map(unbind -> {
                    MerchantPlaceCabinetBindDTO unbindDTO = new MerchantPlaceCabinetBindDTO();
                    BeanUtils.copyProperties(unbind, unbindDTO);
                    return unbindDTO;
                }).collect(Collectors.toList());
                log.info("test unbindDTOList={}", JsonUtil.toJson(unbindDTOList));
            
                result.add(bindDTO);
                result.addAll(unbindDTOList);
                log.info("test resultList={}", JsonUtil.toJson(result));
            }
        } else {
            for (int i = 1; i < unbindList.size(); i++) {
                MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
            
                // 1.处理时间段是否连续  2.处理时间段是否包含
                if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                    unbind1.setUnBindTime(unbind2.getUnBindTime());
                    updatePlaceCabinetBindList.add(unbind2);
                    unbindList.remove(unbind2);
                    i--;
                } else if (unbind1.getUnBindTime() > unbind2.getBindTime()) {
                    updatePlaceCabinetBindList.add(unbind2);
                    unbindList.remove(unbind2);
                    i--;
                }
            }
        
            List<MerchantPlaceCabinetBindDTO> unbindDTOList = unbindList.parallelStream().map(unbind -> {
                MerchantPlaceCabinetBindDTO unbindDTO = new MerchantPlaceCabinetBindDTO();
                BeanUtils.copyProperties(unbind, unbindDTO);
                return unbindDTO;
            }).collect(Collectors.toList());
        
            result.addAll(unbindDTOList);
        
        }
    
        return result;
    }
    
    
    private List<MerchantPlaceCabinetBindDTO> buildBindStatusRecordFirst(List<MerchantPlaceCabinetBind> cabinetBindList, long dayOfMonthStartTime, long dayOfMonthEndTime) {
        List<MerchantPlaceCabinetBindDTO> result = new ArrayList<>();
        List<MerchantPlaceCabinetBind> updatePlaceCabinetBindList = new ArrayList<>();
    
        List<MerchantPlaceCabinetBind> bindList = cabinetBindList.stream()
                .filter(cabinetBind -> Objects.equals(cabinetBind.getStatus(), MerchantPlaceCabinetBindConstant.STATUS_BIND)).collect(Collectors.toList());
    
        List<MerchantPlaceCabinetBind> unbindList = cabinetBindList.stream()
                .filter(cabinetBind -> Objects.equals(cabinetBind.getStatus(), MerchantPlaceCabinetBindConstant.STATUS_UNBIND))
                .sorted(Comparator.comparing(MerchantPlaceCabinetBind::getBindTime)).collect(Collectors.toList());
    
        log.info("test cabinetBindList={}", JsonUtil.toJson(cabinetBindList));
        log.info("test bindList={}", JsonUtil.toJson(bindList));
        log.info("test unbindList={}", JsonUtil.toJson(unbindList));
    
        //绑定记录如果有 则只有一条
        if (DataUtil.collectionIsUsable(bindList)) {
            MerchantPlaceCabinetBind bind = bindList.get(0);
            MerchantPlaceCabinetBindDTO bindDTO = new MerchantPlaceCabinetBindDTO();
            BeanUtils.copyProperties(bind, bindDTO);
            bindDTO.setCabinetEndBind(MerchantPlaceCabinetBindConstant.CABINET_END_UN_BIND);
            bindDTO.setUnBindTime(dayOfMonthEndTime);
            String placeMonthSettlementDetail = bindDTO.getPlaceMonthSettlementDetail();
        
            Long bindTime = bindDTO.getBindTime();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
            String settlementDate = fmt.format(new Date(bindTime));
        
            //判断绑定时间月份是否出过账  如果沒有账，则绑定时间设置为上月初
            if (bindDTO.getBindTime() < dayOfMonthStartTime && (Objects.nonNull(placeMonthSettlementDetail) && placeMonthSettlementDetail.contains(settlementDate))) {
                bindDTO.setBindTime(dayOfMonthStartTime);
            }
            
            // 如果解绑为空则返回绑定的数据
            if (ObjectUtils.isEmpty(unbindList)) {
                result.add(bindDTO);
                return result;
            }
        
            if (DataUtil.collectionIsUsable(unbindList)) {
                log.info("test 22 unbindList={}", JsonUtil.toJson(unbindList));
                // 过滤掉解绑记录的绑定时间大于绑定记录的绑定时间的记录z
                for (int i = 0; i < unbindList.size(); i++) {
                    MerchantPlaceCabinetBind unbind = unbindList.get(i);
                    log.info("unbind time={},bindDTO time={}", unbind.getBindTime(), bindDTO.getBindTime());
                    if (unbind.getBindTime() > bindDTO.getBindTime()) {
                        updatePlaceCabinetBindList.add(unbind);
                        unbindList.remove(unbind);
                        i--;
                    }
                }
            
                log.info("filter unbind={}", JsonUtil.toJson(unbindList));
            
                for (int i = 1; i < unbindList.size(); i++) {
                    MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                    MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
                    if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                        unbind1.setUnBindTime(unbind2.getUnBindTime());
                        unbindList.remove(unbind2);
                        i--;
                    }
                }
            
                log.info("filter 3333 unbind2={}", JsonUtil.toJson(unbindList));
                List<MerchantPlaceCabinetBindDTO> unbindDTOList = unbindList.parallelStream().map(unbind -> {
                    MerchantPlaceCabinetBindDTO unbindDTO = new MerchantPlaceCabinetBindDTO();
                    BeanUtils.copyProperties(unbind, unbindDTO);
                    return unbindDTO;
                }).collect(Collectors.toList());
                log.info("test unbindDTOList={}", JsonUtil.toJson(unbindDTOList));
            
                result.add(bindDTO);
                result.addAll(unbindDTOList);
                log.info("test resultList={}", JsonUtil.toJson(result));
            }
        } else {
            for (int i = 1; i < unbindList.size(); i++) {
                MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
            
                // 1.处理时间段是否连续  2.处理时间段是否包含
                if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                    unbind1.setUnBindTime(unbind2.getUnBindTime());
                    updatePlaceCabinetBindList.add(unbind2);
                    unbindList.remove(unbind2);
                    i--;
                } else if (unbind1.getUnBindTime() > unbind2.getBindTime()) {
                    updatePlaceCabinetBindList.add(unbind2);
                    unbindList.remove(unbind2);
                    i--;
                }
            }
        
            List<MerchantPlaceCabinetBindDTO> unbindDTOList = unbindList.parallelStream().map(unbind -> {
                MerchantPlaceCabinetBindDTO unbindDTO = new MerchantPlaceCabinetBindDTO();
                BeanUtils.copyProperties(unbind, unbindDTO);
                return unbindDTO;
            }).collect(Collectors.toList());
        
            result.addAll(unbindDTOList);
        
        }
    
        return result;
    }
    
    /**
     * 处理月度账单然后获取每一个柜机的有效的时间段
     *
     * @param placeFeeMonthRecords
     * @param oneBeforeMonth
     * @param twoBeforeMonth
     * @return
     */
    private Map<Long, List<MerchantPlaceFeeMonthRecord>> getPlaceCabinetMonthRecord(List<MerchantPlaceFeeMonthRecord> placeFeeMonthRecords, String oneBeforeMonth,
            String twoBeforeMonth) {
        
        Map<String, List<MerchantPlaceFeeMonthRecord>> map = placeFeeMonthRecords.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getMonthDate));
        long twoLastBeforeMonthTime = DateUtils.getBeforeMonthLastDayTimestamp(MerchantPlaceBindConstant.TOW_MONTH_BEFORE);
        
        // 获取前两个月的时间段
        if (ObjectUtils.isNotEmpty(map.get(twoBeforeMonth))) {
            List<MerchantPlaceFeeMonthRecord> placeFeeMonthRecords1 = map.get(twoBeforeMonth);
            if (ObjectUtils.isEmpty(map.get(oneBeforeMonth))) {
                Map<Long, List<MerchantPlaceFeeMonthRecord>> resMap = placeFeeMonthRecords1.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getEid));
                return resMap;
            }
            
            // 过滤掉一个月前的账单的开时间小于两个月前的所在月份的月初时间
            long oneBeforeMonthTime = DateUtils.getBeforeMonthFirstDayTimestamp(MerchantPlaceBindConstant.LAST_MONTH);
            List<MerchantPlaceFeeMonthRecord> oneBeforeList = new ArrayList<>();
            map.get(oneBeforeMonth).stream().forEach(item -> {
                if (item.getRentStartTime() < oneBeforeMonthTime) {
                    oneBeforeList.add(item);
                } else {
                    return;
                }
                
                // 如果结束时间大于两个月前的月末则将结束时间设置为月末
                if (item.getRentEndTime() > twoLastBeforeMonthTime) {
                    item.setRentEndTime(twoLastBeforeMonthTime);
                }
            });
            
            if (ObjectUtils.isEmpty(oneBeforeList)) {
                // 如果不存在补的情况则直接返回两个月前的数据
                Map<Long, List<MerchantPlaceFeeMonthRecord>> resMap = placeFeeMonthRecords1.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getEid));
                return resMap;
            }
            
            // 两个月的记录合并
            placeFeeMonthRecords1.addAll(oneBeforeList);
            placeFeeMonthRecords1.stream().sorted(Comparator.comparing(MerchantPlaceFeeMonthRecord::getRentStartTime));
            Map<Long, List<MerchantPlaceFeeMonthRecord>> finalRecordsMap = placeFeeMonthRecords1.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getEid));
            
            List<Long> existsList = new ArrayList<>();
            
            Map<Long, List<MerchantPlaceFeeMonthRecord>> resMap = new HashMap<>();
            
            for (Map.Entry<Long, List<MerchantPlaceFeeMonthRecord>> entryOne : finalRecordsMap.entrySet()) {
                Long cabinetId = entryOne.getKey();
                List<MerchantPlaceFeeMonthRecord> resList = new ArrayList<>();
                List<MerchantPlaceFeeMonthRecord> value = entryOne.getValue();
                value = value.stream().sorted(Comparator.comparing(MerchantPlaceFeeMonthRecord::getRentStartTime)).collect(Collectors.toList());
                for (int i = 0; i < value.size(); i++) {
                    boolean flag = false;
                    
                    // 如果已经标记了则直接跳出
                    MerchantPlaceFeeMonthRecord oneRecord = value.get(i);
                    if (existsList.contains(oneRecord.getId())) {
                        continue;
                    } else {
                        flag = i == (value.size() - 1);
                    }
                    
                    for (int j = i + 1; j < value.size(); j++) {
                        MerchantPlaceFeeMonthRecord twoRecord = value.get(j);
                        if (oneRecord.getRentEndTime() < twoRecord.getRentStartTime()) {
                            flag = true;
                            break;
                        }
                        
                        if (oneRecord.getRentEndTime() >= twoRecord.getRentEndTime()) {
                            existsList.add(twoRecord.getId());
                            flag = true;
                        }
                    }
                    
                    // 加入
                    if (flag) {
                        resList.add(oneRecord);
                    }
                }
                
                resMap.put(cabinetId, resList);
            }
            
            return resMap;
        }
        
        // 两月前不存在  一月前账单存在
        if (ObjectUtils.isEmpty(map.get(twoBeforeMonth)) && ObjectUtils.isNotEmpty(map.get(oneBeforeMonth))) {
            // 过滤掉一个月前的账单的开时间小于两个月前的所在月份的月初时间
            long oneBeforeMonthTime = DateUtils.getBeforeMonthFirstDayTimestamp(MerchantPlaceBindConstant.LAST_MONTH);
            
            List<MerchantPlaceFeeMonthRecord> oneBeforeList = new ArrayList<>();
            
            map.get(oneBeforeMonth).stream().forEach(item -> {
                if (item.getRentStartTime() < oneBeforeMonthTime) {
                    oneBeforeList.add(item);
                } else {
                    return;
                }
                
                // 如果结束时间大于两个月前的月末则将结束时间设置为月末
                if (item.getRentEndTime() > twoLastBeforeMonthTime) {
                    item.setRentEndTime(twoLastBeforeMonthTime);
                }
            });
            
            if (ObjectUtils.isEmpty(oneBeforeList)) {
                // 如果不存在补的情况则直接返回三个月前的数据
                return null;
            }
            
            Map<Long, List<MerchantPlaceFeeMonthRecord>> resMap = oneBeforeList.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getEid));
            
            return resMap;
        }
        
        return null;
    }
}

