package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * Description: EleUserOperateHistoryQueryModel
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2023/12/28 14:09
 */

@Data
public class EleUserOperateHistoryQueryModel {
    
    private Long size;
    
    private Long offset;
    
    private Integer tenantId;
    
}