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
    

}
