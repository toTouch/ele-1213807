package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/28 21:17
 * @desc
 */
@Data
public class MerchantCabinetFeeDetailShowVO {
    /**
     * 商户详情
     */
    private List<MerchantPlaceCabinetFeeDetailVO> cabinetFeeDetailList;
    
    /**
     * 设备数量
     */
    private Integer cabinetCount;
    
    private Integer total;
    
    
}
