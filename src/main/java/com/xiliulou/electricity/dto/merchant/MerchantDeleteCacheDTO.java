package com.xiliulou.electricity.dto.merchant;

import com.xiliulou.electricity.entity.User;
import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/11 9:11
 * @desc 商户缓存删除dto
 */
@Data
public class MerchantDeleteCacheDTO {
    private Long merchantId;
    private boolean isDeleteUserFlag;
    private Long uid;
    private Integer tenantId;
    // enterprise_Info:
    private Long enterpriseInfoId;
    
    private User user;
}
