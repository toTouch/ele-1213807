package com.xiliulou.electricity.service;


import com.xiliulou.electricity.queryModel.failureAlarm.EleHardwareFailureWarnMsgQueryModel;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;

import java.util.List;

public interface EleHardwareFailureWarnMsgService {
    
    List<EleHardwareFailureWarnMsgVo> list(EleHardwareFailureWarnMsgQueryModel queryModel);
}
