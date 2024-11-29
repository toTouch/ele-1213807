/**
 * Create date: 2024/8/23
 */

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 16:58
 */
@Getter
public enum ProfitSharingConfigReceiverTypeEnum {
    
    MERCHANT(1, "MERCHANT_ID", "商户"),
    PERSONAGE(2, "PERSONAL_OPENID", "个人");
    
    ProfitSharingConfigReceiverTypeEnum(Integer code, String wechatCode, String desc) {
        this.code = code;
        this.wechatCode = wechatCode;
        this.desc = desc;
    }
    
    public static final Map<Integer, String> CODE_MAP = new HashMap<>();
    
    static {
        Arrays.stream(values()).forEach(e -> CODE_MAP.put(e.code, e.wechatCode));
    }
    
    private Integer code;
    
    private String wechatCode;
    
    private String desc;
}
