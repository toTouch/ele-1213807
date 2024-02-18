package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:49
 * @desc
 */
public interface MerchantPlaceBindService {
    
    int batchInsert(List<MerchantPlaceBind> merchantPlaceBindList);
    
    int batchUnBind(Set<Long> unBindList , Long merchantId, long updateTime);
}
