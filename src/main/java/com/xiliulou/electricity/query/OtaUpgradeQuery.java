package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zgw
 * @date 2022/10/14 17:25
 * @mood
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtaUpgradeQuery {
    
    private Integer cellNo;
    
    private String upgradeNo;
}
