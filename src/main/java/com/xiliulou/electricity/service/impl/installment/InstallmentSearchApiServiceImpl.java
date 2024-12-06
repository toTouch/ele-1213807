package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.service.installment.InstallmentSearchApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_SIGN;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_TERMINATE;
import static com.xiliulou.electricity.constant.installment.InstallmentConstants.INSTALLMENT_RECORD_STATUS_UN_SIGN;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/5 11:34
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstallmentSearchApiServiceImpl implements InstallmentSearchApiService {
    
    private final InstallmentRecordMapper installmentRecordMapper;
    
    @Slave
    @Override
    public InstallmentRecord queryUsingRecordForUser(Long uid) {
        return installmentRecordMapper.selectRecordWithStatusForUser(uid,
                Arrays.asList(INSTALLMENT_RECORD_STATUS_UN_SIGN, INSTALLMENT_RECORD_STATUS_SIGN, INSTALLMENT_RECORD_STATUS_TERMINATE));
    }
}
