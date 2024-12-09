package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentDeductionPlanMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import com.xiliulou.electricity.utils.InstallmentUtil;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionPlanAssemblyVO;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionPlanEachVO;
import com.xiliulou.pay.deposit.fengyun.config.FengYunConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_DEDUCTING;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.DEDUCTION_PLAN_STATUS_INIT;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.PACKAGE_TYPE_BATTERY;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:53
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstallmentDeductionPlanServiceImpl implements InstallmentDeductionPlanService {
    
    private final InstallmentDeductionPlanMapper installmentDeductionPlanMapper;
    
    private final BatteryMemberCardService batteryMemberCardService;
    
    private final FengYunConfig fengYunConfig;
    
    
    @Override
    public Integer insert(InstallmentDeductionPlan installmentDeductionPlan) {
        return installmentDeductionPlanMapper.insert(installmentDeductionPlan);
    }
    
    @Override
    public Integer batchInsert(List<InstallmentDeductionPlan> installmentDeductionPlans) {
        return installmentDeductionPlanMapper.batchInsert(installmentDeductionPlans);
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
    
    @Slave
    @Override
    @Deprecated
    public R<List<InstallmentDeductionPlan>> listDeductionPlanByAgreementNoOld(InstallmentDeductionPlanQuery query) {
        List<InstallmentDeductionPlan> deductionPlanList = installmentDeductionPlanMapper.selectListDeductionPlanByAgreementNo(query);
        if (CollectionUtils.isEmpty(deductionPlanList)) {
            return R.ok(Collections.emptyList());
        }
        
        List<InstallmentDeductionPlan> deductionPlans = deductionPlanList.stream().peek(item -> {
            if (Objects.equals(item.getStatus(), DEDUCTION_PLAN_STATUS_DEDUCTING)) {
                item.setStatus(DEDUCTION_PLAN_STATUS_INIT);
            }
        }).sorted(Comparator.comparingInt(InstallmentDeductionPlan::getIssue)).collect(Collectors.toList());
        
        return R.ok(deductionPlans);
    }
    
    @Override
    public R<List<InstallmentDeductionPlanAssemblyVO>> listDeductionPlanForRecordUser(InstallmentDeductionPlanQuery query) {
        List<InstallmentDeductionPlan> deductionPlans = listDeductionPlanByAgreementNo(query).getData();
        
        Map<Integer, InstallmentDeductionPlanAssemblyVO> assemblyVOMap = new HashMap<>();
        
        for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
            if (Objects.isNull(assemblyVOMap.get(deductionPlan.getIssue()))) {
                InstallmentDeductionPlanAssemblyVO assemblyVO = new InstallmentDeductionPlanAssemblyVO();
                BeanUtils.copyProperties(deductionPlan, assemblyVO);
                
                assemblyVOMap.put(deductionPlan.getIssue(), assemblyVO);
            }
        }
        
        for (InstallmentDeductionPlan deductionPlan : deductionPlans) {
            InstallmentDeductionPlanEachVO eachVO = new InstallmentDeductionPlanEachVO();
            BeanUtils.copyProperties(deductionPlan, eachVO);
            
            InstallmentDeductionPlanAssemblyVO planAssemblyVO = assemblyVOMap.get(deductionPlan.getIssue());
            if (Objects.isNull(planAssemblyVO.getInstallmentDeductionPlanEachVOs())) {
                planAssemblyVO.setInstallmentDeductionPlanEachVOs(new ArrayList<>());
            }
            planAssemblyVO.getInstallmentDeductionPlanEachVOs().add(eachVO);
        }
        
        return R.ok(new ArrayList<>(assemblyVOMap.values()));
    }
    
    @Override
    public List<InstallmentDeductionPlan> generateDeductionPlan(InstallmentRecord installmentRecord) {
        // 生成一个签约记录下所有代扣计划的相同数据
        InstallmentDeductionPlan basicDeductionPlan = new InstallmentDeductionPlan();
        basicDeductionPlan.setExternalAgreementNo(installmentRecord.getExternalAgreementNo());
        basicDeductionPlan.setPackageId(installmentRecord.getPackageId());
        basicDeductionPlan.setPackageType(installmentRecord.getPackageType());
        basicDeductionPlan.setStatus(DEDUCTION_PLAN_STATUS_INIT);
        basicDeductionPlan.setTenantId(installmentRecord.getTenantId());
        basicDeductionPlan.setFranchiseeId(installmentRecord.getFranchiseeId());
        basicDeductionPlan.setCreateTime(System.currentTimeMillis());
        basicDeductionPlan.setUpdateTime(System.currentTimeMillis());
        
        // 获取套餐
        BatteryMemberCard batteryMemberCard;
        List<InstallmentDeductionPlan> planList;
        
        BigDecimal deductionMaxAmount = new BigDecimal(fengYunConfig.getDeductionMaxAmount());
        
        if (Objects.equals(installmentRecord.getPackageType(), PACKAGE_TYPE_BATTERY)) {
            batteryMemberCard = batteryMemberCardService.queryByIdFromCache(installmentRecord.getPackageId());
            if (Objects.isNull(batteryMemberCard)) {
                log.warn("GENERATE DEDUCTION PLAN WARN! batteryMemberCard is null. externalAgreementNo={}", installmentRecord.getExternalAgreementNo());
                return null;
            }
            
            planList = new ArrayList<>();
            for (int i = 1; i <= installmentRecord.getInstallmentNo(); i++) {
                // 计算每一期需要代扣的金额
                BigDecimal suborderAmount = InstallmentUtil.calculateSuborderAmount(i, installmentRecord, batteryMemberCard);
                
                // 生成每一期内所有代扣计划的相同数据
                InstallmentDeductionPlan deductionPlan = new InstallmentDeductionPlan();
                BeanUtils.copyProperties(basicDeductionPlan, deductionPlan);
                deductionPlan.setIssue(i);
                deductionPlan.setRentTime(InstallmentUtil.calculateSuborderRentTime(i, installmentRecord, batteryMemberCard));
                deductionPlan.setDeductTime(InstallmentUtil.calculateSuborderDeductTime(i));
                
                if (suborderAmount.compareTo(deductionMaxAmount) <= 0) {
                    // 单期金额在最大单笔代扣金额内，不拆分
                    deductionPlan.setAmount(suborderAmount);
                    planList.add(deductionPlan);
                } else {
                    // 单期金额过大，需要拆分
                    // 求金额为最大代扣金额的子订单的数量
                    BigDecimal suborderNumber = suborderAmount.divide(deductionMaxAmount, 0, RoundingMode.DOWN);
                    
                    // 求剩余需代扣金额
                    BigDecimal remainingAmount = suborderAmount.subtract(deductionMaxAmount.multiply(suborderNumber));
                    
                    // 首先生成剩余金额的代扣计划
                    deductionPlan.setAmount(remainingAmount);
                    planList.add(deductionPlan);
                    
                    // 生成金额为单笔最大金额的代扣计划
                    for (int i1 = 0; i1 < suborderNumber.intValue(); i1++) {
                        InstallmentDeductionPlan maxAmountDeductionPlan = new InstallmentDeductionPlan();
                        BeanUtils.copyProperties(deductionPlan, maxAmountDeductionPlan);
                        maxAmountDeductionPlan.setAmount(deductionMaxAmount);
                        planList.add(maxAmountDeductionPlan);
                    }
                }
            }
        } else {
            // 租车、车电一体套餐
            return Collections.emptyList();
        }
        
        return planList;
    }
    
    @Slave
    @Override
    public List<String> listExternalAgreementNoForDeduct(Long time) {
        return installmentDeductionPlanMapper.selectListExternalAgreementNoForDeduct(time);
    }
    
    @Slave
    @Override
    public List<InstallmentDeductionPlan> listByExternalAgreementNoAndIssue(Integer tenantId, String externalAgreementNo, Integer issue) {
        return installmentDeductionPlanMapper.selectListByExternalAgreementNoAndIssue(tenantId, externalAgreementNo, issue);
    }
    
    @Slave
    @Override
    public InstallmentDeductionPlan queryById(Long id) {
        return installmentDeductionPlanMapper.selectById(id);
    }
}
