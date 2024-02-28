package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.query.merchant.MerchantPlaceMapQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceMapVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceSelectVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 17:00
 * @desc
 */
public interface MerchantPlaceMapMapper {
    
    int batchInsert(@Param("list") List<MerchantPlaceMap> merchantPlaceMapList);
    
    List<MerchantPlaceMap> list(MerchantPlaceMapQueryModel queryModel);
    
    int batchDeleteByMerchantId(@Param("merchantId") Long merchantId, @Param("placeIdList") Set<Long> placeIdList);
    
    List<MerchantPlaceSelectVO> selectListByMerchantId(@Param("merchantId")Long merchantId);
    
    List<MerchantPlaceMap> selectBindList(@Param("notMerchantId") Long notMerchantId,@Param("franchiseeId") Long franchiseeId);
    
    List<MerchantPlaceMapVO> selectBindMerchantName(MerchantPlaceMapQueryModel placeMapQueryModel);
    
    List<MerchantPlaceMapVO> countByMerchantIdList(MerchantPlaceMapQueryModel placeMapQueryModel);
    
    List<Long> selectCabinetIdsByMerchantId(@Param("merchantId") Long merchantId);
}
