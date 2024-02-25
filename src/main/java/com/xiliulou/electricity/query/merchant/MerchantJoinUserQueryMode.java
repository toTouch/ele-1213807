package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description
 * @date 2024/2/25 12:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantJoinUserQueryMode {

    private Long uid;

    private String name;

    private String phone;

    private Long merchantUid;

    private Integer tenantId;

    /**
     * 查询类型 1---查询所有 2--查询正常 3--查询临近过期 4--查询已过期
     */
    private Integer type;

    private Long expireTime;

    private Long size;

    private Long offset;

}
