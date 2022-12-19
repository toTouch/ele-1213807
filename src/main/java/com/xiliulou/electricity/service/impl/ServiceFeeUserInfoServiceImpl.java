package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.CityMapper;
import com.xiliulou.electricity.mapper.ServiceFeeUserInfoMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 用户停卡绑定(TServiceFeeUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Service("serviceFeeUserInfoService")
@Slf4j
public class ServiceFeeUserInfoServiceImpl implements ServiceFeeUserInfoService {

    @Resource
    ServiceFeeUserInfoMapper serviceFeeUserInfoMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    UserInfoService userInfoService;

    @Override
    public int insert(ServiceFeeUserInfo serviceFeeUserInfo) {
        return serviceFeeUserInfoMapper.insert(serviceFeeUserInfo);
    }

    @Override
    public int update(ServiceFeeUserInfo serviceFeeUserInfo) {
        return serviceFeeUserInfoMapper.update(serviceFeeUserInfo);
    }

    @Override
    public ServiceFeeUserInfo queryByUidFromCache(Long uid) {
        ServiceFeeUserInfo cache = redisService.getWithHash(CacheConstant.SERVICE_FEE_USER_INFO + uid, ServiceFeeUserInfo.class);
        if (Objects.nonNull(cache)) {
            return cache;
        }

        ServiceFeeUserInfo serviceFeeUserInfo = serviceFeeUserInfoMapper.selectOne(new LambdaQueryWrapper<ServiceFeeUserInfo>().eq(ServiceFeeUserInfo::getUid, uid).eq(ServiceFeeUserInfo::getDelFlag, ServiceFeeUserInfo.DEL_NORMAL));
        if (Objects.isNull(serviceFeeUserInfo)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.SERVICE_FEE_USER_INFO + uid, serviceFeeUserInfo);
        return serviceFeeUserInfo;
    }

    @Override
    public void updateByUid(ServiceFeeUserInfo serviceFeeUserInfo) {

        int update = serviceFeeUserInfoMapper.updateByUid(serviceFeeUserInfo);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //更新缓存
            redisService.delete(CacheConstant.SERVICE_FEE_USER_INFO + serviceFeeUserInfo.getUid());
            return null;
        });
        return;
    }


    @Override
    public EleBatteryServiceFeeVO queryUserBatteryServiceFee(Long uid) {

        //获取新用户所绑定的加盟商的电池服务费
        Franchisee franchisee = franchiseeService.queryByUserId(uid);
        EleBatteryServiceFeeVO eleBatteryServiceFeeVO = new EleBatteryServiceFeeVO();
        //计算用户所产生的电池服务费
        if (Objects.isNull(franchisee)) {
            return eleBatteryServiceFeeVO;
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELE ERROR! not found user,uid={}", uid);
            return eleBatteryServiceFeeVO;
        }

        Integer modelType = franchisee.getModelType();
        if (Objects.equals(modelType, Franchisee.OLD_MODEL_TYPE) && Objects.equals(franchisee.getBatteryServiceFee(), BigDecimal.valueOf(0))) {
            return eleBatteryServiceFeeVO;
        }

        eleBatteryServiceFeeVO.setBatteryServiceFee(franchisee.getBatteryServiceFee());

        eleBatteryServiceFeeVO.setModelType(franchisee.getModelType());

        ServiceFeeUserInfo serviceFeeUserInfo = queryByUidFromCache(uid);

        BigDecimal userChangeServiceFee = BigDecimal.valueOf(0);
        Long now = System.currentTimeMillis();
        long cardDays = 0;
        //用户产生的套餐过期电池服务费
        if (Objects.nonNull(serviceFeeUserInfo) && Objects.nonNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
            //查询用户是否存在套餐过期电池服务费
            userChangeServiceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, franchisee, cardDays);
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);

        Integer memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_NOT_DISABLE;

        //用户产生的停卡电池服务费
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {
                cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
                //不足一天按一天计算
                double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
                if (time < 24) {
                    cardDays = 1;
                }
                userChangeServiceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, uid, cardDays, null, serviceFeeUserInfo);
                memberCardStatus = UserBatteryMemberCard.MEMBER_CARD_DISABLE;
            }
        }
        eleBatteryServiceFeeVO.setMemberCardStatus(memberCardStatus);
        eleBatteryServiceFeeVO.setUserBatteryServiceFee(userChangeServiceFee);

        return eleBatteryServiceFeeVO;
    }


}
