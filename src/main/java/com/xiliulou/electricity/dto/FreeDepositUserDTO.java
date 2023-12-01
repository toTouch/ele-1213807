package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2023/12/1 11:07
 */
@Data
@Builder
public class FreeDepositUserDTO {
    
    private Long uid;
    
    private String phoneNumber;
    
    private String idCard;
    
    private String realName;
    
}
