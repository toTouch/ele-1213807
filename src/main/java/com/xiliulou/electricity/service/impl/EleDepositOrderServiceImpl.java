package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Service("eleDepositOrderService")
@Slf4j
public class EleDepositOrderServiceImpl implements EleDepositOrderService {
    @Resource
    EleDepositOrderMapper eleDepositOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserOauthBindService userOauthBindService;
    @Autowired
    EleRefundOrderService eleRefundOrderService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleDepositOrder queryByIdFromDB(Long id) {
        return this.eleDepositOrderMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleDepositOrder queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 新增数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleDepositOrder insert(EleDepositOrder eleDepositOrder) {
        this.eleDepositOrderMapper.insert(eleDepositOrder);
        return eleDepositOrder;
    }

    /**
     * 修改数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleDepositOrder eleDepositOrder) {
        return this.eleDepositOrderMapper.update(eleDepositOrder);

    }

    @Override
    public EleDepositOrder queryByOrderId(String orderNo) {
        return eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R payDeposit(HttpServletRequest request) {
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //限频
        Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断是否实名认证
        UserInfo userInfo = userInfoService.queryByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_AUTH)) {
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //计算押金
        //根据用户cid找到对应的加盟商
        Franchisee franchisee = franchiseeService.queryByCid(user.getCid());
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ", user.getCid());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        BigDecimal payAmount = franchisee.getBatteryDeposit();
        String orderId = generateOrderId(uid);

        //生成订单
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                .orderId(orderId)
                .uid(user.getUid())
                .phone(userInfo.getPhone())
                .userName(user.getName())
                .payAmount(payAmount)
                .status(EleDepositOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();

        //支付零元
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
            eleDepositOrderMapper.insert(eleDepositOrder);
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            userInfoUpdate.setServiceStatus(UserInfo.STATUS_IS_DEPOSIT);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoUpdate.setBatteryDeposit(BigDecimal.valueOf(0));
            userInfoUpdate.setOrderId(orderId);
            userInfoService.updateById(userInfoUpdate);
            return R.ok();
        }
        eleDepositOrderMapper.insert(eleDepositOrder);

        //调起支付
        CommonPayOrder commonPayOrder = CommonPayOrder.builder()
                .orderId(orderId)
                .uid(user.getUid())
                .payAmount(payAmount)
                .orderType(ElectricityTradeOrder.ORDER_TYPE_DEPOSIT)
                .attach(ElectricityTradeOrder.ATTACH_DEPOSIT).build();
        ElectricityPayParams electricityPayParams = electricityPayParamsService.getElectricityPayParams();
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid);
        Pair<Boolean, Object> getPayParamsPair =
                electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
        if (!getPayParamsPair.getLeft()) {
            return R.failMsg(getPayParamsPair.getRight().toString());
        }
        return R.ok(getPayParamsPair.getRight());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R returnDeposit(HttpServletRequest request) {
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //限频
        Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("操作频繁,请稍后再试!");
        }
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //判断是否退电池
        UserInfo userInfo = userInfoService.queryByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_BATTERY)) {
            log.error("ELECTRICITY  ERROR! not return battery! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0044", "未退还电池");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_DEPOSIT)||Objects.isNull(userInfo.getBatteryDeposit())||Objects.isNull(userInfo.getOrderId())) {
            log.error("ELECTRICITY  ERROR! not pay deposit! userInfo:{} ", userInfo);
            return R.fail("ELECTRICITY.0045", "未缴纳押金");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, userInfo.getOrderId()));
        if(Objects.isNull(eleDepositOrder)){
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        BigDecimal deposit=userInfo.getBatteryDeposit();
        if(!Objects.equals(eleDepositOrder.getPayAmount(),deposit)){
            return R.fail("ELECTRICITY.0044","退款金额不符");
        }

        BigDecimal payAmount = eleDepositOrder.getPayAmount();
        //退款零元
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            eleDepositOrder.setStatus(EleDepositOrder.STATUS_SUCCESS);
            eleDepositOrderMapper.insert(eleDepositOrder);
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            userInfoUpdate.setServiceStatus(UserInfo.STATUS_IS_AUTH);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoUpdate.setBatteryDeposit(null);
            userInfoUpdate.setOrderId(null);
            userInfoService.updateRefund(userInfoUpdate);
            return R.ok();
        }

        String orderId = generateOrderId(uid);

        //生成退款订单
        EleRefundOrder eleRefundOrder=EleRefundOrder.builder()
                .orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(orderId)
                .payAmount(payAmount)
                .refundAmount(payAmount)
                .status(EleRefundOrder.STATUS_INIT)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        eleRefundOrderService.insert(eleRefundOrder);

        //等到后台同意退款
        return R.ok();
    }



    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid +
                RandomUtil.randomNumbers(6);
    }
}