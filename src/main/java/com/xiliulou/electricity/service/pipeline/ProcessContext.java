package com.xiliulou.electricity.service.pipeline;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.dto.ExchangeAssertProcessDTO;
import com.xiliulou.electricity.enums.ExchangeAssertChainTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: ProcessContext
 * @description: 责任链上下文
 * @author: renhang
 * @create: 2024-11-14
 */
@Data
@AllArgsConstructor
@Builder
public class ProcessContext<T extends ExchangeAssertProcessDTO> implements Serializable {
    
    /**
     * 换电业务类型
     *
     * @see ExchangeAssertChainTypeEnum
     */
    private Integer code;
    
    /**
     * 存储责任链上下文数据的模型
     *
     * @see ExchangeAssertProcessDTO
     */
    private T processModel;
    
    /**
     * 责任链中断的标识
     */
    private Boolean needBreak;
    
    /**
     * 返回的对象
     */
    private R result;
}
