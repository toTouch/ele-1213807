package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentRecordMapper;
import com.xiliulou.electricity.service.installment.InstallmentRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:51
 */
@Service
@Slf4j
public class InstallmentRecordServiceImpl implements InstallmentRecordService {
    
    @Autowired
    private InstallmentRecordMapper installmentRecordMapper;
    
    @Override
    public int insert(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.insert(installmentRecord);
    }
    
    @Override
    public int update(InstallmentRecord installmentRecord) {
        return installmentRecordMapper.update(installmentRecord);
    }
}
