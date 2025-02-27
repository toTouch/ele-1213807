package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xiliulou.electricity.bo.EleDepositRefundBO;
import com.xiliulou.electricity.bo.base.BasePayConfig;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.UserOperateRecordConstant;
import com.xiliulou.electricity.constant.WechatPayConstant;
import com.xiliulou.electricity.converter.PayConfigConverter;
import com.xiliulou.electricity.converter.model.OrderRefundParamConverterModel;
import com.xiliulou.electricity.entity.BatteryMembercardRefundOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.EleRefundOrderHistory;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.RefundOrder;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.CheckPayParamsResultEnum;
import com.xiliulou.electricity.enums.RefundPayOptTypeEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.EleRefundOrderMapper;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.query.UnFreeDepositOrderQuery;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.service.BatteryMembercardRefundOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderHistoryService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.FreeDepositService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.pay.PayConfigBizService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleRefundOrderVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.pay.base.PayServiceDispatcher;
import com.xiliulou.pay.base.dto.BasePayOrderRefundDTO;
import com.xiliulou.pay.base.exception.PayException;
import com.xiliulou.pay.base.request.BaseOrderRefundCallBackResource;
import com.xiliulou.pay.base.request.BasePayRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositUnfreezeRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzDepositUnfreezeRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
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

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_INIT;

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
    PayConfigConverter payConfigConverter;
    
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
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    FreeDepositOrderService freeDepositOrderService;
    
    @Autowired
    PxzConfigService pxzConfigService;
    
    @Autowired
    PxzDepositService pxzDepositService;
    
    @Autowired
    FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    InsuranceOrderService insuranceOrderService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Autowired
    BatteryMembercardRefundOrderService batteryMembercardRefundOrderService;
    
    @Autowired
    private FranchiseeServiceImpl franchiseeService;
    
    @Resource
    private PayServiceDispatcher payServiceDispatcher;
    
    @Resource
    private PayConfigBizService payConfigBizService;
    
    @Resource
    private TenantService tenantService;
    
    
    @Resource
    private FreeDepositService freeDepositService;
    
    @Resource
    private InstallmentBizService installmentBizService;
    
    @Resource
    private InstallmentDeductionRecordService installmentDeductionRecordService;
    
    /**
     * 新增数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
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
    public Integer update(EleRefundOrder eleRefundOrder) {
        return this.eleRefundOrderMapper.updateById(eleRefundOrder);
    }
    
    @Override
    public List<EleRefundOrder> selectBatteryFreeDepositRefundingOrder(Integer offset, Integer size) {
        return this.eleRefundOrderMapper.selectBatteryFreeDepositRefundingOrder(offset, size);
    }
    
    @Override
    public List<EleRefundOrder> selectCarFreeDepositRefundingOrder(int offset, Integer size) {
        return this.eleRefundOrderMapper.selectCarFreeDepositRefundingOrder(offset, size);
    }
    
    
    @Override
    public BasePayOrderRefundDTO commonCreateRefundOrderV2(RefundOrder refundOrder, BasePayConfig payConfig, HttpServletRequest request) throws PayException {
        
        // 第三方订单号
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
        
        OrderRefundParamConverterModel model = new OrderRefundParamConverterModel();
        model.setRefundId(refundOrder.getRefundOrderNo());
        model.setOrderId(tradeOrderNo);
        model.setReason("退款");
        model.setRefund(refundOrder.getRefundAmount());
        model.setTotal(total);
        model.setCurrency("CNY");
        model.setPayConfig(payConfig);
        model.setTenantId(electricityTradeOrder.getTenantId());
        model.setFranchiseeId(electricityTradeOrder.getPayFranchiseeId());
        model.setRefundType(RefundPayOptTypeEnum.BATTERY_DEPOSIT_REFUND_CALL_BACK.getCode());
        
        BasePayRequest basePayRequest = payConfigConverter.converterOrderRefund(model);
        
        return payServiceDispatcher.refund(basePayRequest);
    }
    
    @Override
    public Pair<Boolean, Object> notifyDepositRefundOrder(BaseOrderRefundCallBackResource callBackResource) {
        //幂等加锁
        
        if (!redisService.setNx(WechatPayConstant.REFUND_ORDER_ID_CALL_BACK + callBackResource.getOutTradeNo(), String.valueOf(System.currentTimeMillis()), 10 * 1000L, false)) {
            return Pair.of(false, "频繁操作!");
        }
        // 回调参数
        String tradeRefundNo = callBackResource.getOutRefundNo();
        String outTradeNo = callBackResource.getOutTradeNo();
        
        // 退款订单
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, tradeRefundNo));
        if (Objects.isNull(eleRefundOrder)) {
            log.error("NOTIFY_MEMBER_ORDER ERROR ,NOT FOUND ELECTRICITY_TRADE_ORDER ORDER_NO={}", tradeRefundNo);
            return Pair.of(false, "未找到退款订单!");
        }
        if (ObjectUtil.notEqual(EleRefundOrder.STATUS_REFUND, eleRefundOrder.getStatus())) {
            log.error("NOTIFY_MEMBER_ORDER ERROR , ELECTRICITY_TRADE_ORDER  STATUS IS NOT INIT, ORDER_NO={}", tradeRefundNo);
            return Pair.of(false, "退款订单已处理");
        }
        
        // 获取押金订单号
        Pair<Boolean, Object> findDepositOrderNOResult = findDepositOrder(outTradeNo, eleRefundOrder);
        if (Boolean.FALSE.equals(findDepositOrderNOResult.getLeft())) {
            return findDepositOrderNOResult;
        }
        
        String orderNo = (String) findDepositOrderNOResult.getRight();
        
        Integer refundOrderStatus = EleRefundOrder.STATUS_FAIL;
        boolean result = false;
        if (callBackResource.refundStatusIsSuccess()) {
            refundOrderStatus = EleRefundOrder.STATUS_SUCCESS;
            result = true;
        } else {
            log.error("NOTIFY REDULT PAY FAIL,ORDER_NO={}" + tradeRefundNo);
        }
        
        // 租电池退押金
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
                
                // 更新用户套餐订单为已失效
                electricityMemberCardOrderService
                        .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
                
                userBatteryService.deleteByUid(userInfo.getUid());
                
                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                    // 更新用户保险订单为已失效
                    insuranceOrderService.updateUseStatusByOrderId(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);

                    // 是否存在未生效的保险
                    InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY, InsuranceOrder.NOT_EFFECTIVE);
                    if (Objects.nonNull(insuranceOrder)) {
                        insuranceOrderService.updateUseStatusByOrderId(insuranceOrder.getOrderId(), InsuranceOrder.INVALID);
                    }
                }
                
                // 退押金解绑用户所属加盟商
                userInfoService.unBindUserFranchiseeId(userInfo.getUid());
                
                // 删除用户电池套餐资源包
                userBatteryMemberCardPackageService.deleteByUid(userInfo.getUid());
                
                // 删除用户电池型号
                userBatteryTypeService.deleteByUid(userInfo.getUid());
                
                // 删除用户电池服务费
                serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
                
                // 解约分期签约，如果有的话
                installmentBizService.terminateForReturnDeposit(userInfo.getUid());
            }
        }
        
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setStatus(refundOrderStatus);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderMapper.updateById(eleRefundOrderUpdate);
        return Pair.of(result, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> handleRefundOrder(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid, Integer offlineRefund,
            HttpServletRequest request) {
    
        Boolean getLockSuccess = redisService.setNx(String.format(CacheConstant.BATTERY_DEPOSIT_REFUND_AUDIT_LOCK_KEY,refundOrderNo), IdUtil.fastSimpleUUID(), 10 * 1000L, false);
        if (!getLockSuccess) {
            throw new BizException("ELECTRICITY.0034", "操作频繁");
        }
        
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo).eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUSE_REFUND));
        if (Objects.isNull(eleRefundOrder)) {
            return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            return Triple.of(false, "100247", "用户信息不存在");
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            return Triple.of(false, "ELECTRICITY.100273", "未查询到支付订单，操作失败，请联系客服处理");
        }
        
        // 校验退款金额
        if (Objects.nonNull(refundAmount)) {
            if (refundAmount.compareTo(eleRefundOrder.getRefundAmount()) > 0) {
                log.warn("REFUND ORDER WARN!refundAmount is illegal,refoundOrderNo={},uid={}", refundOrderNo, uid);
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
        
        // 拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
//            eleRefundOrderUpdate.setPaymentChannel(eleDepositOrder.getPaymentChannel());
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return Triple.of(true, "", null);
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setUid(userInfo.getUid());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!org.springframework.util.CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return Triple.of(false, "301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        // 修改企业用户代付状态为已过期
        enterpriseChannelUserService.updatePaymentStatusForRefundDeposit(userInfo.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
        
        // 支付配置校验未通过，原线上退款需转为线下退款
        if (Objects.equals(offlineRefund, CheckPayParamsResultEnum.FAIL.getCode())) {
            eleRefundOrderUpdate.setPayType(EleRefundOrder.PAY_TYPE_OFFLINE);
            log.info("CHANGE TO OFFLINE REFUND! refundOrderNo={}", eleRefundOrder.getRefundOrderNo());
        }
        
        // 退款0元及线上强制转线下退款处理
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0 || Objects.equals(offlineRefund, CheckPayParamsResultEnum.FAIL.getCode())) {
            return handleBatteryZeroDepositAndOfflineRefundOrder(eleRefundOrderUpdate, userInfo);
        }
        
        // 置于此处为了避免干扰线下退款，将异常抛出是为了回滚避免生成数据错误的订单，避免把数据修改成错误的中间态
        BasePayConfig basePayConfig = null;
        try {
            basePayConfig = payConfigBizService.queryPayParams(eleDepositOrder.getPaymentChannel(), TenantContextHolder.getTenantId(), eleDepositOrder.getParamFranchiseeId(),null);
        } catch (PayException e) {
            log.warn("BATTERY DEPOSIT WARN!not found pay params,refundOrderNo={}", eleRefundOrder.getRefundOrderNo());
            throw new BizException("PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
        }
        if (Objects.isNull(basePayConfig)) {
            log.warn("BATTERY DEPOSIT WARN!not found pay params,refundOrderNo={}", eleRefundOrder.getRefundOrderNo());
            throw new BizException("100307", "未配置支付参数!");
        }
        
        try {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
//            eleRefundOrderUpdate.setPaymentChannel(eleDepositOrder.getPaymentChannel());
            eleRefundOrderService.update(eleRefundOrderUpdate);
            
            RefundOrder refundOrder = RefundOrder.builder().orderId(eleRefundOrder.getOrderId()).refundOrderNo(eleRefundOrder.getRefundOrderNo())
                    .payAmount(eleRefundOrder.getPayAmount()).refundAmount(eleRefundOrderUpdate.getRefundAmount()).build();
            eleRefundOrderService.commonCreateRefundOrderV2(refundOrder, basePayConfig, request);
            
            
            
            return Triple.of(true, "", null);
        } catch (Exception e) {
            log.error("REFUND ORDER ERROR! wechat v3 refund  error! ", e);
        }
        
        // 提交失败
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_FAIL);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
//        eleRefundOrderUpdate.setPaymentChannel(eleRefundOrder.getPaymentChannel());
        eleRefundOrderService.update(eleRefundOrderUpdate);
        
        return Triple.of(false, "PAY_TRANSFER.0020", "支付调用失败，请检查相关配置");
    }
    
    @Override
    public Triple<Boolean, String, Object> batteryFreeDepostRefundAudit(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid) {
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo).eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER).in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT));
        //.in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUSE_REFUND));
        if (Objects.isNull(eleRefundOrder)) {
            log.warn("FREE REFUND ORDER WARN! eleRefundOrder is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("FREE REFUND ORDER WARN!userInfo is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("FREE REFUND ORDER WARN!eleDepositOrder is null,orderId={},uid={}", eleRefundOrder.getOrderId(), uid);
            return Triple.of(false, "ELECTRICITY.0015", "换电订单不存在");
        }
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleRefundOrder.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("REFUND ORDER WARN! not found freeDepositOrder,uid={}", userInfo.getUid());
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        EleRefundOrder carRefundOrder = null;
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            carRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, eleRefundOrder.getOrderId())
                    .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId()).eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
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
                // return Triple.of(true, "", null);
            }
        }
        
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        // eleRefundOrderUpdate.setRefundAmount(refundAmount);
        eleRefundOrderUpdate.setErrMsg(errMsg);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return Triple.of(true, "", null);
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setUid(userInfo.getUid());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!org.springframework.util.CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return Triple.of(false, "301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        // 处理电池免押订单退款
        if (!Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            log.warn("FREE REFUND ORDER WARN!depositOrder payType is illegal,orderId={},uid={}", eleRefundOrder.getOrderId(), uid);
            return Triple.of(false, "100406", "订单非免押支付");
        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.warn("REFUND ORDER WARN! not found pxzConfig,uid={}", userInfo.getUid());
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }
        
        // 如果车电一起免押，检查用户是否归还车辆
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY) && Objects.equals(userInfo.getCarRentStatus(),
                UserInfo.CAR_RENT_STATUS_YES)) {
            log.warn("REFUND ORDER WARN! user not return car,uid={}", userInfo.getUid());
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
        
        // 如果解冻成功
        if (Objects.equals(pxzUnfreezeDepositCommonRsp.getData().getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
            // 更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositOrderService.update(freeDepositOrderUpdate);
            
            // 更新退款订单
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUND);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);
            
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);
            
            // 更新用户套餐订单为已失效
            electricityMemberCardOrderService
                    .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(uid), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
            
            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
            userBatteryService.deleteByUid(userInfo.getUid());
            
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
                
                // 更新用户保险订单为已失效
                insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);

                // 是否存在未生效的保险
                InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY, InsuranceOrder.NOT_EFFECTIVE);
                if (Objects.nonNull(insuranceOrder)){
                    insuranceOrderService.updateUseStatusByOrderId(insuranceOrder.getOrderId(), InsuranceOrder.INVALID);
                }
            }
            
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
            
            // 修改企业用户代付状态为代付过期
            enterpriseChannelUserService.updatePaymentStatusForRefundDeposit(userInfo.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
            
            // 解约分期签约，如果有的话
            installmentBizService.terminateForReturnDeposit(userInfo.getUid());
            
            return Triple.of(true, "", "免押解冻成功");
        }
        
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        // 更新退款订单
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
    public Triple<Boolean, String, Object> batteryFreeDepostRefundAuditV2(String refundOrderNo, String errMsg, Integer status, BigDecimal refundAmount, Long uid) {
        EleRefundOrder eleRefundOrder = eleRefundOrderMapper.selectOne(
                new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getRefundOrderNo, refundOrderNo).eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId())
                        .eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER).in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT));
        
        if (Objects.isNull(eleRefundOrder)) {
            log.error("FREE REFUND ORDER ERROR! eleRefundOrder is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0015", "未找到退款订单!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("FREE REFUND ORDER ERROR!userInfo is null,refoundOrderNo={},uid={}", refundOrderNo, uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
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
        
        if (!Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            Integer payingByOrderId = freeDepositAlipayHistoryService.queryPayingByOrderId(freeDepositOrder.getOrderId());
            // 如果存在代扣的免押订单，则不允许退押
            if (!Objects.equals(payingByOrderId, NumberConstant.ZERO)) {
                return Triple.of(false, "100426", "当前有正在执行中的免押代扣，无法退押");
            }
        }
        
        EleRefundOrder carRefundOrder = null;
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            carRefundOrder = eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, eleRefundOrder.getOrderId())
                    .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId()).eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER)
                    .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT));
            
            if (Objects.nonNull(carRefundOrder) && Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
                EleRefundOrder carRefundOrderUpdate = new EleRefundOrder();
                carRefundOrderUpdate.setId(carRefundOrder.getId());
                carRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
                carRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
                eleRefundOrderService.update(carRefundOrderUpdate);
            }
        }
        
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setErrMsg(errMsg);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        
        // 拒绝退款
        if (Objects.equals(status, EleRefundOrder.STATUS_REFUSE_REFUND)) {
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_REFUSE_REFUND);
            eleRefundOrderService.update(eleRefundOrderUpdate);
            return Triple.of(true, "", null);
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setUid(userInfo.getUid());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!org.springframework.util.CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return Triple.of(false, "301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        if (Objects.nonNull(refundAmount) && refundAmount.compareTo(BigDecimal.ZERO) != 0 && refundAmount.compareTo(BigDecimal.valueOf(freeDepositOrder.getPayTransAmt())) > 0) {
            log.warn("FREE DEPOSIT WARN! refundAmount is over payTransAmt,orderId={}", freeDepositOrder.getOrderId());
            return Triple.of(false, "100434", "退押失败，超过代扣金额");
        }
        
        
        // 处理电池免押订单退款
        if (!Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            log.error("FREE REFUND ORDER ERROR!depositOrder payType is illegal,orderId={},uid={}", eleRefundOrder.getOrderId(), uid);
            return Triple.of(false, "100406", "订单非免押支付");
        }
        
        
        // 如果车电一起免押，检查用户是否归还车辆
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY) && Objects.equals(userInfo.getCarRentStatus(),
                UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("REFUND ORDER ERROR! user not return car,uid={}", userInfo.getUid());
            return Triple.of(false, "100253", "用户已绑定车辆");
        }
        
        // 三方解冻
        UnFreeDepositOrderQuery query = UnFreeDepositOrderQuery.builder().channel(freeDepositOrder.getChannel()).authNO(freeDepositOrder.getAuthNo()).orderId(freeDepositOrder.getOrderId())
                .subject("电池押金解冻").tenantId(freeDepositOrder.getTenantId()).uid(freeDepositOrder.getUid()).amount(freeDepositOrder.getPayTransAmt().toString()).build();
        Triple<Boolean, String, Object> triple = freeDepositService.unFreezeDeposit(query);
        if (!triple.getLeft()) {
            return Triple.of(false, "100406", triple.getRight());
        }
        
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        // 更新退款订单
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
    /**
     * 电池免押退押金
     *
     * @return
     */
    @Override
    public Triple<Boolean, String, Object> batteryFreeDepositRefund(String errMsg, Long uid) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("REFUND ORDER WARN!userInfo is null,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        // 设置企业信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(userInfo.getUid());
        if (Objects.nonNull(enterpriseChannelUserVO) && Objects.equals(enterpriseChannelUserVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.warn("ELE DEPOSIT WARN! battery free deposit refund channel user is disable! uid={}", userInfo.getUid());
            return Triple.of(false, "120303", "您已是渠道用户，请联系站点开启自主续费后，进行退押操作");
        }
        
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        if (Objects.isNull(pxzConfig) || StringUtils.isBlank(pxzConfig.getAesKey()) || StringUtils.isBlank(pxzConfig.getMerchantCode())) {
            log.warn("REFUND ORDER WARN! not found pxzConfig,uid={}", uid);
            return Triple.of(false, "100400", "免押功能未配置相关信息,请联系客服处理");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("REFUND ORDER WARN! user is disable! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.warn("REFUND ORDER WARN! user is not rent deposit,uid={}", uid);
            return Triple.of(false, "100238", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("REFUND ORDER WARN! user membercard is disable,uid={}", uid);
            return Triple.of(false, "100211", "用户套餐已暂停！");
        }
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("REFUND ORDER WARN! disable member card is reviewing,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.100003", "套餐暂停正在审核中");
        }
        
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("REFUND ORDER WARN! not return battery,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0046", "未退还电池");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("REFUND ORDER WARN！userBatteryDeposit is null,uid={}", uid);
            return Triple.of(false, "100247", "用户电池押金信息不存在");
        }
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("REFUND ORDER WARN! not found freeDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setUid(userInfo.getUid());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!org.springframework.util.CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return Triple.of(false, "301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_UN_FREEZING)) {
            return Triple.of(false, "", "免押退款中，请稍后！");
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("REFUND ORDER WARN! not found eleDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }
        
        // 企业渠道订单暂不支持退押
        if (PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode().equals(eleDepositOrder.getOrderType())) {
            log.warn("REFUND ORDER WARN! deposit order is enterprise channel, can't refund deposit, uid={}, orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100032", "企业渠道订单暂不支持退押,请联系企业负责人");
        }
        
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return Triple.of(false, "100018", "押金退款审核中");
        }
        
        List<EleRefundOrder> refundOrders = eleRefundOrderMapper.selectList(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, userBatteryDeposit.getOrderId())
                .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId()).eq(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT)
                .eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER));
        
        if (!CollectionUtils.isEmpty(refundOrders)) {
            log.warn("REFUND ORDER WARN! Refund in progress ,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100031", "不能重复退押金");
        }
/*
        //如果车电一起免押，检查用户是否归还车辆
        EleDepositOrder carDepositOrder = null;
        UserCarDeposit userCarDeposit = null;
        if (Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY)) {
            if(Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)){
                log.error("REFUND ORDER ERROR! user not return car,uid={}", userInfo.getUid());
                return Triple.of(false, "100253", "用户已绑定车辆");
            }

            if (!Objects.equals(userInfo.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
                log.error("ELE CAR REFUND ERROR! user is not rent deposit,uid={}", uid);
                return Triple.of(false, "100238", "未缴纳押金");
            }


            userCarDeposit = userCarDepositService.selectByUidFromCache(uid);
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
    */
        // 获取订单代扣信息计算返还金额
        BigDecimal refundAmount = eleDepositOrder.getPayAmount();
        FreeDepositAlipayHistory freeDepositAlipayHistory = freeDepositAlipayHistoryService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.nonNull(freeDepositAlipayHistory)) {
            refundAmount = eleDepositOrder.getPayAmount().subtract(freeDepositAlipayHistory.getAlipayAmount());
            
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
            log.warn("Pxz WARN! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100402", "免押查询失败！");
        }
        
        if (!pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
            return Triple.of(false, "100402", pxzDepositUnfreezeRspPxzCommonRsp.getRespDesc());
        }
        
        if (Objects.equals(pxzDepositUnfreezeRspPxzCommonRsp.getData().getAuthStatus(), FreeDepositOrder.AUTH_UN_FROZEN)) {
            // 更新免押订单状态
            FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
            freeDepositOrderUpdate.setId(freeDepositOrder.getId());
            freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
            freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
            freeDepositOrderService.update(freeDepositOrderUpdate);
            
            // 生成退款订单
            EleRefundOrder eleRefundOrder = EleRefundOrder.builder().orderId(eleDepositOrder.getOrderId())
                    .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, uid)).payAmount(eleDepositOrder.getPayAmount())
                    .refundAmount(eleRefundAmount).status(EleRefundOrder.STATUS_SUCCESS).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                    .tenantId(eleDepositOrder.getTenantId()).franchiseeId(userInfo.getFranchiseeId()).payType(eleDepositOrder.getPayType()).paymentChannel(eleDepositOrder.getPaymentChannel())
                    .build();
            eleRefundOrderService.insert(eleRefundOrder);
            
            // 更新用户状态
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(uid);
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);
            
            // 更新用户套餐订单为已失效
            electricityMemberCardOrderService
                    .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(uid), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
            
            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
            userBatteryService.deleteByUid(userInfo.getUid());
            
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
                
                // 更新用户保险订单为已失效
                insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
            }
            
            // 退押金解绑用户所属加盟商
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
            
            // 删除用户电池套餐资源包
            userBatteryMemberCardPackageService.deleteByUid(userInfo.getUid());
            
            // 删除用户电池型号
            userBatteryTypeService.deleteByUid(userInfo.getUid());
            
            // 删除用户电池服务费
            serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
            
            // 修改企业用户代付状态为代付过期
            enterpriseChannelUserService.updatePaymentStatusForRefundDeposit(userInfo.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
            
            // 解约分期签约，如果有的话
            installmentBizService.terminateForReturnDeposit(userInfo.getUid());
            
            return Triple.of(true, "", null);
        }
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        // 生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder().orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, uid)).payAmount(eleDepositOrder.getPayAmount())
                .refundAmount(eleRefundAmount).status(EleRefundOrder.STATUS_REFUND).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId()).franchiseeId(userInfo.getFranchiseeId()).payType(eleDepositOrder.getPayType()).paymentChannel(eleDepositOrder.getPaymentChannel())
                .build();
        eleRefundOrderService.insert(eleRefundOrder);
        
        return Triple.of(true, "100413", "免押押金解冻中");
    }
    
    
    
    /**
     * 电池免押退押金
     *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> batteryFreeDepositRefundV2(String errMsg, Long uid, BigDecimal refundMoney) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.warn("REFUND ORDER WARN!userInfo is null,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0001", "未找到用户");
        }
        
        // 设置企业信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(userInfo.getUid());
        if (Objects.nonNull(enterpriseChannelUserVO) && Objects.equals(enterpriseChannelUserVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.warn("ELE DEPOSIT WARN! battery free deposit refund channel user is disable! uid={}", userInfo.getUid());
            return Triple.of(false, "120303", "您已是渠道用户，请联系站点开启自主续费后，进行退押操作");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("REFUND ORDER WARN! user is disable! uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.warn("REFUND ORDER WARN! user is not rent deposit,uid={}", uid);
            return Triple.of(false, "100238", "未缴纳押金");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("REFUND ORDER WARN! user membercard is disable,uid={}", uid);
            return Triple.of(false, "100211", "用户套餐已暂停！");
        }
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("REFUND ORDER WARN! disable member card is reviewing,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.100003", "套餐暂停正在审核中");
        }
        
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("REFUND ORDER WARN! not return battery,uid={}", uid);
            return Triple.of(false, "ELECTRICITY.0046", "未退还电池");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("REFUND ORDER WARN！userBatteryDeposit is null,uid={}", uid);
            return Triple.of(false, "100247", "用户电池押金信息不存在");
        }
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(freeDepositOrder)) {
            log.warn("REFUND ORDER WARN! not found freeDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100403", "免押订单不存在");
        }
        
        if (Objects.isNull(freeDepositOrder.getPayTransAmt()) || BigDecimal.valueOf(freeDepositOrder.getPayTransAmt()).compareTo(BigDecimal.valueOf(freeDepositOrder.getTransAmt())) > 0) {
            log.warn("FREE DEPOSIT WARN! freeDepositOrder.payTransAmt is 0 ,orderId={}", freeDepositOrder.getOrderId());
            return Triple.of(false, "100434", "剩余可代扣金额超过免押金额");
        }
        
        if (Objects.isNull(refundMoney)) {
            log.warn("FREE DEPOSIT WARN! refundAmount is null or refundAmount > payTransAmt ,orderId={}", freeDepositOrder.getOrderId());
            return Triple.of(false, "100437", "退款金额不能为空");
        }
        
        if (refundMoney.compareTo(BigDecimal.valueOf(freeDepositOrder.getPayTransAmt())) > 0) {
            log.warn("FREE DEPOSIT WARN! refundAmount is null or refundAmount > payTransAmt ,orderId={}", freeDepositOrder.getOrderId());
            return Triple.of(false, "100438", "退款金额不能大于剩余可代扣金额");
        }
        
        if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_UN_FREEZING)) {
            return Triple.of(false, "", "免押退款中，请稍后！");
        }
        
        Integer payingByOrderId = freeDepositAlipayHistoryService.queryPayingByOrderId(freeDepositOrder.getOrderId());
        // 如果存在代扣的免押订单，则不允许退押
        if (!Objects.equals(payingByOrderId, NumberConstant.ZERO)) {
            return Triple.of(false, "100426", "当前有正在执行中的免押代扣，无法退押");
        }
        
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("REFUND ORDER WARN! not found eleDepositOrder,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "ELECTRICITY.0015", "未找到订单");
        }
        
        // 企业渠道订单暂不支持退押
        if (PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode().equals(eleDepositOrder.getOrderType())) {
            log.warn("REFUND ORDER WARN! deposit order is enterprise channel, can't refund deposit, uid={}, orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100032", "企业渠道订单暂不支持退押,请联系企业负责人");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setUid(userInfo.getUid());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!org.springframework.util.CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return Triple.of(false, "301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        Integer refundCount = eleRefundOrderService.queryIsRefundingCountByOrderId(userBatteryDeposit.getOrderId());
        if (refundCount > 0) {
            return Triple.of(false, "100018", "押金退款审核中");
        }
        
        List<EleRefundOrder> refundOrders = eleRefundOrderMapper.selectList(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, userBatteryDeposit.getOrderId())
                .eq(EleRefundOrder::getTenantId, TenantContextHolder.getTenantId()).eq(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT)
                .eq(EleRefundOrder::getRefundOrderType, EleRefundOrder.RENT_CAR_DEPOSIT_REFUND_ORDER));
        
        if (!CollectionUtils.isEmpty(refundOrders)) {
            log.warn("REFUND ORDER WARN! Refund in progress ,uid={},orderId={}", uid, userBatteryDeposit.getOrderId());
            return Triple.of(false, "100031", "不能重复退押金");
        }
        
        
        // 三方解冻
        UnFreeDepositOrderQuery query = UnFreeDepositOrderQuery.builder().channel(freeDepositOrder.getChannel()).authNO(freeDepositOrder.getAuthNo()).orderId(freeDepositOrder.getOrderId())
                .subject("电池免押解冻").tenantId(freeDepositOrder.getTenantId()).uid(freeDepositOrder.getUid()).amount(freeDepositOrder.getPayTransAmt().toString()).build();
        Triple<Boolean, String, Object> triple = freeDepositService.unFreezeDeposit(query);
        if (!triple.getLeft()) {
            return Triple.of(false, "100406", triple.getRight());
        }
        
        // 获取订单代扣信息计算返还金额
        BigDecimal refundAmount = BigDecimal.valueOf(freeDepositOrder.getPayTransAmt());
        
        BigDecimal eleRefundAmount = refundAmount.doubleValue() < 0 ? BigDecimal.ZERO : refundAmount;
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        // 生成退款订单
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder().orderId(eleDepositOrder.getOrderId())
                .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, uid)).payAmount(eleDepositOrder.getPayAmount())
                .refundAmount(eleRefundAmount).status(EleRefundOrder.STATUS_REFUND).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId()).franchiseeId(userInfo.getFranchiseeId()).payType(eleDepositOrder.getPayType()).paymentChannel(eleDepositOrder.getPaymentChannel()).build();
        eleRefundOrderService.insert(eleRefundOrder);
        
        
        return Triple.of(true, "100413", "免押押金解冻中");
    }
    
    /**
     * 处理电池押金0元
     */
    private Triple<Boolean, String, Object> handleBatteryZeroDepositAndOfflineRefundOrder(EleRefundOrder eleRefundOrderUpdate, UserInfo userInfo) {
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);
        
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        // 更新用户套餐订单为已失效
        electricityMemberCardOrderService
                .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        
        userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
        userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
        userBatteryService.deleteByUid(userInfo.getUid());
        
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
        if (Objects.nonNull(insuranceUserInfo)) {
            insuranceUserInfoService.deleteById(insuranceUserInfo);
            
            // 更新用户保险订单为已失效
            insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);

            InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY, InsuranceOrder.NOT_EFFECTIVE);
            if (Objects.nonNull(insuranceOrder)){
                insuranceOrderService.updateUseStatusByOrderId(insuranceOrder.getOrderId(), InsuranceOrder.INVALID);
            }
        }
        
        // 退押金解绑用户所属加盟商
        userInfoService.unBindUserFranchiseeId(userInfo.getUid());
        
        // 更新用户套餐订单为已失效
        electricityMemberCardOrderService
                .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        
        userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
        
        // 删除用户电池套餐资源包
        userBatteryMemberCardPackageService.deleteByUid(userInfo.getUid());
        
        // 删除用户电池型号
        userBatteryTypeService.deleteByUid(userInfo.getUid());
        
        // 删除用户电池服务费
        serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
        
        // 解约分期签约，如果有的话
        installmentBizService.terminateForReturnDeposit(userInfo.getUid());
        
        return Triple.of(true, "", null);
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
    public R batteryOffLineRefund(String errMsg, BigDecimal refundAmount, Long uid, Integer refundType, Integer offlineRefund) {
        
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("admin payRentCarDeposit  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("admin payRentCarDeposit WARN! not found user,uid={}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            return R.ok();
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 设置企业信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(userInfo.getUid());
        if (Objects.nonNull(enterpriseChannelUserVO) && Objects.equals(enterpriseChannelUserVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.warn("ELE DEPOSIT WARN! battery offline refund channel user is disable! uid={}", user.getUid());
            return R.fail("120303", "您已是渠道用户，请联系站点开启自主续费后，进行退押操作");
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("BATTERY DEPOSIT REFUND WARN! user membercard is disable,uid={}", uid);
            return R.fail("100211", "用户套餐已暂停！");
        }
        
        if (Objects.nonNull(userBatteryMemberCard) && Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
            log.warn("BATTERY DEPOSIT REFUND WARN! disable member card is reviewing,uid={}", uid);
            return R.fail("ELECTRICITY.100003", "停卡正在审核中");
        }
        
        // 判断是否退电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("battery deposit OffLine Refund WARN! not return battery! uid={} ", uid);
            return R.fail("ELECTRICITY.0046", "未退还电池");
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("battery deposit OffLine Refund WARN ,NOT FOUND ELECTRICITY_REFUND_ORDER uid={}", uid);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        // 查找缴纳押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("battery deposit OffLine Refund WARN ,NOT FOUND ELECTRICITY_REFUND_ORDER uid={}", uid);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        // 退押时校验是否有在退租的订单
        List<BatteryMembercardRefundOrder> batteryMembercardRefundOrders = batteryMembercardRefundOrderService.selectRefundingOrderByUid(userInfo.getUid());
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(batteryMembercardRefundOrders)) {
            log.warn("BATTERY DEPOSIT WARN! battery membercard refund review,uid={}", userInfo.getUid());
            return R.fail(false, "100018", "套餐租金退款审核中");
        }
        
        // 退款中
        Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.nonNull(refundStatus) && (Objects.equals(refundStatus, EleRefundOrder.STATUS_REFUND) || Objects.equals(refundStatus, EleRefundOrder.STATUS_INIT))) {
            log.warn("battery deposit OffLine Refund WARN ,Inconsistent refund amount uid={}", uid);
            return R.fail("ELECTRICITY.0051", "押金正在退款中，请勿重复提交");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setUid(userInfo.getUid());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!org.springframework.util.CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        if (Objects.nonNull(refundAmount)) {
            if (refundAmount.compareTo(eleDepositOrder.getPayAmount()) > 0) {
                log.warn("battery deposit OffLine Refund WARN ,refundAmount > payAmount uid={}", uid);
                return R.fail("退款金额不能大于支付金额!");
            }
            
            // 插入修改记录
            EleRefundOrderHistory eleRefundOrderHistory = new EleRefundOrderHistory();
            eleRefundOrderHistory.setRefundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, uid));
            eleRefundOrderHistory.setRefundAmount(refundAmount);
            eleRefundOrderHistory.setCreateTime(System.currentTimeMillis());
            eleRefundOrderHistory.setTenantId(userInfo.getTenantId());
            eleRefundOrderHistoryService.insert(eleRefundOrderHistory);
        } else {
            refundAmount = userBatteryDeposit.getBatteryDeposit();
        }
        
        EleRefundOrder eleRefundOrder = new EleRefundOrder();
        eleRefundOrder.setOrderId(userBatteryDeposit.getOrderId());
        eleRefundOrder.setRefundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, uid));
        eleRefundOrder.setTenantId(userInfo.getTenantId());
        eleRefundOrder.setFranchiseeId(userInfo.getFranchiseeId());
        eleRefundOrder.setCreateTime(System.currentTimeMillis());
        eleRefundOrder.setUpdateTime(System.currentTimeMillis());
        eleRefundOrder.setPayAmount(eleDepositOrder.getPayAmount());
        eleRefundOrder.setErrMsg(errMsg);
        eleRefundOrder.setPayType(eleDepositOrder.getPayType());
        eleRefundOrder.setPaymentChannel(eleDepositOrder.getPaymentChannel());
        // 修改企业用户代付状态为代付过期
        enterpriseChannelUserService.updatePaymentStatusForRefundDeposit(userInfo.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
        
        // 支付配置校验不通过时，需传递offlineRefund，线上支付的押金强制走线下退款
        if (Objects.equals(offlineRefund, CheckPayParamsResultEnum.FAIL.getCode())) {
            eleRefundOrder.setPayType(EleRefundOrder.PAY_TYPE_OFFLINE);
        }
        
        if (Objects.equals(eleRefundOrder.getPayType(), EleRefundOrder.PAY_TYPE_OFFLINE)) {
            // 生成退款订单
            eleRefundOrder.setRefundAmount(refundAmount);
            eleRefundOrder.setStatus(EleRefundOrder.STATUS_SUCCESS);
//            eleRefundOrder.setPaymentChannel(eleDepositOrder.getPaymentChannel());
            eleRefundOrderService.insert(eleRefundOrder);
            
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUid(userInfo.getUid());
            updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
            updateUserInfo.setUpdateTime(System.currentTimeMillis());
            userInfoService.updateByUid(updateUserInfo);
            
            // 更新用户套餐订单为已失效
            electricityMemberCardOrderService
                    .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(uid), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
            
            userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
            
            userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
            
            userBatteryService.deleteByUid(userInfo.getUid());
            
            InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
            if (Objects.nonNull(insuranceUserInfo)) {
                insuranceUserInfoService.deleteById(insuranceUserInfo);
                // 更新用户保险订单为已失效
                insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);

                // 是否存在未生效的保险
                InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY, InsuranceOrder.NOT_EFFECTIVE);
                if (Objects.nonNull(insuranceOrder)){
                    insuranceOrderService.updateUseStatusByOrderId(insuranceOrder.getOrderId(), InsuranceOrder.INVALID);
                }
            }
            
            userInfoService.unBindUserFranchiseeId(uid);
            
            // 删除用户电池套餐资源包
            userBatteryMemberCardPackageService.deleteByUid(uid);
            
            // 删除用户电池型号
            userBatteryTypeService.deleteByUid(uid);
            
            // 删除用户电池服务费
            serviceFeeUserInfoService.deleteByUid(uid);
            
            // 解约分期签约，如果有的话
            installmentBizService.terminateForReturnDeposit(userInfo.getUid());
            
            // 生成后台操作记录
            EleUserOperateRecord eleUserOperateRecord = EleUserOperateRecord.builder().operateModel(EleUserOperateRecord.DEPOSIT_MODEL)
                    .operateContent(EleUserOperateRecord.REFUND_DEPOSIT_CONTENT).operateType(UserOperateRecordConstant.OPERATE_TYPE_BATTERY).operateUid(user.getUid()).uid(uid)
                    .name(user.getUsername()).oldBatteryDeposit(userBatteryDeposit.getBatteryDeposit()).newBatteryDeposit(null).tenantId(TenantContextHolder.getTenantId())
                    .createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).build();
            eleUserOperateRecordService.insert(eleUserOperateRecord);
            return R.ok();
        } else {
            // 退款0元，不捕获异常，成功退款
            if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
                
                eleRefundOrder.setStatus(EleRefundOrder.STATUS_SUCCESS);
                eleRefundOrder.setRefundAmount(refundAmount);
//                eleRefundOrder.setPaymentChannel(eleDepositOrder.getPaymentChannel());
                eleRefundOrderService.insert(eleRefundOrder);
                
                UserInfo updateUserInfo = new UserInfo();
                updateUserInfo.setUid(userInfo.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateByUid(updateUserInfo);
                
                // 更新用户套餐订单为已失效
                electricityMemberCardOrderService
                        .batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(uid), ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                
                userBatteryService.deleteByUid(userInfo.getUid());
                
                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                    // 更新用户保险订单为已失效
                    insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);

                    // 是否存在未生效的保险
                    InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY, InsuranceOrder.NOT_EFFECTIVE);
                    if (Objects.nonNull(insuranceOrder)){
                        insuranceOrderService.updateUseStatusByOrderId(insuranceOrder.getOrderId(), InsuranceOrder.INVALID);
                    }
                }
                
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
                
                userInfoService.unBindUserFranchiseeId(uid);
                
                // 删除用户电池套餐资源包
                userBatteryMemberCardPackageService.deleteByUid(uid);
                
                // 删除用户电池型号
                userBatteryTypeService.deleteByUid(uid);
                
                // 删除用户电池服务费
                serviceFeeUserInfoService.deleteByUid(uid);
                
                // 解约分期签约，如果有的话
                installmentBizService.terminateForReturnDeposit(userInfo.getUid());
                
                return R.ok();
            }
            
            // 置于此处为了避免干扰线下退款，将异常抛出是为了回滚避免生成数据错误的订单，避免把数据修改成错误的中间态
            BasePayConfig basePayConfig = null;
            try {
                basePayConfig = payConfigBizService.queryPayParams(eleDepositOrder.getPaymentChannel(), eleDepositOrder.getTenantId(), eleDepositOrder.getParamFranchiseeId(),null);
            } catch (PayException e) {
                log.warn("BATTERY DEPOSIT WARN!not found pay params,orderId={}", eleDepositOrder.getOrderId());
                throw new BizException("PAY_TRANSFER.0021", "支付配置有误，请检查相关配置");
            }
            if (Objects.isNull(basePayConfig)) {
                log.warn("BATTERY DEPOSIT WARN!not found pay params,orderId={}", eleDepositOrder.getOrderId());
                throw new BizException("100307", "未配置支付参数!");
            }
            
            // 调起退款
            try {
                RefundOrder refundOrder = RefundOrder.builder().orderId(eleRefundOrder.getOrderId()).refundOrderNo(eleRefundOrder.getRefundOrderNo())
                        .payAmount(eleDepositOrder.getPayAmount()).refundAmount(refundAmount).build();
                
                eleRefundOrderService.commonCreateRefundOrderV2(refundOrder, basePayConfig, null);
                // 提交成功
                eleRefundOrder.setStatus(EleRefundOrder.STATUS_REFUND);
                eleRefundOrder.setRefundAmount(refundAmount);
                eleRefundOrder.setUpdateTime(System.currentTimeMillis());
                eleRefundOrderService.insert(eleRefundOrder);
                return R.ok();
            } catch (Exception e) {
                log.error("battery deposit OffLine Refund ERROR! wechat v3 refund  error! ", e);
            }
            // 提交失败
            eleRefundOrder.setStatus(EleRefundOrder.STATUS_FAIL);
            eleRefundOrder.setRefundAmount(refundAmount);
            eleRefundOrder.setUpdateTime(System.currentTimeMillis());
            eleRefundOrder.setPaymentChannel(eleDepositOrder.getPaymentChannel());
            eleRefundOrderService.insert(eleRefundOrder);
            return R.fail("PAY_TRANSFER.0020", "支付调用失败，请检查相关配置");
        }
    }
    
    @Slave
    @Override
    public R queryList(EleRefundQuery eleRefundQuery) {
        List<EleRefundOrderVO> eleRefundOrderVOS = eleRefundOrderMapper.queryList(eleRefundQuery);
        if (CollectionUtils.isEmpty(eleRefundOrderVOS)) {
            return R.ok(new ArrayList<>());
        }
        
        eleRefundOrderVOS.forEach(item -> {
            if (Objects.equals(item.getStatus(), EleRefundOrder.STATUS_REFUSE_REFUND)) {
                item.setRefundAmount(null);
            }
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            item.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());
            
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(item.getOrderId());
            if (Objects.nonNull(freeDepositOrder)) {
                item.setPayTransAmt(freeDepositOrder.getPayTransAmt());
            }
           
            if (!Objects.equals(item.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }
            
            List<FreeDepositAlipayHistory> freeDepositAlipayHistory = freeDepositAlipayHistoryService.queryListByOrderId(item.getOrderId());
            if (CollUtil.isEmpty(freeDepositAlipayHistory)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }
            
            item.setIsFreeDepositAliPay(true);
        });
        return R.ok(eleRefundOrderVOS);
    }
    
    @Override
    public Integer queryCountByOrderId(String orderId, Integer refundOrderType) {
        return eleRefundOrderMapper.selectCount(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId)
                .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_AGREE_REFUND, EleRefundOrder.STATUS_REFUND, EleRefundOrder.STATUS_SUCCESS));
    }
    
    @Override
    public Integer queryIsRefundingCountByOrderId(String orderId) {
        return eleRefundOrderMapper.selectCount(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId)
                .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_AGREE_REFUND, EleRefundOrder.STATUS_REFUND));
    }
    
    @Override
    public Integer queryStatusByOrderId(String orderId) {
        List<EleRefundOrder> eleRefundOrderList = eleRefundOrderMapper
                .selectList(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).orderByDesc(EleRefundOrder::getUpdateTime));
        if (ObjectUtil.isEmpty(eleRefundOrderList)) {
            return null;
        }
        return eleRefundOrderList.get(0).getStatus();
    }
    
    @Slave
    @Override
    public R queryCount(EleRefundQuery eleRefundQuery) {
        return R.ok(eleRefundOrderMapper.queryCount(eleRefundQuery));
    }
    
    @Override
    public Long queryUserInfoIdByRefundOrderNo(String refundOrderNo) {
        return eleRefundOrderMapper.queryUserInfoId(refundOrderNo);
    }
    
    @Slave
    @Override
    public BigDecimal queryTurnOver(Integer tenantId) {
        return Optional.ofNullable(eleRefundOrderMapper.queryTurnOver(tenantId)).orElse(new BigDecimal("0"));
    }
    
    @Slave
    @Override
    public BigDecimal queryTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType, List<Long> franchiseeIds, Integer payType) {
        return Optional.ofNullable(eleRefundOrderMapper.queryTurnOverByTime(tenantId, todayStartTime, refundOrderType, franchiseeIds, payType)).orElse(BigDecimal.valueOf(0));
    }
    
    @Slave
    @Override
    public BigDecimal queryCarRefundTurnOverByTime(Integer tenantId, Long todayStartTime, Integer refundOrderType, List<Long> franchiseeIds, Integer payType) {
        return Optional.ofNullable(eleRefundOrderMapper.queryCarRefundTurnOverByTime(tenantId, todayStartTime, refundOrderType, franchiseeIds, payType))
                .orElse(BigDecimal.valueOf(0));
    }
    
    @Override
    public Long queryRefundTime(String orderId, Integer refundOrderType) {
        return eleRefundOrderMapper.queryRefundTime(orderId, refundOrderType);
    }
    
    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(3) + uid + RandomUtil.randomNumbers(2);
    }
    
    @Override
    public boolean checkDepositOrderIsRefund(String orderId, Integer refundOrderType) {
        EleRefundOrder eleRefundOrder = this.eleRefundOrderMapper.selectOne(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId)
                .in(EleRefundOrder::getStatus, EleRefundOrder.STATUS_SUCCESS, EleRefundOrder.STATUS_REFUND).eq(EleRefundOrder::getRefundOrderType, refundOrderType));
        if (!Objects.isNull(eleRefundOrder)) {
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    /**
     * 获取押金订单号
     *
     * @param outTradeNo
     * @param eleRefundOrder
     * @return
     */
    private Pair<Boolean, Object> findDepositOrder(String outTradeNo, EleRefundOrder eleRefundOrder) {
        String depositOrderNO = null;
        
        // 单独支付
        ElectricityTradeOrder electricityTradeOrder = electricityTradeOrderService.selectTradeOrderByTradeOrderNo(outTradeNo);
        if (Objects.nonNull(electricityTradeOrder)) {
            depositOrderNO = electricityTradeOrder.getOrderNo();
        }
        
        // 混合支付
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
            
            // 租电池押金退款
            if (Objects.equals(eleRefundOrder.getRefundOrderType(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER)) {
                int index = orderTypeList.indexOf(UnionPayOrder.ORDER_TYPE_DEPOSIT);
                if (index < 0) {
                    log.error("NOTIFY UNION PAY ORDER ERROR! not found orderType,orderNo={},orderType={}", outTradeNo, UnionPayOrder.ORDER_TYPE_DEPOSIT);
                    return Pair.of(false, "租电池押金退款订单类型不存!");
                }
                
                depositOrderNO = orderIdList.get(index);
            }
            
            // 租车押金退款
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
    
    @Override
    public List<EleRefundOrder> selectByOrderId(String orderId) {
        return this.eleRefundOrderMapper
                .selectList(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId).eq(EleRefundOrder::getStatus, EleRefundOrder.STATUS_SUCCESS));
    }
    
    public List<EleRefundOrder> selectByOrderIdNoFilerStatus(String orderId) {
        return this.eleRefundOrderMapper.selectList(new LambdaQueryWrapper<EleRefundOrder>().eq(EleRefundOrder::getOrderId, orderId));
    }
    
    @Override
    public EleRefundOrder selectLatestRefundDepositOrder(String paymentOrderNo) {
        return eleRefundOrderMapper.selectLatestRefundDepositOrder(paymentOrderNo);
    }
    
    @Override
    public Integer existByOrderIdAndStatus(String orderId, List<Integer> statusList) {
        return eleRefundOrderMapper.existByOrderIdAndStatus(orderId, statusList);
    }
    
    @Slave
    @Override
    public Integer existsRefundOrderByUid(Long uid) {
        return eleRefundOrderMapper.existsRefundOrderByUid(uid);
    }
    
    @Override
    public Integer updateById(EleRefundOrder eleRefundOrderUpdate) {
        return eleRefundOrderMapper.update(eleRefundOrderUpdate);
    }
    
    @Override
    @Slave
    public R listSuperAdminPage(EleRefundQuery eleRefundQuery) {
        List<EleRefundOrderVO> eleRefundOrderVOS = eleRefundOrderMapper.selectListSuperAdminPage(eleRefundQuery);
        
        if (CollectionUtils.isEmpty(eleRefundOrderVOS)) {
            return R.ok(new ArrayList<>());
        }
        
        eleRefundOrderVOS.forEach(item -> {
            if (Objects.nonNull(item.getTenantId())) {
                Tenant tenant = tenantService.queryByIdFromCache(item.getTenantId());
                item.setTenantName(Objects.nonNull(tenant) ? tenant.getName() : null);
            }
            
            if (Objects.equals(item.getStatus(), EleRefundOrder.STATUS_REFUSE_REFUND)) {
                item.setRefundAmount(null);
            }
            
            if (!Objects.equals(item.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }
        
            List<FreeDepositAlipayHistory> freeDepositAlipayHistory = freeDepositAlipayHistoryService.queryListByOrderId(item.getOrderId());
            if (CollUtil.isEmpty(freeDepositAlipayHistory)) {
                item.setIsFreeDepositAliPay(false);
                return;
            }
            
            item.setIsFreeDepositAliPay(true);
        });
        return R.ok(eleRefundOrderVOS);
    }
    
    @Override
    public Integer updateRefundAmountById(Long id, BigDecimal refundAmount) {
        return eleRefundOrderMapper.updateRefundAmountById(id, refundAmount, System.currentTimeMillis());
    }
    
    @Slave
    @Override
    public List<EleRefundOrder> listByOrderIdList(Integer tenantId, List<String> orderIdList) {
        return eleRefundOrderMapper.selectListByOrderIdList(tenantId, orderIdList);
    }
    
    @Slave
    @Override
    public EleRefundOrder queryLastByOrderId(String orderId) {
        return eleRefundOrderMapper.selectLastByOrderId(orderId);
    }
    
    @Slave
    @Override
    public EleDepositRefundBO queryLastSuccessOrderByUid(Long uid) {
        return eleRefundOrderMapper.selectLastSuccessOrderByUid(uid);
    }
}
