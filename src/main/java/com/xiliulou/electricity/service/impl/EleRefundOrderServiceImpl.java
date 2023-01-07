package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.EleRefundOrderMapper;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleRefundOrderVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiRefundResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3RefundQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 退款订单表(TEleRefundOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
@Service("eleRefundOrderService")
@Slf4j
public class EleRefundOrderServiceImpl implements EleRefundOrderService {
    @Resource
    EleRefundOrderMapper eleRefundOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    @Autowired
    WechatV3JsapiService wechatV3JsapiService;
    @Autowired
    WechatConfig wechatConfig;
    @Autowired
    EleRefundOrderHistoryService eleRefundOrderHistoryService;
    @Autowired
    EleUserOperateRecordService eleUserOperateRecordService;
    @Autowired
    UnionTradeOrderService unionTradeOrderService;

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    @Autowired
    UserBatteryDepositService userBatteryDepositService;

    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    UserCarService userCarService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    UserBatteryService userBatteryService;
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    @Autowired
    CarDepositOrderService carDepositOrderService;
    @Autowired
    MemberCardFailureRecordService memberCardFailureRecordService;

    /**
     * 新增数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleRefundOrder insert(EleRefundOrder eleRefundOrder) {
        this.eleRefundOrderMapper.insert(eleRefundOrder);
        return eleRefundOrder;
    }

    /**
     * 修改数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleRefundOrder eleRefundOrder) {
        return this.eleRefundOrderMapper.updateById(eleRefundOrder);

    }

    @Override
    public WechatJsapiRefundResultDTO commonCreateRefundOrder(RefundOrder refundOrder, HttpServletRequest request) throws WechatPayException {

        //第三方订单号
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByOrderId(refundOrder.getOrderId());
        String tradeOrderNo = null;
        Integer total = null;
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", refundOrder.getOrderId());
            throw new CustomBusinessException("未找到交易订单!");
        }
        tradeOrderNo = electricityTradeOrder.getTradeOrderNo();
        total = refundOrder.getPayAmount().multiply(new BigDecimal(100)).intValue();

        if (Objects.nonNull(electricityTradeOrder.getParentOrderId())) {
            UnionTradeOrder unionTradeOrder = unionTradeOrderService.selectTradeOrderById(electricityTradeOrder.getParentOrderId());
            if (Objects.nonNull(unionTradeOrder)) {
                tradeOrderNo = unionTradeOrder.getTradeOrderNo();
                total = unionTradeOrder.getTotalFee().multiply(new BigDecimal(100)).intValue();
            }
        }

        //退款
        WechatV3RefundQuery wechatV3RefundQuery = new WechatV3RefundQuery();
        wechatV3RefundQuery.setTenantId(electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setTotal(total);
        wechatV3RefundQuery.setRefund(refundOrder.getRefundAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3RefundQuery.setReason("退款");
        wechatV3RefundQuery.setOrderId(tradeOrderNo);
        wechatV3RefundQuery.setNotifyUrl(wechatConfig.getRefundCallBackUrl() + electricityTradeOrder.getTenantId());
        wechatV3RefundQuery.setCurrency("CNY");
        wechatV3RefundQuery.setRefundId(refundOrder.getRefundOrderNo());

        return wechatV3JsapiService.refund(wechatV3RefundQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyDepositRefundOrder(WechatJsapiRefundOrderCallBackResource callBackResource) {
        //回调参数
        String tradeRefundNo = callBackResource.getOutRefundNo();
        String outTradeNo = callBackResource.getOutTradeNo();
        String refundStatus = callBackResource.getRefundStatus();

        //退款订单
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, tradeRefundNo));
        if (Objects.isNull(eleRefundOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO={}", tradeRefundNo);
            return Pair.of(false, "未找到退款订单!");
        }
        if (ObjectUtil.notEqual(EleRefundOrder.STATUS_REFUND, eleRefundOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO={}", tradeRefundNo);
            return Pair.of(false, "退款订单已处理");
        }

/*        //交易订单
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByTradeOrderNo(outTradeNo);

        String orderNo = null;
        if (Objects.isNull(electricityTradeOrder)) {
            UnionTradeOrder unionTradeOrder = unionTradeOrderService.selectTradeOrderByOrderId(outTradeNo);
            if (Objects.isNull(unionTradeOrder)) {
                log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO={}", outTradeNo);
                return Pair.of(false, "未找到交易订单!");
            }
            String jsonOrderId = unionTradeOrder.getJsonOrderId();
            List<String> orderIdLIst = JsonUtil.fromJsonArray(jsonOrderId, String.class);
            if (CollectionUtils.isEmpty(orderIdLIst)) {
                log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO={}", outTradeNo);
                return Pair.of(false, "未找到交易订单");
            }
            orderNo = orderIdLIst.get(0);
        } else {
            orderNo = electricityTradeOrder.getOrderNo();
        }*/

        //获取押金订单号
        Pair<Boolean, Object> findDepositOrderNOResult = findDepositOrder(outTradeNo, eleRefundOrder);
        if (!findDepositOrderNOResult.getLeft()) {
            return findDepositOrderNOResult;
        }

        String orderNo = (String) findDepositOrderNOResult.getRight();


        Integer refundOrderStatus = EleRefundOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(refundStatus) && ObjectUtil.equal(refundStatus, "SUCCESS")) {
            refundOrderStatus = EleRefundOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeRefundNo);
        }

        //租电池退押金
        if (Objects.equals(eleRefundOrder.getRefundOrderType(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER)) {
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(orderNo);
            if (ObjectUtil.isEmpty(eleDepositOrder)) {
                log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO={}", orderNo);
                return Pair.of(false, "未找到订单!");
            }

            UserInfo userInfo = userInfoService.queryByUidFromCache(eleDepositOrder.getUid());
            if (Objects.isNull(userInfo)) {
                log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},ORDER_NO={}", eleDepositOrder.getUid(), tradeRefundNo);
                return Pair.of(false, "未找到用户信息!");
            }

            if (Objects.equals(refundOrderStatus, EleRefundOrder.STATUS_SUCCESS)) {
                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(userInfo.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);

                userBatteryMemberCardService.deleteByUid(userInfo.getUid());

                userBatteryDepositService.deleteByUid(userInfo.getUid());

                userBatteryService.deleteByUid(userInfo.getUid());

                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }

                userInfoService.unBindUserFranchiseeId(userInfo.getUid());
            }
        }


        //租车退押金
        if (Objects.equals(eleRefundOrder.getRefundOrderType(), EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)) {
            CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(orderNo);
            if (Objects.isNull(carDepositOrder)) {
                log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND CAR DEPOSIT ORDER ORDER_NO={}", orderNo);
                return Pair.of(false, "未找到订单!");
            }

            UserInfo userInfo = userInfoService.queryByUidFromCache(carDepositOrder.getUid());
            if (Objects.isNull(userInfo)) {
                log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID={},ORDER_NO={}", carDepositOrder.getUid(), tradeRefundNo);
                return Pair.of(false, "未找到用户信息!");
            }

            //租车押金退款
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            //退押金时保存用户失效套餐记录
            //memberCardFailureRecordService.saveRentCarMemberCardFailureRecord(userInfo.getUid());

            userCarDepositService.deleteByUid(userInfo.getUid());

            userCarService.deleteByUid(userInfo.getUid());

            userCarDepositService.deleteByUid(userInfo.getUid());

            userCarMemberCardService.deleteByUid(userInfo.getUid());

            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
        }

        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setStatus(refundOrderStatus);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderMapper.updateById(eleRefundOrderUpdate);
        return Pair.of(result, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R handleRefundRentCar(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request) {
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo)
                        .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT,
                                EleRefundOrder.STATUS_REFUSE_REFUND));
        if (Objects.isNull(eleRefundOrder)) {
            log.error("REFUND ORDER ERROR! NOT FOUND ELECTRICITY_REFUND_ORDER ORDER_NO={}", refundOrderNo);
            return R.fail("未找到退款订单!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("REFUND_ORDER ERROR ,NOT FOUND ELECTRICITY_REFUND_ORDER ORDER_NO={}", refundOrderNo);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }


        if (Objects.nonNull(refundAmount)) {
            if (refundAmount.compareTo(eleRefundOrder.getRefundAmount()) > 0) {
                log.error("REFUND_ORDER ERROR ,refundAmount > payAmount ORDER_NO={}", refundOrderNo);
                return R.fail("退款金额不能大于支付金额!");
            }

            //插入修改记录
            EleRefundOrderHistory eleRefundOrderHistory = new EleRefundOrderHistory();
            eleRefundOrderHistory.setRefundOrderNo(eleRefundOrder.getRefundOrderNo());
            eleRefundOrderHistory.setRefundAmount(refundAmount);
            eleRefundOrderHistory.setCreateTime(System.currentTimeMillis());
            eleRefundOrderHistory.setTenantId(eleRefundOrder.getTenantId());
            eleRefundOrderHistoryService.insert(eleRefundOrderHistory);


        } else {
            refundAmount = eleRefundOrder.getRefundAmount();
        }

        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderUpdate.setErrMsg(errMsg);


        //同意退款
        if (Objects.equals(status, EleRefundOrder.STATUS_AGREE_REFUND)) {
            //修改订单状态
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_AGREE_REFUND);
            eleRefundOrderUpdate.setRefundAmount(refundAmount);
            eleRefundOrderService.update(eleRefundOrderUpdate);

            //退款0元，不捕获异常，成功退款
            if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {

                eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
                eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.update(eleRefundOrderUpdate);

                //查询押金绑定表的id
//                Long id = eleRefundOrderService.queryUserInfoIdByRefundOrderNo(refundOrderNo);

                if (Objects.equals(eleRefundOrder.getRefundOrderType(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER)) {
                    UserInfo updateUserInfo = new UserInfo();
                    updateUserInfo.setUid(uid);
                    updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                    updateUserInfo.setUpdateTime(System.currentTimeMillis());
                    userInfoService.updateByUid(updateUserInfo);

                    userBatteryMemberCardService.deleteByUid(userInfo.getUid());

                    userBatteryDepositService.deleteByUid(uid);

                    userBatteryService.deleteByUid(userInfo.getUid());

                    InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
                    if (Objects.nonNull(insuranceUserInfo)) {
                        insuranceUserInfoService.deleteById(insuranceUserInfo);
                    }

                    userInfoService.unBindUserFranchiseeId(uid);
                } else {
                    UserInfo updateUserInfo = new UserInfo();
                    updateUserInfo.setUid(uid);
                    updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                    updateUserInfo.setUpdateTime(System.currentTimeMillis());
                    userInfoService.updateByUid(updateUserInfo);

                    userCarDepositService.deleteByUid(uid);

                    userInfoService.unBindUserFranchiseeId(uid);
                }
                return R.ok();

            }

            //调起退款
            try {

                RefundOrder refundOrder = RefundOrder.builder()
                        .orderId(eleRefundOrder.getOrderId())
                        .refundOrderNo(eleRefundOrder.getRefundOrderNo())
                        .payAmount(eleRefundOrder.getPayAmount())
                        .refundAmount(eleRefundOrderUpdate.getRefundAmount()).build();


                eleRefundOrderService.commonCreateRefundOrder(refundOrder, request);
                //提交成功
                eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
                eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.update(eleRefundOrderUpdate);
                return R.ok();
            } catch (WechatPayException e) {
                log.error("handleRefund ERROR! wechat v3 refund  error! ", e);
            }
            //提交失败
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_FAIL);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return R.fail("ELECTRICITY.00100", "退款失败");

        }

        //拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            //修改订单状态
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
        }
        return R.ok();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public R handleOffLineRefundRentCar(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request) {

        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo)
                        .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT,
                                EleRefundOrder.STATUS_REFUSE_REFUND));
        if (Objects.isNull(eleRefundOrder)) {
            log.error("REFUND_ORDER ERROR ,NOT FOUND ELECTRICITY_REFUND_ORDER ORDER_NO={}", refundOrderNo);
            return R.fail("未找到退款订单!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("REFUND_ORDER ERROR ,NOT FOUND ELECTRICITY_REFUND_ORDER ORDER_NO={}", refundOrderNo);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        if (Objects.equals(status, EleRefundOrder.STATUS_AGREE_REFUND) && Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("returnRentCarDeposit  ERROR! user is bind car! ,uid={} ", refundOrderNo);
            return R.fail("100012", "用户绑定车辆");
        }

        if (Objects.nonNull(refundAmount)) {
            if (refundAmount.compareTo(eleRefundOrder.getRefundAmount()) > 0) {
                log.error("REFUND_ORDER ERROR ,refundAmount > payAmount ORDER_NO={}", refundOrderNo);
                return R.fail("退款金额不能大于支付金额!");
            }

            //插入修改记录
            EleRefundOrderHistory eleRefundOrderHistory = new EleRefundOrderHistory();
            eleRefundOrderHistory.setRefundOrderNo(eleRefundOrder.getRefundOrderNo());
            eleRefundOrderHistory.setRefundAmount(refundAmount);
            eleRefundOrderHistory.setCreateTime(System.currentTimeMillis());
            eleRefundOrderHistory.setTenantId(eleRefundOrder.getTenantId());
            eleRefundOrderHistoryService.insert(eleRefundOrderHistory);


        } else {
            refundAmount = eleRefundOrder.getRefundAmount();
        }

        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderUpdate.setErrMsg(errMsg);


        //同意退款
        if (Objects.equals(status, EleRefundOrder.STATUS_AGREE_REFUND)) {
            //修改订单状态
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_AGREE_REFUND);
            eleRefundOrderUpdate.setRefundAmount(refundAmount);
            eleRefundOrderService.update(eleRefundOrderUpdate);

            //退款0元，不捕获异常，成功退款
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);

            //查询押金绑定表的id
            Long id = eleRefundOrderService.queryUserInfoIdByRefundOrderNo(refundOrderNo);

//            FranchiseeUserInfo updateFranchiseeUserInfo = new FranchiseeUserInfo();
//            updateFranchiseeUserInfo.setUserInfoId(id);
//            updateFranchiseeUserInfo.setRentCarOrderId(null);
//            updateFranchiseeUserInfo.setRentCarDeposit(null);
//            updateFranchiseeUserInfo.setBindCarId(null);
//            updateFranchiseeUserInfo.setBindCarModelId(null);
//            updateFranchiseeUserInfo.setRentCarMemberCardExpireTime(null);
//            updateFranchiseeUserInfo.setRentCarStatus(FranchiseeUserInfo.RENT_CAR_STATUS_INIT);
//            updateFranchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
//            updateFranchiseeUserInfo.setRentCarMemberCardExpireTime(null);
//            updateFranchiseeUserInfo.setRentCarCardId(null);
//            franchiseeUserInfoService.modifyRentCarStatusByUserInfoId(updateFranchiseeUserInfo);

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(uid);
            updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            userCarService.deleteByUid(uid);

            return R.ok();
        }


        //拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            //修改订单状态
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
        }
        return R.ok();
    }


    @Override
    public R queryUserDepositPayType(Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("admin query user deposit pay type  ERROR! not found user,uid:{} ", uid);
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("admin query user deposit pay type ERROR! not found batteryDeposit,uid={}", uid);
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryLastPayDepositTimeByUid(uid, userInfo.getFranchiseeId(), userInfo.getTenantId(), EleDepositOrder.ELECTRICITY_DEPOSIT);
        return R.ok(eleDepositOrder.getPayType());
    }

    @Override
    public R batteryOffLineRefund(String errMsg, BigDecimal refundAmount, Long uid, Integer refundType) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin payRentCarDeposit  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }


        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("admin payRentCarDeposit  ERROR! not found user,uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());

        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.error("BATTERY DEPOSIT REFUND ERROR! user membercard is disable,uid={}", uid);
            return R.fail("100211", "用户套餐已暂停！");
        }

        //判断是否退电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("battery deposit OffLine Refund ERROR! not return battery! uid={} ", uid);
            return R.fail("ELECTRICITY.0046", "未退还电池");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);

        if (Objects.isNull(userBatteryDeposit)) {
            log.error("battery deposit OffLine Refund ERROR ,NOT FOUND ELECTRICITY_REFUND_ORDER uid={}", uid);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //查找缴纳押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());

        if (Objects.isNull(eleDepositOrder)) {
            log.error("battery deposit OffLine Refund ERROR ,NOT FOUND ELECTRICITY_REFUND_ORDER uid={}", uid);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        BigDecimal deposit = userBatteryDeposit.getBatteryDeposit();
        if (!Objects.equals(eleDepositOrder.getPayAmount(), deposit)) {
            log.error("battery deposit OffLine Refund ERROR ,Inconsistent refund amount uid={}", uid);
            return R.fail("ELECTRICITY.0044", "退款金额不符");
        }

        //退款中
        Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.nonNull(refundStatus) && (Objects.equals(refundStatus, EleRefundOrder.STATUS_REFUND) || Objects.equals(refundStatus, EleRefundOrder.STATUS_INIT))) {
            log.error("battery deposit OffLine Refund ERROR ,Inconsistent refund amount uid={}", uid);
            return R.fail("ELECTRICITY.0051", "押金正在退款中，请勿重复提交");
        }


        if (Objects.nonNull(refundAmount)) {
            if (refundAmount.compareTo(eleDepositOrder.getPayAmount()) > 0) {
                log.error("battery deposit OffLine Refund ERROR ,refundAmount > payAmount uid={}", uid);
                return R.fail("退款金额不能大于支付金额!");
            }

            //插入修改记录
            EleRefundOrderHistory eleRefundOrderHistory = new EleRefundOrderHistory();
            eleRefundOrderHistory.setRefundOrderNo(generateOrderId(uid));
            eleRefundOrderHistory.setRefundAmount(refundAmount);
            eleRefundOrderHistory.setCreateTime(System.currentTimeMillis());
            eleRefundOrderHistory.setTenantId(userInfo.getTenantId());
            eleRefundOrderHistoryService.insert(eleRefundOrderHistory);
        } else {
            refundAmount = userBatteryDeposit.getBatteryDeposit();
        }

        EleRefundOrder eleRefundOrder = new EleRefundOrder();
        eleRefundOrder.setOrderId(userBatteryDeposit.getOrderId());
        eleRefundOrder.setRefundOrderNo(generateOrderId(uid));
        eleRefundOrder.setTenantId(userInfo.getTenantId());
        eleRefundOrder.setCreateTime(System.currentTimeMillis());
        eleRefundOrder.setUpdateTime(System.currentTimeMillis());
        eleRefundOrder.setPayAmount(eleDepositOrder.getPayAmount());
        eleRefundOrder.setErrMsg(errMsg);

        if (Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.OFFLINE_PAYMENT)) {
            //生成退款订单

            eleRefundOrder.setRefundAmount(refundAmount);
            eleRefundOrder.setStatus(EleRefundOrder.STATUS_SUCCESS);
            eleRefundOrderService.insert(eleRefundOrder);

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            userBatteryMemberCardService.deleteByUid(userInfo.getUid());

            userBatteryDepositService.deleteByUid(userInfo.getUid());

            userBatteryService.deleteByUid(userInfo.getUid());

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
            }

            userInfoService.unBindUserFranchiseeId(uid);

            //生成后台操作记录
            EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                    .operateModel(EleUserOperateRecord.DEPOSIT_MODEL)
                    .operateContent(EleUserOperateRecord.REFUND_DEPOSIT__CONTENT)
                    .operateUid(user.getUid())
                    .uid(uid)
                    .name(user.getUsername())
                    .oldBatteryDeposit(userBatteryDeposit.getBatteryDeposit())
                    .newBatteryDeposit(null)
                    .tenantId(TenantContextHolder.getTenantId())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserOperateRecord);
            return R.ok();
        } else {
            //退款0元，不捕获异常，成功退款
            if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {


                eleRefundOrder.setStatus(EleRefundOrder.STATUS_SUCCESS);
                eleRefundOrder.setRefundAmount(refundAmount);
                eleRefundOrderService.insert(eleRefundOrder);

                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(userInfo.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);

                userBatteryMemberCardService.deleteByUid(userInfo.getUid());

                userBatteryService.deleteByUid(userInfo.getUid());

                userBatteryDepositService.deleteByUid(userInfo.getUid());

                userInfoService.unBindUserFranchiseeId(uid);
                return R.ok();
            }

            //调起退款
            try {

                RefundOrder refundOrder = RefundOrder.builder()
                        .orderId(eleRefundOrder.getOrderId())
                        .refundOrderNo(eleRefundOrder.getRefundOrderNo())
                        .payAmount(eleDepositOrder.getPayAmount())
                        .refundAmount(refundAmount).build();


                eleRefundOrderService.commonCreateRefundOrder(refundOrder, null);
                //提交成功
                eleRefundOrder.setStatus(EleRefundOrder.STATUS_REFUND);
                eleRefundOrder.setRefundAmount(refundAmount);
                eleRefundOrder.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.insert(eleRefundOrder);
                return R.ok();
            } catch (WechatPayException e) {
                log.error("battery deposit OffLine Refund ERROR! wechat v3 refund  error! ", e);
            }
            //提交失败
            eleRefundOrder.setStatus(EleRefundOrder.STATUS_FAIL);
            eleRefundOrder.setRefundAmount(refundAmount);
            eleRefundOrder.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.insert(eleRefundOrder);
            return R.fail("ELECTRICITY.00100", "退款失败");
        }
    }

    @Override
    public R queryList(EleRefundQuery eleRefundQuery) {
        return R.ok(eleRefundOrderMapper.queryList(eleRefundQuery));
    }

    @Override
    public List<EleRefundOrderVO> selectCarRefundPageList(EleRefundQuery eleRefundQuery) {
        return eleRefundOrderMapper.selectCarRefundPageList(eleRefundQuery);
    }

    @Override
    public Integer selectCarRefundPageCount(EleRefundQuery eleRefundQuery) {
        return eleRefundOrderMapper.selectCarRefundPageCount(eleRefundQuery);
    }

    @Override
    public Integer queryCountByOrderId(String orderId) {
        return eleRefundOrderMapper.selectCount(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_AGREE_REFUND, EleRefundOrder.STATUS_REFUND, EleRefundOrder.STATUS_SUCCESS));
    }

    @Override
    public Integer queryIsRefundingCountByOrderId(String orderId) {
        return eleRefundOrderMapper.selectCount(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_AGREE_REFUND, EleRefundOrder.STATUS_REFUND));
    }

    @Override
    public Integer queryStatusByOrderId(String orderId) {
        List<EleRefundOrder> eleRefundOrderList = eleRefundOrderMapper.selectList(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).orderByDesc(EleRefundOrder::getUpdateTime));
        if (ObjectUtil.isEmpty(eleRefundOrderList)) {
            return null;
        }
        return eleRefundOrderList.get(0).getStatus();
    }

    @Override
    public R queryCount(EleRefundQuery eleRefundQuery) {
        return R.ok(eleRefundOrderMapper.queryCount(eleRefundQuery));
    }

    @Override
    public Long queryUserInfoIdByRefundOrderNo(String refundOrderNo) {
        return eleRefundOrderMapper.queryUserInfoId(refundOrderNo);
    }

    @Override
    public BigDecimal queryTurnOver(Integer tenantId) {
        return Optional.ofNullable(eleRefundOrderMapper.queryTurnOver(tenantId)).orElse(new BigDecimal("0"));
    }

    @Override
    public BigDecimal queryTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType, List<Long> franchiseeIds) {
        return Optional.ofNullable(eleRefundOrderMapper.queryTurnOverByTime(tenantId, todayStartTime, refundOrderType,franchiseeIds)).orElse(BigDecimal.valueOf(0));
    }

    @Override
    public BigDecimal queryCarRefundTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType, List<Long> franchiseeIds) {
        return Optional.ofNullable(eleRefundOrderMapper.queryCarRefundTurnOverByTime(tenantId, todayStartTime, refundOrderType,franchiseeIds)).orElse(BigDecimal.valueOf(0));
    }

    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(3) + uid +
                RandomUtil.randomNumbers(2);
    }

    @Override
    public boolean checkDepositOrderIsRefund(String orderId) {
        EleRefundOrder eleRefundOrder = this.eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).eq(EleRefundOrder::getStatus, EleRefundOrder.STATUS_SUCCESS));
        if(!Objects.isNull(eleRefundOrder)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * 获取押金订单号
     * @param outTradeNo
     * @param eleRefundOrder
     * @return
     */
    private Pair<Boolean, Object> findDepositOrder(String outTradeNo, EleRefundOrder eleRefundOrder) {
        String depositOrderNO = null;

        //单独支付
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByTradeOrderNo(outTradeNo);
        if (Objects.nonNull(electricityTradeOrder)) {
            depositOrderNO = electricityTradeOrder.getOrderNo();
        }

        //混合支付
        if (Objects.isNull(electricityTradeOrder)) {
            UnionTradeOrder unionTradeOrder = unionTradeOrderService.selectTradeOrderByOrderId(outTradeNo);
            if (Objects.isNull(unionTradeOrder)) {
                log.error("NOTIFY UNION PAY ORDER ERROR!not found union trade order orderNo={}", outTradeNo);
                return Pair.of(false, "未找到交易订单!");
            }

            List<String> orderIdList = JsonUtil.fromJsonArray(unionTradeOrder.getJsonOrderId(), String.class);
            if (CollectionUtils.isEmpty(orderIdList)) {
                log.error("NOTIFY UNION PAY ORDER ERROR!orderIdList is empty,orderNo={}", outTradeNo);
                return Pair.of(false, "交易订单编号不存在!");
            }

            List<Integer> orderTypeList = JsonUtil.fromJsonArray(unionTradeOrder.getJsonOrderType(), Integer.class);
            if (CollectionUtils.isEmpty(orderTypeList)) {
                log.error("NOTIFY UNION PAY ORDER ERROR!orderTypeList is empty,orderNo={}", outTradeNo);
                return Pair.of(false, "交易订单类型不存!");
            }

            //租电池押金退款
            if (Objects.equals(eleRefundOrder.getRefundOrderType(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER)) {
                int index = orderTypeList.indexOf(UnionPayOrder.ORDER_TYPE_DEPOSIT);
                if (index < 0) {
                    log.error("NOTIFY UNION PAY ORDER ERROR! not found orderType,orderNo={},orderType={}", outTradeNo, UnionPayOrder.ORDER_TYPE_DEPOSIT);
                    return Pair.of(false, "租电池押金退款订单类型不存!");
                }

                depositOrderNO = orderIdList.get(index);
            }

            //租车押金退款
            if (Objects.equals(eleRefundOrder.getRefundOrderType(), EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)) {
                int index = orderTypeList.indexOf(UnionPayOrder.ORDER_TYPE_RENT_CAR_DEPOSIT);
                if (index < 0) {
                    log.error("NOTIFY UNION PAY ORDER ERROR! not found orderType,orderNo={},orderType={}", outTradeNo, UnionPayOrder.ORDER_TYPE_RENT_CAR_DEPOSIT);
                    return Pair.of(false, "租车押金退款订单类型不存!");
                }

                depositOrderNO = orderIdList.get(index);
            }
        }

        return Pair.of(true, depositOrderNO);
    }

}
