package com.xiliulou.electricity.query;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/2/15 11:31
 * @mood
 */
@Data
public class CarMemberCardExpireBreakPowerQuery {
    private Integer tenantId;
    //private Long cid;
    private String sn;
    
    private Long uid;
    
    private String name;
    
    private String phone;
}
