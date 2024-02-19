package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FailureAlarmProtectMeasure;

import java.util.List;

/**
 * @author: maxiaodong
 * @Date: 2023/12/15 11:27
 * @Description: 故障预警设置-保护措施
 */
public interface FailureAlarmProtectMeasureService {
    
    List<FailureAlarmProtectMeasure> listByFailureAlarmIdList(List<Long> failureAlarmIdList);
}
