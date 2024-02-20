package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序端-电费/场地费 筛选条件：场地列表、柜机列表
 * @date 2024/2/20 15:14:00
 */
@Data
public class MerchantPlaceAndCabinetUserVO {
    
    private Long merchantId;
    
    List<MerchantPlaceUserVO> placeList;
    
    List<MerchantPlaceCabinetVO> cabinetList;
}
