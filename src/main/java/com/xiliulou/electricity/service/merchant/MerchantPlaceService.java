package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPlacePageRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceSaveRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:46
 * @desc
 */
public interface MerchantPlaceService {
    
    Integer existsByAreaId(Long areaId);
    List<MerchantPlace> queryList(MerchantPlaceQueryModel placeQueryModel);
    
    Triple<Boolean, String, Object> save(MerchantPlaceSaveRequest merchantPlaceSaveRequest);
    
    Triple<Boolean, String, Object> update(MerchantPlaceSaveRequest merchantPlaceSaveRequest);
    
    void deleteCache(MerchantPlace merchantPlace);
    
    Triple<Boolean, String, Object> remove(Long id);
    
    Integer countTotal(MerchantPlacePageRequest merchantPlacePageRequest);
    
    List<MerchantPlaceVO> listByPage(MerchantPlacePageRequest merchantPlacePageRequest);
    
    MerchantPlace queryFromCacheById(Long placeId);
    
    Triple<Boolean, String, Object> getCabinetList(MerchantPlacePageRequest merchantPlacePageRequest);
    
    List<MerchantPlaceVO> queryPlaceList(MerchantPlacePageRequest merchantPlacePageRequest);
    
    Triple<Boolean, String, Object> queryById(Long id);
}
