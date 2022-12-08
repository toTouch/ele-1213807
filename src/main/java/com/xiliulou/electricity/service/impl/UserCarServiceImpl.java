package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.mapper.UserCarMapper;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * (UserCar)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-07 17:35:15
 */
@Service("userCarService")
@Slf4j
public class UserCarServiceImpl implements UserCarService {
    @Autowired
    private UserCarMapper userCarMapper;
    @Autowired
    private RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserCar selectByUidFromDB(Long uid) {
        return this.userCarMapper.selectByUid(uid);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserCar selectByUidFromCache(Long uid) {
        UserCar cacheUserCar = redisService.getWithHash(CacheConstant.CACHE_USER_CAR + uid, UserCar.class);
        if (Objects.nonNull(cacheUserCar)) {
            return cacheUserCar;
        }

        UserCar userCar = this.selectByUidFromDB(uid);
        if (Objects.isNull(userCar)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_CAR + uid, userCar);

        return userCar;
    }

    /**
     * 新增数据
     *
     * @param userCar 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCar insert(UserCar userCar) {
        int insert = this.userCarMapper.insertOne(userCar);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_CAR + userCar.getUid(), userCar);
            return null;
        });

        return userCar;
    }

    /**
     * 修改数据
     *
     * @param userCar 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserCar userCar) {
        int update = this.userCarMapper.updateByUid(userCar);
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_CAR + userCar.getUid());
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
        int delete = this.userCarMapper.deleteByUid(uid);

        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_CAR + uid);
            return null;
        });

        return delete;
    }
}
