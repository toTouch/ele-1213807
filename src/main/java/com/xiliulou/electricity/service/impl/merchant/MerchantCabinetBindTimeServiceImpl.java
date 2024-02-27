package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindTime;
import com.xiliulou.electricity.mapper.merchant.MerchantCabinetBindTimeMapper;
import com.xiliulou.electricity.service.merchant.MerchantCabinetBindTimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/26 18:45
 * @desc
 */
@Service("merchantCabinetBindTimeService")
@Slf4j
public class MerchantCabinetBindTimeServiceImpl implements MerchantCabinetBindTimeService {
    @Resource
    private MerchantCabinetBindTimeMapper merchantCabinetBindTimeMapper;
    
    @Override
    public List<MerchantCabinetBindTime> queryListByMerchantId(Long merchantId, Long cabinetId, Long placeId) {
        return merchantCabinetBindTimeMapper.queryListByMerchantId(merchantId, cabinetId, placeId);
    }
}
