package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName : MerchantPlaceFeeMonthRecordMapper
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */

public interface MerchantPlaceFeeMonthRecordMapper {
    Integer save(MerchantPlaceFeeMonthRecord record);
    
    List<MerchantPlaceFeeMonthRecord> selectListBySettlementTime(@Param("date") String date, @Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    List<MerchantPlaceFeeMonthRecord> selectList(@Param("placeIdList") List<Long> placeIdList,@Param("monthList") List<String> monthList);
}
