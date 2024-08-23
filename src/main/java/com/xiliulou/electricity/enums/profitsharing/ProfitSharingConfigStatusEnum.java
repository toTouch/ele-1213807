

package com.xiliulou.electricity.enums.profitsharing;

import lombok.Data;
import lombok.Getter;

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
}
