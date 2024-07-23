package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthSummaryRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeMonthSummaryRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeMonthSummaryRecordQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthSummaryRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName : MerchantPlaceFeeMonthSummaryRecordServiceImpl
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
@Service
public class MerchantPlaceFeeMonthSummaryRecordServiceImpl implements MerchantPlaceFeeMonthSummaryRecordService {
    
    @Resource
    private MerchantPlaceFeeMonthSummaryRecordMapper merchantPlaceFeeMonthSummaryRecordMapper;
    
    @Slave
    @Override
    public List<MerchantPlaceFeeMonthSummaryRecord> selectByCondition(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel) {
        return merchantPlaceFeeMonthSummaryRecordMapper.selectListByCondition(queryModel);
    }
    
    @Slave
    @Override
    public Integer pageCountByCondition(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel) {
        Integer count = merchantPlaceFeeMonthSummaryRecordMapper.pageCountByCondition(queryModel);
        if(Objects.nonNull(count)){
            return count;
        }
        return 0;
    }
}
