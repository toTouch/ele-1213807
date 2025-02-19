package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentTerminatingRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_PAID;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_SOURCE_CANCEL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_SOURCE_COMPLETED;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_INIT;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 10:52
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstallmentTerminatingRecordServiceImpl implements InstallmentTerminatingRecordService {
    
    private final InstallmentTerminatingRecordMapper installmentTerminatingRecordMapper;
    
    private final FranchiseeService franchiseeService;
    
    private final BatteryMemberCardService batteryMemberCardService;
    
    private final CarRentalPackageService carRentalPackageService;
    
    private final InstallmentDeductionPlanService installmentDeductionPlanService;
    
    private final UserService userService;
    
    
    @Override
    public Integer insert(InstallmentTerminatingRecord installmentTerminatingRecord) {
        return installmentTerminatingRecordMapper.insert(installmentTerminatingRecord);
    }
    
    @Override
    public Integer update(InstallmentTerminatingRecord installmentTerminatingRecord) {
        return installmentTerminatingRecordMapper.update(installmentTerminatingRecord);
    }
    
    @Slave
    @Override
    public R<List<InstallmentTerminatingRecordVO>> listForPage(InstallmentTerminatingRecordQuery query) {
        List<InstallmentTerminatingRecord> records = installmentTerminatingRecordMapper.selectPage(query);
        if (CollectionUtils.isEmpty(records)) {
            return R.ok(Collections.emptyList());
        }
        
        List<InstallmentTerminatingRecordVO> collect = records.parallelStream().map(installmentTerminatingRecord -> {
            InstallmentTerminatingRecordVO vo = new InstallmentTerminatingRecordVO();
            BeanUtils.copyProperties(installmentTerminatingRecord, vo);
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(installmentTerminatingRecord.getFranchiseeId());
            vo.setFranchiseeName(Objects.isNull(franchisee) ? null : franchisee.getName());
            
            // 设置电或者车的套餐名称，设置总金额和未支付金额
            if (Objects.equals(installmentTerminatingRecord.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                // 根据代扣计划计算签约总金额与未支付金额
                Pair<BigDecimal, BigDecimal> pair = queryRentPriceAndUnpaidAmount(installmentTerminatingRecord.getExternalAgreementNo());
                
                BatteryMemberCard memberCard = batteryMemberCardService.queryByIdFromCache(installmentTerminatingRecord.getPackageId());
                if (Objects.nonNull(memberCard)) {
                    vo.setPackageName(memberCard.getName());
                    vo.setAmount(pair.getLeft());
                    vo.setUnpaidAmount(pair.getRight());
                }
            } else {
                CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(installmentTerminatingRecord.getPackageId());
                if (Objects.nonNull(carRentalPackagePo)) {
                    vo.setPackageName(carRentalPackagePo.getName());
                }
            }
            
            // 设置审核人名称
            if (Objects.nonNull(installmentTerminatingRecord.getAuditorId())) {
                User user = userService.queryByUidFromCache(installmentTerminatingRecord.getAuditorId());
                vo.setAuditorName(Objects.isNull(user) ? null : user.getName());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return R.ok(collect);
    }
    
    @Slave
    @Override
    public R<Integer> count(InstallmentTerminatingRecordQuery query) {
        return R.ok(installmentTerminatingRecordMapper.count(query));
    }
    
    @Slave
    @Override
    public List<InstallmentTerminatingRecord> listForRecordWithStatus(InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordMapper.selectListForRecordWithStatus(query);
    }
    
    @Override
    public InstallmentTerminatingRecord generateTerminatingRecord(InstallmentRecord installmentRecord, String reason, Boolean completedOrNot) {
        InstallmentDeductionPlanQuery query = new InstallmentDeductionPlanQuery();
        query.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        query.setStatuses(List.of(DEDUCTION_PLAN_STATUS_PAID));
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(query).getData();
        
        BigDecimal paidAmount = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(deductionPlans)) {
            for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
                paidAmount = paidAmount.add(deductionPlan.getAmount());
            }
        }
        
        Integer source = completedOrNot ? TERMINATING_RECORD_SOURCE_COMPLETED : TERMINATING_RECORD_SOURCE_CANCEL;
        
        InstallmentTerminatingRecord installmentTerminatingRecord = new InstallmentTerminatingRecord();
        installmentTerminatingRecord.setUid(installmentRecord.getUid());
        installmentTerminatingRecord.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        installmentTerminatingRecord.setUserName(installmentRecord.getUserName());
        installmentTerminatingRecord.setMobile(installmentRecord.getMobile());
        installmentTerminatingRecord.setPackageId(installmentRecord.getPackageId());
        installmentTerminatingRecord.setPackageType(installmentRecord.getPackageType());
        installmentTerminatingRecord.setSource(source);
        installmentTerminatingRecord.setPaidAmount(paidAmount);
        installmentTerminatingRecord.setStatus(TERMINATING_RECORD_STATUS_INIT);
        installmentTerminatingRecord.setReason(reason);
        installmentTerminatingRecord.setTenantId(installmentRecord.getTenantId());
        installmentTerminatingRecord.setFranchiseeId(installmentRecord.getFranchiseeId());
        installmentTerminatingRecord.setCreateTime(System.currentTimeMillis());
        installmentTerminatingRecord.setUpdateTime(System.currentTimeMillis());
        return installmentTerminatingRecord;
    }
    
    @Slave
    @Override
    public InstallmentTerminatingRecord queryById(Long id) {
        return installmentTerminatingRecordMapper.selectById(id);
    }
    
    @Slave
    @Override
    public InstallmentTerminatingRecord queryLatestByExternalAgreementNo(String externalAgreementNo) {
        return installmentTerminatingRecordMapper.selectLatestByExternalAgreementNo(externalAgreementNo);
    }
    
    @Slave
    @Override
    public List<InstallmentTerminatingRecord> listForUserWithStatus(InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordMapper.selectListForUserWithStatus(query);
    }
    
    @Override
    public Pair<BigDecimal, BigDecimal> queryRentPriceAndUnpaidAmount(String externalAgreementNo) {
        InstallmentDeductionPlanQuery planQuery = new InstallmentDeductionPlanQuery();
        planQuery.setExternalAgreementNo(externalAgreementNo);
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(planQuery).getData();
        BigDecimal rentPrice = BigDecimal.ZERO;
        BigDecimal unpaidPrice = BigDecimal.ZERO;
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(deductionPlans)) {
            for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
                rentPrice = rentPrice.add(deductionPlan.getAmount());
                if (Objects.equals(deductionPlan.getStatus(), DEDUCTION_PLAN_STATUS_PAID)) {
                    continue;
                }
                unpaidPrice = unpaidPrice.add(deductionPlan.getAmount());
            }
        }
        
        return Pair.of(rentPrice, unpaidPrice);
    }
    
    @Slave
    @Override
    public List<InstallmentTerminatingRecord> listByExternalAgreementNo(InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordMapper.selectListByExternalAgreementNo(query);
    }
    
}
