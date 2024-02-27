package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantPowerDetailVO {
    
    private Long eid;
    
    private Double power;
    
    private Double charge;
    
    private Long startTime;
    
    private Long endTime;
    
    private Long merchantId;
    
    private Long placeId;
    
    /**
     * 最新上报数据的时间，用于排序
     */
    private Long latestTime;
    
    /**
     * 柜机和商户的绑定状态：0-绑定，1-解绑
     */
    private Integer cabinetMerchantBindStatus;
}
