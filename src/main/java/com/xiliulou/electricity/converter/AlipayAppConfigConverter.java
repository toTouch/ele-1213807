/**
 *  Create date: 2024/7/17
 */

package com.xiliulou.electricity.converter;

import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.entity.AlipayAppConfig;

import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/17 11:09
 */
public class AlipayAppConfigConverter {
    
    
    /**
     * 支付配置查询转换
     *
     * @param alipayAppConfig
     * @author caobotao.cbt
     * @date 2024/7/17 11:10
     */
    public static AlipayAppConfigBizDetails qryDoToDetails(AlipayAppConfig alipayAppConfig) {
        if (Objects.isNull(alipayAppConfig)) {
            return null;
        }
        AlipayAppConfigBizDetails alipayAppConfigBizDetails = new AlipayAppConfigBizDetails();
        alipayAppConfigBizDetails.setId(alipayAppConfig.getId());
        alipayAppConfigBizDetails.setAppId(alipayAppConfig.getAppId());
        alipayAppConfigBizDetails.setReceivableAccounts(alipayAppConfig.getReceivableAccounts());
        alipayAppConfigBizDetails.setAppSecret(alipayAppConfig.getAppSecret());
        alipayAppConfigBizDetails.setPublicKey(alipayAppConfig.getPublicKey());
        alipayAppConfigBizDetails.setPrivateKey(alipayAppConfig.getPrivateKey());
        alipayAppConfigBizDetails.setAppPublicKey(alipayAppConfig.getAppPublicKey());
        alipayAppConfigBizDetails.setAppPrivateKey(alipayAppConfig.getAppPrivateKey());
        alipayAppConfigBizDetails.setLoginDecryptionKey(alipayAppConfig.getLoginDecryptionKey());
        alipayAppConfigBizDetails.setTenantId(alipayAppConfig.getTenantId());
        alipayAppConfigBizDetails.setConfigType(alipayAppConfig.getConfigType());
        alipayAppConfigBizDetails.setFranchiseeId(alipayAppConfig.getFranchiseeId());
        return alipayAppConfigBizDetails;
        
    }
}
