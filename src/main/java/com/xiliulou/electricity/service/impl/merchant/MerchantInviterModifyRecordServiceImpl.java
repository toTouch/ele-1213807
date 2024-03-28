package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantInviterModifyRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantInviterModifyRecordQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantInviterModifyRecordRequest;
import com.xiliulou.electricity.service.merchant.MerchantInviterModifyRecordService;
import com.xiliulou.electricity.vo.merchant.MerchantInviterModifyRecordVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @description 邀请人修改记录
 * @date 2024/3/28 09:34:26
 */
@Service
public class MerchantInviterModifyRecordServiceImpl implements MerchantInviterModifyRecordService {
    
    @Resource
    private MerchantInviterModifyRecordMapper merchantInviterModifyRecordMapper;
    
    @Override
    public Integer insertOne(MerchantInviterModifyRecord record) {
        return merchantInviterModifyRecordMapper.insertOne(record);
    }
    
    @Slave
    @Override
    public List<MerchantInviterModifyRecordVO> listByPage(MerchantInviterModifyRecordRequest request) {
        MerchantInviterModifyRecordQueryModel queryModel = new MerchantInviterModifyRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        
        List<MerchantInviterModifyRecord> merchantInviterModifyRecordList = merchantInviterModifyRecordMapper.selectPage(queryModel);
        if (CollectionUtils.isEmpty(merchantInviterModifyRecordList)) {
            return Collections.emptyList();
        }
        
        return merchantInviterModifyRecordList.stream().map(item -> {
            MerchantInviterModifyRecordVO recordVO = new MerchantInviterModifyRecordVO();
            BeanUtils.copyProperties(item, recordVO);
            recordVO.setOperateTime(item.getCreateTime());
            
            return recordVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantInviterModifyRecordRequest request) {
        MerchantInviterModifyRecordQueryModel queryModel = new MerchantInviterModifyRecordQueryModel();
        BeanUtils.copyProperties(request, queryModel);
        
        return merchantInviterModifyRecordMapper.countTotal(queryModel);
    }
}
