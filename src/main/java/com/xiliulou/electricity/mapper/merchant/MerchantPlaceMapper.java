package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:48
 * @desc
 */
public interface MerchantPlaceMapper {
    List<MerchantPlace> list(MerchantPlaceQueryModel placeQueryModel);
    
    Integer checkIsExists(MerchantPlaceQueryModel queryModel);
    
    Integer insert(MerchantPlace merchantPlace);
    
    Integer update(MerchantPlace merchantPlace);
    MerchantPlace selectById(@Param("id") Long id);
    Integer remove(MerchantPlace merchantPlaceDel);
    
    Integer countTotal(MerchantPlaceQueryModel merchantQueryModel);
    
    List<MerchantPlace> selectListByPage(MerchantPlaceQueryModel merchantQueryModel);
    
    List<MerchantPlaceCabinetVO> selectCabinetList(MerchantPlaceQueryModel queryModel);
    
    MerchantPlace selectHistoryById(@Param("id") Long id);
    
    List<MerchantPlace> selectByIdList(@Param("idList") List<Long> placeIdList,@Param("tenantId") Integer tenantId);
}
