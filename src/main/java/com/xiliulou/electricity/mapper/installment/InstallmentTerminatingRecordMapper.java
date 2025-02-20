package com.xiliulou.electricity.mapper.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import com.xiliulou.electricity.query.installment.InstallmentTerminatingRecordQuery;
import com.xiliulou.electricity.vo.installment.InstallmentTerminatingRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    
    List<InstallmentTerminatingRecord> selectListForRecordWithStatus(InstallmentTerminatingRecordQuery query);
    
    InstallmentTerminatingRecord selectById(@Param("id") Long id);
    
    InstallmentTerminatingRecord selectLatestByExternalAgreementNo(String externalAgreementNo);
    
    List<InstallmentTerminatingRecord> selectListForUserWithStatus(InstallmentTerminatingRecordQuery query);
    
    List<InstallmentTerminatingRecord> selectListByExternalAgreementNo(InstallmentTerminatingRecordQuery query);
}
