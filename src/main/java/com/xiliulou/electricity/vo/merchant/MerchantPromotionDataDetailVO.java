package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @ClassName : MerchantPromotionDataDetailVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-24
 */
@Data
public class MerchantPromotionDataDetailVO {
    private Long uid;
    
    private String userName;
    
    private String phone;
    
    private Integer status;
    
    private Long scanCodeTime;
}
