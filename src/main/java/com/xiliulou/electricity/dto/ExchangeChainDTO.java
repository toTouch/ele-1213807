package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Franchisee;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName: ExchangeChainDTO
 * @description: 责任链内部存储，后续业务使用
 * @author: renhang
 * @create: 2024-11-19 15:14
 */
@Data
@Accessors(chain = true)
public class ExchangeChainDTO {
    
    /**
     * 格挡电池
     */
    private String batteryName;
    
    
    /**
     * 柜机
     */
    private ElectricityCabinet electricityCabinet;
    
    /**
     * 加盟商
     */
    private Franchisee franchisee;
    
    /**
     * 租户配置
     */
    private ElectricityConfig electricityConfig;
    
    /**
     * 用户绑定的电池
     */
    private ElectricityBattery electricityBattery;
}
