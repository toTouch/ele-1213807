package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeMonthRecordMapper;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeMonthRecordService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName : merchantPlaceFeeMonthRecordService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
@Service
public class MerchantPlaceFeeMonthRecordServiceImpl implements MerchantPlaceFeeMonthRecordService {
    
    @Resource
    private MerchantPlaceFeeMonthRecordMapper merchantPlaceFeeMonthRecordMapper;
    
    @Override
    @Slave
    public List<MerchantPlaceFeeMonthRecord> selectByMonthDate(String monthDate, Integer tenantId, Long franchiseeId) {
        return merchantPlaceFeeMonthRecordMapper.selectListBySettlementTime(monthDate, tenantId, franchiseeId);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceFeeMonthRecord> queryList(List<Long> placeIdList, List<String> monthList) {
        List<MerchantPlaceFeeMonthRecord> lastMonthRecords = merchantPlaceFeeMonthRecordMapper.selectList(placeIdList, monthList);
        if (ObjectUtils.isEmpty(lastMonthRecords)) {
            return Collections.emptyList();
        }
    
        // 过滤掉开始时间为空的数据
        lastMonthRecords = lastMonthRecords.stream().filter(item -> Objects.nonNull(item.getRentStartTime()) && Objects.nonNull(item.getRentEndTime())).collect(Collectors.toList());
    
        if (ObjectUtils.isEmpty(lastMonthRecords)) {
            return Collections.emptyList();
        }
        
        return lastMonthRecords;
    }
}
