package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author BaoYu
 * @description
 * @date 2024/2/25 12:03
 */

@Data
public class MerchantJoinUserVO {

    private Long id;

    private Long joinUid;

    private String name;

    private String phone;

    private Long merchantId;

    private String merchantName;

    private Long packageId;

    private Long expireTime;

    private Integer status;

    private Integer tenantId;

}
