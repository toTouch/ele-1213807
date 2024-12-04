package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-18-16:35
 */
@Data
public class AlipayAppConfigQuery {
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    @NotBlank(message = "appId不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String appId;
    
    /**
     * 小程序支付中，商户实际经营主体的小程序应用的appid，也即最终唤起收银台支付所在的小程序的应用id
     */
    @NotBlank(message = "receivableAccounts不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String receivableAccounts;
    
    /**
     * 支付宝小程序appSecret
     */
    private String appSecret;
    
    @NotBlank(message = "支付宝公钥不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String publicKey;
    
    /**
     * 支付宝私钥
     */
    private String privateKey;
    
    /**
     * 应用公钥
     */
    private String appPublicKey;
    
    @NotBlank(message = "应用私钥不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String appPrivateKey;
    
    @NotBlank(message = "解密密钥不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String loginDecryptionKey;
    
    /**
     * 配置类型
     */
    private Integer configType;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 商户授权token
     */
    private String appAuthToken;
    
}
