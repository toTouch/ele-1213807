

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 08:58
 */
@Getter
public enum ProfitSharingConfigStatusEnum {
    
    OPEN(0, "开启"),
    CLOSE(1, "关闭");
    
    private Integer code;
    
    private String desc;
    
    
    ProfitSharingConfigStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static final Map<Integer, ProfitSharingConfigStatusEnum> MAP = new HashMap<>();
    
    static {
        Arrays.stream(values()).forEach(v -> {
            MAP.put(v.code, v);
        });
    }
    
}
