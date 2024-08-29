package com.xiliulou.electricity.callback.impl.business;


import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.enterprise.EnterprisePaymentStatusEnum;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Description: This class is CarBusinessHandler!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/8/27
 **/
@Slf4j
@Service
@AllArgsConstructor
public class BatteryBusinessHandler implements BusinessHandler {
    
    private final UserInfoService userInfoService;
    
    private final UserBatteryDepositService userBatteryDepositService;
    
    private final EleDepositOrderService eleDepositOrderService;
    
    private final UserBatteryTypeService userBatteryTypeService;
    
    private final MemberCardBatteryTypeService memberCardBatteryTypeService;
    
    private final EleRefundOrderService eleRefundOrderService;
    
    private final UserBatteryMemberCardService userBatteryMemberCardService;
    
    private final ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    private final InsuranceUserInfoService insuranceUserInfoService;
    
    private final InsuranceOrderService insuranceOrderService;
    
    private final EnterpriseChannelUserService enterpriseChannelUserService;
    
    private final UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Override
    public boolean support(Integer type) {
        return Objects.equals(type, FreeDepositOrder.DEPOSIT_TYPE_BATTERY);
    }
    
    @Override
    public boolean freeDeposit(FreeDepositOrder order) {
        
        Long uid = order.getUid();
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.warn("userInfo is null, uid is {}", uid);
            return true;
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("handlerFreeDepositSuccess warn! userBatteryDeposit is null, uid is {}", uid);
            return true;
        }
        EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(userBatteryDeposit.getOrderId());
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("handlerFreeDepositSuccess warn! eleDepositOrder is null, orderId is {}", userBatteryDeposit.getOrderId());
            return true;
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
        
        return true;
    }
    
    @Override
    public boolean unfree(FreeDepositOrder order) {
        // 更新退款订单
        EleRefundOrder eleRefundOrder = eleRefundOrderService.selectLatestRefundDepositOrder(order.getOrderId());
        EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
        eleRefundOrderUpdate.setId(eleRefundOrder.getId());
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
        eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
        eleRefundOrderService.update(eleRefundOrderUpdate);
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(order.getUid());
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
        
        return true;
    }
    
    @Override
    public boolean authPay(FreeDepositOrder order) {
        return true; //主要逻辑是处理freeDepositOrder表，已在上层处理，无需处理
    }
}
