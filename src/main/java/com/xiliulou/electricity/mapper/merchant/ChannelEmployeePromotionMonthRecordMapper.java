package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.ChannelEmployeePromotionMonthRecord;
import com.xiliulou.electricity.query.merchant.ChannelEmployeePromotionQueryModel;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeePromotionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/21 13:56
 * @desc
 */
public interface ChannelEmployeePromotionMonthRecordMapper {
    
    void batchInsert(@Param("list") List<ChannelEmployeePromotionMonthRecord> list);
    
    List<ChannelEmployeePromotionVO> selectListByPage(ChannelEmployeePromotionQueryModel queryModel);
    
    Integer countTotal(ChannelEmployeePromotionQueryModel channelEmployeePromotionQueryModel);
    
    List<ChannelEmployeePromotionMonthRecord> selectByFeeDate(@Param("tenantId") Integer tenantId, @Param("monthDate") String monthDate,@Param("franchiseeIdList") List<Long> franchiseeIdList);
}
