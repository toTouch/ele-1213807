package com.xiliulou.electricity.service.impl.installment;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import com.xiliulou.electricity.mapper.installment.InstallmentDeductionRecordMapper;
import com.xiliulou.electricity.query.installment.InstallmentDeductionRecordQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.installment.InstallmentDeductionRecordService;
import com.xiliulou.electricity.vo.installment.InstallmentDeductionRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/26 10:56
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InstallmentDeductionRecordServiceImpl implements InstallmentDeductionRecordService {
    
    private final InstallmentDeductionRecordMapper installmentDeductionRecordMapper;
    
    private final FranchiseeService franchiseeService;
    
    
    @Override
    public Integer insert(InstallmentDeductionRecord installmentDeductionRecord) {
        return installmentDeductionRecordMapper.insert(installmentDeductionRecord);
    }
    
    @Override
    public Integer update(InstallmentDeductionRecord installmentDeductionRecord) {
        return installmentDeductionRecordMapper.update(installmentDeductionRecord);
    }
    
    @Slave
    @Override
    public R<List<InstallmentDeductionRecordVO>> listForPage(InstallmentDeductionRecordQuery installmentDeductionRecordQuery) {
        List<InstallmentDeductionRecord> installmentDeductionRecords = installmentDeductionRecordMapper.selectPage(installmentDeductionRecordQuery);
        
        List<InstallmentDeductionRecordVO> collect = installmentDeductionRecords.parallelStream().map(installmentDeductionRecord -> {
            InstallmentDeductionRecordVO recordVO = new InstallmentDeductionRecordVO();
            BeanUtils.copyProperties(installmentDeductionRecord, recordVO);
            
            recordVO.setFranchiseeName(franchiseeService.queryByIdFromCache(installmentDeductionRecord.getFranchiseeId()).getName());
            return recordVO;
        }).collect(Collectors.toList());
        return R.ok(collect);
    }
    
    @Slave
    @Override
    public R<Integer> count(InstallmentDeductionRecordQuery installmentDeductionRecordQuery) {
        return R.ok(installmentDeductionRecordMapper.count(installmentDeductionRecordQuery));
    }
    
    @Slave
    @Override
    public InstallmentDeductionRecord queryByPayNo(String payNo) {
        return installmentDeductionRecordMapper.selectRecordByPayNo(payNo);
    }
    
    @Slave
    @Override
    public List<InstallmentDeductionRecord> listDeductionRecord(InstallmentDeductionRecordQuery installmentDeductionRecordQuery) {
        return installmentDeductionRecordMapper.selectListDeductionRecord(installmentDeductionRecordQuery);
    }
    
    
}
