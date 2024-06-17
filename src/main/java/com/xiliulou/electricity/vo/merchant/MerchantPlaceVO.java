package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/16 14:32
 * @desc
 */
@Data
public class MerchantPlaceVO {
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 联系方式
     */
    private String phone;
    /**
     * 区域
     */
    private String merchantAreaName;
    
    /**
     * 区域
     */
    private String merchantName;
    
    /**
     * 柜机名称
     */
    private String cabinetName;
    
    private List<Long> cabinetIdList;
    
    private List<MerchantPlaceCabinetBindVO> cabinetList;
    
    private Long createTime;
    
    /**
     * 是否被禁用：1 是，0 否
     */
    private Integer disable;
    
    /**
     * 区域id
     */
    private Long merchantAreaId;
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
}
