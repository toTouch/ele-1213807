package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.mapper.UserCarDepositMapper;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * (UserCarDeposit)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-07 17:35:46
 */
@Service("userCarDepositService")
@Slf4j
public class UserCarDepositServiceImpl implements UserCarDepositService {
    @Autowired
    private UserCarDepositMapper userCarDepositMapper;
    @Autowired
    private RedisService redisService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserCarDeposit selectByUidFromDB(Long uid) {
        return this.userCarDepositMapper.selectByUid(uid);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserCarDeposit selectByUidFromCache(Long uid) {
        UserCarDeposit cacheUserCarDeposit = redisService.getWithHash(CacheConstant.CACHE_USER_CAR_DEPOSIT + uid, UserCarDeposit.class);
        if (Objects.nonNull(cacheUserCarDeposit)) {
            return cacheUserCarDeposit;
        }

        UserCarDeposit userCarDeposit = this.selectByUidFromDB(uid);
        if (Objects.isNull(userCarDeposit)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_CAR_DEPOSIT + uid, userCarDeposit);

        return userCarDeposit;
    }


    /**
     * 新增数据
     *
     * @param userCarDeposit 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCarDeposit insert(UserCarDeposit userCarDeposit) {
        int insert = this.userCarDepositMapper.insertOne(userCarDeposit);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.saveWithHash(CacheConstant.CACHE_USER_CAR_DEPOSIT + userCarDeposit.getUid(), userCarDeposit);
            return null;
        });

        return userCarDeposit;
    }

    /**
     * 修改数据
     *
     * @param userCarDeposit 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateByUid(UserCarDeposit userCarDeposit) {
        int update = this.userCarDepositMapper.updateByUid(userCarDeposit);

        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_CAR_DEPOSIT + userCarDeposit.getUid());
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
        int delete = this.userCarDepositMapper.deleteByUid(uid);

        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_CAR_DEPOSIT + uid);
            return null;
        });

        return delete;
    }
}
