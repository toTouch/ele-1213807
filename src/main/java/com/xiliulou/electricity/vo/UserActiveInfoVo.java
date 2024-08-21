package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/3/1 18:21
 * @mood
 */
@Data
public class UserActiveInfoVo {
    
    private Long id;
    
    private Long uid;
    
    private Long activeTime;
    
    private String phone;
    
    private String userName;
    
    private String batterySn;
    
    private Long payCount;
    
    private BigDecimal batteryServiceFee;
    
    private Long franchiseeId;
    
    private String franchiseeName;
    
    /**
     * 套餐总到期时间
     */
    private Long memberCardExpireTime;
    
    private Long storeId;
    
    private Double soc;
}
