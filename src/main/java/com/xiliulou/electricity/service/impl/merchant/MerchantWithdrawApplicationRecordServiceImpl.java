package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.merchant.MerchantWithdrawApplicationRecordBO;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantWithdrawApplicationRecordMapper;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRecordRequest;
import com.xiliulou.electricity.service.merchant.MerchantWithdrawApplicationRecordService;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        return merchantWithdrawApplicationRecordMapper.updateOne(merchantWithdrawApplicationRecord);
    }
    
    @Override
    public Integer removeById(Long id) {
        return null;
    }
    
    @Slave
    @Override
    public Integer countByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest) {
        return merchantWithdrawApplicationRecordMapper.countByCondition(merchantWithdrawApplicationRecordRequest);
    }
    
    @Slave
    @Override
    public List<MerchantWithdrawApplicationRecordVO> selectListByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest) {
       
        return merchantWithdrawApplicationRecordMapper.selectListByCondition(merchantWithdrawApplicationRecordRequest);
    }
    
    @Slave
    @Override
    public MerchantWithdrawApplicationRecordVO selectById(Long id) {
        return merchantWithdrawApplicationRecordMapper.selectById(id);
    }
    
    @Slave
    @Override
    public MerchantWithdrawApplicationRecord selectByOrderNo(String orderNo, Integer tenantId) {
        return merchantWithdrawApplicationRecordMapper.selectByOrderNo(orderNo, tenantId);
    }
    
    @Override
    public Integer updateApplicationRecordStatusByBatchNo(Integer status, String batchNo, Integer tenantId) {
        Long updateTime = System.currentTimeMillis();
        return merchantWithdrawApplicationRecordMapper.updateApplicationRecordStatusByBatchNo(status, updateTime, batchNo, tenantId);
    }
    
    @Slave
    @Override
    public List<MerchantWithdrawApplicationRecordBO> selectListByBatchNo(String batchNo, Integer tenantId) {
        if(Objects.isNull(batchNo) || Objects.isNull(tenantId)){
            return Collections.EMPTY_LIST;
        }
        return merchantWithdrawApplicationRecordMapper.selectListByBatchNo(batchNo, tenantId);
    }
    
    @Override
    public Integer updateMerchantWithdrawRecordStatus(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord) {
        return merchantWithdrawApplicationRecordMapper.updateApplicationRecordStatus(merchantWithdrawApplicationRecord);
    }
    
    @Override
    public List<MerchantWithdrawApplicationRecordVO> selectWithdrawRecordList(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest) {
        return null;
    }
    
    @Override
    public Integer selectWithdrawRecordListCount(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest) {
        return null;
    }
    
    
}
