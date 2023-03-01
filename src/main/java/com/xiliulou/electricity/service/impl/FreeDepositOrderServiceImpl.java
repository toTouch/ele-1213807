package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.FreeDepositOrderMapper;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.*;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzAuthToPayOrderQueryRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzAuthToPayRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * (FreeDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2023-02-15 11:39:27
 */
@Service("freeDepositOrderService")
@Slf4j
public class FreeDepositOrderServiceImpl implements FreeDepositOrderService {

    private static final Integer REFUND_ORDER_LIMIT = 50;

    @Resource
    private FreeDepositOrderMapper freeDepositOrderMapper;

    @Autowired
    RedisService redisService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    ElectricityConfigService electricityConfigService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    PxzDepositService pxzDepositService;

    @Autowired
    PxzConfigService pxzConfigService;

    @Autowired
    UserBatteryDepositService userBatteryDepositService;

    @Autowired
    EleDepositOrderService eleDepositOrderService;

    @Autowired
    UserBatteryService userBatteryService;

    @Autowired
    ElectricityPayParamsService electricityPayParamsService;

    @Autowired
    UserOauthBindService userOauthBindService;

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    InsuranceOrderService insuranceOrderService;

    @Autowired
    UnionTradeOrderService unionTradeOrderService;

    @Autowired
    StoreService storeService;

    @Autowired
    ElectricityCarModelService electricityCarModelService;

    @Autowired
    CarDepositOrderService carDepositOrderService;

    @Autowired
    UserCarDepositService userCarDepositService;

    @Autowired
    UserCarService userCarService;

    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;

    @Autowired
    FreeDepositDataService freeDepositDataService;

    @Autowired
    TradeOrderService tradeOrderService;

    @Autowired
    EleRefundOrderService eleRefundOrderService;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;

    @Autowired
    UserCarMemberCardService userCarMemberCardService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositOrder queryByIdFromDB(Long id) {
        return this.freeDepositOrderMapper.queryById(id);
    }

    @Override
    public FreeDepositOrder selectByOrderId(String orderId) {
        return this.freeDepositOrderMapper.selectOne(new LambdaQueryWrapper<FreeDepositOrder>().eq(FreeDepositOrder::getOrderId, orderId));
    }

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Override
    public List<FreeDepositOrder> selectByPage(FreeDepositOrderQuery query) {
        List<FreeDepositOrder> freeDepositOrders = this.freeDepositOrderMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(freeDepositOrders)) {
            return Collections.EMPTY_LIST;
        }

        return freeDepositOrders;
    }

    @Override
    public Integer selectByPageCount(FreeDepositOrderQuery query) {
        return this.freeDepositOrderMapper.selectByPageCount(query);
    }

    /**
     * 新增数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreeDepositOrder insert(FreeDepositOrder freeDepositOrder) {
        this.freeDepositOrderMapper.insertOne(freeDepositOrder);
        return freeDepositOrder;
    }

    /**
     * 修改数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FreeDepositOrder freeDepositOrder) {
        return this.freeDepositOrderMapper.update(freeDepositOrder);
    }
    
    @Override
    public Triple<Boolean, String, Object> freeDepositAuthToPay(String orderId, BigDecimal payTransAmt) {
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("FREE DEPOSIT ERROR! not found freeDepositOrder,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        if (!Objects.equals(freeDepositOrder.getPayStatus(), FreeDepositOrder.PAY_STATUS_INIT)) {
            log.error("FREE DEPOSIT ERROR! freeDepositOrder already AuthToPay,orderId={}", orderId);
            return Triple.of(false, "100412", "免押订单已授权支付");
        }
        
        if (Objects.isNull(payTransAmt) || payTransAmt.compareTo(BigDecimal.valueOf(freeDepositOrder.getTransAmt())) > 0) {
            log.error("FREE DEPOSIT ERROR! payTransAmt is illegal,orderId={}", orderId);
            return Triple.of(false, "ELECTRICITY.0007", "退款金额不能大于支付金额!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(freeDepositOrder.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", freeDepositOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT ERROR! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("FREE DEPOSIT ERROR! user not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils
                .isBlank(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }
        
        PxzCommonRequest<PxzFreeDepositAuthToPayRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositAuthToPayRequest request = new PxzFreeDepositAuthToPayRequest();
        request.setPayNo(freeDepositOrder.getOrderId());
        request.setTransId(freeDepositOrder.getOrderId());
        request.setAuthNo(freeDepositOrder.getAuthNo());
        request.setTransAmt(payTransAmt.multiply(BigDecimal.valueOf(100)).longValue());
        query.setData(request);
        
        PxzCommonRsp<PxzAuthToPayRsp> pxzAuthToPayRspPxzCommonRsp = null;
        try {
            pxzAuthToPayRspPxzCommonRsp = pxzDepositService.authToPay(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder authToPay fail! uid={},orderId={}", userInfo.getUid(),
                    freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100411", "授权支付调用失败！");
        }
        
        if (Objects.isNull(pxzAuthToPayRspPxzCommonRsp)) {
            log.error("Pxz ERROR! freeDepositOrder authToPay fail! rsp is null! uid={},orderId={}", userInfo.getUid(),
                    freeDepositOrder.getOrderId());
            return Triple.of(false, "100411", "授权支付调用失败！");
        }
        
        if (!pxzAuthToPayRspPxzCommonRsp.isSuccess()) {
            return Triple.of(false, "100411", pxzAuthToPayRspPxzCommonRsp.getRespDesc());
        }
        
        //更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(pxzAuthToPayRspPxzCommonRsp.getData().getOrderStatus());
        freeDepositOrderUpdate.setPayTransAmt(payTransAmt.doubleValue());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);
        
        return Triple.of(true, "", "授权转支付交易处理中！");
    }
    
    @Override
    public Triple<Boolean, String, Object> selectFreeDepositAuthToPay(String orderId) {
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("FREE DEPOSIT ERROR! not found freeDepositOrder,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }
        
        PxzCommonRequest<PxzFreeDepositAuthToPayOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositAuthToPayOrderQueryRequest queryRequest = new PxzFreeDepositAuthToPayOrderQueryRequest();
        queryRequest.setPayNo(freeDepositOrder.getOrderId());
        queryRequest.setAuthNo(freeDepositOrder.getAuthNo());
        query.setData(queryRequest);
        
        PxzCommonRsp<PxzAuthToPayOrderQueryRsp> pxzAuthToPayOrderQueryRspPxzCommonRsp = null;
        
        try {
            pxzAuthToPayOrderQueryRspPxzCommonRsp = pxzDepositService.authToPayOrderQuery(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! query freeDepositOrder authToPay fail! orderId={}", freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100411", "授权支付调用失败！");
        }
        
        if (Objects.isNull(pxzAuthToPayOrderQueryRspPxzCommonRsp)) {
            log.error("Pxz ERROR! query freeDepositOrder authToPay fail! rsp is null! orderId={}",
                    freeDepositOrder.getOrderId());
            return Triple.of(false, "100411", "授权支付调用失败！");
        }
        
        if (!pxzAuthToPayOrderQueryRspPxzCommonRsp.isSuccess()) {
            return Triple.of(false, "100411", pxzAuthToPayOrderQueryRspPxzCommonRsp.getRespDesc());
        }
        
        //更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(pxzAuthToPayOrderQueryRspPxzCommonRsp.getData().getOrderStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);
        
        return Triple.of(true, "", pxzAuthToPayOrderQueryRspPxzCommonRsp.getData());
    }

    /**
     * 查询免押订单状态
     */
    @Override
    public Triple<Boolean, String, Object> selectFreeDepositOrderStatus(String orderId) {

        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("FREE DEPOSIT ERROR! not found freeDepositOrder,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }

        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderId);
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(freeDepositOrder.getOrderId());
        query.setData(request);


        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! uid={},orderId={}", orderId, e);
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (Objects.isNull(pxzQueryOrderRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", orderId);
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (!pxzQueryOrderRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzQueryOrderRsp.getRespDesc());
        }

        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        if (Objects.isNull(queryOrderRspData)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! queryOrderRspData is null! uid={},orderId={}", orderId);
            return Triple.of(false, "100402", "免押查询失败！");
        }

        return Triple.of(true, "", queryOrderRspData);
    }

    @Override
    public Triple<Boolean, String, Object> selectFreeDepositOrderStatus(FreeDepositOrder freeDepositOrder) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(freeDepositOrder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("Pxz ERROR! pxzConfig is null! uid={},orderId={}", freeDepositOrder.getUid(), freeDepositOrder.getOrderId());
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }

        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(freeDepositOrder.getOrderId());
        query.setData(request);


        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! uid={},orderId={}", freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (Objects.isNull(pxzQueryOrderRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", freeDepositOrder.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (!pxzQueryOrderRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzQueryOrderRsp.getRespDesc());
        }

        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        if (Objects.isNull(queryOrderRspData)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! queryOrderRspData is null! uid={},orderId={}", freeDepositOrder.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        return Triple.of(true, "", queryOrderRspData);
    }

    /**
     * 生成电池免押订单
     *
     * @param freeBatteryDepositQuery
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> freeBatteryDepositOrder(FreeBatteryDepositQuery freeBatteryDepositQuery) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        //获取租户免押次数
        FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            log.error("FREE DEPOSIT ERROR! freeDepositData is null,uid={}", uid);
            return Triple.of(false, "100404", "免押次数未充值，请联系管理员");
        }

        if (freeDepositData.getFreeDepositCapacity() <= NumberConstant.ZERO) {
            log.error("FREE DEPOSIT ERROR! freeDepositCapacity already run out,uid={}", uid);
            return Triple.of(false, "100405", "免押次数已用完，请联系管理员");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }

        Triple<Boolean, String, Object> checkUserCanFreeDepositResult = checkUserCanFreeBatteryDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkUserCanFreeDepositResult.getLeft())) {
            return checkUserCanFreeDepositResult;
        }

        Triple<Boolean, String, Object> generateDepositOrderResult = generateBatteryDepositOrder(userInfo, freeBatteryDepositQuery);
        if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
            return generateDepositOrderResult;
        }
        EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();

        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder()
                .uid(uid)
                .authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE)
                .idCard(freeBatteryDepositQuery.getIdCard())
                .orderId(eleDepositOrder.getOrderId())
                .phone(freeBatteryDepositQuery.getPhoneNumber())
                .realName(freeBatteryDepositQuery.getRealName())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .payStatus(FreeDepositOrder.AUTH_INIT)
                .tenantId(TenantContextHolder.getTenantId())
                .transAmt(eleDepositOrder.getPayAmount().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO)
                .depositType(FreeDepositOrder.DEPOSIT_TYPE_BATTERY).build();

        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeBatteryDepositQuery.getPhoneNumber());
        request.setSubject("电池免押");
        request.setRealName(freeBatteryDepositQuery.getRealName());
        request.setIdNumber(freeBatteryDepositQuery.getIdCard());
        request.setTransId(freeDepositOrder.getOrderId());
        request.setTransAmt(BigDecimal.valueOf(freeDepositOrder.getTransAmt()).multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);


        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder fail! uid={},orderId={}", uid, freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (Objects.isNull(callPxzRsp)) {
            log.error("Pxz ERROR! freeDepositOrder fail! rsp is null! uid={},orderId={}", uid, freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (!callPxzRsp.isSuccess()) {
            return Triple.of(false, "100401", callPxzRsp.getRespDesc());
        }

        insert(freeDepositOrder);
        eleDepositOrderService.insert(eleDepositOrder);

        //绑定免押订单
        UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
        userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
        userBatteryDeposit.setUid(uid);
        userBatteryDeposit.setDid(eleDepositOrder.getId());
        userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
        userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
        userBatteryDeposit.setCreateTime(System.currentTimeMillis());
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);

        return Triple.of(true, null, callPxzRsp.getData());
    }


    /**
     * 生成车辆免押订单
     *
     * @param freeCarDepositQuery
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> freeCarDepositOrder(FreeCarDepositQuery freeCarDepositQuery) {

        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        //获取租户免押次数
        FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            log.error("FREE DEPOSIT ERROR! freeDepositData is null,uid={}", uid);
            return Triple.of(false, "100404", "免押次数未充值，请联系管理员");
        }

        if (freeDepositData.getFreeDepositCapacity() <= NumberConstant.ZERO) {
            log.error("FREE DEPOSIT ERROR! freeDepositCapacity already run out,uid={}", uid);
            return Triple.of(false, "100405", "免押次数已用完，请联系管理员");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            return Triple.of(false, "100400", "免押功能未配置相关信息！请联系客服处理");
        }

        Triple<Boolean, String, Object> checkUserCanFreeCarDepositResult = checkUserCanFreeCarDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkUserCanFreeCarDepositResult.getLeft())) {
            return checkUserCanFreeCarDepositResult;
        }

        Triple<Boolean, String, Object> generateCarDepositOrderResult = generateCarDepositOrder(userInfo, freeCarDepositQuery);
        if (Boolean.FALSE.equals(generateCarDepositOrderResult.getLeft())) {
            return generateCarDepositOrderResult;
        }

        CarDepositOrder carDepositOrder = (CarDepositOrder) generateCarDepositOrderResult.getRight();

        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder()
                .uid(uid)
                .authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE)
                .idCard(freeCarDepositQuery.getIdCard())
                .orderId(carDepositOrder.getOrderId())
                .phone(freeCarDepositQuery.getPhoneNumber())
                .realName(freeCarDepositQuery.getRealName())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .payStatus(FreeDepositOrder.AUTH_INIT)
                .tenantId(TenantContextHolder.getTenantId())
                .transAmt(carDepositOrder.getPayAmount().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO)
                .depositType(FreeDepositOrder.DEPOSIT_TYPE_CAR).build();

        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeCarDepositQuery.getPhoneNumber());
        request.setSubject("车辆免押");
        request.setRealName(freeCarDepositQuery.getRealName());
        request.setIdNumber(freeCarDepositQuery.getIdCard());
        request.setTransId(freeDepositOrder.getOrderId());
        request.setTransAmt(BigDecimal.valueOf(freeDepositOrder.getTransAmt()).multiply(BigDecimal.valueOf(100)).intValue());
        query.setData(request);


        PxzCommonRsp<String> callPxzRsp = null;
        try {
            callPxzRsp = pxzDepositService.freeDepositOrder(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! freeDepositOrder fail! uid={},orderId={}", uid, freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (Objects.isNull(callPxzRsp)) {
            log.error("Pxz ERROR! freeDepositOrder fail! rsp is null! uid={},orderId={}", uid, freeDepositOrder.getOrderId());
            return Triple.of(false, "100401", "免押调用失败！");
        }

        if (!callPxzRsp.isSuccess()) {
            return Triple.of(false, "100401", callPxzRsp.getRespDesc());
        }

        insert(freeDepositOrder);
        carDepositOrderService.insert(carDepositOrder);

        //绑定免押订单

        UserCarDeposit userCarDeposit = new UserCarDeposit();
        userCarDeposit.setOrderId(carDepositOrder.getOrderId());
        userCarDeposit.setUid(userInfo.getUid());
        userCarDeposit.setDid(carDepositOrder.getId());
        userCarDeposit.setCarDeposit(carDepositOrder.getPayAmount());
        userCarDeposit.setDepositType(UserCarDeposit.DEPOSIT_TYPE_FREE);
        userCarDeposit.setDelFlag(UserCarDeposit.DEL_NORMAL);
        userCarDeposit.setApplyDepositTime(System.currentTimeMillis());
        userCarDeposit.setCreateTime(System.currentTimeMillis());
        userCarDeposit.setUpdateTime(System.currentTimeMillis());
        userCarDepositService.insertOrUpdate(userCarDeposit);

        return Triple.of(true, null, callPxzRsp.getData());
    }

    /**
     * 查询电池免押订单状态
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> acquireUserFreeBatteryDepositStatus() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FREE_BATTERY_DEPOSIT_LOCK_KEY + uid, "1", 2 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("FREE DEPOSIT ERROR! not found pxzConfig,uid={}", uid);
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("FREE DEPOSIT ERROR! not found userBatteryDeposit,uid={}", uid);
            return Triple.of(true, "", "");
        }

        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.error("FREE DEPOSIT ERROR! not found eleDepositOrder! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        FreeDepositOrder freeDepositOrder = this.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("FREE DEPOSIT ERROR! not found freeDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(userBatteryDeposit.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(freeDepositOrder.getOrderId());
        query.setData(request);


        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! uid={},orderId={}", uid, userBatteryDeposit.getOrderId(), e);
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (Objects.isNull(pxzQueryOrderRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (!pxzQueryOrderRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzQueryOrderRsp.getRespDesc());
        }

        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        if (Objects.isNull(queryOrderRspData)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! queryOrderRspData is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        //更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthNo(queryOrderRspData.getAuthNo());
        freeDepositOrderUpdate.setAuthStatus(queryOrderRspData.getAuthStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);

        //冻结成功
        if (!Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN) && Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {

            //扣减免押次数
            FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId());
            FreeDepositData freeDepositDataUpdate = new FreeDepositData();
            freeDepositDataUpdate.setId(freeDepositData.getId());
            freeDepositDataUpdate.setFreeDepositCapacity(freeDepositData.getFreeDepositCapacity() - 1);
            freeDepositDataUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositDataService.update(freeDepositDataUpdate);


            //更新押金订单状态
            EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
            eleDepositOrderUpdate.setId(eleDepositOrder.getId());
            eleDepositOrderUpdate.setStatus(EleDepositOrder.STATUS_SUCCESS);
            eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleDepositOrderService.update(eleDepositOrderUpdate);

            //绑定加盟商、更新押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            userInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);

            //绑定电池型号
            UserBattery userBattery = new UserBattery();
            userBattery.setUid(uid);
            userBattery.setBatteryType(eleDepositOrder.getBatteryType());
            userBattery.setUpdateTime(System.currentTimeMillis());
            userBatteryService.insertOrUpdate(userBattery);
        }

        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());
        freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
        freeDepositUserInfoVo.setBatteryDepositAuthStatus(queryOrderRspData.getAuthStatus());

        return Triple.of(true, null, freeDepositUserInfoVo);
    }

    /**
     * 查询租车免押订单状态
     */
    @Override
    public Triple<Boolean, String, Object> acquireFreeCarDepositStatus() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FREE_CAR_DEPOSIT_LOCK_KEY + uid, "1", 2 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.error("FREE DEPOSIT ERROR! not found pxzConfig,uid={}", uid);
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("FREE DEPOSIT ERROR! not found userCarDeposit,uid={}", uid);
            return Triple.of(true, "", "");
        }

        CarDepositOrder carDepositOrder = carDepositOrderService.selectByOrderId(userCarDeposit.getOrderId());
        if (Objects.isNull(carDepositOrder)) {
            log.error("FREE DEPOSIT ERROR! not found carDepositOrder! uid={},orderId={}", uid, userCarDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }

        FreeDepositOrder freeDepositOrder = this.selectByOrderId(carDepositOrder.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.error("FREE DEPOSIT ERROR! not found freeDepositOrder,uid={},orderId={}", uid, carDepositOrder.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }

        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(carDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());

        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(freeDepositOrder.getOrderId());
        query.setData(request);


        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! uid={},orderId={}", uid, carDepositOrder.getOrderId(), e);
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (Objects.isNull(pxzQueryOrderRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", uid, carDepositOrder.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        if (!pxzQueryOrderRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzQueryOrderRsp.getRespDesc());
        }

        PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
        if (Objects.isNull(queryOrderRspData)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! queryOrderRspData is null! uid={},orderId={}", uid, carDepositOrder.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }

        //更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthNo(queryOrderRspData.getAuthNo());
        freeDepositOrderUpdate.setAuthStatus(queryOrderRspData.getAuthStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);

        //冻结成功
        if (!Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN) && Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            //扣减免押次数
            FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId());
            FreeDepositData freeDepositDataUpdate = new FreeDepositData();
            freeDepositDataUpdate.setId(freeDepositData.getId());
            freeDepositDataUpdate.setFreeDepositCapacity(freeDepositData.getFreeDepositCapacity() - 1);
            freeDepositDataUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositDataService.update(freeDepositDataUpdate);

            //更新押金订单状态
            CarDepositOrder carDepositOrderUpdate = new CarDepositOrder();
            carDepositOrderUpdate.setId(carDepositOrder.getId());
            carDepositOrderUpdate.setStatus(EleDepositOrder.STATUS_SUCCESS);
            carDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            carDepositOrderService.update(carDepositOrderUpdate);

            //绑定加盟商、更新押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            userInfoUpdate.setFranchiseeId(carDepositOrder.getFranchiseeId());
            userInfoUpdate.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);

            //绑定车辆型号
            UserCar userCar = new UserCar();
            userCar.setUid(uid);
            userCar.setCarModel(carDepositOrder.getCarModelId());
            userCar.setUpdateTime(System.currentTimeMillis());
            userCarService.insertOrUpdate(userCar);
        }

        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());
        freeDepositUserInfoVo.setApplyCarDepositTime(userCarDeposit.getApplyDepositTime());
        freeDepositUserInfoVo.setCarDepositAuthStatus(queryOrderRspData.getAuthStatus());

        return Triple.of(true, null, freeDepositUserInfoVo);
    }

    /**
     * 电池免押 套餐、保险混合支付
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> freeBatteryDepositHybridOrder(FreeBatteryDepositHybridOrderQuery query, HttpServletRequest request) {

        Integer tenantId = TenantContextHolder.getTenantId();

        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FREE_DEPOSIT_MEMBERCARD_LOCK_KEY + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found userInfo,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT HYBRID ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("FREE DEPOSIT HYBRID ERROR!not found electricityPayParams,uid={}", uid);
            return Triple.of(false, "100234", "未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("FREE DEPOSIT HYBRID ERROR!not found userOauthBind,uid={}", uid);
            return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
        }

        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("FFREE DEPOSIT HYBRID ERROR! not found userBatteryDeposit,uid={}", uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }

        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> payAmountList = new ArrayList<>();
        BigDecimal totalPayAmount = BigDecimal.valueOf(0);


        FreeDepositOrder freeDepositOrder = this.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            log.error("FFREE DEPOSIT HYBRID ERROR! freeDepositOrder is anomaly,uid={}", uid);
            return Triple.of(false, "100402", "免押失败！");
        }

        //处理电池套餐相关
        Triple<Boolean, String, Object> rentBatteryMemberCardTriple = electricityMemberCardOrderService.handleRentBatteryMemberCard(
                query.getProductKey(), query.getDeviceName(), query.getUserCouponId(), query.getMemberCardId(), userInfo.getFranchiseeId(), userInfo);
        if (Boolean.FALSE.equals(rentBatteryMemberCardTriple.getLeft())) {
            return rentBatteryMemberCardTriple;
        }

        //处理保险套餐相关
        Triple<Boolean, String, Object> rentBatteryInsuranceTriple = insuranceOrderService.handleRentBatteryInsurance(query.getInsuranceId(), userInfo);
        if (Boolean.FALSE.equals(rentBatteryInsuranceTriple.getLeft())) {
            return rentBatteryInsuranceTriple;
        }

        //保存保险订单
        if (Objects.nonNull(rentBatteryInsuranceTriple.getRight())) {
            InsuranceOrder insuranceOrder = (InsuranceOrder) rentBatteryInsuranceTriple.getRight();
            insuranceOrderService.insert(insuranceOrder);

            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            payAmountList.add(insuranceOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(insuranceOrder.getPayAmount());
        }

        //保存套餐订单
        if (Objects.nonNull(rentBatteryMemberCardTriple.getRight())) {
            ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) rentBatteryMemberCardTriple.getRight();
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);

            orderList.add(electricityMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
            payAmountList.add(electricityMemberCardOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(electricityMemberCardOrder.getPayAmount());
        }

        //处理支付0元场景
        if (totalPayAmount.doubleValue() <= NumberConstant.ZERO) {
            Triple<Boolean, String, Object> result = tradeOrderService.handleTotalAmountZero(userInfo, orderList, orderTypeList);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return result;
            }

            return Triple.of(true, "", null);
        }

        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(payAmountList))
                    .payAmount(totalPayAmount)
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)
                    .description("免押租电")
                    .uid(uid).build();
            WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, null, resultDTO);
        } catch (WechatPayException e) {
            log.error("FREE DEPOSIT HYBRID ERROR! wechat v3 order  error! uid={}", uid, e);
        }

        return Triple.of(false, "ELECTRICITY.0099", "下单失败");
    }

    /**
     * 车辆免押 套餐混合支付
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> freeCarDepositHybridOrder(FreeCarDepositHybridOrderQuery query, HttpServletRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();

        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FREE_DEPOSIT_MEMBERCARD_LOCK_KEY + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("FREE DEPOSIT HYBRID ERROR!not found electricityPayParams,uid={}", uid);
            return Triple.of(false, "100234", "未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("FREE DEPOSIT HYBRID ERROR!not found userOauthBind,uid={}", uid);
            return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found userInfo,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT HYBRID ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("FFREE DEPOSIT HYBRID ERROR! not found userCarDeposit,uid={}", uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }


        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> payAmountList = new ArrayList<>();
        BigDecimal totalPayAmount = BigDecimal.valueOf(0);


        FreeDepositOrder freeDepositOrder = this.selectByOrderId(userCarDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            log.error("FFREE DEPOSIT HYBRID ERROR! freeDepositOrder is anomaly,uid={}", uid);
            return Triple.of(false, "100402", "免押失败！");
        }

        //处理租车套餐订单
        Triple<Boolean, String, Object> rentCarMemberCardTriple = carMemberCardOrderService.handleRentCarMemberCard(query.getStoreId(), query.getCarModelId(), query.getRentTime(), query.getRentType(), userInfo);
        if (Boolean.FALSE.equals(rentCarMemberCardTriple.getLeft())) {
            return rentCarMemberCardTriple;
        }


        //保存租车套餐订单
        if (Objects.nonNull(rentCarMemberCardTriple.getRight())) {
            CarMemberCardOrder carMemberCardOrder = (CarMemberCardOrder) rentCarMemberCardTriple.getRight();
            carMemberCardOrderService.insert(carMemberCardOrder);

            orderList.add(carMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_RENT_CAR_MEMBER_CARD);
            payAmountList.add(carMemberCardOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(carMemberCardOrder.getPayAmount());
        }

        if (totalPayAmount.doubleValue() <= NumberConstant.ZERO) {
            return Triple.of(false, "ELECTRICITY.0007", "支付金额异常！");
        }

        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(payAmountList))
                    .payAmount(totalPayAmount)
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)
                    .description("免押租车")
                    .uid(uid).build();
            WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, null, resultDTO);
        } catch (WechatPayException e) {
            log.error("FREE DEPOSIT HYBRID ERROR! wechat v3 order  error! uid={}", uid, e);
        }

        return Triple.of(false, "ELECTRICITY.0099", "下单失败");
    }

    /**
     * 车辆电池同时免押混合支付
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> freeCarBatteryDepositHybridOrder(FreeCarBatteryDepositHybridOrderQuery query, HttpServletRequest request) {

        Integer tenantId = TenantContextHolder.getTenantId();

        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found user");
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.ELE_CACHE_FREE_DEPOSIT_MEMBERCARD_LOCK_KEY + uid, "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }

        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryFromCache(tenantId);
        if (Objects.isNull(electricityPayParams)) {
            log.error("FREE DEPOSIT HYBRID ERROR!not found electricityPayParams,uid={}", uid);
            return Triple.of(false, "100234", "未配置支付参数!");
        }

        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.error("FREE DEPOSIT HYBRID ERROR!not found userOauthBind,uid={}", uid);
            return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT HYBRID ERROR! not found userInfo,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT HYBRID ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> payAmountList = new ArrayList<>();
        BigDecimal totalPayAmount = BigDecimal.valueOf(0);


        //处理租车套餐订单
        Triple<Boolean, String, Object> rentCarMemberCardTriple = carMemberCardOrderService.handleRentCarMemberCard(query.getStoreId(), query.getCarModelId(), query.getRentTime(), query.getRentType(), userInfo);
        if (Boolean.FALSE.equals(rentCarMemberCardTriple.getLeft())) {
            return rentCarMemberCardTriple;
        }


        //处理电池套餐相关
        Triple<Boolean, String, Object> rentBatteryMemberCardTriple = electricityMemberCardOrderService.handleRentBatteryMemberCard(
                query.getProductKey(), query.getDeviceName(), query.getUserCouponId(), query.getMemberCardId(), userInfo.getFranchiseeId(), userInfo);
        if (Boolean.FALSE.equals(rentBatteryMemberCardTriple.getLeft())) {
            return rentBatteryMemberCardTriple;
        }

        //处理保险套餐相关
        Triple<Boolean, String, Object> rentBatteryInsuranceTriple = insuranceOrderService.handleRentBatteryInsurance(query.getInsuranceId(), userInfo);
        if (Boolean.FALSE.equals(rentBatteryInsuranceTriple.getLeft())) {
            return rentBatteryInsuranceTriple;
        }

        //保存租车套餐订单
        if (Objects.nonNull(rentCarMemberCardTriple.getRight())) {
            CarMemberCardOrder carMemberCardOrder = (CarMemberCardOrder) rentCarMemberCardTriple.getRight();
            carMemberCardOrderService.insert(carMemberCardOrder);

            orderList.add(carMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_RENT_CAR_MEMBER_CARD);
            payAmountList.add(carMemberCardOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(carMemberCardOrder.getPayAmount());
        }


        //保存保险订单
        if (Objects.nonNull(rentBatteryInsuranceTriple.getRight())) {
            InsuranceOrder insuranceOrder = (InsuranceOrder) rentBatteryInsuranceTriple.getRight();
            insuranceOrderService.insert(insuranceOrder);

            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            payAmountList.add(insuranceOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(insuranceOrder.getPayAmount());
        }

        //保存套餐订单
        if (Objects.nonNull(rentBatteryMemberCardTriple.getRight())) {
            ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) rentBatteryMemberCardTriple.getRight();
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);

            orderList.add(electricityMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
            payAmountList.add(electricityMemberCardOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(electricityMemberCardOrder.getPayAmount());
        }

        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder()
                    .jsonOrderId(JsonUtil.toJson(orderList))
                    .jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(payAmountList))
                    .payAmount(totalPayAmount)
                    .tenantId(tenantId)
                    .attach(UnionTradeOrder.ATTACH_INTEGRATED_PAYMENT)
                    .description("免押租车租电")
                    .uid(uid).build();
            WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            return Triple.of(true, null, resultDTO);
        } catch (WechatPayException e) {
            log.error("FREE DEPOSIT HYBRID ERROR! wechat v3 order  error! uid={}", uid, e);
        }

        return Triple.of(false, "ELECTRICITY.0099", "下单失败");
    }


    @Override
    public Triple<Boolean, String, Object> freeBatteryDepositPreCheck() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        Triple<Boolean, String, Object> checkResult = checkUserCanFreeBatteryDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkResult.getLeft())) {
            return checkResult;
        }

        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());

        return Triple.of(true, null, freeDepositUserInfoVo);
    }

    @Override
    public Triple<Boolean, String, Object> freeCarDepositPreCheck() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("FREE DEPOSIT ERROR! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }

        Triple<Boolean, String, Object> checkResult = checkUserCanFreeCarDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkResult.getLeft())) {
            return checkResult;
        }

        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        freeDepositUserInfoVo.setName(userInfo.getName());
        freeDepositUserInfoVo.setPhone(userInfo.getPhone());
        freeDepositUserInfoVo.setIdCard(userInfo.getIdNumber());

        return Triple.of(true, null, freeDepositUserInfoVo);
    }

    @Override
    public void handleFreeDepositRefundOrder() {

        //处理电池免押解冻退款中订单
        batteryFreeDepositRefundingOrder();

        //处理电池免押解冻退款中订单
        carFreeDepositRefundingOrder();
    }

    private void batteryFreeDepositRefundingOrder() {
        int offset = 0;
        Long timeFlag = System.currentTimeMillis() + 300 * 1000L;
        while (System.currentTimeMillis() < timeFlag) {

            List<EleRefundOrder> eleRefundOrders = eleRefundOrderService.selectBatteryFreeDepositRefundingOrder(offset, REFUND_ORDER_LIMIT);
            offset += REFUND_ORDER_LIMIT;

            if (CollectionUtils.isEmpty(eleRefundOrders)) {
                break;
            }

            for (EleRefundOrder eleRefundOrder : eleRefundOrders) {
                //获取免押订单
                FreeDepositOrder freeDepositOrder = this.selectByOrderId(eleRefundOrder.getOrderId());
                if (Objects.isNull(freeDepositOrder)) {
                    log.error("FREE DEPOSIT TASK ERROR!not found batteryFreeDepositOrder,orderId={}", eleRefundOrder.getOrderId());
                    continue;
                }

                //获取免押解冻结果
                Triple<Boolean, String, Object> depositOrderStatusResult = this.selectFreeDepositOrderStatus(freeDepositOrder);
                if (Boolean.FALSE.equals(depositOrderStatusResult.getLeft())) {
                    log.error("FREE DEPOSIT TASK ERROR!acquire batteryFreeDepositOrder UN_FROZEN fail,orderId={},uid={}", eleRefundOrder.getOrderId(), freeDepositOrder.getUid());
                    continue;
                }

                PxzQueryOrderRsp queryOrderRspData = (PxzQueryOrderRsp) depositOrderStatusResult.getRight();
                if (!Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
                    log.error("FREE DEPOSIT TASK ERROR!batteryFreeDepositOrder not un_frozen,orderId={},uid={}", eleRefundOrder.getOrderId(), freeDepositOrder.getUid());
                    continue;
                }

                //更新押金订单
                EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
                eleRefundOrderUpdate.setId(eleRefundOrder.getId());
                eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
                eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.update(eleRefundOrderUpdate);

                //更新免押订单
                FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                freeDepositOrderUpdate.setId(freeDepositOrder.getId());
                freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
                freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                this.update(freeDepositOrderUpdate);

                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(freeDepositOrder.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);

                userBatteryMemberCardService.unbindMembercardInfoByUid(freeDepositOrder.getUid());
                userBatteryDepositService.logicDeleteByUid(freeDepositOrder.getUid());
                userBatteryService.deleteByUid(freeDepositOrder.getUid());

                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.queryByUidFromCache(freeDepositOrder.getUid());
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                }

                userInfoService.unBindUserFranchiseeId(freeDepositOrder.getUid());
            }
        }
    }

    private void carFreeDepositRefundingOrder() {
        int offset = 0;
        Long timeFlag = System.currentTimeMillis() + 300 * 1000L;
        while (System.currentTimeMillis() < timeFlag) {
            List<EleRefundOrder> eleRefundOrders = eleRefundOrderService.selectCarFreeDepositRefundingOrder(offset, REFUND_ORDER_LIMIT);
            offset += REFUND_ORDER_LIMIT;

            if (CollectionUtils.isEmpty(eleRefundOrders)) {
                break;
            }

            for (EleRefundOrder eleRefundOrder : eleRefundOrders) {
                //获取免押订单
                FreeDepositOrder freeDepositOrder = this.selectByOrderId(eleRefundOrder.getOrderId());
                if (Objects.isNull(freeDepositOrder)) {
                    log.error("FREE DEPOSIT TASK ERROR!not found carFreeDepositOrder,orderId={}", eleRefundOrder.getOrderId());
                    continue;
                }

                //获取免押解冻结果
                Triple<Boolean, String, Object> depositOrderStatusResult = this.selectFreeDepositOrderStatus(freeDepositOrder);
                if (Boolean.FALSE.equals(depositOrderStatusResult.getLeft())) {
                    log.error("FREE DEPOSIT TASK ERROR!acquire carFreeDepositOrder un_frozen fail,orderId={},uid={}", eleRefundOrder.getOrderId(), freeDepositOrder.getUid());
                    continue;
                }

                PxzQueryOrderRsp queryOrderRspData = (PxzQueryOrderRsp) depositOrderStatusResult.getRight();
                if (!Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
                    log.error("FREE DEPOSIT TASK ERROR!carFreeDepositOrder not un_frozen,orderId={},uid={}", eleRefundOrder.getOrderId(), freeDepositOrder.getUid());
                    continue;
                }

                //更新押金订单
                EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
                eleRefundOrderUpdate.setId(eleRefundOrder.getId());
                eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
                eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.update(eleRefundOrderUpdate);

                //更新免押订单
                FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                freeDepositOrderUpdate.setId(freeDepositOrder.getId());
                freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
                freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                this.update(freeDepositOrderUpdate);

                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(freeDepositOrder.getUid());
                updateUserInfo.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);

                userCarService.deleteByUid(freeDepositOrder.getUid());
                userCarDepositService.logicDeleteByUid(freeDepositOrder.getUid());
                userCarMemberCardService.deleteByUid(freeDepositOrder.getUid());
                userInfoService.unBindUserFranchiseeId(freeDepositOrder.getUid());
            }
        }
    }

    private Triple<Boolean, String, Object> checkUserCanFreeBatteryDeposit(Long uid, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (!(Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_BATTERY) || Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL))) {
            return Triple.of(false, null, "押金免押功能未开启,请联系客服处理");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "电池押金已经缴纳，无需重复缴纳");
        }
        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> checkUserCanFreeCarDeposit(Long uid, UserInfo userInfo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        if (!(Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_CAR) || Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL))) {
            return Triple.of(false, null, "押金免押功能未开启,请联系客服处理");
        }

        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("FREE DEPOSIT ERROR! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("FREE DEPOSIT ERROR! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "车辆押金已经缴纳，无需重复缴纳");
        }
        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> generateBatteryDepositOrder(UserInfo userInfo, FreeBatteryDepositQuery freeBatteryDepositQuery) {

        BigDecimal depositPayAmount = null;

        Franchisee franchisee = franchiseeService.queryByIdFromCache(freeBatteryDepositQuery.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("payDeposit  ERROR! not found Franchisee ！franchiseeId={},uid={}", freeBatteryDepositQuery.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }

        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }

        //型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(freeBatteryDepositQuery.getModel())) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }

            //型号押金
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(
                    franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("payDeposit  ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}",
                        freeBatteryDepositQuery.getFranchiseeId(), userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
            }

            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                if ((double) (modelBatteryDeposit.getModel()) - freeBatteryDepositQuery.getModel() < 1
                        && (double) (modelBatteryDeposit.getModel()) - freeBatteryDepositQuery.getModel() >= 0) {
                    depositPayAmount = modelBatteryDeposit.getBatteryDeposit();
                    break;
                }
            }
        }

        if (Objects.isNull(depositPayAmount)) {
            log.error("payDeposit  ERROR! payAmount is null ！franchiseeId{},uid={}", freeBatteryDepositQuery.getFranchiseeId(),
                    userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
        }

        //电池型号
        String batteryType = Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? BatteryConstant.acquireBatteryShort(freeBatteryDepositQuery.getModel()) : null;

        //生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid())
                .phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId())
                .franchiseeId(franchisee.getId()).payType(EleDepositOrder.FREE_DEPOSIT_PAYMENT).storeId(null)
                .modelType(franchisee.getModelType())
                .batteryType(batteryType).build();

        return Triple.of(true, null, eleDepositOrder);
    }

    private Triple<Boolean, String, Object> generateCarDepositOrder(UserInfo userInfo, FreeCarDepositQuery query) {
        Store store = storeService.queryByIdFromCache(query.getStoreId());
        if (Objects.isNull(store)) {
            log.error("ELE CAR DEPOSIT ERROR! not found store,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0018", "未找到门店");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(query.getCarModelId().intValue());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE CAR DEPOSIT ERROR! not find carMode, carModelId={},uid={}", query.getCarModelId(), userInfo.getUid());
            return Triple.of(false, "100009", "未找到该型号车辆");
        }

        //若缴纳电池押金  判断用户加盟商与租车加盟商是否一致  TODO
//        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && !Objects.equals(userInfo.getFranchiseeId(), electricityCarModel.getFranchiseeId())) {
//            log.error("ELE CAR DEPOSIT ERROR! car model franchiseeId not equals userInfo franchiseeId, franchiseeId1={},franchiseeId2={}", userInfo.getFranchiseeId(), electricityCarModel.getFranchiseeId());
//            return Triple.of(false, "100255", "车辆型号加盟商与用户加盟商不一致！");
//        }

        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.CAR_DEPOSIT, userInfo.getUid());

        BigDecimal payAmount = electricityCarModel.getCarDeposit();

        CarDepositOrder carDepositOrder = new CarDepositOrder();
        carDepositOrder.setUid(userInfo.getUid());
        carDepositOrder.setOrderId(orderId);
        carDepositOrder.setPhone(userInfo.getPhone());
        carDepositOrder.setName(userInfo.getName());
        carDepositOrder.setPayAmount(payAmount);
        carDepositOrder.setDelFlag(CarDepositOrder.DEL_NORMAL);
        carDepositOrder.setStatus(CarDepositOrder.STATUS_INIT);
        carDepositOrder.setTenantId(TenantContextHolder.getTenantId());
        carDepositOrder.setCreateTime(System.currentTimeMillis());
        carDepositOrder.setUpdateTime(System.currentTimeMillis());
        carDepositOrder.setFranchiseeId(store.getFranchiseeId());
        carDepositOrder.setStoreId(query.getStoreId());
        carDepositOrder.setPayType(CarDepositOrder.FREE_DEPOSIT_PAYTYPE);
        carDepositOrder.setCarModelId(query.getCarModelId());
        carDepositOrder.setRentBattery(Objects.isNull(query.getMemberCardId()) ? CarDepositOrder.RENTBATTERY_NO : CarDepositOrder.RENTBATTERY_YES);

        return Triple.of(true, "", carDepositOrder);
    }

}
