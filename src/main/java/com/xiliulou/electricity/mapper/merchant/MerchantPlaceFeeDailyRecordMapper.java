package com.xiliulou.electricity.mapper.merchant;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @ClassName : MerchantPlaceFeeDailyRecordMapper
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */

@Repository
public interface MerchantPlaceFeeDailyRecordMapper {
    
    BigDecimal selectList(@Param("cabinetStartTime") Long cabinetStartTime,@Param("cabinetEndTime") Long cabinetEndTime,@Param("cabinetId") Long cabinetId);
}
