package com.xiliulou.electricity.service.merchant;


import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionDayRecord;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/21 14:03
 * @desc
 */
public interface ChannelEmployeePromotionDayRecordService {
    
    void batchInsert(List<ChannelEmployeePromotionDayRecord> channelEmployeePromotionDayRecords);
    
    List<ChannelEmployeePromotionDayRecord> queryList(long startTime, long endTime, Long offset, Long size);
    
    List<ChannelEmployeePromotionDayRecord> queryListByFeeDate(long startTime, long endTime, Integer tenantId, List<Long> franchiseeIdList);
}
