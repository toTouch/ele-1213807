package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentDeductionPlanMapper;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.utils.InstallmentUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    public R<List<InstallmentDeductionPlan>> listDeductionPlanByAgreementNo(InstallmentRecordQuery installmentRecordQuery) {
        return R.ok(installmentDeductionPlanMapper.selectListDeductionPlanByAgreementNo(installmentRecordQuery));
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
                log.warn("GENERATE DEDUCTION PLAN WARN! batteryMemberCard is null. externalAgreementNo={}",installmentRecord.getExternalAgreementNo());
                return Triple.of(false, null, null);
            }
        } else {
            carRentalPackagePo = carRentalPackageService.selectById(installmentRecord.getPackageId());
            
            if (Objects.isNull(carRentalPackagePo)) {
                log.warn("GENERATE DEDUCTION PLAN WARN! carRentalPackage is null. externalAgreementNo={}",installmentRecord.getExternalAgreementNo());
                return Triple.of(false, null, null);
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
        return Triple.of(true, null, planList);
    }
    
}
