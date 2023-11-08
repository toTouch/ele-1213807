package com.xiliulou.electricity.query.enterprise;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2023/10/19 9:43
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseUserCostRecordQuery {
    
    private Long uid;
    
    private Long enterpriseId;
    
    /**
     * 起始时间
     */
    private Long beginTime;
    
    /**
     * 终止时间
     */
    private Long endTime;
    
    private Long size;
    
    private Long offset;
    
}
