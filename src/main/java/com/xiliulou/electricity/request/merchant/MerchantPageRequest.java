package com.xiliulou.electricity.request.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 22:28
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPageRequest {
    private Long size;
    
    private Long offset;
    
    private String name;
    
    private Long franchiseeId;
    
    private Long merchantGradeId;
    
    private Long channelEmployeeUid;
    
    private Integer tenantId;
    
    private String phone;
    
    private List<Long> franchiseeIdList;
}
