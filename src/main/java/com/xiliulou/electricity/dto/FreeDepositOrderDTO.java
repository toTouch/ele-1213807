package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: FreeDepositOrderDTO
 * @description:
 * @author: renhang
 * @create: 2024-08-22 09:43
 */
@Data
@Builder
public class FreeDepositOrderDTO {
    
    /**
     * 免押渠道
     */
    private Integer channel;
    
    private String data;
    
    private String extraData;
    
    private String path;
    
}
