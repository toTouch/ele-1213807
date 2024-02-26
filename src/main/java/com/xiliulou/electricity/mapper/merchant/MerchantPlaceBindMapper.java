package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
import com.xiliulou.electricity.request.merchant.MerchantPlaceConditionRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:51
 * @desc
 */
public interface MerchantPlaceBindMapper {
    
    int batchInsert(List<MerchantPlaceBind> merchantPlaceBindList);
    
    int batchUnBind(@Param("placeIdList") Set<Long> placeIdList, @Param("merchantId") Long merchantId, @Param("updateTime") long updateTime);
    
    List<MerchantPlaceBind> selectListByMerchantId(@Param("merchantId") Long merchantId, @Param("status") Integer status);
    
    Integer existPlaceFeeByMerchantId(@Param("merchantId") Long merchantId);
    
    List<MerchantPlaceBind> queryNoSettleByMerchantId(@Param("merchantId") Long merchantId);
    
    List<MerchantPlaceBind> selectListBindRecord(MerchantPlaceConditionRequest request);
    
    List<MerchantPlaceBind> selectListUnbindRecord(MerchantPlaceConditionRequest request);
}
