/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/17
 */

package com.xiliulou.electricity.bo.pay;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.core.base.enums.ChannelEnum;
import lombok.Data;

/**
 * description: 支付宝支付配置业务详情
 *
 * @author caobotao.cbt
 * @date 2024/7/17 10:56
 */
@Data
public class AlipayAppConfigBizDetails extends BasePayConfig {
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 支付宝小程序appId
     */
    private String appId;
    
    /**
     * 卖家支付宝用户ID
     */
    private String sellerId;
    
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
    
    /**
     * 配置类型
     *
     * @see ChannelEnum
     */
    private Integer configType;
    
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    
    @Override
    public String getThirdPartyMerchantId() {
        return this.sellerId;
    }
    
    @Override
    public String getPaymentChannel() {
        return ChannelEnum.ALIPAY.getCode();
    }
    
}
