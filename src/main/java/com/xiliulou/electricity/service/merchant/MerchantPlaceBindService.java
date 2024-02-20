package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceAndCabinetUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:49
 * @desc
 */
public interface MerchantPlaceBindService {
    
    int batchInsert(List<MerchantPlaceBind> merchantPlaceBindList);
    
    int batchUnBind(Set<Long> unBindList, Long merchantId, long updateTime);
    
    /**
     * 是否显示电费页面：0-不显示，1-显示
     */
    Integer isShowPowerPage(Long merchantId);
    
    MerchantPlaceAndCabinetUserVO listPlaceAndCabinetByMerchantId(Long merchantId);
    
    List<MerchantPlaceCabinetVO> listCabinetByPlaceId(Long merchantId, Long placeId);
}
