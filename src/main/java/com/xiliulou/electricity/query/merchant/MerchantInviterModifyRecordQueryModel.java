package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 邀请人修改记录查询
 * @date 2024/3/28 09:32:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantInviterModifyRecordQueryModel {
    
    private Long size;
    
    private Long offset;
    
    private Long uid;
    
    private Integer tenantId;
}
