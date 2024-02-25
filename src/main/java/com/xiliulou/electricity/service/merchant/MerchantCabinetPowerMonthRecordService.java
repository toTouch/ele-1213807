package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.MerchantPowerRequest;
import com.xiliulou.electricity.vo.merchant.MerchantCabinetPowerMonthRecordVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author HeYafeng
 * @description 电费月度统计
 * @date 2024/2/20 12:35:30
 */
public interface MerchantCabinetPowerMonthRecordService {
    
    List<MerchantCabinetPowerMonthRecordVO> listByPage(MerchantPowerRequest request);
    
    Integer countTotal(MerchantPowerRequest request);
    
    void exportExcel(MerchantPowerRequest request, HttpServletResponse response);
}
