package com.xiliulou.electricity.request.merchant;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 商户：扫码参数
 * @date 2024/2/29 21:11:18
 */
@Data
public class MerchantJoinScanRequest {
    
    /**
     * 规则：merchantId:inviterUid:inviterType     inviterType：1-商户本人 2-场地员工
     */
    private String code;
}
