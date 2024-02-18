package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

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
     * 柜机名称
     */
    private String cabinetName;
    
    /**
     * 状态
     */
    private String status;
    
    private Long createTime;
}
