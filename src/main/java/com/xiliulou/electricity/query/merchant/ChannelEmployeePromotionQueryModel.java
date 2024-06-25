package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/21 20:48
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelEmployeePromotionQueryModel {
    /**
     * 偏移量
     */
    private Integer offset;
    
    /**
     * 取值数量
     */
    private Integer size;
    
    /**
     * 出账年月日
     */
    private Long time;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 开始时间
     */
    private Long startTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    
    /**
     * 出账年月日
     */
    private String monthDate;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
}
