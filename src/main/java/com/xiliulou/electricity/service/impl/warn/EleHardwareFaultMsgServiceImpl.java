package com.xiliulou.electricity.service.impl.warn;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.warn.EleHardwareFaultMsg;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmDeviceTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmGradeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureWarnMsgStatusEnum;
import com.xiliulou.electricity.mapper.FailureAlarmMapper;
import com.xiliulou.electricity.mapper.warn.EleHardwareFaultMsgMapper;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureAlarmQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureFaultMsgTaskQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FaultMsgPageQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFaultMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FailureAlarmService;
import com.xiliulou.electricity.service.warn.EleHardwareFaultMsgService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionExportVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;
import com.xiliulou.electricity.vo.warn.EleHardwareFaultMsgPageVo;
import com.xiliulou.electricity.vo.warn.FaultMsgExcelVo;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/5/23 11:19
 * @desc
 */
@Service
@Slf4j
public class EleHardwareFaultMsgServiceImpl implements EleHardwareFaultMsgService {
    
    @Resource
    private EleHardwareFaultMsgMapper eleHardwareFaultMsgMapper;
    
    @Resource
    private FailureAlarmService failureAlarmService;
    
    @Resource
    private ElectricityCabinetService cabinetService;
    
    @Resource
    private FailureAlarmMapper failureAlarmMapper;
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    @DS(value = "clickhouse")
    public List<EleHardwareFailureWarnMsgVo> list(FailureAlarmTaskQueryRequest request) {
        FailureFaultMsgTaskQueryModel queryModel = FailureFaultMsgTaskQueryModel.builder().build();
        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(request.getStartTime() / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(request.getEndTime() / 1000, 0, ZoneOffset.ofHours(8));
        queryModel.setStartTime(formatter.format(beginLocalDateTime));
        queryModel.setEndTime(formatter.format(endLocalDateTime));
        
        return eleHardwareFaultMsgMapper.selectList(queryModel);
    }
    
    @Override
    @Slave
    public R transferListPage(List<EleHardwareFaultMsg> eleHardwareFaultMsgList, EleHardwareFaultMsgPageRequest request) {
        Integer type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode();
        
        List<EleHardwareFaultMsgPageVo> resultList = new ArrayList<>();
        Integer finalType = type;
        eleHardwareFaultMsgList.forEach(item -> {
            EleHardwareFaultMsgPageVo vo = new EleHardwareFaultMsgPageVo();
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
    
    @Override
    @DS(value = "clickhouse")
    public R countTotal(FaultMsgPageQueryModel queryModel) {
        return R.ok(eleHardwareFaultMsgMapper.countTotal(queryModel));
    }
    
    @Override
    @Slave
    public R superExportPage(EleHardwareFaultMsgPageRequest request, List<FaultMsgExcelVo> list) {
        Integer type = FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode();
        
        if (ObjectUtils.isNotEmpty(list)) {
            Map<String, Map<String, String>> map = new HashMap<>();
            for (FaultMsgExcelVo vo : list) {
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
                
                FailureWarnMsgStatusEnum statusEnum = BasicEnum.getEnum(vo.getAlarmFlag(), FailureWarnMsgStatusEnum.class);
                if (ObjectUtils.isNotEmpty(statusEnum)) {
                    vo.setAlarmFlagExport(statusEnum.getDesc());
                }
                
                if (Objects.equals(vo.getCellNo(), NumberConstant.ZERO)) {
                    vo.setCellNo(null);
                }
            }
        }
        
        return R.ok(list);
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
    
    @Override
    public Triple<Boolean, String, Object> checkAndInitQuery(EleHardwareFaultMsgPageRequest request, FaultMsgPageQueryModel queryModel, int daySize) {
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
        if (ObjectUtils.isNotEmpty(request.getTenantVisible()) || ObjectUtils.isNotEmpty(request.getStatus())) {
            // 查询故障告警设置是否存在
            FailureAlarmQueryModel failureAlarmQueryModel = FailureAlarmQueryModel.builder().tenantVisible(request.getTenantVisible()).status(request.getStatus()).build();
            List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(failureAlarmQueryModel);
            if (ObjectUtils.isEmpty(failureAlarmList)) {
                log.error("failure warn query alarm is empty");
                return Triple.of(true, null, Collections.emptyList());
            }
            
            List<String> signalIdList = failureAlarmList.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList());
            queryModel.setSignalIdList(signalIdList);
        }
        
        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(request.getAlarmStartTime() / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(request.getAlarmEndTime() / 1000, 0, ZoneOffset.ofHours(8));
        queryModel.setStartTime(formatter.format(beginLocalDateTime));
        queryModel.setEndTime(formatter.format(endLocalDateTime));
        
        return Triple.of(true, null, null);
    }
    
    @Override
    @DS(value = "clickhouse")
    public List<EleHardwareFaultMsg> listByPage(EleHardwareFaultMsgPageRequest request, FaultMsgPageQueryModel queryModel) {
        return eleHardwareFaultMsgMapper.selectListByPage(queryModel);
    }
    
    @Override
    @DS(value = "clickhouse")
    public List<FaultMsgExcelVo> listExportData(FaultMsgPageQueryModel queryModel, Triple<Boolean, String, Object> triple) {
        List<FaultMsgExcelVo> list = new ArrayList<>();
        
        if (triple.getLeft() && Objects.isNull(triple.getRight())) {
            list = eleHardwareFaultMsgMapper.selectListExport(queryModel);
        }
        
        return list;
    }
    
    @Override
    @DS(value = "clickhouse")
    public List<FailureWarnProportionVo> listProportion(EleHardwareFaultMsgPageRequest request) {
        FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
        queryModel.setSignalIdList(request.getSignalIdList());
        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(request.getAlarmStartTime() / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(request.getAlarmEndTime() / 1000, 0, ZoneOffset.ofHours(8));
        queryModel.setStartTime(formatter.format(beginLocalDateTime));
        queryModel.setEndTime(formatter.format(endLocalDateTime));
        
        return eleHardwareFaultMsgMapper.selectListProportion(queryModel);
    }
    
    @Override
    @Slave
    public List<FailureWarnProportionVo> faultProportion(Map<String, Integer> failureMap) {
        Map<Integer, List<FailureAlarm>> gradeMap = new HashMap<>();
        
        FailureAlarmQueryModel alarmQueryModel = FailureAlarmQueryModel.builder().type(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode()).status(FailureAlarm.enable)
                .build();
        List<FailureAlarm> failureAlarmList = failureAlarmMapper.selectList(alarmQueryModel);
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
    
    @Override
    public void proportionExport(List<FailureWarnProportionVo> failureWarnProportionVos, HttpServletResponse response) {
        List<FailureWarnProportionExportVo> exportVoList = new ArrayList<>();
        
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
    @DS(value = "clickhouse")
    public void setFailureInfo(FailureWarnFrequencyVo vo, FailureWarnMsgPageQueryModel queryModel) {
        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(queryModel.getAlarmStartTime() / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(queryModel.getAlarmEndTime() / 1000, 0, ZoneOffset.ofHours(8));
        queryModel.setStartTime(formatter.format(beginLocalDateTime));
        queryModel.setEndTime(formatter.format(endLocalDateTime));
        
        // 统计选中时间段的告警次数
        Integer faultNum = eleHardwareFaultMsgMapper.countFaultNum(queryModel);
        
        vo.setFailureCount(faultNum);
    }
    
}
