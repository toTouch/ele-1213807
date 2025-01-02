package com.xiliulou.electricity.callback.impl.business;


import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.callback.BusinessHandler;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.constant.CacheConstant.UN_FREE_DEPOSIT_USER_INFO_LOCK_KEY;

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
    
    private final UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    private final ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    private final RedisService redisService;
    
    private final InstallmentBizService installmentBizService;
    
    @Override
    public boolean support(Integer type) {
        return Objects.equals(type, FreeDepositOrder.DEPOSIT_TYPE_BATTERY);
    }
    
    @Override
    public boolean freeDeposit(FreeDepositOrder order) {
        try {
            Long uid = order.getUid();
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("userInfo is null, uid is {}", uid);
                return true;
            }
            
//            UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(uid);
//            if (Objects.isNull(userBatteryDeposit)) {
//                log.warn("handlerFreeDepositSuccess warn! userBatteryDeposit is null, uid is {}", uid);
//                return true;
//            }
            
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(order.getOrderId());
            if (Objects.isNull(eleDepositOrder)) {
                log.warn("handlerFreeDepositSuccess warn! eleDepositOrder is null, orderId is {}", order.getOrderId());
                return true;
            }
            //绑定免押订单
            UserBatteryDeposit userBatteryDeposit = new UserBatteryDeposit();
            userBatteryDeposit.setOrderId(eleDepositOrder.getOrderId());
            userBatteryDeposit.setUid(order.getUid());
            userBatteryDeposit.setDid(eleDepositOrder.getMid());
            userBatteryDeposit.setBatteryDeposit(eleDepositOrder.getPayAmount());
            userBatteryDeposit.setDelFlag(UserBatteryDeposit.DEL_NORMAL);
            userBatteryDeposit.setDepositType(UserBatteryDeposit.DEPOSIT_TYPE_FREE);
            userBatteryDeposit.setApplyDepositTime(System.currentTimeMillis());
            userBatteryDeposit.setCreateTime(System.currentTimeMillis());
            userBatteryDeposit.setUpdateTime(System.currentTimeMillis());
            userBatteryDepositService.insertOrUpdate(userBatteryDeposit);
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
            //删除5分钟的二维码
            String userKey = String.format(CacheConstant.FREE_DEPOSIT_USER_INFO_KEY, uid);
            String md5s = redisService.get(userKey);
            if (ObjectUtils.isNotEmpty(md5s)) {
                Arrays.stream(md5s.split(","))
                        .forEach(md5 -> {
                            // TODO: 2024/10/10 兼容历史数据 后续删除
                            String batteryKeyOld = String.format(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2_OLD, uid, md5);
                            if (redisService.hasKey(batteryKeyOld)) {
                                redisService.delete(batteryKeyOld);
                            }
                            String batteryKey = String.format(CacheConstant.ELE_CACHE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2, uid,md5);
                            if (redisService.hasKey(batteryKey)) {
                                redisService.delete(batteryKey);
                            }
                            String enterpriseKey = String.format(CacheConstant.ELE_CACHE_ENTERPRISE_BATTERY_FREE_DEPOSIT_ORDER_GENERATE_LOCK_KEY_V2, uid,md5);
                            if (redisService.hasKey(enterpriseKey)) {
                                redisService.delete(enterpriseKey);
                            }
                        });
                redisService.delete(userKey);
            }
            log.info("Battery/battery electronics order no deposit callback completed, order number: {}",order.getOrderId());
        }catch (Exception e){
            log.error("battery freeDeposit error!", e);
            return false;
        }
        return true;
    }
    
    @Override
    public boolean unfree(FreeDepositOrder order) {
        if (!redisService.hasKey(String.format(UN_FREE_DEPOSIT_USER_INFO_LOCK_KEY, order.getOrderId()))){
            return false;
        }
        try {
            // 更新退款订单
            EleRefundOrder eleRefundOrder = eleRefundOrderService.selectLatestRefundDepositOrder(order.getOrderId());
            EleDepositOrder eleDepositOrder = eleDepositOrderService.queryByOrderId(order.getOrderId());
            if (Objects.isNull(eleDepositOrder)){
                log.error("battery unfree error error! eleDepositOrder is null, orderId is {}", order.getOrderId());
                return true;
            }
            
            if (Objects.isNull(eleRefundOrder)){
                // 生成退款订单
                eleRefundOrder = EleRefundOrder.builder().orderId(order.getOrderId())
                        .refundOrderNo(OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, order.getUid())).payAmount(eleDepositOrder.getPayAmount())
                        .refundAmount(new BigDecimal(order.getPayTransAmt().toString())).status(EleRefundOrder.STATUS_SUCCESS).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                        .tenantId(eleDepositOrder.getTenantId()).franchiseeId(order.getFranchiseeId()).payType(eleDepositOrder.getPayType()).paymentChannel(eleDepositOrder.getPaymentChannel()).build();
                eleRefundOrderService.insert(eleRefundOrder);
            }
            EleRefundOrder eleRefundOrderUpdate = new EleRefundOrder();
            eleRefundOrderUpdate.setId(eleRefundOrder.getId());
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderUpdate.setStatus(EleRefundOrder.STATUS_SUCCESS);
            eleRefundOrderUpdate.setUpdateTime(System.currentTimeMillis());
            eleRefundOrderService.update(eleRefundOrderUpdate);
            
            // 企业套餐免押只需要修改押金退款订单的状态即可 因为云豆回收后下的逻辑已经提前执行了
            if (Objects.equals(eleDepositOrder.getOrderType(), PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())) {
                return true;
            }
            
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

                InsuranceOrder insuranceOrder = insuranceOrderService.queryByUid(uid, FranchiseeInsurance.INSURANCE_TYPE_BATTERY, InsuranceOrder.NOT_EFFECTIVE);
                if (Objects.nonNull(insuranceOrder)) {
                    insuranceOrderService.updateUseStatusForRefund(insuranceOrder.getOrderId(), InsuranceOrder.INVALID);
                }
            }
            
            userInfoService.unBindUserFranchiseeId(userInfo.getUid());
            
            // 删除用户电池套餐资源包
            userBatteryMemberCardPackageService.deleteByUid(userInfo.getUid());
            
            // 删除用户电池型号
            userBatteryTypeService.deleteByUid(userInfo.getUid());
            
            // 删除用户电池服务费
            serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
            
            // 修改企业用户代付状态为代付过期
//            enterpriseChannelUserService.updatePaymentStatusForRefundDeposit(userInfo.getUid(), EnterprisePaymentStatusEnum.PAYMENT_TYPE_EXPIRED.getCode());
            
            // 解约分期签约，如果有的话
            installmentBizService.terminateForReturnDeposit(userInfo.getUid());
            
            redisService.delete(String.format(UN_FREE_DEPOSIT_USER_INFO_LOCK_KEY, order.getOrderId()));
        }catch (Exception e){
            log.error("battery unfree error!", e);
            return false;
        }
        return true;
    }
    
    @Override
    public boolean authPay(FreeDepositOrder order) {
        return true; //主要逻辑是处理freeDepositOrder表，已在上层处理，无需处理
    }
}
