package com.xiliulou.electricity.service.callback;

import java.util.Map;

public interface FreeDepositCallBackSerivce {
    
    Object authPayNotified(Integer channel, Map<String, Object> params);
    
    Object unFreeNotified(Integer channel, Map<String, Object> params);
    
    Object freeNotified(Integer channel, Map<String, Object> params);
}
