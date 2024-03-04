package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/25 23:05
 * @desc
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantCabinetFeeDetailVO {
    /**
     * 柜机名称
     */
    private String cabinetName;
    /**
     * 场地费
     */
    private BigDecimal placeFee;
    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 结束时间
     */
    private Long endTime;
    
    /**
     * 类型(1-解绑，0-绑定)
     */
    private Integer status;
    
    /**
     * 场地名称
     */
    private String placeName;
    
    private Long placeId;
    
    private Long merchantId;
    
    private Long cabinetId;
    
    private String calculateMonth;
}
