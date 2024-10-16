package com.xiliulou.electricity.service.impl.warn;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.TimeConstant;
import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.warn.EleHardwareFaultMsg;
import com.xiliulou.electricity.enums.failureAlarm.FailureAlarmTypeEnum;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureAlarmQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FailureWarnMsgPageQueryModel;
import com.xiliulou.electricity.queryModel.failureAlarm.FaultMsgPageQueryModel;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFaultMsgPageRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FailureAlarmService;
import com.xiliulou.electricity.service.warn.EleHardwareFaultMsgBusinessService;
import com.xiliulou.electricity.service.warn.EleHardwareFaultMsgService;
import com.xiliulou.electricity.service.warn.EleHardwareWarnMsgService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnProportionVo;
import com.xiliulou.electricity.vo.warn.FaultMsgExcelVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/5/23 17:00
 * @desc
 */
@Service
@Slf4j
public class EleHardwareFaultMsgBusinessServiceImpl implements EleHardwareFaultMsgBusinessService {
    @Resource
    private EleHardwareFaultMsgService eleHardwareFaultMsgService;
    
    @Resource
    private EleHardwareWarnMsgService eleHardwareWarnMsgService;
    
    @Resource
    private FailureAlarmService failureAlarmService;
    
    @Resource
    private ElectricityCabinetService electricityCabinetService;
    
    
    @Override
    public R listByPage(EleHardwareFaultMsgPageRequest request) {
        FaultMsgPageQueryModel queryModel = new FaultMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = eleHardwareFaultMsgService.checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
    
        if (triple.getLeft() && !Objects.isNull(triple.getRight())) {
            return R.ok(Collections.emptyList());
        }
    
        List<EleHardwareFaultMsg> eleHardwareFaultMsgList = eleHardwareFaultMsgService.listByPage(request, queryModel);
    
        return eleHardwareFaultMsgService.transferListPage(eleHardwareFaultMsgList, request);
    }
    
    @Override
    public R countTotal(EleHardwareFaultMsgPageRequest request) {
        FaultMsgPageQueryModel queryModel = new FaultMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = eleHardwareFaultMsgService.checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
    
        if (triple.getLeft() && !Objects.isNull(triple.getRight())) {
            return R.ok(NumberConstant.ZERO);
        }
    
        return eleHardwareFaultMsgService.countTotal(queryModel);
    }
    
    @Override
    public R superExportPage(EleHardwareFaultMsgPageRequest request) {
        FaultMsgPageQueryModel queryModel = new FaultMsgPageQueryModel();
        // 检测数据
        Triple<Boolean, String, Object> triple = eleHardwareFaultMsgService.checkAndInitQuery(request, queryModel, TimeConstant.ONE_MONTH);
        if (!triple.getLeft()) {
            throw new CustomBusinessException((String) triple.getRight());
        }
    
        List<FaultMsgExcelVo> failureWarnMsgExcelVos = eleHardwareFaultMsgService.listExportData(queryModel, triple);
    
        return eleHardwareFaultMsgService.superExportPage(request, failureWarnMsgExcelVos);
    }
    
    @Override
    public Triple<Boolean, String, Object> proportion(EleHardwareFaultMsgPageRequest request) {
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            return triple;
        }
    
        List<FailureWarnProportionVo> list = new ArrayList<>();
    
        FailureAlarmQueryModel failureAlarmQueryModel = FailureAlarmQueryModel.builder().status(FailureAlarm.enable).type(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode()).build();
        List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(failureAlarmQueryModel);
        if (ObjectUtils.isNotEmpty(failureAlarmList)) {
            List<String> signalIdList = failureAlarmList.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList());
            request.setSignalIdList(signalIdList);
            list = eleHardwareFaultMsgService.listProportion(request);
        }
    
        Map<String, Integer> failureMap = new HashMap<>();
        if (ObjectUtils.isNotEmpty(list)) {
            failureMap = list.stream().collect(Collectors.toMap(FailureWarnProportionVo::getSignalId, FailureWarnProportionVo::getCount));
        }
    
        List<FailureWarnProportionVo> result = eleHardwareFaultMsgService.faultProportion(failureMap);
        
        return Triple.of(true, null, result);
    }
    
    private Triple<Boolean, String, Object> checkParams(EleHardwareFaultMsgPageRequest request) {
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
    public void proportionExport(EleHardwareFaultMsgPageRequest request, HttpServletResponse response) {
        // 检测参数
        Triple<Boolean, String, Object> triple = checkParams(request);
        if (!triple.getLeft()) {
            throw new CustomBusinessException((String) triple.getRight());
        }
        List<FailureWarnProportionVo> list = new ArrayList<>();
    
        FailureAlarmQueryModel failureAlarmQueryModel = FailureAlarmQueryModel.builder().status(FailureAlarm.enable).type(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode()).build();
        List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(failureAlarmQueryModel);
        if (ObjectUtils.isNotEmpty(failureAlarmList)) {
            List<String> signalIdList = failureAlarmList.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList());
            request.setSignalIdList(signalIdList);
            list = eleHardwareFaultMsgService.listProportion(request);
        }
    
        eleHardwareFaultMsgService.proportionExport(list, response);
    }
    
    @Override
    public Triple<Boolean, String, Object> calculateFrequency(EleHardwareFailureWarnMsgPageRequest request) {
        // 查询设备数量和告警数量
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
    
        // 查询设备数量
        ElectricityCabinetQuery electricityCabinetQuery = new ElectricityCabinetQuery();
        R r = electricityCabinetService.queryCount(electricityCabinetQuery);
        Optional.ofNullable(r.getData()).ifPresent(i -> {
            Integer count = (Integer) r.getData();
            vo.setCabinetShipment(count);
        });
    
        FailureAlarmQueryModel failureAlarmQueryModel = FailureAlarmQueryModel.builder().status(FailureAlarm.enable).build();
        List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(failureAlarmQueryModel);
        if (ObjectUtils.isEmpty(failureAlarmList)) {
            return Triple.of(true, null, vo);
        }
    
        Map<Integer, List<String>> typeMap = failureAlarmList.parallelStream().collect(Collectors.groupingBy(FailureAlarm::getType,
                Collectors.collectingAndThen(Collectors.toList(), e -> e.stream().map(FailureAlarm::getSignalId).collect(Collectors.toList()))));
        
        FailureWarnMsgPageQueryModel queryModel = new FailureWarnMsgPageQueryModel();
        queryModel.setAlarmStartTime(request.getAlarmStartTime());
        queryModel.setAlarmEndTime(request.getAlarmEndTime());
        queryModel.setSignalIdList(typeMap.get(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_FAILURE.getCode()));
    
        // 设置故障信息
        eleHardwareFaultMsgService.setFailureInfo(vo, queryModel);
    
        queryModel.setSignalIdList(typeMap.get(FailureAlarmTypeEnum.FAILURE_ALARM_TYPE_WARING.getCode()));
        // 设置告警数量和设备数量
        eleHardwareWarnMsgService.setWarnInfo(vo, queryModel);
        
        return Triple.of(true, null, vo);
    }
}
