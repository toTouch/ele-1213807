package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.query.merchant.MerchantPowerQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthRecordVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 电费月度统计
 * @date 2024/2/20 12:36:46
 */
public interface MerchantCabinetPowerMonthRecordMapper {
    
    List<MerchantCabinetPowerMonthRecordVO> selectListByPage(MerchantPowerQueryModel queryModel);
    
    Integer countTotal(MerchantPowerQueryModel queryModel);
}
