package com.xiliulou.electricity.enums.lostuser;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PackageTypeEnum implements BasicEnum<Integer, String> {
    ELECTRICITY(0, "电"),
    CAR(1, "车")
    ;
    
    private final Integer code;
    
    private final String desc;
}
