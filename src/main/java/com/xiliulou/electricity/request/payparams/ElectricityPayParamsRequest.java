/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/12
 */

package com.xiliulou.electricity.request.payparams;

import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * description: 支付参数请求
 *
 * @author caobotao.cbt
 * @date 2024/6/12 15:51
 */
@Data
public class ElectricityPayParamsRequest {
    
    @NotNull(groups = UpdateGroup.class, message = "id不可为空")
    private Integer id;
    
    /**
     * 配置类型
     *
     * @see ElectricityPayParamsConfigEnum
     */
    @NotNull(groups = {CreateGroup.class}, message = "配置类型不能为空")
    private Integer configType;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
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
     * <p>
     *    Description: 微信支付平台公钥ID
     * </p>
    */
    private String pubKeyId;
    
//    /**
//     * 商家版小程序 appid
//     */
//    private String merchantAppletId;
//
//    /**
//     * 商家版小程序 appSecret
//     */
//    private String merchantAppletSecret;

}
