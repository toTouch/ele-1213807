package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/28 18:30
 */

@Data
public class MerchantEmployeeQrCodeVO {
    private Long merchantId;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private Integer type;
    
    private String code;
    
}
