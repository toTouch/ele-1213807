package com.xiliulou.electricity.service.pipeline;


import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.service.process.ExchangeAssertProcess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExchangeProcessChain {
    
    private Map<Integer, List<ExchangeAssertProcess<ExchangeAssertProcessDTO>>> processMap = new HashMap<>();
    
    public List<ExchangeAssertProcess<ExchangeAssertProcessDTO>> getProcessList(Integer code) {
        return processMap.get(code);
    }
    
    public void setProcessList(Integer code, List<ExchangeAssertProcess<ExchangeAssertProcessDTO>> processList) {
        this.processMap.put(code, processList);
    }
}