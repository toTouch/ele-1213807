package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeRecord;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRecordPageRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceVO;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:34
 * @desc
 */
public interface MerchantPlaceFeeRecordService {
    
    Integer countTotal(MerchantPlaceFeeRecordPageRequest merchantPlacePageRequest);
    
    List<MerchantPlaceFeeRecordVO> listByPage(MerchantPlaceFeeRecordPageRequest merchantPlacePageRequest);
    
    Integer save(MerchantPlaceFeeRecord merchantPlaceFeeRecord);
}
