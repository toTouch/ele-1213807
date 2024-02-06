package com.xiliulou.electricity.vo.merchant;

/**
 * @author HeYafeng
 * @description 二维码属性
 * @date 2024/2/6 17:38:07
 */
public class MerchantCodeVO {
    
    /**
     * 租户编号
     */
    private String tenantCode;
    
    /**
     * code规则：merchantId;inviterUid;inviterType
     * 邀请人类型：1-商户本人 2-场地员工
     */
    private String code;
}
