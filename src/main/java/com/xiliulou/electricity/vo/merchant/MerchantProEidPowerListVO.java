package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 电量/电费
 * @date 2024/2/26 03:02:06
 */
@Data
public class MerchantProEidPowerListVO {
    
    private Long eid;
    
    private List<MerchantProLivePowerVO> powerList;
}
