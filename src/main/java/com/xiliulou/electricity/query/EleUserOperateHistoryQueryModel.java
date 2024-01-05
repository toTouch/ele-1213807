package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * Description: EleUserOperateHistoryQueryModel
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2023/12/28 14:09
 */

@Data
@Builder
public class EleUserOperateHistoryQueryModel {
    
    private Integer tenantId;
    
    private Long uid;
    
}