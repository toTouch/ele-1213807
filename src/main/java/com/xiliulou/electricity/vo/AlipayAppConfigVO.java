package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-18-16:16
 */
@Data
public class AlipayAppConfigVO {
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 支付宝小程序appId
     */
    private String appId;
    
    /**
     * 小程序支付中，商户实际经营主体的小程序应用的appid，也即最终唤起收银台支付所在的小程序的应用id
     */
    private String opAppId;
    
    /**
     * 支付宝小程序appSecret
     */
    private String appSecret;
    
    /**
     * 支付宝公钥
     */
    private String publicKey;
    
    /**
     * 支付宝私钥
     */
    private String privateKey;
    
    /**
     * 应用公钥
     */
    private String appPublicKey;
    
    /**
     * 应用私钥
     */
    private String appPrivateKey;
    
    /**
     * 解密密钥
     */
    private String loginDecryptionKey;
    
    /**
     * 配置类型
     */
    private Integer configType;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    private String franchiseeName;
    
}
