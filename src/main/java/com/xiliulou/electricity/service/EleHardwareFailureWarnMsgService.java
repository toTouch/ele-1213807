package com.xiliulou.electricity.service;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.warn.WarnNoteCallBack;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.FailureAlarmTaskQueryRequest;
import com.xiliulou.electricity.vo.failureAlarm.EleHardwareFailureWarnMsgVo;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface EleHardwareFailureWarnMsgService {
    
    List<EleHardwareFailureWarnMsgVo> list(FailureAlarmTaskQueryRequest request);
    
    R listByPage(EleHardwareFailureWarnMsgPageRequest request);
    
    R countTotal(EleHardwareFailureWarnMsgPageRequest request);
    
    Triple<Boolean, String, Object> calculateFrequency(EleHardwareFailureWarnMsgPageRequest request);
    
    R superExportPage(EleHardwareFailureWarnMsgPageRequest request);
    
    Triple<Boolean, String, Object> proportion(EleHardwareFailureWarnMsgPageRequest request);
    
    void proportionExport(EleHardwareFailureWarnMsgPageRequest request, HttpServletResponse response);
    
    Triple<Boolean, String, Object> warnNoteNotice(WarnNoteCallBack warnNoteCallBack);
}
