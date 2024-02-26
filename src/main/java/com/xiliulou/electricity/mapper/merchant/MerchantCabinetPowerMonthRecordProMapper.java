package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.vo.merchant.MerchantPowerVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 商户电费（小程序端）
 * @date 2024/2/26 03:46:48
 */
public interface MerchantCabinetPowerMonthRecordProMapper {
    
    MerchantPowerVO sumMonthPower(@Param("cabinetIds") List<Long> cabinetIds, @Param("monthDate") String monthDate);
}
