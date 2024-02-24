package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.dto.merchant.MerchantDeleteCacheDTO;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeMerchantNumQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUserVO;
import com.xiliulou.electricity.vo.merchant.MerchantVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:09
 * @desc
 */
public interface MerchantService {
    
    Triple<Boolean, String, Object> save(MerchantSaveRequest merchantSaveRequest);
    
    Triple<Boolean, String, Object> update(MerchantSaveRequest merchantSaveRequest);
    
    void deleteCache(MerchantDeleteCacheDTO merchantDeleteCacheDTO);
    
    Triple<Boolean, String, Object> remove(Long id);
    
    Integer countTotal(MerchantPageRequest merchantPageRequest);
    
    List<MerchantVO> listByPage(MerchantPageRequest merchantPageRequest);
    
    Triple<Boolean, String, Object> queryById(Long id);
    
    Merchant queryFromCacheById(Long id);
    
    Triple<Boolean, String, Object> queryByIdList(List<Long> idList);
    
    List<MerchantVO> getDict(MerchantPageRequest merchantPageRequest);
    
    Merchant queryByUid(Long uid);
    
    List<MerchantPlaceUserVO> queryPlaceListByUid(Long uid, Long merchantEmployeeUid);
    
    Integer updateById(Merchant merchant);
    
    MerchantVO queryMerchantDetail();
    
    Integer countMerchantNumByTime(MerchantPromotionFeeMerchantNumQueryModel todayQueryModel);
}
