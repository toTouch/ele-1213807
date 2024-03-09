package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/26 13:00
 * @desc
 */
public interface MerchantCabinetBindHistoryMapper {
    
    Integer batchInsert(@Param("list") List<List<MerchantCabinetBindHistory>> list);
    
    List<MerchantCabinetBindHistory> queryListByMonth(@Param("cabinetId") Long cabinetId,@Param("placeId") Long placeId,@Param("monthList") List<String> monthList,
            @Param("merchantId") Long merchantId,@Param("tenantId") Integer tenantId);
}
