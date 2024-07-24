/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/14
 */

package com.xiliulou.electricity.bo.wechat;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.core.base.enums.ChannelEnum;
import lombok.Data;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;

/**
 * description: 微信支付参数详情
 *
 * @author caobotao.cbt
 * @date 2024/6/14 13:04
 */
@Data
public class WechatPayParamsDetails extends BasePayConfig {
    
    private Integer id;
    
    /**
     * 微信公众号id
     */
    private String officeAccountAppId;
    
    /**
     * 微信公众号密钥
     */
    private String officeAccountAppSecret;
    
    /**
     * 商家小程序appid
     */
    private String merchantMinProAppId;
    
    /**
     * 商家小程序appSecert
     */
    private String merchantMinProAppSecert;
    
    /**
     * 微信商户号
     */
    private String wechatMerchantId;
    
    /**
     * 微信商户证书号
     */
    private String wechatMerchantCertificateSno;
    
    /**
     * 微信商户私钥证书地址
     */
    private String wechatMerchantPrivateKeyPath;
    
    /**
     * 微信支付v3的api密钥
     */
    private String wechatV3ApiKey;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    
    /**
     * apiName
     */
    private String apiName;
    
    /**
     * paternerKey
     */
    private String paternerKey;
    
    
    /**
     * 商家版小程序 appid
     */
    private String merchantAppletId;
    
    /**
     * 商家版小程序 appSecret
     */
    private String merchantAppletSecret;
    
    
    /**
     * 配置类型
     */
    private Integer configType;
    
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    
    /**
     * 私钥
     */
    private PrivateKey privateKey;
    
    
    /**
     * 微信证书
     */
    private HashMap<BigInteger, X509Certificate> wechatPlatformCertificateMap;
    
    @Override
    public String getThirdPartyMerchantId() {
        return this.wechatMerchantId;
    }
    
    
    @Override
    public String getPaymentChannel() {
        return ChannelEnum.WECHAT.getCode();
    }
    
}
