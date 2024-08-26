package com.xiliulou.electricity.service.callback.impl;

import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.FreeDepositConstant;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.FreeDepositChannelEnum;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositDataService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.callback.FreeDepositCallBackSerivce;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.pay.deposit.fengyun.constant.FyConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: FreeDepositCallBackServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-08-25 12:59
 */
@Service
@Slf4j
public class FreeDepositCallBackServiceImpl implements FreeDepositCallBackSerivce {
    
    @Resource
    private FreeDepositOrderService freeDepositOrderService;
    
    @Resource
    private FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Resource
    EleRefundOrderService eleRefundOrderService;
    
    @Resource
    private UserInfoService userInfoService;
    
    @Resource
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Resource
    private UserBatteryDepositService userBatteryDepositService;
    
    @Resource
    private InsuranceUserInfoService insuranceUserInfoService;
    
    @Resource
    private InsuranceOrderService insuranceOrderService;
    
    @Resource
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    private UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private FreeDepositDataService freeDepositDataService;
    
    @Resource
    private EleDepositOrderService eleDepositOrderService;
    
    @Resource
    private MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    @Resource
    private UserBatteryTypeService userBatteryTypeService;
    
    @Override
    public String authPayNotified(Integer channel, Map<String, Object> params) {
        
        if (Objects.equals(channel, FreeDepositChannelEnum.PXZ.getChannel())) {
            String orderId = (String) params.get("orderId");
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
            
            Map<String, Object> map = new HashMap<>(1);
            // 如果没有订单则确认成功
            if (Objects.isNull(freeDepositOrder)) {
                log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            Integer orderStatus = (Integer) params.get("orderStatus");
            if (Objects.equals(orderStatus, FreeDepositConstant.AUTH_PXZ_SUCCESS_RECEIVE)) {
                handlerAuthPaySuccess(freeDepositOrder);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            map.put("respCode", FreeDepositConstant.AUTH_PXZ_FAIL_RSP);
            return JsonUtil.toJson(map);
        }
        
        if (Objects.equals(channel, FreeDepositChannelEnum.FY.getChannel())) {
            String tradeType = (String) params.get("tradeType");
            // 蜂云代扣
            if (Objects.equals(tradeType, FyConstants.HANDLE_FUND_TRADE_TYPE_PAY)) {
                // 蜂云只要有回调就一定是成功
                String orderId = (String) params.get("payNo");
                FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
                if (Objects.isNull(freeDepositOrder)) {
                    log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                    return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
                }
                handlerAuthPaySuccess(freeDepositOrder);
                return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
            }
        }
        
        throw new CustomBusinessException("代扣回调异常");
    }
    
    private void handlerAuthPaySuccess(FreeDepositOrder freeDepositOrder) {
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setPayStatus(FreeDepositOrder.PAY_STATUS_DEAL_SUCCESS);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        FreeDepositAlipayHistory freeDepositAlipayHistory = new FreeDepositAlipayHistory();
        freeDepositAlipayHistory.setOrderId(freeDepositOrder.getOrderId());
        freeDepositAlipayHistory.setPayStatus(freeDepositOrderUpdate.getPayStatus());
        freeDepositAlipayHistory.setUpdateTime(System.currentTimeMillis());
        freeDepositAlipayHistoryService.updateByOrderId(freeDepositAlipayHistory);
    }
    
    /**
     * 解冻回调
     *
     * @param channel 渠道
     * @param params  String
     * @return String
     */
    @Override
    public String unFreeNotified(Integer channel, Map<String, Object> params) {
        
        // pxz 免押和解冻使用的同一个回调，所以要根据之前的状态区分
        if (Objects.equals(channel, FreeDepositChannelEnum.PXZ.getChannel())) {
            String orderId = (String) params.get("transId");
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
            
            Map<String, Object> map = new HashMap<>(1);
            // 如果没有订单则确认成功
            if (Objects.isNull(freeDepositOrder)) {
                log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            Integer orderStatus = (Integer) params.get("authStatus");
            
            // 上一个状态是待冻结，本次状态是已冻结
            if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_PENDING_FREEZE) && Objects.equals(orderStatus, FreeDepositOrder.AUTH_FROZEN)) {
                // 免押成功 修改状态逻辑
                handlerUnfree(freeDepositOrder);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            
            
        }
        
        if (Objects.equals(channel, FreeDepositChannelEnum.FY.getChannel())) {
            String tradeType = (String) params.get("tradeType");
            // 解冻
            if (Objects.equals(tradeType, FyConstants.HANDLE_FUND_TRADE_TYPE_UNFREEZE)) {
                // 蜂云只要有回调就一定是成功
                String orderId = (String) params.get("payNo");
                FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
                if (Objects.isNull(freeDepositOrder)) {
                    log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                    return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
                }
                
                // 修改状态逻辑
                handlerUnfree(freeDepositOrder);
                
                return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
            }
        }
        
        throw new CustomBusinessException("代扣回调异常");
    }
    
    
    private void handlerUnfree(FreeDepositOrder freeDepositOrder) {
        
        // 更新免押订单状态
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FROZEN);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
        
        // 更新退款订单
        EleRefundOrder eleRefundOrder = eleRefundOrderService.selectLatestRefundDepositOrder(freeDepositOrder.getOrderId());
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(freeDepositOrder.getUid());
        Long uid = userInfo.getUid();
        
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(userInfo.getUid());
        updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        // 更新用户套餐订单为已失效
        electricityMemberCardOrderService.batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(uid),
                ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
        
        userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
        userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
        
        InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
        if (Objects.nonNull(insuranceUserInfo)) {
            insuranceUserInfoService.deleteById(insuranceUserInfo);
            
            // 更新用户保险订单为已失效
            insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
        }
        
        userInfoService.unBindUserFranchiseeId(userInfo.getUid());
        
        // 修改企业用户代付状态为代付过期
        enterpriseChannelUserService.updatePaymentStatusForRefundDeposit(userInfo.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
        
        // 删除用户分组
        userInfoGroupDetailService.handleAfterRefundDeposit(userInfo.getUid());
        
    }
    
    
    @Override
    public String freeNotified(Integer channel, Map<String, Object> params) {
        
        // pxz 免押和解冻使用的同一个回调，所以要根据之前的状态区分
        if (Objects.equals(channel, FreeDepositChannelEnum.PXZ.getChannel())) {
            Map<String, Object> map = new HashMap<>(1);
            
            String orderId = (String) params.get("transId");
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
            // 如果没有订单则确认成功
            if (Objects.isNull(freeDepositOrder)) {
                log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            
            Integer orderStatus = (Integer) params.get("authStatus");
            // 上一个状态是冻结，本次是解冻
            if (Objects.equals(freeDepositOrder.getAuthStatus(), FreeDepositOrder.AUTH_FROZEN) && Objects.equals(orderStatus, FreeDepositOrder.AUTH_UN_FROZEN)) {
                // 解冻成功 修改状态逻辑
                handlerFreeDepositSuccess(channel, freeDepositOrder);
                map.put("respCode", FreeDepositConstant.AUTH_PXZ_SUCCESS_RSP);
                return JsonUtil.toJson(map);
            }
            
        }
        
        if (Objects.equals(channel, FreeDepositChannelEnum.FY.getChannel())) {
            
            // 蜂云只要有回调就一定是成功
            String orderId = (String) params.get("thirdOrderNo");
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(orderId);
            if (Objects.isNull(freeDepositOrder)) {
                log.error("authPayNotified Error! freeDepositOrder is null, orderId is{}", orderId);
                return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
            }
            
            // 修改状态逻辑
            handlerFreeDepositSuccess(channel, freeDepositOrder);
            
            return FreeDepositConstant.AUTH_FY_SUCCESS_RSP;
            
        }
        
        throw new CustomBusinessException("免押回调异常");
    }
    
    private void handlerFreeDepositSuccess(Integer channel, FreeDepositOrder freeDepositOrder) {
        
        Long uid = freeDepositOrder.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("handlerFreeDepositSuccess warn! userInfo is null, uid is {}", uid);
            return;
        }
        
        // todo 区分渠道扣减免押次数
        if (Objects.equals(channel, FreeDepositChannelEnum.PXZ.getChannel())) {
            freeDepositDataService.deductionFreeDepositCapacity(userInfo.getTenantId(), 1);
        }
        if (Objects.equals(channel, FreeDepositChannelEnum.FY.getChannel())) {
            freeDepositDataService.deductionFyFreeDepositCapacity(userInfo.getTenantId(), 1);
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("handlerFreeDepositSuccess warn! userBatteryDeposit is null, uid is {}", uid);
            return;
        }
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("handlerFreeDepositSuccess warn! eleDepositOrder is null, orderId is {}", userBatteryDeposit.getOrderId());
            return;
        }
        // 更新押金订单状态
        EleDepositOrder eleDepositOrderUpdate = new EleDepositOrder();
        eleDepositOrderUpdate.setId(eleDepositOrder.getId());
        eleDepositOrderUpdate.setStatus(EleDepositOrder.STATUS_SUCCESS);
        eleDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleDepositOrderService.update(eleDepositOrderUpdate);
        
        // 绑定加盟商、更新押金状态
        UserInfo userInfoUpdate = new UserInfo();
        userInfoUpdate.setUid(uid);
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
