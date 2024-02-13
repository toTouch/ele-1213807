package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:48
 * @desc
 */
public interface MerchantPlaceMapper {
    List<MerchantPlace> list(MerchantPlaceQueryModel placeQueryModel);
}
