package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.MerchantCabinetPowerRequest;
import com.xiliulou.electricity.vo.merchant.MerchantProCabinetPowerDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerLineVO;
import com.xiliulou.electricity.vo.merchant.MerchantProPowerVO;

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
    Integer isShowPowerPage(Long uid);
    
    /**
     * 从绑定记录表中获取商户关联所有场地下的柜机
     */
    MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long uid);
    
    /**
     * 从绑定记录表中获取商户关联某个场地下的柜机
     */
    List<MerchantPlaceCabinetVO> listCabinetByPlaceId(Long uid, Long placeId);
    
    /**
     * 电费/场地费 筛选条件-获取要统计的柜机id
     */
    List<Long> getStaticsCabinetIds(MerchantCabinetPowerRequest request);
    
    MerchantProPowerLineVO lineData(MerchantCabinetPowerRequest request);
    
    List<MerchantProCabinetPowerDetailVO> cabinetPowerDetail(MerchantCabinetPowerRequest request);
    
    MerchantProPowerVO powerData(MerchantCabinetPowerRequest request);
}
