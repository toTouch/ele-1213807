package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVo;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 9:52
 * @desc 场地柜机绑定
 */
public interface MerchantPlaceCabinetBindService {
    
    List<MerchantPlaceCabinetBind> queryList(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    List<MerchantPlaceCabinetBindVo> queryListByMerchantId(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
}
