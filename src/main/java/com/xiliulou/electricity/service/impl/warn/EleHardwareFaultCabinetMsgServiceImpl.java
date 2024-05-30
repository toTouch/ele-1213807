package com.xiliulou.electricity.service.impl.warn;

import com.alibaba.excel.EasyExcel;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.entity.warn.EleHardwareFaultCabinetMsg;
import com.xiliulou.electricity.mapper.warn.EleHardwareFaultCabinetMsgMapper;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureCabinetMsgQueryModel;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureWarnCabinetMsgPageRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.warn.EleHardwareFaultCabinetMsgService;
import com.xiliulou.electricity.service.warn.EleHardwareFaultMsgService;
import com.xiliulou.electricity.service.warn.EleHardwareWarnMsgService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetCountVO;
import com.xiliulou.electricity.vo.failureAlarm.CabinetOverviewFailureExportVo;
import com.xiliulou.electricity.vo.failureAlarm.CabinetOverviewWarnExportVo;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnCabinetOverviewVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnTenantOverviewVo;
import com.xiliulou.electricity.vo.failureAlarm.TenantOverviewFailureExportVo;
import com.xiliulou.electricity.vo.failureAlarm.TenantOverviewWarnExportVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:18
 * @desc 每天定时刷新故障告警的数据：统计每个柜子对应的故障的次数，告警次数
 */

@Service
@Slf4j
public class EleHardwareFaultCabinetMsgServiceImpl implements EleHardwareFaultCabinetMsgService {
    @Resource
    private EleHardwareFaultCabinetMsgMapper faultCabinetMsgMapper;
    
    @Resource
    private TenantService tenantService;
    
    @Resource
    private ElectricityCabinetService cabinetService;
    
    @Resource
    private EleHardwareFaultMsgService eleHardwareFaultMsgService;
    
    @Resource
    private EleHardwareWarnMsgService eleHardwareWarnMsgService;
    
    
    private EleHardwareFaultCabinetMsg getCabinetFailureWarnMsg(List<EleHardwareFailureWarnMsgVo> failureWarnMsgVoList, FailureAlarmTaskQueryRequest request) {
        EleHardwareFaultCabinetMsg faultCabinetMsg = new EleHardwareFaultCabinetMsg();
        Integer failureNum = 0;
        Integer warnNum = 0;
        for (EleHardwareFailureWarnMsgVo item : failureWarnMsgVoList) {
            if (ObjectUtils.isEmpty(faultCabinetMsg.getTenantId())) {
                faultCabinetMsg.setCabinetId(item.getCabinetId());
                faultCabinetMsg.setTenantId(item.getTenantId());
                faultCabinetMsg.setCreateTime(request.getTime());
            }
            
            if (Objects.equals(item.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
                failureNum += item.getFailureWarnNum();
            }
            
            if (Objects.equals(item.getType(), EleHardwareFailureWarnMsg.WARN)) {
                warnNum += item.getFailureWarnNum();
            }
        }
        
        faultCabinetMsg.setFailureCount(failureNum);
        faultCabinetMsg.setWarnCount(warnNum);
        
        return faultCabinetMsg;
    }
    
    /**
     * 每天凌晨一点默认查询昨天的告警数据
     *
     * @return
     */
    private FailureAlarmTaskQueryRequest getQueryRequest() {
        FailureAlarmTaskQueryRequest request = new FailureAlarmTaskQueryRequest();
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        long startTime = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        
        long endTime = calendar.getTimeInMillis();
        
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        long time = calendar.getTimeInMillis();
        
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setTime(time);
        
        return request;
    }
    
    @Override
    public void tenantOverviewExport(FailureWarnCabinetMsgPageRequest request, HttpServletResponse response) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            throw new CustomBusinessException((String) triple.getRight());
        }
        
        List<FailureWarnTenantOverviewVo> list = new ArrayList<>();
        
        FailureCabinetMsgQueryModel queryModel = FailureCabinetMsgQueryModel.builder().alarmStartTime(request.getAlarmStartTime()).alarmEndTime(request.getAlarmEndTime())
                .offset(0L).size(2000L).build();
        
        List<TenantOverviewFailureExportVo> failureExportVos = new ArrayList<>();
        List<TenantOverviewWarnExportVo> warnExportVoList = new ArrayList<>();
        
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
            // 查询
            list = faultCabinetMsgMapper.selectListForFailure(queryModel);
            // 设置信息
            setTenantOverviewExport(failureExportVos, warnExportVoList, request, list);
            // 导出
            doTenantOverviewFailureExport(failureExportVos, response);
        }
        
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN)) {
            // 查询
            list = faultCabinetMsgMapper.selectListForWarn(queryModel);
            // 设置信息
            setTenantOverviewExport(failureExportVos, warnExportVoList, request, list);
            // 导出
            doTenantOverviewWarnExport(warnExportVoList, response);
        }
    }
    
    /**
     * 设备总览
     *
     * @param request
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> cabinetOverviewPage(FailureWarnCabinetMsgPageRequest request) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            return triple;
        }
        
        List<FailureWarnCabinetOverviewVo> list = new ArrayList<>();
        
        FailureCabinetMsgQueryModel queryModel = FailureCabinetMsgQueryModel.builder().alarmStartTime(request.getAlarmStartTime()).alarmEndTime(request.getAlarmEndTime())
                .size(request.getSize()).type(request.getType()).offset(request.getOffset()).build();
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
            list = faultCabinetMsgMapper.selectListCabinetFailure(queryModel);
        }
        
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN)) {
            list = faultCabinetMsgMapper.selectListCabinetWarn(queryModel);
        }
        
        cabinetOverviewInfo(list, request);
        
        return Triple.of(true, null, list);
    }
    
    @Override
    public Triple<Boolean, String, Object> cabinetOverviewPageCount(FailureWarnCabinetMsgPageRequest request) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            return triple;
        }
        
        FailureCabinetMsgQueryModel queryModel = FailureCabinetMsgQueryModel.builder().alarmStartTime(request.getAlarmStartTime()).alarmEndTime(request.getAlarmEndTime())
                .size(request.getSize()).type(request.getType()).offset(request.getOffset()).build();
        Integer count = faultCabinetMsgMapper.countCabinetOverview(queryModel);
        
        return Triple.of(true, null, count);
    }
    
    @Override
    public void cabinetOverviewExport(FailureWarnCabinetMsgPageRequest request, HttpServletResponse response) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            throw new CustomBusinessException((String) triple.getRight());
        }
        
        List<FailureWarnCabinetOverviewVo> list = new ArrayList<>();
        
        FailureCabinetMsgQueryModel queryModel = FailureCabinetMsgQueryModel.builder().alarmStartTime(request.getAlarmStartTime()).alarmEndTime(request.getAlarmEndTime())
                .offset(0L).size(2000L).build();
        
        List<CabinetOverviewFailureExportVo> failureExportVos = new ArrayList<>();
        List<CabinetOverviewWarnExportVo> warnExportVoList = new ArrayList<>();
        
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
            // 查询
            list = faultCabinetMsgMapper.selectListCabinetFailure(queryModel);
            // 设置信息
            setCabinetOverviewExport(failureExportVos, warnExportVoList, request, list);
            // 导出
            doCabinetOverviewFailureExport(failureExportVos, response);
        }
        
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN)) {
            // 查询
            list = faultCabinetMsgMapper.selectListCabinetWarn(queryModel);
            // 设置信息
            setCabinetOverviewExport(failureExportVos, warnExportVoList, request, list);
            // 导出
            doCabinetOverviewWarnExport(warnExportVoList, response);
        }
    }
    
    @Override
    public void createFaultWarnData() {
        FailureAlarmTaskQueryRequest request = this.getQueryRequest();
        List<EleHardwareFailureWarnMsgVo> faultWarnMsgList = new ArrayList<>();
        
        List<EleHardwareFailureWarnMsgVo> faultMsgList = eleHardwareFaultMsgService.list(request);
        if (ObjectUtils.isNotEmpty(faultMsgList)) {
            faultWarnMsgList.addAll(faultMsgList);
        }
        
        List<EleHardwareFailureWarnMsgVo> warnMsgList = eleHardwareWarnMsgService.list(request);
        if (ObjectUtils.isNotEmpty(warnMsgList)) {
            faultWarnMsgList.addAll(warnMsgList);
        }
    
        
        Map<Integer, EleHardwareFaultCabinetMsg> cabinetMsgMap = faultWarnMsgList.stream().collect(Collectors.groupingBy(EleHardwareFailureWarnMsgVo::getCabinetId,
                Collectors.collectingAndThen(Collectors.toList(), e -> this.getCabinetFailureWarnMsg(e, request))));
    
        if (ObjectUtils.isNotEmpty(cabinetMsgMap)) {
            // 删除昨天的历史数据
            faultCabinetMsgMapper.batchDelete(request.getStartTime(), request.getEndTime());
            
            List<EleHardwareFaultCabinetMsg> failureCabinetMsgList = cabinetMsgMap.values().parallelStream().collect(Collectors.toList());
            // 批量插入新的数据
            faultCabinetMsgMapper.batchInsert(failureCabinetMsgList);
        }
    }
    
    private void doCabinetOverviewWarnExport(List<CabinetOverviewWarnExportVo> warnExportVoList, HttpServletResponse response) {
        String fileName = "设备故障总览报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, CabinetOverviewWarnExportVo.class).sheet("sheet").doWrite(warnExportVoList);
            return;
        } catch (IOException e) {
            log.error("tenant Overview failure Export error", e);
        }
    }
    
    private void doCabinetOverviewFailureExport(List<CabinetOverviewFailureExportVo> failureExportVos, HttpServletResponse response) {
        String fileName = "设备故障总览报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, CabinetOverviewFailureExportVo.class).sheet("sheet").doWrite(failureExportVos);
            return;
        } catch (IOException e) {
            log.error("tenant Overview failure Export error", e);
        }
    }
    
    private void setCabinetOverviewExport(List<CabinetOverviewFailureExportVo> failureExportVos, List<CabinetOverviewWarnExportVo> warnExportVoList,
            FailureWarnCabinetMsgPageRequest request, List<FailureWarnCabinetOverviewVo> list) {
        if (ObjectUtils.isNotEmpty(list)) {
            long usageDays = DateUtils.diffDayV2(request.getAlarmStartTime(), request.getAlarmEndTime());
            
            for (FailureWarnCabinetOverviewVo vo : list) {
                
                Optional.ofNullable(cabinetService.queryByIdFromCache(vo.getCabinetId())).ifPresent(item -> {
                    vo.setSn(item.getSn());
                    
                    // 租户名称
                    Optional.ofNullable(tenantService.queryByIdFromCache(item.getTenantId())).ifPresent(t -> {
                        vo.setTenantName(t.getName());
                    });
                });
                
                vo.setUseDays(Integer.valueOf(String.valueOf(usageDays)));
                
                BigDecimal usageDaysCount = new BigDecimal(String.valueOf(usageDays));
                
                // 计算失败率：告警次数/累计使用天数* 100
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE) && ObjectUtils.isNotEmpty(vo.getFailureCount())) {
                    // 故障率 故障次数 / 柜机出货量* 100
                    BigDecimal failureCountBig = new BigDecimal(String.valueOf(vo.getFailureCount()));
                    BigDecimal failureRate = failureCountBig.divide(usageDaysCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setFailureRate(failureRate);
                    
                    CabinetOverviewFailureExportVo failureExportVo = new CabinetOverviewFailureExportVo();
                    BeanUtils.copyProperties(vo, failureExportVo);
                    failureExportVo.setFailureRate(failureRate.stripTrailingZeros().toPlainString() + StringConstant.PERCENT);
                    failureExportVos.add(failureExportVo);
                }
                
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN) && ObjectUtils.isNotEmpty(vo.getWarnCount())) {
                    // 告警率 故障次数 / 柜机出货量* 100
                    BigDecimal warnCountBig = new BigDecimal(String.valueOf(vo.getWarnCount()));
                    BigDecimal failureRate = warnCountBig.divide(usageDaysCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setFailureRate(failureRate);
                    
                    CabinetOverviewWarnExportVo warnExportVo = new CabinetOverviewWarnExportVo();
                    BeanUtils.copyProperties(vo, warnExportVo);
                    warnExportVo.setFailureRate(failureRate.stripTrailingZeros().toPlainString() + StringConstant.PERCENT);
                    warnExportVoList.add(warnExportVo);
                }
            }
        }
    }
    
    private void cabinetOverviewInfo(List<FailureWarnCabinetOverviewVo> list, FailureWarnCabinetMsgPageRequest request) {
        if (ObjectUtils.isNotEmpty(list)) {
            long usageDays = DateUtils.diffDayV2(request.getAlarmStartTime(), request.getAlarmEndTime());
            
            for (FailureWarnCabinetOverviewVo vo : list) {
                Optional.ofNullable(cabinetService.queryByIdFromCache(vo.getCabinetId())).ifPresent(item -> {
                    vo.setSn(item.getSn());
                    
                    // 租户名称
                    Optional.ofNullable(tenantService.queryByIdFromCache(item.getTenantId())).ifPresent(t -> {
                        vo.setTenantName(t.getName());
                    });
                });
                
                vo.setUseDays(Integer.valueOf(String.valueOf(usageDays)));
                
                BigDecimal usageDaysCount = new BigDecimal(String.valueOf(usageDays));
                
                // 计算失败率：告警次数/累计使用天数* 100
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE) && ObjectUtils.isNotEmpty(vo.getFailureCount())) {
                    // 故障率 故障次数 / 柜机出货量* 100
                    BigDecimal failureCountBig = new BigDecimal(String.valueOf(vo.getFailureCount()));
                    BigDecimal failureRate = failureCountBig.divide(usageDaysCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setFailureRate(failureRate);
                }
                
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN) && ObjectUtils.isNotEmpty(vo.getWarnCount())) {
                    // 告警率 故障次数 / 柜机出货量* 100
                    BigDecimal warnCountBig = new BigDecimal(String.valueOf(vo.getWarnCount()));
                    BigDecimal failureRate = warnCountBig.divide(usageDaysCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setFailureRate(failureRate);
                }
            }
        }
    }
    
    private void doTenantOverviewWarnExport(List<TenantOverviewWarnExportVo> warnExportVoList, HttpServletResponse response) {
        String fileName = "运营商告警总览报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, TenantOverviewWarnExportVo.class).sheet("sheet").doWrite(warnExportVoList);
            return;
        } catch (IOException e) {
            log.error("tenant Overview Warn Export error", e);
        }
    }
    
    private void doTenantOverviewFailureExport(List<TenantOverviewFailureExportVo> failureExportVos, HttpServletResponse response) {
        String fileName = "运营商故障总览报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, TenantOverviewFailureExportVo.class).sheet("sheet").doWrite(failureExportVos);
            return;
        } catch (IOException e) {
            log.error("tenant Overview failure Export error", e);
        }
    }
    
    private void setTenantOverviewExport(List<TenantOverviewFailureExportVo> failureExportVos, List<TenantOverviewWarnExportVo> warnExportVoList,
            FailureWarnCabinetMsgPageRequest request, List<FailureWarnTenantOverviewVo> list) {
        if (ObjectUtils.isNotEmpty(list)) {
            Set<Integer> tenantIdSet = list.stream().map(FailureWarnTenantOverviewVo::getTenantId).collect(Collectors.toSet());
            List<Integer> tenantIdList = tenantIdSet.stream().collect(Collectors.toList());
            // 查询柜机的出货量
            ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().tenantIdList(tenantIdList).build();
            Map<Integer, Integer> cabinetCountMap = new HashMap<>();
            List<ElectricityCabinetCountVO> cabinetCountVOList = cabinetService.queryCabinetCount(cabinetQuery);
            if (ObjectUtils.isNotEmpty(cabinetCountVOList)) {
                cabinetCountMap = cabinetCountVOList.stream().collect(Collectors.toMap(ElectricityCabinetCountVO::getTenantId, ElectricityCabinetCountVO::getCabinetCount));
            }
            
            for (FailureWarnTenantOverviewVo vo : list) {
                // 租户名称
                Optional.ofNullable(tenantService.queryByIdFromCache(vo.getTenantId())).ifPresent(t -> {
                    vo.setTenantName(t.getName());
                });
                
                // 柜机出货量
                Optional.ofNullable(cabinetCountMap.get(vo.getTenantId())).ifPresent(integer -> {
                    vo.setCabinetShipment(integer);
                });
                
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE) && ObjectUtils.isNotEmpty(vo.getFailureCount()) && ObjectUtils.isNotEmpty(
                        vo.getCabinetShipment()) && vo.getCabinetShipment() > 0) {
                    // 故障率 故障次数 / 柜机出货量* 100
                    BigDecimal failureCountBig = new BigDecimal(String.valueOf(vo.getFailureCount()));
                    BigDecimal cabinetCount = new BigDecimal(String.valueOf(vo.getCabinetShipment()));
                    BigDecimal failureRate = failureCountBig.divide(cabinetCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setFailureRate(failureRate);
                    
                    TenantOverviewFailureExportVo failureExportVo = new TenantOverviewFailureExportVo();
                    BeanUtils.copyProperties(vo, failureExportVo);
                    failureExportVo.setFailureRate(failureRate.stripTrailingZeros().toPlainString() + StringConstant.PERCENT);
                    failureExportVos.add(failureExportVo);
                }
                
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN) && ObjectUtils.isNotEmpty(vo.getWarnCount()) && ObjectUtils.isNotEmpty(
                        vo.getCabinetShipment()) && vo.getCabinetShipment() > 0) {
                    // 告警率 故障次数 / 柜机出货量* 100
                    BigDecimal warnCountBig = new BigDecimal(String.valueOf(vo.getWarnCount()));
                    BigDecimal cabinetCount = new BigDecimal(String.valueOf(vo.getCabinetShipment()));
                    BigDecimal warnRate = warnCountBig.divide(cabinetCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setWarnRate(warnRate);
                    
                    TenantOverviewWarnExportVo warnExportVo = new TenantOverviewWarnExportVo();
                    BeanUtils.copyProperties(vo, warnExportVo);
                    warnExportVo.setWarnRate(warnRate.stripTrailingZeros().toPlainString() + StringConstant.PERCENT);
                    warnExportVoList.add(warnExportVo);
                }
            }
        }
    }
    
    @Override
    public Triple<Boolean, String, Object> tenantOverviewPageCount(FailureWarnCabinetMsgPageRequest request) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            return triple;
        }
        
        FailureCabinetMsgQueryModel queryModel = FailureCabinetMsgQueryModel.builder().alarmStartTime(request.getAlarmStartTime()).alarmEndTime(request.getAlarmEndTime())
                .type(request.getType()).build();
        Integer count = faultCabinetMsgMapper.countTenantOverview(queryModel);
        
        return Triple.of(true, null, count);
    }
    
    /**
     * 运营商故障总览
     *
     * @param request
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> tenantOverviewPage(FailureWarnCabinetMsgPageRequest request) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            return triple;
        }
        
        List<FailureWarnTenantOverviewVo> list = new ArrayList<>();
        
        FailureCabinetMsgQueryModel queryModel = FailureCabinetMsgQueryModel.builder().alarmStartTime(request.getAlarmStartTime()).alarmEndTime(request.getAlarmEndTime())
                .size(request.getSize()).type(request.getType()).offset(request.getOffset()).build();
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE)) {
            list = faultCabinetMsgMapper.selectListForFailure(queryModel);
        }
        
        if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN)) {
            list = faultCabinetMsgMapper.selectListForWarn(queryModel);
        }
        
        tenantOverviewInfo(list, request);
        
        return Triple.of(true, null, list);
    }
    
    private Triple<Boolean, String, Object> checkParams(FailureWarnCabinetMsgPageRequest request) {
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
    
    private void tenantOverviewInfo(List<FailureWarnTenantOverviewVo> list, FailureWarnCabinetMsgPageRequest request) {
        if (ObjectUtils.isNotEmpty(list)) {
            Set<Integer> tenantIdSet = list.stream().map(FailureWarnTenantOverviewVo::getTenantId).collect(Collectors.toSet());
            List<Integer> tenantIdList = tenantIdSet.stream().collect(Collectors.toList());
            // 查询柜机的出货量
            ElectricityCabinetQuery cabinetQuery = ElectricityCabinetQuery.builder().tenantIdList(tenantIdList).build();
            Map<Integer, Integer> cabinetCountMap = new HashMap<>();
            List<ElectricityCabinetCountVO> cabinetCountVOList = cabinetService.queryCabinetCount(cabinetQuery);
            if (ObjectUtils.isNotEmpty(cabinetCountVOList)) {
                cabinetCountMap = cabinetCountVOList.stream().collect(Collectors.toMap(ElectricityCabinetCountVO::getTenantId, ElectricityCabinetCountVO::getCabinetCount));
            }
            
            for (FailureWarnTenantOverviewVo vo : list) {
                // 租户名称
                Optional.ofNullable(tenantService.queryByIdFromCache(vo.getTenantId())).ifPresent(t -> {
                    vo.setTenantName(t.getName());
                });
                
                // 柜机出货量
                Optional.ofNullable(cabinetCountMap.get(vo.getTenantId())).ifPresent(integer -> {
                    vo.setCabinetShipment(integer);
                });
                
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.FAILURE) && ObjectUtils.isNotEmpty(vo.getFailureCount()) && ObjectUtils.isNotEmpty(
                        vo.getCabinetShipment()) && vo.getCabinetShipment() > 0) {
                    // 故障率 故障次数 / 柜机出货量* 100
                    BigDecimal failureCountBig = new BigDecimal(String.valueOf(vo.getFailureCount()));
                    BigDecimal cabinetCount = new BigDecimal(String.valueOf(vo.getCabinetShipment()));
                    BigDecimal failureRate = failureCountBig.divide(cabinetCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setFailureRate(failureRate);
                }
                
                if (Objects.equals(request.getType(), EleHardwareFailureWarnMsg.WARN) && ObjectUtils.isNotEmpty(vo.getWarnCount()) && ObjectUtils.isNotEmpty(
                        vo.getCabinetShipment()) && vo.getCabinetShipment() > 0) {
                    // 告警率 故障次数 / 柜机出货量* 100
                    BigDecimal warnCountBig = new BigDecimal(String.valueOf(vo.getWarnCount()));
                    BigDecimal cabinetCount = new BigDecimal(String.valueOf(vo.getCabinetShipment()));
                    BigDecimal warnRate = warnCountBig.divide(cabinetCount, 3, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    vo.setWarnRate(warnRate);
                }
            }
        }
    }
}
