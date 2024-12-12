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
    
    /**
     * 旧电池检测失败，灵活续费发生套餐转换，灵活续费为换电时，会拦截不分配电池，传1-开始换电
     */
    private Integer secondFlexibleRenewal;


    private final Integer code;
}
