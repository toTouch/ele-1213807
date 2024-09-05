package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentTerminatingRecordMapper;
import com.xiliulou.electricity.query.EleRefundQuery;
import com.xiliulou.electricity.query.installment.HandleTerminatingRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EleRefundOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_PAID;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_CANCELLED;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_COMPLETED;
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
    
    private InstallmentRecordService installmentRecordService;
    
    private EleRefundOrderService eleRefundOrderService;
    
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
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
            String packageName;
            if (Objects.equals(installmentTerminatingRecord.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
                BatteryMemberCard memberCard = batteryMemberCardService.queryByIdFromCache(installmentTerminatingRecord.getPackageId());
                vo.setPackageName(memberCard.getName());
                vo.setAmount(memberCard.getRentPrice());
                vo.setUnpaidAmount(vo.getAmount().subtract(vo.getPaidAmount()));
            } else {
                CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(installmentTerminatingRecord.getPackageId());
                vo.setPackageName(carRentalPackagePo.getName());
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
    
    @Override
    public List<InstallmentTerminatingRecord> listForRecordWithStatus(InstallmentTerminatingRecordQuery query) {
        return installmentTerminatingRecordMapper.selectListForRecordWithStatus(query);
    }
    
    @Override
    public R<String> createTerminatingRecord(String externalAgreementNo, String reason) {
        InstallmentRecord installmentRecord = installmentRecordService.queryByExternalAgreementNo(externalAgreementNo);
        if (Objects.isNull(installmentRecord) || Arrays.asList(INSTALLMENT_RECORD_STATUS_COMPLETED, INSTALLMENT_RECORD_STATUS_CANCELLED).contains(installmentRecord.getStatus())) {
            log.info("CREATE TERMINATING RECORD INFO! Record cancellation externalAgreementNo={}", externalAgreementNo);
            return R.fail("分期套餐已解约");
        }
        
        Integer num = (Integer) eleRefundOrderService.queryCount(
                EleRefundQuery.builder().uid(installmentRecord.getUid()).statuses(Arrays.asList(EleRefundOrder.STATUS_INIT, EleRefundOrder.STATUS_REFUND)).build()).getData();
        if (Objects.nonNull(num) && num > 0) {
            return R.fail("有未完成的押金退款订单");
        }
        
        List<InstallmentTerminatingRecord> records = installmentTerminatingRecordMapper.selectListForRecordWithStatus(
                InstallmentTerminatingRecordQuery.builder().uid(installmentRecord.getUid()).statuses(List.of(TERMINATING_RECORD_STATUS_INIT)).build());
        if (CollectionUtils.isNotEmpty(records)) {
            return R.fail("有未完成的解约申请");
        }
        
        InstallmentTerminatingRecord installmentTerminatingRecord = generateTerminatingRecord(installmentRecord, reason);
        installmentTerminatingRecordMapper.insert(installmentTerminatingRecord);
        
        return R.ok();
    }
    
    @Override
    public InstallmentTerminatingRecord generateTerminatingRecord(InstallmentRecord installmentRecord, String reason) {
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(
                InstallmentRecordQuery.builder().externalAgreementNo(installmentRecord.getExternalAgreementNo()).statuses(List.of(DEDUCTION_PLAN_STATUS_PAID)).build()).getData();
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
        installmentTerminatingRecord.setPackageId(installmentTerminatingRecord.getPackageId());
        installmentTerminatingRecord.setPackageType(installmentRecord.getPackageType());
        installmentTerminatingRecord.setPaidAmount(paidAmount);
        installmentTerminatingRecord.setStatus(TERMINATING_RECORD_STATUS_INIT);
        installmentTerminatingRecord.setReason(reason);
        installmentTerminatingRecord.setTenantId(installmentRecord.getTenantId());
        installmentTerminatingRecord.setFranchiseeId(installmentTerminatingRecord.getFranchiseeId());
        installmentTerminatingRecord.setCreateTime(System.currentTimeMillis());
        installmentTerminatingRecord.setUpdateTime(System.currentTimeMillis());
        return installmentTerminatingRecord;
    }
    
    @Override
    public R<String> handleTerminatingRecord(HandleTerminatingRecordQuery query) {
        return null;
    }
    
    
}
