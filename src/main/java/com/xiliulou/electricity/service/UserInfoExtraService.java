package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserInfoExtra;

/**
 * @ClassName: UserInfoExtraService
 * @description:
 * @author: renhang
 * @create: 2024-03-26 10:12
 */
public interface UserInfoExtraService {
    UserInfoExtra queryByUidFromDB(Long uid);
    
    UserInfoExtra queryByUidFromCache(Long uid);
    
    UserInfoExtra insert(UserInfoExtra userInfoExtra);
    
    Integer updateByUid(UserInfoExtra userInfoExtra);
    
    Integer deleteByUid(Long uid);
    
    void bindMerchant(Long uid, String orderId ,Long memberCardId);
}
