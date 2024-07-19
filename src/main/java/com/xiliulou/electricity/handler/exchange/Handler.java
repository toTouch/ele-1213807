package com.xiliulou.electricity.handler.exchange;

import com.xiliulou.electricity.dto.ExchangeReasonCellDTO;

/**
 * @ClassName: Handler
 * @description:
 * @author: renhang
 * @create: 2024-07-19 14:07
 */
public interface Handler {
    
    /**
     * 如果 1：新仓门 如果 0: 旧仓门
     *
     * @param dto
     * @return
     */
    Integer doHandler(ExchangeReasonCellDTO dto);
}
