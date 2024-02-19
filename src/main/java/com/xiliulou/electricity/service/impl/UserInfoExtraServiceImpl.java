package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.mapper.UserInfoExtraMapper;
import com.xiliulou.electricity.service.UserInfoExtraService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (UserInfoExtra)表服务实现类
 *
 * @author zzlong
 * @since 2024-02-18 10:39:59
 */
@Service("userInfoExtraService")
@Slf4j
public class UserInfoExtraServiceImpl implements UserInfoExtraService {
    
    @Resource
    private UserInfoExtraMapper userInfoExtraMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Override
    public UserInfoExtra queryByUidFromDB(Long uid) {
        return this.userInfoExtraMapper.selectByUid(uid);
    }
    
    @Override
    public UserInfoExtra queryByUidFromCache(Long uid) {
        UserInfoExtra cacheUserInfoExtra = redisService.getWithHash(CacheConstant.CACHE_USER_INFO_EXTRA + uid, UserInfoExtra.class);
        if (Objects.nonNull(cacheUserInfoExtra)) {
            return cacheUserInfoExtra;
        }
        
        UserInfoExtra userInfoExtra = this.queryByUidFromDB(uid);
        if (Objects.isNull(userInfoExtra)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_USER_INFO_EXTRA + uid, userInfoExtra);
        
        return userInfoExtra;
    }
    
    @Override
    public UserInfoExtra insert(UserInfoExtra userInfoExtra) {
        this.userInfoExtraMapper.insert(userInfoExtra);
        return userInfoExtra;
    }
    
    @Override
    public Integer updateByUid(UserInfoExtra userInfoExtra) {
        int update = this.userInfoExtraMapper.updateByUid(userInfoExtra);
        
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO_EXTRA + userInfoExtra.getUid());
        });
        
        return update;
    }
    
    @Override
    public Integer deleteByUid(Long uid) {
        int delete = this.userInfoExtraMapper.deleteByUid(uid);
        
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_USER_INFO_EXTRA + uid);
        });
        
        return delete;
    }
}
