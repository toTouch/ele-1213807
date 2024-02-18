package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/18 20:40
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantUserAmountQueryMode {
    private List<Long> uidList;
    private Integer tenantId;
}
