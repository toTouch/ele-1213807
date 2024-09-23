package com.xiliulou.electricity.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.http.resttemplate.service.RestTemplateService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.message.MessageCenterConfig;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.constant.note.MessageCenterConstant;
import com.xiliulou.electricity.constant.note.TenantNoteConstant;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.MaintenanceUserNotifyConfig;
import com.xiliulou.electricity.entity.TenantNote;
import com.xiliulou.electricity.entity.notify.MessageCenterSendReceiver;
import com.xiliulou.electricity.entity.notify.TenantNoteLowerNotice;
import com.xiliulou.electricity.entity.warn.WarnNoteCallBack;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmDeviceTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmGradeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureWarnMsgStatusEnum;
import com.xiliulou.electricity.mapper.EleHardwareFailureWarnMsgMapper;
import com.xiliulou.electricity.mapper.FailureAlarmMapper;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureAlarmQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgTaskQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.request.notify.MessageCenterRequest;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.FailureAlarmService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.TenantNoteService;
import com.xiliulou.electricity.service.warn.EleHardwareWarnMsgService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgPageVo;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnMsgExcelVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionExportVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @since 2023-12-26 09:07:48
 */
@Service
@Slf4j
public class EleHardwareFailureWarnMsgServiceImpl implements EleHardwareFailureWarnMsgService {
    
    @Resource
    private EleHardwareFailureWarnMsgMapper failureWarnMsgMapper;
    
    @Resource
    private FailureAlarmService failureAlarmService;
    
    @Resource
    private FailureAlarmMapper failureAlarmMapper;
    
    @Resource
    private ElectricityCabinetService cabinetService;
    
    @Resource
    private ElectricityBatteryService batteryService;
    
    @Resource
    private TenantNoteService tenantNoteService;
    
    @Qualifier("restTemplateServiceImpl")
    @Autowired
    RestTemplateService restTemplateService;
    
    @Resource
    private EleHardwareWarnMsgService eleHardwareWarnMsgService;
    
    @Resource
    private MessageCenterConfig messageCenterConfig;
    
    @Resource
    private ElectricityConfigService electricityConfigService;
    
    @Resource
    private MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    XllThreadPoolExecutorService xllThreadPoolExecutorService = XllThreadPoolExecutors.newFixedThreadPool("WARN-LOWER-NOTE-NOTICE-THREAD-POOL", 2, "warnLowerNoteNoticeThread:");
    
    
    @Slave
    @Override
    public List<EleHardwareFailureWarnMsgVo> list(FailureAlarmTaskQueryRequest request) {
        FailureWarnMsgTaskQueryModel queryModel = FailureWarnMsgTaskQueryModel.builder().startTime(request.getStartTime()).endTime(request.getEndTime()).build();
        return failureWarnMsgMapper.selectList(queryModel);
    }
    
    /**
     * 故障告警记录列表分页
     *
     * @param request
     * @return
     */
    @Override
    public R listByPage(EleHardwareFailureWarnMsgPageRequest request) {
        FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        if (triple.getLeft() && !Objects.isNull(triple.getRight())) {
            return R.ok(Collections.emptyList());
        }
        
        List<EleHardwareFailureWarnMsg> list = failureWarnMsgMapper.selectListByPage(queryModel);
        
        if (ObjectUtils.isEmpty(list)) {
            return R.ok(Collections.emptyList());
        }
        
        Integer type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode();
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
            type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode();
        }
        
        Set<Long> batteryIdSet = list.stream().filter(item -> !Objects.equals(item.getBatteryId(), NumberConstant.ZERO_L)).map(EleHardwareFailureWarnMsg::getBatteryId)
                .collect(Collectors.toSet());
        
        Map<Long, String> batterySnMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(batteryIdSet)) {
            List<Long> idList = batteryIdSet.stream().collect(Collectors.toList());
            List<ElectricityBattery> batteryList = batteryService.queryListByIdList(idList);
            if (ObjectUtils.isNotEmpty(batteryList)) {
                batterySnMap = batteryList.stream().collect(Collectors.toMap(ElectricityBattery::getId, ElectricityBattery::getSn));
            }
        }
        
        List<EleHardwareFailureWarnMsgPageVo> resultList = new ArrayList<>();
        Integer finalType = type;
        Map<Long, String> finalBatterySnMap = batterySnMap;
        list.forEach(item -> {
            EleHardwareFailureWarnMsgPageVo vo = new EleHardwareFailureWarnMsgPageVo();
            BeanUtils.copyProperties(item, vo);
            
            if (Objects.equals(vo.getCellNo(), NumberConstant.ZERO)) {
                vo.setCellNo(null);
            }
            
            // 查询柜机版本
            ElectricityCabinet electricityCabinet = cabinetService.queryByIdFromCache(vo.getCabinetId());
            Optional.ofNullable(electricityCabinet).ifPresent(electricityCabinet1 -> {
                vo.setCabinetVersion(electricityCabinet1.getVersion());
                vo.setSn(electricityCabinet1.getSn());
            });
            
            Map<String, Map<String, String>> map = new HashMap<>();
            
            // 上报的记录没有
            FailureAlarm failureAlarm = failureAlarmService.queryFromCacheBySignalId(vo.getSignalId());
            if (Objects.nonNull(failureAlarm) && Objects.equals(failureAlarm.getType(), finalType)) {
                String signalName = failureAlarm.getSignalName();
                Map<String, String> descMap = map.get(failureAlarm.getSignalId());
                if (ObjectUtils.isEmpty(descMap)) {
                    descMap = getDescMap(failureAlarm.getEventDesc());
                    map.put(failureAlarm.getSignalId(), descMap);
                }
                
                if (ObjectUtils.isNotEmpty(descMap.get(vo.getAlarmDesc()))) {
                    signalName = signalName + CommonConstant.STR_COMMA + descMap.get(vo.getAlarmDesc());
                }
                
                vo.setFailureAlarmName(signalName);
                vo.setGrade(failureAlarm.getGrade());
                vo.setDeviceType(failureAlarm.getDeviceType());
            } else if (Objects.isNull(request.getNoLimitSignalId())) {
                vo.setFailureAlarmName("");
                vo.setGrade(null);
                vo.setDeviceType(null);
            }
            
            if (ObjectUtils.isNotEmpty(item.getBatterySn())) {
                vo.setSn(item.getBatterySn());
            }
            
            // 根据电池id重置电池编号
            if (ObjectUtils.isNotEmpty(item.getBatteryId()) && ObjectUtils.isNotEmpty(finalBatterySnMap.get(item.getBatteryId()))) {
                String batterySn = finalBatterySnMap.get(item.getBatteryId());
                vo.setSn(batterySn);
            }
            
            resultList.add(vo);
        });
        
        return R.ok(resultList);
    }
    
    private Triple<Boolean, String, Object> checkAndInitQuery(EleHardwareFailureWarnMsgPageRequest request, FailureWarnMsgPageQueryModel queryModel, int daySize) {
        if (ObjectUtils.isEmpty(request.getAlarmStartTime())) {
            return Triple.of(false, "300828", "查询开始时间不能为空");
        }
        
        if (ObjectUtils.isEmpty(request.getAlarmEndTime())) {
            return Triple.of(false, "300829", "查询结束时间不能为空");
        }
        
        // 计算查询时间不能大于三十天
        if (request.getAlarmEndTime() < request.getAlarmStartTime()) {
            return Triple.of(false, "300826", "查询结束时间不能小于开始时间");
        }
        
        long days = DateUtils.diffDayV2(request.getAlarmStartTime(), request.getAlarmEndTime());
        if (days > daySize) {
            return Triple.of(false, "300825", String.format("查询天数不能大于%s天", daySize));
        }
        
        // 设置查询参数
        BeanUtils.copyProperties(request, queryModel);
        if (ObjectUtils.isNotEmpty(queryModel.getDeviceType()) || ObjectUtils.isNotEmpty(queryModel.getGrade()) || ObjectUtils.isNotEmpty(request.getTenantVisible())
                || ObjectUtils.isNotEmpty(request.getStatus())) {
            // 查询故障告警设置是否存在
            FailureAlarmQueryModel failureAlarmQueryModel = FailureAlarmQueryModel.builder().deviceType(queryModel.getDeviceType()).grade(queryModel.getGrade())
                    .tenantVisible(request.getTenantVisible()).status(request.getStatus()).build();
            List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(failureAlarmQueryModel);
            if (ObjectUtils.isEmpty(failureAlarmList)) {
                log.warn("failure warn query alarm is empty");
                return Triple.of(true, null, Collections.emptyList());
            }
            
            List<String> signalIdList = failureAlarmList.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList());
            queryModel.setSignalIdList(signalIdList);
        }
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public R countTotal(EleHardwareFailureWarnMsgPageRequest request) {
        FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        if (triple.getLeft() && !Objects.isNull(triple.getRight())) {
            return R.ok(0);
        }
        
        Integer count = failureWarnMsgMapper.countTotal(queryModel);
        return R.ok(count);
    }
    
    /**
     * 故障告警分析：设备出货量，告警频率环比
     *
     * @param request
     * @return
     */
    @Slave
    @Override
    public Triple<Boolean, String, Object> calculateFrequency(EleHardwareFailureWarnMsgPageRequest request) {
        if (ObjectUtils.isEmpty(request.getAlarmStartTime())) {
            return Triple.of(false, "300828", "查询开始时间不能为空");
        }
        
        if (ObjectUtils.isEmpty(request.getAlarmEndTime())) {
            return Triple.of(false, "300829", "查询结束时间不能为空");
        }
        
        if (request.getAlarmStartTime() > request.getAlarmEndTime()) {
            return Triple.of(false, "300826", "查询结束时间不能小于开始时间");
        }
        
        // 使用天数
        long usageDays = DateUtils.diffDayV2(request.getAlarmStartTime(), request.getAlarmEndTime());
        
        if (usageDays > TimeConstant.ONE_MONTH) {
            return Triple.of(false, "300825", "查询天数不能大于30天");
        }
        
        FailureWarnFrequencyVo vo = new FailureWarnFrequencyVo();
        // 使用天数
        vo.setUsageDays(usageDays);
        
        FailureWarnMsgTaskQueryModel queryModel = FailureWarnMsgTaskQueryModel.builder().startTime(request.getAlarmStartTime()).endTime(request.getAlarmEndTime())
                .limitFailureAlarmStatus(FailureAlarm.enable).build();
        
        // 设置故障信息
        setFailureInfo(usageDays, vo, queryModel);
        
        // 设置故障和告警环比
        setFailureCycleRate(request, usageDays, vo, queryModel);
        
        return Triple.of(true, null, vo);
    }
    
    private void setFailureCycleRate(EleHardwareFailureWarnMsgPageRequest request, long usageDays, FailureWarnFrequencyVo vo, FailureWarnMsgTaskQueryModel queryModel) {
        Integer diffDays = -Integer.valueOf(String.valueOf(usageDays));
        
        // 计算上一个周期的开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(request.getAlarmStartTime());
        calendar.add(Calendar.DAY_OF_MONTH, diffDays);
        request.setAlarmStartTime(calendar.getTimeInMillis());
        
        // 计算上一个周期的结束时间
        calendar.setTimeInMillis(request.getAlarmEndTime());
        calendar.add(Calendar.DAY_OF_MONTH, diffDays);
        request.setAlarmEndTime(calendar.getTimeInMillis());
        
        // 统计选中时间段的告警次数
        queryModel.setStartTime(request.getAlarmStartTime());
        queryModel.setEndTime(request.getAlarmEndTime());
        List<EleHardwareFailureWarnMsgVo> failureWarnMsgList = failureWarnMsgMapper.countFailureWarnNum(queryModel);
        
        Integer failureNum = 0;
        Integer warnNum = 0;
        if (ObjectUtils.isNotEmpty(failureWarnMsgList)) {
            Map<Integer, Integer> failureNumMap = failureWarnMsgList.stream()
                    .collect(Collectors.toMap(EleHardwareFailureWarnMsgVo::getType, EleHardwareFailureWarnMsgVo::getFailureWarnNum));
            // 故障次数
            if (ObjectUtils.isNotEmpty(failureNumMap.get(EleHardwareFailureWarnMsg.FAILURE))) {
                failureNum = failureNumMap.get(EleHardwareFailureWarnMsg.FAILURE);
            }
            
            // 告警次数
            if (ObjectUtils.isNotEmpty(failureNumMap.get(EleHardwareFailureWarnMsg.WARN))) {
                warnNum = failureNumMap.get(EleHardwareFailureWarnMsg.WARN);
            }
        }
        
        BigDecimal failureCycleRate = BigDecimal.ZERO;
        // 环比增长速度=（本期数－上期数）÷上期数×100%，上一期为0，则环比为0
        if (!Objects.equals(failureNum, NumberConstant.ZERO)) {
            int i = vo.getFailureCount() - failureNum;
            BigDecimal bigDecimal = new BigDecimal(i);
            BigDecimal failureCountBig = new BigDecimal(String.valueOf(failureNum));
            failureCycleRate = bigDecimal.divide(failureCountBig, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }
        
        // 故障环比
        vo.setFailureCycleRate(failureCycleRate);
        
        BigDecimal warnCycleRate = BigDecimal.ZERO;
        // 环比增长速度=（本期数－上期数）÷上期数×100%，上一期为0，则环比为0
        if (!Objects.equals(warnNum, NumberConstant.ZERO)) {
            int i = vo.getWarnCount() - warnNum;
            BigDecimal bigDecimal = new BigDecimal(i);
            BigDecimal failureCountBig = new BigDecimal(String.valueOf(warnNum));
            warnCycleRate = bigDecimal.divide(failureCountBig, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }
        
        // 告警环比
        vo.setWarnCycleRate(warnCycleRate);
    }
    
    private void setFailureInfo(long usageDays, FailureWarnFrequencyVo vo, FailureWarnMsgTaskQueryModel queryModel) {
        // 统计平台中所有的柜机的数量
        ElectricityCabinetQuery electricityCabinetQuery = new ElectricityCabinetQuery();
        R r = cabinetService.queryCount(electricityCabinetQuery);
        Optional.ofNullable(r.getData()).ifPresent(i -> {
            Integer count = (Integer) r.getData();
            vo.setCabinetShipment(count);
        });
        
        // 统计选中时间段的告警次数
        List<EleHardwareFailureWarnMsgVo> failureWarnMsgList = failureWarnMsgMapper.countFailureWarnNum(queryModel);
        
        Integer failureNum = 0;
        Integer warnNum = 0;
        if (ObjectUtils.isNotEmpty(failureWarnMsgList)) {
            Map<Integer, Integer> failureNumMap = failureWarnMsgList.stream()
                    .collect(Collectors.toMap(EleHardwareFailureWarnMsgVo::getType, EleHardwareFailureWarnMsgVo::getFailureWarnNum));
            // 故障次数
            if (ObjectUtils.isNotEmpty(failureNumMap.get(EleHardwareFailureWarnMsg.FAILURE))) {
                failureNum = failureNumMap.get(EleHardwareFailureWarnMsg.FAILURE);
            }
            
            // 告警次数
            if (ObjectUtils.isNotEmpty(failureNumMap.get(EleHardwareFailureWarnMsg.WARN))) {
                warnNum = failureNumMap.get(EleHardwareFailureWarnMsg.WARN);
            }
        }
        
        vo.setFailureCount(failureNum);
        vo.setWarnCount(warnNum);
        
        // 计算故障率: 故障次数/使用天数
        BigDecimal failureCountBig = new BigDecimal(String.valueOf(vo.getFailureCount()));
        BigDecimal usageDaysBig = new BigDecimal(String.valueOf(usageDays));
        BigDecimal failureRate = failureCountBig.divide(usageDaysBig, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
        vo.setFailureRate(failureRate);
        
        // 计算告警率：告警次数除/使用天数
        BigDecimal warnCountBig = new BigDecimal(String.valueOf(vo.getWarnCount()));
        BigDecimal warnRate = warnCountBig.divide(usageDaysBig, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
        
        vo.setWarnRate(warnRate);
    }
    
    @Override
    public R superExportPage(EleHardwareFailureWarnMsgPageRequest request) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        
        FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            log.error("failure warn msg export check error info={}", triple.getRight());
            throw new CustomBusinessException((String) triple.getRight());
        }
        
        List<FailureWarnMsgExcelVo> list = new ArrayList<>();
        if (triple.getLeft() && Objects.isNull(triple.getRight())) {
            list = failureWarnMsgMapper.selectListExport(queryModel);
        }
        
        Integer type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode();
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
            type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode();
        }
        
        if (ObjectUtils.isNotEmpty(list)) {
            
            Map<Long, String> batterySnMap = new HashMap<>();
            setBatterySnMap(batterySnMap, list);
            
            Map<String, Map<String, String>> map = new HashMap<>();
            for (FailureWarnMsgExcelVo vo : list) {
                // 查询柜机sn
                ElectricityCabinet electricityCabinet = cabinetService.queryByIdFromCache(vo.getCabinetId());
                Optional.ofNullable(electricityCabinet).ifPresent(electricityCabinet1 -> {
                    vo.setSn(electricityCabinet1.getSn());
                });
                
                FailureAlarm failureAlarm = failureAlarmService.queryFromCacheBySignalId(vo.getSignalId());
                
                if (Objects.nonNull(failureAlarm) && Objects.equals(failureAlarm.getType(), type)) {
                    String signalName = failureAlarm.getSignalName();
                    
                    Map<String, String> descMap = map.get(failureAlarm.getSignalId());
                    if (ObjectUtils.isEmpty(descMap)) {
                        descMap = getDescMap(failureAlarm.getEventDesc());
                        map.put(failureAlarm.getSignalId(), descMap);
                    }
                    log.info("descMap:{},signalId:{}, desc:{}, res:{}", descMap, failureAlarm.getSignalId(), vo.getAlarmDesc(), descMap.get(vo.getAlarmDesc()));
                    if (ObjectUtils.isNotEmpty(descMap.get(vo.getAlarmDesc()))) {
                        signalName = signalName + CommonConstant.STR_COMMA + descMap.get(vo.getAlarmDesc());
                    }
                    
                    vo.setFailureAlarmName(signalName);
                    vo.setGrade(String.valueOf(failureAlarm.getGrade()));
                    vo.setDeviceType(String.valueOf(failureAlarm.getDeviceType()));
                    FailureAlarmDeviceTypeEnum deviceTypeEnum = BasicEnum.getEnum(Integer.valueOf(vo.getDeviceType()), FailureAlarmDeviceTypeEnum.class);
                    if (ObjectUtils.isNotEmpty(deviceTypeEnum)) {
                        vo.setDeviceType(deviceTypeEnum.getDesc());
                    }
                    
                    FailureAlarmGradeEnum gradeEnum = BasicEnum.getEnum(Integer.valueOf(vo.getGrade()), FailureAlarmGradeEnum.class);
                    if (ObjectUtils.isNotEmpty(gradeEnum)) {
                        vo.setGrade(gradeEnum.getDesc());
                    }
                } else if (Objects.isNull(request.getNoLimitSignalId())) {
                    vo.setFailureAlarmName("");
                    vo.setGrade("");
                    vo.setDeviceType("");
                }
                
                if (ObjectUtils.isNotEmpty(vo.getAlarmTime())) {
                    date.setTime(vo.getAlarmTime());
                    vo.setAlarmTimeExport(sdf.format(date));
                }
                
                if (ObjectUtils.isNotEmpty(vo.getRecoverTime())) {
                    date.setTime(vo.getRecoverTime());
                    vo.setRecoverTimeExport(sdf.format(date));
                }
                
                FailureWarnMsgStatusEnum statusEnum = BasicEnum.getEnum(vo.getAlarmFlag(), FailureWarnMsgStatusEnum.class);
                if (ObjectUtils.isNotEmpty(statusEnum)) {
                    vo.setAlarmFlagExport(statusEnum.getDesc());
                }
                
                if (Objects.equals(vo.getCellNo(), NumberConstant.ZERO)) {
                    vo.setCellNo(null);
                }
                
                if (ObjectUtils.isNotEmpty(vo.getBatterySn())) {
                    vo.setSn(vo.getBatterySn());
                }
                
                if (ObjectUtils.isNotEmpty(vo.getBatterySn())) {
                    vo.setSn(vo.getBatterySn());
                }
                
                // 根据电池id重置电池编号
                Long batteryId = vo.getBatteryId();
                if (ObjectUtils.isNotEmpty(batteryId) && ObjectUtils.isNotEmpty(batterySnMap.get(batteryId))) {
                    String batterySn = batterySnMap.get(batteryId);
                    vo.setSn(batterySn);
                }
            }
        }
        
        return R.ok(list);
    }
    
    private void setBatterySnMap(Map<Long, String> batterySnMap, List<FailureWarnMsgExcelVo> list) {
        Set<Long> batteryIdSet = list.stream().filter(item -> !Objects.equals(item.getBatteryId(), NumberConstant.ZERO_L)).map(FailureWarnMsgExcelVo::getBatteryId)
                .collect(Collectors.toSet());
        
        if (ObjectUtils.isNotEmpty(batteryIdSet)) {
            List<Long> collect = batteryIdSet.stream().collect(Collectors.toList());
            List<List<Long>> partition = ListUtils.partition(collect, 500);
            List<ElectricityBattery> batteryList = new ArrayList<>();
            partition.stream().forEach(item -> {
                List<ElectricityBattery> batteryList1 = batteryService.queryListByIdList(item);
                if (ObjectUtils.isNotEmpty(batteryList1)) {
                    batteryList.addAll(batteryList1);
                }
            });
            if (ObjectUtils.isNotEmpty(batteryList)) {
                batterySnMap = batteryList.stream().collect(Collectors.toMap(ElectricityBattery::getId, ElectricityBattery::getSn));
            }
        }
    }
    
    private Map<String, String> getDescMap(String eventDesc) {
        Map<String, String> resMap = new HashMap<>();
        if (StringUtils.isNotEmpty(eventDesc)) {
            eventDesc = eventDesc.replace("：", ":");
            String[] split = eventDesc.split(StringConstant.CHANGE_ROW);
            if (ObjectUtils.isNotEmpty(split) && split.length > 0) {
                for (String desc : split) {
                    String[] dArr = desc.split(":");
                    if (ObjectUtils.isNotEmpty(dArr) && dArr.length == 2) {
                        resMap.put(dArr[0], desc);
                    }
                }
            }
        }
        
        return resMap;
    }
    
    @Slave
    @Override
    public Triple<Boolean, String, Object> proportion(EleHardwareFailureWarnMsgPageRequest request) {
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            log.error("failure warn proportion params is error={}", triple.getRight());
            return triple;
        }
        
        FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
        queryModel.setType(request.getType());
        queryModel.setAlarmStartTime(request.getAlarmStartTime());
        queryModel.setAlarmEndTime(request.getAlarmEndTime());
        
        // 统计每个信号量故障或者告警的数量
        List<FailureWarnProportionVo> list = failureWarnMsgMapper.selectListProportion(queryModel);
        Map<String, Integer> failureMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(list)) {
            failureMap = list.stream().collect(Collectors.toMap(FailureWarnProportionVo::getSignalId, FailureWarnProportionVo::getCount));
        }
        
        List<FailureWarnProportionVo> result = null;
        
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN)) {
            result = warnProportion(failureMap);
        } else if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
            result = failureProportion(failureMap);
        }
        
        return Triple.of(true, null, result);
    }
    
    @Slave
    @Override
    public void proportionExport(EleHardwareFailureWarnMsgPageRequest request, HttpServletResponse response) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            throw new CustomBusinessException((String) triple.getRight());
        }
        
        FailureWarnMsgPageQueryModel queryModel = FailureWarnMsgPageQueryModel.builder().alarmStartTime(request.getAlarmStartTime()).alarmEndTime(request.getAlarmEndTime())
                .type(request.getType()).build();
        
        List<FailureWarnProportionExportVo> exportVoList = new ArrayList<>();
        
        // 统计每个信号量故障或者告警的数量
        List<FailureWarnProportionVo> failureWarnProportionVos = failureWarnMsgMapper.selectListProportion(queryModel);
        if (ObjectUtils.isNotEmpty(failureWarnProportionVos)) {
            List<FailureWarnProportionVo> list = failureWarnProportionVos.stream().sorted(Comparator.comparing(FailureWarnProportionVo::getCount, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
            int i = 1;
            
            for (FailureWarnProportionVo item : list) {
                FailureWarnProportionExportVo vo = new FailureWarnProportionExportVo();
                vo.setSignalId(item.getSignalId());
                vo.setNum(item.getCount());
                vo.setSerialNumber(i++);
                
                // 查询故障告警设置
                FailureAlarm failureAlarm = failureAlarmService.queryFromCacheBySignalId(item.getSignalId());
                Optional.ofNullable(failureAlarm).ifPresent(failureAlarm1 -> {
                    // 设备分类
                    FailureAlarmDeviceTypeEnum deviceTypeEnum = BasicEnum.getEnum(Integer.valueOf(failureAlarm1.getDeviceType()), FailureAlarmDeviceTypeEnum.class);
                    if (ObjectUtils.isNotEmpty(deviceTypeEnum)) {
                        vo.setDeviceType(deviceTypeEnum.getDesc());
                    }
                    
                    // 等级
                    FailureAlarmGradeEnum gradeEnum = BasicEnum.getEnum(Integer.valueOf(failureAlarm1.getGrade()), FailureAlarmGradeEnum.class);
                    if (ObjectUtils.isNotEmpty(gradeEnum)) {
                        vo.setGrade(gradeEnum.getDesc());
                    }
                    
                    // 标准名
                    vo.setSignalName(failureAlarm1.getSignalName());
                });
                
                exportVoList.add(vo);
            }
        }
        
        String fileName = "故障告警占比导出.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, FailureWarnProportionExportVo.class).sheet("sheet").doWrite(exportVoList);
            return;
        } catch (IOException e) {
            log.error("failure warn proportion export Export error", e);
        }
    }
    
    @Override
    @Klock(name = "warnNoteNotice", keys = {"#warnNoteCallBack.tenantId"}, waitTime = 5, customLockTimeoutStrategy = "warnNoteNoticeFail")
    public Triple<Boolean, String, Object> warnNoteNotice(WarnNoteCallBack warnNoteCallBack) {
        log.info("warn note notice info! failure warn sessionId = {}, alarmId={}, tenantId={}, count={}", warnNoteCallBack.getSessionId(), warnNoteCallBack.getAlarmId(),
                warnNoteCallBack.getTenantId(), warnNoteCallBack.getCount());
        
        if (Objects.isNull(warnNoteCallBack.getCount()) || warnNoteCallBack.getCount() < NumberConstant.ZERO_L) {
            log.warn("warn note notice info! failure warn count is empty, count={}", warnNoteCallBack.getCount());
            return Triple.of(false, "300832", "无效的短信次数");
        }
        
        if (Objects.isNull(warnNoteCallBack.getTenantId())) {
            log.warn("warn note notice info! failure warn tenantId is empty, tenantId={}", warnNoteCallBack.getTenantId());
            return Triple.of(false, "300833", "无效的租户id");
        }
        
        if (Objects.isNull(warnNoteCallBack.getAlarmId())) {
            log.warn("warn note notice info! failure warn alarmId is empty, alarmId={}", warnNoteCallBack.getAlarmId());
            return Triple.of(false, "300834", "无效的告警id");
        }
        
        // 检测告警是否存在
        int count = eleHardwareWarnMsgService.existByAlarmId(warnNoteCallBack.getAlarmId());
        if (count == 0) {
            log.warn("warn note notice info! failure warn alarmId is not exist, alarmId={}", warnNoteCallBack.getAlarmId());
            return Triple.of(false, "300835", "无效的告警id");
        }
        
        // 修改租户对应的短信次数：更新缓存等。
        // 查询每个租户对应的短信的次数
        TenantNote tenantNote = tenantNoteService.queryFromCacheByTenantId(warnNoteCallBack.getTenantId());
        
        // 判断租户是否分配过短信次数
        if (ObjectUtils.isEmpty(tenantNote)) {
            log.warn("warn note notice error! failure warn not find tenant note config, tenantId={}", warnNoteCallBack.getTenantId());
            return Triple.of(false, "300836", "租户未充值短信");
        }
        
        TenantNote tenantNoteUpdate = new TenantNote();
        tenantNoteUpdate.setId(tenantNote.getId());
        tenantNoteUpdate.setNoteNum(warnNoteCallBack.getCount());
        tenantNoteUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 扣减短信次数
        tenantNoteService.reduceNoteNumById(tenantNoteUpdate);
        
        // 修改短信标志
        eleHardwareWarnMsgService.updateNoteFlagByAlarmId(warnNoteCallBack.getAlarmId());
        
        Long noteNum = tenantNote.getNoteNum();
        for (int i = 0; i < warnNoteCallBack.getCount(); i++) {
            noteNum = noteNum - NumberConstant.ONE_L;
            if (Objects.equals(noteNum, TenantNoteConstant.NOTE_NUM_FIRST) || Objects.equals(noteNum, TenantNoteConstant.NOTE_NUM_SECOND) || Objects.equals(noteNum,
                    TenantNoteConstant.NOTE_NUM_THIRD)) {
                
                Long finalNoteNum = noteNum;
                xllThreadPoolExecutorService.execute(() -> {
                    sendLowerNoteNotice(warnNoteCallBack, finalNoteNum);
                });
                break;
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    private void warnNoteNoticeFail(WarnNoteCallBack warnNoteCallBack) {
        log.error("WARN NOTE NOTICE FAIL ERROR, msg = {}", warnNoteCallBack);
    }
    
    private void sendLowerNoteNotice(WarnNoteCallBack warnNoteCallBack, Long noteNum) {
        Integer tenantId = warnNoteCallBack.getTenantId();
        // 平台名称
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig)) {
            log.warn("send lower note notice warn! not find electricity config, tenantId={}", tenantId);
        }
        
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones()) || ObjectUtils.isEmpty(notifyConfig.getPermissions())) {
            log.warn("send lower note notice warn! not found maintenanceUserNotifyConfig,tenantId={}", tenantId);
            return;
        }
        
        List<String> phones = JSON.parseObject(notifyConfig.getPhones(), List.class);
        if (CollectionUtils.isEmpty(phones)) {
            log.info("send lower note notice warn! phones is empty,tenantId={}", tenantId);
            return;
        }
        
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        
        MessageCenterRequest messageCenterRequest = new MessageCenterRequest();
        // 消息模板编码
        messageCenterRequest.setMessageTemplateCode(messageCenterConfig.getLowNoteNoticeMessageTemplateCode());
        // 消息id
        messageCenterRequest.setMessageId(sessionId);
        // 租户id
        messageCenterRequest.setTenantId(tenantId);
        
        // 设置消息内容
        TenantNoteLowerNotice tenantNoteLowerNotice = TenantNoteLowerNotice.builder().platformName(electricityConfig.getName()).noteNum(noteNum).build();
        messageCenterRequest.setParamMap(tenantNoteLowerNotice);
        
        // 短信通知
        MessageCenterSendReceiver noteMessageCenterSendReceiver = MessageCenterSendReceiver.builder().sendChannel(MessageCenterConstant.SEND_CHANNEL_NOTE).callBackUrl(null)
                .callBackMap(null).receiver(phones).build();
        
        // 消息接收者
        List<MessageCenterSendReceiver> sendReceiverList = new ArrayList<>();
        sendReceiverList.add(noteMessageCenterSendReceiver);
        
        messageCenterRequest.setSendReceiverList(sendReceiverList);
        try {
            ResponseEntity<String> responseEntity = restTemplateService.postJsonForResponseEntity(messageCenterConfig.getUrl(), JsonUtil.toJson(messageCenterRequest), null);
            
            if (Objects.isNull(responseEntity)) {
                log.error("send lower note notice error! failure warn send note result is null, sessionId={}, alarmId={}", sessionId, warnNoteCallBack.getAlarmId());
            }
            
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("send lower note notice error! failure warn send note error, sessionId={}, alarmId={}, msg = {}", sessionId, warnNoteCallBack.getAlarmId(),
                        responseEntity.getBody());
            }
            
        } catch (Exception e) {
            log.error("send lower note notice error! exception, sessionId={}, alarmId={}, e = {}", sessionId, warnNoteCallBack.getAlarmId(), e);
        }
    }
    
    private void createFranchiseeSplitAccountLockFail(Franchisee franchisee, ElectricityTradeOrder payRecord, int percent) {
        log.error("ELE ORDER ERROR! handleSplitAccount error! franchiseeId={}", franchisee.getId());
    }
    
    private List<FailureWarnProportionVo> failureProportion(Map<String, Integer> failureMap) {
        FailureAlarmQueryModel alarmQueryModel = FailureAlarmQueryModel.builder().type(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode()).status(FailureAlarm.enable)
                .build();
        List<FailureAlarm> failureAlarmList = failureAlarmMapper.selectList(alarmQueryModel);
        Map<Integer, List<FailureAlarm>> gradeMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(failureAlarmList)) {
            gradeMap = failureAlarmList.stream().collect(Collectors.groupingBy(FailureAlarm::getGrade));
        }
        
        List<FailureWarnProportionVo> resultList = new ArrayList<>();
        
        for (FailureAlarmGradeEnum alarmGradeEnum : FailureAlarmGradeEnum.values()) {
            FailureWarnProportionVo vo = new FailureWarnProportionVo();
            vo.setName(alarmGradeEnum.getDesc() + FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getDesc());
            vo.setPath(vo.getName());
            Integer count = 0;
            if (ObjectUtils.isNotEmpty(gradeMap.get(alarmGradeEnum.getCode()))) {
                List<FailureWarnProportionVo> children = new ArrayList<>();
                List<FailureAlarm> alarmList = gradeMap.get(alarmGradeEnum.getCode());
                for (FailureAlarm failureAlarm : alarmList) {
                    if (ObjectUtils.isNotEmpty(failureMap.get(failureAlarm.getSignalId()))) {
                        count += failureMap.get(failureAlarm.getSignalId());
                        FailureWarnProportionVo failureWarnProportionVo = new FailureWarnProportionVo();
                        failureWarnProportionVo.setName(failureAlarm.getSignalName());
                        failureWarnProportionVo.setPath(vo.getPath() + StringConstant.FORWARD_SLASH + failureAlarm.getSignalName());
                        failureWarnProportionVo.setValue(failureMap.get(failureAlarm.getSignalId()));
                        children.add(failureWarnProportionVo);
                    }
                }
                
                if (count > 0) {
                    vo.setChildren(children);
                    vo.setValue(count);
                    resultList.add(vo);
                }
            }
        }
        
        return resultList;
    }
    
    private List<FailureWarnProportionVo> warnProportion(Map<String, Integer> failureMap) {
        FailureAlarmQueryModel alarmQueryModel = FailureAlarmQueryModel.builder().type(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode()).status(FailureAlarm.enable)
                .build();
        List<FailureAlarm> failureAlarmList = failureAlarmMapper.selectList(alarmQueryModel);
        Map<Integer, List<FailureAlarm>> gradeMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(failureAlarmList)) {
            gradeMap = failureAlarmList.stream().collect(Collectors.groupingBy(FailureAlarm::getGrade));
        }
        
        List<FailureWarnProportionVo> resultList = new ArrayList<>();
        
        for (FailureAlarmGradeEnum alarmGradeEnum : FailureAlarmGradeEnum.values()) {
            FailureWarnProportionVo vo = new FailureWarnProportionVo();
            vo.setName(alarmGradeEnum.getDesc() + FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getDesc());
            vo.setPath(vo.getName());
            Integer count = 0;
            if (ObjectUtils.isNotEmpty(gradeMap.get(alarmGradeEnum.getCode()))) {
                List<FailureWarnProportionVo> children = new ArrayList<>();
                List<FailureAlarm> alarmList = gradeMap.get(alarmGradeEnum.getCode());
                for (FailureAlarm failureAlarm : alarmList) {
                    if (ObjectUtils.isNotEmpty(failureMap.get(failureAlarm.getSignalId()))) {
                        count += failureMap.get(failureAlarm.getSignalId());
                        FailureWarnProportionVo failureWarnProportionVo = new FailureWarnProportionVo();
                        failureWarnProportionVo.setName(failureAlarm.getSignalName());
                        failureWarnProportionVo.setPath(vo.getPath() + StringConstant.FORWARD_SLASH + failureAlarm.getSignalName());
                        failureWarnProportionVo.setValue(failureMap.get(failureAlarm.getSignalId()));
                        children.add(failureWarnProportionVo);
                    }
                }
                
                if (count > 0) {
                    vo.setChildren(children);
                    vo.setValue(count);
                    resultList.add(vo);
                }
            }
        }
        
        return resultList;
    }
    
    private Triple<Boolean, String, Object> checkParams(EleHardwareFailureWarnMsgPageRequest request) {
        if (ObjectUtils.isEmpty(request.getAlarmStartTime())) {
            return Triple.of(false, "300828", "查询开始时间不能为空");
        }
        
        if (ObjectUtils.isEmpty(request.getAlarmEndTime())) {
            return Triple.of(false, "300829", "查询结束时间不能为空");
        }
        
        if (request.getAlarmStartTime() > request.getAlarmEndTime()) {
            return Triple.of(false, "300826", "查询结束时间不能小于开始时间");
        }
        
        if (!(Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN) || Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE))) {
            return Triple.of(false, "300827", "请选择正确的故障类型");
        }
        
        // 使用天数
        long usageDays = DateUtils.diffDayV2(request.getAlarmStartTime(), request.getAlarmEndTime());
        
        if (usageDays > TimeConstant.ONE_MONTH) {
            return Triple.of(false, "300825", "查询天数不能大于30天");
        }
        
        return Triple.of(true, null, null);
    }
    
}
