package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.EleHardwareFailureWarnMsg;

import java.util.List;

public interface EleHardwareFailureWarnMsgService {
    List<EleHardwareFailureWarnMsg> list(List<String> alarmIdList);
}
