package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.FranchiseeInsurance;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.InsuranceUserInfo;
import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import com.xiliulou.electricity.entity.ServiceFeeUserInfo;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.enums.CheckPayParamsResultEnum;
import com.xiliulou.electricity.enums.message.SiteMessageType;
import com.xiliulou.electricity.event.SiteMessageEvent;
import com.xiliulou.electricity.event.publish.SiteMessagePublish;
import com.xiliulou.electricity.mapper.EleBatteryServiceFeeOrderMapper;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.query.EleDepositOrderQuery;
import com.xiliulou.electricity.query.ModelBatteryDeposit;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.BatteryModelService;
import com.xiliulou.electricity.service.EleBatteryServiceFeeOrderService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.EleDisableMemberCardRecordService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.EleUserOperateRecordService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.service.FreeDepositOrderService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.InsuranceUserInfoService;
import com.xiliulou.electricity.service.PxzConfigService;
import com.xiliulou.electricity.service.RentBatteryOrderService;
import com.xiliulou.electricity.service.ServiceFeeUserInfoService;
import com.xiliulou.electricity.service.StoreGoodsService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserBatteryDepositService;
import com.xiliulou.electricity.service.UserBatteryMemberCardPackageService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserBatteryService;
import com.xiliulou.electricity.service.UserBatteryTypeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.installment.InstallmentBizService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.service.userinfo.userInfoGroup.UserInfoGroupDetailService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleDepositOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.electricity.vo.PayDepositOrderVO;
import com.xiliulou.electricity.vo.UserBatteryDepositVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzCommonRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.request.PxzFreeDepositUnfreezeRequest;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzCommonRsp;
import com.xiliulou.pay.deposit.paixiaozu.pojo.rsp.PxzDepositUnfreezeRsp;
import com.xiliulou.pay.deposit.paixiaozu.service.PxzDepositService;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_RECORD_STATUS_INIT;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Service("eleDepositOrderService")
@Slf4j
public class EleDepositOrderServiceImpl implements EleDepositOrderService {
    
    @Resource
    EleDepositOrderMapper eleDepositOrderMapper;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    FranchiseeService franchiseeService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    
    @Autowired
    UserOauthBindService userOauthBindService;
    
    @Autowired
    EleRefundOrderService eleRefundOrderService;
    
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    StoreService storeService;
    
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Resource
    EleBatteryServiceFeeOrderMapper eleBatteryServiceFeeOrderMapper;
    
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    
    @Autowired
    StoreGoodsService storeGoodsService;
    
    @Autowired
    ElectricityCarService electricityCarService;
    
    @Autowired
    EleUserOperateRecordService eleUserOperateRecordService;
    
    @Autowired
    InsuranceUserInfoService insuranceUserInfoService;
    
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Autowired
    EleDisableMemberCardRecordService eleDisableMemberCardRecordService;
    
    @Autowired
    UserBatteryService userBatteryService;
    
    @Autowired
    UserBatteryDepositService userBatteryDepositService;
    
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    BatteryMemberCardService batteryMemberCardService;
    
    @Autowired
    EleBatteryServiceFeeOrderService eleBatteryServiceFeeOrderService;
    
    @Autowired
    FreeDepositAlipayHistoryService freeDepositAlipayHistoryService;
    
    @Autowired
    FreeDepositOrderService freeDepositOrderService;
    
    @Autowired
    PxzDepositService pxzDepositService;
    
    @Autowired
    PxzConfigService pxzConfigService;
    
    @Autowired
    InsuranceOrderService insuranceOrderService;
    
    @Autowired
    UserBatteryMemberCardPackageService userBatteryMemberCardPackageService;
    
    @Autowired
    UserBatteryTypeService userBatteryTypeService;
    
    @Resource
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    @Resource
    UserInfoGroupDetailService userInfoGroupDetailService;
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Resource
    private TenantService tenantService;
    
    @Autowired
    private SiteMessagePublish siteMessagePublish;
    
    @Autowired
    private InstallmentBizService installmentBizService;
    
    @Autowired
    private InstallmentDeductionRecordService installmentDeductionRecordService;
    
    @Override
    public EleDepositOrder queryByOrderId(String orderNo) {
        return eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, orderNo));
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R returnDeposit(HttpServletRequest request) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + user.getUid(), IdUtil.fastSimpleUUID(), 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.000000", "操作频繁,请稍后再试!");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        // 用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.warn("ELE DEPOSIT WARN! user is disable! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        // 设置企业信息
        EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryUserRelatedEnterprise(userInfo.getUid());
        if (Objects.nonNull(enterpriseChannelUserVO) && Objects.equals(enterpriseChannelUserVO.getRenewalStatus(), EnterpriseChannelUser.RENEWAL_CLOSE)) {
            log.warn("ELE DEPOSIT WARN! return Deposit channel user is disable! uid={}", user.getUid());
            return R.fail("120303", "您已是渠道用户，请联系站点开启自主续费后，进行退押操作");
        }
        
        InstallmentDeductionRecordQuery recordQuery = new InstallmentDeductionRecordQuery();
        recordQuery.setUid(userInfo.getUid());
        recordQuery.setStatus(DEDUCTION_RECORD_STATUS_INIT);
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordService.listDeductionRecord(recordQuery);
        if (!CollectionUtils.isEmpty(installmentDeductionRecords)) {
            return R.fail("301015", "当前有正在执行中的分期代扣，请前往分期代扣记录更新状态");
        }
        
        BatteryMemberCard batteryMemberCard = null;
        ServiceFeeUserInfo serviceFeeUserInfo = null;
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
        
        // 是否存在换电次数欠费情况
        Integer packageOwe = null;
        // 套餐欠费次数
        Integer memberCardOweNumber = null;
        if (Objects.nonNull(userBatteryMemberCard) && !Objects.equals(userBatteryMemberCard.getMemberCardId(), NumberConstant.ZERO_L)) {
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE_REVIEW)) {
                log.warn("ELE DEPOSIT WARN! disable member card is reviewing userId={}", user.getUid());
                return R.fail("ELECTRICITY.100003", "停卡正在审核中");
            }
            
            if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
                log.warn("ELE DEPOSIT WARN! member card is disable userId={}", user.getUid());
                return R.fail("ELECTRICITY.100004", "月卡已暂停");
            }
            
            batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("ELE DEPOSIT WARN! batteryMemberCard not found! uid={}", userInfo.getUid());
                return R.fail("ELECTRICITY.00121", "套餐不存在");
            }
            
            serviceFeeUserInfo = serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid());
            if (Objects.isNull(serviceFeeUserInfo)) {
                log.error("ELE DEPOSIT WARN!not found serviceFeeUserInfo,uid={}", userInfo.getUid());
                return R.fail("100247", "未找到用户信息");
            }
            
            if (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && Objects.nonNull(userBatteryMemberCard.getRemainingNumber())
                    && userBatteryMemberCard.getRemainingNumber() < 0) {
                log.error("ELE DEPOSIT WARN!user battery membercard disable,uid={}", userInfo.getUid());
                return R.fail("", "用户套餐次数欠费");
            }
        }
        
        // 判断是否缴纳押金
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit) || !Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("ELE DEPOSIT WARN! not pay deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }
        
        if (Objects.equals(userBatteryDeposit.getOrderId(), "-1")) {
            return R.fail("ELECTRICITY.00115", "请线下退押");
        }
        
        // 是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0013", "存在未完成租电订单，不能下单");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "ELECTRICITY.0095", "存在未完成还电订单，不能下单");
            }
        }
        
        // 是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = electricityCabinetOrderService.queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "ELECTRICITY.0094", "存在未完成换电订单，不能下单");
        }
        
        // 查找缴纳押金订单
        EleDepositOrder eleDepositOrder = eleDepositOrderMapper.selectOne(
                new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId, userBatteryDeposit.getOrderId()));
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("ELE DEPOSIT WARN! not found eleDepositOrder! userId={}", user.getUid());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        Triple<Boolean, Integer, BigDecimal> checkUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard,
                batteryMemberCard, serviceFeeUserInfo);
        if (Boolean.TRUE.equals(checkUserBatteryServiceFeeResult.getLeft())) {
            log.warn("BATTERY MEMBERCARD REFUND WARN! user exit battery service fee,uid={}", user.getUid());
            return R.fail("100220", "用户存在电池服务费", checkUserBatteryServiceFeeResult.getRight());
        }
        
        // 判断是否退电池
        if (Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("ELE DEPOSIT WARN! not return battery,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0046", "未退还电池");
        }
        
        // 判断是否线上押金
        if (Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.OFFLINE_PAYMENT) || Objects.equals(eleDepositOrder.getPayType(),
                EleDepositOrder.MEITUAN_DEPOSIT_PAYMENT)) {
            log.warn("ELE DEPOSIT WARN! travel to store,uid={}", user.getUid());
            return R.fail("ELECTRICITY.00115", "请前往门店退押金");
        }
        
        BigDecimal payAmount = eleDepositOrder.getPayAmount();
        
        // 是否有正在进行中的退款
        Integer refundCount = eleRefundOrderService.queryCountByOrderId(eleDepositOrder.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
        if (refundCount > 0) {
            log.warn("ELE DEPOSIT WARN! have refunding order,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0047", "请勿重复退款");
        }
        
        
        FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleDepositOrder.getOrderId());
        
        BigDecimal refundAmount = getRefundAmountV2(eleDepositOrder,freeDepositOrder);
        
        BigDecimal eleRefundAmount = refundAmount.doubleValue() < 0 ? BigDecimal.valueOf(0) : refundAmount;
        
        
        UserInfo updateUserInfo = new UserInfo();
        boolean eleRefund = false;
        boolean carRefund = false;
        Integer tenantId = user.getTenantId();
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        // 生成退款订单
        String generateBusinessOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT_REFUND, user.getUid());
        EleRefundOrder eleRefundOrder = EleRefundOrder.builder().orderId(eleDepositOrder.getOrderId()).refundOrderNo(generateBusinessOrderId).payAmount(payAmount)
                .refundAmount(eleRefundAmount).status(EleRefundOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(eleDepositOrder.getTenantId()).memberCardOweNumber(memberCardOweNumber).payType(eleDepositOrder.getPayType()).build();
        
        // 发送站内信
        siteMessagePublish.publish(
                SiteMessageEvent.builder(this).tenantId(TenantContextHolder.getTenantId().longValue()).code(SiteMessageType.EXCHANGE_BATTERY_AND_RETURN_THE_DEPOSIT)
                        .notifyTime(System.currentTimeMillis()).addContext("name", userInfo.getName()).addContext("phone", userInfo.getPhone())
                        .addContext("refundOrderNo", generateBusinessOrderId).addContext("amount", eleRefundAmount.toString()).build());
        
        // 退款零元
        if (eleRefundAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            
            if (Objects.isNull(electricityConfig) || Objects.equals(ElectricityConfig.DISABLE_ZERO_DEPOSIT_AUDIT, electricityConfig.getIsZeroDepositAuditEnabled())) {
                eleRefund = true;
                eleRefundOrder.setStatus(EleRefundOrder.STATUS_REFUND);
                
                eleRefundOrder.setStatus(EleRefundOrder.STATUS_SUCCESS);
                
                updateUserInfo.setUid(userInfo.getUid());
                updateUserInfo.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_NO);
                updateUserInfo.setUpdateTime(System.currentTimeMillis());
                
                userInfoService.updateByUid(updateUserInfo);
                
                // 更新用户套餐订单为已失效
                electricityMemberCardOrderService.batchUpdateStatusByOrderNo(userBatteryMemberCardService.selectUserBatteryMemberCardOrder(userInfo.getUid()),
                        ElectricityMemberCardOrder.USE_STATUS_EXPIRE);
                
                userBatteryMemberCardService.unbindMembercardInfoByUid(userInfo.getUid());
                
                userBatteryDepositService.logicDeleteByUid(userInfo.getUid());
                
                userBatteryService.deleteByUid(userInfo.getUid());
                
                // 退押金解绑用户所属加盟商
                userInfoService.unBindUserFranchiseeId(userInfo.getUid());
                
                InsuranceUserInfo insuranceUserInfo = insuranceUserInfoService.selectByUidAndTypeFromCache(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY);
                if (Objects.nonNull(insuranceUserInfo)) {
                    insuranceUserInfoService.deleteById(insuranceUserInfo);
                    // 更新用户保险订单为已失效
                    insuranceOrderService.updateUseStatusForRefund(insuranceUserInfo.getInsuranceOrderId(), InsuranceOrder.INVALID);
                }
                
                // 删除用户电池套餐资源包
                userBatteryMemberCardPackageService.deleteByUid(userInfo.getUid());
                
                // 删除用户电池型号
                userBatteryTypeService.deleteByUid(userInfo.getUid());
                
                // 删除用户电池服务费
                serviceFeeUserInfoService.deleteByUid(userInfo.getUid());
                
                eleRefundOrderService.insert(eleRefundOrder);
                
                // 解约分期签约，如果有的话
                installmentBizService.terminateForReturnDeposit(userInfo.getUid());
                
                return R.ok("SUCCESS");
            }
        }
        
        eleRefundOrderService.insert(eleRefundOrder);
        
        // if (Objects.nonNull(freeDepositOrder) && ((Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_CAR_BATTERY) && carRefund && eleRefund) || (
        //        Objects.equals(freeDepositOrder.getDepositType(), FreeDepositOrder.DEPOSIT_TYPE_BATTERY) && eleRefund))) {
        //     freeDepositOrderThaw(userBatteryDeposit, freeDepositOrder);
        // }
        
        // 等到后台同意退款
        return R.ok(packageOwe);
    }
    
    private void freeDepositOrderThaw(UserBatteryDeposit userBatteryDeposit, FreeDepositOrder freeDepositOrder) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(freeDepositOrder.getTenantId());
        if (Objects.isNull(pxzConfig)) {
            log.error("CAR REFUND DEPOSIT ERROR! pxzConfig is null, tenantid={}", freeDepositOrder.getTenantId());
            return;
        }
        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(userBatteryDeposit.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark("车辆免押解冻");
        queryRequest.setTransId(userBatteryDeposit.getOrderId());
        query.setData(queryRequest);
        
        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzDepositUnfreezeRspPxzCommonRsp = null;
        try {
            pxzDepositUnfreezeRspPxzCommonRsp = pxzDepositService.unfreezeDeposit(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! unFreeDepositOrderQuery fail! uid={},orderId={}", userBatteryDeposit.getUid(), userBatteryDeposit.getOrderId(), e);
        }
        
        if (Objects.isNull(pxzDepositUnfreezeRspPxzCommonRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", userBatteryDeposit.getUid(), userBatteryDeposit.getOrderId());
            return;
        }
        
        if (!pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
            return;
        }
        
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
    }
    
    private BigDecimal getRefundAmount(EleDepositOrder eleDepositOrder) {
        if (!Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            return eleDepositOrder.getPayAmount();
        }
        
        BigDecimal refundAmount = eleDepositOrder.getPayAmount();
        FreeDepositAlipayHistory freeDepositAlipayHistory = freeDepositAlipayHistoryService.queryByOrderId(eleDepositOrder.getOrderId());
        if (Objects.nonNull(freeDepositAlipayHistory)) {
            refundAmount = eleDepositOrder.getPayAmount().subtract(freeDepositAlipayHistory.getAlipayAmount());
        }
        
        return refundAmount;
    }
    
    private BigDecimal getRefundAmountV2(EleDepositOrder eleDepositOrder, FreeDepositOrder freeDepositOrder) {
        if (!Objects.equals(eleDepositOrder.getPayType(), EleDepositOrder.FREE_DEPOSIT_PAYMENT)) {
            return eleDepositOrder.getPayAmount();
        }
        
        BigDecimal refundAmount = eleDepositOrder.getPayAmount();
        
        if (Objects.nonNull(freeDepositOrder)) {
            return BigDecimal.valueOf(freeDepositOrder.getPayTransAmt());
        }
        
        return refundAmount;
    }
    
    @Slave
    @Override
    public R queryList(EleDepositOrderQuery eleDepositOrderQuery) {
        List<EleDepositOrderVO> eleDepositOrderVOS = eleDepositOrderMapper.queryList(eleDepositOrderQuery);
        
        eleDepositOrderVOS.forEach(eleDepositOrderVO -> {
            eleDepositOrderVO.setRefundFlag(true);
            // orderId
            FreeDepositOrder freeDepositOrder = freeDepositOrderService.selectByOrderId(eleDepositOrderVO.getOrderId());
            if (Objects.nonNull(freeDepositOrder)){
                eleDepositOrderVO.setPayTransAmt(freeDepositOrder.getPayTransAmt());
            }
            
            List<EleRefundOrder> eleRefundOrders = eleRefundOrderService.selectByOrderIdNoFilerStatus(eleDepositOrderVO.getOrderId());
            // 订单已退押或正在退押中
            if (!CollectionUtils.isEmpty(eleRefundOrders)) {
                for (EleRefundOrder e : eleRefundOrders) {
                    if (EleRefundOrder.STATUS_SUCCESS.equals(e.getStatus()) || EleRefundOrder.STATUS_REFUND.equals(e.getStatus())) {
                        eleDepositOrderVO.setRefundFlag(false);
                    }
                }
            }
        });
        
        return R.ok(eleDepositOrderVOS);
    }
    
    @Override
    public R queryListToUser(EleDepositOrderQuery eleDepositOrderQuery) {
        return R.ok(eleDepositOrderMapper.queryListForUser(eleDepositOrderQuery));
    }
    
    @Override
    public R payDepositOrderList(EleDepositOrderQuery eleDepositOrderQuery) {
        List<PayDepositOrderVO> payDepositOrderVOList = eleDepositOrderMapper.payDepositOrderList(eleDepositOrderQuery);
        if (CollectionUtils.isEmpty(payDepositOrderVOList)) {
            return R.ok();
        }
        
        for (PayDepositOrderVO payDepositOrderVO : payDepositOrderVOList) {
            Long refundTime = eleRefundOrderService.queryRefundTime(payDepositOrderVO.getOrderId(), EleRefundOrder.BATTERY_DEPOSIT_REFUND_ORDER);
            payDepositOrderVO.setRefundTime(refundTime);
            
            payDepositOrderVO.setModel(batteryModelService.acquireBatteryModel(payDepositOrderVO.getBatteryType(), TenantContextHolder.getTenantId()));
        }
        
        return R.ok(payDepositOrderVOList);
    }
    
    @Override
    public void update(EleDepositOrder eleDepositOrderUpdate) {
        eleDepositOrderMapper.updateById(eleDepositOrderUpdate);
    }
    
    @Override
    public R queryUserDeposit() {
        // 优化 TODO
        Map<String, String> map = new HashMap<>();
        
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        if (Objects.isNull(userInfo)) {
            log.error("ELE DEPOSIT ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // 如果为企业用户，返回1给前端，代表当前用户为企业用户
        /*EnterpriseChannelUserVO enterpriseChannelUserVO = enterpriseChannelUserService.queryEnterpriseChannelUser(uid);
        if(Objects.nonNull(enterpriseChannelUserVO)){
            map.put("isEnterpriseUser", NumberConstant.ONE.toString());
            return R.ok(map);
        }*/
        
        // 是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.info("ELE DEPOSIT INFO! user not rent deposit,uid={}", user.getUid());
            return R.ok(null);
        }
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.error("ELE DEPOSIT ERROR! not found userBatteryDeposit,uid={}", user.getUid());
            return R.fail("100247", "用户信息不存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        // 兼容2.0版本小程序
        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(userInfo.getUid());
        if (Objects.nonNull(electricityBattery) && Objects.nonNull(franchisee) && Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            String batteryModel = batteryModelService.analysisBatteryTypeByBatteryName(electricityBattery.getSn());
            Integer acquireBattery = null;
            if (StringUtils.isNotBlank(batteryModel)) {
                acquireBattery = batteryModelService.acquireBatteryModel(batteryModel, TenantContextHolder.getTenantId());
            }
            map.put("batteryType", Objects.isNull(acquireBattery) ? null : String.valueOf(acquireBattery));
        } else {
            map.put("batteryType", null);
        }
        
        if ((Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES) && Objects.nonNull(userBatteryDeposit.getBatteryDeposit()) && Objects.nonNull(
                userBatteryDeposit.getOrderId()))) {
            
            if (Objects.equals(userBatteryDeposit.getOrderId(), "-1")) {
                map.put("refundStatus", null);
                map.put("deposit", userBatteryDeposit.getBatteryDeposit().toString());
                map.put("time", String.valueOf(System.currentTimeMillis()));
                map.put("franchiseeName", Objects.nonNull(franchisee) ? franchisee.getName() : "");
                map.put("franchiseeId", String.valueOf(Objects.nonNull(franchisee) ? franchisee.getId() : ""));
            } else {
                // 是否退款
                Integer refundStatus = eleRefundOrderService.queryStatusByOrderId(userBatteryDeposit.getOrderId());
                if (Objects.nonNull(refundStatus)) {
                    map.put("refundStatus", refundStatus.toString());
                } else {
                    map.put("refundStatus", null);
                }
                
                EleDepositOrder eleDepositOrder = queryByOrderId(userBatteryDeposit.getOrderId());
                if (Objects.isNull(eleDepositOrder)) {
                    map.put("store", null);
                } else {
                    map.put("time", eleDepositOrder.getCreateTime().toString());
                    Store store = storeService.queryByIdFromCache(eleDepositOrder.getStoreId());
                    if (Objects.nonNull(store)) {
                        map.put("store", store.getName());
                    } else {
                        map.put("store", null);
                    }
                }
                
                // 是否退押
                EleRefundOrder eleRefundOrder = eleRefundOrderService.selectLatestRefundDepositOrder(userBatteryDeposit.getOrderId());
                if (Objects.nonNull(eleRefundOrder) && EleRefundOrder.STATUS_REFUSE_REFUND.equals(eleRefundOrder.getStatus())) {
                    map.put("rejectReason", eleRefundOrder.getErrMsg());
                }
                
                map.put("deposit", userBatteryDeposit.getBatteryDeposit().toString());
                
                map.put("franchiseeName", franchisee.getName());
                map.put("franchiseeId", String.valueOf(franchisee.getId()));
                map.put("rentBatteryStatus", userInfo.getBatteryRentStatus().toString());
                map.put("depositType", Objects.isNull(userBatteryDeposit.getDepositType()) ? null : String.valueOf(userBatteryDeposit.getDepositType()));
            }
            
            // 判断押金订单对应的套餐类型
            EleDepositOrder eleDepositOrder = this.queryByOrderId(userBatteryDeposit.getOrderId());
            if (Objects.nonNull(eleDepositOrder)) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(eleDepositOrder.getMid());
                if (Objects.nonNull(batteryMemberCard)) {
                    map.put("currentPackageType", batteryMemberCard.getBusinessType().toString());
                }
            }
            
            return R.ok(map);
        }
        return R.ok(null);
    }
    
    @Override
    public R selectUserBatteryDeposit() {
        UserBatteryDepositVO userBatteryDepositVO = new UserBatteryDepositVO();
        userBatteryDepositVO.setBatteryRentStatus(UserInfo.BATTERY_RENT_STATUS_NO);
        userBatteryDepositVO.setBatteryDepositStatus(UserInfo.BATTERY_DEPOSIT_STATUS_YES);
        userBatteryDepositVO.setBatteryDeposit(BigDecimal.ZERO);
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.warn("ELE DEPOSIT WARN! not found userInfo,uid={}", SecurityUtils.getUid());
            return R.ok(userBatteryDepositVO);
        }
        
        userBatteryDepositVO.setBatteryRentStatus(userInfo.getBatteryRentStatus());
        userBatteryDepositVO.setBatteryDepositStatus(userInfo.getBatteryDepositStatus());
        
        UserBatteryDeposit userBatteryDeposit = userBatteryDepositService.selectByUidFromCache(userInfo.getUid());
        if (Objects.isNull(userBatteryDeposit)) {
            log.warn("ELE DEPOSIT WARN! not found userBatteryDeposit,uid={}", userInfo.getUid());
            return R.ok(userBatteryDepositVO);
        }
        
        userBatteryDepositVO.setBatteryDeposit(userBatteryDeposit.getBatteryDeposit());
        userBatteryDepositVO.setDepositType(userBatteryDeposit.getDepositType());
        
        return R.ok(userBatteryDepositVO);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryDepositAndInsuranceDetail(String orderId) {
        EleDepositOrder eleDepositOrder = this.queryByOrderId(orderId);
        if (Objects.isNull(eleDepositOrder) || !Objects.equals(eleDepositOrder.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "", "押金订单不存在");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(eleDepositOrder.getUid());
        if (Objects.isNull(userInfo)) {
            return Triple.of(false, "ELECTRICITY.0019", "未找到用户");
        }
        
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }
        
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        
        if (!CollectionUtils.isEmpty(eleRefundOrderService.selectByOrderId(orderId))) {
            return Triple.of(false, "ELECTRICITY.0042", "订单已退押金");
        }
        
        return Triple.of(true, null, insuranceUserInfoService.selectUserInsuranceDetailByUidAndType(userInfo.getUid(), FranchiseeInsurance.INSURANCE_TYPE_BATTERY));
    }
    
    @Override
    public EleDepositOrderVO queryByUidAndSourceOrderNo(Long uid, String sourceOrderNo) {
        
        EleDepositOrder eleDepositOrder = eleDepositOrderMapper.queryByUidAndSourceOrderNo(uid, sourceOrderNo);
        EleDepositOrderVO eleDepositOrderVO = new EleDepositOrderVO();
        if (Objects.nonNull(eleDepositOrder)) {
            BeanUtils.copyProperties(eleDepositOrder, eleDepositOrderVO);
        }
        
        return eleDepositOrderVO;
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
        return eleDepositOrderMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
    
    @Override
    public R checkPayParamsDetails(String orderId) {
        EleDepositOrder eleDepositOrder = this.queryByOrderId(orderId);
        if (Objects.isNull(eleDepositOrder)) {
            log.warn("CHECK PAY PARAMS DETAILS WARN! NOT FOUND ELECTRICITY_REFUND_ORDER orderId={}", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }
        
        ElectricityPayParams electricityPayParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(eleDepositOrder.getTenantId(),
                eleDepositOrder.getParamFranchiseeId());
        if (Objects.isNull(electricityPayParams) || !Objects.equals(eleDepositOrder.getParamFranchiseeId(), electricityPayParams.getFranchiseeId()) || !Objects.equals(
                eleDepositOrder.getWechatMerchantId(), electricityPayParams.getWechatMerchantId())) {
            return R.ok(CheckPayParamsResultEnum.FAIL.getCode());
        }
        return R.ok(CheckPayParamsResultEnum.SUCCESS.getCode());
    }
    
    @Override
    @Slave
    public R listSuperAdminPage(EleDepositOrderQuery eleDepositOrderQuery) {
        List<EleDepositOrderVO> eleDepositOrderVOS = eleDepositOrderMapper.selectListSuperAdminPage(eleDepositOrderQuery);
        
        eleDepositOrderVOS.stream().map(eleDepositOrderVO -> {
            eleDepositOrderVO.setRefundFlag(true);
            
            List<EleRefundOrder> eleRefundOrders = eleRefundOrderService.selectByOrderIdNoFilerStatus(eleDepositOrderVO.getOrderId());
            // 订单已退押或正在退押中
            if (!CollectionUtils.isEmpty(eleRefundOrders)) {
                for (EleRefundOrder e : eleRefundOrders) {
                    if (EleRefundOrder.STATUS_SUCCESS.equals(e.getStatus()) || EleRefundOrder.STATUS_REFUND.equals(e.getStatus())) {
                        eleDepositOrderVO.setRefundFlag(false);
                    }
                }
            }
            
            if (Objects.nonNull(eleDepositOrderVO.getTenantId())) {
                Tenant tenant = tenantService.queryByIdFromCache(eleDepositOrderVO.getTenantId());
                eleDepositOrderVO.setTenantName(Objects.nonNull(tenant) ? tenant.getName() : null);
            }
            
            return eleDepositOrderVO;
        }).collect(Collectors.toList());
        
        return R.ok(eleDepositOrderVOS);
    }
    
    @Override
    public Integer deleteById(Long id) {
        return eleDepositOrderMapper.deleteById(id);
    }
    
    @Override
    @Slave
    public EleDepositOrder queryLastEnterpriseDeposit(Long uid) {
        return eleDepositOrderMapper.selectLastEnterpriseDeposit(uid);
    }
    
    @Override
    public Triple<Boolean, String, Object> generateDepositOrder(UserInfo userInfo, BatteryMemberCard batteryMemberCard, ElectricityCabinet electricityCabinet,
            ElectricityPayParams electricityPayParams) {
        // 生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        EleDepositOrder eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName())
                .payAmount(batteryMemberCard.getDeposit()).status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .tenantId(userInfo.getTenantId()).franchiseeId(batteryMemberCard.getFranchiseeId()).payType(EleDepositOrder.ONLINE_PAYMENT)
                .storeId(Objects.nonNull(electricityCabinet) ? electricityCabinet.getStoreId() : userInfo.getStoreId()).mid(batteryMemberCard.getId()).modelType(0)
                .paramFranchiseeId(electricityPayParams.getFranchiseeId()).wechatMerchantId(electricityPayParams.getWechatMerchantId()).build();
        
        return Triple.of(true, null, eleDepositOrder);
    }
    
    @Override
    public R queryFranchiseeDeposit(String productKey, String deviceName, Long franchiseeId) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE DEPOSIT ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        // TODO: 2022/12/21 没有加盟商id什么情景下
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("ELE DEPOSIT ERROR! not found franchisee,franchiseeId={}", franchiseeId);
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        Map<String, Object> map = new HashMap();
        
        // 根据类型分押金
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            // 型号押金
            // TODO: 2022/12/21 bug
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            map.put("modelType", Franchisee.NEW_MODEL_TYPE);
            map.put("batteryDeposit", modelBatteryDepositList);
            return R.ok(map);
        }
        
        map.put("modelType", Franchisee.OLD_MODEL_TYPE);
        map.put("batteryDeposit", franchisee.getBatteryDeposit());
        return R.ok(map);
    }
    
    @Override
    public R queryDeposit(String productKey, String deviceName, Long franchiseeId) {
        
        if (Objects.isNull(franchiseeId)) {
            // 换电柜
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
            if (Objects.isNull(electricityCabinet)) {
                log.error("queryDeposit  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey, deviceName);
                return R.fail("ELECTRICITY.0005", "未找到换电柜");
            }
            
            // 查询押金
            // 查找换电柜门店
            if (Objects.isNull(electricityCabinet.getStoreId())) {
                log.error("queryDeposit  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
                return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
            }
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("queryDeposit  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            
            // 查找门店加盟商
            if (Objects.isNull(store.getFranchiseeId())) {
                log.error("queryDeposit  ERROR! not found Franchisee ！storeId{}", store.getId());
                return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
            }
            
            franchiseeId = store.getFranchiseeId();
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromDB(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("queryDeposit  ERROR! not found Franchisee ！franchiseeId{}", franchiseeId);
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        // 根据类型分押金
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            // 型号押金
            List modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
            return R.ok(modelBatteryDepositList);
        }
        
        return R.ok(franchisee.getBatteryDeposit());
    }
    
    @Slave
    @Override
    public R queryCount(EleDepositOrderQuery eleDepositOrderQuery) {
        return R.ok(eleDepositOrderMapper.queryCount(eleDepositOrderQuery));
    }
    
    @Override
    public void insert(EleDepositOrder eleDepositOrder) {
        eleDepositOrderMapper.insert(eleDepositOrder);
    }
    
    @Override
    public R queryModelType(String productKey, String deviceName) {
        // 换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE DEPOSIT ERROR! not found electricityCabinet,productKey={},deviceName={}", productKey, deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }
        
        // 查询押金
        // 查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("ELE DEPOSIT ERROR! not found store,electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("ELE DEPOSIT ERROR! not found store,storeId={}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }
        
        // 查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.error("ELE DEPOSIT ERROR! not found franchiseeId,storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(store.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE DEPOSIT ERROR! not found franchisee,franchiseeId={}", store.getFranchiseeId());
            return R.fail("ELECTRICITY.0038", "未找到加盟商");
        }
        
        return R.ok(franchisee.getModelType());
    }
    
    @Override
    public EleDepositOrder queryLastPayDepositTimeByUid(Long uid, Long franchiseeId, Integer tenantId, Integer depositType) {
        return eleDepositOrderMapper.queryLastPayDepositTimeByUid(uid, franchiseeId, tenantId, depositType);
    }
    
    @Override
    public EleDepositOrder selectLatestByUid(Long uid) {
        return eleDepositOrderMapper.selectLatestByUid(uid);
    }
    
    @Slave
    @Override
    public BigDecimal queryTurnOver(Integer tenantId) {
        return Optional.ofNullable(eleDepositOrderMapper.queryTurnOver(tenantId)).orElse(new BigDecimal("0"));
    }
    
    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid + RandomUtil.randomNumbers(6);
    }
    
    @Slave
    @Override
    public BigDecimal queryDepositTurnOverByDepositType(Integer tenantId, Long todayStartTime, Integer depositType, List<Long> franchiseeIds, Integer payType) {
        return Optional.ofNullable(eleDepositOrderMapper.queryDepositTurnOverByDepositType(tenantId, todayStartTime, depositType, franchiseeIds, payType))
                .orElse(BigDecimal.valueOf(0));
    }
    
    @Slave
    @Override
    public List<HomePageTurnOverGroupByWeekDayVo> queryDepositTurnOverAnalysisByDepositType(Integer tenantId, Integer depositType, List<Long> franchiseeId, Long beginTime,
            Long enTime) {
        return eleDepositOrderMapper.queryDepositTurnOverAnalysisByDepositType(tenantId, depositType, franchiseeId, beginTime, enTime);
    }
    
    @Slave
    @Override
    public BigDecimal querySumDepositTurnOverAnalysis(Integer tenantId, List<Long> franchiseeId, Long beginTime, Long enTime) {
        return eleDepositOrderMapper.querySumDepositTurnOverAnalysis(tenantId, franchiseeId, beginTime, enTime);
    }
    
    @Override
    public Triple<Boolean, String, Object> handleRentBatteryDeposit(Long franchiseeId, Integer memberCard, Integer model, UserInfo userInfo) {
        EleDepositOrder eleDepositOrder = null;
        if (Objects.isNull(memberCard)) {
            return Triple.of(true, "", null);
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee)) {
            log.error("BATTERY DEPOSIT ERROR! not found Franchisee ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.0038", "未找到加盟商");
        }
        
        BigDecimal depositPayAmount = null;
        
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            depositPayAmount = franchisee.getBatteryDeposit();
        }
        
        // 型号押金计算
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(model)) {
                return Triple.of(false, "ELECTRICITY.0007", "不合法的参数");
            }
            
            // TODO: 2022/12/21 jsonArray  对象
            // 型号押金
            List<ModelBatteryDeposit> modelBatteryDepositList = JsonUtil.fromJsonArray(franchisee.getModelBatteryDeposit(), ModelBatteryDeposit.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("BATTERY DEPOSIT ERROR! not found modelBatteryDepositList ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
            }
            
            // TODO: 2022/12/21 理解一下
            for (ModelBatteryDeposit modelBatteryDeposit : modelBatteryDepositList) {
                if ((double) (modelBatteryDeposit.getModel()) - model < 1 && (double) (modelBatteryDeposit.getModel()) - model >= 0) {
                    depositPayAmount = modelBatteryDeposit.getBatteryDeposit();
                    break;
                }
            }
        }
        
        if (Objects.isNull(depositPayAmount)) {
            log.error("BATTERY DEPOSIT ERROR! payAmount is null ！franchiseeId={},uid={}", franchiseeId, userInfo.getUid());
            return Triple.of(false, "ELECTRICITY.00110", "未找到押金");
        }
        
        // 生成押金独立订单
        String depositOrderId = OrderIdUtil.generateBusinessOrderId(BusinessType.BATTERY_DEPOSIT, userInfo.getUid());
        eleDepositOrder = EleDepositOrder.builder().orderId(depositOrderId).uid(userInfo.getUid()).phone(userInfo.getPhone()).name(userInfo.getName()).payAmount(depositPayAmount)
                .status(EleDepositOrder.STATUS_INIT).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis()).tenantId(userInfo.getTenantId())
                .franchiseeId(franchisee.getId()).payType(EleDepositOrder.ONLINE_PAYMENT).batteryType(
                        Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) ? batteryModelService.acquireBatteryShort(model, TenantContextHolder.getTenantId())
                                : null).storeId(null).modelType(franchisee.getModelType()).build();
        
        return Triple.of(true, "", eleDepositOrder);
    }
    
    private void freeDepositOrderThaw(UserCarDeposit userCarDeposit, FreeDepositOrder freeDepositOrder) {
        PxzConfig pxzConfig = pxzConfigService.queryByTenantIdFromCache(freeDepositOrder.getTenantId());
        if (Objects.isNull(pxzConfig)) {
            log.error("CAR REFUND DEPOSIT ERROR! pxzConfig is null, tenantid={}", userCarDeposit.getTenantId());
            return;
        }
        PxzCommonRequest<PxzFreeDepositUnfreezeRequest> query = new PxzCommonRequest<>();
        query.setAesSecret(pxzConfig.getAesKey());
        query.setDateTime(System.currentTimeMillis());
        query.setSessionId(userCarDeposit.getOrderId());
        query.setMerchantCode(pxzConfig.getMerchantCode());
        
        PxzFreeDepositUnfreezeRequest queryRequest = new PxzFreeDepositUnfreezeRequest();
        queryRequest.setRemark("车辆免押解冻");
        queryRequest.setTransId(userCarDeposit.getOrderId());
        query.setData(queryRequest);
        
        PxzCommonRsp<PxzDepositUnfreezeRsp> pxzDepositUnfreezeRspPxzCommonRsp = null;
        try {
            pxzDepositUnfreezeRspPxzCommonRsp = pxzDepositService.unfreezeDeposit(query);
        } catch (Exception e) {
            log.error("Pxz ERROR! unFreeDepositOrderQuery fail! uid={},orderId={}", userCarDeposit.getUid(), userCarDeposit.getOrderId(), e);
        }
        
        if (Objects.isNull(pxzDepositUnfreezeRspPxzCommonRsp)) {
            log.error("Pxz ERROR! freeDepositOrderQuery fail! pxzQueryOrderRsp is null! uid={},orderId={}", userCarDeposit.getUid(), userCarDeposit.getOrderId());
            return;
        }
        
        if (!pxzDepositUnfreezeRspPxzCommonRsp.isSuccess()) {
            return;
        }
        
        FreeDepositOrder freeDepositOrderUpdate = new FreeDepositOrder();
        freeDepositOrderUpdate.setId(freeDepositOrder.getId());
        freeDepositOrderUpdate.setAuthStatus(FreeDepositOrder.AUTH_UN_FREEZING);
        freeDepositOrderUpdate.setUpdateTime(System.currentTimeMillis());
        freeDepositOrderService.update(freeDepositOrderUpdate);
    }
    
    /**
     * 根据型号计算押金
     *
     * @param franchisee
     * @param model
     * @param userInfo
     * @return
     */
    private Pair<Boolean, Object> caculDepositByFranchisee(Franchisee franchisee, Integer model, UserInfo userInfo) {
        // 单型号
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            return Pair.of(Boolean.TRUE, franchisee.getBatteryDeposit());
        }
        
        // 多型号押金
        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
            if (Objects.isNull(model)) {
                log.error("ELE DEPOSIT ERROR! illegal model,uid={},model={}", userInfo.getUid(), model);
                return Pair.of(Boolean.FALSE, null);
            }
            
            // 型号押金
            List<Map> modelBatteryDepositList = JsonUtil.fromJson(franchisee.getModelBatteryDeposit(), List.class);
            if (ObjectUtil.isEmpty(modelBatteryDepositList)) {
                log.error("ELE DEPOSIT ERROR! not found modelBatteryDepositList,uid={}", userInfo.getUid());
                return Pair.of(Boolean.FALSE, null);
            }
            
            // 没看懂
            for (Map map : modelBatteryDepositList) {
                if ((double) (map.get("model")) - model < 1 && (double) (map.get("model")) - model >= 0) {
                    //                    payAmount = BigDecimal.valueOf((double) map.get("batteryDeposit"));
                    //                    break;
                    return Pair.of(Boolean.TRUE, BigDecimal.valueOf((double) map.get("batteryDeposit")));
                }
            }
        }
        
        return Pair.of(Boolean.FALSE, null);
    }
}
