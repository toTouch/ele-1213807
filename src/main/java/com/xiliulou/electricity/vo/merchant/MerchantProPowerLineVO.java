package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 折线图
 * @date 2024/2/26 03:20:07
 */
@Data
public class MerchantProPowerLineVO {
    
    /**
     * 电量折线图
     */
    private List<MerchantProPowerLineDataVO> powerList;
    
    /**
     * 电费折线图
     */
    private List<MerchantProPowerChargeLineDataVO> chargeList;
}
