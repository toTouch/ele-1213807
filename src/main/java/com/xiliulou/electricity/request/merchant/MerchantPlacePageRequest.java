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
public class MerchantPlacePageRequest {
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
     *  场地id集合
     */
    private List<Long> idList;
    
    /**
     * 商户id
     */
    private Long merchantId;
    
    /**
     * 登录用户绑定的加盟商
     */
    private List<Long> bindFranchiseeIdList;
    
    private List<Long> franchiseeIdList;
}
