package com.xiliulou.electricity.service.process;

import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.service.pipeline.ProcessContext;


/**
 * description: 换电责任链校验处理器
 *
 * @author renhang
 * @date 2024/11/14
 */
public interface ExchangeAssertProcess<T extends ExchangeAssertProcessDTO> {
    
    /**
     * 处理
     *
     * @param context 参数
     */
    void process(ProcessContext<T> context);
}
