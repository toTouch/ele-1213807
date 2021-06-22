package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 09:26
 **/
@Data
@TableName("t_electricity_pay_params")
public class ElectricityPayParams {
    @TableId()
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

}
