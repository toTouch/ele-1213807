package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.service.installment.InstallmentSearchApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public InstallmentRecord queryRecordWithStatusForUser(Long uid, List<Integer> statuses) {
        return installmentRecordMapper.selectRecordWithStatusForUser(uid, statuses);
    }
}
