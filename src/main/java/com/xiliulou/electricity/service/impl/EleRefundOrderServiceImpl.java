package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.EleRefundOrderMapper;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleRefundOrderVO;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositUnfreezeRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzDepositUnfreezeRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
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
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
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
    @Autowired
    FreeDepositOrderService freeDepositOrderService;
    @Autowired
    PxzConfigService pxzConfigService;
    @Autowired
    PxzDepositService pxzDepositService;
    
    @Autowired
    FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;

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
    public List<EleRefundOrder> selectBatteryFreeDepositRefundingOrder(Integer offset, Integer size) {
        return this.eleRefundOrderMapper.selectBatteryFreeDepositRefundingOrder(offset,size);
    }

    @Override
    public List<EleRefundOrder> selectCarFreeDepositRefundingOrder(int offset, Integer size) {
        return this.eleRefundOrderMapper.selectCarFreeDepositRefundingOrder(offset,size);
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
    
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());

//                userBatteryDepositService.deleteByUid(userInfo.getUid());
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());

                userBatteryService.deleteByUid(userInfo.getUid());

                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }

                //退押金解绑用户所属加盟商
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
    
            //userCarDepositService.deleteByUid(userInfo.getUid());

            userCarService.deleteByUid(userInfo.getUid());

            userCarDepositService.logicDeleteByUid(userInfo.getUid());

            userCarMemberCardService.deleteByUid(userInfo.getUid());

            //退押金解绑用户所属加盟商
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
//    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> handleRefundOrder(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, HttpServletRequest request) {

        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo)
                        .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUSE_REFUND));
        if (Objects.isNull(eleRefundOrder)) {
            log.error("REFUND ORDER ERROR! eleRefundOrder is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("REFUND ORDER ERROR!userInfo is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("REFUND ORDER ERROR!userBatteryDeposit is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }

        //校验退款金额
        if (Objects.nonNull(refundAmount)) {
            if (refundAmount.compareTo(eleRefundOrder.getRefundAmount()) > 0) {
                log.error("REFUND ORDER ERROR!refundAmount is illegal,refoundOrderNo={},uid={}", refundOrderNo, uid);
                return Triple.of(false, "ELECTRICITY.0007", "退款金额不能大于支付金额!");
            }

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
        eleRefundOrderUpdate.setRefundAmount(refundAmount);
        eleRefundOrderUpdate.setErrMsg(errMsg);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());

        //拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return Triple.of(true, "", null);
        }

//        eleRefundOrderUpdate.setRefundAmount(refundAmount);
//        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_AGREE_REFUND);
//        eleRefundOrderService.update(eleRefundOrderUpdate);

        //退款0元
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            return handleBatteryZeroDepositRefundOrder(eleRefundOrderUpdate, userInfo);
        }

        try {
            RefundOrder refundOrder = RefundOrder.builder()
                    .orderId(eleRefundOrder.getOrderId())
                    .refundOrderNo(eleRefundOrder.getRefundOrderNo())
                    .payAmount(eleRefundOrder.getPayAmount())
                    .refundAmount(eleRefundOrderUpdate.getRefundAmount()).build();

            eleRefundOrderService.commonCreateRefundOrder(refundOrder, request);

            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);

            return Triple.of(true, "", null);
        } catch (WechatPayException e) {
            log.error("REFUND ORDER ERROR! wechat v3 refund  error! ", e);
        }

        //提交失败
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_FAIL);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);

        return Triple.of(false, "ELECTRICITY.00100", "退款失败");
    }

    @Override
    public Triple<Boolean, String, Object> batteryFreeDepostRefundAudit(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid) {
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo)
                        .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER)
                        .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT));
                        //.in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUSE_REFUND));
        if (Objects.isNull(eleRefundOrder)) {
            log.error("FREE REFUND ORDER ERROR! eleRefundOrder is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("FREE REFUND ORDER ERROR!userInfo is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("FREE REFUND ORDER ERROR!eleDepositOrder is null,orderId={},uid={}", eleRefundOrder.getOrderId(), uid);
            return Triple.of(false, "ELECTRICITY.0015", "换电订单不存在");
        }

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("REFUND ORDER ERROR! not found freeDepositOrder,uid={}", userInfo.getUid());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        EleRefundOrder carRefundOrder = null;
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            carRefundOrder = eleRefundOrderMapper.selectOne(
                    new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, eleRefundOrder.getOrderId())
                            .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                            .eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
                            .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT));
                            //.in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUSE_REFUND));
//            if (Objects.isNull(carRefundOrder)) {
//                log.error("FREE REFUND ORDER ERROR! carRefundOrder is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
//                return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
//            }

            if (Objects.nonNull(carRefundOrder) && Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
                EleRefundOrder carRefundOrderUpdate = new EleRefundOrder();
                carRefundOrderUpdate.setId(carRefundOrder.getId());
                carRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                carRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
                eleRefundOrderService.update(carRefundOrderUpdate);
                //return Triple.of(true, "", null);
            }
        }

        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setRefundAmount(refundAmount);
        eleRefundOrderUpdate.setErrMsg(errMsg);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());

        //拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return Triple.of(true, "", null);
        }

        //处理电池免押订单退款
        if (!Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            log.error("FREE REFUND ORDER ERROR!depositOrder payType is illegal,orderId={},uid={}", eleRefundOrder.getOrderId(), uid);
            return Triple.of(false, "100406", "订单非免押支付");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("REFUND ORDER ERROR! not found pxzConfig,uid={}", userInfo.getUid());
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }


        //如果车电一起免押，检查用户是否归还车辆
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY) && Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("REFUND ORDER ERROR! user not return car,uid={}", userInfo.getUid());
            return Triple.of(false, "100253", "用户已绑定车辆");
        }

        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> testQuery = new PxzCommonRequest<>();
        testQuery.setAesSecret(pxzConfig.getAesKey());
        testQuery.setDateTime(System.currentTimeMillis());
        testQuery.setSessionId(eleRefundOrder.getOrderId());
        testQuery.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark("电池押金解冻");
        queryRequest.setTransId(freeDepositOrder.getOrderId());
        testQuery.setData(queryRequest);

        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzUnfreezeDepositCommonRsp = null;

        try {
            pxzUnfreezeDepositCommonRsp = pxzDepositService.unfreezeDeposit(testQuery);
        } catch (Exception e) {
            log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100401", "免押解冻调用失败！");
        }

        if (Objects.isNull(pxzUnfreezeDepositCommonRsp)) {
            log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (!pxzUnfreezeDepositCommonRsp.isSuccess()) {
            log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", pxzUnfreezeDepositCommonRsp.getRespDesc());
        }

        //如果解冻成功
        if (Objects.equals(pxzUnfreezeDepositCommonRsp.getData().getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
            //更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositOrderService.update(freeDepositOrderUpdate);

            //更新退款订单
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);

            UserInfo updateUserInfo = new UserInfo();

            //如果车电一起免押，解绑用户车辆信息
            if (Objects.nonNull(carRefundOrder) && Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {

                EleRefundOrder carRefundOrderUpdate = new EleRefundOrder();
                carRefundOrderUpdate.setId(carRefundOrder.getId());
                carRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
                carRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.update(carRefundOrderUpdate);

                updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);

                userCarService.deleteByUid(uid);

                userCarDepositService.logicDeleteByUid(uid);

                userCarMemberCardService.deleteByUid(uid);
            }

            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
            userBatteryService.deleteByUid(userInfo.getUid());

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
            }
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());

            return Triple.of(true, "", "免押解冻成功");
        }

        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);

        //更新退款订单
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);

        if (Objects.nonNull(carRefundOrder) && Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            EleRefundOrder carRefundOrderUpdate = new EleRefundOrder();
            carRefundOrderUpdate.setId(carRefundOrder.getId());
            carRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            carRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(carRefundOrderUpdate);
        }
            return Triple.of(true, "", "退款中，请稍后");
    }
    
    @Override
    public Triple<Boolean, String, Object> carRefundDepositReview(Long id, String errMsg, Integer status,
            BigDecimal refundAmount, HttpServletRequest request) {
        TokenUser tokenUser = SecurityUtils.getUserInfo();
        if (Objects.isNull(tokenUser)) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! not found user!");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
    
        User user = userService.queryByUidFromCache(tokenUser.getUid());
        if (Objects.isNull(user)) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! not found user! uid={}", tokenUser.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectById(id);
        if (Objects.isNull(eleRefundOrder)) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! not found electricityRefundOrder! id={}", id);
            return Triple.of(false, "", "未找到退款订单!");
        }
        
        //订单状态判断
        if (!Objects.equals(eleRefundOrder.getStatus(), EleRefundOrder.STATUS_INIT)) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! EleRefundOrder status illegal! id={}", id);
            return Triple.of(false, "", "退款订单已处理，请勿重复提交");
        }
        
        CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(carDepositOrder)) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! not found carDepositOrder! orderId={}",
                    eleRefundOrder.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(carDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! not found userInfo!  uid={}", carDepositOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, null, null);
        }
        
        if (Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! user is bind car! uid={} ", carDepositOrder.getUid());
            return Triple.of(false, "100012", "用户绑定车辆");
        }
        
        BigDecimal userRefundAmount = refundAmount;
        
        if (Objects.nonNull(userRefundAmount)) {
            if (userRefundAmount.compareTo(eleRefundOrder.getRefundAmount()) > 0) {
                log.error("CAR REFUND DEPOSIT REVIEW ERROR! ,refundAmount > payAmount! eleRefundOrder={}",
                        eleRefundOrder.getRefundOrderNo());
                return Triple.of(false, "", "退款金额不能大于支付金额!");
            }
            
            //插入修改记录
            EleRefundOrderHistory eleRefundOrderHistory = new EleRefundOrderHistory();
            eleRefundOrderHistory.setRefundOrderNo(eleRefundOrder.getRefundOrderNo());
            eleRefundOrderHistory.setRefundAmount(refundAmount);
            eleRefundOrderHistory.setCreateTime(System.currentTimeMillis());
            eleRefundOrderHistory.setTenantId(eleRefundOrder.getTenantId());
            eleRefundOrderHistoryService.insert(eleRefundOrderHistory);
        } else {
            userRefundAmount = eleRefundOrder.getRefundAmount();
        }
        
        //更新退款订单
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderUpdate.setErrMsg(errMsg);
        
        //后台拒绝
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return Triple.of(true, "", null);
        }
        
        //后台同意
        //零元或线下
        if (BigDecimal.valueOf(0).compareTo(userRefundAmount) == 0 || CarDepositOrder.OFFLINE_PAYTYPE
                .equals(carDepositOrder.getPayType())) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
            eleRefundOrderService.update(eleRefundOrderUpdate);
            
            
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);
        
            userCarService.deleteByUid(userInfo.getUid());
        
            userCarDepositService.logicDeleteByUid(userInfo.getUid());
        
            userCarMemberCardService.deleteByUid(userInfo.getUid());
        
            //退押金解绑用户所属加盟商
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
            return Triple.of(true, "", null);
        }
        
        try {
            RefundOrder refundOrder = RefundOrder.builder().orderId(eleRefundOrder.getOrderId())
                    .refundOrderNo(eleRefundOrder.getRefundOrderNo()).payAmount(eleRefundOrder.getPayAmount())
                    .refundAmount(eleRefundOrder.getRefundAmount()).build();
            
            eleRefundOrderService.commonCreateRefundOrder(refundOrder, request);
            
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);
            
            return Triple.of(true, "", null);
        } catch (WechatPayException e) {
            log.error("CAR REFUND DEPOSIT REVIEW ERROR! wechat v3 refund  error! ", e);
        }
        
        //提交失败
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_FAIL);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);
        return Triple.of(false, "ELECTRICITY.00100", "退款失败");
    }
    
    @Override
    public Triple<Boolean, String, Object> carFreeDepostRefundAudit(Long id, String errMsg, Integer status,
            BigDecimal refundAmount) {
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectById(id);
        if (Objects.isNull(eleRefundOrder)) {
            log.error("CAR FREE REFUND ORDER ERROR! not found electricityRefundOrder! id={}", id);
            return Triple.of(false, "", "未找到退款订单!");
        }

        //订单状态判断
        if (!Objects.equals(eleRefundOrder.getStatus(), EleRefundOrder.STATUS_INIT)) {
            log.error("CAR FREE REFUND ORDER ERROR! EleRefundOrder status illegal! id={}", id);
            return Triple.of(false, "", "退款订单已处理，请勿重复提交");
        }

        CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(carDepositOrder)) {
            log.error("CAR  FREE REFUND ORDER ERROR! carDepositOrder is null,orderId={}", eleRefundOrder.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(carDepositOrder.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("CAR FREE REFUND ORDER ERROR! userInfo is null,id={}, uid={}", id, carDepositOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("CAR REFUND ORDER ERROR! not found freeDepositOrder,uid={}", userInfo.getUid());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        EleRefundOrder batteryRefundOrder= null;
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            batteryRefundOrder = eleRefundOrderMapper.selectOne(
                    new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, eleRefundOrder.getOrderId())
                            .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                            .eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER)
                            .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT));
//            if (Objects.isNull(batteryRefundOrder)) {
//                log.error("FREE REFUND ORDER ERROR! eleRefundOrder is null,refoundOrderNo={},uid={}", eleRefundOrder.getOrderId(), userInfo.getUid());
//                return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
//            }

            if (Objects.nonNull(batteryRefundOrder) && Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
                EleRefundOrder carRefundOrderUpdate = new EleRefundOrder();
                carRefundOrderUpdate.setId(batteryRefundOrder.getId());
                carRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                carRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
                eleRefundOrderService.update(carRefundOrderUpdate);
                //return Triple.of(true, "", null);
            }
        }

        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setRefundAmount(refundAmount);
        eleRefundOrderUpdate.setErrMsg(errMsg);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());

        //拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return Triple.of(true, "", null);
        }

        //处理电池免押订单退款
        if (!Objects.equals(carDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            log.error("CAR FREE REFUND ORDER ERROR!depositOrder payType is illegal,orderId={},uid={}",
                    eleRefundOrder.getOrderId(), carDepositOrder.getUid());
            return Triple.of(false, "100406", "订单非免押支付");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils
                .isBlank(pxzConfig.getMerchantCode())) {
            log.error("CAR REFUND ORDER ERROR! not found pxzConfig,uid={}", userInfo.getUid());
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }



        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> testQuery = new PxzCommonRequest<>();
        testQuery.setAesSecret(pxzConfig.getAesKey());
        testQuery.setDateTime(System.currentTimeMillis());
        testQuery.setSessionId(eleRefundOrder.getOrderId());
        testQuery.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark("电池押金解冻");
        queryRequest.setTransId(freeDepositOrder.getOrderId());
        testQuery.setData(queryRequest);

        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzUnfreezeDepositCommonRsp = null;

        try {
            pxzUnfreezeDepositCommonRsp = pxzDepositService.unfreezeDeposit(testQuery);
        } catch (Exception e) {
            log.error("CAR REFUND ORDER ERROR! unfreeDepositOrder fail! uid={},orderId={}", userInfo.getUid(),
                    freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100401", "免押解冻调用失败！");
        }

        if (Objects.isNull(pxzUnfreezeDepositCommonRsp)) {
            log.error("CAR REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={}",
                    userInfo.getUid(),
                    freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (!pxzUnfreezeDepositCommonRsp.isSuccess()) {
            log.error("CAR REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={}",
                    userInfo.getUid(),
                    freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", pxzUnfreezeDepositCommonRsp.getRespDesc());
        }

        //如果解冻成功
        if (Objects.equals(pxzUnfreezeDepositCommonRsp.getData().getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
            //更新免押订单状态
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
            eleRefundOrderService.update(eleRefundOrderUpdate);

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());

            if (Objects.nonNull(batteryRefundOrder) && Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {

                EleRefundOrder batteryRefundOrderUpdate = new EleRefundOrder();
                batteryRefundOrderUpdate.setId(batteryRefundOrder.getId());
                batteryRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
                batteryRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.update(batteryRefundOrderUpdate);

                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);

                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
                userBatteryService.deleteByUid(userInfo.getUid());

                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }
            }


            userInfoService.updateByUid(updateUserInfo);

            userCarService.deleteByUid(userInfo.getUid());

            userCarDepositService.logicDeleteByUid(userInfo.getUid());

            userCarMemberCardService.deleteByUid(userInfo.getUid());

            //退押金解绑用户所属加盟商
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());

            return Triple.of(true, "", "免押解冻成功");
        }

        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);

        //更新退款订单
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);

        if (Objects.nonNull(batteryRefundOrder) && Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            EleRefundOrder batteryRefundOrderUpdate = new EleRefundOrder();
            batteryRefundOrderUpdate.setId(batteryRefundOrder.getId());
            batteryRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            batteryRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(batteryRefundOrderUpdate);
        }
        return Triple.of(true, "", "退款中，请稍后");
    }
    
    /**
     * 电池免押退押金
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> batteryFreeDepositRefund(String errMsg, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("REFUND ORDER ERROR!userInfo is null,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("REFUND ORDER ERROR! not found pxzConfig,uid={}", uid);
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("REFUND ORDER ERROR! user is disable! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("REFUND ORDER ERROR! user is not rent deposit,uid={}", uid);
            return Triple.of(false, "100238", "未缴纳押金");
        }

        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.error("REFUND ORDER ERROR! user membercard is disable,uid={}", uid);
            return Triple.of(false, "100211", "用户套餐已暂停！");
        }
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("REFUND ORDER ERROR! disable member card is reviewing,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.100003", "套餐暂停正在审核中");
        }

        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("REFUND ORDER ERROR! not return battery,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0046", "未退还电池");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("REFUND ORDER ERROR！userBatteryDeposit is null,uid={}", uid);
            return Triple.of(false, "100247", "用户电池押金信息不存在");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("REFUND ORDER ERROR! not found eleDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("REFUND ORDER ERROR! not found freeDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        //如果车电一起免押，检查用户是否归还车辆
        EleDepositOrder carDepositOrder = null;
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            if(Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)){
                log.error("REFUND ORDER ERROR! user not return car,uid={}", userInfo.getUid());
                return Triple.of(false, "100253", "用户已绑定车辆");
            }

            if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                log.error("ELE CAR REFUND ERROR! user is not rent deposit,uid={}", uid);
                return Triple.of(false, "100238", "未缴纳押金");
            }


            UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(uid);
            if (Objects.isNull(userCarDeposit)) {
                log.error("ELE CAR REFUND ERROR! not found userCarDeposit! uid={}", uid);
                return Triple.of(false, "ELECTRICITY.0001", "未找到用户信息");
            }

            //查找缴纳押金订单
            carDepositOrder = eleDepositOrderService.queryByOrderId(userCarDeposit.getOrderId());
            if (Objects.isNull(carDepositOrder)) {
                log.error("ELE CAR REFUND ERROR! not found eleDepositOrder! uid={},orderId={}", uid,
                        userCarDeposit.getOrderId());
                return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
            }
        }
    
        //获取订单代扣信息计算返还金额
        BigDecimal refundAmount = eleDepositOrder.getPayAmount();
        FreeDepositAlipayHistory freeDepositAlipayHistory = freeDepositAlipayHistoryService
                .queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.nonNull(freeDepositAlipayHistory)) {
            refundAmount = eleDepositOrder.getPayAmount()
                    .subtract(freeDepositAlipayHistory.getAlipayAmount());
    
        }
    
        BigDecimal eleRefundAmount = refundAmount.doubleValue() < 0 ? BigDecimal.ZERO : refundAmount;
        BigDecimal carRefundAmount = BigDecimal.ZERO;
        
        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(userBatteryDeposit.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark("电池免押解冻");
        queryRequest.setTransId(userBatteryDeposit.getOrderId());
        query.setData(queryRequest);

        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzDepositUnfreezeRspPxzCommonRsp = null;
        try {
            pxzDepositUnfreezeRspPxzCommonRsp = pxzDepositService.unfreezeDeposit(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! unFreeDepositOrderQuery fail! uid={},orderId={}", uid, userBatteryDeposit.getOrderId(), e);
            return Triple.of(false, "100406", "免押解冻失败！");
        }

        if (Objects.isNull(pxzDepositUnfreezeRspPxzCommonRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (!pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzDepositUnfreezeRspPxzCommonRsp.getRespDesc());
        }

        if(Objects.equals(pxzDepositUnfreezeRspPxzCommonRsp.getData().getAuthStatus(),FreeDepositOrder.AUTH_UN_FROZEN)){
            //更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositOrderService.update(freeDepositOrderUpdate);

            //生成退款订单
            EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                    .orderId(eleDepositOrder.getOrderId())
                    .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REFUND, uid))
                    .payAmount(eleDepositOrder.getPayAmount()).refundAmount(eleRefundAmount)
                    .status(EleRefundOrder.STATUS_SUCCESS)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(eleDepositOrder.getTenantId())
                    .build();
            eleRefundOrderService.insert(eleRefundOrder);

            //更新用户状态
            UserInfo updateUserInfo = new UserInfo();

            //如果车电一起免押，解绑用户车辆信息
            //            if(Objects.equals(freeDepositOrder.getDepositType(),FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)){
            //                updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            //
            //                userCarService.deleteByUid(uid);
            //
            //                userCarDepositService.logicDeleteByUid(uid);
            //
            //                userCarMemberCardService.deleteByUid(uid);
            //            }

            updateUserInfo.setUid(uid);
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());


            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
            userBatteryService.deleteByUid(userInfo.getUid());

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
            }

            userInfoService.unBindUserFranchiseeId(uid);
    
            if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {

                carRefundAmount = refundAmount.doubleValue() > 0 ? carDepositOrder.getPayAmount()
                        :carDepositOrder.getPayAmount().add(refundAmount);
        
                EleRefundOrder carRefundOrder = EleRefundOrder.builder().orderId(carDepositOrder.getOrderId())
                        .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_REFUND, uid))
                        .payAmount(carDepositOrder.getPayAmount()).refundAmount(carRefundAmount)
                        .status(EleRefundOrder.STATUS_SUCCESS).createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis()).tenantId(carDepositOrder.getTenantId())
                        .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER).build();
        
                updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
        
                userCarService.deleteByUid(uid);
        
                userCarDepositService.logicDeleteByUid(uid);
        
                userCarMemberCardService.deleteByUid(uid);
                eleRefundOrderService.insert(carRefundOrder);
        
            }

            userInfoService.updateByUid(updateUserInfo);
            return Triple.of(true, "", null);
        }

        //更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);

        //生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                .orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REFUND, uid))
                .payAmount(eleDepositOrder.getPayAmount()).refundAmount(eleRefundAmount)
                .status(EleRefundOrder.STATUS_REFUND)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId())
                .build();
        eleRefundOrderService.insert(eleRefundOrder);
    
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            EleRefundOrder carRefundOrder = EleRefundOrder.builder().orderId(carDepositOrder.getOrderId())
                    .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_REFUND, uid))
                    .payAmount(carRefundAmount).refundAmount(carRefundAmount).status(EleRefundOrder.STATUS_SUCCESS)
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                    .tenantId(eleDepositOrder.getTenantId())
                    .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER).build();
            eleRefundOrderService.insert(carRefundOrder);
        }

        return Triple.of(false, "100413", "免押押金解冻中");
    }

    /**
     * 车辆免押退押金
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> carFreeDepositRefund(String errMsg, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("REFUND ORDER ERROR!userInfo is null,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("REFUND ORDER ERROR! not found pxzConfig,uid={}", uid);
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("REFUND ORDER ERROR! user is disable! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("REFUND ORDER ERROR! user is not rent deposit,uid={}", uid);
            return Triple.of(false, "100238", "未缴纳车辆押金");
        }

        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_NO)) {
            log.error("REFUND ORDER ERROR! user is rent car,uid={}", uid);
            return Triple.of(false, "100250", "用户未归还车辆");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userCarDeposit)) {
            log.error("REFUND ORDER ERROR! not found userCarDeposit! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户信息");
        }

        CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(userCarDeposit.getOrderId(), TenantContextHolder.getTenantId());
        if (Objects.isNull(carDepositOrder)) {
            log.error("REFUND ORDER ERROR! not found carDepositOrder,uid={},orderId={}", uid, userCarDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userCarDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("REFUND ORDER ERROR! not found freeDepositOrder,uid={},orderId={}", uid, userCarDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        //如果车电一起免押，检查用户是否归还电池
        Triple<Boolean, String, Object> batteryDepositPreCheckResult = eleDepositOrderService.returnDepositPreCheck(userInfo);
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)
                && Boolean.TRUE.equals(!batteryDepositPreCheckResult.getLeft())) {
            log.error("REFUND ORDER ERROR! user not return battery,uid={}", userInfo.getUid());
            return batteryDepositPreCheckResult;
        }

        EleDepositOrder eleDepositOrder = null;
        if(Boolean.TRUE.equals(!batteryDepositPreCheckResult.getLeft())){
            eleDepositOrder = (EleDepositOrder)batteryDepositPreCheckResult.getRight();
        }
    
        //获取订单代扣信息计算返还金额
        BigDecimal refundAmount = carDepositOrder.getPayAmount();
        FreeDepositAlipayHistory freeDepositAlipayHistory = freeDepositAlipayHistoryService
                .queryByOrderId(userCarDeposit.getOrderId());
        if (Objects.nonNull(freeDepositAlipayHistory)) {
             refundAmount = carDepositOrder.getPayAmount()
                    .subtract(freeDepositAlipayHistory.getAlipayAmount());
        }

        BigDecimal carRefundAmount = refundAmount.doubleValue() < 0 ? BigDecimal.ZERO : refundAmount;
        BigDecimal eleRefundAmount = BigDecimal.ZERO;

        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(userCarDeposit.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark("车辆免押解冻");
        queryRequest.setTransId(userCarDeposit.getOrderId());
        query.setData(queryRequest);

        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzDepositUnfreezeRspPxzCommonRsp = null;
        try {
            pxzDepositUnfreezeRspPxzCommonRsp = pxzDepositService.unfreezeDeposit(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! unFreeDepositOrderQuery fail! uid={},orderId={}", uid, userCarDeposit.getOrderId(), e);
            return Triple.of(false, "100406", "免押解冻失败！");
        }

        if (Objects.isNull(pxzDepositUnfreezeRspPxzCommonRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", uid, userCarDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (!pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzDepositUnfreezeRspPxzCommonRsp.getRespDesc());
        }

        if(Objects.equals(pxzDepositUnfreezeRspPxzCommonRsp.getData().getAuthStatus(),FreeDepositOrder.AUTH_UN_FROZEN)){
            //更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositOrderService.update(freeDepositOrderUpdate);

            EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                    .orderId(carDepositOrder.getOrderId())
                    .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_REFUND, uid))
                    .payAmount(carDepositOrder.getPayAmount()).refundAmount(carRefundAmount)
                    .status(EleRefundOrder.STATUS_SUCCESS)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(carDepositOrder.getTenantId())
                    .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
                    .build();
            eleRefundOrderService.insert(eleRefundOrder);

            UserInfo updateUserInfo = new UserInfo();

            updateUserInfo.setUid(uid);
            updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            userCarService.deleteByUid(uid);
            userCarDepositService.logicDeleteByUid(uid);
            userCarMemberCardService.deleteByUid(uid);
            userInfoService.unBindUserFranchiseeId(uid);
            //车辆电池一起免押，退押金解绑用户电池信息
            if (Objects.nonNull(eleDepositOrder) && Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
                eleRefundAmount = refundAmount.doubleValue() > 0? eleDepositOrder.getPayAmount() : eleDepositOrder.getPayAmount().add(refundAmount);

                EleRefundOrder insertEleRefundOrder = EleRefundOrder.builder()
                        .orderId(eleDepositOrder.getOrderId())
                        .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REFUND, uid))
                        .payAmount(eleDepositOrder.getPayAmount()).refundAmount(eleRefundAmount)
                        .status(EleRefundOrder.STATUS_SUCCESS)
                        .createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis())
                        .tenantId(eleDepositOrder.getTenantId())
                        .build();
                eleRefundOrderService.insert(insertEleRefundOrder);

                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);

                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
                userBatteryService.deleteByUid(userInfo.getUid());

                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }
            }

            return Triple.of(true, "", "解冻成功");
        }

        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);

        EleRefundOrder eleRefundOrder = EleRefundOrder.builder()
                .orderId(carDepositOrder.getOrderId())
                .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_REFUND, uid))
                .payAmount(carDepositOrder.getPayAmount()).refundAmount(refundAmount)
                .status(EleRefundOrder.STATUS_REFUND)
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(carDepositOrder.getTenantId())
                .refundOrderType(EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
                .build();
        eleRefundOrderService.insert(eleRefundOrder);

        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            EleRefundOrder insertEleRefundOrder = EleRefundOrder.builder()
                    .orderId(eleDepositOrder.getOrderId())
                    .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_REFUND, uid))
                    .payAmount(eleDepositOrder.getPayAmount()).refundAmount(eleRefundAmount)
                    .status(EleRefundOrder.STATUS_REFUND)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .tenantId(eleDepositOrder.getTenantId())
                    .build();
            eleRefundOrderService.insert(insertEleRefundOrder);
        }

        return Triple.of(false, "100413", "免押押金解冻中");    }

    /**
     * 处理电池押金0元
     */
    private Triple<Boolean, String, Object> handleBatteryZeroDepositRefundOrder(EleRefundOrder eleRefundOrderUpdate, UserInfo userInfo) {
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);

        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);

        userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
        userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
        userBatteryService.deleteByUid(userInfo.getUid());

        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(insuranceUserInfo)) {
            insuranceUserInfoService.deleteById(insuranceUserInfo);
        }

        //退押金解绑用户所属加盟商
        userInfoService.unBindUserFranchiseeId(userInfo.getUid());

        return Triple.of(true, "", null);
    }

    /**
     * 电池免押退押金
     */
    @Deprecated
    private Triple<Boolean, String, Object> handleBatteryFreeDepositRefundOrder(UserBatteryDeposit userBatteryDeposit, EleRefundOrder eleRefundOrder, EleRefundOrder eleRefundOrderUpdate, UserInfo userInfo) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("REFUND ORDER ERROR! not found pxzConfig,uid={}", userInfo.getUid());
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }

        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("REFUND ORDER ERROR! not found freeDepositOrder,uid={}", userInfo.getUid());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> testQuery = new PxzCommonRequest<>();
        testQuery.setAesSecret(pxzConfig.getAesKey());
        testQuery.setDateTime(System.currentTimeMillis());
        testQuery.setSessionId(userBatteryDeposit.getOrderId());
        testQuery.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark("电池押金解冻");
        queryRequest.setTransId(freeDepositOrder.getOrderId());
        testQuery.setData(queryRequest);

        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzUnfreezeDepositCommonRsp = null;

        try {
            pxzUnfreezeDepositCommonRsp = pxzDepositService.unfreezeDeposit(testQuery);
        } catch (Exception e) {
            log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100401", "免押解冻调用失败！");
        }

        if (Objects.isNull(pxzUnfreezeDepositCommonRsp)) {
            log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (!pxzUnfreezeDepositCommonRsp.isSuccess()) {
            log.error("REFUND ORDER ERROR! unfreeDepositOrder fail! rsp is null! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", pxzUnfreezeDepositCommonRsp.getRespDesc());
        }

        if(Objects.equals(pxzUnfreezeDepositCommonRsp.getData().getAuthStatus(),FreeDepositOrder.AUTH_UN_FROZEN)){
            //更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositOrderService.update(freeDepositOrderUpdate);

            //更新退款订单
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);

            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);

            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
            userBatteryService.deleteByUid(userInfo.getUid());

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
            }
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
        }

        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);

        //更新退款订单
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);

        return Triple.of(true, "", "退款中，请稍后");
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

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("DEPOSIT ERROR! not found userBatteryDeposit,uid={}", uid);
            return R.ok();
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("DEPOSIT ERROR! not found eleDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return R.ok();
        }

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
    
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(),
                UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.error("BATTERY DEPOSIT REFUND ERROR! disable member card is reviewing,uid={}", uid);
            return R.fail("ELECTRICITY.100003", "停卡正在审核中");
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
    
            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());

            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());

            userBatteryService.deleteByUid(userInfo.getUid());

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
            }

            userInfoService.unBindUserFranchiseeId(uid);

            //生成后台操作记录
            EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder()
                    .operateModel(EleUserOperateRecord.DEPOSIT_MODEL)
                    .operateContent(EleUserOperateRecord.REFUND_DEPOSIT_CONTENT)
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
    
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());

                userBatteryService.deleteByUid(userInfo.getUid());

                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(uid);
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }

                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());

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
        List<EleRefundOrderVO> eleRefundOrderVOS = eleRefundOrderMapper.queryList(eleRefundQuery);
        if(CollectionUtils.isEmpty(eleRefundOrderVOS)) {
            return R.ok(new ArrayList<>());
        }

        eleRefundOrderVOS.forEach(item -> {
            if(!Objects.equals(item.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }

            FreeDepositAlipayHistory freeDepositAlipayHistory = freeDepositAlipayHistoryService.queryByOrderId(item.getOrderId());
            if(Objects.isNull(freeDepositAlipayHistory)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }

            item.setIsFreeDepositAliPay(true);
        });
        return R.ok();
    }

    @Override
    public List<EleRefundOrderVO> selectCarRefundPageList(EleRefundQuery eleRefundQuery) {
        List<EleRefundOrderVO> eleRefundOrderVOS = eleRefundOrderMapper.selectCarRefundPageList(eleRefundQuery);
        if(CollectionUtils.isEmpty(eleRefundOrderVOS)) {
            return new ArrayList<>();
        }

        eleRefundOrderVOS.forEach(item -> {
            if(!Objects.equals(item.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }

            FreeDepositAlipayHistory freeDepositAlipayHistory = freeDepositAlipayHistoryService.queryByOrderId(item.getOrderId());
            if(Objects.isNull(freeDepositAlipayHistory)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }

            item.setIsFreeDepositAliPay(true);
        });
        return eleRefundOrderVOS;
    }

    @Override
    public Integer selectCarRefundPageCount(EleRefundQuery eleRefundQuery) {
        return eleRefundOrderMapper.selectCarRefundPageCount(eleRefundQuery);
    }

    @Override
    public Integer queryCountByOrderId(String orderId, Integer refundOrderType) {
        return eleRefundOrderMapper.selectCount(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_AGREE_REFUND, EleRefundOrder.STATUS_REFUND, EleRefundOrder.STATUS_SUCCESS).eq(EleRefundOrder::getRefundOrderType, refundOrderType));
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
    public BigDecimal queryTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType,
            List<Long> franchiseeIds, Integer payType) {
        return Optional.ofNullable(eleRefundOrderMapper
                .queryTurnOverByTime(tenantId, todayStartTime, refundOrderType, franchiseeIds, payType))
                .orElse(BigDecimal.valueOf(0));
    }

    @Override
    public BigDecimal queryCarRefundTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType,
            List<Long> franchiseeIds, Integer payType) {
        return Optional.ofNullable(eleRefundOrderMapper
                .queryCarRefundTurnOverByTime(tenantId, todayStartTime, refundOrderType, franchiseeIds, payType))
                .orElse(BigDecimal.valueOf(0));
    }

    @Override
    public Long queryRefundTime(String orderId, Integer refundOrderType) {
        return eleRefundOrderMapper.queryRefundTime(orderId, refundOrderType);
    }

    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(3) + uid +
                RandomUtil.randomNumbers(2);
    }

    @Override
    public boolean checkDepositOrderIsRefund(String orderId, Integer refundOrderType) {
        EleRefundOrder eleRefundOrder = this.eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId)
                        .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_SUCCESS, EleRefundOrder.STATUS_REFUND)
                        .eq(EleRefundOrder::getRefundOrderType, refundOrderType));
        if (!Objects.isNull(eleRefundOrder)) {
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
