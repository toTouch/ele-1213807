package com.xiliulou.electricity.enums.lostuser;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LostUserFirstEnum implements BasicEnum<Integer, String> {
    OPEN(0, "开启"),
    CLOSE(1, "关闭")
    ;
    
    private final Integer code;
    
    private final String desc;
}
