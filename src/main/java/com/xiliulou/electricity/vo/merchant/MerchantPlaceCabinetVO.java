package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/16 14:32
 * @desc
 */
@Data
public class MerchantPlaceCabinetVO {
    
    /**
     * 柜机id
     */
    private Long cabinetId;
    /**
     * 柜机名称
     */
    private String cabinetName;
    /**
     * 是否禁用：0：否，1：是
     */
    private Integer disable;
    
    private Long placeId;
    
    public final static Integer YES = 1;
    public final static Integer NO = 0;
    
}
