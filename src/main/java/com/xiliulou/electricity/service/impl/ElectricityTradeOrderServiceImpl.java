package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.mapper.ElectricityTradeOrderMapper;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.pay.weixin.entity.PayOrder;
import com.xiliulou.pay.weixin.pay.PayAdapterHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;

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
        electricityTradeOrder.setOrderNo(electricityMemberCardOrder.getOrderNo());
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

        PayOrder payOrder = new PayOrder();
        payOrder.setAppId(electricityPayParams.getAppId());
        payOrder.setAppSecret(electricityPayParams.getAppSecret());
        payOrder.setMchId(electricityPayParams.getMchId());
        payOrder.setPaternerKey(electricityPayParams.getPaternerKey());
        payOrder.setBody("换电卡:" + electricityMemberCardOrder.getOrderNo());
        payOrder.setChannelId(PayOrder.CHANNEL_ID_WX_PRO);
        payOrder.setOpenId(openId);
        payOrder.setOutTradeNo(electricityTradeOrder.getTradeOrderNo());
        payOrder.setSpbillCreateIp(ip);
        payOrder.setTotalFee(electricityMemberCardOrder.getPayAmount().multiply(new BigDecimal(100)).longValue());
        //订单有效期为三分钟
        payOrder.setTimeExpire(DateUtil.format(new Date(System.currentTimeMillis() + (3 * 60 * 1000L)),
                "yyyyMMddHHmmss"));

        return payAdapterHandler.adaptAndPay(payOrder);

    }


}
