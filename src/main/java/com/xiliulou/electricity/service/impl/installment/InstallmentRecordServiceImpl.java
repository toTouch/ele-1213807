package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.installment.InstallmentConstants;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.car.CarRentalPackagePo;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentPayQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import com.xiliulou.electricity.service.installment.InstallmentTerminatingRecordService;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.installment.InstallmentRecordVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_FAIL;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_TERMINATE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UN_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.TERMINATING_RECORD_STATUS_INIT;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:51
 */
@Service
@AllArgsConstructor
@Slf4j
public class InstallmentRecordServiceImpl implements InstallmentRecordService {
    
    private InstallmentRecordMapper installmentRecordMapper;
    
    private FranchiseeService franchiseeService;
    
    private BatteryMemberCardService batteryMemberCardService;
    
    private CarRentalPackageService carRentalPackageService;
    
    private InstallmentDeductionPlanService installmentDeductionPlanService;
    
    private InstallmentTerminatingRecordService installmentTerminatingRecordService;
    
    
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
            
            // 设置套餐信息
            setPackageMessage(installmentRecordVO, installmentRecord);
            
            installmentRecordVO.setUnpaidInstallmentNo(installmentRecordVO.getInstallmentNo() - installmentRecordVO.getPaidInstallment());
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
        InstallmentRecord installmentRecord = new InstallmentRecord();
        installmentRecord.setUid(userInfo.getUid());
        installmentRecord.setExternalAgreementNo(externalAgreementNo);
        installmentRecord.setUserName(null);
        installmentRecord.setMobile(null);
        installmentRecord.setPackageType(query.getPackageType());
        installmentRecord.setPaidAmount(new BigDecimal("0"));
        installmentRecord.setStatus(INSTALLMENT_RECORD_STATUS_INIT);
        installmentRecord.setPaidInstallment(0);
        installmentRecord.setCreateTime(System.currentTimeMillis());
        installmentRecord.setUpdateTime(System.currentTimeMillis());
        
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
    public InstallmentRecord queryRecordWithStatusForUser(Long uid, List<Integer> statuses) {
        return installmentRecordMapper.selectRecordWithStatusForUser(uid, statuses);
    }
    
    
    @Override
    public InstallmentRecord queryByExternalAgreementNo(String externalAgreementNo) {
        return installmentRecordMapper.selectByExternalAgreementNo(externalAgreementNo);
    }
    
    @Slave
    @Override
    public R<InstallmentRecordVO> queryInstallmentRecordForUser() {
        Long uid = SecurityUtils.getUid();
        InstallmentRecord installmentRecord = installmentRecordMapper.selectRecordWithStatusForUser(uid,
                Arrays.asList(INSTALLMENT_RECORD_STATUS_INIT, INSTALLMENT_RECORD_STATUS_UN_SIGN, INSTALLMENT_RECORD_STATUS_SIGN, INSTALLMENT_RECORD_STATUS_TERMINATE));
        
        if (Objects.isNull(installmentRecord)) {
            return R.ok();
        }
        InstallmentRecordVO installmentRecordVO = new InstallmentRecordVO();
        BeanUtils.copyProperties(installmentRecord, installmentRecordVO);
        // 设置套餐信息
        setPackageMessage(installmentRecordVO, installmentRecord);
        
        // 查询有无逾期代扣计划
        InstallmentDeductionPlanQuery deductionPlanQuery = new InstallmentDeductionPlanQuery();
        deductionPlanQuery.setStatuses(Arrays.asList(DEDUCTION_PLAN_STATUS_INIT, DEDUCTION_PLAN_STATUS_FAIL));
        deductionPlanQuery.setEndTime(System.currentTimeMillis());
        deductionPlanQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        
        List<InstallmentDeductionPlan> deductionPlans = installmentDeductionPlanService.listDeductionPlanByAgreementNo(deductionPlanQuery).getData();
        installmentRecordVO.setOverdue(CollectionUtils.isEmpty(deductionPlans) ? 0 : 1);
        
        // 查询有无审核中的解约申请
        InstallmentTerminatingRecordQuery terminatingRecordQuery = new InstallmentTerminatingRecordQuery();
        terminatingRecordQuery.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        terminatingRecordQuery.setStatus(TERMINATING_RECORD_STATUS_INIT);
        
        List<InstallmentTerminatingRecord> terminatingRecords = installmentTerminatingRecordService.listForRecordWithStatus(terminatingRecordQuery);
        installmentRecordVO.setUnderReview(CollectionUtils.isEmpty(terminatingRecords) ? 0 : 1);
        
        return R.ok(installmentRecordVO);
    }
    
    
    private void setPackageMessage(InstallmentRecordVO installmentRecordVO, InstallmentRecord installmentRecord) {
        if (Objects.equals(installmentRecordVO.getPackageType(), InstallmentConstants.PACKAGE_TYPE_BATTERY)) {
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecordVO.getPackageId());
            
            installmentRecordVO.setPackageName(batteryMemberCard.getName());
            installmentRecordVO.setInstallmentServiceFee(batteryMemberCard.getInstallmentServiceFee());
            installmentRecordVO.setDownPayment(batteryMemberCard.getDownPayment());
            installmentRecordVO.setRentPrice(batteryMemberCard.getRentPrice());
            installmentRecordVO.setUnpaidAmount(batteryMemberCard.getRentPrice().subtract(installmentRecord.getPaidAmount()));
            
            // 计算剩余每期金额
            installmentRecordVO.setRemainingPrice(InstallmentUtil.calculateSuborderAmount(2, installmentRecord, batteryMemberCard));
        } else {
            CarRentalPackagePo carRentalPackagePo = carRentalPackageService.selectById(installmentRecordVO.getPackageId());
            installmentRecordVO.setPackageName(carRentalPackagePo.getName());
        }
    }
    
    
}
