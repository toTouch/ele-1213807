package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ExchangeUserSelectVo
 * @description:
 * @author: renhang
 * @create: 2024-07-19 11:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeUserSelectVo {
    
    /**
     * 上一次换电是否成功,1为上次成功，0失败
     */
    private Integer lastExchangeIsSuccess;
    
    
    public static final Integer LAST_EXCHANGE_SUCCESS = 1;
    
    public static final Integer LAST_EXCHANGE_FAIL = 0;
}
