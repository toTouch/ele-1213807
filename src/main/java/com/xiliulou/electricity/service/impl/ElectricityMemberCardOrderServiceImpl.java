package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.query.MemberCardOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 10:54
 **/
@Service
@Slf4j
public class ElectricityMemberCardOrderServiceImpl extends ServiceImpl<ElectricityMemberCardOrderMapper, ElectricityMemberCardOrder> implements ElectricityMemberCardOrderService {

    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    UserService userService;
    @Autowired
    UserOauthBindService UserOauthBindService;

    /**
     * 创建月卡订单
     *
     * @param uid
     * @param memberId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createOrder(Long uid, Integer memberId, HttpServletRequest request) {
        ElectricityPayParams electricityPayParams = electricityPayParamsService.getElectricityPayParams();
        if (Objects.isNull(electricityPayParams)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND PAY_PARAMS");
            return R.failMsg("未配置支付参数!");
        }
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND SYS_USER UID:{}", uid);
            return R.failMsg("未找到系统用户信息!");
        }
        UserOauthBind userOauthBind = UserOauthBindService.queryUserOauthBySysId(uid);

        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USEROAUTHBIND OR THIRDID IS NULL  UID:{}", uid);
            return R.failMsg("未找到用户的第三方授权信息!");
        }
        UserInfo userInfo = userInfoService.selectUserByUid(uid);
        if (ObjectUtil.isNull(userInfo)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND USER_INFO. UID:{}", uid);
            return R.failMsg("未找到用户信息!");
        }
        if (!ObjectUtil.equal(UserInfo.IS_SERVICE_STATUS, userInfo.getServiceStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,THIS USER  NOT YET OPEN ELECTRICITY_SERVICE. UID:{}", uid);
            return R.failMsg("您还未开通换电服务!");
        }


        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.getElectricityMemberCard(memberId);
        if (Objects.isNull(electricityMemberCard)) {
            log.error("CREATE MEMBER_ORDER ERROR ,NOT FOUND MEMBER_CARD BY ID:{}", memberId);
            return R.failMsg("未找到月卡套餐!");
        }
        if (ObjectUtil.equal(ElectricityMemberCard.STATUS_UN_USEABLE, electricityMemberCard.getStatus())) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS UN_USABLE ID:{}", memberId);
            return R.failMsg("月卡已禁用!");
        }
        log.info("userInfo1 is -->{}",userInfo);
        if(Objects.nonNull(userInfo.getMemberCardExpireTime())){
            log.info("userInfo2 is -->{}",userInfo);
        }
        if(Objects.nonNull(userInfo.getRemainingNumber())){
            log.info("userInfo3 is -->{}",userInfo);
        }
        if( userInfo.getMemberCardExpireTime() > System.currentTimeMillis()){
            log.info("userInfo4 is -->{}",userInfo);
        }
        if(ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, userInfo.getRemainingNumber()) || userInfo.getRemainingNumber() > 0){
            log.info("userInfo5 is -->{}",userInfo);
        }

        if (Objects.nonNull(userInfo.getMemberCardExpireTime()) && Objects.nonNull(userInfo.getRemainingNumber()) &&
                userInfo.getMemberCardExpireTime() > System.currentTimeMillis() &&
                (ObjectUtil.equal(ElectricityMemberCard.UN_LIMITED_COUNT, userInfo.getRemainingNumber()) || userInfo.getRemainingNumber() > 0)) {
            log.error("CREATE MEMBER_ORDER ERROR ,MEMBER_CARD IS NOT EXPIRED USERINFO:{}", userInfo);
            return R.failMsg("您的月卡还未过期,无需再次购买!");
        }

        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(String.valueOf(System.currentTimeMillis()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(memberId);
        electricityMemberCardOrder.setUid(uid);
        electricityMemberCardOrder.setMaxUseCount(electricityMemberCard.getMaxUseCount());
        electricityMemberCardOrder.setMemberCardType(electricityMemberCard.getType());
        electricityMemberCardOrder.setPayAmount(electricityMemberCard.getHolidayPrice());
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(electricityMemberCard.getValidDays());
        baseMapper.insert(electricityMemberCardOrder);
        //支付零元
        if(electricityMemberCardOrder.getPayAmount().compareTo(BigDecimal.valueOf(0.01))<0){
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            Long memberCardExpireTime = System.currentTimeMillis() +
                    electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
            userInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
            userInfoUpdate.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
            userInfoUpdate.setCardType(electricityMemberCardOrder.getMemberCardType());
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateById(userInfoUpdate);
            ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
            electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
            electricityMemberCardOrderUpdate.setStatus(ElectricityMemberCardOrder.STATUS_SUCCESS);
            electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
            baseMapper.updateById(electricityMemberCardOrderUpdate);
            return R.ok();
        }
        Pair<Boolean, Object> getPayParamsPair =
                electricityTradeOrderService.createTradeOrderAndGetPayParams(electricityMemberCardOrder, electricityPayParams, userOauthBind.getThirdId(), request);
        if (!getPayParamsPair.getLeft()) {
            return R.failMsg(getPayParamsPair.getRight().toString());
        }
        return R.ok(getPayParamsPair.getRight());
    }

    @Override
    public BigDecimal homeOne(Long first, Long now) {
        return baseMapper.homeOne(first, now);
    }

    @Override
    public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay) {
        return baseMapper.homeThree(startTimeMilliDay, endTimeMilliDay);
    }


    @Override
    public R getMemberCardOrderPage(Long uid, Long offset, Long size, Long startTime, Long endTime) {
        return R.ok(baseMapper.getMemberCardOrderPage(uid, offset, size, startTime, endTime));
    }

    /**
     * 获取交易次数
     *
     * @param uid
     * @return
     */
    @Override
    public R getMemberCardOrderCount(Long uid, Long startTime, Long endTime) {
        return R.ok(baseMapper.getMemberCardOrderCount(uid, startTime, endTime));
    }

    /**
     * 获取最近的电池交易记录
     *
     * @param uid
     * @return
     */
    @Override
    public ElectricityMemberCardOrder getRecentOrder(Long uid) {
        return baseMapper.getRecentOrder(uid);
    }

    @Override
    @DS("slave_1")
    public R memberCardOrderPage(Long offset, Long size, MemberCardOrderQuery memberCardOrderQuery) {
        Page page = PageUtil.getPage(offset, size);

        return R.ok(baseMapper.memberCardOrderPage(page, offset, size, memberCardOrderQuery));
    }
}