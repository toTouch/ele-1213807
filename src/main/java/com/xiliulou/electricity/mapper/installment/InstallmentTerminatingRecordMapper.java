package com.xiliulou.electricity.mapper.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 10:54
 */
@Mapper
public interface InstallmentTerminatingRecordMapper {
    
    Integer insert(InstallmentTerminatingRecord installmentTerminatingRecord);
    
    Integer update(InstallmentTerminatingRecord installmentTerminatingRecord);
    
    List<InstallmentTerminatingRecord> selectPage(InstallmentTerminatingRecordQuery query);
    
    Integer count(InstallmentTerminatingRecordQuery query);
}
