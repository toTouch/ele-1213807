package com.xiliulou.electricity.service.impl.warn;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.warn.EleHardwareWarnMsg;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmDeviceTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmGradeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureWarnMsgStatusEnum;
import com.xiliulou.electricity.mapper.FailureAlarmMapper;
import com.xiliulou.electricity.mapper.warn.EleHardwareWarnMsgMapper;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureAlarmQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgTaskQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.WarnMsgPageQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FailureAlarmService;
import com.xiliulou.electricity.service.warn.EleHardwareWarnMsgService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgPageVo;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnMsgExcelVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionExportVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/5/23 11:20
 * @desc
 */
@Service
@Slf4j
public class EleHardwareWarnMsgServiceImpl implements EleHardwareWarnMsgService {
    
    @Resource
    private EleHardwareWarnMsgMapper eleHardwareWarnMsgMapper;
    
    @Resource
    private FailureAlarmService failureAlarmService;
    
    @Resource
    private ElectricityCabinetService cabinetService;
    
    @Resource
    private FailureAlarmMapper failureAlarmMapper;
    
    @Slave
    @Override
    public List<EleHardwareFailureWarnMsgVo> list(FailureAlarmTaskQueryRequest request) {
        FailureWarnMsgTaskQueryModel queryModel = FailureWarnMsgTaskQueryModel.builder().startTime(request.getStartTime()).endTime(request.getEndTime()).build();
        
        return eleHardwareWarnMsgMapper.selectList(queryModel);
    }
    
    @Slave
    @Override
    public R listByPage(EleHardwareWarnMsgPageRequest request) {
        WarnMsgPageQueryModel queryModel = new WarnMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        if (triple.getLeft() && !Objects.isNull(triple.getRight())) {
            return R.ok(Collections.emptyList());
        }
        
        List<EleHardwareWarnMsg> list = eleHardwareWarnMsgMapper.selectListByPage(queryModel);
        
        if (ObjectUtils.isEmpty(list)) {
            return R.ok(Collections.emptyList());
        }
        
        Integer type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode();
        
        List<EleHardwareFailureWarnMsgPageVo> resultList = new ArrayList<>();
        Integer finalType = type;
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
            
            resultList.add(vo);
        });
        
        return R.ok(resultList);
    }
    
    @Slave
    @Override
    public R countTotal(EleHardwareWarnMsgPageRequest request) {
        WarnMsgPageQueryModel queryModel = new WarnMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
        
        if (triple.getLeft() && !Objects.isNull(triple.getRight())) {
            return R.ok(0);
        }
        
        Integer count = eleHardwareWarnMsgMapper.countTotal(queryModel);
        return R.ok(count);
    }
    
    @Override
    @Slave
    public R superExportPage(EleHardwareWarnMsgPageRequest request) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        
        WarnMsgPageQueryModel queryModel = new WarnMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            log.error("warn msg export check error info={}", triple.getRight());
            throw new CustomBusinessException((String) triple.getRight());
        }
        
        List<FailureWarnMsgExcelVo> list = new ArrayList<>();
        if (triple.getLeft() && Objects.isNull(triple.getRight())) {
            list = eleHardwareWarnMsgMapper.selectListExport(queryModel);
        }
        
        Integer type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode();
        
        if (ObjectUtils.isNotEmpty(list)) {
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
            }
        }
        
        return R.ok(list);
    }
    
    private Triple<Boolean, String, Object> checkParams(EleHardwareWarnMsgPageRequest request) {
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
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @Slave
    public Triple<Boolean, String, Object> proportion(EleHardwareWarnMsgPageRequest request) {
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            log.error("warn proportion params is error={}", triple.getRight());
            return triple;
        }
    
        List<FailureWarnProportionVo> list = new ArrayList<>();
        
        FailureAlarmQueryModel failureAlarmQueryModel = FailureAlarmQueryModel.builder().status(FailureAlarm.enable).type(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode()).build();
        List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(failureAlarmQueryModel);
        if (ObjectUtils.isNotEmpty(failureAlarmList)) {
            List<String> signalIdList = failureAlarmList.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList());
    
            FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
            queryModel.setAlarmStartTime(request.getAlarmStartTime());
            queryModel.setAlarmEndTime(request.getAlarmEndTime());
            queryModel.setSignalIdList(signalIdList);
            
            list = eleHardwareWarnMsgMapper.selectListProportion(queryModel);
        }
        
        // 统计每个信号量故障或者告警的数量
        Map<String, Integer> failureMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(list)) {
            failureMap = list.stream().collect(Collectors.toMap(FailureWarnProportionVo::getSignalId, FailureWarnProportionVo::getCount));
        }
        
        List<FailureWarnProportionVo> result = warnProportion(failureMap);
        
        return Triple.of(true, null, result);
    }
    
    @Override
    public void proportionExport(EleHardwareWarnMsgPageRequest request, HttpServletResponse response) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            throw new CustomBusinessException((String) triple.getRight());
        }
    
        List<FailureWarnProportionExportVo> exportVoList = new ArrayList<>();
        List<FailureWarnProportionVo> failureWarnProportionVos = new ArrayList<>();
        
        FailureAlarmQueryModel failureAlarmQueryModel = FailureAlarmQueryModel.builder().status(FailureAlarm.enable).type(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode()).build();
        List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(failureAlarmQueryModel);
        if (ObjectUtils.isNotEmpty(failureAlarmList)) {
            List<String> signalIdList = failureAlarmList.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList());
        
            FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
            queryModel.setAlarmStartTime(request.getAlarmStartTime());
            queryModel.setAlarmEndTime(request.getAlarmEndTime());
            queryModel.setSignalIdList(signalIdList);
    
            failureWarnProportionVos = eleHardwareWarnMsgMapper.selectListProportion(queryModel);
        }
        
        if (ObjectUtils.isNotEmpty(failureWarnProportionVos)) {
            List<FailureWarnProportionVo> list = failureWarnProportionVos.stream().sorted(Comparator.comparing(FailureWarnProportionVo::getCount, Comparator.reverseOrder())).collect(Collectors.toList());
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
    @Slave
    public void setWarnInfo(FailureWarnFrequencyVo vo, FailureWarnMsgPageQueryModel queryModel) {
        // 统计选中时间段的告警次数
        Integer warnNum = eleHardwareWarnMsgMapper.countWarnNum(queryModel);
        vo.setWarnCount(warnNum);
    }
    
    @Override
    @Slave
    public int existByAlarmId(String alarmId) {
        return eleHardwareWarnMsgMapper.existByAlarmId(alarmId);
    }
    
    @Override
    public int updateNoteFlagByAlarmId(String alarmId) {
        return eleHardwareWarnMsgMapper.updateNoteFlagByAlarmId(alarmId, System.currentTimeMillis());
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
    
    
    private Triple<Boolean, String, Object> checkAndInitQuery(EleHardwareWarnMsgPageRequest request, WarnMsgPageQueryModel queryModel, int daySize) {
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
                log.error("failure warn query alarm is empty");
                return Triple.of(true, null, Collections.emptyList());
            }
            
            List<String> signalIdList = failureAlarmList.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList());
            queryModel.setSignalIdList(signalIdList);
        }
        
        return Triple.of(true, null, null);
    }
}
