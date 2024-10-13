package com.xiliulou.electricity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName: CellTypeEnum
 * @description:
 * @author: renhang
 * @create: 2024-09-19 17:31
 */
@AllArgsConstructor
@Getter
public enum CellTypeEnum {
    OLD_CELL(1, "旧仓门"),
    
    NEW_CELL(2, "新仓门");
    
    private final Integer code;
    
    private final String desc;
}
