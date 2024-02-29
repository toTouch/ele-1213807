package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.query.merchant.MerchantPlaceMapQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceMapVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceSelectVO;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 16:55
 * @desc
 */
public interface MerchantPlaceMapService {
    int batchInsert(List<MerchantPlaceMap> merchantPlaceMapList);
    
    List<MerchantPlaceMap> queryList(MerchantPlaceMapQueryModel queryModel);
    
    int batchDeleteByMerchantId(Long id, Set<Long> placeIdList);
    
    List<MerchantPlaceSelectVO> queryListByMerchantId(Long merchantId);
    
    List<MerchantPlaceMap> queryBindList(Long notMerchantId, Long franchiseeId);
    
    List<MerchantPlaceMapVO> queryBindMerchantName(MerchantPlaceMapQueryModel placeMapQueryModel);
    
    List<MerchantPlaceMapVO> countByMerchantIdList(MerchantPlaceMapQueryModel placeMapQueryModel);
    
    Integer countCabinetNumByMerchantId(Long merchantId);
    
    Integer existsPlaceFee(Long merchantId);
}
