package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserBatteryMemberCardPackageMapper;
import com.xiliulou.electricity.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (UserBatteryMemberCardPackage)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-12 14:44:01
 */
@Service("userBatteryMemberCardPackageService")
@Slf4j
public class UserBatteryMemberCardPackageServiceImpl implements UserBatteryMemberCardPackageService {
    @Resource
    private UserBatteryMemberCardPackageMapper userBatteryMemberCardPackageMapper;

    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private BatteryMemberCardService batteryMemberCardService;

    @Autowired
    private ElectricityMemberCardOrderService batteryMemberCardOrderService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserBatteryMemberCardPackage queryByIdFromDB(Long id) {
        return this.userBatteryMemberCardPackageMapper.queryById(id);
    }

    @Override
    public List<UserBatteryMemberCardPackage> selectByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.selectByUid(uid);
    }

    @Override
    public UserBatteryMemberCardPackage selectNearestByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.selectNearestByUid(uid);
    }

    @Override
    public Integer insert(UserBatteryMemberCardPackage userBatteryMemberCardPackage) {
        return userBatteryMemberCardPackageMapper.insert(userBatteryMemberCardPackage);
    }

    /**
     * 修改数据
     *
     * @param userBatteryMemberCardPackage 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserBatteryMemberCardPackage userBatteryMemberCardPackage) {
        return this.userBatteryMemberCardPackageMapper.update(userBatteryMemberCardPackage);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.userBatteryMemberCardPackageMapper.deleteById(id) > 0;
    }

    @Override
    public Integer deleteByOrderId(String orderId) {
        return this.userBatteryMemberCardPackageMapper.deleteByOrderId(orderId);
    }

    @Override
    public Integer deleteByUid(Long uid) {
        return this.userBatteryMemberCardPackageMapper.deleteByUid(uid);
    }

    @Override
    public UserBatteryMemberCardPackage selectByOrderNo(String orderId) {
        return this.userBatteryMemberCardPackageMapper.selectByOrderNo(orderId);
    }

    /**
     * 用户端懒加载处理换电套餐资源包转换
     */
    @Override
    public Triple<Boolean, String, Object> batteryMembercardTransform(Long uid) {

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("BATTERY MEMBER TRANSFORM ERROR! not found user,uid={}", uid);
            return Triple.of(true, null, null);
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! user is unUsable,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }

        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryMemberCard) || !Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! not found userBatteryMemberCard,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }

        if (!(userBatteryMemberCard.getOrderExpireTime() < System.currentTimeMillis() + 5 * 60 * 1000L || userBatteryMemberCard.getOrderRemainingNumber() <= 2)) {
            return Triple.of(true, null, null);
        }

        UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = this.selectNearestByUid(userBatteryMemberCard.getUid());
        if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
            log.warn("BATTERY MEMBER TRANSFORM WARN! not found userBatteryMemberCardPackageLatest,uid={}", userInfo.getUid());
            return Triple.of(true, null, null);
        }

        updateUserBatteryMembercardInfo(userBatteryMemberCard, userBatteryMemberCardPackageLatest);

        return Triple.of(true, null, null);
    }

    /**
     * 定时任务处理换电套餐资源包转换
     */
    @Override
    public void handlerTransferBatteryMemberCardPackage() {
        int offset = 0;
        int size = 200;

        while (true) {
            List<UserBatteryMemberCard> userBatteryMemberCardList = userBatteryMemberCardService.selectUseableList(offset, size);
            if (CollectionUtils.isEmpty(userBatteryMemberCardList)) {
                return;
            }

            userBatteryMemberCardList.parallelStream().forEach(item -> {
                if (!Objects.equals(item.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE)) {
                    return;
                }

                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getMemberCardId());
                if (Objects.isNull(batteryMemberCard)) {
                    return;
                }

                log.error("=====================11111{}",JsonUtil.toJson(batteryMemberCard));
                log.error("=====================2222{}", JsonUtil.toJson(item));
                if (!(item.getOrderExpireTime() < System.currentTimeMillis() + 5 * 60 * 1000L || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && item.getOrderRemainingNumber()<=1))) {
                    return;
                }

                UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest = this.selectNearestByUid(item.getUid());
                if (Objects.isNull(userBatteryMemberCardPackageLatest)) {
                    return;
                }

                updateUserBatteryMembercardInfo(item, userBatteryMemberCardPackageLatest);

            });

            offset += size;
        }
    }

    private void updateUserBatteryMembercardInfo(UserBatteryMemberCard userBatteryMemberCard, UserBatteryMemberCardPackage userBatteryMemberCardPackageLatest) {
        //更新当前用户绑定的套餐数据
        UserBatteryMemberCard userBatteryMemberCardUpdate = new UserBatteryMemberCard();
        userBatteryMemberCardUpdate.setUid(userBatteryMemberCard.getUid());
        userBatteryMemberCardUpdate.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        userBatteryMemberCardUpdate.setMemberCardId(userBatteryMemberCardPackageLatest.getMemberCardId());
        userBatteryMemberCardUpdate.setOrderEffectiveTime(System.currentTimeMillis());
        userBatteryMemberCardUpdate.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime() + userBatteryMemberCardPackageLatest.getMemberCardExpireTime());
        userBatteryMemberCardUpdate.setOrderRemainingNumber(userBatteryMemberCardPackageLatest.getRemainingNumber());
        userBatteryMemberCardService.updateByUid(userBatteryMemberCardUpdate);

        //删除资源包
        this.deleteByOrderId(userBatteryMemberCardPackageLatest.getOrderId());

        //更新原来绑定的套餐订单状态
        ElectricityMemberCardOrder oldMemberCardOrder = new ElectricityMemberCardOrder();
        oldMemberCardOrder.setOrderId(userBatteryMemberCard.getOrderId());
        oldMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USED);
        oldMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(oldMemberCardOrder);

        //更新新绑定的套餐订单的状态
        ElectricityMemberCardOrder currentMemberCardOrder = new ElectricityMemberCardOrder();
        currentMemberCardOrder.setOrderId(userBatteryMemberCardPackageLatest.getOrderId());
        currentMemberCardOrder.setUseStatus(ElectricityMemberCardOrder.USE_STATUS_USING);
        currentMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        batteryMemberCardOrderService.updateStatusByOrderNo(oldMemberCardOrder);
    }
}
