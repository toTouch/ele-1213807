package com.xiliulou.electricity.bo.batteryPackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/3/21 17:49
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBatteryMemberCardPackageBO {
    /**
     * 押金
     */
    private BigDecimal deposit;
    
    /**
     * 0:不限制,1:限制
     */
    private Integer limitCount;
    
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户id
     */
    private Long uid;
    /**
     * 套餐id
     */
    private Long memberCardId;
    /**
     * 套餐订单Id
     */
    private String orderId;
    
    private Integer payType;
    
    private String createTime;
}
