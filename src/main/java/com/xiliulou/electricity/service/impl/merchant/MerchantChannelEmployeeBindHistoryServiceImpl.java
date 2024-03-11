package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantChannelEmployeeBindHistory;
import com.xiliulou.electricity.mapper.merchant.MerchantChannelEmployeeBindHistoryMapper;
import com.xiliulou.electricity.query.merchant.MerchantPromotionFeeMerchantNumQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantChannelEmployeeBindHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : MerchantChannelEmployeeBindHistoryServiceImpl
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-03-11
 */
@Service
public class MerchantChannelEmployeeBindHistoryServiceImpl implements MerchantChannelEmployeeBindHistoryService {
    
    @Resource
    private MerchantChannelEmployeeBindHistoryMapper merchantChannelEmployeeBindHistoryMapper;
    
    @Override
    public Integer insertOne(MerchantChannelEmployeeBindHistory merchantChannelEmployeeBindHistory) {
        return merchantChannelEmployeeBindHistoryMapper.insertOne(merchantChannelEmployeeBindHistory);
    }
    
    @Override
    public Integer updateUnbindTimeByMerchantUid(MerchantChannelEmployeeBindHistory merchantChannelEmployeeBindHistory) {
        return merchantChannelEmployeeBindHistoryMapper.updateUnbindTimeByMerchantUid(merchantChannelEmployeeBindHistory);
    }
    
    @Override
    @Slave
    public List<MerchantChannelEmployeeBindHistory> selectListByChannelEmployeeUid(Integer tenantId, Long channelEmployeeUid) {
        return merchantChannelEmployeeBindHistoryMapper.selectListByChannelEmployeeUid(tenantId, channelEmployeeUid);
    }
    
    @Slave
    @Override
    public Integer countMerchantNumByTime(MerchantPromotionFeeMerchantNumQueryModel queryModel) {
        return merchantChannelEmployeeBindHistoryMapper.countMerchantNumByTime(queryModel);
    }
    
    @Override
    public MerchantChannelEmployeeBindHistory queryByMerchantUid(Integer tenantId, Long merchantUid) {
        return merchantChannelEmployeeBindHistoryMapper.selectByMerchantUid(tenantId, merchantUid);
    }
}
