package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserCarMemberCard;
import com.xiliulou.electricity.mapper.UserCarMemberCardMapper;
import com.xiliulou.electricity.service.UserCarMemberCardService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * (UserCarMemberCard)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-07 17:36:11
 */
@Service("userCarMemberCardService")
@Slf4j
public class UserCarMemberCardServiceImpl implements UserCarMemberCardService {
    @Autowired
    private UserCarMemberCardMapper userCarMemberCardMapper;
    @Autowired
    private RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserCarMemberCard selectByUidFromDB(Long uid) {
        return this.userCarMemberCardMapper.selectByUid(uid);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserCarMemberCard selectByUidFromCache(Long uid) {
        UserCarMemberCard cacheUserCarMemberCard = redisService.getWithHash(CacheConstant.CACHE_USER_CAR_MEMBERCARD + uid, UserCarMemberCard.class);
        if (Objects.nonNull(cacheUserCarMemberCard)) {
            return cacheUserCarMemberCard;
        }

        UserCarMemberCard userCarMemberCard = this.selectByUidFromDB(uid);
        if (Objects.isNull(userCarMemberCard)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_CAR_MEMBERCARD + uid, userCarMemberCard);

        return userCarMemberCard;
    }

    /**
     * 新增数据
     *
     * @param userCarMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCarMemberCard insert(UserCarMemberCard userCarMemberCard) {
        int insert = this.userCarMemberCardMapper.insertOne(userCarMemberCard);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_CAR_MEMBERCARD + userCarMemberCard.getUid(), userCarMemberCard);
            return null;
        });

        return userCarMemberCard;
    }

    /**
     * 修改数据
     *
     * @param userCarMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserCarMemberCard userCarMemberCard) {
        int update = this.userCarMemberCardMapper.updateByUid(userCarMemberCard);

        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_CAR_MEMBERCARD + userCarMemberCard.getUid());
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
        int delete = this.userCarMemberCardMapper.deleteByUid(uid);

        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_CAR_MEMBERCARD + uid);
            return null;
        });

        return delete;
    }
}
