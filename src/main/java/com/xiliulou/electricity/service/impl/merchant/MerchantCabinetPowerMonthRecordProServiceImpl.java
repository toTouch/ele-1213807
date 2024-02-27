package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.electricity.mapper.merchant.MerchantCabinetPowerMonthRecordProMapper;
import com.xiliulou.electricity.service.merchant.MerchantCabinetPowerMonthRecordProService;
import com.xiliulou.electricity.vo.merchant.MerchantPowerDetailVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HeYafeng
 * @description 商户电费业务（小程序端）
 * @date 2024/2/26 03:33:41
 */
@Service
public class MerchantCabinetPowerMonthRecordProServiceImpl implements MerchantCabinetPowerMonthRecordProService {
    
    @Resource
    private MerchantCabinetPowerMonthRecordProMapper merchantCabinetPowerMonthRecordProMapper;
    
    @Override
    public MerchantPowerDetailVO sumMonthPower(List<Long> cabinetIds, List<String> monthDateList) {
        return merchantCabinetPowerMonthRecordProMapper.sumMonthPower(cabinetIds, monthDateList);
    }
}
