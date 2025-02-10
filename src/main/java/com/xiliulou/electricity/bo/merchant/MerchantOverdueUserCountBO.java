package com.xiliulou.electricity.bo.merchant;

import lombok.Data;

@Data
public class MerchantOverdueUserCountBO {
    /**
     * 商户id
     */
    private Long merchantId;

    /**
     * 用户数量
     */
    private Integer overdueUserCount;
}
