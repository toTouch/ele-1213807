/**
 * Create date: 2024/11/6
 */

package com.xiliulou.electricity.enums.payparams;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 微信支付凭证类型
 *
 * @author caobotao.cbt
 * @date 2024/11/6 10:26
 */
@Getter
public enum ElectricityPayParamsCertTypeEnum {
    
    PLATFORM_CERTIFICATE(0, "平台证书"),
    
    PLATFORM_PUBLIC_KEY(1, "平台公钥"),
    ;
    
    private Integer type;
    
    private String desc;
    
    ElectricityPayParamsCertTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    
    
    private static final Map<Integer, ElectricityPayParamsCertTypeEnum> MAP = new ConcurrentHashMap<>();
    
    
    static {
        Arrays.stream(values()).forEach(e -> {
            MAP.put(e.type, e);
        });
    }
    
    
    public static ElectricityPayParamsCertTypeEnum getByType(Integer type) {
        return MAP.get(type);
    }
}
