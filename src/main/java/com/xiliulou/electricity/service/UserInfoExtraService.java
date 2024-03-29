package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterRequest;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;

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
    
    void bindMerchant(Long uid, String orderId ,Long memberCardId);
    
    MerchantInviterVO querySuccessInviter(Long uid, Integer tenantId);
    
    R selectInviterList(Long uid, Long size, Long offset);
    
    R modifyInviter(MerchantModifyInviterRequest merchantModifyInviterRequest, Long operator);
}
