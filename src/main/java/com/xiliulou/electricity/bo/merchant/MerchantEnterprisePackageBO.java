package com.xiliulou.electricity.bo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/27 15:52
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantEnterprisePackageBO {
    /**
     *  套餐名称
     */
    private String name;
    /**
     * 套餐id
     */
    private Long packageId;
}
