package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityConfig;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: ExchangeMemberResultDTO
 * @description:
 * @author: renhang
 * @create: 2024-11-22 11:17
 */
@Data
@Builder
public class ExchangeMemberResultDTO {
    
    /**
     * 租户配置
     */
    private ElectricityConfig electricityConfig;
    
    /**
     * 用户绑定的电池
     */
    private ElectricityBattery electricityBattery;
}
