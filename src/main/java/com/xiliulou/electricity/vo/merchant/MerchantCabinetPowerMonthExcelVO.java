package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 场地电费详情
 * @date 2024/2/25 15:55:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantCabinetPowerMonthExcelVO {
    
    private String monthDate;
    
    private String placeName;
    
    /**
     * 月用电量
     */
    private Double monthSumPower;
    
    /**
     * 月电费
     */
    private Double monthSumCharge;
    
    /**
     * 电柜编号
     */
    private String sn;
    
    /**
     * 开始度数
     */
    private Double startPower;
    
    /**
     * 结束度数
     */
    private Double endPower;
    
    /**
     * 用电量
     */
    private Double sumPower;
    
    /**
     * 电价规则
     */
    private String jsonRule;
    
    /**
     * 用电电费
     */
    private Double sumCharge;
    
    /**
     * 开始时间
     */
    private Long beginTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    
}
