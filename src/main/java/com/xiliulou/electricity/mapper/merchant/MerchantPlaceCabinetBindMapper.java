package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVo;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 9:54
 * @desc
 */
public interface MerchantPlaceCabinetBindMapper {
    
    List<MerchantPlaceCabinetBind> list(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
    
    List<MerchantPlaceCabinetBindVo> listByMerchantId(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel);
}
