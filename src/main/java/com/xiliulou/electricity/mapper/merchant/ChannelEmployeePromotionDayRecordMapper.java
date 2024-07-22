package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionDayRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/21 13:57
 * @desc
 */
public interface ChannelEmployeePromotionDayRecordMapper {
    
    void batchInsert(List<ChannelEmployeePromotionDayRecord> channelEmployeePromotionDayRecords);
    
    List<ChannelEmployeePromotionDayRecord> queryList(@Param("startTime") long startTime,@Param("endTime") long endTime
            ,@Param("offset") Long offset,@Param("size") Long size);
    
    List<ChannelEmployeePromotionDayRecord> selectListByFeeDate(@Param("startTime") long startTime,@Param("endTime") long endTime,
            @Param("tenantId") Integer tenantId,@Param("franchiseeIdList") List<Long> franchiseeIdList);
}
