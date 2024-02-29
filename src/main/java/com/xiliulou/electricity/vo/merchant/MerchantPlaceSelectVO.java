package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/2/18 16:33
 * @desc 小程序场地员工下拉框返回vo
 */
@Data
public class MerchantPlaceSelectVO {
    /**
     * 场地id
     */
    private Long placeId;
    
    /**
     * 场地名称
     */
    private String placeName;
    /**
     * 商户id
     */
    private Long merchantId;
    
    /**
     * 是否禁用：0 否，1 是
     */
    private Integer status;
}
