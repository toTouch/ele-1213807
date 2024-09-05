package com.xiliulou.electricity.mapper.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:46
 */
@Mapper
public interface InstallmentDeductionRecordMapper {
    
    Integer insert(InstallmentDeductionRecord installmentDeductionRecord);
    
    Integer update(InstallmentDeductionRecord installmentDeductionRecord);
    
    List<InstallmentDeductionRecord> selectPage(InstallmentDeductionRecordQuery installmentDeductionRecordQuery);
    
    Integer count(InstallmentDeductionRecordQuery installmentDeductionRecordQuery);
    
    InstallmentDeductionRecord selectRecordByPayNo(String payNo);
}
