package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.enums.ElectricityConfigExtraEnum;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 系统设置VO
 * @date 2025/2/12 17:52:43
 */
@Data
public class ElectricityConfigVO extends ElectricityConfig {
    
    /**
     * 用户账号注销开关：0-开启 1-关闭（默认）
     *
     * @see ElectricityConfigExtraEnum
     */
    private Integer accountDelSwitch;
}
