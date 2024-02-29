package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/13 12:27
 * @desc
 */
@Data
public class MerchantPlaceCabinetBindVO {
    private Long merchantId;
    /**
     * 电柜编号
     */
    private String cabinetSn;
    
    /**
     * 电柜名称
     */
    private String cabinetName;
    
    /**
     *  主键ID
     */
    private Long id;
    /**
     *  场地id
     */
    private Long placeId;
    /**
     *  柜机id
     */
    private Long cabinetId;
    /**
     *  绑定时间
     */
    private Long bindTime;
    /**
     *  解绑时间
     */
    private Long unBindTime;
    /**
     *  类型(1-解绑，0-绑定)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Long createTime;
}
