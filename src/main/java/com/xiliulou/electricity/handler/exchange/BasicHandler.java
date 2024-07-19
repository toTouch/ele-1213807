package com.xiliulou.electricity.handler.exchange;

import com.xiliulou.electricity.dto.ExchangeReasonCellDTO;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @ClassName: BasicHandler
 * @description:
 * @author: renhang
 * @create: 2024-07-19 14:05
 */
public abstract class BasicHandler implements Handler {
    
    public Integer reasonCode;
    
    @Resource
    private HandlerController handlerController;
    
    @PostConstruct
    public void init() {
        handlerController.putHandler(reasonCode, this);
    }
    
    @Override
    public Integer doHandler(ExchangeReasonCellDTO dto) {
        return handler(dto);
    }
    
    public abstract Integer handler(ExchangeReasonCellDTO dto);
    
    
}