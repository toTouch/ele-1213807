package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardMapper;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (UserBatteryMemberCard)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-06 13:38:52
 */
@Service("userBatteryMemberCardService")
@Slf4j
public class UserBatteryMemberCardServiceImpl implements UserBatteryMemberCardService {
    
    @Autowired
    private UserBatteryMemberCardMapper userBatteryMemberCardMapper;
    
    @Autowired
    private RedisService redisService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCard selectByUidFromDB(Long uid) {
        return this.userBatteryMemberCardMapper.selectByUid(uid);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCard selectByUidFromCache(Long uid) {
        UserBatteryMemberCard cacheUserBatteryMemberCard = redisService.getWithHash(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid, UserBatteryMemberCard.class);
        if (Objects.nonNull(cacheUserBatteryMemberCard)) {
            return cacheUserBatteryMemberCard;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = this.selectByUidFromDB(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            return null;
        }
        
        redisService.saveWithHash(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid, userBatteryMemberCard);
        
        return userBatteryMemberCard;
    }
    
    /**
     * 新增数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBatteryMemberCard insert(UserBatteryMemberCard userBatteryMemberCard) {
        int insert = this.userBatteryMemberCardMapper.insertOne(userBatteryMemberCard);
        
        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid(),
                    userBatteryMemberCard);
            return null;
        });
        
        return userBatteryMemberCard;
    }
    
    /**
     * 修改数据
     *
     * @param userBatteryMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserBatteryMemberCard userBatteryMemberCard) {
        int update = this.userBatteryMemberCardMapper.updateByUid(userBatteryMemberCard);
        
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            return null;
        });
        
        return update;
    }
    
    /**
     * 通过主键删除数据
     *
     * @param uid 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteByUid(Long uid) {
        int delete = this.userBatteryMemberCardMapper.deleteByUid(uid);
        
        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid);
            return null;
        });
        
        return delete;
    }

    @Override
    public Integer minCount(Long id) {
        return userBatteryMemberCardMapper.minCount(id);
    }
}
