package com.xiliulou.electricity.enums.asset;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockStatusEnum  implements BasicEnum<Integer, String> {
    STOCK(0, "库存"),
    UN_STOCK(1, "已出库"),
    ;
    
    private final Integer code;
    
    private final String desc;
}
