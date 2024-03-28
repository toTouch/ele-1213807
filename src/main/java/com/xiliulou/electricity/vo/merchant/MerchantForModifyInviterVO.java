package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 修改邀请人-商户列表
 * @date 2024/3/28 11:34:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantForModifyInviterVO {
    
    private Long id;
    
    /**
     * 商户UID
     */
    private Long uid;
    
    /**
     * 商户名称
     */
    private String name;
}
