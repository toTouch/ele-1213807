package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantPowerDetailVO {
    
    private Long eid;
    
    private Double power;
    
    private Double charge;
    
    /**
     * 最新上报数据的时间，用于排序
     */
    private Long latestTime;
    
    private Long startTime;
    
    private Long endTime;
}
