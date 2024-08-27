package com.xiliulou.electricity.bo.wechat;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigReceiverStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigStatusEnum;
import lombok.Data;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description: 微信支付参数详情
 *
 * @author caobotao.cbt
 * @date 2024/6/14 13:04
 */
@Data
public class WechatPayParamsDetails {
    
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
    
    
    /**
     * 分账方配置
     */
    private ProfitSharingConfig profitSharingConfig;
    
    /**
     * 分账接收方配置
     */
    private List<ProfitSharingReceiverConfig> profitSharingReceiverConfigs;
    
    
    /**
     * 获取可用的分账接收方支付配置
     *
     * @author caobotao.cbt
     * @date 2024/8/27 10:39
     */
    public List<ProfitSharingReceiverConfig> getEnableProfitSharingReceiverConfigs() {
        // 分账主配置必须可用
        if (Objects.isNull(this.profitSharingConfig) || !ProfitSharingConfigStatusEnum.OPEN.getCode().equals(this.profitSharingConfig.getConfigStatus())) {
            return Collections.emptyList();
        }
        
        // 分账接收方配置为启用的
        return Optional.ofNullable(this.profitSharingReceiverConfigs).orElse(Collections.emptyList()).stream()
                .filter(c -> ProfitSharingConfigReceiverStatusEnum.ENABLE.getCode().equals(c.getReceiverStatus())).collect(Collectors.toList());
        
    }
}
