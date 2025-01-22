package com.xiliulou.electricity.request.failureAlarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/1/2 9:23
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WarnHandlePageRequest {
    /**
     * 批次号
     */
    private String batchNo;
    
    private Integer tenantId;
    
    
    private Long size;
    
    private Long offset;
}
