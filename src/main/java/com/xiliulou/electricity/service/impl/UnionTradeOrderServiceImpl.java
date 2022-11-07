package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.ElectricityTradeOrderMapper;
import com.xiliulou.electricity.mapper.UnionTradeOrderMapper;
import com.xiliulou.electricity.service.*;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderCallBackResource;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.pay.weixinv3.query.WechatV3OrderQuery;
import com.xiliulou.pay.weixinv3.service.WechatV3JsapiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.HRP
 * @create: 2022-11-07 11:34
 **/
@Service
@Slf4j
public class UnionTradeOrderServiceImpl extends
        ServiceImpl<UnionTradeOrderMapper, UnionTradeOrder> implements UnionTradeOrderService {

    @Resource
    UnionTradeOrderMapper unionTradeOrderMapper;

    @Autowired
    WechatConfig wechatConfig;

    @Autowired
    WechatV3JsapiService wechatV3JsapiService;


    @Override
    public WechatJsapiOrderResultDTO unionCreateTradeOrderAndGetPayParams(UnionPayOrder unionPayOrder, ElectricityPayParams electricityPayParams, String openId, HttpServletRequest request) throws WechatPayException {

        String ip = request.getRemoteAddr();
        UnionTradeOrder unionTradeOrder = new UnionTradeOrder();
        unionTradeOrder.setJsonOrderId(unionPayOrder.getJsonOrderId());
        unionTradeOrder.setTradeOrderNo(String.valueOf(System.currentTimeMillis()).substring(2) + unionPayOrder.getUid() + RandomUtil.randomNumbers(6));
        unionTradeOrder.setClientId(ip);
        unionTradeOrder.setCreateTime(System.currentTimeMillis());
        unionTradeOrder.setUpdateTime(System.currentTimeMillis());
        unionTradeOrder.setJsonOrderType(unionPayOrder.getJsonOrderType());
        unionTradeOrder.setStatus(UnionTradeOrder.STATUS_INIT);
        unionTradeOrder.setTotalFee(unionPayOrder.getPayAmount());
        unionTradeOrder.setUid(unionPayOrder.getUid());
        unionTradeOrder.setTenantId(unionPayOrder.getTenantId());
        baseMapper.insert(unionTradeOrder);

        //支付参数
        WechatV3OrderQuery wechatV3OrderQuery = new WechatV3OrderQuery();
        wechatV3OrderQuery.setOrderId(unionTradeOrder.getTradeOrderNo());
        wechatV3OrderQuery.setTenantId(unionTradeOrder.getTenantId());
        wechatV3OrderQuery.setNotifyUrl(wechatConfig.getPayCallBackUrl() + unionTradeOrder.getTenantId());
        wechatV3OrderQuery.setExpireTime(System.currentTimeMillis() + 3600000);
        wechatV3OrderQuery.setOpenId(openId);
        wechatV3OrderQuery.setDescription(unionPayOrder.getDescription());
        wechatV3OrderQuery.setCurrency("CNY");
        wechatV3OrderQuery.setAttach(unionPayOrder.getAttach());
        wechatV3OrderQuery.setAmount(unionPayOrder.getPayAmount().multiply(new BigDecimal(100)).intValue());
        wechatV3OrderQuery.setAppid(electricityPayParams.getMerchantMinProAppId());
        log.info("wechatV3OrderQuery is -->{}", wechatV3OrderQuery);
        return wechatV3JsapiService.order(wechatV3OrderQuery);
    }

    @Override
    public Pair<Boolean, Object> notifyUnionDepositAndInsurance(WechatJsapiOrderCallBackResource callBackResource) {
        return null;
    }
}
