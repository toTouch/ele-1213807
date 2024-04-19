package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/28 14:01
 * @desc
 */
@Data
public class MerchantPlaceUpdateShowVO {
    /**
     * id
     */
    private Long id;
    /**
     * id
     */
    private String name;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 联系方式
     */
    private String phone;
    /**
     * 区域id
     */
    private Long merchantAreaId;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;
    /**
     * 场地地址
     */
    private String address;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 区域名称
     */
    private String merchantAreaName;
    
}
