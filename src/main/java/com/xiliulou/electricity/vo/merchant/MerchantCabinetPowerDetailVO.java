package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 柜机电费详情
 * @date 2024/2/26 03:20:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantCabinetPowerDetailVO {
    
    private String monthDate;
    
    private Long cabinetId;
    
    private String cabinetName;
    
    private String sn;
    
    private Double power;
    
    private Double charge;
    
    private Long startTime;
    
    private Long endTime;
    
    private Long placeId;
    
    private String placeName;
    
    private Integer bindStatus;
}
