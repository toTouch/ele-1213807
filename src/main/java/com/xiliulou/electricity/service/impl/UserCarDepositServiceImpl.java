package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.service.UserCarDepositService;
import org.springframework.stereotype.Service;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-06-19-16:08
 */
@Service
public class UserCarDepositServiceImpl implements UserCarDepositService {
    
    @Override
    public UserCarDeposit selectByUidFromDB(Long uid) {
        return null;
    }
    
    @Override
    public UserCarDeposit selectByUidFromCache(Long uid) {
        return null;
    }
    
    @Override
    public UserCarDeposit insert(UserCarDeposit userCarDeposit) {
        return null;
    }
    
    @Override
    public UserCarDeposit insertOrUpdate(UserCarDeposit userCarDeposit) {
        return null;
    }
    
    @Override
    public Integer updateByUid(UserCarDeposit userCarDeposit) {
        return null;
    }
    
    @Override
    public Integer deleteByUid(Long uid) {
        return null;
    }
    
    @Override
    public Integer logicDeleteByUid(Long uid) {
        return null;
    }
}
