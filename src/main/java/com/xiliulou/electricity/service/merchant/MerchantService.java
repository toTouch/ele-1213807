package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.dto.merchant.MerchantDeleteCacheDTO;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.query.merchant.MerchantJoinUserQueryMode;
import com.xiliulou.electricity.query.merchant.MerchantUnbindReq;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import com.xiliulou.electricity.vo.merchant.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:09
 * @desc
 */
public interface MerchantService {
    
    Triple<Boolean, String, Object> save(MerchantSaveRequest merchantSaveRequest);
    
    Triple<Boolean, String, Object> update(MerchantSaveRequest merchantSaveRequest);
    
    void deleteCache(MerchantDeleteCacheDTO merchantDeleteCacheDTO);
    
    Triple<Boolean, String, Object> remove(Long id, List<Long> franchiseeIds);
    
    Integer countTotal(MerchantPageRequest merchantPageRequest);
    
    List<MerchantVO> listByPage(MerchantPageRequest merchantPageRequest);
    
    Triple<Boolean, String, Object> queryById(Long id, List<Long> franchiseeIdList);
    
    Merchant queryByIdFromCache(Long id);
    
    Triple<Boolean, String, Object> queryByIdList(List<Long> idList);
    
    List<MerchantVO> queryList(MerchantPageRequest merchantPageRequest);
    
    Merchant queryByUid(Long uid);
    
    List<Merchant> queryByChannelEmployeeUid(Long channelEmployeeId);
    
    List<MerchantPlaceSelectVO> queryPlaceListByUid(Long merchantUid, Long employeeUid);
    
    Integer updateById(Merchant merchant);
    
    MerchantUserVO queryMerchantUserDetail();
    
    MerchantQrCodeVO getMerchantQrCode(Long uid, Long id);
    
    void deleteCacheById(Long id);
    
    Integer batchUpdateExistPlaceFee(List<Long> merchantIdList, Integer existsPlaceFeeYes, Long currentTimeMillis);
    
    List<Merchant> queryListByUidList(Set<Long> merchantUidList, Integer tenantId);
    
    List<Merchant> listAllByIds(List<Long> merchantIdList, Integer tenantId);
    
    void repairEnterprise(List<Long> enterpriseIds, List<Long> merchantIds, Integer tenantId);
    
    void deleteCacheForRepairEnterprise(List<Long> enterpriseIds, List<Long> merchantIds);
    
    Pair<Boolean, Object> unbindOpenId(MerchantUnbindReq params);
    
    List<Merchant> listByEnterpriseList(List<Long> enterpriseIdList);

    Integer countOverdueUserTotal(MerchantJoinUserQueryMode merchantJoinUserQueryMode);

    List<MerchantJoinUserVO> listOverdueUserByPage(MerchantJoinUserQueryMode merchantJoinUserQueryMode);

    List<Merchant> list(int offset, int size);
}
