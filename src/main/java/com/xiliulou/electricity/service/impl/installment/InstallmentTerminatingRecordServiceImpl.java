package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentTerminatingRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_PAID;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_INIT;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 10:52
 */
@Service
@Slf4j
@AllArgsConstructor
public class InstallmentTerminatingRecordServiceImpl implements InstallmentTerminatingRecordService {
    
    private InstallmentTerminatingRecordMapper installmentTerminatingRecordMapper;
    
    private FranchiseeService franchiseeService;
    
    private BatteryMemberCardService batteryMemberCardService;
    
    private CarRentalPackageService carRentalPackageService;
    
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    private UserInfoService userInfoService;
    
    
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
        
        List<InstallmentTerminatingRecordVO> collect = records.parallelStream().map(installmentTerminatingRecord -> {
            InstallmentTerminatingRecordVO vo = new InstallmentTerminatingRecordVO();
            BeanUtils.copyProperties(installmentTerminatingRecord, vo);
            
            vo.setFranchiseeName(franchiseeService.queryByIdFromCache(installmentTerminatingRecord.getFranchiseeId()).getName());
            
            // 设置电或者车的套餐名称，设置总金额和未支付金额
            if (Objects.equals(installmentTerminatingRecord.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                BatteryMemberCard memberCard = batteryMemberCardService.queryByIdFromCache(installmentTerminatingRecord.getPackageId());
                vo.setPackageName(memberCard.getName());
                vo.setAmount(memberCard.getRentPrice());
                vo.setUnpaidAmount(vo.getAmount().subtract(vo.getPaidAmount()));
            } else {
                CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(installmentTerminatingRecord.getPackageId());
                vo.setPackageName(carRentalPackagePo.getName());
            }
            
            // 设置审核人名称
            vo.setAuditorName(userInfoService.queryByUidFromCache(installmentTerminatingRecord.getAuditorId()).getName());
            
            return vo;
        }).collect(Collectors.toList());
        
        return R.ok(collect);
    }
    
    @Slave
    @Override
    public R<Integer> count(InstallmentTerminatingRecordQuery query) {
        return R.ok(installmentTerminatingRecordMapper.count(query));
    }
    
    @Override
    public List<InstallmentTerminatingRecord> listForRecordWithStatus(InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordMapper.selectListForRecordWithStatus(query);
    }
    
    @Override
    public InstallmentTerminatingRecord generateTerminatingRecord(InstallmentRecord installmentRecord, String reason) {
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
        
        InstallmentTerminatingRecord installmentTerminatingRecord = new InstallmentTerminatingRecord();
        installmentTerminatingRecord.setUid(installmentRecord.getUid());
        installmentTerminatingRecord.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        installmentTerminatingRecord.setUserName(installmentRecord.getUserName());
        installmentTerminatingRecord.setMobile(installmentRecord.getMobile());
        installmentTerminatingRecord.setPackageId(installmentRecord.getPackageId());
        installmentTerminatingRecord.setPackageType(installmentRecord.getPackageType());
        installmentTerminatingRecord.setPaidAmount(paidAmount);
        installmentTerminatingRecord.setStatus(TERMINATING_RECORD_STATUS_INIT);
        installmentTerminatingRecord.setReason(reason);
        installmentTerminatingRecord.setTenantId(installmentRecord.getTenantId());
        installmentTerminatingRecord.setFranchiseeId(installmentRecord.getFranchiseeId());
        installmentTerminatingRecord.setCreateTime(System.currentTimeMillis());
        installmentTerminatingRecord.setUpdateTime(System.currentTimeMillis());
        return installmentTerminatingRecord;
    }
    
    
    @Override
    public InstallmentTerminatingRecord queryById(Long id) {
        return installmentTerminatingRecordMapper.selectById(id);
    }
    
    @Override
    public InstallmentTerminatingRecord queryLatestByExternalAgreementNo(String externalAgreementNo) {
        return installmentTerminatingRecordMapper.selectLatestByExternalAgreementNo(externalAgreementNo);
    }
    
}
