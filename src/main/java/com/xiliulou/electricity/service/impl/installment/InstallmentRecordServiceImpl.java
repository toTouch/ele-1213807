package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.InsuranceOrder;
import com.xiliulou.electricity.entity.UnionPayOrder;
import com.xiliulou.electricity.entity.UnionTradeOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignNotifyQuery;
import com.xiliulou.electricity.query.installment.InstallmentSignQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.InsuranceOrderService;
import com.xiliulou.electricity.service.UnionTradeOrderService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import com.xiliulou.pay.deposit.fengyun.pojo.query.FyCommonQuery;
import com.xiliulou.pay.deposit.fengyun.pojo.request.FySignAgreementRequest;
import com.xiliulou.pay.deposit.fengyun.pojo.request.Vars;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FyResult;
import com.xiliulou.pay.deposit.fengyun.pojo.response.FySignAgreementRsp;
import com.xiliulou.pay.deposit.fengyun.service.FyAgreementService;
import com.xiliulou.pay.deposit.fengyun.utils.FyAesUtil;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UN_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.NOTIFY_STATUS_SIGN;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:51
 */
@Service
@Slf4j
@AllArgsConstructor
public class InstallmentRecordServiceImpl implements InstallmentRecordService {
    
    private InstallmentRecordMapper installmentRecordMapper;
    
    private FranchiseeService franchiseeService;
    
    private BatteryMemberCardService batteryMemberCardService;
    
    private CarRentalPackageService carRentalPackageService;
    
    private RedisService redisService;
    
    private UserOauthBindService userOauthBindService;
    
    private UserInfoService userInfoService;
    
    private EnterpriseChannelUserService enterpriseChannelUserService;
    
    private ElectricityCabinetService electricityCabinetService;
    
    private ElectricityPayParamsService electricityPayParamsService;
    
    private EleDepositOrderService eleDepositOrderService;
    
    private InsuranceOrderService insuranceOrderService;
    
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    private UnionTradeOrderService unionTradeOrderService;
    
    private ApplicationContext applicationContext;
    
    private FyAgreementService fyAgreementService;
    
    private FengYunConfig fengYunConfig;
    
    @Override
    public Integer insert(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.insert(installmentRecord);
    }
    
    @Override
    public Integer update(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.update(installmentRecord);
    }
    
    @Slave
    @Override
    public R<List<InstallmentRecordVO>> listForPage(InstallmentRecordQuery installmentRecordQuery) {
        List<InstallmentRecord> installmentRecords = installmentRecordMapper.selectPage(installmentRecordQuery);
        
        List<InstallmentRecordVO> installmentRecordVos = installmentRecords.parallelStream().map(installmentRecord -> {
            InstallmentRecordVO installmentRecordVO = new InstallmentRecordVO();
            BeanUtils.copyProperties(installmentRecord, installmentRecordVO);
            
            // 设置加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(installmentRecord.getFranchiseeId());
            installmentRecordVO.setFranchiseeName(franchisee.getName());
            
            // 设置电或者车的套餐名称
            String packageName;
            if (Objects.equals(installmentRecord.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                packageName = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId()).getName();
            } else {
                packageName = carRentalPackageService.selectById(installmentRecord.getPackageId()).getName();
            }
            installmentRecordVO.setPackageName(packageName);
            
            return installmentRecordVO;
        }).collect(Collectors.toList());
        
        return R.ok(installmentRecordVos);
    }
    
    @Slave
    @Override
    public R<Integer> count(InstallmentRecordQuery installmentRecordQuery) {
        return R.ok(installmentRecordMapper.count(installmentRecordQuery));
    }
    
    @Override
    public R<Object> pay(InstallmentPayQuery query, HttpServletRequest request) {
        Long uid = SecurityUtils.getUid();
        Integer tenantId = TenantContextHolder.getTenantId();
        
        boolean getLockSuccess = redisService.setNx(CacheConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + uid, "1", 3 * 1000L, false);
        if (!getLockSuccess) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
            if (Objects.isNull(userInfo)) {
                log.warn("BATTERY DEPOSIT WARN! not found user,uid={}", uid);
                return R.fail("ELECTRICITY.0019", "未找到用户");
            }
            
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.warn("BATTERY DEPOSIT WARN! user is unUsable,uid={}", uid);
                return R.fail("ELECTRICITY.0024", "用户已被禁用");
            }
            
            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.warn("BATTERY DEPOSIT WARN! user not auth,uid={}", uid);
                return R.fail("ELECTRICITY.0041", "未实名认证");
            }
            
            // 检查是否为自主续费状态
            Boolean userRenewalStatus = enterpriseChannelUserService.checkRenewalStatusByUid(uid);
            if (!userRenewalStatus) {
                log.warn("BATTERY MEMBER ORDER WARN! user renewal status is false, uid={}, mid={}", uid, query.getPackageId());
                return R.fail("000088", "您已是渠道用户，请联系对应站点购买套餐");
            }
            
            ElectricityPayParams electricityPayParams = electricityPayParamsService.queryCacheByTenantIdAndFranchiseeId(tenantId, query.getFranchiseeId());
            if (Objects.isNull(electricityPayParams)) {
                log.warn("BATTERY DEPOSIT WARN!not found pay params,uid={}", uid);
                return R.fail("100307", "未配置支付参数!");
            }
            
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid, tenantId);
            if (Objects.isNull(userOauthBind) || Objects.isNull(userOauthBind.getThirdId())) {
                log.warn("BATTERY DEPOSIT WARN!not found useroauthbind or thirdid is null,uid={}", uid);
                return R.fail("100308", "未找到用户的第三方授权信息!");
            }
            
            // 换电与租车-车电一体两种处理均使用以下三个对象接收对应处理的结果，saveOrderAndPayResult与调起签约接口的逻辑相关
            Triple<Boolean, String, Object> saveOrderAndPayResult = null;
            Triple<Boolean, String, Object> insuranceOrderTriple = null;
            Triple<Boolean, String, Object> eleDepositOrderTriple = null;
            Triple<Boolean, String, InstallmentRecord> installmentRecordTriple = null;
            // 分换电与租车做响应的处理
            if (Objects.equals(query.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                // 购买换电套餐
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(query.getPackageId());
                if (Objects.isNull(batteryMemberCard)) {
                    log.warn("BATTERY DEPOSIT WARN!not found batteryMemberCard,uid={},mid={}", uid, query.getPackageId());
                    return R.fail("ELECTRICITY.00121", "电池套餐不存在");
                }
                
                if (!Objects.equals(BatteryMemberCard.STATUS_UP, batteryMemberCard.getStatus())) {
                    log.warn("BATTERY DEPOSIT WARN! batteryMemberCard is disable,uid={},mid={}", uid, query.getPackageId());
                    return R.fail("100275", "电池套餐不可用");
                }
                
                if (Objects.nonNull(userInfo.getFranchiseeId()) && !Objects.equals(userInfo.getFranchiseeId(), NumberConstant.ZERO_L) && !Objects.equals(userInfo.getFranchiseeId(),
                        batteryMemberCard.getFranchiseeId())) {
                    log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals,uid={},mid={}", uid, query.getPackageId());
                    return R.fail("100349", "用户加盟商与套餐加盟商不一致");
                }
                
                // 校验用户与套餐的分组是否一致
                Triple<Boolean, String, String> checkMemberCardGroup = userInfoService.checkMemberCardGroup(userInfo, batteryMemberCard);
                if (!checkMemberCardGroup.getLeft()) {
                    return R.fail(checkMemberCardGroup.getMiddle(), checkMemberCardGroup.getRight());
                }
                
                // 获取扫码柜机
                ElectricityCabinet electricityCabinet = null;
                if (StringUtils.isNotBlank(query.getProductKey()) && StringUtils.isNotBlank(query.getDeviceName())) {
                    electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
                }
                
                if (Objects.nonNull(electricityCabinet) && !Objects.equals(electricityCabinet.getFranchiseeId(), NumberConstant.ZERO_L) && Objects.nonNull(
                        electricityCabinet.getFranchiseeId()) && !Objects.equals(electricityCabinet.getFranchiseeId(), batteryMemberCard.getFranchiseeId())) {
                    log.warn("BATTERY DEPOSIT WARN! batteryMemberCard franchiseeId not equals electricityCabinet,eid={},mid={}", electricityCabinet.getId(),
                            batteryMemberCard.getId());
                    return R.fail("100375", "柜机加盟商与套餐加盟商不一致,请删除小程序后重新进入");
                }
                
                // 生成押金订单
                if (UserInfo.BATTERY_DEPOSIT_STATUS_NO.equals(query.getBatteryDepositStatus())) {
                    eleDepositOrderTriple = eleDepositOrderService.generateDepositOrder(userInfo, batteryMemberCard, electricityCabinet, electricityPayParams);
                }
                // 生成保险订单
                if (Objects.nonNull(query.getInsuranceId())) {
                    insuranceOrderTriple = insuranceOrderService.generateInsuranceOrder(userInfo, query.getInsuranceId(), electricityCabinet, electricityPayParams);
                }
                // 生成分期签约记录
                installmentRecordTriple = generateInstallmentRecord(query, batteryMemberCard, null, userInfo);
                
                // 保存相关订单并调起支付获取支付结果
                saveOrderAndPayResult = applicationContext.getBean(InstallmentRecordServiceImpl.class)
                        .saveOrderAndPay(eleDepositOrderTriple, insuranceOrderTriple, installmentRecordTriple, batteryMemberCard, userOauthBind, userInfo, request);
                
                
            }// 购买租车、车电一体套餐在此处扩展else代码块
            
            // 根据支付调用结果返回
            if (saveOrderAndPayResult.getLeft()) {
                return R.ok(saveOrderAndPayResult.getRight());
            }
            return R.fail(saveOrderAndPayResult.getRight());
            
        } catch (Exception e) {
            log.error("INSTALLMENT PAY ERROR! uid={}", uid, e);
            return R.fail("301001", "购买失败，请联系管理员");
        }
    }
    
    @Override
    public Triple<Boolean, String, InstallmentRecord> generateInstallmentRecord(InstallmentPayQuery query, BatteryMemberCard batteryMemberCard,
            CarRentalPackagePo carRentalPackagePo, UserInfo userInfo) {
        // 生成分期签约记录订单号
        String externalAgreementNo = OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SIGN, userInfo.getUid());
        InstallmentRecord installmentRecord = InstallmentRecord.builder().uid(userInfo.getUid()).externalAgreementNo(externalAgreementNo).userName(null).mobile(null)
                .packageType(query.getPackageType()).status(InstallmentConstants.INSTALLMENT_RECORD_STATUS_INIT).paidInstallment(0).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        
        if (InstallmentConstants.PACKAGE_TYPE_BATTERY.equals(query.getPackageType())) {
            if (Objects.isNull(batteryMemberCard)) {
                return Triple.of(false, null, null);
            }
            
            Integer installmentNo = batteryMemberCard.getValidDays() / 30;
            installmentRecord.setInstallmentNo(installmentNo);
            installmentRecord.setTenantId(batteryMemberCard.getTenantId());
            installmentRecord.setFranchiseeId(batteryMemberCard.getFranchiseeId());
            installmentRecord.setPackageId(batteryMemberCard.getId());
        }
        return Triple.of(true, null, installmentRecord);
    }
    
    @Override
    public R<Object> sign(InstallmentSignQuery query, HttpServletRequest request) {
        try {
            InstallmentRecord installmentRecord = queryRecordWithStatusForUser(query.getUid(), InstallmentConstants.INSTALLMENT_RECORD_STATUS_INIT);
            if (Objects.isNull(installmentRecord)) {
                return R.fail("301002", "无初始化分期订单");
            }
            
            Vars vars = new Vars();
            vars.setUserName(query.getUserName());
            vars.setMobile(query.getMobile());
            
            FySignAgreementRequest agreementRequest = new FySignAgreementRequest();
            agreementRequest.setChannelFrom("miniapp");
            agreementRequest.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
            agreementRequest.setMerchantName("test");
            agreementRequest.setServiceName("");
            agreementRequest.setServiceDescription("分期签约");
            agreementRequest.setNotifyUrl("/test");
            agreementRequest.setVars(JsonUtil.toJson(vars));
            
            FyCommonQuery<FySignAgreementRequest> commonQuery = new FyCommonQuery<>();
            commonQuery.setChannelCode("test");
            commonQuery.setFlowNo("XLLTEST" + System.currentTimeMillis());
            commonQuery.setFyRequest(agreementRequest);
            FyResult<FySignAgreementRsp> fySignResult = fyAgreementService.signAgreement(commonQuery);
            
            if (InstallmentConstants.FY_SUCCESS_CODE.equals(fySignResult.getCode())) {
                return R.ok(fySignResult.getFyResponse());
            }
            return null;
        } catch (Exception e) {
            log.error("INSTALLMENT SIGN ERROR! uid={}", query.getUid());
        }
        return R.fail("购买失败，请联系管理员");
    }
    
    @Override
    public InstallmentRecord queryRecordWithStatusForUser(Long uid, Integer status) {
        return installmentRecordMapper.selectRecordWithStatusForUser(uid, status);
    }
    
    @Override
    public String signNotify(String bizContent, Long uid) {
        try {
            String decrypt = FyAesUtil.decrypt(bizContent, fengYunConfig.getAesKey());
            InstallmentSignNotifyQuery signNotifyQuery = JsonUtil.fromJson(decrypt, InstallmentSignNotifyQuery.class);
            
            if (NOTIFY_STATUS_SIGN.equals(Integer.valueOf(signNotifyQuery.getStatus()))) {
                InstallmentRecord installmentRecord = applicationContext.getBean(InstallmentRecordService.class)
                        .queryByExternalAgreementNo(signNotifyQuery.getExternalAgreementNo());
                
                if (Objects.isNull(installmentRecord) || !INSTALLMENT_RECORD_STATUS_UN_SIGN.equals(installmentRecord.getStatus())) {
                    log.warn("INSTALLMENT NOTIFY ERROR! sign notify error, no right installmentRecord, uid={}, externalAgreementNo={}", uid,
                            signNotifyQuery.getExternalAgreementNo());
                }
            }
            
            return "";
        } catch (Exception e) {
            log.error("INSTALLMENT NOTIFY ERROR! uid={}, bizContent={}", uid, bizContent, e);
            return null;
        }
    }
    
    @Override
    public InstallmentRecord queryByExternalAgreementNo(String externalAgreementNo) {
        return installmentRecordMapper.selectByExternalAgreementNo(externalAgreementNo);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> saveOrderAndPay(Triple<Boolean, String, Object> eleDepositOrderTriple, Triple<Boolean, String, Object> insuranceOrderTriple,
            Triple<Boolean, String, InstallmentRecord> installmentRecordTriple, BatteryMemberCard batteryMemberCard, UserOauthBind userOauthBind, UserInfo userInfo,
            HttpServletRequest request) throws WechatPayException {
        List<String> orderList = new ArrayList<>();
        List<Integer> orderTypeList = new ArrayList<>();
        List<BigDecimal> payAmountList = new ArrayList<>();
        
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        
        // 保存押金订单
        if (Objects.nonNull(eleDepositOrderTriple) && Boolean.TRUE.equals(eleDepositOrderTriple.getLeft()) && Objects.nonNull(eleDepositOrderTriple.getRight())) {
            EleDepositOrder eleDepositOrder = (EleDepositOrder) eleDepositOrderTriple.getRight();
            eleDepositOrderService.insert(eleDepositOrder);
            
            orderList.add(eleDepositOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_DEPOSIT);
            payAmountList.add(eleDepositOrder.getPayAmount());
            totalAmount = totalAmount.add(eleDepositOrder.getPayAmount());
        }
        
        // 保存保险订单
        if (Objects.nonNull(insuranceOrderTriple) && Boolean.TRUE.equals(insuranceOrderTriple.getLeft()) && Objects.nonNull(insuranceOrderTriple.getRight())) {
            InsuranceOrder insuranceOrder = (InsuranceOrder) insuranceOrderTriple.getRight();
            insuranceOrderService.insert(insuranceOrder);
            
            orderList.add(insuranceOrder.getOrderId());
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSURANCE);
            payAmountList.add(insuranceOrder.getPayAmount());
            totalAmount = totalAmount.add(insuranceOrder.getPayAmount());
        }
        
        // 保存签约记录
        if (Objects.nonNull(installmentRecordTriple) && Boolean.TRUE.equals(installmentRecordTriple.getLeft()) && Objects.nonNull(installmentRecordTriple.getRight())) {
            installmentRecordMapper.insert(installmentRecordTriple.getRight());
        }
        
        // 计算服务费并设置ElectricityTradeOrder的相关数据
        if (batteryMemberCard.getInstallmentServiceFee().compareTo(BigDecimal.valueOf(0.01)) >= 0) {
            orderList.add(OrderIdUtil.generateBusinessOrderId(BusinessType.INSTALLMENT_SERVICE_FEE, userInfo.getUid()));
            orderTypeList.add(UnionPayOrder.ORDER_TYPE_INSTALLMENT_SERVICE_FEE);
            payAmountList.add(batteryMemberCard.getInstallmentServiceFee());
            totalAmount = totalAmount.add(batteryMemberCard.getInstallmentServiceFee());
        }
        
        // 处理0元问题
        // if (totalAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
        //     Triple<Boolean, String, Object> result = handleTotalAmountZero(userInfo, orderList, orderTypeList);
        //     if (Boolean.FALSE.equals(result.getLeft())) {
        //         return result;
        //     }
        //
        //     return Triple.of(true, "", null);
        // }
        
        // 非0元查询详情用于调起支付，查询详情会因为证书问题报错，置于0元处理前会干扰其逻辑
        WechatPayParamsDetails wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(userInfo.getTenantId(),
                batteryMemberCard.getFranchiseeId());
        
        // 调起支付
        UnionPayOrder unionPayOrder = UnionPayOrder.builder().jsonOrderId(JsonUtil.toJson(orderList)).jsonOrderType(JsonUtil.toJson(orderTypeList))
                .jsonSingleFee(JsonUtil.toJson(payAmountList)).payAmount(totalAmount).tenantId(userInfo.getTenantId()).attach(UnionTradeOrder.ATTACH_INSTALLMENT)
                .description("购买分期套餐").uid(userInfo.getUid()).build();
        WechatJsapiOrderResultDTO resultDTO = unionTradeOrderService.unionCreateTradeOrderAndGetPayParams(unionPayOrder, wechatPayParamsDetails, userOauthBind.getThirdId(),
                request);
        return Triple.of(true, null, resultDTO);
    }
}
