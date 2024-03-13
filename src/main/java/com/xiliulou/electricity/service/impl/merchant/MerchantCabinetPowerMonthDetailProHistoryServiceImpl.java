package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetPowerMonthDetailProHistory;
import com.xiliulou.electricity.mapper.merchant.MerchantCabinetPowerMonthDetailProHistoryMapper;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthDetailProHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HeYafeng
 * @description 小程序月度电费详情-柜机详情
 * @date 2024/2/21 20:11:40
 */
@Service
public class MerchantCabinetPowerMonthDetailProHistoryServiceImpl implements MerchantCabinetPowerMonthDetailProHistoryService {
    
    @Resource
    private MerchantCabinetPowerMonthDetailProHistoryMapper merchantCabinetPowerMonthDetailProHistoryMapper;
    
    @Slave
    @Override
    public List<MerchantCabinetPowerMonthDetailProHistory> listByMonth(Long cabinetId, List<String> monthList, Long merchantId) {
        return merchantCabinetPowerMonthDetailProHistoryMapper.selectListByMonth(cabinetId, monthList, merchantId);
    }
}
