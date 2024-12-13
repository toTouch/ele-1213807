package com.xiliulou.electricity.bo.meituan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @date 2024/12/13 09:51:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MtBatteryPackageBO {
    
    private Long id;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 订单ID
     */
    private String meiTuanOrderId;
    
    /**
     * 下单时间
     */
    private Long meiTuanOrderTime;
    
    /**
     * 实付价
     */
    private BigDecimal meiTuanActuallyPayPrice;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 订单使用状态:0-未使用 1-已使用
     */
    private Integer orderUseStatus;
    
    /**
     * 套餐押金
     */
    private BigDecimal packageDeposit;
    
    /**
     * 套餐是否支持免押:0--是 1--否
     */
    private Integer freeDeposit;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
}
