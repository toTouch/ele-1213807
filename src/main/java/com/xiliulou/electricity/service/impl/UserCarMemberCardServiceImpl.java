package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.UserCarMemberCard;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-06-19-16:07
 */
@Service
public class UserCarMemberCardServiceImpl implements UserCarMemberCardService {
    
    @Override
    public UserCarMemberCard selectByUidFromDB(Long uid) {
        return null;
    }
    
    @Override
    public UserCarMemberCard selectByUidFromCache(Long uid) {
        return null;
    }
    
    @Override
    public UserCarMemberCard insert(UserCarMemberCard userCarMemberCard) {
        return null;
    }
    
    @Override
    public UserCarMemberCard insertOrUpdate(UserCarMemberCard userCarMemberCard) {
        return null;
    }
    
    @Override
    public Integer updateByUid(UserCarMemberCard userCarMemberCard) {
        return null;
    }
    
    @Override
    public Integer deleteByUid(Long uid) {
        return null;
    }
    
    @Override
    public void carMemberCardExpireReminder() {
    
    }
    
    @Override
    public List<FailureMemberCardVo> queryMemberCardExpireUser(int offset, int size, long nowTime) {
        return null;
    }
    
    @Override
    public List<CarMemberCardExpiringSoonQuery> selectCarMemberCardExpire(int offset, int size, long firstTime, long lastTime) {
        return null;
    }
    
    @Override
    public void expireBreakPowerHandel() {
    
    }
}
