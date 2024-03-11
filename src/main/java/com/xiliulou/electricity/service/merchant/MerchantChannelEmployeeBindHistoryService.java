package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantChannelEmployeeBindHistory;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeMerchantNumQueryModel;

import java.util.List;

/**
 * @ClassName : MerchantChannelEmployeeBindHistoryService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-03-11
 */
public interface MerchantChannelEmployeeBindHistoryService {
    
    Integer insertOne(MerchantChannelEmployeeBindHistory merchantChannelEmployeeBindHistory);
    Integer updateUnbindTimeByMerchantUid(MerchantChannelEmployeeBindHistory merchantChannelEmployeeBindHistory);
    List<MerchantChannelEmployeeBindHistory> selectListByChannelEmployeeUid(Integer tenantId,Long channelEmployeeUid);
    
    Integer countMerchantNumByTime(MerchantPromotionFeeMerchantNumQueryModel todayQueryModel);
    
    MerchantChannelEmployeeBindHistory queryByMerchantUid(Integer tenantId,Long merchantUid);
}
