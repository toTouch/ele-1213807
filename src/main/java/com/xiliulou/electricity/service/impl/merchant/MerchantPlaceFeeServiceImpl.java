package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.date.DateUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceBindConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceCabinetBindConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceConstant;
import com.xiliulou.electricity.dto.merchant.MerchantPlaceCabinetBindDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindHistory;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindTime;
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
import org.apache.commons.collections4.CollectionUtils;
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
        
        // 检测当商户绑定的柜机是否存在场地费
        Integer placeCount = merchantPlaceMapService.existsPlaceFee(merchantId);
        if (Objects.nonNull(placeCount) && placeCount > 0) {
            return MerchantConstant.EXISTS_PLACE_FEE_YES;
        }
        
        return MerchantConstant.EXISTS_PLACE_FEE_NO;
    }
    
    @Slave
    @Override
    public MerchantPlaceFeeCurMonthVO getFeeData(MerchantPlaceFeeRequest request) {
        MerchantPlaceFeeCurMonthVO merchantPlaceFeeCurMonthVO = new MerchantPlaceFeeCurMonthVO();
        // 计算上个月一号到当前场地费的总和
        // 获取上个月的场地费
        BigDecimal lastMothFee = getLastMothFee(request);
        merchantPlaceFeeCurMonthVO.setLastMonthFee(lastMothFee);
        
        // 获取本月的场地费用
        BigDecimal curMothFee = getCurMothFee(request);
        merchantPlaceFeeCurMonthVO.setCurrentMonthFee(curMothFee);
        
        // 获取商户当前绑定的设备数
//        Integer cabinetCount = merchantPlaceMapService.countCabinetNumByMerchantId(request.getMerchantId());
//        merchantPlaceFeeCurMonthVO.setCabinetCount(cabinetCount);
        
        // 计算累计场地费 上月之前的月份+上月+本月
        // 上月的第一天
        long time = DateUtils.getBeforeMonthFirstDayTimestamp(1);
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
        MerchantCabinetFeeDetailShowVO resVo = new MerchantCabinetFeeDetailShowVO();
        
        // 根据商户id查询所有的柜机的id
        List<Long> cabinetIdList = merchantPlaceFeeMonthService.selectCabinetIdByMerchantId(request.getMerchantId());
        List<MerchantPlaceFeeMonth> feeMonthsHistory = merchantPlaceFeeMonthService.queryListByMerchantId(request.getMerchantId(), request.getCabinetId(), request.getPlaceId());
        
        Map<Long, BigDecimal> feeMonthsHistoryMap = new HashMap<>();
        
        if (ObjectUtils.isNotEmpty(feeMonthsHistory)) {
            feeMonthsHistoryMap = feeMonthsHistory.stream()
                    .collect(Collectors.groupingBy(MerchantPlaceFeeMonth::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.sumHistoryFee(e))));
        }
        
        // 查询上月
        List<MerchantPlaceFeeMonthDetail> lastMonthFeeRecords = getLastMonthFeeRecords(request);
        
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
        List<MerchantPlaceFeeMonthDetail> curMothFeeRecords = getCurMothFeeRecords(request);
        
        if (Objects.nonNull(request.getPlaceId())) {
            curMothFeeRecords = curMothFeeRecords.stream().filter(item -> Objects.equals(item.getPlaceId(), request.getPlaceId())).collect(Collectors.toList());
        }
        
        if (Objects.nonNull(request.getCabinetId())) {
            curMothFeeRecords = curMothFeeRecords.stream().filter(item -> Objects.equals(item.getCabinetId(), request.getCabinetId())).collect(Collectors.toList());
        }
        
        Map<Long, BigDecimal> curMonthCabinetFeeMap = new HashMap<>();
        Map<Long, Long> cabinetTimeMap = new HashMap<>();
        
        if (ObjectUtils.isNotEmpty(curMothFeeRecords)) {
            List<Long> curMonthCabinetIdList = curMothFeeRecords.stream().map(MerchantPlaceFeeMonthDetail::getCabinetId).distinct().collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(curMonthCabinetIdList)) {
                cabinetIdList.addAll(curMonthCabinetIdList);
            }
            
            curMonthCabinetFeeMap = curMothFeeRecords.stream()
                    .collect(Collectors.groupingBy(MerchantPlaceFeeMonthDetail::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.sumFee(e))));
            
            Map<Long, Long> curCabinetTimeMap = curMothFeeRecords.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthDetail::getCabinetId, Collectors.collectingAndThen(Collectors.toList(),
                    e -> e.stream().sorted(Comparator.comparing(MerchantPlaceFeeMonthDetail::getStartTime).reversed()).findFirst().get().getStartTime())));
            
            if (ObjectUtils.isNotEmpty(curCabinetTimeMap)) {
                cabinetTimeMap.putAll(curCabinetTimeMap);
            }
        }
        
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
            if (ObjectUtils.isNotEmpty(finalCurMonthCabinetFeeMap.get(cabinetId))) {
                vo.setCurrentMonthFee(finalCurMonthCabinetFeeMap.get(cabinetId));
            }
            
            BigDecimal historyFee = BigDecimal.ZERO;
            // 设置累加的场地费
            if (ObjectUtils.isNotEmpty(finalFeeMonthsHistoryMap.get(cabinetId))) {
                historyFee = historyFee.add(finalFeeMonthsHistoryMap.get(cabinetId));
            }
            
            if (ObjectUtils.isNotEmpty(finalLastMonthCabinetFeeMap.get(cabinetId))) {
                historyFee = historyFee.add(finalLastMonthCabinetFeeMap.get(cabinetId));
            }
           
            historyFee = historyFee.add(vo.getCurrentMonthFee());
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
        List<MerchantCabinetFeeDetailVO> resList = new ArrayList<>();
        // 获取当前月份
        String currentMonth = DateUtil.format(new Date(), "yyyy-MM");
        
        String today = DateUtil.format(new Date(), "yyyy-MM-dd");
        if (Objects.equals(currentMonth, request.getMonth())) {
            List<MerchantPlaceFeeMonthDetail> curMothFeeRecords = getCurMothFeeRecords(request);
            if (ObjectUtils.isEmpty(curMothFeeRecords)) {
                return Collections.emptyList();
            }
            
            if (ObjectUtils.isNotEmpty(curMothFeeRecords)) {
                for (MerchantPlaceFeeMonthDetail placeFeeMonthDetail : curMothFeeRecords) {
                    MerchantCabinetFeeDetailVO vo = new MerchantCabinetFeeDetailVO();
                    vo.setPlaceFee(placeFeeMonthDetail.getPlaceFee());
                    MerchantPlace merchantPlace = merchantPlaceService.queryFromCacheById(placeFeeMonthDetail.getPlaceId());
                    if (Objects.nonNull(merchantPlace)) {
                        vo.setPlaceName(merchantPlace.getName());
                    }
                    ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(placeFeeMonthDetail.getCabinetId().intValue());
                    if (Objects.nonNull(cabinet)) {
                        vo.setCabinetName(cabinet.getName());
                    }
                    vo.setStartTime(placeFeeMonthDetail.getStartTime());
                    vo.setEndTime(placeFeeMonthDetail.getEndTime());
                    String endDay = DateUtil.format(new Date(placeFeeMonthDetail.getEndTime()), "yyyy-MM-dd");
                    if (!Objects.equals(today, endDay)) {
                        vo.setStatus(MerchantPlaceCabinetBindConstant.STATUS_UNBIND);
                    } else {
                        // 根据场地和柜判断当前是否绑定
                        Integer count = merchantPlaceCabinetBindService.checkIsBindByPlaceId(placeFeeMonthDetail.getPlaceId(), placeFeeMonthDetail.getCabinetId());
                        if (count >= 0) {
                            vo.setStatus(MerchantPlaceCabinetBindConstant.STATUS_BIND);
                        }
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
        if (ObjectUtils.isEmpty(placeFeeMonths)) {
            return Collections.emptyList();
        }
        
        if (ObjectUtils.isNotEmpty(placeFeeMonths)) {
            for (MerchantCabinetBindHistory placeFeeMonthDetail : placeFeeMonths) {
                MerchantCabinetFeeDetailVO vo = new MerchantCabinetFeeDetailVO();
                vo.setPlaceFee(placeFeeMonthDetail.getPlaceFee());
                MerchantPlace merchantPlace = merchantPlaceService.queryFromCacheById(placeFeeMonthDetail.getPlaceId());
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
        List<MerchantPlaceFeeMonthDetail> list = getCurMothFeeRecords(request);
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
    
    private List<MerchantPlaceFeeMonthDetail> getCurMothFeeRecords(MerchantPlaceFeeRequest request) {
        List<MerchantPlaceBind> merchantPlaceBinds = merchantPlaceBindService.queryNoSettleByMerchantId(request.getMerchantId());
        if (ObjectUtils.isEmpty(merchantPlaceBinds)) {
            return Collections.emptyList();
        }
        
        List<Long> placeIdList = new ArrayList<>();
        // 排除掉开始时间和结束时间在一天的数据
        merchantPlaceBinds.stream().filter(item -> {
            String startDate = DateUtil.format(new Date(item.getBindTime()), dateFormat);
            String endDate = DateUtil.format(new Date(item.getUnBindTime()), dateFormat);
            if (Objects.equals(startDate, endDate)) {
                return false;
            }
            // 将结束的时间戳回退一天
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.getUnBindTime());
            calendar.set(Calendar.DAY_OF_MONTH, -1);
            item.setUnBindTime(calendar.getTimeInMillis());
            placeIdList.add(item.getPlaceId());
            return true;
        });
        
        // 三个月前的第一天
        long startTime = DateUtils.getBeforeMonthFirstDayTimestamp(0);
        // 三个月的最后一天
        long endTime = System.currentTimeMillis();
        
        // 计算当前月份
        String curMonth = DateUtil.format(new Date(), "yyyy-MM");
        // 修改标记id集合
        List<Long> updateIdList = new ArrayList<>();
        List<MerchantPlaceFeeMonthDetail> list = new ArrayList<>();
        
        // 计算当前月份的账单
        List<MerchantPlaceFeeMonthRecord> curPlaceFeeMonthRecords = getCurMonthRecordFirst(placeIdList);
        if (ObjectUtils.isEmpty(curPlaceFeeMonthRecords)) {
            log.info("get current moth place fee records is empty,merchantId={},placeIdList={}", request.getMerchantId(), placeIdList);
            return null;
        }
        
        Map<Long, List<MerchantPlaceFeeMonthRecord>> cabinetMap = curPlaceFeeMonthRecords.stream().collect(Collectors.groupingBy(MerchantPlaceFeeMonthRecord::getEid));
        
        for (MerchantPlaceBind bind : merchantPlaceBinds) {
            
            Long merchantId = request.getMerchantId();
            Long placeId = bind.getPlaceId();
            
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
                    merchantPlaceFeeMonthDetail.setPlaceFee(feeSum);
                    list.add(merchantPlaceFeeMonthDetail);
                }
            }
        }
        
        return list;
    }
    
    private List<MerchantPlaceFeeMonthRecord> getCurMonthRecordFirst(List<Long> placeIdList) {
        List<MerchantPlaceFeeMonthRecord> list = new ArrayList<>();
        // 获取柜机绑定记录表
        
        // 获取本月第一天的时间戳
        long dayOfMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(0);
        
        // 获取上月最后一天的时间戳
        long dayOfMonthEndTime = System.currentTimeMillis();
        
        String settleDate = new SimpleDateFormat("yyyy-MM").format(new Date(dayOfMonthStartTime));
        // 获取场地的柜机绑定记录
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.queryListByPlaceId(placeIdList,
                MerchantPlaceCabinetBindConstant.PLACE_MONTH_NOT_SETTLEMENT);
        if (ObjectUtils.isEmpty(cabinetBindList)) {
            return list;
        }
        
        // 根据柜机的场地进行分组
        Map<Long, List<MerchantPlaceCabinetBind>> placeCabinetBindMap = cabinetBindList.stream().collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getPlaceId));
        placeCabinetBindMap.forEach((placeId, bindList) -> {
            if (DataUtil.collectionIsUsable(bindList)) {
                // 获取场地的费用记录
                List<MerchantPlaceCabinetBindDTO> placeCabinetBindList = buildBindStatusRecordFirst(bindList, dayOfMonthStartTime, dayOfMonthEndTime);
                if (DataUtil.collectionIsUsable(placeCabinetBindList)) {
                    placeCabinetBindList.forEach(cabinetBind -> {
                        MerchantPlaceFeeMonthRecord record = new MerchantPlaceFeeMonthRecord();
                        record.setMonthDate(settleDate);
                        record.setPlaceId(cabinetBind.getPlaceId());
                        record.setEid(cabinetBind.getId());
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
        
        List<Long> placeIdList = new ArrayList<>();
        // 排除掉开始时间和结束时间在一天的数据
        merchantPlaceBinds.stream().filter(item -> {
            String startDate = DateUtil.format(new Date(item.getBindTime()), dateFormat);
            String endDate = DateUtil.format(new Date(item.getUnBindTime()), dateFormat);
            if (Objects.equals(startDate, endDate)) {
                return false;
            }
            
            // 将结束的时间戳回退一天
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.getUnBindTime());
            calendar.set(Calendar.DAY_OF_MONTH, -1);
            item.setUnBindTime(calendar.getTimeInMillis());
            
            placeIdList.add(item.getPlaceId());
            
            return true;
        });
        
        // 三个月前的第一天
        long startTime = DateUtils.getBeforeMonthFirstDayTimestamp(1);
        // 三个月的最后一天
        long endTime = DateUtils.getBeforeMonthLastDayTimestamp(1);
        
        Date startDate = new Date(startTime);
        
        // 计算三个月前的月份
        String lastMonth = DateUtil.format(startDate, "yyyy-MM");
        // 计算当前月份
        String curMonth = DateUtil.format(new Date(), "yyyy-MM");
        
        // 修改标记id集合
        List<Long> updateIdList = new ArrayList<>();
        List<String> monthList = new ArrayList<>();
        monthList.add(lastMonth);
        List<MerchantPlaceFeeMonthDetail> list = new ArrayList<>();
        
        // 查询场地下的月度账单信息
        List<MerchantPlaceFeeMonthRecord> placeFeeMonthRecords = merchantPlaceFeeMonthRecordService.queryList(placeIdList, monthList);
        
        // 计算当前月份的账单
        List<MerchantPlaceFeeMonthRecord> curPlaceFeeMonthRecords = getCurMonthRecord(placeIdList);
        if (ObjectUtils.isNotEmpty(curPlaceFeeMonthRecords)) {
            placeFeeMonthRecords.addAll(curPlaceFeeMonthRecords);
        }
        
        for (MerchantPlaceBind bind : merchantPlaceBinds) {
            
            Long merchantId = request.getMerchantId();
            Long placeId = bind.getPlaceId();
            
            // 获取柜机再三个月前的有效的时间段
            Map<Long, List<MerchantPlaceFeeMonthRecord>> cabinetMap = getPlaceCabinetMonthRecord(placeFeeMonthRecords, curMonth, lastMonth);
            if (ObjectUtils.isEmpty(cabinetMap)) {
                log.info("merchant place fee month detail handler bind month cabinet record is empty, merchantId={},placeId={}, twoBeforeMonth={}, lastMonth={}", merchantId,
                        placeId, curMonth, lastMonth);
                continue;
            }
            
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
                    merchantPlaceFeeMonthDetail.setPlaceFee(feeSum);
                    list.add(merchantPlaceFeeMonthDetail);
                }
            }
        }
        return list;
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
        
        // 获取上月第一天的时间戳
        long dayOfMonthStartTime = DateUtils.getBeforeMonthFirstDayTimestamp(0);
        
        // 获取上月最后一天的时间戳
        long dayOfMonthEndTime = System.currentTimeMillis();
        
        String settleDate = new SimpleDateFormat("yyyy-MM").format(new Date(dayOfMonthStartTime));
        // 获取场地的柜机绑定记录
        List<MerchantPlaceCabinetBind> cabinetBindList = merchantPlaceCabinetBindService.queryListByPlaceId(placeIdList,
                MerchantPlaceCabinetBindConstant.PLACE_MONTH_NOT_SETTLEMENT);
        if (ObjectUtils.isEmpty(cabinetBindList)) {
            return list;
        }
        
        // 根据柜机的场地进行分组
        Map<Long, List<MerchantPlaceCabinetBind>> placeCabinetBindMap = cabinetBindList.stream().collect(Collectors.groupingBy(MerchantPlaceCabinetBind::getPlaceId));
        placeCabinetBindMap.forEach((placeId, bindList) -> {
            if (DataUtil.collectionIsUsable(bindList)) {
                // 获取场地的费用记录
                List<MerchantPlaceCabinetBindDTO> placeCabinetBindList = buildBindStatusRecord(bindList, dayOfMonthStartTime, dayOfMonthEndTime);
                if (DataUtil.collectionIsUsable(placeCabinetBindList)) {
                    placeCabinetBindList.forEach(cabinetBind -> {
                        MerchantPlaceFeeMonthRecord record = new MerchantPlaceFeeMonthRecord();
                        record.setMonthDate(settleDate);
                        record.setPlaceId(cabinetBind.getPlaceId());
                        record.setEid(cabinetBind.getId());
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
        
        //绑定记录如果有 则只有一条
        if (DataUtil.collectionIsUsable(bindList)) {
            MerchantPlaceCabinetBind bind = bindList.get(0);
            MerchantPlaceCabinetBindDTO bindDTO = new MerchantPlaceCabinetBindDTO();
            BeanUtils.copyProperties(bind, bindDTO);
            bindDTO.setIsNeedMonthSettle(Boolean.FALSE);
            String placeMonthSettlementDetail = bindDTO.getPlaceMonthSettlementDetail();
            Long bindTime = bindDTO.getBindTime();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
            String settlementDate = fmt.format(new Date(bindTime));
            
            //判断绑定时间是否是上月 并且上月未出账
            if (bindDTO.getBindTime() < dayOfMonthStartTime && (Objects.nonNull(placeMonthSettlementDetail) && placeMonthSettlementDetail.contains(settlementDate))) {
                bindDTO.setBindTime(dayOfMonthStartTime);
            }
            
            if (DataUtil.collectionIsUsable(unbindList)) {
                
                // 过滤掉解绑记录的绑定时间大于绑定记录的绑定时间的记录
                unbindList.forEach(unbind -> {
                    if (unbind.getBindTime() > bindDTO.getBindTime()) {
                        updatePlaceCabinetBindList.add(unbind);
                        unbindList.remove(unbind);
                    }
                });
                
                //过滤完后unbindList为空
                if (CollectionUtils.isEmpty(unbindList)) {
                    MerchantPlaceCabinetBindDTO bindInsert = new MerchantPlaceCabinetBindDTO();
                    BeanUtils.copyProperties(bindDTO, bindInsert);
                    bindInsert.setUnBindTime(dayOfMonthEndTime);
                    result.add(bindInsert);
                } else {
                    for (int i = 1; i < unbindList.size(); i++) {
                        MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                        MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
                        if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                            unbind1.setUnBindTime(unbind2.getUnBindTime());
                            unbindList.remove(unbind2);
                        }
                    }
                    
                    List<MerchantPlaceCabinetBindDTO> unbindDTOList = unbindList.parallelStream().map(unbind -> {
                        MerchantPlaceCabinetBindDTO unbindDTO = new MerchantPlaceCabinetBindDTO();
                        BeanUtils.copyProperties(unbind, unbindDTO);
                        return unbindDTO;
                    }).collect(Collectors.toList());
                    
                    // 设置绑定时间~月末最后一天
                    MerchantPlaceCabinetBindDTO bindInsert = new MerchantPlaceCabinetBindDTO();
                    BeanUtils.copyProperties(bindDTO, bindInsert);
                    bindInsert.setUnBindTime(dayOfMonthEndTime);
                    result.add(bindInsert);
                    result.addAll(unbindDTOList);
                }
            }
        } else {
            for (int i = 1; i < unbindList.size(); i++) {
                MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
                if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                    unbind1.setUnBindTime(unbind2.getUnBindTime());
                    unbindList.remove(unbind2);
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
        
        //绑定记录如果有 则只有一条
        if (DataUtil.collectionIsUsable(bindList)) {
            MerchantPlaceCabinetBind bind = bindList.get(0);
            MerchantPlaceCabinetBindDTO bindDTO = new MerchantPlaceCabinetBindDTO();
            BeanUtils.copyProperties(bind, bindDTO);
            bindDTO.setIsNeedMonthSettle(Boolean.FALSE);
            //            String placeMonthSettlementDetail = bindDTO.getPlaceMonthSettlementDetail();
            Long bindTime = bindDTO.getBindTime();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM");
            //            String settlementDate = fmt.format(new Date(bindTime));
            // 绑定时间都是从本月出开始计算
            if (bindDTO.getBindTime() < dayOfMonthStartTime) {
                bindDTO.setBindTime(dayOfMonthStartTime);
            }
    
           /* //判断绑定时间是否是上月 并且上月未出账
            if (bindDTO.getBindTime() < dayOfMonthStartTime && (Objects.nonNull(placeMonthSettlementDetail) && placeMonthSettlementDetail.contains(settlementDate))) {
                bindDTO.setBindTime(dayOfMonthStartTime);
            } else {
            
            }*/
            
            if (DataUtil.collectionIsUsable(unbindList)) {
                
                // 过滤掉解绑记录的绑定时间大于绑定记录的绑定时间的记录
                unbindList.forEach(unbind -> {
                    if (unbind.getBindTime() > bindDTO.getBindTime()) {
                        updatePlaceCabinetBindList.add(unbind);
                        unbindList.remove(unbind);
                    }
                });
                
                //过滤完后unbindList为空
                if (CollectionUtils.isEmpty(unbindList)) {
                    MerchantPlaceCabinetBindDTO bindInsert = new MerchantPlaceCabinetBindDTO();
                    BeanUtils.copyProperties(bindDTO, bindInsert);
                    bindInsert.setUnBindTime(dayOfMonthEndTime);
                    result.add(bindInsert);
                } else {
                    for (int i = 1; i < unbindList.size(); i++) {
                        MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                        MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
                        if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                            unbind1.setUnBindTime(unbind2.getUnBindTime());
                            unbindList.remove(unbind2);
                        }
                    }
                    
                    List<MerchantPlaceCabinetBindDTO> unbindDTOList = unbindList.parallelStream().map(unbind -> {
                        MerchantPlaceCabinetBindDTO unbindDTO = new MerchantPlaceCabinetBindDTO();
                        BeanUtils.copyProperties(unbind, unbindDTO);
                        return unbindDTO;
                    }).collect(Collectors.toList());
                    
                    // 设置绑定时间~月末最后一天
                    MerchantPlaceCabinetBindDTO bindInsert = new MerchantPlaceCabinetBindDTO();
                    BeanUtils.copyProperties(bindDTO, bindInsert);
                    bindInsert.setUnBindTime(dayOfMonthEndTime);
                    result.add(bindInsert);
                    result.addAll(unbindDTOList);
                }
            }
        } else {
            for (int i = 1; i < unbindList.size(); i++) {
                MerchantPlaceCabinetBind unbind1 = unbindList.get(i - 1);
                MerchantPlaceCabinetBind unbind2 = unbindList.get(i);
                if (DateUtils.isSameDay(unbind1.getUnBindTime(), unbind2.getBindTime())) {
                    unbind1.setUnBindTime(unbind2.getUnBindTime());
                    unbindList.remove(unbind2);
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
            if (ObjectUtils.isEmpty(ObjectUtils.isEmpty(map.get(oneBeforeMonth)))) {
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
                
                for (int i = 0; i < value.size(); i++) {
                    boolean flag = false;
                    
                    // 如果已经标记了则直接跳出
                    MerchantPlaceFeeMonthRecord oneRecord = value.get(i);
                    if (existsList.contains(oneRecord.getId())) {
                        continue;
                    }
                    
                    for (int j = i + 1; j < value.size(); j++) {
                        MerchantPlaceFeeMonthRecord twoRecord = value.get(j);
                        if (oneRecord.getRentEndTime() < twoRecord.getRentStartTime()) {
                            flag = true;
                            break;
                        }
                        
                        if (oneRecord.getRentEndTime() >= twoRecord.getRentEndTime()) {
                            existsList.add(twoRecord.getId());
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

