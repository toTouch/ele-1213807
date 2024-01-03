package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.entity.FailureAlarm;
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
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgPageVo;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import com.xiliulou.electricity.vo.failureAlarm.FailureWarnFrequencyVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel);
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
    
    private Triple<Boolean, String, Object> checkAndInitQuery(EleHardwareFailureWarnMsgPageRequest request, FailureWarnMsgPageQueryModel queryModel) {
        // 计算查询时间不能大于三十天
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(request.getAlarmStartTime());
    
        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(request.getAlarmEndTime());
    
        long days = DateUtils.diffDay(request.getAlarmStartTime(), request.getAlarmEndTime());
    
        if (days > 30) {
            return Triple.of(false, "300825", "查询天数不能大于30天");
        }
    
        // 设置查询参数
        BeanUtils.copyProperties(request, queryModel);
        if (ObjectUtils.isNotEmpty(queryModel.getDeviceType()) || ObjectUtils.isNotEmpty(queryModel.getGrade()) || ObjectUtils.isNotEmpty(request.getTenantVisible())
                || ObjectUtils.isNotEmpty(request.getStatus())) {
            // 查询故障告警设置是否存在
            List<FailureAlarm> failureAlarmList = failureAlarmService.listByParams(queryModel.getDeviceType(), queryModel.getGrade(), request.getTenantVisible(),
                    request.getStatus());
            if (ObjectUtils.isEmpty(failureAlarmList)) {
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
        Triple<Boolean, String, Object> triple = checkAndInitQuery(request, queryModel);
        if (!triple.getLeft()) {
            return R.fail(triple.getMiddle(), (String) triple.getRight());
        }
    
        if (triple.getLeft() && !Objects.isNull(triple.getRight())) {
            return R.ok(Collections.emptyList());
        }
    
        Integer count = failureWarnMsgMapper.countTotal(queryModel);
        return R.ok(count);
    }
    
    /**
     * 故障告警分析：设备出货量，告警频率环比
     * @param request
     * @return
     */
    @Override
    public FailureWarnFrequencyVo calculateFrequency(EleHardwareFailureWarnMsgPageRequest request) {
        FailureWarnFrequencyVo vo = new FailureWarnFrequencyVo();
        // 统计平台中所有的柜机的数量
        ElectricityCabinetQuery electricityCabinetQuery = new ElectricityCabinetQuery();
        R r = cabinetService.queryCount(electricityCabinetQuery);
        Optional.ofNullable(r.getData()).ifPresent(i -> {
            Integer count = (Integer) r.getData();
            vo.setCabinetShipment(count);
        });
  
        return vo;
    }
}
