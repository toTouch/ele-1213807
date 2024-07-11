package com.xiliulou.electricity.service.warn;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFailureWarnMsgPageRequest;
import com.xiliulou.electricity.request.failureAlarm.EleHardwareFaultMsgPageRequest;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;

/**
 * @author maxiaodong
 * @date 2024/5/23 16:59
 * @desc
 */
public interface EleHardwareFaultMsgBusinessService {
    
    R listByPage(EleHardwareFaultMsgPageRequest request);
    
    R countTotal(EleHardwareFaultMsgPageRequest request);
    
    R superExportPage(EleHardwareFaultMsgPageRequest request);
    
    Triple<Boolean, String, Object> proportion(EleHardwareFaultMsgPageRequest request);
    
    void proportionExport(EleHardwareFaultMsgPageRequest request, HttpServletResponse response);
    
    Triple<Boolean, String, Object> calculateFrequency(EleHardwareFailureWarnMsgPageRequest request);
}
