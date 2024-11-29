/**
 * Create date: 2024/7/17
 */

package com.xiliulou.electricity.bo.pay;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import lombok.Data;

import java.util.List;

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
     * 收款账号
     */
    private String receivableAccounts;
    
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
        return this.receivableAccounts;
    }
    
    @Override
    public String getPaymentChannel() {
        return ChannelEnum.ALIPAY.getCode();
    }
    
    @Override
    public List<ProfitSharingReceiverConfig> getEnableProfitSharingReceiverConfigs() {
        // TODO: 2024/9/6 支付宝暂时无分账配置
        return null;
    }
    
    @Override
    public ProfitSharingConfig getEnableProfitSharingConfig() {
        // TODO: 2024/9/6 支付宝暂时无分账配置
        return null;
    }
    
}
