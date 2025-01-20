package com.xiliulou.electricity.service.pipeline;


import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.service.process.ExchangeAssertProcess;

import java.util.ArrayList;
import java.util.List;


public class ExchangeProcessChain {

    private List<ExchangeAssertProcess<ExchangeAssertProcessDTO>> processList = new ArrayList<>();

    public List<ExchangeAssertProcess<ExchangeAssertProcessDTO>> getProcessList() {
        return processList;
    }

    public void setProcessList(List<ExchangeAssertProcess<ExchangeAssertProcessDTO>> processList) {
        this.processList = processList;
    }

}