package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/18 20:16
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantJoinRecordQueryMode {
    /**
     * 商户id集合
     */
    private List<Long> merchantIdList;
    
    /**
     * 参与状态 1-已参与，2-邀请成功，3-已过期
     */
    private Integer status;
    
    private Integer tenantId;
    
    /**
     * 商户id
     */
    private Long merchantId;
    
    private Long size;
    
    private Long offset;
    
    private List<Long> franchiseeIds;
}
