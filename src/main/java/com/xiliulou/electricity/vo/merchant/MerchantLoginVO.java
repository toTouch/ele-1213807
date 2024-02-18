package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : eclair
 * @date : 2024/2/18 13:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantLoginVO {
    
    /*
     * 手机号
     */
    private String phone;
    
    /*
     *用户姓名
     */
    private String username;
    
    // 授权的token
    private String token;
    
    /*
     * 租户ID
     */
    private Long tenantId;
    
    /*
     *绑定的商户/渠道ID
     */
    private Long bindBusinessId;
    
    /*
     * 用户UID
     */
    private Long uid;
    
    /*
     * 用户类型
     */
    private Integer userType;
    
    
    
}
