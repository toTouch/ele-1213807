package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.CommonPayOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.ElectricityTradeOrderMapper;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.pay.weixin.entity.PayOrder;
import com.xiliulou.pay.weixin.entity.WeiXinPayNotify;
import com.xiliulou.pay.weixin.pay.PayAdapterHandler;
import com.xiliulou.pay.weixin.refund.RefundAdapterHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-03 11:34
 **/
@Service
@Slf4j
public class ElectricityTradeOrderServiceImpl extends
        ServiceImpl<ElectricityTradeOrderMapper, ElectricityTradeOrder> implements ElectricityTradeOrderService {
    @Autowired
    PayAdapterHandler payAdapterHandler;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    ElectricityMemberCardOrderMapper electricityMemberCardOrderMapper;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    EleDepositOrderService eleDepositOrderService;



    /**
     * 创建并获取支付参数
     *
     * @param electricityMemberCardOrder
     * @return
     */
    @Override
    public Pair<Boolean, Object> createTradeOrderAndGetPayParams(ElectricityMemberCardOrder electricityMemberCardOrder,
                                                                 ElectricityPayParams electricityPayParams,
                                                                 String openId,
                                                                 HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
        electricityTradeOrder.setOrderNo(electricityMemberCardOrder.getOrderId());
        electricityTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()));
        electricityTradeOrder.setClientId(ip);
        electricityTradeOrder.setCreateTime(System.currentTimeMillis());
        electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrder.setOrderType(ElectricityTradeOrder.ORDER_TYPE_MEMBER_CARD);
        electricityTradeOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
        electricityTradeOrder.setTotalFee(electricityMemberCardOrder.getPayAmount());
        electricityTradeOrder.setUid(electricityMemberCardOrder.getUid());
        baseMapper.insert(electricityTradeOrder);
        //支付
        com.xiliulou.pay.weixin.entity.PayOrder payOrder = new com.xiliulou.pay.weixin.entity.PayOrder();
        payOrder.setAppId(electricityPayParams.getAppId());
        payOrder.setAppSecret(electricityPayParams.getAppSecret());
        payOrder.setMchId(electricityPayParams.getMchId());
        payOrder.setPaternerKey(electricityPayParams.getPaternerKey());
        payOrder.setBody("换电卡:" + electricityMemberCardOrder.getOrderId());
        payOrder.setChannelId(com.xiliulou.pay.weixin.entity.PayOrder.CHANNEL_ID_WX_PRO);
        payOrder.setOpenId(openId);
        payOrder.setOutTradeNo(electricityTradeOrder.getTradeOrderNo());
        payOrder.setSpbillCreateIp(ip);
        payOrder.setTotalFee(electricityMemberCardOrder.getPayAmount().multiply(new BigDecimal(100)).longValue());
        //订单有效期为三分钟
        payOrder.setTimeExpire(3 * 60 * 1000L);
        return payAdapterHandler.adaptAndPay(payOrder);

    }

    /**
     * 处理  回调
     *
     * @param weiXinPayNotify
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pair<Boolean, Object> notifyMemberOrder(WeiXinPayNotify weiXinPayNotify) {
        String tradeOrderNo = weiXinPayNotify.getOutTradeNo();

        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderMapper.selectByOrderNo(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(electricityMemberCardOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_MEMBER_CARD_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }
        if (!ObjectUtil.equal(ElectricityMemberCardOrder.STATUS_INIT, electricityMemberCardOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_MEMBER_CARD_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "套餐订单已处理!");
        }
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer memberOrderStatus = ElectricityMemberCardOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(weiXinPayNotify.getReturnCode()) && ObjectUtil.equal("SUCCESS", weiXinPayNotify.getReturnCode())) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            memberOrderStatus = ElectricityMemberCardOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + weiXinPayNotify.getOutTradeNo());
        }
        UserInfo userInfo = userInfoService.selectUserByUid(electricityMemberCardOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", electricityMemberCardOrder.getUid(), weiXinPayNotify.getOutTradeNo());
            return Pair.of(false, "未找到用户信息!");
        }
        log.info("NOTIFY ELECTRICITYMEMBERCARDORDER:{}", electricityMemberCardOrder);
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setId(userInfo.getId());
        Long memberCardExpireTime = System.currentTimeMillis() +
                electricityMemberCardOrder.getValidDays() * (24 * 60 * 60 * 1000L);
        userInfoUpdate.setMemberCardExpireTime(memberCardExpireTime);
        userInfoUpdate.setRemainingNumber(electricityMemberCardOrder.getMaxUseCount());
        userInfoUpdate.setCardType(electricityMemberCardOrder.getMemberCardType());
        userInfoUpdate.setUpdateTime(System.currentTimeMillis());
        log.info("NOTIFY info USERINFO:{}", userInfoUpdate);
        userInfoService.updateById(userInfoUpdate);
        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        baseMapper.updateById(electricityTradeOrderUpdate);
        ElectricityMemberCardOrder electricityMemberCardOrderUpdate = new ElectricityMemberCardOrder();
        electricityMemberCardOrderUpdate.setId(electricityMemberCardOrder.getId());
        electricityMemberCardOrderUpdate.setStatus(memberOrderStatus);
        electricityMemberCardOrderUpdate.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrderMapper.updateById(electricityMemberCardOrderUpdate);


        return Pair.of(result, null);
    }

    @Override
    public Pair<Boolean, Object> commonCreateTradeOrderAndGetPayParams(CommonPayOrder commonOrder, ElectricityPayParams electricityPayParams, String openId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        ElectricityTradeOrder electricityTradeOrder = new ElectricityTradeOrder();
        electricityTradeOrder.setOrderNo(commonOrder.getOrderId());
        electricityTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()));
        electricityTradeOrder.setClientId(ip);
        electricityTradeOrder.setCreateTime(System.currentTimeMillis());
        electricityTradeOrder.setUpdateTime(System.currentTimeMillis());
        electricityTradeOrder.setOrderType(commonOrder.getOrderType());
        electricityTradeOrder.setStatus(ElectricityTradeOrder.STATUS_INIT);
        electricityTradeOrder.setTotalFee(commonOrder.getPayAmount());
        electricityTradeOrder.setUid(commonOrder.getUid());
        baseMapper.insert(electricityTradeOrder);

        //支付
        PayOrder payOrder = new PayOrder();
        payOrder.setAppId(electricityPayParams.getAppId());
        payOrder.setAppSecret(electricityPayParams.getAppSecret());
        payOrder.setMchId(electricityPayParams.getMchId());
        payOrder.setPaternerKey(electricityPayParams.getPaternerKey());
        payOrder.setBody("换电押金:" + commonOrder.getOrderId());
        payOrder.setChannelId(com.xiliulou.pay.weixin.entity.PayOrder.CHANNEL_ID_WX_PRO);
        payOrder.setOpenId(openId);
        payOrder.setOutTradeNo(electricityTradeOrder.getTradeOrderNo());
        payOrder.setSpbillCreateIp(ip);
        payOrder.setTotalFee(commonOrder.getPayAmount().multiply(new BigDecimal(100)).longValue());
        //订单有效期为三分钟
        payOrder.setTimeExpire(3 * 60 * 1000L);
        payOrder.setAttach(commonOrder.getAttach());
        return payAdapterHandler.adaptAndPay(payOrder);

    }

    @Override
    public Pair<Boolean, Object> notifyDepositOrder(WeiXinPayNotify weiXinPayNotify) {
        //支付订单
        String tradeOrderNo = weiXinPayNotify.getOutTradeNo();

        ElectricityTradeOrder electricityTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(electricityTradeOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(ElectricityTradeOrder.STATUS_INIT, electricityTradeOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(electricityTradeOrder.getOrderNo());
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "未找到订单!");
        }
        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", electricityTradeOrder.getOrderNo());
            return Pair.of(false, "押金订单已处理!");
        }
        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer depositOrderStatus = EleDepositOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(weiXinPayNotify.getReturnCode()) && ObjectUtil.equal("SUCCESS", weiXinPayNotify.getReturnCode())) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            depositOrderStatus = EleDepositOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + weiXinPayNotify.getOutTradeNo());
        }
        UserInfo userInfo = userInfoService.selectUserByUid(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleDepositOrder.getUid(), weiXinPayNotify.getOutTradeNo());
            return Pair.of(false, "未找到用户信息!");
        }

        if(Objects.equals(depositOrderStatus,EleDepositOrder.STATUS_SUCCESS)) {
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setId(userInfo.getId());
            userInfoUpdate.setServiceStatus(UserInfo.STATUS_IS_DEPOSIT);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoUpdate.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userInfoService.updateById(userInfoUpdate);
        }

        ElectricityTradeOrder electricityTradeOrderUpdate = new ElectricityTradeOrder();
        electricityTradeOrderUpdate.setId(electricityTradeOrder.getId());
        electricityTradeOrderUpdate.setStatus(tradeOrderStatus);
        electricityTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        baseMapper.updateById(electricityTradeOrderUpdate);
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(eleDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(depositOrderStatus);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());

        return Pair.of(result, null);
    }

    @Override
    public ElectricityTradeOrder selectTradeOrderByTradeOrderNo(String outTradeNo) {
        return baseMapper.selectTradeOrderByTradeOrderNo(outTradeNo);
    }
}
