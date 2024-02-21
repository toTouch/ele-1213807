package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 商户/场地/下 柜机电量/电费
 * @date 2024/2/20 19:13:10
 */
public interface MerchantCabinetPowerService {
    
    /**
     * 是否显示电费页面：0-不显示，1-显示
     */
    Integer isShowPowerPage(Long merchantId);
    
    /**
     * 从绑定记录表中获取商户关联所有场地下的柜机
     */
    MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long merchantId);
    
    /**
     * 从绑定记录表中获取商户关联某个场地下的柜机
     */
    List<MerchantPlaceCabinetVO> listCabinetByPlaceId(Long merchantId, Long placeId);
    
    MerchantCabinetPowerVO todayPowerAndCharge(MerchantCabinetPowerRequest request);
    
    MerchantCabinetPowerVO yesterdayPowerAndCharge(MerchantCabinetPowerRequest request);
    
    MerchantCabinetPowerVO thisMonthPowerAndCharge(MerchantCabinetPowerRequest request);
    
}
