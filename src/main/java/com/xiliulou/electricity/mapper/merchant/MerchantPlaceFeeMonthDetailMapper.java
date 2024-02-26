package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/22 13:40
 * @desc
 */
public interface MerchantPlaceFeeMonthDetailMapper {
    
    Integer batchInsert(@Param("list") List<List<MerchantPlaceFeeMonthDetail>> list);
    
    List<Long> selectMerchantIdList(@Param("startTime") Long startTime,@Param("endTime") Long endTime);
    
    List<MerchantPlaceFeeMonthDetail> selectListByMerchantId(@Param("merchantId") Long merchantId,@Param("startTime") Long startTime,@Param("endTime") Long endTime);
    
    List<MerchantPlaceFeeMonthDetail> selectListByMonth(@Param("cabinetId") Long cabinetId,@Param("placeId") Long placeId,@Param("monthList") List<String> monthList);
}
