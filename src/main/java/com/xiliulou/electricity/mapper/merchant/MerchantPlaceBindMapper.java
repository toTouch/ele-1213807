package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceBind;
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
    
    int batchUnBind(@Param("placeIdList") Set<Long> placeIdList,@Param("merchantId") Long merchantId,@Param("updateTime") long updateTime);
}
