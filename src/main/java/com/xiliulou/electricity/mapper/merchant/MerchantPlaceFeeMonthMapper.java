package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/22 11:34
 * @desc
 */
public interface MerchantPlaceFeeMonthMapper {
    
    Integer batchInsert(@Param("list") List<MerchantPlaceFeeMonth> list);
    
    List<Long> selectCabinetIdByMerchantId(@Param("merchantId") Long merchantId);
    
    Integer existPlaceFeeByMerchantId(@Param("merchantId") Long merchantId);
    
    List<MerchantPlaceFeeMonth> selectListByMonth(@Param("placeId") Long placeId,@Param("cabinetId") Long cabinetId
            ,@Param("monthList") List<String> monthList);
    
    List<MerchantPlaceFeeMonth> selectListByMerchantId(@Param("merchantId") Long merchantId,@Param("cabinetId") Long cabinetId,@Param("placeId") Long placeId);
}
