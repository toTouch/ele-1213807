package com.xiliulou.electricity.service.warn;


import com.xiliulou.electricity.request.failureAlarm.WarnHandleRequest;
import org.apache.commons.lang3.tuple.Triple;


/**
 * @author maxiaodong
 * @date 2024/5/23 16:59
 * @desc
 */
public interface EleHardwareWarnMsgBusinessService {
    
    Triple<Boolean, String, Object> handle(WarnHandleRequest request);
}
