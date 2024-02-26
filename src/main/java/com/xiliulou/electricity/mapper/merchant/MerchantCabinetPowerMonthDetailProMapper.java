package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序月度电费详情
 * @date 2024/2/22 09:46:34
 */
public interface MerchantCabinetPowerMonthDetailProMapper {
    
    Long selectLatestReportTime(@Param("cabinetId") Long cabinetId, @Param("monthList") List<String> monthList);
    
    List<MerchantCabinetPowerMonthDetail> selectListByMonth(@Param("cabinetId") Long cabinetId, @Param("monthList") List<String> monthList);
}
