package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: LessTimeExchangeDTO
 * @description:
 * @author: renhang
 * @create: 2024-09-18 09:48
 */
@Data
@Builder
public class LessTimeExchangeDTO {
    
    private Integer eid;
    
    
    /**
     * 如果不是同一个柜机，重新扫码换电不拦截，1
     */
    private Integer isReScanExchange;
    
    
}
