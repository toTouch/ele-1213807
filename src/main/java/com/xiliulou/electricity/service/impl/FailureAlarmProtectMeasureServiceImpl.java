package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.FailureAlarmProtectMeasure;
import com.xiliulou.electricity.mapper.FailureAlarmMapper;
import com.xiliulou.electricity.mapper.FailureAlarmProtectMeasureMapper;
import com.xiliulou.electricity.service.FailureAlarmProtectMeasureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 故障预警设置
 *
 * @author maxiaodong
 * @since 2023-12-15 14:06:24
 */
@Service
@Slf4j
public class FailureAlarmProtectMeasureServiceImpl implements FailureAlarmProtectMeasureService {
    @Resource
    private FailureAlarmProtectMeasureMapper measureMapper;
    
    @Slave
    @Override
    public List<FailureAlarmProtectMeasure> listByFailureAlarmIdList(List<Long> failureAlarmIdList) {
        return measureMapper.selectListByFailureAlarmIdList(failureAlarmIdList);
    }
}
