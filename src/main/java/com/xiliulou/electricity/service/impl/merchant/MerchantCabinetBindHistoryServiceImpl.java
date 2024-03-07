package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindHistory;
import com.xiliulou.electricity.mapper.merchant.MerchantCabinetBindHistoryMapper;
import com.xiliulou.electricity.service.merchant.MerchantCabinetBindHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/26 14:20
 * @desc
 */
@Service("merchantCabinetBindHistoryService")
@Slf4j
public class MerchantCabinetBindHistoryServiceImpl implements MerchantCabinetBindHistoryService {
    @Resource
    private MerchantCabinetBindHistoryMapper merchantCabinetBindHistoryMapper;
    
    @Slave
    @Override
    public List<MerchantCabinetBindHistory> queryListByMonth(Long cabinetId, Long placeId, List<String> monthList, Long merchantId) {
        return merchantCabinetBindHistoryMapper.queryListByMonth(cabinetId, placeId, monthList, merchantId);
    }
}
