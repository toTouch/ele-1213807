package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.query.UserCarQuery;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.vo.UserCarVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-06-19-16:06
 */
@Service
public class UserCarServiceImpl implements UserCarService {
    
    @Override
    public UserCar selectByUidFromDB(Long uid) {
        return null;
    }
    
    @Override
    public UserCar selectByUidFromCache(Long uid) {
        return null;
    }
    
    @Override
    public UserCar insert(UserCar userCar) {
        return null;
    }
    
    @Override
    public UserCar insertOrUpdate(UserCar userCar) {
        return null;
    }
    
    @Override
    public Integer updateByUid(UserCar userCar) {
        return null;
    }
    
    @Override
    public Integer unBindingCarByUid(UserCar userCar) {
        return null;
    }
    
    @Override
    public Integer deleteByUid(Long uid) {
        return null;
    }
    
    @Override
    public UserCarVO selectDetailByUid(Long uid) {
        return null;
    }
    
    @Override
    public List<UserCar> selectByQuery(UserCarQuery userCarQuery) {
        return null;
    }
}
