package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.request.merchant.MerchantPlaceConditionRequest;

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
     * 根据商户id查询所有绑定记录
     * @param status 状态 1-解绑，0-绑定
     */
    List<MerchantPlaceBind> listByMerchantId(Long merchantId, Integer status);
    
    Integer existPlaceFeeByMerchantId(Long merchantId);
    
    List<MerchantPlaceBind> queryNoSettleByMerchantId(Long merchantId);
    
    List<MerchantPlaceBind> listBindRecord(MerchantPlaceConditionRequest request);
    
    List<MerchantPlaceBind> listUnbindRecord(MerchantPlaceConditionRequest request);
}
