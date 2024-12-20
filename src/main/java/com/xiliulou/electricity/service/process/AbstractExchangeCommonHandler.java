package com.xiliulou.electricity.service.process;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.service.pipeline.ProcessContext;

/**
 * @ClassName: AbstractExchangeCommonHandler
 * @description:
 * @author: renhang
 * @create: 2024-11-12 15:03
 */
public abstract class AbstractExchangeCommonHandler {
    
    /**
     * 中断返回信息
     *
     * @param context 上下文
     * @param code 错误码
     * @param msg 错误信息
     */
    public void breakChain(ProcessContext<ExchangeAssertProcessDTO> context, String code, String msg) {
        context.setNeedBreak(true);
        context.setResult(R.fail(code, msg));
    }
    
    
    
}
