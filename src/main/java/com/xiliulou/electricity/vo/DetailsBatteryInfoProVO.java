package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author HeYafeng
 * @date 2025/1/3 10:45:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailsBatteryInfoProVO {
    
    /**
     * 套餐 id
     */
    private Long memberCardId;
    
    /**
     * 电池押金
     */
    private BigDecimal batteryDeposit;
    
    /**
     * 当前套餐到期时间
     */
    private Long orderExpireTime;
    
    /**
     * 用户绑定的电池型号
     */
    private List<String> batteryModels;
    
    /**
     * 电池编号
     */
    private String batterySn;
    
    /**
     * 当前套餐剩余次数
     */
    private Long orderRemainingNumber;
    
}
