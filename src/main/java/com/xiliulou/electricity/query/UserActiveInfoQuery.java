package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/1 17:41
 * @mood
 */
@Data
@Builder
public class UserActiveInfoQuery {
    
    private String userName;
    
    private String phone;
    
    private Integer day;
    
    private String batterySn;
    
    private Integer payCount;
    
    private Long offset;
    
    private Long size;
    
    private Integer tenantId;
    
    private Long limitTime;
}
