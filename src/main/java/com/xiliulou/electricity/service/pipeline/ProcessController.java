package com.xiliulou.electricity.service.pipeline;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.service.process.ExchangeAssertProcess;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: ProcessController
 * @description: 责任链控制器
 * @author: renhang
 * @create: 2024-11-19
 */
@Data
@Slf4j
public class ProcessController {


    private Map<Integer, ExchangeProcessChain> processMap = null;


    @SuppressWarnings("all")
    public ProcessContext process(ProcessContext<ExchangeAssertProcessDTO> context) {
        // 自检
        preCheck(context);
        // 扩展预留，根据枚举code不同，判定的责任链不同
        List<ExchangeAssertProcess<ExchangeAssertProcessDTO>> processList = processMap.get(context.getCode()).getProcessList();
        for (ExchangeAssertProcess process : processList) {
            // handler
            process.process(context);
            if (context.getNeedBreak()) {
                break;
            }
        }
        return context;
    }
    
    private void preCheck(ProcessContext<ExchangeAssertProcessDTO> context) {
        if (Objects.isNull(context)) {
            throw new CustomBusinessException("责任链上下文为空");
        }
        
        if (Objects.isNull(context.getProcessModel())) {
            throw new CustomBusinessException("责任链数据模型为空");
        }
        
        List<ExchangeAssertProcess<ExchangeAssertProcessDTO>> processList = processMap.get(context.getCode()).getProcessList();
        if (CollUtil.isEmpty(processList)) {
            log.warn("ProcessController Warn! processList is null , code is {}", context.getCode());
            throw new CustomBusinessException("责任链为空");
        }
    }
    
    
}
