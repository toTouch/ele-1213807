package com.xiliulou.electricity.service.callback;

import java.util.Map;

public interface FreeDepositCallBackSerivce {
    
    String authPayNotified(Integer channel, Map<String, Object> params);
}
