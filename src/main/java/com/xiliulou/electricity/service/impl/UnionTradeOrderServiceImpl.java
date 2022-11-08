package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.core.json.JsonUtil;
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

    @Autowired
    EleDepositOrderService eleDepositOrderService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;

    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;

    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;


    @Override
    public WechatJsapiOrderResultDTO unionCreateTradeOrderAndGetPayParams(UnionPayOrder unionPayOrder, ElectricityPayParams electricityPayParams, String openId, HttpServletRequest request) throws WechatPayException {

        String ip = request.getRemoteAddr();
        UnionTradeOrder unionTradeOrder = new UnionTradeOrder();
        unionTradeOrder.setJsonOrderId(unionPayOrder.getJsonOrderId());
        unionTradeOrder.setJsonSingleFee(unionPayOrder.getJsonSingleFee());
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

        //回调参数
        String tradeOrderNo = callBackResource.getOutTradeNo();
        String tradeState = callBackResource.getTradeState();
        String transactionId = callBackResource.getTransactionId();

        UnionTradeOrder unionTradeOrder = baseMapper.selectTradeOrderByTradeOrderNo(tradeOrderNo);
        if (Objects.isNull(unionTradeOrder)) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单!");
        }
        if (ObjectUtil.notEqual(UnionTradeOrder.STATUS_INIT, unionTradeOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "交易订单已处理");
        }

        //处理保险订单
        String jsonOrderId = unionTradeOrder.getJsonOrderId();
        List<String> orderIdLIst = JsonUtil.fromJsonArray(jsonOrderId, String.class);
        if (Objects.isNull(orderIdLIst) || orderIdLIst.size()==0){
            log.error("NOTIFY_INSURANCE_UNION_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER TRADE_ORDER_NO:{}", tradeOrderNo);
            return Pair.of(false, "未找到交易订单");
        }

        //押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(orderIdLIst.get(0));
        if (ObjectUtil.isEmpty(eleDepositOrder)) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", orderIdLIst.get(0));
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleDepositOrder.STATUS_INIT, eleDepositOrder.getStatus())) {
            log.error("NOTIFY_DEPOSIT_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", orderIdLIst.get(0));
            return Pair.of(false, "押金订单已处理!");
        }

        //保险订单
        InsuranceOrder insuranceOrder = insuranceOrderService.queryByOrderId(orderIdLIst.get(1));
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", orderIdLIst.get(1));
            return Pair.of(false, "未找到订单!");
        }

        if (!ObjectUtil.equal(EleBatteryServiceFeeOrder.STATUS_INIT, insuranceOrder.getStatus())) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR , ELECTRICITY_DEPOSIT_ORDER  STATUS IS NOT INIT, ORDER_NO:{}", orderIdLIst.get(1));
            return Pair.of(false, "押金订单已处理!");
        }

        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByCache(insuranceOrder.getInsuranceId());
        if (ObjectUtil.isEmpty(insuranceOrder)) {
            log.error("NOTIFY_INSURANCE_ORDER ERROR ,NOT FOUND ELECTRICITY_DEPOSIT_ORDER ORDER_NO:{}", orderIdLIst.get(1));
            return Pair.of(false, "未找到订单!");
        }


        Integer tradeOrderStatus = ElectricityTradeOrder.STATUS_FAIL;
        Integer depositOrderStatus = EleDepositOrder.STATUS_FAIL;
        boolean result = false;
        if (StringUtils.isNotEmpty(tradeState) && ObjectUtil.equal("SUCCESS", tradeState)) {
            tradeOrderStatus = ElectricityTradeOrder.STATUS_SUCCESS;
            depositOrderStatus = EleDepositOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO:{}" + tradeOrderNo);
        }

        //用户
        UserInfo userInfo = userInfoService.selectUserByUid(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("NOTIFY  ERROR,NOT FOUND USERINFO,USERID:{},ORDER_NO:{}", eleDepositOrder.getUid(), tradeOrderNo);
            return Pair.of(false, "未找到用户信息!");
        }

        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
            return Pair.of(false, "未找到用户信息!");

        }

        //用户押金和保险
        if (Objects.equals(depositOrderStatus, EleDepositOrder.STATUS_SUCCESS)) {

            FranchiseeUserInfo franchiseeUserInfoUpdate = new FranchiseeUserInfo();
            franchiseeUserInfoUpdate.setId(franchiseeUserInfo.getId());
            franchiseeUserInfoUpdate.setServiceStatus(FranchiseeUserInfo.STATUS_IS_DEPOSIT);
            franchiseeUserInfoUpdate.setUpdateTime(System.currentTimeMillis());
            franchiseeUserInfoUpdate.setBatteryDeposit(eleDepositOrder.getPayAmount());
            franchiseeUserInfoUpdate.setOrderId(eleDepositOrder.getOrderId());
            franchiseeUserInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());

            franchiseeUserInfoUpdate.setModelType(eleDepositOrder.getModelType());

            if (Objects.equals(eleDepositOrder.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                franchiseeUserInfoUpdate.setBatteryType(eleDepositOrder.getBatteryType());
            }
            franchiseeUserInfoService.update(franchiseeUserInfoUpdate);

            InsuranceUserInfo updateOrAddInsuranceUserInfo = new InsuranceUserInfo();
            updateOrAddInsuranceUserInfo.setUid(userInfo.getUid());
            updateOrAddInsuranceUserInfo.setUpdateTime(System.currentTimeMillis());
            updateOrAddInsuranceUserInfo.setIsUse(InsuranceUserInfo.NOT_USE);
            updateOrAddInsuranceUserInfo.setInsuranceOrderId(insuranceOrder.getOrderId());
            updateOrAddInsuranceUserInfo.setInsuranceId(franchiseeInsurance.getId());
            updateOrAddInsuranceUserInfo.setInsuranceExpireTime(franchiseeInsurance.getValidDays() * ((24 * 60 * 60 * 1000L)));
            updateOrAddInsuranceUserInfo.setTenantId(insuranceOrder.getTenantId());
            updateOrAddInsuranceUserInfo.setForehead(franchiseeInsurance.getForehead());
            updateOrAddInsuranceUserInfo.setPremium(franchiseeInsurance.getPremium());
            updateOrAddInsuranceUserInfo.setFranchiseeId(franchiseeInsurance.getFranchiseeId());
            updateOrAddInsuranceUserInfo.setCreateTime(System.currentTimeMillis());

            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.isNull(insuranceUserInfo)) {
                insuranceUserInfoService.insert(updateOrAddInsuranceUserInfo);
            } else {
                insuranceUserInfoService.update(updateOrAddInsuranceUserInfo);
            }

        }

        //系统订单
        UnionTradeOrder unionTradeOrderUpdate=new UnionTradeOrder();
        unionTradeOrderUpdate.setId(unionTradeOrder.getId());
        unionTradeOrderUpdate.setStatus(tradeOrderStatus);
        unionTradeOrderUpdate.setUpdateTime(System.currentTimeMillis());
        unionTradeOrderUpdate.setChannelOrderNo(transactionId);
        baseMapper.updateById(unionTradeOrderUpdate);


        //押金订单
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(eleDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(depositOrderStatus);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleDepositOrderService.update(eleDepositOrderUpdate);

        //保险订单
        InsuranceOrder updateInsuranceOrder = new InsuranceOrder();
        updateInsuranceOrder.setId(insuranceOrder.getId());
        updateInsuranceOrder.setUpdateTime(System.currentTimeMillis());
        updateInsuranceOrder.setStatus(depositOrderStatus);
        insuranceOrderService.updateOrderStatusById(updateInsuranceOrder);
        return Pair.of(result, null);
    }
}
