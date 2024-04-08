package com.xiliulou.electricity.request.merchant;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 修改邀请人请求
 * @date 2024/3/27 10:37:25
 */
@Builder
@Data
public class MerchantModifyInviterRequest {
    
    private Long size;
    
    private Long offset;
    
    /***
     * 骑手uid
     */
    private Long uid;
    
    /***
     * 商户名称
     */
    private String merchantName;
}
