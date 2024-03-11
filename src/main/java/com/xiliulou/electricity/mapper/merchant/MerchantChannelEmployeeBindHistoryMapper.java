package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantChannelEmployeeBindHistory;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeMerchantNumQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName : MerchantChannelEmployeeBindHistory
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-03-11
 */
public interface MerchantChannelEmployeeBindHistoryMapper {
    
    Integer insertOne(MerchantChannelEmployeeBindHistory merchantChannelEmployeeBindHistory);
    
    Integer updateUnbindTimeByMerchantUid(MerchantChannelEmployeeBindHistory merchantChannelEmployeeBindHistory);
    
    List<MerchantChannelEmployeeBindHistory> selectListByChannelEmployeeUid(@Param("tenantId") Integer tenantId,@Param("channelEmployeeUid") Long channelEmployeeuid);
    
    Integer countMerchantNumByTime(MerchantPromotionFeeMerchantNumQueryModel queryModel);
    
    MerchantChannelEmployeeBindHistory selectByMerchantUid(@Param("tenantId")Integer tenantId, @Param("merchantUid")Long merchantUid);
    
}
