package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterRequest;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterUpdateRequest;
import com.xiliulou.electricity.request.userinfo.UserInfoLimitRequest;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (UserInfoExtra)表服务接口
 *
 * @author zzlong
 * @since 2024-02-18 10:39:59
 */
public interface UserInfoExtraService {
    
    UserInfoExtra queryByUidFromDB(Long uid);
    
    UserInfoExtra queryByUidFromCache(Long uid);
    
    UserInfoExtra insert(UserInfoExtra userInfoExtra);
    
    Integer updateByUid(UserInfoExtra userInfoExtra);
    
    Integer deleteByUid(Long uid);
    
    void bindMerchant(UserInfo userInfo, String orderId, Long memberCardId);
    
    MerchantInviterVO querySuccessInviter(Long uid);
    
    R selectInviterList(MerchantModifyInviterRequest request);
    
    R modifyInviter(MerchantModifyInviterUpdateRequest merchantModifyInviterUpdateRequest, Long operator, List<Long> franchiseeIds);
    
    MerchantInviterVO judgeInviterTypeForMerchant(Long joinUid, Long inviterUid, Integer tenantId);
    
    Triple<Boolean, String, String> isLimitPurchase(Long uid, Integer tenantId);
    
    R updateEleLimit(UserInfoLimitRequest request, List<Long> franchiseeIds);
    
    Triple<Boolean, String, Object> bindMerchantForLostUser(UserInfo userInfo, String orderId, Long memberCardId);
    
    Integer updateUserNotActivityByUid(Long uid);
}
