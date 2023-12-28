package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;
import com.xiliulou.electricity.mapper.EleHardwareFailureWarnMsgMapper;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @since 2023-12-26 09:07:48
 */
@Service
@Slf4j
public class EleHardwareFailureWarnMsgServiceImpl implements EleHardwareFailureWarnMsgService {
    @Resource
    private EleHardwareFailureWarnMsgMapper failureWarnMsgMapper;
    
    @Slave
    @Override
    public List<EleHardwareFailureWarnMsg> list(List<String> alarmIdList) {
        return failureWarnMsgMapper.selectList(alarmIdList);
    }
    
}
