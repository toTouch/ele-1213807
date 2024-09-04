package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentDeductionPlanMapper;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import com.xiliulou.electricity.service.installment.InstallmentDeductionPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:53
 */
@Service
@Slf4j
public class InstallmentDeductionPlanServiceImpl implements InstallmentDeductionPlanService {

    @Autowired
    private InstallmentDeductionPlanMapper installmentDeductionPlanMapper;
    
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
    
}
