package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 柜机电费详情
 * @date 2024/2/26 03:20:07
 */
@Data
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
