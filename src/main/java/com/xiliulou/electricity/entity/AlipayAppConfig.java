package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 支付宝小程序配置(AlipayAppConfig)实体类
 *
 * @author zzlong
 * @since 2024-07-08 16:45:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_alipay_app_config")
public class AlipayAppConfig {
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 支付宝小程序appId
     */
    private String appId;
    
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
    
    private Integer tenantId;
    
    private Long createTime;
    
    private Long updateTime;
    
}
