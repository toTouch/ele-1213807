package com.xiliulou.electricity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author hrp
 * @date 2022/3/23 15:37
 * @mood
 */
@Data
public class OrderStatisticsVo {
    
    /**
     * 换电订单
     */
    private Integer electricityOrderCount;
    
    /**
     * 购卡数量
     */
    private Integer memberCardOrderCount;
    
    /**
     * 租电订单
     */
    private Integer rentBatteryCount;
    
    /**
     * 周订单统计
     */
    private List<WeekOrderStatisticVo> weekOrderStatisticVos;
    
    /**
     * 周月卡统计
     */
    private List<WeekOrderStatisticVo> weekMemberCardStatisticVos;
    
    /**
     * 周租电统计
     */
    private List<WeekOrderStatisticVo> weekRentBatteryStatisticVos;
    
}
