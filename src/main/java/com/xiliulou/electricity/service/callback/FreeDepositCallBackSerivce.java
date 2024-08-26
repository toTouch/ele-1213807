package com.xiliulou.electricity.service.callback;

import java.util.Map;

public interface FreeDepositCallBackSerivce {
    
    Object freeDepositNotified(Integer channel, Integer business, Map<String, Object> params);
}
