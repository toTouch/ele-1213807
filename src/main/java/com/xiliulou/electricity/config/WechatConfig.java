package com.xiliulou.electricity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author: eclair
 * @Date: 2021/3/18 08:34
 * @Description:
 */
@Configuration
@ConfigurationProperties("wechat")
@Data
@RefreshScope
public class WechatConfig {
    /**
     * 小程序appId
     */
    private String minProAppId;
    /**
     * 小程序秘钥
     */
    private String minProAppSecret;
    /**
     * 公众号appId
     */
    private String officeAccountAppId;
    /**
     * 公众号秘钥
     */
    private String officeAccountAppSecret;
    /**
     * 商户秘钥
     */
    private String mchid;
    /**
     * api秘钥
     */
    private String apiV3Key;
    /**
     * 商户证书序列号
     */
    private String mchCertificateSerialNo;
    /**
     * 商户证书私钥文件地址
     */
    private String mchCertificatePrivateKeyPath;


    /**
     * 商户证书私钥文件目录
     */
    private String mchCertificateDirectory;
    /**
     * 支付回调,后缀必须有"/"
     */
    private String payCallBackUrl;
    /**
     * 退款回调,后缀必须有"/"
     */
    private String refundCallBackUrl;


    /**
     * appId
     */
    private String appId;

    /**
     * apiName
     */
    private String apiName;

    /**
     * paternerKey
     */
    private String paternerKey;


    /**
     * tenantId
     */
    private Integer tenantId;

    /**
     * 运维二维码
     */
    private String maintenanceQr;

    /**
     * 租车套餐押金退款回调
     */
    private String carDepositRefundCallBackUrl;

    /**
     * 租车套餐租金退款回调
     */
    private String carRentRefundCallBackUrl;

    /**
     * 电池套餐退租金回调
     */
    private String batteryRentRefundCallBackUrl;

}
