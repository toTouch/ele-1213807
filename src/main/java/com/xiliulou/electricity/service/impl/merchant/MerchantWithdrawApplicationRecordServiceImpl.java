package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawApplicationRecordMapper;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRecordRequest;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 14:37
 */

@Slf4j
@Service("merchantWithdrawApplicationRecordService")
public class MerchantWithdrawApplicationRecordServiceImpl implements MerchantWithdrawApplicationRecordService {
    
    @Resource
    private MerchantWithdrawApplicationRecordMapper merchantWithdrawApplicationRecordMapper;
    
    @Override
    public Integer insertOne(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord) {
        return merchantWithdrawApplicationRecordMapper.insertOne(merchantWithdrawApplicationRecord);
    }
    
    @Override
    public Integer batchInsert(List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecords) {
        return merchantWithdrawApplicationRecordMapper.batchInsert(merchantWithdrawApplicationRecords);
    }
    
    @Override
    public Integer updateOne(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord) {
        return null;
    }
    
    @Override
    public Integer removeById(Long id) {
        return null;
    }
    
    @Override
    public Integer countByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest) {
        return null;
    }
    
    @Override
    public List<MerchantWithdrawApplicationRecordRequest> selectListByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest) {
        return null;
    }
    
    @Override
    public MerchantWithdrawApplicationRecordRequest selectById(Long id) {
        return null;
    }
}
