package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.UserExtra;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserExtraMapper;
import com.xiliulou.electricity.query.UpdateUserSourceQuery;
import com.xiliulou.electricity.query.UserSourceQuery;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserExtraService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (UserExtra)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-03 15:08:23
 */
@Service("userExtraService")
@Slf4j
public class UserExtraServiceImpl implements UserExtraService {
    @Resource
    private UserExtraMapper userExtraMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ElectricityCabinetService electricityCabinetService;
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserExtra queryByIdFromDB(Long uid) {
        return this.userExtraMapper.queryById(uid);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param uid 主键
     * @return 实例对象
     */
    @Override
    public UserExtra queryByIdFromCache(Long uid) {

        UserExtra cacheUserExtra = redisService.getWithHash(CacheConstant.CACHE_USER_EXTRA + uid, UserExtra.class);
        if (Objects.nonNull(cacheUserExtra)) {
            return cacheUserExtra;
        }

        UserExtra userExtra = this.queryByIdFromDB(uid);
        if (Objects.isNull(userExtra)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_USER_EXTRA + uid, userExtra);
        return userExtra;
    }

    /**
     * 新增数据
     *
     * @param userExtra 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserExtra insert(UserExtra userExtra) {
        this.userExtraMapper.insertOne(userExtra);
        return userExtra;
    }

    /**
     * 修改数据
     *
     * @param userExtra 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserExtra userExtra) {
        int update = this.userExtraMapper.update(userExtra);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_USER_EXTRA + userExtra.getUid());
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
    public Integer deleteById(Long uid) {
        int delete = this.userExtraMapper.deleteById(uid);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_USER_EXTRA + uid);
        });

        return delete;
    }

    @Override
    public void loginCallBack(UserSourceQuery query) {

        UserExtra userExtra = this.queryByIdFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userExtra)) {
            log.error("USER SOURCE ERROR! not found user,uid={}", SecurityUtils.getUid());
            return;
        }

        if (!Objects.equals(userExtra.getSource(), NumberConstant.MINUS_ONE)) {
            return;
        }

        Long storeId = null;

        UserExtra userExtraUpdate = new UserExtra();
        userExtraUpdate.setUid(userExtra.getUid());
        userExtraUpdate.setSource(query.getSource());
        userExtraUpdate.setUpdateTime(System.currentTimeMillis());
        //扫码
        if (Objects.equals(query.getSource(), UserExtra.SOURCE_TYPE_SCAN)) {
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
            if (Objects.nonNull(electricityCabinet)) {
                userExtraUpdate.setEid(electricityCabinet.getId().longValue());
                storeId = electricityCabinet.getStoreId();
            } else {
                log.warn("USER SOURCE WARN! not found electricityCabinet,p={},d={},uid={}", query.getProductKey(), query.getDeviceName(), SecurityUtils.getUid());
            }
        }

        //邀请
        if (Objects.equals(query.getSource(), UserExtra.SOURCE_TYPE_INVITE)) {
            userExtraUpdate.setInviter(query.getInviter());
            UserInfo inviterUserInfo = userInfoService.queryByUidFromCache(query.getInviter());
            storeId = Objects.nonNull(inviterUserInfo) ? inviterUserInfo.getStoreId() : null;
        }

        this.update(userExtraUpdate);

        if (Objects.nonNull(storeId)) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUid(userExtra.getUid());
            userInfo.setStoreId(storeId);
            userInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfo);
        }

//        if (Objects.isNull(userExtra)) {
//            UserExtra userExtraInsert = new UserExtra();
//            userExtraInsert.setUid(SecurityUtils.getUid());
//            userExtraInsert.setSource(query.getSource());
//            userExtraInsert.setUpdateTime(System.currentTimeMillis());
//            userExtraInsert.setCreateTime(System.currentTimeMillis());
//            userExtraInsert.setTenantId(TenantContextHolder.getTenantId());
//
//            if (Objects.equals(query.getSource(), UserExtra.SOURCE_TYPE_SCAN) && StringUtils.isNotBlank(query.getProductKey()) && StringUtils.isNotBlank(query.getDeviceName())) {
//                ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
//                if (Objects.nonNull(electricityCabinet)) {
//                    userExtraInsert.setEid(electricityCabinet.getId().longValue());
//                } else {
//                    log.warn("USER SOURCE WARN! not found electricityCabinet,p={},d={},uid={}", query.getProductKey(), query.getDeviceName(), SecurityUtils.getUid());
//                }
//            }
//
//            this.insert(userExtraInsert);
//            return;
//        }
//
//        if (!Objects.equals(userExtra.getSource(), NumberConstant.ZERO)) {
//            return;
//        }
//
//        UserExtra userExtraUpdate = new UserExtra();
//        userExtraUpdate.setUid(userExtra.getUid());
//        userExtraUpdate.setSource(query.getSource());
//        userExtraUpdate.setUpdateTime(System.currentTimeMillis());
//        if (Objects.equals(query.getSource(), UserExtra.SOURCE_TYPE_SCAN) && StringUtils.isNotBlank(query.getProductKey()) && StringUtils.isNotBlank(query.getDeviceName())) {
//            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
//            if (Objects.nonNull(electricityCabinet)) {
//                userExtraUpdate.setEid(electricityCabinet.getId().longValue());
//            } else {
//                log.warn("USER SOURCE WARN! not found electricityCabinet,p={},d={},uid={}", query.getProductKey(), query.getDeviceName(), SecurityUtils.getUid());
//            }
//        }
//
//        this.update(userExtraUpdate);
    }

    @Override
    public Triple<Boolean, String, Object> updateUserSource(UpdateUserSourceQuery userSourceQuery) {
        UserExtra userExtra = this.queryByIdFromCache(userSourceQuery.getUid());
        if (Objects.isNull(userExtra) || !Objects.equals(userExtra.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "", "用户不存在");
        }

        UserExtra userExtraUpdate = new UserExtra();
        userExtraUpdate.setUid(userExtra.getUid());
        userExtraUpdate.setSource(userSourceQuery.getSource());
        userExtraUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(userExtraUpdate);

        return Triple.of(true, null, null);
    }
}
