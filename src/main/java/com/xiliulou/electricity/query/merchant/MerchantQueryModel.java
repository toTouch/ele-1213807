package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/13 9:59
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantQueryModel {
    private Long size;
    
    private Long offset;
    
    private String name;
    
    private Long franchiseeId;
    
    private Long merchantGradeId;
    
    private Long channelEmployeeUid;
    
    private List<Long> idList;
    
    private Integer tenantId;
    
    private String phone;
    
    private List<Long> franchiseeIdList;
    
}
