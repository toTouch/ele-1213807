package com.xiliulou.electricity.service.impl;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmDeviceTypeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmGradeEnum;
import com.xiliulou.electricity.enums.failureAlarm.FailureWarnMsgStatusEnum;
import com.xiliulou.electricity.mapper.EleHardwareFailureWarnMsgMapper;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgTaskQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FailureAlarmService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgPageVo;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnMsgExcelVo;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private ElectricityCabinetService cabinetService;
    
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
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, 30);
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
    
        List<EleHardwareFailureWarnMsgPageVo> resultList = new ArrayList<>();
        list.forEach(item -> {
            EleHardwareFailureWarnMsgPageVo vo = new EleHardwareFailureWarnMsgPageVo();
            BeanUtils.copyProperties(item, vo);
            
            if (ObjectUtils.isEmpty(item.getFailureAlarmName())) {
                FailureAlarm failureAlarm = failureAlarmService.queryFromCacheBySignalId(item.getSignalId());
                Optional.ofNullable(failureAlarm).ifPresent(i -> {
                    String signalName = failureAlarm.getSignalName();
                    if (StringUtils.isNotEmpty(failureAlarm.getEventDesc())) {
                        signalName = signalName + CommonConstant.STR_COMMA + failureAlarm.getEventDesc();
                    }
                    item.setFailureAlarmName(signalName);
                });
            }
            resultList.add(vo);
        });
        
        return R.ok(list);
    }
    
    private Triple<Boolean, String, Object> checkAndInitQuery(EleHardwareFailureWarnMsgPageRequest request, FailureWarnMsgPageQueryModel queryModel, int daySize) {
        // 计算查询时间不能大于三十天
        if (request.getAlarmStartTime() < request.getAlarmEndTime()) {
            return Triple.of(false, "300826", "查询结束时间不能小于开始时间");
        }
        
        long days = DateUtils.diffDay(request.getAlarmStartTime(), request.getAlarmEndTime());
        if (days > daySize) {
            return Triple.of(false, "300825", String.format("查询天数不能大于%s天", daySize));
        }
    
        // 设置查询参数
        BeanUtils.copyProperties(request, queryModel);
        if (ObjectUtils.isNotEmpty(queryModel.getDeviceType()) || ObjectUtils.isNotEmpty(queryModel.getGrade()) || ObjectUtils.isNotEmpty(request.getTenantVisible())
                || ObjectUtils.isNotEmpty(request.getStatus())) {
            // 查询故障告警设置是否存在
            List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(queryModel.getDeviceType(), queryModel.getGrade(), request.getTenantVisible(),
                    request.getStatus());
            if (ObjectUtils.isEmpty(failureAlarmList)) {
                log.error("failure warn query alarm is empty");
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
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, 30);
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
     * @param request
     * @return
     */
    @Slave
    @Override
    public Triple<Boolean, String, Object> calculateFrequency(EleHardwareFailureWarnMsgPageRequest request) {
        FailureWarnFrequencyVo vo = new FailureWarnFrequencyVo();
        // 统计平台中所有的柜机的数量
        ElectricityCabinetQuery electricityCabinetQuery = new ElectricityCabinetQuery();
        R r = cabinetService.queryCount(electricityCabinetQuery);
        Optional.ofNullable(r.getData()).ifPresent(i -> {
            Integer count = (Integer) r.getData();
            vo.setCabinetShipment(count);
        });
    
        if (request.getAlarmStartTime() > request.getAlarmEndTime()) {
            return Triple.of(false, "", "开始时间不能大于结束时间");
        }
        
        // 使用天数
        long usageDays = DateUtils.diffDayV2(request.getAlarmStartTime(), request.getAlarmEndTime());
        vo.setUsageDays(usageDays);
        
        // 统计选中时间段的告警次数
        FailureWarnMsgTaskQueryModel queryModel = FailureWarnMsgTaskQueryModel.builder().startTime(request.getAlarmStartTime()).endTime(request.getAlarmEndTime()).build();
        List<EleHardwareFailureWarnMsgVo> failureWarnMsgList = failureWarnMsgMapper.selectList(queryModel);
    
        if (ObjectUtils.isEmpty(failureWarnMsgList)) {
            log.error("Hardware Failure CabinetMsg task is empty");
        }
    
//        Map<Integer, EleHardwareFailureCabinetMsg> cabinetMsgMap = failureWarnMsgList.stream().collect(
//                Collectors.groupingBy(EleHardwareFailureWarnMsgVo::getCabinetId, Collectors.collectingAndThen(Collectors.toList(), e -> this.getCabinetFailureWarnMsg(e, request))));
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public void exportExcel(EleHardwareFailureWarnMsgPageRequest request, HttpServletResponse response) {
        Long userId = SecurityUtils.getUid();
        if (Objects.isNull(userId)) {
            throw new CustomBusinessException("未查询到用户");
        }
        
        if (ObjectUtils.isEmpty(request.getDays())) {
            request.setDays(1);
        }
        
        if (ObjectUtils.isEmpty(request.getSize())) {
            request.setSize(5000L);
        }
    
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
    
        FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel, 1);
        if (!triple.getLeft()) {
            log.error("failure warn msg export check error info={}", triple.getRight());
            throw new CustomBusinessException((String) triple.getRight());
        }
        
        List<FailureWarnMsgExcelVo> list = new ArrayList<>();
        if (triple.getLeft() && Objects.isNull(triple.getRight())) {
            list = failureWarnMsgMapper.selectListExport(queryModel);
        }
    
        if (ObjectUtils.isNotEmpty(list)) {
            for (FailureWarnMsgExcelVo vo : list) {
                // 上报的记录没有
                if (ObjectUtils.isEmpty(vo.getDeviceType())) {
                    FailureAlarm failureAlarm = failureAlarmService.queryFromCacheBySignalId(vo.getSignalId());
                    Optional.ofNullable(failureAlarm).ifPresent(i -> {
                        String signalName = failureAlarm.getSignalName();
                        if (StringUtils.isNotEmpty(failureAlarm.getEventDesc())) {
                            signalName = signalName + CommonConstant.STR_COMMA + failureAlarm.getEventDesc();
                        }
                        vo.setFailureAlarmName(signalName);
                        vo.setGrade(String.valueOf(failureAlarm.getGrade()));
                        vo.setDeviceType(String.valueOf(failureAlarm.getDeviceType()));
                        
                    });
                }
                
                FailureAlarmDeviceTypeEnum deviceTypeEnum = BasicEnum.getEnum(Integer.valueOf(vo.getDeviceType()), FailureAlarmDeviceTypeEnum.class);
                if (ObjectUtils.isNotEmpty(deviceTypeEnum)) {
                    vo.setDeviceType(deviceTypeEnum.getDesc());
                }
                
                FailureAlarmGradeEnum gradeEnum = BasicEnum.getEnum(Integer.valueOf(vo.getGrade()), FailureAlarmGradeEnum.class);
                if (ObjectUtils.isNotEmpty(gradeEnum)) {
                    vo.setGrade(gradeEnum.getDesc());
                }
                
                if (ObjectUtils.isNotEmpty(vo.getAlarmTime())) {
                    date.setTime(vo.getAlarmTime());
                    vo.setAlarmTimeExport(sdf.format(date));
                }
                
                if (ObjectUtils.isNotEmpty(vo.getRecoverTimeExport())) {
                    date.setTime(vo.getRecoverTime());
                    vo.setRecoverTimeExport(sdf.format(date));
                }
    
                FailureWarnMsgStatusEnum statusEnum = BasicEnum.getEnum(vo.getAlarmFlag(), FailureWarnMsgStatusEnum.class);
                if (ObjectUtils.isNotEmpty(statusEnum)) {
                    vo.setAlarmFlagExport(statusEnum.getDesc());
                }
                
            }
        }
    
        String fileName = "故障告警记录报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, FailureWarnMsgExcelVo.class).sheet("sheet").doWrite(list);
            return;
        } catch (IOException e) {
            log.error("failure warn msg export error", e);
        }
    }
}
