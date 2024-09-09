package com.xiliulou.electricity.mapper.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.query.installment.InstallmentRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:44
 */
@Mapper
public interface InstallmentRecordMapper {
    
    Integer insert(InstallmentRecord installmentRecord);
    
    Integer update(InstallmentRecord installmentRecord);
    
    List<InstallmentRecord> selectPage(InstallmentRecordQuery installmentRecordQuery);
    
    Integer count(InstallmentRecordQuery installmentRecordQuery);

    InstallmentRecord selectRecordWithStatusForUser(@Param("uid") Long uid, @Param("statuses") List<Integer> statuses);
    
    InstallmentRecord selectByExternalAgreementNo(@Param("externalAgreementNo") String externalAgreementNo);
    
    InstallmentRecord selectLatestRecordByUid(@Param("uid") Long uid);
}
