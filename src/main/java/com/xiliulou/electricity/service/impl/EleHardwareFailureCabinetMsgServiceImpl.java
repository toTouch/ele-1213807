package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.mapper.EleHardwareFailureCabinetMsgMapper;
import com.xiliulou.electricity.service.EleHardwareFailureCabinetMsgService;
import com.xiliulou.electricity.service.EleHardwareFailureWarnMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author maxiaodong
 * @date 2023/12/28 16:18
 * @desc
 */

@Service
@Slf4j
public class EleHardwareFailureCabinetMsgServiceImpl implements EleHardwareFailureCabinetMsgService {
    @Resource
    private EleHardwareFailureCabinetMsgMapper failureCabinetMsgMapper;
    
    @Resource
    private EleHardwareFailureWarnMsgService hardwareFailureWarnMsgService;
    
    @Override
    public void createFailureWarnData() {
    
    }
}
