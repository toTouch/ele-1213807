package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardMapper;
import com.xiliulou.electricity.query.BatteryMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
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

    @Resource
    private UserBatteryMemberCardMapper userBatteryMemberCardMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    MemberCardFailureRecordService memberCardFailureRecordService;

    @Autowired
    UserBatteryDepositService userBatteryDepositService;

    @Autowired
    UserBatteryService userBatteryService;

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

    @Override
    public UserBatteryMemberCard insertOrUpdate(UserBatteryMemberCard userBatteryMemberCard) {
        int insert = this.userBatteryMemberCardMapper.insertOrUpdate(userBatteryMemberCard);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
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

        saveMemberCardFailureRecord(uid);

        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + uid);
            return null;
        });

        return delete;
    }


    //处理失效套餐
    public void saveMemberCardFailureRecord(Long uid) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found user,uid={}", uid);
            return;
        }


        UserBatteryMemberCard userBatteryMemberCard = this.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found userCarMemberCard,uid={}", uid);
            return;
        }

        //若套餐已过期  不添加记录
        if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            return;
        }


        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            log.warn("ELE FAILURE CAR MEMBERCARD WARN! memberCard is typeCount,uid={}", uid);
            return;
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.queryLastPayMemberCardTimeByUid(uid, userInfo.getFranchiseeId(), userInfo.getTenantId());


        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found userCarDeposit,uid={}", uid);
            return;
        }

        UserBattery userBattery = userBatteryService.selectByUidFromCache(uid);

        MemberCardFailureRecord memberCardFailureRecord = new MemberCardFailureRecord();
        memberCardFailureRecord.setUid(userInfo.getUid());
        memberCardFailureRecord.setCardName(electricityMemberCardOrder.getCardName());
        memberCardFailureRecord.setDeposit(userBatteryDeposit.getBatteryDeposit());
        memberCardFailureRecord.setCarMemberCardOrderId(electricityMemberCardOrder.getOrderId());
        memberCardFailureRecord.setMemberCardExpireTime(System.currentTimeMillis());
        memberCardFailureRecord.setType(MemberCardFailureRecord.FAILURE_TYPE_FOR_BATTERY);
        memberCardFailureRecord.setBatteryType(Objects.isNull(userBattery) ? "" : userBattery.getBatteryType());
        memberCardFailureRecord.setTenantId(userInfo.getTenantId());
        memberCardFailureRecord.setCreateTime(System.currentTimeMillis());
        memberCardFailureRecord.setUpdateTime(System.currentTimeMillis());

        memberCardFailureRecordService.insert(memberCardFailureRecord);
    }

    @Override
    public Integer minCount(UserBatteryMemberCard userBatteryMemberCard) {

        Integer update = userBatteryMemberCardMapper.minCount(userBatteryMemberCard.getId());


        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            return null;
        });

        return update;

    }

    @Override
    public Integer minCountForOffLineEle(UserBatteryMemberCard userBatteryMemberCard) {
        Integer update = userBatteryMemberCardMapper.minCountForOffLineEle(userBatteryMemberCard.getId());
        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            return null;
        });

        return update;
    }

    @Override
    public Integer plusCount(Long id) {
        return userBatteryMemberCardMapper.plusCount(id);
    }

    @Override
    public Integer updateByUidForDisableCard(UserBatteryMemberCard userBatteryMemberCard) {
        int update = this.userBatteryMemberCardMapper.updateByUidForDisableCard(userBatteryMemberCard);

        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_USER_BATTERY_MEMBERCARD + userBatteryMemberCard.getUid());
            return null;
        });

        return update;
    }

    @Override
    public List<UserBatteryMemberCard> selectByMemberCardId(Integer id, Integer tenantId) {
        return userBatteryMemberCardMapper.selectList(new LambdaQueryWrapper<UserBatteryMemberCard>().eq(UserBatteryMemberCard::getMemberCardId, id).eq(UserBatteryMemberCard::getTenantId, tenantId)
                .eq(UserBatteryMemberCard::getDelFlag, UserBatteryMemberCard.DEL_NORMAL));
    }

    @Override
    public List<BatteryMemberCardExpiringSoonQuery> batteryMemberCardExpire(Integer offset, Integer size,
                                                                            Long firstTime, Long lastTime) {
        return userBatteryMemberCardMapper.batteryMemberCardExpire(offset, size, firstTime, lastTime);
    }


    @Override
    public List<CarMemberCardExpiringSoonQuery> carMemberCardExpire(Integer offset, Integer size, Long firstTime,
                                                                    Long lastTime) {
        return userBatteryMemberCardMapper.carMemberCardExpire(offset, size, firstTime, lastTime);
    }

    @Override
    public List<FailureMemberCardVo> queryMemberCardExpireUser(Integer offset, Integer size, Long nowTime) {
        return userBatteryMemberCardMapper.queryMemberCardExpireUser(offset, size, nowTime);
    }
}
