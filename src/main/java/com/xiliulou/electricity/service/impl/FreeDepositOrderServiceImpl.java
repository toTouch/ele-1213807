package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.AuthPayStatusBO;
import com.xiliulou.electricity.bo.FreeDepositOrderStatusBO;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.FreeDepositOrderDTO;
import com.xiliulou.electricity.dto.FreeDepositUserDTO;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositData;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.car.CarRentalPackageDepositPayPo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.mapper.EleRefundOrderMapper;
import com.xiliulou.electricity.mapper.FreeDepositOrderMapper;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.mq.producer.DelayFreeProducer;
import com.xiliulou.electricity.query.FreeBatteryDepositHybridOrderQuery;
import com.xiliulou.electricity.query.FreeBatteryDepositQuery;
import com.xiliulou.electricity.query.FreeBatteryDepositQueryV3;
import com.xiliulou.electricity.query.FreeDepositAuthToPayQuery;
import com.xiliulou.electricity.query.FreeDepositAuthToPayStatusQuery;
import com.xiliulou.electricity.query.FreeDepositOrderQuery;
import com.xiliulou.electricity.query.FreeDepositOrderRequest;
import com.xiliulou.electricity.query.FreeDepositOrderStatusQuery;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.service.BatteryMemberCardOrderCouponService;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeInsuranceService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.TradeOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.car.CarRentalPackageDepositPayService;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FreeDepositOrderVO;
import com.xiliulou.electricity.vo.FreeDepositUserInfoVo;
import com.xiliulou.pay.deposit.paixiaozu.exception.PxzFreeDepositException;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositAuthToPayOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositAuthToPayRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderQueryRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositOrderRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzAuthToPayOrderQueryRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzAuthToPayRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzQueryOrderRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.bouncycastle.util.encoders.DecoderException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private CarRentalPackageMemberTermService carRentalPackageMemberTermService;
    
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
    UserCouponService userCouponService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    @Autowired
    BatteryMemberCardOrderCouponService memberCardOrderCouponService;
    
    @Resource
    EleRefundOrderMapper eleRefundOrderMapper;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    FranchiseeInsuranceService franchiseeInsuranceService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    CarRentalPackageDepositPayService carRentalPackageDepositPayService;
    
    @Resource
    UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Resource
    private FreeDepositService freeDepositService;
    
    @Resource
    private DelayFreeProducer delayFreeProducer;
    
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
    
    @Slave
    @Override
    public FreeDepositOrder selectByOrderId(String orderId) {
        return this.freeDepositOrderMapper.selectOne(new LambdaQueryWrapper<FreeDepositOrder>().eq(FreeDepositOrder::getOrderId, orderId));
    }
    
    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    @Slave
    @Override
    public List<FreeDepositOrderVO> selectByPage(FreeDepositOrderQuery query) {
        List<FreeDepositOrder> freeDepositOrders = this.freeDepositOrderMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(freeDepositOrders)) {
            return Collections.EMPTY_LIST;
        }
        List<FreeDepositOrderVO> freeDepositOrderVOs = freeDepositOrders.parallelStream().map(item -> {
            FreeDepositOrderVO freeDepositOrderVO = new FreeDepositOrderVO();
            BeanUtils.copyProperties(item, freeDepositOrderVO);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            freeDepositOrderVO.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());
            return freeDepositOrderVO;
        }).collect(Collectors.toList());
        
        return freeDepositOrderVOs;
    }
    
    @Slave
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
    public FreeDepositOrder insert(FreeDepositOrder freeDepositOrder) {
        this.freeDepositOrderMapper.insert(freeDepositOrder);
        return freeDepositOrder;
    }
    
    /**
     * 修改数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    public Integer update(FreeDepositOrder freeDepositOrder) {
        return this.freeDepositOrderMapper.update(freeDepositOrder);
    }
    
    @Override
    public Triple<Boolean, String, Object> selectFreeDepositOrderDetail() {
        FreeDepositOrderVO freeDepositOrderVO = new FreeDepositOrderVO();
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(SecurityUtils.getUid());
        if (Objects.nonNull(userBatteryDeposit) && StringUtils.isNotBlank(userBatteryDeposit.getOrderId())) {
            FreeDepositOrder freeDepositOrder = this.selectByOrderId(userBatteryDeposit.getOrderId());
            if (Objects.nonNull(freeDepositOrder)) {
                BeanUtils.copyProperties(freeDepositOrder, freeDepositOrderVO);
                return Triple.of(true, "", freeDepositOrderVO);
            }
        }
        
        return Triple.of(true, "", freeDepositOrderVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> freeDepositAuthToPay(String orderId, BigDecimal payTransAmt, String remark) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("FREE DEPOSIT ERROR! not found user!");
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(uid)) {
            log.warn("FREE DEPOSIT WARN! not found user! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        if (!Objects.equals(freeDepositOrder.getPayStatus(), FreeDepositOrder.PAY_STATUS_INIT)) {
            log.warn("FREE DEPOSIT WARN! freeDepositOrder already AuthToPay,orderId={}", orderId);
            return Triple.of(false, "100412", "免押订单已进行代扣，请勿重复操作");
        }
        
        if (Objects.isNull(payTransAmt)) {
            payTransAmt = BigDecimal.valueOf(freeDepositOrder.getTransAmt());
        }
        
        if (Objects.isNull(payTransAmt) || payTransAmt.compareTo(BigDecimal.valueOf(freeDepositOrder.getTransAmt())) > 0) {
            log.warn("FREE DEPOSIT WARN! payTransAmt is illegal,orderId={}", orderId);
            return Triple.of(false, "ELECTRICITY.0007", "扣款金额不能大于支付金额!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(freeDepositOrder.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found user info! uid={}", freeDepositOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("FREE DEPOSIT WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("FREE DEPOSIT WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && !Objects.equals(userInfo.getCarDepositStatus(),
                UserInfo.CAR_DEPOSIT_STATUS_YES) && !Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.warn("FREE DEPOSIT WARN! user not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
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
            log.error("Pxz ERROR! freeDepositOrder authToPay fail! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId(), e);
            return Triple.of(false, "100411", "授权支付调用失败！");
        }
        
        if (Objects.isNull(pxzAuthToPayRspPxzCommonRsp)) {
            log.error("Pxz ERROR! freeDepositOrder authToPay fail! rsp is null! uid={},orderId={}", userInfo.getUid(), freeDepositOrder.getOrderId());
            return Triple.of(false, "100411", "授权支付调用失败！");
        }
        
        if (!pxzAuthToPayRspPxzCommonRsp.isSuccess()) {
            return Triple.of(false, "100411", pxzAuthToPayRspPxzCommonRsp.getRespDesc());
        }
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(FreeDepositOrder.PAY_STATUS_DEALING);
        freeDepositOrderUpdate.setPayTransAmt(freeDepositOrder.getTransAmt() - payTransAmt.doubleValue());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);
        
        // 代扣记录
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setOrderId(freeDepositOrder.getOrderId());
        freeDepositAlipayHistory.setUid(freeDepositOrder.getUid());
        freeDepositAlipayHistory.setName(freeDepositOrder.getRealName());
        freeDepositAlipayHistory.setPhone(freeDepositOrder.getPhone());
        freeDepositAlipayHistory.setIdCard(freeDepositOrder.getIdCard());
        freeDepositAlipayHistory.setOperateName(user.getName());
        freeDepositAlipayHistory.setOperateUid(user.getUid());
        freeDepositAlipayHistory.setPayAmount(BigDecimal.valueOf(freeDepositOrder.getTransAmt()));
        freeDepositAlipayHistory.setAlipayAmount(payTransAmt);
        freeDepositAlipayHistory.setType(freeDepositOrder.getDepositType());
        freeDepositAlipayHistory.setPayStatus(FreeDepositAlipayHistory.PAY_STATUS_DEALING);
        freeDepositAlipayHistory.setRemark(remark);
        freeDepositAlipayHistory.setCreateTime(System.currentTimeMillis());
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistory.setStoreId(freeDepositOrder.getStoreId());
        freeDepositAlipayHistory.setFranchiseeId(freeDepositOrder.getFranchiseeId());
        freeDepositAlipayHistory.setTenantId(TenantContextHolder.getTenantId());
        freeDepositAlipayHistoryService.insert(freeDepositAlipayHistory);
        return Triple.of(true, "", "授权转支付交易处理中！");
    }
    
    @Override
    public Triple<Boolean, String, Object> selectFreeDepositAuthToPay(String orderId) {
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,orderId={}", orderId);
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
            log.error("Pxz ERROR! query freeDepositOrder authToPay fail! rsp is null! orderId={}", freeDepositOrder.getOrderId());
            return Triple.of(false, "100411", "授权支付调用失败！");
        }
        
        if (!pxzAuthToPayOrderQueryRspPxzCommonRsp.isSuccess()) {
            return Triple.of(false, "100411", pxzAuthToPayOrderQueryRspPxzCommonRsp.getRespDesc());
        }
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(pxzAuthToPayOrderQueryRspPxzCommonRsp.getData().getOrderStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);
        
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setOrderId(freeDepositOrder.getOrderId());
        freeDepositAlipayHistory.setPayStatus(freeDepositOrderUpdate.getPayStatus());
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistoryService.updateByOrderId(freeDepositAlipayHistory);
        
        return Triple.of(true, "", pxzAuthToPayOrderQueryRspPxzCommonRsp.getData());
    }
    
    @Override
    public Triple<Boolean, String, Object> synchronizFreeDepositOrderStatus(String orderId) {
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(freeDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("FREE DEPOSIT WARN! not found user info,uid={},orderId={}", freeDepositOrder.getUid(), orderId);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        // 电池免押订单
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_BATTERY)) {
            
            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryDeposit)) {
                log.info("FREE DEPOSIT INFO! not found userBatteryDeposit,uid={},orderId={}", freeDepositOrder.getUid(), orderId);
                return Triple.of(true, "", "");
            }
            
            if (!Objects.equals(orderId, userBatteryDeposit.getOrderId())) {
                log.warn("FREE DEPOSIT WARN! illegal orderId,uid={},orderId={}", freeDepositOrder.getUid(), orderId);
                return Triple.of(false, "100417", "免押订单与用户绑定订单不一致");
            }
            
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
            if (Objects.isNull(eleDepositOrder)) {
                log.warn("FREE DEPOSIT WARN! not found eleDepositOrder! uid={},orderId={}", freeDepositOrder.getUid(), userBatteryDeposit.getOrderId());
                return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
            }
            
            if (EleDepositOrder.STATUS_SUCCESS.equals(eleDepositOrder.getStatus())) {
                log.warn("synchronizFreeDepositOrderStatus failed.  t_ele_deposit_order status is success. orderId is {}", orderId);
                return Triple.of(true, "", "");
            }
            
            // 三方接口免押查询
            FreeDepositOrderStatusQuery dto = FreeDepositOrderStatusQuery.builder().tenantId(userInfo.getTenantId()).channel(freeDepositOrder.getChannel()).orderId(orderId)
                    .uid(userInfo.getUid()).build();
            FreeDepositOrderStatusBO bo = freeDepositService.getFreeDepositOrderStatus(dto);
            if (Objects.isNull(bo)) {
                return Triple.of(false, "100402", "免押查询失败！");
            }
            
            // 更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthNo(bo.getAuthNo());
            freeDepositOrderUpdate.setAuthStatus(bo.getAuthStatus());
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            this.update(freeDepositOrderUpdate);
            
            // 冻结成功
            if (Objects.equals(bo.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
                // 扣减免押次数
                freeDepositDataService.deductionFreeDepositCapacity(TenantContextHolder.getTenantId(), 1);
                
                // 更新押金订单状态
                EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
                eleDepositOrderUpdate.setId(eleDepositOrder.getId());
                eleDepositOrderUpdate.setStatus(EleDepositOrder.STATUS_SUCCESS);
                eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleDepositOrderService.update(eleDepositOrderUpdate);
                
                // 绑定加盟商、更新押金状态
                UserInfo userInfoUpdate = new UserInfo();
                userInfoUpdate.setUid(userInfo.getUid());
                userInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
                userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
                userInfoUpdate.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(userInfoUpdate);
                
                // 绑定电池型号
                List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(eleDepositOrder.getMid());
                if (CollectionUtils.isNotEmpty(batteryTypeList)) {
                    userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo));
                }
            }
        }
        
        // 租车免押订单
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR) || Objects.equals(freeDepositOrder.getDepositType(),
                FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            // 查询押金缴纳信息
            CarRentalPackageDepositPayPo depositPayEntity = carRentalPackageDepositPayService.selectByOrderNo(orderId);
            if (ObjectUtils.isEmpty(depositPayEntity)) {
                log.warn("synchronizFreeDepositOrderStatus failed. not found t_car_rental_package_deposit_pay. depositPayOrderNo is {}", orderId);
                return Triple.of(false, "", "");
            }
            
            if (PayStateEnum.SUCCESS.getCode().equals(depositPayEntity.getPayState())) {
                log.warn("synchronizFreeDepositOrderStatus failed. t_car_rental_package_deposit_pay pay state is success. depositPayOrderNo is {}", orderId);
                return Triple.of(true, "", "");
            }
            
            if (!PayTypeEnum.EXEMPT.getCode().equals(depositPayEntity.getPayType())) {
                log.warn("synchronizFreeDepositOrderStatus failed. t_car_rental_package_deposit_pay payType is wrong. depositPayOrderNo is {}", orderId);
                return Triple.of(false, "", "");
            }
            
            // 三方接口免押查询
            FreeDepositOrderStatusQuery dto = FreeDepositOrderStatusQuery.builder().tenantId(userInfo.getTenantId()).channel(freeDepositOrder.getChannel()).orderId(orderId)
                    .uid(userInfo.getUid()).build();
            FreeDepositOrderStatusBO bo = freeDepositService.getFreeDepositOrderStatus(dto);
            if (Objects.isNull(bo)) {
                return Triple.of(false, "100402", "免押查询失败！");
            }
            
            // 车的免押成功处理
            if (Objects.equals(bo.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
                saveSyncFreeDepositOrderStatusTx(depositPayEntity, freeDepositOrder, bo);
            }
        }
        
        return Triple.of(true, null, "同步成功");
    }
    
    /**
     * 临时抽取方法，后续需要抽取到类中，走代理
     *
     * @param depositPayEntity
     * @param freeDepositOrder
     * @param bo
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSyncFreeDepositOrderStatusTx(CarRentalPackageDepositPayPo depositPayEntity, FreeDepositOrder freeDepositOrder, FreeDepositOrderStatusBO bo) {
        Integer tenantId = depositPayEntity.getTenantId();
        Integer franchiseeId = depositPayEntity.getFranchiseeId();
        Integer storeId = depositPayEntity.getStoreId();
        Long uid = depositPayEntity.getUid();
        Integer rentalPackageType = depositPayEntity.getRentalPackageType();
        String depositPayOrderNo = depositPayEntity.getOrderNo();
        
        // 1. 更新免押记录的状态
        FreeDepositOrder freeDepositOrderModify = new FreeDepositOrder();
        freeDepositOrderModify.setId(freeDepositOrder.getId());
        freeDepositOrderModify.setAuthNo(bo.getAuthNo());
        freeDepositOrderModify.setAuthStatus(bo.getAuthStatus());
        freeDepositOrderModify.setUpdateTime(System.currentTimeMillis());
        update(freeDepositOrderModify);
        // 2. 成功之后更新各种状态
        if (FreeDepositOrder.AUTH_FROZEN.equals(bo.getAuthStatus())) {
            // 1. 扣减免押次数
            freeDepositDataService.deductionFreeDepositCapacity(tenantId, 1);
            // 2. 更新押金缴纳订单数据
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.SUCCESS.getCode());
            // 3. 更新租车会员信息状态
            carRentalPackageMemberTermService.updateStatusByUidAndTenantId(tenantId, uid, MemberTermStatusEnum.NORMAL.getCode(), uid);
            // 4. 更新用户表押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            
            UserInfo userInfo = userInfoService.queryByUidFromDb(uid);
            Long boundFranchiseeId = userInfo.getFranchiseeId();
            if (Objects.isNull(boundFranchiseeId) || Objects.equals(boundFranchiseeId, NumberConstant.ZERO_L)) {
                userInfoUpdate.setFranchiseeId(Long.valueOf(franchiseeId));
            }
            
            Long boundStoreId = userInfo.getStoreId();
            if (Objects.isNull(boundStoreId) || Objects.equals(boundStoreId, NumberConstant.ZERO_L) || Objects.equals(boundFranchiseeId, Long.valueOf(franchiseeId))) {
                userInfoUpdate.setStoreId(Long.valueOf(storeId));
            }
            
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            if (RentalPackageTypeEnum.CAR.getCode().equals(rentalPackageType)) {
                userInfoUpdate.setCarDepositStatus(UserInfo.CAR_DEPOSIT_STATUS_YES);
            }
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
                userInfoUpdate.setCarBatteryDepositStatus(YesNoEnum.YES.getCode());
            }
            userInfoService.updateByUid(userInfoUpdate);
            // 车电一体，同步押金
            if (RentalPackageTypeEnum.CAR_BATTERY.getCode().equals(rentalPackageType)) {
                log.info("saveSyncFreeDepositOrderStatusTx, userBatteryDepositService.synchronizedUserBatteryDepositInfo. depositPayOrderNo is {}", depositPayEntity.getOrderNo());
                userBatteryDepositService.synchronizedUserBatteryDepositInfo(uid, null, depositPayEntity.getOrderNo(), depositPayEntity.getDeposit());
            }
        }
        // 3. 超时关闭之后更新状态
        if (FreeDepositOrder.AUTH_TIMEOUT.equals(bo.getAuthStatus())) {
            // 1. 更新押金缴纳订单数据
            carRentalPackageDepositPayService.updatePayStateByOrderNo(depositPayOrderNo, PayStateEnum.FAILED.getCode());
            // 2. 删除会员期限表数据
            carRentalPackageMemberTermService.delByUidAndTenantId(tenantId, uid, uid);
        }
    }
    
    /**
     * 查询免押订单状态
     */
    @Override
    public Triple<Boolean, String, Object> selectFreeDepositOrderStatus(String orderId) {
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,orderId={}", orderId);
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
    @Deprecated
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
        
        // 获取租户免押次数
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
        
        FreeDepositUserDTO freeDepositUserDTO = FreeDepositUserDTO.builder().uid(userInfo.getUid()).realName(freeBatteryDepositQuery.getRealName())
                .phoneNumber(freeBatteryDepositQuery.getPhoneNumber()).idCard(freeBatteryDepositQuery.getIdCard()).tenantId(TenantContextHolder.getTenantId())
                //.packageId(freeBatteryDepositQuery.get)
                .packageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()).build();
        
        // 检查用户是否已经进行过免押操作，且已免押成功
        Triple<Boolean, String, Object> useFreeDepositStatusResult = checkFreeDepositStatusFromPxz(freeDepositUserDTO, pxzConfig);
        if (Boolean.FALSE.equals(useFreeDepositStatusResult.getLeft())) {
            return useFreeDepositStatusResult;
        }
        
        // 查看缓存中的免押链接信息是否还存在，若存在，并且本次免押传入的用户名称和身份证与上次相同，则获取缓存数据并返回
        boolean freeOrderCacheResult = redisService.hasKey(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid);
        if (Objects.isNull(useFreeDepositStatusResult.getRight()) && freeOrderCacheResult) {
            String result = UriUtils.decode(redisService.get(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid), StandardCharsets.UTF_8);
            result = JsonUtil.fromJson(result, String.class);
            log.info("found the free order result from cache. uid = {}, result = {}", uid, result);
            return Triple.of(true, null, result);
        }
        
        Triple<Boolean, String, Object> generateDepositOrderResult = generateBatteryDepositOrder(userInfo, freeBatteryDepositQuery);
        if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
            return generateDepositOrderResult;
        }
        EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
        
        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder().uid(uid).authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE).idCard(freeBatteryDepositQuery.getIdCard())
                .orderId(eleDepositOrder.getOrderId()).phone(freeBatteryDepositQuery.getPhoneNumber()).realName(freeBatteryDepositQuery.getRealName())
                .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).payStatus(FreeDepositOrder.PAY_STATUS_INIT).storeId(eleDepositOrder.getStoreId())
                .franchiseeId(eleDepositOrder.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId()).transAmt(eleDepositOrder.getPayAmount().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO).depositType(FreeDepositOrder.DEPOSIT_TYPE_BATTERY).build();
        
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
        
        // 绑定免押订单
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
        
        // 保存pxz返回的免押链接信息，5分钟之内不会生成新码
        redisService.saveWithString(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid, UriUtils.encode(callPxzRsp.getData(), StandardCharsets.UTF_8),
                300 * 1000L, false);
        
        return Triple.of(true, null, callPxzRsp.getData());
    }
    
    /**
     * 生成电池免押订单
     */
    @Override
    public Triple<Boolean, String, Object> freeBatteryDepositOrderV3(FreeBatteryDepositQueryV3 freeQuery) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("FREE DEPOSIT WARN! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        // 获取租户免押次数
        FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            log.warn("FREE DEPOSIT WARN! freeDepositData is null,uid={}", uid);
            return Triple.of(false, "100404", "免押次数未充值，请联系管理员");
        }
        
        if (freeDepositData.getFreeDepositCapacity() <= NumberConstant.ZERO) {
            log.warn("FREE DEPOSIT WARN! freeDepositCapacity already run out,uid={}", uid);
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
        
        FreeDepositUserDTO freeDepositUserDTO = FreeDepositUserDTO.builder().uid(userInfo.getUid()).realName(freeQuery.getRealName()).phoneNumber(freeQuery.getPhoneNumber())
                .idCard(freeQuery.getIdCard()).tenantId(TenantContextHolder.getTenantId()).packageId(freeQuery.getMembercardId())
                .packageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()).build();
        
        // 检查用户是否已经进行过免押操作，且已免押成功
        Triple<Boolean, String, Object> useFreeDepositStatusResult = checkFreeDepositStatusFromPxz(freeDepositUserDTO, pxzConfig);
        if (Boolean.FALSE.equals(useFreeDepositStatusResult.getLeft())) {
            return useFreeDepositStatusResult;
        }
        
        // 查看缓存中的免押链接信息是否还存在，若存在，并且本次免押传入的用户名称和身份证与上次相同，则获取缓存数据并返回
        boolean freeOrderCacheResult = redisService.hasKey(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid);
        if (Objects.isNull(useFreeDepositStatusResult.getRight()) && freeOrderCacheResult) {
            String result = UriUtils.decode(redisService.get(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid), StandardCharsets.UTF_8);
            result = JsonUtil.fromJson(result, String.class);
            log.info("found the free order result from cache for battery package. uid = {}, result = {}", uid, result);
            return Triple.of(true, null, result);
        }
        
        Triple<Boolean, String, Object> generateDepositOrderResult = generateBatteryDepositOrderV3(userInfo, freeQuery);
        if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
            return generateDepositOrderResult;
        }
        EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
        
        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder().uid(uid).authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE).idCard(freeQuery.getIdCard())
                .orderId(eleDepositOrder.getOrderId()).phone(freeQuery.getPhoneNumber()).realName(freeQuery.getRealName()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).payStatus(FreeDepositOrder.PAY_STATUS_INIT).storeId(eleDepositOrder.getStoreId())
                .franchiseeId(eleDepositOrder.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId()).transAmt(eleDepositOrder.getPayAmount().doubleValue())
                .type(FreeDepositOrder.TYPE_ZHIFUBAO).depositType(FreeDepositOrder.DEPOSIT_TYPE_BATTERY).build();
        
        PxzCommonRequest<PxzFreeDepositOrderRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(freeDepositOrder.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderRequest request = new PxzFreeDepositOrderRequest();
        request.setPhone(freeQuery.getPhoneNumber());
        request.setSubject("电池免押");
        request.setRealName(freeQuery.getRealName());
        request.setIdNumber(freeQuery.getIdCard());
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
        
        // 绑定免押订单
        UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
        userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
        userBatteryDeposit.setUid(uid);
        userBatteryDeposit.setDid(eleDepositOrder.getMid());
        userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
        userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
        userBatteryDeposit.setCreateTime(System.currentTimeMillis());
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);
        
        log.info("generate free deposit data from pxz for battery package, data = {}", callPxzRsp);
        // 保存pxz返回的免押链接信息，5分钟之内不会生成新码
        redisService.saveWithString(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid, UriUtils.encode(callPxzRsp.getData(), StandardCharsets.UTF_8),
                300 * 1000L, false);
        
        return Triple.of(true, null, callPxzRsp.getData());
    }
    
    
    /**
     * 生成电池免押订单
     */
    @Override
    public Triple<Boolean, String, Object> freeBatteryDepositOrderV4(FreeBatteryDepositQueryV3 freeQuery) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("FREE DEPOSIT WARN! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        // 获取租户免押次数
        FreeDepositData freeDepositData = freeDepositDataService.selectByTenantId(TenantContextHolder.getTenantId());
        if (Objects.isNull(freeDepositData)) {
            log.warn("FREE DEPOSIT WARN! freeDepositData is null,uid={}", uid);
            return Triple.of(false, "100404", "免押次数未充值，请联系管理员");
        }
        
        // 修改免押次数判断，蜂云
        if (freeDepositData.getFreeDepositCapacity() <= NumberConstant.ZERO && freeDepositData.getFyFreeDepositCapacity() <= NumberConstant.ZERO) {
            log.warn("FREE DEPOSIT WARN! fyFreeDepositCapacity already run out,uid={}", uid);
            return Triple.of(false, "100405", "免押次数已用完，请联系管理员");
        }
        
        
        Triple<Boolean, String, Object> checkUserCanFreeDepositResult = checkUserCanFreeBatteryDeposit(uid, userInfo);
        if (Boolean.FALSE.equals(checkUserCanFreeDepositResult.getLeft())) {
            return checkUserCanFreeDepositResult;
        }
        
        // 是否已经免押过
        FreeDepositUserDTO freeDepositUserDTO = FreeDepositUserDTO.builder().uid(userInfo.getUid()).realName(freeQuery.getRealName()).phoneNumber(freeQuery.getPhoneNumber())
                .idCard(freeQuery.getIdCard()).tenantId(TenantContextHolder.getTenantId()).packageId(freeQuery.getMembercardId())
                .packageType(PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()).build();
        
        Triple<Boolean, String, Object> triple = freeDepositService.checkExistSuccessFreeDepositOrder(freeDepositUserDTO);
        if (triple.getLeft()) {
            return triple;
        }
        
        
        // 查看缓存中的免押链接信息是否还存在，若存在，并且本次免押传入的用户名称和身份证与上次相同，则获取缓存数据并返回
        boolean freeOrderCacheResult = redisService.hasKey(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid);
        if (Objects.isNull(triple.getRight()) && freeOrderCacheResult) {
            String result = UriUtils.decode(redisService.get(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid), StandardCharsets.UTF_8);
            result = JsonUtil.fromJson(result, String.class);
            log.info("found the free order result from cache for battery package. uid = {}, result = {}", uid, result);
            return Triple.of(true, null, result);
        }
        
        Triple<Boolean, String, Object> generateDepositOrderResult = generateBatteryDepositOrderV3(userInfo, freeQuery);
        if (Boolean.FALSE.equals(generateDepositOrderResult.getLeft())) {
            return generateDepositOrderResult;
        }
        EleDepositOrder eleDepositOrder = (EleDepositOrder) generateDepositOrderResult.getRight();
        
        // 免押下单
        FreeDepositOrderRequest orderRequest = FreeDepositOrderRequest.builder().uid(uid).tenantId(userInfo.getTenantId()).phoneNumber(freeQuery.getPhoneNumber())
                .idCard(freeQuery.getIdCard()).payAmount(eleDepositOrder.getPayAmount()).freeDepositOrderId(eleDepositOrder.getOrderId()).realName(freeQuery.getRealName())
                .subject("电池免押").build();
        Triple<Boolean, String, Object> freeDepositOrderTriple = freeDepositService.freeDepositOrder(orderRequest);
        if (!freeDepositOrderTriple.getLeft() || Objects.isNull(freeDepositOrderTriple.getRight())) {
            return Triple.of(false, freeDepositOrderTriple.getMiddle(), freeDepositOrderTriple.getRight());
        }
        
        FreeDepositOrderDTO depositOrderDTO = (FreeDepositOrderDTO) freeDepositOrderTriple.getRight();
        
        // 待冻结
        FreeDepositOrder freeDepositOrder = FreeDepositOrder.builder().uid(uid).authStatus(FreeDepositOrder.AUTH_PENDING_FREEZE).idCard(freeQuery.getIdCard())
                .orderId(eleDepositOrder.getOrderId()).phone(freeQuery.getPhoneNumber()).realName(freeQuery.getRealName()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).payStatus(FreeDepositOrder.PAY_STATUS_INIT).storeId(eleDepositOrder.getStoreId())
                .franchiseeId(eleDepositOrder.getFranchiseeId()).tenantId(TenantContextHolder.getTenantId()).transAmt(eleDepositOrder.getPayAmount().doubleValue())
                // 生成免押订单的时候，免押金额=剩余可代扣金额
                .payTransAmt(eleDepositOrder.getPayAmount().doubleValue()).type(FreeDepositOrder.TYPE_ZHIFUBAO).depositType(FreeDepositOrder.DEPOSIT_TYPE_BATTERY)
                .channel(depositOrderDTO.getChannel()).build();
        
        insert(freeDepositOrder);
        eleDepositOrderService.insert(eleDepositOrder);
        
        // 绑定免押订单
        UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
        userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
        userBatteryDeposit.setUid(uid);
        userBatteryDeposit.setDid(eleDepositOrder.getMid());
        userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
        userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
        userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
        userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
        userBatteryDeposit.setCreateTime(System.currentTimeMillis());
        userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
        userBatteryDepositService.insertOrUpdate(userBatteryDeposit);
        
        log.info("generate free deposit data from pxz for battery package, data = {}", depositOrderDTO);
        // 保存pxz返回的免押链接信息，5分钟之内不会生成新码
        redisService.saveWithString(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY + uid,
                UriUtils.encode(JsonUtil.toJson(depositOrderDTO.getData()), StandardCharsets.UTF_8), 300 * 1000L, false);
        
        // 发送延迟队列延迟更新免押状态为最终态
        delayFreeProducer.sendDelayFreeMessage(freeDepositOrder.getOrderId(), MqProducerConstant.FREE_DEPOSIT_TAG_NAME);
        
        return Triple.of(true, null, depositOrderDTO.getData());
    }
    
    /**
     * 检查用户在拍小租侧免押是否成功
     *
     * @param freeDepositUserDTO
     * @param pxzConfig
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> checkFreeDepositStatusFromPxz(FreeDepositUserDTO freeDepositUserDTO, PxzConfig pxzConfig) {
        String orderId;
        Long packageId;
        
        // 判断当前免押操作是购买换电套餐还是租车套餐
        if (PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode().equals(freeDepositUserDTO.getPackageType())) {
            // 获取换电套餐已存在的免押订单信息. 如果不存在或者押金类型为缴纳押金类型则返回
            UserBatteryDeposit batteryDeposit = userBatteryDepositService.selectByUidFromCache(freeDepositUserDTO.getUid());
            if (Objects.isNull(batteryDeposit) || UserBatteryDeposit.DEPOSIT_TYPE_DEFAULT.equals(batteryDeposit.getDepositType())) {
                return Triple.of(true, null, null);
            }
            orderId = batteryDeposit.getOrderId();
            packageId = batteryDeposit.getDid();
        } else {
            // 获取购买租车套餐时已存在的免押订单信息
            CarRentalPackageDepositPayPo carRentalPackageDepositPayPo = carRentalPackageDepositPayService.selectLastByUid(freeDepositUserDTO.getTenantId(),
                    freeDepositUserDTO.getUid());
            if (Objects.isNull(carRentalPackageDepositPayPo) || YesNoEnum.NO.getCode().equals(carRentalPackageDepositPayPo.getFreeDeposit())) {
                return Triple.of(true, null, null);
            }
            orderId = carRentalPackageDepositPayPo.getOrderNo();
            packageId = carRentalPackageDepositPayPo.getRentalPackageId();
        }
        log.info("check free deposit status from pxz. orderId = {}, package id = {}, user data = {}", orderId, packageId, freeDepositUserDTO);
        
        // 检查传入的用户信息是否和前一次传入的内容一致，若用户名或身份证号,以及所选套餐存在不一致，则需要生成新码
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (!Objects.equals(freeDepositOrder.getRealName(), freeDepositUserDTO.getRealName()) || !Objects.equals(freeDepositOrder.getIdCard(), freeDepositUserDTO.getIdCard())
                || !Objects.equals(packageId, freeDepositUserDTO.getPackageId())) {
            log.info("found the different user info or different package for generate deposit link, order id = {}", orderId);
            return Triple.of(true, null, freeDepositOrder);
        }
        
        PxzCommonRequest<PxzFreeDepositOrderQueryRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(orderId);
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositOrderQueryRequest request = new PxzFreeDepositOrderQueryRequest();
        request.setTransId(orderId);
        query.setData(request);
        
        PxzCommonRsp<PxzQueryOrderRsp> pxzQueryOrderRsp = null;
        try {
            pxzQueryOrderRsp = pxzDepositService.queryFreeDepositOrder(query);
        } catch (PxzFreeDepositException e) {
            log.error("query free deposit status from pxz error! uid = {}, orderId = {}", freeDepositUserDTO.getUid(), orderId, e);
        }
        
        if (Objects.nonNull(pxzQueryOrderRsp) && Objects.nonNull(pxzQueryOrderRsp.getData())) {
            PxzQueryOrderRsp queryOrderRspData = pxzQueryOrderRsp.getData();
            if (PxzQueryOrderRsp.AUTH_FROZEN.equals(queryOrderRspData.getAuthStatus())) {
                log.info("query free deposit status from pxz success! uid = {}, orderId = {}", freeDepositUserDTO.getUid(), orderId);
                return Triple.of(false, "100400", "免押已成功，请勿重复操作");
            }
        }
        
        return Triple.of(true, null, null);
    }
    
    /**
     * 查询电池免押是否成功
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> acquireUserFreeBatteryDepositStatus() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("FREE DEPOSIT WARN! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.info("FREE DEPOSIT INFO! not found userBatteryDeposit,uid={}", uid);
            return Triple.of(true, "", "");
        }
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        // 如果已冻结  直接返回
        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
            freeDepositUserInfoVo.setBatteryDepositAuthStatus(freeDepositOrder.getAuthStatus());
            return Triple.of(true, null, freeDepositUserInfoVo);
        }
        
        //        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        //        if (!(Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_BATTERY) || Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL))) {
        //            return Triple.of(false, "100418", "押金免押功能未开启,请联系客服处理");
        //        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.warn("FREE DEPOSIT WARN! not found pxzConfig,uid={}", uid);
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("FREE DEPOSIT WARN! not found eleDepositOrder! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
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
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthNo(queryOrderRspData.getAuthNo());
        freeDepositOrderUpdate.setAuthStatus(queryOrderRspData.getAuthStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);
        
        // 冻结成功
        if (Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            
            // 扣减免押次数
            freeDepositDataService.deductionFreeDepositCapacity(TenantContextHolder.getTenantId(), 1);
            
            // 更新押金订单状态
            EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
            eleDepositOrderUpdate.setId(eleDepositOrder.getId());
            eleDepositOrderUpdate.setStatus(EleDepositOrder.STATUS_SUCCESS);
            eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleDepositOrderService.update(eleDepositOrderUpdate);
            
            // 绑定加盟商、更新押金状态
            UserInfo userInfoUpdate = new UserInfo();
            userInfoUpdate.setUid(uid);
            
            Long boundFranchiseeId = userInfo.getFranchiseeId();
            if (Objects.isNull(boundFranchiseeId) || Objects.equals(boundFranchiseeId, NumberConstant.ZERO_L)) {
                userInfoUpdate.setFranchiseeId(eleDepositOrder.getFranchiseeId());
            }
            
            Long boundStoreId = userInfo.getStoreId();
            if (Objects.isNull(boundStoreId) || Objects.equals(boundStoreId, NumberConstant.ZERO_L)) {
                userInfoUpdate.setStoreId(eleDepositOrder.getStoreId());
            }
            
            userInfoUpdate.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
            userInfoUpdate.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(userInfoUpdate);
            
            // 绑定电池型号
            List<String> batteryTypeList = memberCardBatteryTypeService.selectBatteryTypeByMid(eleDepositOrder.getMid());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(batteryTypeList)) {
                userBatteryTypeService.batchInsert(userBatteryTypeService.buildUserBatteryType(batteryTypeList, userInfo));
            }
        }
        
        freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
        freeDepositUserInfoVo.setBatteryDepositAuthStatus(queryOrderRspData.getAuthStatus());
        
        return Triple.of(true, null, freeDepositUserInfoVo);
    }
    
    
    /**
     * 查询电池免押是否成功
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> acquireUserFreeBatteryDepositStatusV2() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("FREE DEPOSIT WARN! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.info("FREE DEPOSIT INFO! not found userBatteryDeposit,uid={}", uid);
            return Triple.of(true, "", "");
        }
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        // 如果已冻结  直接返回
        FreeDepositUserInfoVo freeDepositUserInfoVo = new FreeDepositUserInfoVo();
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
            freeDepositUserInfoVo.setBatteryDepositAuthStatus(freeDepositOrder.getAuthStatus());
            return Triple.of(true, null, freeDepositUserInfoVo);
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("FREE DEPOSIT WARN! not found eleDepositOrder! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }
        
        
        freeDepositUserInfoVo.setApplyBatteryDepositTime(userBatteryDeposit.getApplyDepositTime());
        freeDepositUserInfoVo.setBatteryDepositAuthStatus(freeDepositOrder.getAuthStatus());
        
        return Triple.of(true, null, freeDepositUserInfoVo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> freeBatteryDepositHybridOrderV3(FreeBatteryDepositHybridOrderQuery query, HttpServletRequest request) {
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
            log.warn("FREE DEPOSIT HYBRID WARN! not found user info,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("FREE DEPOSIT HYBRID WARN! not found userInfo,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("FREE DEPOSIT HYBRID WARN! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        // 检查是否为自主续费状态
        Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(uid);
        if (!userRenewalStatus) {
            log.warn("BATTERY MEMBER ORDER WARN! user renewal status is false, uid={}, mid={}", uid, query.getMemberCardId());
            return Triple.of(false, "000088", "您已是渠道用户，请联系对应站点购买套餐");
        }
        
        UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
        if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
            log.warn("FREE DEPOSIT HYBRID WARN!not found userOauthBind,uid={}", uid);
            return Triple.of(false, "100235", "未找到用户的第三方授权信息!");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("FFREE DEPOSIT HYBRID WARN! not found userBatteryDeposit,uid={}", uid);
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> payAmountList = new ArrayList<>();
        BigDecimal totalPayAmount = BigDecimal.valueOf(0);
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN)) {
            log.warn("FFREE DEPOSIT HYBRID WARN! freeDepositOrder is anomaly,uid={}", uid);
            return Triple.of(false, "100402", "免押失败！");
        }
        
        // 获取押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("FREE DEPOSIT WARN! not found eleDepositOrder! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getMemberCardId().longValue());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("FREE DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", userInfo.getUid(), query.getMemberCardId());
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
        
        WechatPayParamsDetails wechatPayParamsDetails = null;
        try {
            wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, batteryMemberCard.getFranchiseeId());
        } catch (WechatPayException e) {
            log.warn("FREE DEPOSIT HYBRID WARN!not found electricityPayParams,uid={}", uid);
            return Triple.of(false, "PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
        }
        if (Objects.isNull(wechatPayParamsDetails)) {
            log.warn("FREE DEPOSIT HYBRID WARN!not found electricityPayParams,uid={}", uid);
            return Triple.of(false, "100307", "未配置支付参数!");
        }
        
        if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
            log.warn("FREE DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", userInfo.getUid(), query.getMemberCardId());
            return Triple.of(false, "100275", "电池套餐不可用");
        }
        
        // 判断套餐是否为免押套餐
        if (!Objects.equals(batteryMemberCard.getFreeDeposite(), BatteryMemberCard.YES)) {
            log.warn("FREE DEPOSIT WARN! batteryMemberCard is illegal,uid={},mid={}", userInfo.getUid(), query.getMemberCardId());
            return Triple.of(false, "100483", "电池套餐不合法");
        }
        
        if (Objects.nonNull(userBatteryDeposit.getBatteryDeposit()) && batteryMemberCard.getDeposit().compareTo(userBatteryDeposit.getBatteryDeposit()) != 0) {
            log.warn("FREE DEPOSIT WARN! batteryMemberCard not equals free deposit,uid={},mid={}", userInfo.getUid(), query.getMemberCardId());
            return Triple.of(false, "100484", "免押押金与电池套餐押金不一致");
        }
        
        if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                batteryMemberCard.getFranchiseeId())) {
            log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", userInfo.getUid(), query.getMemberCardId());
            return Triple.of(false, "100349", "用户加盟商与套餐加盟商不一致");
        }
        
        // 判断套餐租赁状态，用户为老用户，套餐类型为新租，则不支持购买
        if (userInfo.getPayCount() > 0 && BatteryMemberCard.RENT_TYPE_NEW.equals(batteryMemberCard.getRentType())) {
            log.warn("FREE BATTERY DEPOSIT HYBRID ORDER WARN! The rent type of current package is a new rental package, uid={}, mid={}", userInfo.getUid(),
                    query.getMemberCardId());
            return Triple.of(false, "100376", "已是平台老用户，无法购买新租类型套餐，请刷新页面重试");
        }
        
        // 是否有正在进行中的退押
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(userBatteryDeposit.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0047", "电池押金退款中");
        }
        
        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if (CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
            log.warn("FREE DEPOSIT WARN! battery membercard refund review,uid={}", userInfo.getUid());
            return Triple.of(false, "100018", "套餐租金退款审核中");
        }
        
        // 获取扫码柜机
        ElectricityCabinet electricityCabinet = null;
        if (StringUtils.isNotBlank(query.getProductKey()) && StringUtils.isNotBlank(query.getDeviceName())) {
            electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
        }
        
        // 套餐订单
        Triple<Boolean, String, Object> rentBatteryMemberCardTriple = generateMemberCardOrder(userInfo, batteryMemberCard, query, electricityCabinet, wechatPayParamsDetails);
        if (Boolean.FALSE.equals(rentBatteryMemberCardTriple.getLeft())) {
            return rentBatteryMemberCardTriple;
        }
        
        // 保险订单
        Triple<Boolean, String, Object> rentBatteryInsuranceTriple = generateInsuranceOrder(userInfo, query, electricityCabinet, wechatPayParamsDetails);
        if (Boolean.FALSE.equals(rentBatteryInsuranceTriple.getLeft())) {
            return rentBatteryInsuranceTriple;
        }
        
        // 保存保险订单
        if (Objects.nonNull(rentBatteryInsuranceTriple.getRight())) {
            InsuranceOrder insuranceOrder = (InsuranceOrder) rentBatteryInsuranceTriple.getRight();
            insuranceOrderService.insert(insuranceOrder);
            
            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            payAmountList.add(insuranceOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(insuranceOrder.getPayAmount());
        }
        
        // 保存套餐订单
        if (Objects.nonNull(rentBatteryMemberCardTriple.getRight())) {
            ElectricityMemberCardOrder electricityMemberCardOrder = (ElectricityMemberCardOrder) (rentBatteryMemberCardTriple.getRight());
            electricityMemberCardOrderService.insert(electricityMemberCardOrder);
            
            orderList.add(electricityMemberCardOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_MEMBER_CARD);
            payAmountList.add(electricityMemberCardOrder.getPayAmount());
            totalPayAmount = totalPayAmount.add(electricityMemberCardOrder.getPayAmount());
            
            if (CollectionUtils.isNotEmpty(query.getUserCouponIds())) {
                // 保存订单所使用的优惠券
                memberCardOrderCouponService.batchInsert(
                        electricityMemberCardOrderService.buildMemberCardOrderCoupon(electricityMemberCardOrder.getOrderId(), new HashSet<>(query.getUserCouponIds())));
                // 修改优惠券状态为核销中
                userCouponService.batchUpdateUserCoupon(
                        electricityMemberCardOrderService.buildUserCouponList(new HashSet<>(query.getUserCouponIds()), UserCoupon.STATUS_IS_BEING_VERIFICATION,
                                electricityMemberCardOrder.getOrderId()));
            }
        }
        
        // 处理支付0元场景
        if (totalPayAmount.doubleValue() <= NumberConstant.ZERO) {
            Triple<Boolean, String, Object> result = tradeOrderService.handleTotalAmountZero(userInfo, orderList, orderTypeList);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return result;
            }
            
            return Triple.of(true, "", null);
        }
        
        try {
            UnionPayOrder unionPayOrder = UnionPayOrder.builder().jsonOrderId(JsonUtil.toJson(orderList)).jsonOrderType(JsonUtil.toJson(orderTypeList))
                    .jsonSingleFee(JsonUtil.toJson(payAmountList)).payAmount(totalPayAmount).tenantId(tenantId).attach(UnionTradeOrder.ATTACH_MEMBERCARD_INSURANCE)
                    .description("免押租电").uid(uid).build();
            WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, wechatPayParamsDetails, userOauthBind.getThirdId(),
                    request);
            return Triple.of(true, null, resultDTO);
        } catch (DecoderException | WechatPayException e) {
            log.error("FREE DEPOSIT HYBRID ERROR! wechat v3 order  error! uid={}", uid, e);
        }
        
        return Triple.of(false, "PAY_TRANSFER.0019", "支付未成功，请联系客服处理");
    }
    
    @Override
    public void freeDepositOrderUpdateStatusTask() {
        
        int offset = 0;
        long timeFlag = System.currentTimeMillis() + 300 * 1000L;
        while (System.currentTimeMillis() < timeFlag) {
            
            List<FreeDepositOrder> list = this.freeDepositOrderMapper.selectEnterpriseRefundingOrder(offset, REFUND_ORDER_LIMIT);
            offset += REFUND_ORDER_LIMIT;
            
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            
            for (FreeDepositOrder entity : list) {
                
                // 获取免押解冻结果
                Triple<Boolean, String, Object> depositOrderStatusResult = this.selectFreeDepositOrderStatus(entity);
                if (Boolean.FALSE.equals(depositOrderStatusResult.getLeft())) {
                    log.error("FREE DEPOSIT TASK ERROR!acquire batteryFreeDepositOrder UN_FROZEN fail,orderId={},uid={}", entity.getOrderId(), entity.getUid());
                    continue;
                }
                
                PxzQueryOrderRsp queryOrderRspData = (PxzQueryOrderRsp) depositOrderStatusResult.getRight();
                if (!Objects.equals(queryOrderRspData.getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
                    log.error("FREE DEPOSIT TASK ERROR!batteryFreeDepositOrder not un_frozen,orderId={},uid={}", entity.getOrderId(), entity.getUid());
                    continue;
                }
                
                // 更新免押订单
                FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                freeDepositOrderUpdate.setId(entity.getId());
                freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
                freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                this.update(freeDepositOrderUpdate);
            }
        }
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return freeDepositOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    private Triple<Boolean, String, Object> generateInsuranceOrder(UserInfo userInfo, FreeBatteryDepositHybridOrderQuery query, ElectricityCabinet electricityCabinet,
            WechatPayParamsDetails wechatPayParamsDetails) {
        if (Objects.isNull(query.getInsuranceId())) {
            return Triple.of(true, "", null);
        }
        
        // 查询保险
        FranchiseeInsurance franchiseeInsurance = franchiseeInsuranceService.queryByIdFromCache(query.getInsuranceId());
        if (Objects.isNull(franchiseeInsurance) || !Objects.equals(franchiseeInsurance.getInsuranceType(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY)) {
            log.warn("CREATE INSURANCE_ORDER WARN,NOT FOUND MEMBER_CARD BY ID={},uid={}", query.getInsuranceId(), userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险!");
        }
        if (ObjectUtil.equal(FranchiseeInsurance.STATUS_UN_USABLE, franchiseeInsurance.getStatus())) {
            log.warn("CREATE INSURANCE_ORDER WARN ,MEMBER_CARD IS UN_USABLE ID={},uid={}", query.getInsuranceId(), userInfo.getUid());
            return Triple.of(false, "100306", "保险已禁用!");
        }
        
        if (Objects.isNull(franchiseeInsurance.getPremium())) {
            log.warn("CREATE INSURANCE_ORDER WARN! payAmount is null ！franchiseeId={},uid={}", query.getInsuranceId(), userInfo.getUid());
            return Triple.of(false, "100305", "未找到保险");
        }
        
        // 生成保险独立订单
        String insuranceOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_INSURANCE, userInfo.getUid());
        InsuranceOrder insuranceOrder = InsuranceOrder.builder().insuranceId(franchiseeInsurance.getId()).insuranceName(franchiseeInsurance.getName())
                .insuranceType(franchiseeInsurance.getInsuranceType()).orderId(insuranceOrderId).cid(franchiseeInsurance.getCid())
                .franchiseeId(franchiseeInsurance.getFranchiseeId()).isUse(InsuranceOrder.NOT_USE).payAmount(franchiseeInsurance.getPremium())
                .forehead(franchiseeInsurance.getForehead()).payType(InsuranceOrder.ONLINE_PAY_TYPE).phone(userInfo.getPhone()).status(InsuranceOrder.STATUS_INIT)
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId()).tenantId(userInfo.getTenantId()).uid(userInfo.getUid())
                .userName(userInfo.getName()).validDays(franchiseeInsurance.getValidDays()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .simpleBatteryType(franchiseeInsurance.getSimpleBatteryType()).paramFranchiseeId(wechatPayParamsDetails.getFranchiseeId())
                .wechatMerchantId(wechatPayParamsDetails.getWechatMerchantId()).build();
        
        return Triple.of(true, null, insuranceOrder);
    }
    
    private Triple<Boolean, String, Object> generateMemberCardOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, FreeBatteryDepositHybridOrderQuery query,
            ElectricityCabinet electricityCabinet, WechatPayParamsDetails wechatPayParamsDetails) {
        
        // 多加盟商版本增加：加盟商一致性校验
        Triple<Boolean, String, Object> calculatePayAmountResult = electricityMemberCardOrderService.calculatePayAmount(batteryMemberCard.getRentPrice(),
                CollectionUtils.isEmpty(query.getUserCouponIds()) ? null : new HashSet<>(query.getUserCouponIds()), batteryMemberCard.getFranchiseeId());
        if (Boolean.FALSE.equals(calculatePayAmountResult.getLeft())) {
            return calculatePayAmountResult;
        }
        BigDecimal payAmount = (BigDecimal) calculatePayAmountResult.getRight();
        
        // 支付金额不能为负数
        if (payAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payAmount = BigDecimal.valueOf(0);
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        Integer payCount = electricityMemberCardOrderService.queryMaxPayCount(userBatteryMemberCard);
        
        ElectricityMemberCardOrder electricityMemberCardOrder = new ElectricityMemberCardOrder();
        electricityMemberCardOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_MEMBERCARD, userInfo.getUid()));
        electricityMemberCardOrder.setCreateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setUpdateTime(System.currentTimeMillis());
        electricityMemberCardOrder.setStatus(ElectricityMemberCardOrder.STATUS_INIT);
        electricityMemberCardOrder.setMemberCardId(query.getMemberCardId().longValue());
        electricityMemberCardOrder.setUid(userInfo.getUid());
        electricityMemberCardOrder.setMaxUseCount(batteryMemberCard.getUseCount());
        electricityMemberCardOrder.setCardName(batteryMemberCard.getName());
        electricityMemberCardOrder.setPayAmount(payAmount);
        electricityMemberCardOrder.setUserName(userInfo.getName());
        electricityMemberCardOrder.setValidDays(batteryMemberCard.getValidDays());
        electricityMemberCardOrder.setTenantId(batteryMemberCard.getTenantId());
        electricityMemberCardOrder.setFranchiseeId(userInfo.getFranchiseeId());
        electricityMemberCardOrder.setPayCount(payCount);
        electricityMemberCardOrder.setRefId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getId().longValue() : null);
        electricityMemberCardOrder.setSource(Objects.nonNull(electricityCabinet) ? ElectricityMemberCardOrder.SOURCE_SCAN : ElectricityMemberCardOrder.SOURCE_NOT_SCAN);
        electricityMemberCardOrder.setStoreId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId());
        electricityMemberCardOrder.setCouponIds(batteryMemberCard.getCouponIds());
        electricityMemberCardOrder.setParamFranchiseeId(wechatPayParamsDetails.getFranchiseeId());
        electricityMemberCardOrder.setWechatMerchantId(wechatPayParamsDetails.getWechatMerchantId());
        
        return Triple.of(true, null, electricityMemberCardOrder);
    }
    
    @Override
    public Triple<Boolean, String, Object> freeBatteryDepositPreCheck() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("FREE DEPOSIT WARN! not found user info! uid={}", uid);
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
    public Triple<Boolean, String, Object> freeCarBatteryDepositPreCheck() {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("FREE DEPOSIT WARN! not found user info! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        Triple<Boolean, String, Object> checkResult = checkUserCanFreeCarBatteryDeposit(uid, userInfo);
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
        // 处理电池免押解冻退款中订单
        batteryFreeDepositRefundingOrder();
    }
    
    private void batteryFreeDepositRefundingOrder() {
        int offset = 0;
        long timeFlag = System.currentTimeMillis() + 300 * 1000L;
        while (System.currentTimeMillis() < timeFlag) {
            
            List<EleRefundOrder> eleRefundOrders = eleRefundOrderService.selectBatteryFreeDepositRefundingOrder(offset, REFUND_ORDER_LIMIT);
            offset += REFUND_ORDER_LIMIT;
            
            if (CollectionUtils.isEmpty(eleRefundOrders)) {
                break;
            }
            
            for (EleRefundOrder eleRefundOrder : eleRefundOrders) {
                // 获取免押订单
                FreeDepositOrder freeDepositOrder = this.selectByOrderId(eleRefundOrder.getOrderId());
                if (Objects.isNull(freeDepositOrder)) {
                    log.error("FREE DEPOSIT TASK ERROR!not found batteryFreeDepositOrder,orderId={}", eleRefundOrder.getOrderId());
                    continue;
                }
                
                // 获取免押解冻结果
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
                
                // 更新押金订单
                EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
                eleRefundOrderUpdate.setId(eleRefundOrder.getId());
                eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
                eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.update(eleRefundOrderUpdate);
                
                // 更新免押订单
                FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
                freeDepositOrderUpdate.setId(freeDepositOrder.getId());
                freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
                freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
                this.update(freeDepositOrderUpdate);
                
                UserInfo updateUserInfo = new UserInfo();
                
                EleRefundOrder carRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, eleRefundOrder.getOrderId())
                        .eq(EleRefundOrder::getTenantId, eleRefundOrder.getTenantId()).eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
                        .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT));
                
                updateUserInfo.setUid(freeDepositOrder.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);
                
                // 更新用户套餐订单为已失效
                electricityMemberCardOrderService.batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(freeDepositOrder.getUid()),
                        ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                
                userBatteryMemberCardService.unbindMembercardInfoByUid(freeDepositOrder.getUid());
                userBatteryDepositService.logicDeleteByUid(freeDepositOrder.getUid());
                userBatteryService.deleteByUid(freeDepositOrder.getUid());
                
                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(freeDepositOrder.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                    // 更新用户保险订单为已失效
                    insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
                }
                
                userInfoService.unBindUserFranchiseeId(freeDepositOrder.getUid());
                
                // 删除用户电池套餐资源包
                userBatteryMemberCardPackageService.deleteByUid(freeDepositOrder.getUid());
                
                // 删除用户电池型号
                userBatteryTypeService.deleteByUid(freeDepositOrder.getUid());
                
                // 删除用户电池服务费
                serviceFeeUserInfoService.deleteByUid(freeDepositOrder.getUid());
                
                // 删除用户分组
                userInfoGroupDetailService.handleAfterRefundDeposit(freeDepositOrder.getUid());
            }
        }
    }
    
    
    private Triple<Boolean, String, Object> checkUserCanFreeBatteryDeposit(Long uid, UserInfo userInfo) {
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("FREE DEPOSIT WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("FREE DEPOSIT WARN! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "电池押金已经缴纳，无需重复缴纳");
        }
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> checkUserCanFreeCarBatteryDeposit(Long uid, UserInfo userInfo) {
        //        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        //        if (!Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL)) {
        //            return Triple.of(false, "100418", "押金免押功能未开启,请联系客服处理");
        //        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("FREE DEPOSIT WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("FREE DEPOSIT WARN! user not auth,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "车辆押金已经缴纳，无需重复缴纳");
        }
        
        if (Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0049", "电池押金已经缴纳，无需重复缴纳");
        }
        
        return Triple.of(true, null, null);
    }
    
    private Triple<Boolean, String, Object> checkUserCanFreeCarDeposit(Long uid, UserInfo userInfo) {
        //        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(TenantContextHolder.getTenantId());
        //        if (!(Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_CAR) || Objects.equals(electricityConfig.getFreeDepositType(), ElectricityConfig.FREE_DEPOSIT_TYPE_ALL))) {
        //            return Triple.of(false, "100418", "押金免押功能未开启,请联系客服处理");
        //        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("FREE DEPOSIT WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("FREE DEPOSIT WARN! user not auth,uid={}", uid);
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
            log.warn("payDeposit  WARN! not found Franchisee ！franchiseeId={},uid={}", freeBatteryDepositQuery.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }
        
        // 型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(freeBatteryDepositQuery.getModel())) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }
            
            // 型号押金
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.warn("payDeposit  WARN! not found modelBatteryDepositList ！franchiseeId={},uid={}", freeBatteryDepositQuery.getFranchiseeId(), userInfo.getUid());
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
            log.warn("payDeposit  WARN! payAmount is null ！franchiseeId{},uid={}", freeBatteryDepositQuery.getFranchiseeId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
        }
        
        // 电池型号
        String batteryType = Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? batteryModelService.acquireBatteryShort(freeBatteryDepositQuery.getModel(),
                userInfo.getTenantId()) : null;
        
        // 生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName())
                .payAmount(depositPayAmount).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId()).franchiseeId(franchisee.getId()).payType(EleDepositOrder.FREE_DEPOSIT_PAYMENT).storeId(null).modelType(franchisee.getModelType())
                .batteryType(batteryType).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
    
    private Triple<Boolean, String, Object> generateBatteryDepositOrderV3(UserInfo userInfo, FreeBatteryDepositQueryV3 freeQuery) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(freeQuery.getProductKey(), freeQuery.getDeviceName());
        
        BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(freeQuery.getMembercardId());
        if (Objects.isNull(batteryMemberCard)) {
            log.warn("FREE BATTERY DEPOSIT WARN! not found batteryMemberCard,mid={},uid={}", freeQuery.getMembercardId(), userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00121", "电池套餐不存在");
        }
        
        if (batteryMemberCard.getDeposit().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return Triple.of(false, "100299", "免押金额不合法");
        }
        
        // 生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName())
                .payAmount(batteryMemberCard.getDeposit()).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId()).payType(EleDepositOrder.FREE_DEPOSIT_PAYMENT)
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId()).modelType(0).mid(freeQuery.getMembercardId())
                .batteryType(null).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> freeDepositTrilateralPay(String orderId, BigDecimal payTransAmt, String remark) {
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("FREE DEPOSIT ERROR! not found user!");
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(uid)) {
            log.warn("FREE DEPOSIT WARN! not found user! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        if (!redisService.setNx(CacheConstant.FREE_DEPOSIT_PAY_LOCK + uid, "1", 5 * 1000L, false)) {
            return Triple.of(false, "100002", "操作频繁，请稍后再试");
        }
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        if (System.currentTimeMillis() - freeDepositOrder.getCreateTime() > FreeDepositOrder.YEAR) {
            log.warn("FREE DEPOSIT WARN! order over one year,orderId={}", orderId);
            return Triple.of(false, "100424", "免押订单已超过1年，无法代扣");
        }
        
        if (Objects.isNull(payTransAmt) || payTransAmt.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("FREE DEPOSIT WARN! freeDepositOrder already AuthToPay,orderId={}", orderId);
            return Triple.of(false, "100425", "代扣金额不能为0");
        }
        
        if (!Objects.equals(freeDepositOrder.getPayStatus(), FreeDepositOrder.PAY_STATUS_INIT)) {
            log.warn("FREE DEPOSIT WARN! freeDepositOrder already AuthToPay,orderId={}", orderId);
            return Triple.of(false, "100412", "免押订单已进行代扣，请勿重复操作");
        }
        
        if (payTransAmt.compareTo(BigDecimal.valueOf(freeDepositOrder.getTransAmt())) > 0) {
            log.warn("FREE DEPOSIT WARN! payTransAmt is illegal,orderId={}", orderId);
            return Triple.of(false, "ELECTRICITY.0007", "扣款金额不能大于支付金额!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(freeDepositOrder.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found user info! uid={}", freeDepositOrder.getUid());
            return Triple.of(false, "ELECTRICITY.0001", "未能查到用户信息");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("FREE DEPOSIT WARN! user is disable,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.warn("FREE DEPOSIT WARN! user not auth,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && !Objects.equals(userInfo.getCarDepositStatus(),
                UserInfo.CAR_DEPOSIT_STATUS_YES) && !Objects.equals(userInfo.getCarBatteryDepositStatus(), YesNoEnum.YES.getCode())) {
            log.warn("FREE DEPOSIT WARN! user not pay deposit,uid={}", userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        String authPayOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.WITHHOLD, userInfo.getUid());
        
        FreeDepositAuthToPayQuery payQuery = FreeDepositAuthToPayQuery.builder().payTransAmt(payTransAmt).authPayOrderId(authPayOrderId).authNo(freeDepositOrder.getAuthNo())
                .uid(uid).tenantId(userInfo.getTenantId()).orderId(orderId).channel(freeDepositOrder.getChannel()).build();
        Triple<Boolean, String, Object> authedToPayTriple = freeDepositService.authToPay(payQuery);
        // 代扣调用失败则返回，否则生成代扣记录，状态为初始化
        if (!authedToPayTriple.getLeft()) {
            return authedToPayTriple;
        }
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(FreeDepositOrder.PAY_STATUS_DEALING);
        // 累计代扣金额
        freeDepositOrderUpdate.setWithheldAmt(freeDepositOrder.getWithheldAmt() + payTransAmt.doubleValue());
        freeDepositOrderUpdate.setPayTransAmt(freeDepositOrder.getTransAmt() - payTransAmt.doubleValue());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);
        
        // 代扣记录
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        
        freeDepositAlipayHistory.setAuthPayOrderId(authPayOrderId);
        freeDepositAlipayHistory.setOrderId(freeDepositOrder.getOrderId());
        freeDepositAlipayHistory.setUid(freeDepositOrder.getUid());
        freeDepositAlipayHistory.setName(freeDepositOrder.getRealName());
        freeDepositAlipayHistory.setPhone(freeDepositOrder.getPhone());
        freeDepositAlipayHistory.setIdCard(freeDepositOrder.getIdCard());
        freeDepositAlipayHistory.setOperateName(user.getName());
        freeDepositAlipayHistory.setOperateUid(user.getUid());
        freeDepositAlipayHistory.setPayAmount(BigDecimal.valueOf(freeDepositOrder.getTransAmt()));
        freeDepositAlipayHistory.setAlipayAmount(payTransAmt);
        freeDepositAlipayHistory.setType(freeDepositOrder.getDepositType());
        freeDepositAlipayHistory.setPayStatus(FreeDepositAlipayHistory.PAY_STATUS_DEALING);
        freeDepositAlipayHistory.setRemark(remark);
        freeDepositAlipayHistory.setCreateTime(System.currentTimeMillis());
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistory.setStoreId(freeDepositOrder.getStoreId());
        freeDepositAlipayHistory.setFranchiseeId(freeDepositOrder.getFranchiseeId());
        freeDepositAlipayHistory.setTenantId(TenantContextHolder.getTenantId());
        freeDepositAlipayHistoryService.insert(freeDepositAlipayHistory);
        
        delayFreeProducer.sendDelayFreeMessage(freeDepositOrder.getOrderId(),MqProducerConstant.AUTH_APY_TAG_NAME);
        
        return Triple.of(true, "", "授权转支付交易处理中！");
    }
    
    
    @Override
    public Triple<Boolean, String, Object> syncAuthPayStatus(String orderId) {
        
        FreeDepositOrder freeDepositOrder = this.selectByOrderId(orderId);
        if (Objects.isNull(freeDepositOrder) || !Objects.equals(freeDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE DEPOSIT WARN! not found freeDepositOrder,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }
        FreeDepositAlipayHistory alipayHistory = freeDepositAlipayHistoryService.queryByOrderId(orderId);
        if (Objects.isNull(alipayHistory)) {
            log.warn("FREE DEPOSIT WARN! not found alipayHistory,orderId={}", orderId);
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        // 查询代扣状态
        FreeDepositAuthToPayStatusQuery query = FreeDepositAuthToPayStatusQuery.builder().uid(freeDepositOrder.getUid()).tenantId(freeDepositOrder.getTenantId())
                .orderId(freeDepositOrder.getOrderId()).authPayOrderId(alipayHistory.getAuthPayOrderId()).channel(freeDepositOrder.getChannel()).build();
        AuthPayStatusBO authPayStatusBO = freeDepositService.queryAuthToPayStatus(query);
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(authPayStatusBO.getOrderStatus());
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(freeDepositOrderUpdate);
        
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setOrderId(freeDepositOrder.getOrderId());
        freeDepositAlipayHistory.setPayStatus(authPayStatusBO.getOrderStatus());
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistoryService.updateByOrderId(freeDepositAlipayHistory);
        
        return Triple.of(true, null, null);
    }
}
