package com.xiliulou.electricity.service.impl.installment;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.FyConfig;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentDeductionPlanMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FyConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.InstallmentUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PACKAGE_TYPE_BATTERY;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:53
 */
@Service
@Slf4j
@AllArgsConstructor
public class InstallmentDeductionPlanServiceImpl implements InstallmentDeductionPlanService {
    
    private InstallmentDeductionPlanMapper installmentDeductionPlanMapper;
    
    private BatteryMemberCardService batteryMemberCardService;
    
    private CarRentalPackageService carRentalPackageService;
    
    private InstallmentDeductionRecordService installmentDeductionRecordService;
    
    private InstallmentRecordService installmentRecordService;
    
    private TenantService tenantService;
    
    private FyConfigService fyConfigService;
    
    @Override
    public Integer insert(InstallmentDeductionPlan installmentDeductionPlan) {
        return installmentDeductionPlanMapper.insert(installmentDeductionPlan);
    }
    
    @Override
    public Integer update(InstallmentDeductionPlan installmentDeductionPlan) {
        return installmentDeductionPlanMapper.update(installmentDeductionPlan);
    }
    
    @Slave
    @Override
    public R<List<InstallmentDeductionPlan>> listDeductionPlanByAgreementNo(InstallmentDeductionPlanQuery query) {
        return R.ok(installmentDeductionPlanMapper.selectListDeductionPlanByAgreementNo(query));
    }
    
    @Override
    public List<InstallmentDeductionPlan> generateDeductionPlan(InstallmentRecord installmentRecord) {
        // 生成基础代扣计划
        InstallmentDeductionPlan basicDeductionPlan = InstallmentDeductionPlan.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo())
                .packageId(installmentRecord.getPackageId()).packageType(installmentRecord.getPackageType()).status(DEDUCTION_PLAN_STATUS_INIT)
                .tenantId(installmentRecord.getTenantId()).franchiseeId(installmentRecord.getFranchiseeId()).createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis()).build();
        
        // 获取套餐
        BatteryMemberCard batteryMemberCard = null;
        CarRentalPackagePo carRentalPackagePo = null;
        if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
            batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId());
            
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("GENERATE DEDUCTION PLAN WARN! batteryMemberCard is null. externalAgreementNo={}", installmentRecord.getExternalAgreementNo());
                return null;
            }
        } else {
            carRentalPackagePo = carRentalPackageService.selectById(installmentRecord.getPackageId());
            
            if (Objects.isNull(carRentalPackagePo)) {
                log.warn("GENERATE DEDUCTION PLAN WARN! carRentalPackage is null. externalAgreementNo={}", installmentRecord.getExternalAgreementNo());
                return null;
            }
        }
        
        List<InstallmentDeductionPlan> planList = new ArrayList<>(installmentRecord.getInstallmentNo());
        for (int i = 1; i <= installmentRecord.getInstallmentNo(); i++) {
            InstallmentDeductionPlan deductionPlan = new InstallmentDeductionPlan();
            BeanUtils.copyProperties(basicDeductionPlan, deductionPlan);
            deductionPlan.setIssue(i);
            deductionPlan.setAmount(InstallmentUtil.calculateSuborderAmount(i, installmentRecord, batteryMemberCard));
            deductionPlan.setRentTime(InstallmentUtil.calculateSuborderRentTime(i, installmentRecord, batteryMemberCard));
            deductionPlan.setDeductTime(InstallmentUtil.calculateSuborderDeductTime(i));
            planList.add(deductionPlan);
        }
        return planList;
    }
    
    @Override
    public List<String> listExternalAgreementNoForDeduct(Long time) {
        return installmentDeductionPlanMapper.selectListExternalAgreementNoForDeduct(time);
    }
    
    @Override
    public InstallmentDeductionPlan queryPlanForDeductByAgreementNo(String externalAgreementNo) {
        return installmentDeductionPlanMapper.selectPlanForDeductByAgreementNo(externalAgreementNo);
    }
    
    @Override
    public InstallmentDeductionPlan queryByAgreementNoAndIssue(String agreementNo, Integer issue) {
        return installmentDeductionPlanMapper.selectByAgreementNoAndIssue(agreementNo, issue);
    }
    
    @Override
    public R<String> deduct(Long id) {
        try {
            InstallmentDeductionPlan deductionPlan = queryById(id);
            if (Objects.isNull(deductionPlan)) {
                return R.fail("代扣计划不存在");
            }
            
            InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(deductionPlan.getExternalAgreementNo());
            if (Objects.isNull(installmentRecord)) {
                return R.fail("301005", "签约记录不存在");
            }
            
            Tenant tenant = tenantService.queryByIdFromCache(TenantContextHolder.getTenantId());
            if (Objects.isNull(tenant)) {
                log.warn("INSTALLMENT DEDUCT WARN! The user is not associated with a tenant. uid={}", installmentRecord.getUid());
                return R.fail("301004", "请购买分期套餐后，再签约");
            }
            
            FyConfig fyConfig = fyConfigService.queryByTenantIdFromCache(tenant.getId());
            if (Objects.isNull(fyConfig) || StrUtil.isBlank(fyConfig.getMerchantCode()) || StrUtil.isEmpty(fyConfig.getStoreCode()) || StrUtil.isEmpty(fyConfig.getChannelCode())) {
                return R.fail("301003", "签约代扣功能未配置相关信息！请联系客服处理");
            }
            
            // 发起代扣
            Triple<Boolean, String, Object> initiatingDeductTriple = installmentDeductionRecordService.initiatingDeduct(deductionPlan, installmentRecord, fyConfig);
            
            if (Objects.nonNull(initiatingDeductTriple)) {
                return initiatingDeductTriple.getLeft() ? R.ok() : R.fail(initiatingDeductTriple.getMiddle());
            }
        } catch (Exception e) {
            log.error("INSTALLMENT DEDUCT ERROR!", e);
        }
        return R.fail("301006", "代扣失败");
    }
    
    @Slave
    @Override
    public InstallmentDeductionPlan queryById(Long id) {
        return installmentDeductionPlanMapper.selectById(id);
    }
}
