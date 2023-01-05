package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.wp.entity.AppTemplateQuery;
import com.xiliulou.core.wp.service.WeChatAppTemplateService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserCarMemberCardMapper;
import com.xiliulou.electricity.query.CarMemberCardExpiringSoonQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    TemplateConfigService templateConfigService;
    @Autowired
    WeChatAppTemplateService weChatAppTemplateService;
    @Autowired
    MemberCardFailureRecordService memberCardFailureRecordService;
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;

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

    @Override
    public UserCarMemberCard insertOrUpdate(UserCarMemberCard userCarMemberCard) {
        int insert = this.userCarMemberCardMapper.insertOrUpdate(userCarMemberCard);

        DbUtils.dbOperateSuccessThen(insert, () -> {
            redisService.delete(CacheConstant.CACHE_USER_CAR_MEMBERCARD + userCarMemberCard.getUid());
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

    /**
     * 抄的
     *
     * @See ElectricityMemberCardOrderServiceImpl#carMemberCardExpireReminder()
     */
    @Override
    public void carMemberCardExpireReminder() {
        if (!redisService.setNx(CacheConstant.CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LOCK, "ok", 120000L, false)) {
            log.warn("carMemberCardExpireReminder in execution...");
            return;
        }

        int offset = 0;
        int size = 300;
        Date date = new Date();
        long firstTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis() + 3 * 3600000 * 24;
        SimpleDateFormat simp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String firstTimeStr = redisService.get(CacheConstant.CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LAST_TIME);
        if (StrUtil.isNotBlank(firstTimeStr)) {
            firstTime = Long.parseLong(firstTimeStr);
        }

        redisService.set(CacheConstant.CACHE_ELE_CAR_MEMBER_CARD_EXPIRED_LAST_TIME, String.valueOf(lastTime));

        while (true) {

            List<CarMemberCardExpiringSoonQuery> carMemberCardExpiringList = this.carMemberCardExpire(offset, size, firstTime, lastTime);
            if (!DataUtil.collectionIsUsable(carMemberCardExpiringList)) {
                return;
            }

            carMemberCardExpiringList.parallelStream().forEach(item -> {
                ElectricityPayParams ele = electricityPayParamsService.queryFromCache(item.getTenantId());
                if (Objects.isNull(ele)) {
                    log.error("CAR MEMBER CARD EXPIRING SOON ERROR! ElectricityPayParams is null error! tenantId={}",
                            item.getTenantId());
                    return;
                }

                TemplateConfigEntity templateConfigEntity = templateConfigService.queryByTenantIdFromCache(item.getTenantId());
                if (Objects.isNull(templateConfigEntity) || Objects.isNull(templateConfigEntity.getBatteryOuttimeTemplate())) {
                    log.error("CAR MEMBER CARD EXPIRING SOON ERROR! templateConfigEntity is null error! tenantId={}",
                            item.getTenantId());
                    return;
                }

                date.setTime(item.getRentCarMemberCardExpireTime());

                item.setMerchantMinProAppId(ele.getMerchantMinProAppId());
                item.setMerchantMinProAppSecert(ele.getMerchantMinProAppSecert());
                item.setMemberCardExpiringTemplate(templateConfigEntity.getCarMemberCardExpiringTemplate());
                item.setRentCarMemberCardExpireTimeStr(simp.format(date));
                item.setCardName("租车套餐");
                sendCarMemberCardExpiringTemplate(item);
            });
            offset += size;
        }
    }

    @Override
    public List<FailureMemberCardVo> queryMemberCardExpireUser(int offset, int size, long nowTime) {
        return userCarMemberCardMapper.queryMemberCardExpireUser(offset, size, nowTime);
    }

    private List<CarMemberCardExpiringSoonQuery> carMemberCardExpire(Integer offset, Integer size, Long firstTime, Long lastTime) {
        return this.userCarMemberCardMapper.carMemberCardExpire(offset, size, firstTime, lastTime);
    }

    private void sendCarMemberCardExpiringTemplate(CarMemberCardExpiringSoonQuery carMemberCardExpiringSoonQuery) {
        AppTemplateQuery appTemplateQuery = new AppTemplateQuery();
        appTemplateQuery.setFormId(RandomUtil.randomString(20));
        appTemplateQuery.setTouser(carMemberCardExpiringSoonQuery.getThirdId());
        appTemplateQuery.setAppId(carMemberCardExpiringSoonQuery.getMerchantMinProAppId());
        appTemplateQuery.setSecret(carMemberCardExpiringSoonQuery.getMerchantMinProAppSecert());
        appTemplateQuery.setTemplateId(carMemberCardExpiringSoonQuery.getMemberCardExpiringTemplate());
        Map<String, Object> data = new HashMap<>(4);
        appTemplateQuery.setData(data);

        data.put("thing2", carMemberCardExpiringSoonQuery.getCardName());
        data.put("date4", carMemberCardExpiringSoonQuery.getRentCarMemberCardExpireTimeStr());
        data.put("thing3", "租车套餐即将过期，请及时续费!");

        log.info("CAR MEMBER CARD EXPIRING REMINDER: thirdId={}", carMemberCardExpiringSoonQuery.getThirdId());

        weChatAppTemplateService.sendWeChatAppTemplate(appTemplateQuery);
    }
}
