package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionDayRecord;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeePromotionDayRecordMapper;
import com.xiliulou.electricity.service.merchant.ChannelEmployeePromotionDayRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/21 14:03
 * @desc
 */
@Service
public class ChannelEmployeePromotionDayRecordServiceImpl implements ChannelEmployeePromotionDayRecordService {
    @Resource
    private ChannelEmployeePromotionDayRecordMapper channelEmployeePromotionDayRecordMapper;
    
    @Transactional
    @Override
    public void batchInsert(List<ChannelEmployeePromotionDayRecord> channelEmployeePromotionDayRecords) {
        channelEmployeePromotionDayRecordMapper.batchInsert(channelEmployeePromotionDayRecords);
    }
    
    @Slave
    @Override
    public List<ChannelEmployeePromotionDayRecord> queryList(long startTime, long endTime, Long offset, Long size) {
        return channelEmployeePromotionDayRecordMapper.queryList(startTime, endTime, offset, size);
    }
    
    @Slave
    @Override
    public List<ChannelEmployeePromotionDayRecord> queryListByFeeDate(long startTime, long endTime, Integer tenantId, Long franchiseeId) {
        return channelEmployeePromotionDayRecordMapper.selectListByFeeDate(startTime, endTime, tenantId, franchiseeId);
    }
}
