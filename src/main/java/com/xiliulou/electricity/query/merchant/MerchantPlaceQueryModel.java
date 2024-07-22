package com.xiliulou.electricity.query.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/9 15:19
 * @desc
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MerchantPlaceQueryModel {
    
    private List<Long> idList;
    
    private Long nqId;
    
    private Long size;
    
    private Long offset;
    
    /**
     * 场地名称
     */
    private String name;
    
    /**
     * 区域id
     */
    private Long merchantAreaId;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 场地id
     */
    private Long placeId;
    
    /**
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 商户id
     */
    private Long merchantId;
    
    private List<Long> franchiseeIdList;
    
}
