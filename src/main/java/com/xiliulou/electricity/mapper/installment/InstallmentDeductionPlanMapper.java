package com.xiliulou.electricity.mapper.installment;

import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import com.xiliulou.electricity.query.installment.InstallmentDeductionPlanQuery;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:55
 */
@Mapper
public interface InstallmentDeductionPlanMapper {
    
    Integer insert(InstallmentDeductionPlan installmentDeductionPlan);
    
    Integer update(InstallmentDeductionPlan installmentDeductionPlan);
    
    List<InstallmentDeductionPlan> selectListDeductionPlanByAgreementNo(InstallmentDeductionPlanQuery query);
    
    List<String> selectListExternalAgreementNoForDeduct(@Param("time") Long time);
    
    InstallmentDeductionPlan selectPlanForDeductByAgreementNo(@Param("externalAgreementNo") String externalAgreementNo);
}
