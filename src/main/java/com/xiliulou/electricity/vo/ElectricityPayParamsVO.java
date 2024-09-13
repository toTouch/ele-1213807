

package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * description: 支付参数vo
 *
 * @author caobotao.cbt
 * @date 2024/6/12 18:07
 */
@Data
public class ElectricityPayParamsVO implements Serializable {
    
    private static final long serialVersionUID = -4627629686526138973L;
    
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
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
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
     *
     * @see ElectricityPayParamsConfigEnum
     */
    private Integer configType;
    
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
}
