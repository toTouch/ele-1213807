package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:46
 * @desc
 */
public interface MerchantPlaceService {
    List<MerchantPlace> queryList(MerchantPlaceQueryModel placeQueryModel);
}
