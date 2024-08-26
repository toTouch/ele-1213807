package com.xiliulou.electricity.mapper.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:44
 */
@Mapper
public interface InstallmentRecordMapper {
    
    int insert(InstallmentRecord installmentRecord);
    
    int update(InstallmentRecord installmentRecord);
}
