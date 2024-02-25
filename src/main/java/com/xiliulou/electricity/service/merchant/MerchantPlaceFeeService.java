package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetFeeDetailItemVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetFeeDetailVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeCurMonthVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeLineDataVO;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/25 10:27
 * @desc 小程序段 商户场地费
 */
public interface MerchantPlaceFeeService {
    
    Integer isShowPlacePage(Long merchantId);
    
    /**
     * 商户小程序统计场地费
     * @param request
     * @return
     */
    MerchantPlaceFeeCurMonthVO getFeeData(MerchantPlaceFeeRequest request);
    
    MerchantPlaceFeeLineDataVO lineData(MerchantPlaceFeeRequest request);
    
    List<MerchantPlaceCabinetFeeDetailVO> getCabinetPlaceDetail(MerchantPlaceFeeRequest request);
    
    List<MerchantPlaceCabinetFeeDetailItemVO> getPlaceDetailByCabinetId(MerchantPlaceFeeRequest request);
}
